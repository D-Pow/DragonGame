package characters;

import java.util.ArrayList;
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
        if (health == 0 && alive) {
            alive = false;
            animationCycler = timeToUpdateCycler = 0;
        } //If entity goes off the map
        else if (this.getX() <= 0
                || this.getX() + this.getFitWidth() >= world.getWidth()
                || this.getY() + this.getFitHeight() >= world.getHeight()) {
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
            world.entities.getChildren().remove(this);
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
            setImage(sprites.get(currentAction)[animationCycler]);
            animationCycler++;
            if (animationCycler >= sprites.get(currentAction).length) {
                animationCycler = 0;
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
        if (entity.topLeft || entity.midLeft) {
            entity.hitLeft = true;
        }
        if (entity.topRight || entity.midRight) {
            entity.hitRight = true;
        }
        if (entity.topLeft || entity.topRight || entity.topMiddle
                || (entity.jumping == true && (entity.midLeft || entity.midRight))) {
            entity.jumpTime = entity.jumpHeight;
            entity.topLeft = entity.topRight = entity.topMiddle = false;
        }
        if ((entity.bottomLeft || entity.bottomRight || entity.bottomMiddle)) {
            entity.onGround = true;
        }
    }

}//End class Entity
