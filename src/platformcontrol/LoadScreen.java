package platformcontrol;

import java.util.ArrayList;
import java.util.List;
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
import platformcontrol.GameStateManager.StateType;

public class LoadScreen extends GameState {
    private final GameStateManager gsm;
    private double w;
    private double h;
    private int currentSelection;
    private int lastSelection;
    private Image bg;
    private Background background;
    
    /**
     * Sets background and initializes options and key listener.
     * 
     * @param gsm 
     *          The application's GameStateManager
     */
    public LoadScreen(GameStateManager gsm){
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
    }

    /**
     * Puts the options on the screen.
     */
    @Override
    public void initObjects() {
        int levelNumber = gsm.loadSave();
        List<String> options = new ArrayList<>();
        for (int i = levelNumber; i > 0; i--){
            String option = "Level " + String.valueOf(i);
            options.add(option);
        }
        
        for (int i = 0; i < options.size(); i++){
            Text message = new Text(options.get(i));
            Font font = new Font("vernanda", 40);
            message.setFont(font);
            message.setFill(Color.BLUE);
            message.setTextAlignment(TextAlignment.CENTER);
            double messageW = message.getLayoutBounds().getWidth();
            double messageH = message.getLayoutBounds().getHeight();
            message.setX((w - messageW)/2);
            message.setY(h - (messageH*2 + messageH*i));            
            this.getChildren().add(message);
        }
        
        currentSelection = this.getChildren().size() - 1;
        selectionUpdate();
    }//End initObjects()
    
    /**
     * Adds a key listener to change selection and activate it.
     */
    public void initKeyListener() {
        this.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent e) -> {
            if (e.getCode() == KeyCode.UP &&
                    currentSelection < this.getChildren().size() - 1){
                lastSelection = currentSelection;
                currentSelection++;
                selectionUpdate();
            }
            else if (e.getCode() == KeyCode.DOWN &&
                    currentSelection >= 0){
                lastSelection = currentSelection;
                currentSelection--;
                selectionUpdate();
            }
            
            if (e.getCode() == KeyCode.ENTER){
                activate();
            }
        });
    }
    
    /**
     * Changes the colors of the selected and no-longer selected options.
     */
    private void selectionUpdate(){
        Text activeMessage = (Text) this.getChildren().get(currentSelection);
        activeMessage.setFill(Color.RED);
        Text inactiveMessage = (Text) this.getChildren().get(lastSelection);
        inactiveMessage.setFill(Color.BLUE);
    }
    
    /**
     * Activates the selected option.
     */
    private void activate(){
        Text message = (Text) this.getChildren().get(currentSelection);
        String text = message.getText();
        String len = "LEVEL ";
        String levelNumber = text.substring(len.length());
        int chosen = Integer.valueOf(levelNumber);
        gsm.changeState(StateType.valueOf("LEVEL" + String.valueOf(chosen)));
    }
    
}