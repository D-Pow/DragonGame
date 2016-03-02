package characters;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import platformcontrol.GameState;

abstract public class Entity extends ImageView {

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
    protected int enemyDamage;
    protected Image flinchImage;
    protected boolean flinching;
    protected int flinchCycler;
    protected boolean justHurt;

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

    Image[] deathSprites;
    ArrayList<Image[]> sprites;

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

    public Entity(Image image, GameState world) {
        super(image);
        this.world = world;
        deathSprites = (new SpriteManager()).getDeathSprites();
        tilesToCheck = new ArrayList<>();
        alive = true;
        moving = true; //Default for enemies
        currentAction = WALKING; //Default for enemies
        jumpSpeed = 3;
        jumpHeight = 40; //Pixel jump height = jumpHeight*jumpSpeed
        animationCycler = timeToUpdateCycler = 0;
    }

    /**
     * Updates the character's properties.
     */
    public void updateEntity() {
        if (alive) {
            checkMapCollision(this);
            move();
            updateImage();
            checkDeath();
        } else {
            die();
        }
    }

    /**
     * Moves the character entity left or right. This is designed mostly for
     * enemies, not the player or fireballs.
     */
    public void move() {
        if (moving) {
            currentAction = WALKING;
            switch (direction) {
                case "Left":
                    if (!hitLeft) {
                        setX(getX() - moveSpeed);
                        //Reset if the character hit the right wall
                        if (hitRight) {
                            hitRight = topRight = midRight = false;
                        }
                    } else if (hitLeft) {
                        direction = "Right";
                    }
                    break;

                case "Right":
                    if (!hitRight) {
                        setX(getX() + moveSpeed);
                        if (hitLeft) {
                            hitLeft = topLeft = midLeft = false;
                        }
                    } else if (hitRight) {
                        direction = "Left";
                    }
                    break;
            }
        }
        else {//if not moving
            if (!attacking) {
                currentAction = IDLE;
            }
        }
        jump();
    }
    
    public void jump(){
        //Try to jump
        if (jumping && jumpTime < jumpHeight){
            //jumpTime is how long you can jump before falling
            //Once jumpTime = jumpHeight, you start falling
            //regardless of if you are trying to jump (jumping = true)
            jumpTime++;
            onGround = bottomLeft = bottomRight = bottomMiddle = false;
            setY(getY() - jumpSpeed);
            if (!attacking) {currentAction = JUMPING;}
        }
        //If player can't jump, then fall
        else if (!jumping || jumpTime >= jumpHeight){
            //Setting jumpTime = jumpHeight prevents jumping
            //while in the air
            jumpTime = jumpHeight;
            //Since player isn't jumping, he is falling.
            //This is only called if jumping fails.
            fall();
        }
    }

    public void fall() {
        if (onGround) {
            //Allows entity to jump again
            jumpTime = 0;
            if (!moving) {
                if (!attacking) {
                    currentAction = IDLE;
                }
            }
        } else if (!onGround) {
            if (gliding) {
                setY(getY() + jumpSpeed / 3);
                if (!attacking) {
                    currentAction = GLIDING;
                }
            } else {
                setY(getY() + jumpSpeed);
                if (!attacking) {
                    currentAction = FALLING;
                }
            }
        }
    }

    protected void checkDeath() {
        //alive added to prevent deathSprite overwrite
        //If health is exhausted
        if (health <= 0 && alive) {
            alive = false;
            animationCycler = timeToUpdateCycler = 0;
        } //If entity goes off the map
        else if (this.getY() + this.getFitHeight() >= world.getHeight()) {
            health = 0;
        }
    }
    
    public void die() {
        //Change the sprite to represent death
        timeToUpdateCycler++;
        if (timeToUpdateCycler == UPDATE_TIME) {
            setImage(deathSprites[animationCycler]);
            animationCycler++;
            timeToUpdateCycler = 0;
            if (animationCycler >= deathSprites.length) {
                animationCycler = 0;
            }
            deathCounter++;
        }
        //Remove the entity from the world
        if (deathCounter == DEATH_TIME) {
            try {
                //If Player, remove from entities Group
                if (this instanceof Player){
                    world.entities.getChildren().remove(this);
                }
                //Else, it's an enemy; remove from enemeies Group
                else{
                    world.enemies.getChildren().remove(this);
                }
            } catch(ConcurrentModificationException e){
                //This will happen sometimes. It doesn't affect gameplay.
            }
            //if the entity was the player, reset the world
            if (this instanceof Player) {
                world.reset();
            }
        }
    }

    /**
     * Updates the current sprite of the entity.
     */
    public void updateImage() {
        timeToUpdateCycler++;
        if (timeToUpdateCycler == UPDATE_TIME) {
            if (!flinching){
                setImage(sprites.get(currentAction)[animationCycler]);
                animationCycler++;
                if (animationCycler >= sprites.get(currentAction).length) {
                    animationCycler = 0;
                }
            }
            else{
                if (flinchCycler == 0 || flinchCycler == 2){
                    setImage(flinchImage);
                }
                else if (flinchCycler == 1 || flinchCycler == 3){
                    setImage(world.blankTile);
                }
                flinchCycler++;
                if (flinchCycler == 4){
                    flinchCycler = 0;
                    flinching = false;
                    moving = true;
                    justHurt = false;
                }
            }
            timeToUpdateCycler = 0;
        }
    }

    public boolean checkObjectCollision(ImageView first, ImageView second) {
        //If the two images collide
        if ((first.getX() <= second.getX() + second.getFitWidth()
                && first.getX() + first.getFitWidth() >= second.getX())
                && (first.getY() <= second.getY() + second.getFitHeight()
                && first.getY() + first.getFitHeight() >= second.getY())) {
            return true;
        }
        return false;
    }

    /**
     * Checks collision of the entity passed with the map.
     *
     * @param e Entity whose collision is being checked.
     */
    public void checkMapCollision(Entity e) {
        //For each map tile
        for (Node n : world.map.getChildren()) {
            int tileIndex = world.map.getChildren().indexOf(n);
            int tileNumber = world.mapTileNumbers.get(tileIndex);
            //If the tile isn't a ghost (decoration) tile
            if (tileNumber > world.numDecorationTiles - 1) {
                ImageView tile = (ImageView) n;
                //Check collision
                if (checkObjectCollision((ImageView) e, tile)) {
                    tilesToCheck.add(tile);
                }
            }
        }
        updateCollisions(e, tilesToCheck);
        tilesToCheck.clear();
    }

    /**
     * This method updates the corner collision markers for the first image
     * (character) according to if it collides with a second image (map). This
     * is to be called in the same loop as the checkCollision method, but after
     * it has been called.
     *
     * @param entity Entity whose collisions are being updated
     * @param tiles List of tiles to include in update
     */
    public void updateCollisions(Entity entity, List<ImageView> tiles) {
        ImageView first = (ImageView) entity;

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
        Point mL = new Point(firstTopLeftX, firstTopLeftY + first.getFitHeight() / 2);
        Point mR = new Point(firstTopRightX, firstTopRightY + first.getFitHeight() / 2);
        Point tM = new Point(firstTopLeftX + first.getFitWidth() / 2, firstTopLeftY);
        Point bM = new Point(firstBottomLeftX + first.getFitWidth() / 2, firstBottomLeftY);

        //Distance from corner of the first image to the center of the second
        double leg = Math.pow(GameState.MAP_TILE_SIZE / 2, 2);
        double expectedCollisionDistance = Math.sqrt(leg + leg);

        //Reset if the corners have collided
        //onGround is needed here in order to make player fall after
        //walking off a ledge
        entity.topLeft = entity.topRight = entity.bottomLeft = entity.bottomRight
                = entity.midLeft = entity.midRight = entity.onGround
                = entity.hitLeft = entity.hitRight
                = entity.topMiddle = entity.bottomMiddle = false;

        for (ImageView second : tiles) {
            double secondCenterX = second.getX() + (second.getFitWidth() / 2);
            double secondCenterY = second.getY() + (second.getFitHeight() / 2);
            Point c = new Point(secondCenterX, secondCenterY);

            //update if the corners have collided
            if (Point.getDistance(tL, c) < expectedCollisionDistance) {
                entity.topLeft = true;
            }
            if (Point.getDistance(tR, c) < expectedCollisionDistance) {
                entity.topRight = true;
            }
            if (Point.getDistance(mR, c) < expectedCollisionDistance) {
                entity.midRight = true;
            }
            if (Point.getDistance(mL, c) < expectedCollisionDistance) {
                entity.midLeft = true;
            }
            if (Point.getDistance(tM, c) < expectedCollisionDistance) {
                entity.topMiddle = true;
            }
            if (Point.getDistance(bM, c) < expectedCollisionDistance) {
                entity.bottomMiddle = true;
            }
            if (Point.getDistance(bL, c) < expectedCollisionDistance) {
                entity.bottomLeft = true;
            }
            if (Point.getDistance(bR, c) < expectedCollisionDistance) {
                entity.bottomRight = true;
            }
        }

        //Now that the corners have been updated, have the appropriate affect
        //in the game.
        if (entity.topLeft || entity.midLeft || 
                (!entity.bottomMiddle && entity.bottomLeft)) {
            entity.hitLeft = true;
        }
        if (entity.topRight || entity.midRight ||
                (!entity.bottomMiddle && entity.bottomRight)) {
            entity.hitRight = true;
        }
        if ((entity.bottomLeft || entity.bottomRight || entity.bottomMiddle) &&
                //If one of the bottom corners or middle touches a map tile
                //but not the edge of the character.
                //This prevents the character from sticking to walls when
                //falling or jumping
                //The direction.equals part prevents the character from falling
                //off a tile when it reaches the edge, i.e. forcing the character
                //to fall only takes place when it's facing and touching a wall.
                !(direction.equals("Left") && entity.bottomLeft && entity.hitLeft && !entity.bottomMiddle) &&
                !(direction.equals("Right") && entity.bottomRight && entity.hitRight && !entity.bottomMiddle)) {
            entity.onGround = true;
        }
        //If the top collides but not the sides (which would prevent
        //jumping when on the ground touching a wall)
        if (entity.topMiddle && !entity.midLeft && !entity.midRight) {
            entity.jumpTime = entity.jumpHeight;
            entity.topLeft = entity.topRight = entity.topMiddle = false;
        }
    }

}//End class Entity
