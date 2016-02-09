package levels;

import characters.Player;
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
        h = gsm.height - 0.25*GameState.ENTITY_SIZE;
        setHeight(h);
        setWidth(w);
        
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
        //Making the player also adds the keyListener to the gameState
        player = new Player(this);
        //Set start location
        player.setX(player.getFitWidth()*1.5);
        player.setY(h - GameState.ENTITY_SIZE*3.5);
        characters.getChildren().add(player);
        
        try {
            InputStream in = this.getClass().getResourceAsStream("/levelresources/Level1.map");
            initMap(in);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        this.getChildren().addAll(characters, map);
    }
    
    public void runGame(){
        player.updatePlayer();
        //moveEnemies();
    }
}