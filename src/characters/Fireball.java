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
        setFitWidth(GameState.ENTITY_SIZE / 2);
        setFitHeight(GameState.ENTITY_SIZE / 2);
        if (direction.equals("Left")) {
            //Still need to set fireball's position
            setX(world.player.getX() - getFitWidth()/2);
        }
        if (direction.equals("Right")) {
            //Still need to set fireball's position
            setX(world.player.getX() + getFitWidth());
        }
        setY(world.player.getY() + GameState.ENTITY_SIZE / 4);
        speed = Speed;
        SpriteManager sm = new SpriteManager();
        fireballSprites = sm.getFireballSprites();
        animationCycler = 0;
        Image startImage = fireballSprites.get(0)[animationCycler];
        setImage(startImage);
    }

    public void updateFireball() {
        if (!hitObject) {
            if (direction.equals("Left")) {
                setX(getX() - speed);
            } else if (direction.equals("Right")) {
                setX(getX() + speed);
            }
        }
        checkFireballCollision();
        changeFireballSprite();
    }

    public void checkFireballCollision() {
        //Check map collision
        for (Node n : world.map.getChildren()) {
            int tileIndex = world.map.getChildren().indexOf(n);
            int tileNumber = world.mapTileNumbers.get(tileIndex);
            if (tileNumber > world.numDecorationTiles - 1) {
                ImageView tile = (ImageView) n;
                if (checkObjectCollision((ImageView) this, tile)) {
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

        //Check enemy collision
    }

    public void changeFireballSprite() {
        //0 = Fireball is still active
        //1 = Fireball must dissipated
        timeToUpdateCycler++;
        if (timeToUpdateCycler == UPDATE_TIME / 2) {//Make it update the sprite faster
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
                }
                animationCycler = 0;
            }
            timeToUpdateCycler = 0;
        }
    }

}
