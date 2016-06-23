package levels;

import characters.Player;
import java.io.InputStream;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import platformcontrol.GameState;
import platformcontrol.GameStateManager;

public class Level2 extends GameState{
    
    public Level2(GameStateManager gsm){
        super(gsm);
    }
    
    @Override
    public void initObjects(){
        //Set start location
        double playerStartX = GameState.PLAYER_SIZE*1.5;
        double playerStartY = h - GameState.PLAYER_SIZE*3.5;
        //Making the player also adds the keyListener to the gameState
        player = new Player(this, playerStartX, playerStartY);
        entities.getChildren().add(player);
        
        try {
            InputStream in = this.getClass().getResourceAsStream("/levelresources/Level2.map");
            initMap(in);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        this.getChildren().addAll(entities, map);
        entities.getChildren().add(enemies);
        
        //Set a background that is the window's size
        Image background = new Image("/levelresources/Level1background.png",
                gsm.width, gsm.height, false, true);
        BackgroundImage backgroundImage = new BackgroundImage(background,
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        Background bg = new Background(backgroundImage);
        this.setBackground(bg);
    }
    
}