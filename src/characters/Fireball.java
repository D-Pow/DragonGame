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
public class Fireball extends Entity{
    GameState world;
    String direction;
    int speed;
    ArrayList<Image[]> fireballSprites;
    boolean hitObject;
    
    public Fireball(String direction, int Speed, GameState world) {
        super((Image) null, world);
        this.direction = direction;
        speed = Speed;
        SpriteManager sm = new SpriteManager();
        fireballSprites = sm.getFireballSprites();
        animationCycler = 0;
        Image startImage = fireballSprites.get(0)[animationCycler];
        setImage(startImage);
        world.entities.getChildren().add(this);
    }
    
    public void updateFireball(boolean hitObject){
        if (direction.equals("Left")){
            setX(getX() - speed);
        }
        else if (direction.equals("Right")){
            setX(getX() + speed);
        }
        checkFireballCollision();
        changeFireballSprite();
    }
    
    public void checkFireballCollision(){
        for (Node n : world.map.getChildren()){
            int tileIndex = world.map.getChildren().indexOf(n);
            int tileNumber = world.mapTileNumbers.get(tileIndex);
            if (tileNumber > world.numDecorationTiles - 1){
                ImageView tile = (ImageView) n;
                if (checkObjectCollision((ImageView) this, tile)){
                    hitObject = true;
                    break;
                }
            }
        }
    }
    
    public void changeFireballSprite(){
        //0 = Fireball is still active
        //1 = Fireball must dissipate
        int fireballState = 0;
        if (hitObject){
            fireballState = 1;
            animationCycler = 0;
        }
        setImage(fireballSprites.get(fireballState)[animationCycler]);
        animationCycler++;
        if (animationCycler >= fireballSprites.get(fireballState).length){
            if (fireballState == 1){
                //Destroy fireball
                world.entities.getChildren().remove(this);
            }
            animationCycler = 0;
        }
    }
    
}
