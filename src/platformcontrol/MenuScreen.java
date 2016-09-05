package platformcontrol;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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
    
    /**
     * Sets background and initializes options and key listener.
     * 
     * @param gsm 
     *          The application's GameStateManager
     */
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
    
    /**
     * Puts the options on the screen.
     */
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
        switch (message.getText()){
            case "Start":
                gsm.changeState(GameStateManager.StateType.LOAD);
                break;
            case "Help":
                openHelpDialog();
                break;
            case "Exit":
                System.exit(0);
                break;
            default:
                //Do nothing
        }
    }
    
    /**
     * Opens the help screen, showing controls and allowing
     * the user to view the credits.
     */
    private void openHelpDialog() {
        BorderPane root = new BorderPane();
        root.setBackground(background);
        root.setCenter(new ImageView("/levelresources/Controls.png"));

        //Add credits button
        Button creditsButton = new Button("Credits");
        creditsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showCredits();
            }
        });
        HBox hbox = new HBox();
        hbox.getChildren().add(creditsButton);
        hbox.setAlignment(Pos.BOTTOM_RIGHT);
        root.setBottom(hbox);

        Scene scene = new Scene(root, w * 0.75, h * 0.75);
        Stage stage = new Stage();
        stage.getIcons().add(new Image("/characterimages/DragonIcon.png"));
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        stage.setOnCloseRequest(null);
    }
    
    /**
     * Makes a temporary .txt file that contains the credits and
     * license information. File is deleted upon program termination.
     */
    private void showCredits() {
        try {
            InputStream credits = 
                    this.getClass().getResourceAsStream("/Credits for DragonGame.txt");
            BufferedReader reader = 
                    new BufferedReader(new InputStreamReader(credits));
            File tempFile = 
                    File.createTempFile("credits", ".txt", Paths.get(".").toFile());
            tempFile.deleteOnExit(); //Delete file after program termination
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                String line;
                do {
                    line = reader.readLine();
                    String lineToWrite = String.format(line + "%n");
                    writer.write(lineToWrite);
                } while (line != null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Desktop.getDesktop().open(tempFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}