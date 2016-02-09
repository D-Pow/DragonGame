package platformcontrol;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
    
    public String loadSave(){
        String level = null;
        try(BufferedReader reader = new BufferedReader(new FileReader("./DragonSave.data"))){
            level = reader.readLine();
        } catch (IOException ex) {
            //Do nothing
        }
        
        return level;
    }

    @Override
    public void initObjects() {
        String level = loadSave();
        if (level == null){
            gsm.changeState(StateType.LEVEL1);
        }
        else{
            String lastLetter = level.substring(level.length() - 1);
            int max = Integer.valueOf(lastLetter);
            List<String> options = new ArrayList<>();
            for (int i = max; i > 0; i--){
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
        }//End else
    }//End initObjects()
    
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
    
    private void selectionUpdate(){
        Text activeMessage = (Text) this.getChildren().get(currentSelection);
        activeMessage.setFill(Color.RED);
        Text inactiveMessage = (Text) this.getChildren().get(lastSelection);
        inactiveMessage.setFill(Color.BLUE);
    }
    
    private void activate(){
        Text message = (Text) this.getChildren().get(currentSelection);
        String text = message.getText();
        String lastLetter = text.substring(text.length() - 1);
        int chosen = Integer.valueOf(lastLetter);
        gsm.changeState(StateType.valueOf("LEVEL" + String.valueOf(chosen)));
    }
    
}