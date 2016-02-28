package levels;

import characters.Player;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import platformcontrol.GameState;
import platformcontrol.GameStateManager;

public class Level2 extends GameState{
    
    public Level2(GameStateManager gsm){
        this.gsm = gsm;
        //w and h are only necessary for initObjects(), not for
        //player and enemy movement
        w = gsm.width;
        h = gsm.height;
        
        initObjects();
        
        running = true;
        Thread t = new Thread(new Runnable(){
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
        
        t.start();
    }
    
    @Override
    public void initObjects(){
        //Set start location
        double playerStartX = GameState.PLAYER_SIZE*1.5;
        double playerStartY = h - GameState.PLAYER_SIZE*3.5;
        //Making the player also adds the keyListener to the gameState
        player = new Player(this, playerStartX, playerStartY);
        entities.getChildren().add(player);
        
        this.getChildren().add(entities);
    }
    
    public void runGame(){
        player.updateEntity();
        //moveEnemies();
    }
}