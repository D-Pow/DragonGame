package characters;

import javafx.scene.image.Image;
import platformcontrol.GameState;

/**
 *
 * @author DP
 */
public class SnailEnemy extends Entity{
    boolean hitObject;
    
    public SnailEnemy(double x, double y, GameState world) {
        super((Image) null, world);
        sprites = (new SpriteManager()).getSnailSprites();
        setX(x);
        setY(y);
        setFitWidth(GameState.ENEMY_SIZE);
        setFitHeight(GameState.ENEMY_SIZE);
        direction = "Right";
        health = 100;
        moveSpeed = 1;
        System.out.println(currentAction);
        this.setImage(sprites.get(currentAction)[0]);
    }
    
}
