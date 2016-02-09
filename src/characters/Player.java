package characters;

import java.util.ArrayList;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import platformcontrol.GameState;

public class Player extends Entity{
    SpriteManager sm = new SpriteManager("Player");
    //Animation sprites
    protected final ArrayList<Image[]> rightSprites;
    protected final ArrayList<Image[]> leftSprites;
    private int origWidth;
    public boolean inCenter; //Decide whether or not to move map
    
    public Player(GameState world){
        super(null, world);
        rightSprites = sm.getPlayerSpritesRight();
        leftSprites = sm.getPlayerSpritesLeft();
        currentAction = IDLE;
        this.setImage(rightSprites.get(IDLE)[0]);
        int size = GameState.ENTITY_SIZE;
        setFitWidth(size);
        setFitHeight(size);
        origWidth = size;
        direction = "Right";
        moveSpeed = 2;
        jumpSpeed = 3;
        jumpHeight = 40; //Pixel jump height = jumpHeight*jumpSpeed
        initWorldKeyListener();
    }
    
    /**
     * A single call to this method will check if the
     * player collides with an enemy/wall, allow the
     * player to jump, and allow the player to move.
     * This allows for simple thread construction in
     * each level.
     */
    public void updatePlayer(){
        checkMapCollision();
        //checkEnemyCollision();
        checkMapLocation();
        updateImage();
        move();
        jump();
    }
    
    public void move(){
        if (moving){
            if (!attacking) {currentAction = WALKING;}
            switch(direction){
                case "Left":
                    //If the player isn't in the center of the screen
                    if (!inCenter){
                        if (getX() > 0 && !hitLeft){
                            setX(getX() - moveSpeed);
                            //Reset if the player hit the right wall
                            if (hitRight){
                                hitRight = topRight = midRight = false;
                            }
                        }
                    }
                    //If the player is in the center, move screen instead
                    else if(inCenter){
                        if (hitRight){
                            hitRight = topRight = midRight = false;
                        }
                        world.moveMap(direction, moveSpeed);
                    }
                    break;
                
                case "Right":
                    if (!inCenter){
                        if (getX() < world.getWidth() - getFitWidth() && !hitRight){
                            setX(getX() + moveSpeed);
                            if (hitLeft){
                                hitLeft = topLeft = midLeft = false;
                            }
                        }
                    }
                    //If the player is in the center, move screen instead
                    else if(inCenter){
                        if (hitLeft){
                            hitLeft = topLeft = midLeft = false;
                        }
                        world.moveMap(direction, moveSpeed);
                    }
                    break;
            }
        }
        else{
            if (!attacking) {currentAction = IDLE;}
        }
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
    
    public void fall(){
        //if (getY() >= world.getHeight() - getFitHeight()){
        if(onGround){
            //Allows player to jump again
            jumpTime = 0;
            if (!moving){
                if (!attacking) {currentAction = IDLE;}
            }
        }
        //else if (getY() < world.getHeight() - getFitHeight()){
        else if(!onGround){
            if (gliding){
                setY(getY() + jumpSpeed/3);
                if (!attacking) {currentAction = GLIDING;}
            }
            else{
                setY(getY() + jumpSpeed);
                if (!attacking) {currentAction = FALLING;}
            }
        }
    }
    
    public void scratch(){
        if (!justScratched){
            attacking = true;
            scratching = true;
            currentAction = SCRATCHING;
            animationCycler = 0;
        }
    }
    
    public void checkMapCollision(){
        //For each map tile
        for (Node n : world.map.getChildren()){
            int tileIndex = world.map.getChildren().indexOf(n);
            int tileNumber = world.mapTileNumbers.get(tileIndex);
            //If the tile isn't a ghost (decoration) tile
            if (tileNumber > world.numGhostTiles - 1){
                ImageView tile = (ImageView) n;
                //Check collision
                if (checkObjectCollision((ImageView) this, tile)){
                    tilesToCheck.add(tile);
                }
            }
        }
        updateCollisions((ImageView)this, tilesToCheck);
        tilesToCheck.clear();
    }
    
    public boolean checkEnemyCollision(){
        //For enemy:
        ImageView enemy = null;
        return checkObjectCollision((ImageView) this, enemy);
    }
    
    public void checkMapLocation(){
        double centerAreaWidth = world.getWidth()/6;
        double centerAreaX = (world.getWidth() - centerAreaWidth)/2;
        
        if (direction.equals("Left") && !hitLeft &&     //Check if player should move
                world.mapX <= 0){                      //Check if map is in bounds
            //If player is within the "move map" region defined above
            if (this.getX() >= centerAreaX && this.getX() <= centerAreaX + centerAreaWidth){
                inCenter = true;
            }
        }
        
        //world.getWidth = width of the pane
        //mapX = location of map tiles X-coordinate
        //mapWidth = width of the map tiles
        else if (direction.equals("Right") && !hitRight &&
                world.mapX >= world.getWidth() - world.mapWidth){
            if (this.getX() >= centerAreaX && this.getX() <= centerAreaX + centerAreaWidth){
                inCenter = true;
            }
        }
        
        else{
            inCenter = false;
        }
        
    }
    
    protected void updateImage(){
        timeToUpdateCycler++;
        if (timeToUpdateCycler == UPDATE_TIME){
            ArrayList<Image[]> playerSprites = null;
            switch (direction){
                case "Right":
                    playerSprites = new ArrayList(rightSprites);
                    break;
                case "Left":
                    playerSprites = new ArrayList(leftSprites);
                    break;
            }
            //currentAction will equal one of the action enums
            if (animationCycler >= playerSprites.get(currentAction).length){
                animationCycler = 0;
                //The attacking sequence should only play through once
                if (attacking){
                    attacking = false;
                }
                if (scratching){
                    scratching = false;
                    currentAction = IDLE;
                    if (direction.equals("Left")) {setX(getX() + origWidth);}
                    setFitWidth(origWidth);
                    justScratched = false;
                }
            }
            //Since the scratch image width is twice that of the normal sprite
            if (attacking && scratching){
                //To compensate for the extra width, the character is moved
                //only if facing left
                if (direction.equals("Left") && animationCycler == 0) {setX(getX() - origWidth);}
                setFitWidth(origWidth*2);
            }
            setImage(playerSprites.get(currentAction)[animationCycler]);
            animationCycler++;
            timeToUpdateCycler = 0;
        }
    }
    
    private void initWorldKeyListener(){
        world.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent e) -> {
            if (e.getCode() == KeyCode.A){
                direction = "Left";
                moving = true;
            }
            else if (e.getCode() == KeyCode.D){
                direction = "Right";
                moving = true;
            }
            if (e.getCode() == KeyCode.SPACE){
                jumping = true;
            }
            if (e.getCode() == KeyCode.W){
                gliding = true;
            }
            if (e.getCode() == KeyCode.O){
                scratch();
                justScratched = true;
            }
        });
        
        world.addEventHandler(KeyEvent.KEY_RELEASED, (KeyEvent e) -> {
            if (e.getCode() == KeyCode.SPACE){
                jumping = false;
            }
            if (e.getCode() == KeyCode.A || e.getCode() == KeyCode.D){
                moving = false;
            }
            if (e.getCode() == KeyCode.W){
                gliding = false;
            }
        });
    }
}