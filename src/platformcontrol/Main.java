package platformcontrol;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author DP
 */
public class Main extends Application{
    int width = 800;
    int height = 600;
    GameStateManager gsm;

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("DragonGame");
        stage.setWidth(width);
        stage.setHeight(height);
        stage.setResizable(false);
        stage.getIcons().add(new Image("/characterimages/DragonIcon.png"));
        
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
