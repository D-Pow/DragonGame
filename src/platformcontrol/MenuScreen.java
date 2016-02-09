package platformcontrol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class MenuScreen extends GameState {
    private final GameStateManager gsm;
    private double w;
    private double h;
    private int currentSelection;
    private int lastSelection;
    private Image bg;
    private Background background;
    
    public MenuScreen(GameStateManager gsm){
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

    @Override
    public void initObjects() {
        List<String> options = new ArrayList<>();
        options.addAll(Arrays.asList("Exit", "Help", "Start"));
        
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
    }
    
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
        switch (message.getText()){
            case "Start":
                gsm.changeState(GameStateManager.StateType.LOAD);
                break;
            case "Help":
                StackPane root = new StackPane();
                root.setBackground(background);
                root.getChildren().add(new ImageView("/levelresources/Controls.png"));
                Scene scene = new Scene(root, w, h);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.show();
                stage.setOnCloseRequest(null);
                break;
            case "Exit":
                System.exit(0);
                break;
            default:
                //Do nothing
        }
    }
}