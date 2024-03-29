package platformcontrol;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Simple platformer where you play as a Dragon.
 * The arrow keys highlight the options, and enter
 * selects the highlighted option.
 *
 * @author D-Pow
 */
public class Main extends Application {
    int width = 800;
    int height = 600;
    GameStateManager gsm;

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Dragon Game by D-Pow");
        stage.setWidth(width);
        stage.setHeight(height);
        stage.setResizable(false);
        stage.getIcons().add(new Image("/levelresources/DragonIcon.png"));
        
        gsm = new GameStateManager(stage);
        
        stage.show();
    }
    
    @Override
    public void stop(){
        System.exit(0);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
