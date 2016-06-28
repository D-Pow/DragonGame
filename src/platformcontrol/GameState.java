package platformcontrol;

import characters.Entity;
import characters.Player;
import characters.SnailEnemy;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import platformcontrol.GameStateManager.StateType;

abstract public class GameState extends Pane{
    public GameStateManager gsm;
    protected double w;//Used only for initObjects
    protected double h;//Used only for initObjects
    
    //GameMap and TileSet
    //int[row][col] i.e. int[y][x]
    public double mapX; //Top left corner of map. Used to relocate the images.
    public double mapY; //Top left corner of map. Used to relocate the images.
    public double mapWidth;
    public Group map = new Group(); //Used to hold ImageViews for the levels
    public List<Integer> mapTileNumbers; //Used to get tile # for map group
                                         //in order to know if tile is ghost tile or not
                                         //to calculate character collisions
    protected int[][] mapTiles; //Raw tile numbers in the map
    protected Image[][] tileSet; //Used in putting images on the screen
    protected int tileSize;
    protected int numTileColumns;
    public int numDecorationTiles; //Number of tiles to not include in entity collision
    
    //Thread the game runs on
    public static Thread gameThread;
    //Used to check if game is paused
    public boolean running;
    //Synchronized thread lock monitor for pausing game
    public final Object threadLockMonitor; //
    
    //Characters
    public Player player;
    public Group entities = new Group();
    public Group enemies = new Group();
    
    //Used in Entity class for the death sequence
    public Image blankTile;
    
    private static final int NUMTILEROWS = 2;
    public static final int PLAYER_SIZE = 80;
    public static final int ENEMY_SIZE = 50;
    public static final int MAP_TILE_SIZE = 50;
    public static final List<Integer> WINNING_TILES = new ArrayList<>();
    public static final List<Integer> ENEMY_TILES = new ArrayList<>();
    public static final int PLAYER_TILE = 25;
    
    /**
     * Constructor used in MenuScreen/LoadScreen classes.
     */
    public GameState(){
        threadLockMonitor = new Object();
    }
    
    /**
     * Constructor used in Level classes.
     * 
     * @param gsm
     *          GameStateManager used for the game
     */
    public GameState(GameStateManager gsm){
        this.gsm = gsm;
        //w and h are only necessary for initObjects(), not for
        //player and enemy movement
        w = gsm.width;
        h = gsm.height - 0.25*GameState.PLAYER_SIZE; //positioning the map inside
                                                     //inside the stage perfectly
        WINNING_TILES.add(15);
        WINNING_TILES.add(16);
        ENEMY_TILES.add(26);
        //Synchronized thread lock monitor for pausing game
        threadLockMonitor = new Object();
        setHeight(h);
        setWidth(w);
        
        initObjects();
        
        running = true;
        GameState.gameThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    synchronized (threadLockMonitor) {
                        if (!running) {
                            try {
                                threadLockMonitor.wait();
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                        //if running
                        try {
                            Platform.runLater(() -> runGame());
                            TimeUnit.MILLISECONDS.sleep(10);
                            threadLockMonitor.notify();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
        
        GameState.gameThread.start();
    }
    
    /**
     * Basically, only lets levels choose their background image.
     */
    abstract public void initObjects();
    
    /**
     * Loads the map for the given level by calling
     * all appropriate methods.
     * 
     * @param in 
     *          InputStream for .map file
     */
    public final void initMap(InputStream in){
        loadTiles();       //Put tiles from sheet into Image matrix
        loadMapSheet(in); //Load the saved matrix of int values from resources
        loadMap();       //Make images for each tile and put them on screen
        mapWidth = mapTiles[0].length*MAP_TILE_SIZE;
    }
    
    /**
     * Updates every entity in the game.
     */
    public void runGame(){
        player.updateEntity();
        for (Node n : enemies.getChildren()){
            ((Entity) n).updateEntity();
        }
    }
    
    /**
     * Pauses and unpauses the game by halting and releasing
     * the game thread lock monitor.
     */
    public void pauseGame() {
        if (running) {
            togglePauseMenu();
            running = false;
        } else {
            synchronized (threadLockMonitor) {
                togglePauseMenu();
                running = true;
                threadLockMonitor.notifyAll();
            }
        }
    }
    
    /**
     * Displays and hides the pause menu.
     */
    private void togglePauseMenu() {
        FadeTransition fade = new FadeTransition(Duration.millis(500), this);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
        
        if (running) {
            Rectangle screenCover = new Rectangle(gsm.width, gsm.height);
            screenCover.setFill(new Color(0,0,0,0.5));
            this.getChildren().add(screenCover);

            String[] options = new String[]{
                                "Backspace: Main Menu",
                                "Enter: Resume"};
            for (int i = 0; i < options.length; i++) {
                Text message = new Text(options[i]);
                Font font = new Font("vernanda", 40);
                message.setFont(font);
                message.setFill(Color.RED);
                message.setTextAlignment(TextAlignment.CENTER);
                double messageW = message.getLayoutBounds().getWidth();
                double messageH = message.getLayoutBounds().getHeight();
                message.setX((w - messageW) / 2);
                message.setY(0.6*h - (messageH * 2 + messageH * i));
                this.getChildren().add(message);
            }
        } else {
            int size = this.getChildren().size();
            this.getChildren().remove(size-3, size);
        }
    }
    
    /**
     * Resets the current level.
     */
    public void reset(){
        gameThread.stop(); //Necessary to prevent lagging
        gsm.changeState(gsm.getCurrentState());
    }
    
    /**
     * Returns the user back to the main menu.
     */
    public void quit() {
        gsm.changeState(StateType.MENU);
    }
    
    /**
     * Shows a win message and changes to next level.
     */
    public void win(){
        GameState.gameThread.stop();
        
        Text winMessage = new Text("Completed " + gsm.getCurrentState() + "!"
                + "\nPress Shift to Continue");
        Font font = new Font("vernanda", 40);
        winMessage.setFont(font);
        winMessage.setFill(Color.RED);
        winMessage.setTextAlignment(TextAlignment.CENTER);
        double messageW = winMessage.getLayoutBounds().getWidth();
        double messageH = winMessage.getLayoutBounds().getHeight();
        winMessage.setX((w - messageW) / 2);
        winMessage.setY((h - messageH) / 2);

        getChildren().add(winMessage);
        
        this.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent e)-> {
            if (e.getCode() == KeyCode.SHIFT) {
                gsm.changeState();
            }
        });
    }
    
    /**
     * Moves the image matrix (map) in the opposite direction
     * that the player is moving. Also, moves the enemies along with
     * the map so that the whole world moves for the player.
     * 
     * @param direction
     *          "Left" or "Right" as specified by Player class
     * @param moveSpeed 
     *          How fast the map should move as specified by Player class
     */
    public void moveMap(String direction, int moveSpeed){
        switch(direction){
            case "Left":
                //Do nothing; moveSpeed from Player is already positive
                break;
            case "Right":
                moveSpeed *= -1;
                break;
        }
        
        mapX += moveSpeed;
        
        for (Node n : map.getChildren()){
            ImageView tile = (ImageView) n;
            tile.setX(tile.getX() + moveSpeed);
        }
        
        for (Node m : enemies.getChildren()){
            ImageView enemy = (ImageView) m;
            enemy.setX(enemy.getX() + moveSpeed);
        }
    }
    
    /**
     * Converts the mapTiles matrix of int values to a
     * matrix of images (map). Player and enemy tiles are used to place
     * respective entities on the map, and then a blank tile is put on
     * the map in their location.
     */
    public final void loadMap(){
        mapTileNumbers = new ArrayList();
        int tileRow;
        int tileCol;
        int tile;
        //The map should start with the bottom-left corner in the screen,
        //so the map is initiated with the lowest row first, and the rest later.
        for (int row = mapTiles.length - 1; row >= 0; row--){
            for (int col = 0; col < mapTiles[row].length; col++){
                //Pull tile number from the map matrix
                tile = mapTiles[row][col];
                //If the tile number is in the array of enemy tiles, then
                //it represents an enemy location. Send the level the
                //corresponging location for the enemy.
                if (ENEMY_TILES.contains(tile)){
                    switch(tile){
                        case 26: //Snail Enemy
                            double snailStartX = GameState.MAP_TILE_SIZE*col;
                            double snailStartY = GameState.MAP_TILE_SIZE*(row-1);
                            SnailEnemy snail = new SnailEnemy(snailStartX, snailStartY, this);
                            enemies.getChildren().add(snail);
                            break;
                    }
                    //Make the map be filled with a blank tile in place of
                    //the enemy tile
                    tile = 0;
                }
                
                //If the tile is the player, set the start location
                if (tile == PLAYER_TILE) {
                    double playerStartX = GameState.MAP_TILE_SIZE * col;
                    double playerStartY = GameState.MAP_TILE_SIZE * (row - 1);
                    player = new Player(this, playerStartX, playerStartY);
                    tile = 0;
                }
                
                //Convert the single integer into a row/column location
                tileRow = 0;
                tileCol = tile;
                while (tileCol > numTileColumns - 1){
                    tileCol -= numTileColumns;
                    tileRow++;
                }
                //Get the correct tile image
                Image selectedTile = tileSet[tileRow][tileCol];
                //Put it in the map group with the right location
                ImageView image = new ImageView(selectedTile);
                image.setFitHeight(MAP_TILE_SIZE);
                image.setFitWidth(MAP_TILE_SIZE);
                
                //Place tiles with the bottom-left picture in the
                //bottom-left of the screen
                int y = (mapTiles.length - row)*MAP_TILE_SIZE;
                image.setY(getHeight() - y);
                image.setX(col*MAP_TILE_SIZE);
                
                //If it's the top-left image, set mapX and mapY
                if (row == 0 && col == 0){
                    mapX = image.getX();
                    mapY = image.getY();
                }
                
                mapTileNumbers.add(tile);
                map.getChildren().add(image);
            }
        }
        blankTile = tileSet[0][0];
        
        //Add enemies and player last
        entities.getChildren().addAll(enemies, player);
        this.getChildren().addAll(map, entities);
    }//End loadMap()
    
    /**
     * Loads the .map file (holding int matrix) associated with the given level.
     * Map file must be loaded as input stream.
     * 
     * @param in The input stream of the .map file describing
     *           which tile goes where.
     */
    public final void loadMapSheet(InputStream in){
        try{
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(isr);
            
            //Map files intentionally stored with row length in the first line
            //and column length in the second line.
            //Length = number of tiles
            int mapHeight = Integer.parseInt(reader.readLine());
            int mapWidth = Integer.parseInt(reader.readLine());
            mapTiles = new int[mapHeight][mapWidth];
            for (int row = 0; row < mapHeight; row++){
                String numberLine = reader.readLine();
                String[] nums = numberLine.split("\\s+");
                for (int col = 0; col < mapWidth; col++){
                    int tile = Integer.parseInt(nums[col]);
                    mapTiles[row][col] = tile;
                }
            }
            
            reader.close();
            isr.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * Initializes the set of tile images by making the matrix index
     * correspond to a given tile image.
     */
    public final void loadTiles(){
        try{
            Image image =
                    new Image(this.getClass().getResourceAsStream("/levelresources/TileSet.png"));
            BufferedImage tileImage = SwingFXUtils.fromFXImage(image, null);
            tileSize = Math.round(tileImage.getHeight()/NUMTILEROWS);
            numTileColumns = Math.round(tileImage.getWidth()/tileSize);
            tileSet = new Image[NUMTILEROWS][numTileColumns];
            for (int row = 0; row < NUMTILEROWS; row++){
                for (int col = 0; col < numTileColumns; col++){
                    BufferedImage temp;
                    temp = tileImage.getSubimage(col*tileSize, row*tileSize,
                            tileSize, tileSize);
                    tileSet[row][col] = SwingFXUtils.toFXImage(temp, null);
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        numDecorationTiles = numTileColumns;
    }
}
