package characters;

import javafx.scene.image.Image;
import platformcontrol.GameState;

/**
 *
 * @author DP
 */
public class SnailEnemy extends Entity{
    boolean hitObject;
    
    /**
     * Sets health and damage for snail enemies.
     * 
     * @param x
     *          Starting X-coordinate
     * @param y
     *          Starting Y-coordinate
     * @param world 
     *          The current GameState
     */
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
        enemyDamage = 50;
        flinchImage = sprites.get(currentAction)[0];
        this.setImage(sprites.get(currentAction)[0]);
    }

    @Override
    public void playDeathTone() {
        SoundEffect.DEATH.play();
    }
    
    @Override
    public void playFlinchTone() {
        SoundEffect.INSECT_GRUNT.play();
    }
    
}
