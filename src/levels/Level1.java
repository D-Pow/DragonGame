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

public class Level1 extends GameState{
    
    public Level1(GameStateManager gsm){
        super(gsm);
    }
    
    @Override
    public void initObjects(){
        //Enemies are initiated and located in initMap() below
        
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