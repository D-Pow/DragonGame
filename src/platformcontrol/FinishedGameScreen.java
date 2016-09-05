package platformcontrol;

import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import levels.MusicPlayer;
import platformcontrol.GameStateManager.StateType;

/**
 * This screen shows once the player has completed all levels in the game.
 *
 * @author dPow
 */
public class FinishedGameScreen extends GameState {
    GameStateManager gsm;
    Image bg;
    Background background;
    
    /**
     * Sets text and key listener.
     * 
     * @param gsm 
     *          The application's GameStateManager
     */
    public FinishedGameScreen(GameStateManager gsm) {
        this.gsm = gsm;
        this.w = gsm.width;
        this.h = gsm.height;
        
        bg = new Image("/levelresources/MenuBG.png", gsm.width, gsm.height, false, false);
        
        background = new Background(new BackgroundImage(bg,
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                null, null));
        this.setBackground(background);
        
        initKeyListener();
        initObjects();
        MusicPlayer.happySong.playSong();
    }

    @Override
    public void initObjects() {
        Text message = new Text("You won!\nPress Enter to return "
                + "to the main menu");
        Font font = new Font("vernanda", 40);
        message.setFont(font);
        message.setFill(Color.RED);
        message.setTextAlignment(TextAlignment.CENTER);
        double messageW = message.getLayoutBounds().getWidth();
        double messageH = message.getLayoutBounds().getHeight();
        message.setX((w - messageW) / 2);
        message.setY((h - messageH) / 2);
        this.getChildren().add(message);
    }
    
    private void initKeyListener() {
        this.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent e)-> {
            if (e.getCode() == KeyCode.ENTER) {
                gsm.changeState(StateType.MENU);
            }
        });
    }
    
}
