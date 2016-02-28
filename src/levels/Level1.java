package levels;

import characters.Player;
import characters.SnailEnemy;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import platformcontrol.GameState;
import platformcontrol.GameStateManager;

public class Level1 extends GameState{
    
    public Level1(GameStateManager gsm){
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
    
    @Override
    public void initObjects(){
        double snail1StartX = GameState.MAP_TILE_SIZE*4;
        double snail1StartY = h - GameState.MAP_TILE_SIZE - GameState.PLAYER_SIZE;
        SnailEnemy snail_1 = new SnailEnemy(snail1StartX, snail1StartY, this);
        enemies.getChildren().add(snail_1);
        entities.getChildren().add(enemies);
        
        //Set start location
        double playerStartX = GameState.MAP_TILE_SIZE*2;
        double playerStartY = h - GameState.MAP_TILE_SIZE - GameState.PLAYER_SIZE;
        //Making the player also adds the keyListener to the gameState
        player = new Player(this, playerStartX, playerStartY);
        entities.getChildren().add(player);//Player is added last so the image is
                                           //on top of all enemies
        
        try {
            InputStream in = this.getClass().getResourceAsStream("/levelresources/Level1.map");
            initMap(in);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        this.getChildren().addAll(entities, map);
    }
    
    public void runGame(){
        player.updateEntity();
        //moveEnemies();
    }
}