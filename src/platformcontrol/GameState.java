package platformcontrol;

import characters.Entity;
import characters.Player;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

abstract public class GameState extends Pane{
    protected GameStateManager gsm;
    protected double w;//Used only for initObjects
    protected double h;//Used only for initObjects
    public static Thread gameThread;
    
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
    
    //Used to check if game is paused
    protected boolean running;
    
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
    
    //Enemies
    private final int SNAIL_ENEMY = -1;
    
    /**
     * Constructor used in Menu/LoadScreen classes.
     */
    public GameState(){}
    
    /**
     * Constructor used in Level classes.
     * @param gsm GameStateManager used for the game
     */
    public GameState(GameStateManager gsm){
        this.gsm = gsm;
        //w and h are only necessary for initObjects(), not for
        //player and enemy movement
        w = gsm.width;
        h = gsm.height - 0.25*GameState.PLAYER_SIZE;
        setHeight(h);
        setWidth(w);
        
        initObjects();
        
        running = true;
        GameState.gameThread = new Thread(new Runnable(){
            @Override
            public void run() {
                while (true){
                    if (running){
                        try {
                            Platform.runLater(() -> runGame());
                            TimeUnit.MILLISECONDS.sleep(10);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
        
        GameState.gameThread.start();
    }
    
    abstract public void initObjects();
    
    public final void initMap(InputStream in){
        loadTiles();       //Put tiles from sheet into JavaFX matrix
        loadMapSheet(in); //Load the saved matrix of int values from resources
        loadMap();       //Make images for each tile and put them on screen
        mapWidth = mapTiles[0].length*MAP_TILE_SIZE;
    }
    
    public void runGame(){
        player.updateEntity();
        for (Node n : enemies.getChildren()){
            ((Entity) n).updateEntity();
        }
    }
    
    public void reset(){
        gameThread.stop(); //Necessary to prevent lagging
        gsm.changeState(gsm.getCurrentState());
    }
    
    public void moveMap(String direction, int moveSpeed){
        switch(direction){
            case "Left":
                //Do nothing; moveSpeed is already positive
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
     * matrix of images.
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
                //If the tile number is < 0, it represents an enemy
                //location. Send the level the corresponging location
                //for the enemy.
                /*
                ** Code to be implemented next update: This will make
                * Loading enemy locations much easier.
                if (tile < 0){
                    switch(tile){
                        case -1:
                            double snailStartX = GameState.MAP_TILE_SIZE * col;
                            double snailStartY = GameState.MAP_TILE_SIZE*row;
                            SnailEnemy snail = new SnailEnemy(snailStartX, snailStartY, this);
                            enemies.getChildren().add(snail);
                            break;
                    }
                    tile = 0;
                }
                */
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
    }//End loadMap()
    
    /**
     * Loads the .mapTiles file associated with the platformer.
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
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * Initializes the set of tile images
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
