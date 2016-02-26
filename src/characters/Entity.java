package characters;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import platformcontrol.GameState;

public class Entity extends ImageView{
    protected GameState world;
    
    //Character Properties
    protected int health;
    protected int moveSpeed;
    protected int jumpSpeed;
    protected int jumpHeight;
    protected int fireSpeed;
    protected boolean jumped;
    protected boolean jumping;
    protected boolean falling;
    protected boolean moving;
    protected boolean gliding;
    protected boolean attacking;
    protected boolean scratching;
    protected boolean justScratched;
    protected boolean firing;
    protected boolean justFired;
    protected String direction;
    protected int jumpTime;
    protected boolean justJumped;
    public boolean alive;
    protected Image deathSprite;
    
    //Collision with map
    protected boolean onGround;
    protected boolean topLeft;
    protected boolean topRight;
    protected boolean midLeft;
    protected boolean midRight;
    protected boolean bottomLeft;
    protected boolean bottomRight;
    protected boolean topMiddle;
    protected boolean bottomMiddle;
    public boolean hitLeft;
    public boolean hitRight;
    protected List<ImageView> tilesToCheck;
    
    //currentAction is the action enum that is being done
    protected int currentAction;
    //animationCycler is which sprite of the current action to show
    protected int animationCycler;
    //when timeToUpdateCycler == UPDATE_TIME, it changes the sprite image
    protected int timeToUpdateCycler;
    //when deathCounter == DEATH_TIME, the level resets
    protected int deathCounter;
    
    
    //Animation enums
    protected static final int UPDATE_TIME = 15;
    protected static final int DEATH_TIME = 10;
    protected static final int IDLE = 0;
    protected static final int WALKING = 1;
    protected static final int JUMPING = 2;
    protected static final int FALLING = 3;
    protected static final int GLIDING = 4;
    protected static final int FIRING = 5;
    protected static final int SCRATCHING = 6;
    
    public Entity(Image image, GameState world){
        super(image);
        this.world = world;
        tilesToCheck = new ArrayList<>();
    }
    
    protected void checkDeath(){
        //alive added to prevent deathSprite overwrite
        //If health is exhausted
        if (health == 0 && alive){
            alive = false;
            deathSprite = this.getImage();
            animationCycler = timeToUpdateCycler = 0;
        }
        //If entity goes off the map
        else if(
                this.getX() <= 0 ||
                this.getX() + this.getFitWidth() >= world.getWidth() ||
                this.getY() + this.getFitHeight() >= world.getHeight()
                ){
            health = 0;
        }
    }
    
    public void die(){
        //Change the sprite to represent death
        timeToUpdateCycler++;
        if (timeToUpdateCycler == UPDATE_TIME){
            if (animationCycler == 0){
                setImage(deathSprite);
            }
            else if (animationCycler == 1){
                setImage(world.blankTile);
            }
            animationCycler++;
            timeToUpdateCycler = 0;
            if (animationCycler > 1){
                animationCycler = 0;
            }
            deathCounter++;
        }
        //Remove the entity from the world
        if (deathCounter == DEATH_TIME){
            world.entities.getChildren().remove(this);
            //if the entity was the player, reset the world
            if (this instanceof Player){
                world.reset();
            }
        }
    }
    
    public boolean checkObjectCollision(ImageView first, ImageView second){
        //If the two images collide
        if ((first.getX() <= second.getX() + second.getFitWidth() &&
                first.getX() + first.getFitWidth() >= second.getX())
                &&
                (first.getY() <= second.getY() + second.getFitHeight() &&
                first.getY() + first.getFitHeight() >= second.getY())){
            return true;
        }
        return false;
    }
    
    /**
     * This method updates the corner collision markers for
     * the first image (character) according to if it collides
     * with a second image (map). This is to be called in the same
     * loop as the checkCollision method, but after it has been called.
     * 
     * @param first Character tile
     * @param tiles List of tiles to include in update
     */
    public void updateCollisions(ImageView first, List<ImageView> tiles){
        //Get points
        double firstTopLeftX = first.getX();
        double firstTopLeftY = first.getY();
        double firstTopRightX = first.getX() + first.getFitWidth();
        double firstTopRightY = first.getY();
        double firstBottomRightX = first.getX() + first.getFitWidth();
        double firstBottomRightY = first.getY() + first.getFitHeight();
        double firstBottomLeftX = first.getX();
        double firstBottomLeftY = first.getY() + first.getFitHeight();
        
        Point tL = new Point(firstTopLeftX, firstTopLeftY);
        Point tR = new Point(firstTopRightX, firstTopRightY);
        Point bL = new Point(firstBottomLeftX, firstBottomLeftY);
        Point bR = new Point(firstBottomRightX, firstBottomRightY);
        Point mL = new Point(firstTopLeftX, firstTopLeftY + first.getFitHeight()/2);
        Point mR = new Point(firstTopRightX, firstTopRightY + first.getFitHeight()/2);
        Point tM = new Point(firstTopLeftX + first.getFitWidth()/2, firstTopLeftY);
        Point bM = new Point(firstBottomLeftX + first.getFitWidth()/2, firstBottomLeftY);
        
        //Distance from corner of the first image to the center of the second
        double leg = Math.pow(GameState.MAP_TILE_SIZE/2, 2);
        double expectedCollisionDistance = Math.sqrt(leg + leg);
        
        //Reset if the corners have collided
        //onGround is needed here in order to make player fall after
        //walking off a ledge
        topLeft = topRight = bottomLeft = bottomRight = midLeft = midRight =
            onGround = hitLeft = hitRight = topMiddle = bottomMiddle = false;
        
        for (ImageView second : tiles){
            double secondCenterX = second.getX() + (second.getFitWidth()/2);
            double secondCenterY = second.getY() + (second.getFitHeight()/2);
            Point c = new Point(secondCenterX, secondCenterY);
            
            //update if the corners have collided
            if (Point.getDistance(tL, c) < expectedCollisionDistance){
                topLeft = true;
            }
            if (Point.getDistance(tR, c) < expectedCollisionDistance){
                topRight = true;
            }
            if (Point.getDistance(mR, c) < expectedCollisionDistance){
                midRight = true;
            }
            if (Point.getDistance(mL, c) < expectedCollisionDistance){
                midLeft = true;
            }
            if (Point.getDistance(tM, c) < expectedCollisionDistance){
                topMiddle = true;
            }
            if (Point.getDistance(bM, c) < expectedCollisionDistance){
                bottomMiddle = true;
            }
            if (Point.getDistance(bL, c) < expectedCollisionDistance){
                bottomLeft = true;
            }
            if (Point.getDistance(bR, c) < expectedCollisionDistance){
                bottomRight = true;
            }
        }
        
        //Now that the corners have been updated, have the appropriate affect
        //in the game.
        if (topLeft || midLeft){
            hitLeft = true;
        }
        if (topRight || midRight){
            hitRight = true;
        }
        if (topLeft || topRight || topMiddle ||
                (jumping == true && (midLeft || midRight))){
            jumpTime = jumpHeight;
            topLeft = topRight = topMiddle = false;
        }
        if ((bottomLeft || bottomRight || bottomMiddle)){
            onGround = true;
        }
    }
    
}//End class Entity
