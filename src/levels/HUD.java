package levels;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import platformcontrol.GameState;
import static platformcontrol.GameState.MAP_TILE_SIZE;

/**
 * This class serves to show the user the health and fire energy of the dragon.
 *
 * @author dPow
 */
public class HUD extends Group {
    ImageView hudImage;
    Text healthText;
    Text fireText;
    int health;
    int maxHealth;
    int fire;
    int maxFire;
    
    /**
     * Creates the initial HUD display.
     * @param world
     */
    public HUD(GameState world) {
        Image hudImage = new Image("/levelresources/hud.png");
        this.hudImage = new ImageView(hudImage);
        this.hudImage.setX(0);
        this.hudImage.setY(10);
        this.hudImage.setFitWidth(MAP_TILE_SIZE * 2);
        this.hudImage.setFitHeight(MAP_TILE_SIZE);
        
        Font textFont = new Font("vernanda", 16);
        healthText = new Text("0/0");
        healthText.setFont(textFont);
        healthText.setX(this.hudImage.getX() + hudImage.getWidth()*1/3);
        healthText.setY(this.hudImage.getY() + hudImage.getHeight()*2/5);
        
        fireText = new Text("0/0");
        fireText.setFont(textFont);
        fireText.setX(this.hudImage.getX() + hudImage.getWidth()*1/3);
        fireText.setY(this.hudImage.getY() + hudImage.getHeight() + 3);
        this.getChildren().addAll(this.hudImage, healthText, fireText);
    }
    
    /**
     * Updates the health and fire energy markers on the HUD.
     * 
     * @param playerHealth
     *          The player's current health
     * @param playerMaxHealth
     *          The player's current max health
     * @param playerFire
     *          The player's current fire energy
     * @param playerMaxFire
     *          The player's current max fire energy
     */
    public void updateHUD(int playerHealth, int playerMaxHealth,
                          int playerFire, int playerMaxFire) {
        healthText.setText(playerHealth + "/" + playerMaxHealth);
        fireText.setText(playerFire + "/" + playerMaxFire);
    }

}
