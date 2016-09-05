package characters;

import java.util.ArrayList;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import platformcontrol.GameState;

/**
 *
 * @author DP
 */
public class Fireball extends Entity {
    int speed;
    ArrayList<Image[]> fireballSprites;
    boolean hitObject;
    public boolean dissipated;

    public Fireball(String direction, int Speed, GameState world) {
        super((Image) null, world);
        this.direction = direction;
        setFitWidth(GameState.PLAYER_SIZE / 2);
        setFitHeight(GameState.PLAYER_SIZE / 2);
        if (direction.equals("Left")) {
            //Still need to set fireball's position
            setX(world.player.getX() - getFitWidth()/2);
        }
        if (direction.equals("Right")) {
            //Still need to set fireball's position
            setX(world.player.getX() + getFitWidth());
        }
        setY(world.player.getY() + GameState.PLAYER_SIZE / 4);
        speed = Speed;
        SpriteManager sm = new SpriteManager();
        fireballSprites = sm.getFireballSprites();
        animationCycler = 0;
        Image startImage = fireballSprites.get(0)[animationCycler];
        setImage(startImage);
    }
    
    /**
     * Fireball's movement.
     */
    @Override
    public void updateEntity() {
        if (!hitObject) {
            if (direction.equals("Left")) {
                setX(getX() - speed);
            } else if (direction.equals("Right")) {
                setX(getX() + speed);
            }
            checkFireballCollision();
        }
        updateImage();
    }
    
    /**
     * Replaces the entity's checkMapCollision() method
     * because it needs to toggle if the fireball hits
     * any object, including an enemy.
     */
    public void checkFireballCollision() {
        //Check map collision
        for (Node n : world.map.getChildren()) {
            int tileIndex = world.map.getChildren().indexOf(n);
            int tileNumber = world.mapTileNumbers.get(tileIndex);
            if (tileNumber > world.numDecorationTiles - 1) {
                ImageView tile = (ImageView) n;
                //Delete fireball if it collides with map or
                //if it goes off-screen
                if (checkObjectCollision(this, tile) ||
                        getX() > world.getWidth() || getX() + getFitWidth() < 0) {
                    if (!hitObject) {
                        //Reset animation cycler only if the fireball newly
                        //collided with a map tile
                        animationCycler = 0; //Resets the sprite cycler to the first
                        //sprite to show the disipation of fireball
                    }
                    hitObject = true;
                    break;
                }
            }
        }

        for (Node m : world.enemies.getChildren()){
            Entity enemy = (Entity) m;
            if (checkObjectCollision(this, enemy)){
                hitObject = true;
                animationCycler = 0;
                enemy.health -= world.player.fireDamage;
                enemy.flinching = true;
                enemy.moving = false;
            }
        }
    }
    
    /**
     * Updates the sprite of the fireball. If the fireball is dissipated,
     * then it is removed in the Player.updateFireballs() method.
     */
    @Override
    public void updateImage() {
        //0 = Fireball is still active
        //1 = Fireball must dissipate
        timeToUpdateCycler++;
        //Fireball sprite should update faster than character sprites
        if (timeToUpdateCycler == UPDATE_TIME / 2) {
            int fireballState = 0;
            if (hitObject) {
                fireballState = 1;
            }
            setImage(fireballSprites.get(fireballState)[animationCycler]);
            animationCycler++;
            if (animationCycler >= fireballSprites.get(fireballState).length) {
                if (fireballState == 1) {
                    //Label fireball for destruction
                    dissipated = true;
                    //playDeathTone(); I think it's better without the dissipate sound
                }
                animationCycler = 0;
            }
            timeToUpdateCycler = 0;
        }
    }
    
    @Override
    public void playDeathTone() {
        SoundEffect.DISSIPATE.play();
    }

}
