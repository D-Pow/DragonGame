package characters;

import java.util.ArrayList;
import java.util.Iterator;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import platformcontrol.GameState;

public class Player extends Entity{
    SpriteManager sm;
    //Animation sprites
    protected final ArrayList<Image[]> rightSprites;
    protected final ArrayList<Image[]> leftSprites;
    ArrayList<Image[]> playerSprites;//which of the right/left sprites is active
    private int origWidth;
    public boolean inCenter; //Decide whether or not to move map
    int scratchDamage;
    int fireDamage;
    int fireEnergyAtStart;
    int fireEnergy;
    int fireCost;
    
    public Player(GameState world, double startX, double startY){
        super(null, world);
        sm = new SpriteManager();
        rightSprites = sm.getPlayerSpritesRight();
        leftSprites = sm.getPlayerSpritesLeft();
        playerSprites = null;
        currentAction = IDLE;
        this.setImage(rightSprites.get(IDLE)[0]);
        int size = GameState.PLAYER_SIZE;
        setX(startX);
        setY(startY);
        setFitWidth(size);
        setFitHeight(size);
        origWidth = size;
        direction = "Right";
        moving = false; //To counter the default moving = true from Entity
        moveSpeed = 2;
        fireSpeed = 4;
        health = 500;
        scratchDamage = 100;
        fireDamage = 50;
        fireEnergyAtStart = 70;
        fireEnergy = fireEnergyAtStart;
        fireCost = 10;
        initWorldKeyListener();
    }
    
    /**
     * A single call to this method will check if the
     * player collides with an enemy/wall, allow the
     * player to jump, and allow the player to move.
     * This allows for simple thread construction in
     * each level.
     */
    @Override
    public void updateEntity(){
        if (alive){
            checkMapCollision(this);
            checkEnemyCollision();
            checkMapLocation();
            move();
            jump();
            updateImage();
            updateFireballs();
            checkDeath();
        }
        else{
            die();
        }
    }
    
    
    @Override
    public void move(){
        if (moving && !attacking){
            if (!attacking) {
                currentAction = WALKING;
            }
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
        else{//if not moving
            if (!attacking) {
                currentAction = IDLE;
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
    
    public void fire(){
        if (!justFired && fireEnergy > fireCost){
            attacking = true;
            firing = true;
            justFired = true;
            fireEnergy -= fireCost;
            currentAction = FIRING;
            animationCycler = 0;
            timeToUpdateCycler = UPDATE_TIME - 1;
            updateImage(); //Force player image to change so that it syncs
                           //with the formation of the fireball
            animationCycler = 0;
            Fireball fireball = new Fireball(direction, fireSpeed, world);
            world.entities.getChildren().add(fireball);
        }
    }
    
    public void updateFireballs(){
        Iterator<Node> iterator = world.entities.getChildren().iterator();
        while (iterator.hasNext()){
            Node child = iterator.next();
            if (child instanceof Fireball){
                Fireball fireball = (Fireball) child;
                fireball.updateEntity();
                if (fireball.dissipated){
                    iterator.remove();
                }
            }
            
        }
    }
    
    public void checkEnemyCollision(){
        if (!attacking && !flinching) {
            for (Node n : world.enemies.getChildren()) {
                Entity enemy = (Entity) n;
                if (checkObjectCollision(this, enemy) && enemy.alive){
                    health -= enemy.enemyDamage;
                    flinchImage = playerSprites.get(FIRING)[0];
                    flinching = true;
                }
            }
        }
        else if (attacking){
            for (Node m : world.enemies.getChildren()) {
                Entity enemy = (Entity) m;
                if (scratching && !enemy.justHurt){ //Prevents one attack from
                                                    //doing multiple hits
                    if (checkObjectCollision(this, enemy)){
                        enemy.health -= scratchDamage;
                        enemy.justHurt = true;
                        enemy.flinching = true;
                    }
                }
                //Fireball collisions are taken care of in Fireball class
            }
        }
    }
    
    public void checkMapLocation(){
        double centerAreaWidth = world.getWidth()/6;
        double centerAreaX = (world.getWidth() - centerAreaWidth)/2;
        
        if (direction.equals("Left") && !hitLeft &&     //Check if player should move
                world.mapX < 0){                      //Check if map is in bounds
            //If player is within the "move map" region defined above
            if (this.getX() >= centerAreaX && this.getX() <= centerAreaX + centerAreaWidth){
                inCenter = true;
            }
        }
        
        //world.getWidth = width of the pane
        //mapX = location of the map tiles group's X-coordinate
        //mapWidth = total width of the map tiles
        else if (direction.equals("Right") && !hitRight &&
                world.mapX > world.getWidth() - world.mapWidth){
            if (this.getX() >= centerAreaX && this.getX() <= centerAreaX + centerAreaWidth){
                inCenter = true;
            }
        }
        
        else{
            inCenter = false;
        }
        
    }
    
    @Override
    public void updateImage(){
        timeToUpdateCycler++;
        if (timeToUpdateCycler == UPDATE_TIME){
            switch (direction){
                case "Right":
                    playerSprites = new ArrayList(rightSprites);
                    break;
                case "Left":
                    playerSprites = new ArrayList(leftSprites);
                    break;
            }
            if (!flinching){
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
                        if (direction.equals("Left")) {
                            setX(getX() + origWidth);
                        }
                        setFitWidth(origWidth);
                        justScratched = false;
                    }
                    if (firing){
                        firing = false;
                        currentAction = IDLE;
                        justFired = false;
                    }
                }
                //Since the scratch image width is twice that of the normal sprite
                if (attacking && scratching){
                    //To compensate for the extra width, the character is moved
                    //only if facing left
                    if (direction.equals("Left") && animationCycler == 0) {
                        setX(getX() - origWidth);
                    }
                    setFitWidth(origWidth*2);
                }
                if (fireEnergy < fireEnergyAtStart){
                    fireEnergy++;
                }
                setImage(playerSprites.get(currentAction)[animationCycler]);
                animationCycler++;
            }
            else if (flinching){
                if (flinchCycler == 1 || flinchCycler == 3){
                    setImage(flinchImage);
                }
                else if (flinchCycler == 0 || flinchCycler == 2){
                    setImage(world.blankTile);
                }
                flinchCycler++;
                if (flinchCycler == 4){
                    flinchCycler = 0;
                    flinching = false;
                }
            }
            timeToUpdateCycler = 0;
        }
    }
    
    private void initWorldKeyListener(){
        world.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent e) -> {
            if (e.getCode() == KeyCode.A){
                if (!attacking){
                    direction = "Left";
                    moving = true;
                }
            }
            else if (e.getCode() == KeyCode.D){
                if (!attacking){
                    direction = "Right";
                    moving = true;
                }
            }
            if (e.getCode() == KeyCode.SPACE){
                jumping = true;
            }
            if (e.getCode() == KeyCode.W){
                gliding = true;
            }
            if (e.getCode() == KeyCode.J){
                if(!attacking){
                    scratch();
                    justScratched = true;
                }
            }
            if (e.getCode() == KeyCode.K){
                if(!attacking){
                    fire();
                }
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
