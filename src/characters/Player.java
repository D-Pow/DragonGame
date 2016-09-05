package characters;

import java.util.ArrayList;
import java.util.Iterator;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    int maxFireEnergy; //Maximum fire energy possible
    int fireEnergy; //How much energy the dragon has to use for shooting fireballs
    int fireCost; //how much each fireball costs
    int fireEnergyDelayCounter; //cycler; when this equals fireEnergyDelay, fireEnergy++
    int fireEnergyDelay; //determines length of time for the fire regeneration delay
    
    /**
     * Sets initial values for speed, health, damage, etc.
     * 
     * @param world
     *          The current GameState
     * @param startX
     *          Starting X-coordinate
     * @param startY 
     *          Starting Y-coordinate
     */
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
        origWidth = size; //Used to revert to normal after done scratching
        direction = "Right";
        moving = false; //To counter the default moving = true from Entity
        moveSpeed = 2;
        fireSpeed = 4;
        health = maxHealth = 50;
        scratchDamage = 10;
        fireDamage = 5;
        fireEnergy = maxFireEnergy = 5;
        fireCost = 1;
        fireEnergyDelay = 10;
        playedDeathTone = false;
        playedFlinchTone = false;
        initWorldKeyListener();
        SoundEffect.SILENCE.play(); //Play a blank sound to load all sounds into cache
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
            checkWin();
            checkEnemyCollision();
            checkMapLocation();
            move();
            jump();
            updateImage();
            updateFireballs();
            world.hud.updateHUD(health, maxHealth, fireEnergy, maxFireEnergy);
            checkDeath();
        }
        else{
            die();
        }
    }
    
    /**
     * The player has more options for moving than normal entities
     * do, so the Entity.move() method is overridden here.
     * This also moves the map instead of the Player if the Player
     * is in the center of the screen.
     */
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
    
    /**
     * Scratch attack.
     */
    public void scratch(){
        if (!justScratched){
            attacking = true;
            scratching = true;
            currentAction = SCRATCHING;
            animationCycler = 0;
            if (animationCycler == 0) {
                //Only play scratch at the beginning of the attack
                SoundEffect.SCRATCH.play();
            }
        }
    }
    
    /**
     * Fireball attack; shoots new fireball.
     * Resets fireEnergyDelayCounter so that the player has
     * to wait longer before firing again.
     */
    public void fire(){
        if (!justFired && fireEnergy >= fireCost){
            attacking = true;
            firing = true;
            justFired = true;
            fireEnergy -= fireCost;
            fireEnergyDelayCounter = 0;
            currentAction = FIRING;
            animationCycler = 0;
            timeToUpdateCycler = UPDATE_TIME - 1;
            updateImage(); //Force player image to change so that it syncs
                           //with the formation of the fireball
            animationCycler = 0;
            Fireball fireball = new Fireball(direction, fireSpeed, world);
            world.entities.getChildren().add(fireball);
            SoundEffect.FIREBALL.play();
        }
    }
    
    /**
     * Moves fireballs and deletes them if the fireballs have dissipated
     * (dissipated = hit an object and run through the dissipation sprites).
     */
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
    
    /**
     * Checks if the Player has collided with any enemies.
     */
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
    
    /**
     * Checks if the player is in the center of the map and updates inCenter.
     * If it is, then move the map rather than the player.
     */
    public void checkMapLocation(){
        double centerAreaWidth = this.getFitWidth()/4;
        double centerAreaX = world.getWidth()/2 - centerAreaWidth;
        
        if (direction.equals("Left") && !hitLeft &&   //Check if player should move
                world.mapX < 0){                      //Check if map is in bounds
            //If player is within the "move map" region defined above
            if (this.getX() >= centerAreaX && this.getX() <= centerAreaX + centerAreaWidth){
                inCenter = true;
            }
        }
        
        //world.getWidth = width of the pane
        //mapX = location of the entire map-tiles group's X-coordinate (very left)
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
    
    /**
     * Checks if the player won the level, and initializes GameState.win() if true.
     */
    public void checkWin(){
        for (Node n : world.map.getChildren()) {
            int tileIndex = world.map.getChildren().indexOf(n);
            int tileNumber = world.mapTileNumbers.get(tileIndex);
            //Check collision
            ImageView tile = (ImageView) n;
            if (checkObjectCollision((ImageView) this, tile)) {
                //If player touches the winning tile
                if (GameState.WINNING_TILES.contains(tileNumber)) {
                    world.win();
                }
            }
        }
    }
    
    /**
     * Player includes actions other than moving and flinching,
     * so Entity.updateImage() is overridden here to include firing,
     * scratching, and gliding.
     * 
     * This also updates the dragon's fireEnergy.
     */
    @Override
    public void updateImage(){
        timeToUpdateCycler++;
        if (timeToUpdateCycler == UPDATE_TIME ||
                //Make scratching animation play faster for aesthetic purposes
                (scratching && timeToUpdateCycler == UPDATE_TIME*0.6)){
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
                
                //Increase the fireEnergy
                if (fireEnergy < maxFireEnergy){
                    fireEnergyDelayCounter++;
                    if (fireEnergyDelayCounter == fireEnergyDelay) {
                        fireEnergy++;
                        fireEnergyDelayCounter = 0;
                    }
                }
                
                setImage(playerSprites.get(currentAction)[animationCycler]);
                animationCycler++;
            }
            else if (flinching){
                if (!playedFlinchTone) {
                    playFlinchTone();
                    playedFlinchTone = true;
                }
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
                    playedFlinchTone = false;
                }
            }
            timeToUpdateCycler = 0;
        }
    }
    
    @Override
    public void playDeathTone() {
        SoundEffect.GROWL.play();
    }
    
    
    @Override
    public void playFlinchTone() {
        SoundEffect.DRAGON_GRUNT.play();
    }
    
    /**
     * Add all the controls for the game, including movement, attacks,
     * and pausing the game.
     */
    private void initWorldKeyListener(){
        world.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent e) -> {
            //Move left
            if (e.getCode() == KeyCode.A){
                if (!attacking){
                    direction = "Left";
                    moving = true;
                }
            }
            //Move right
            else if (e.getCode() == KeyCode.D){
                if (!attacking){
                    direction = "Right";
                    moving = true;
                }
            }
            
            //Jump (or glide if jumpTime == jumpHeight)
            if (e.getCode() == KeyCode.SPACE){
                jumping = true;
                gliding = true;
                justJumped = true;
            }
            
            //Scratch
            if (e.getCode() == KeyCode.J){
                if(!attacking){
                    scratch();
                    justScratched = true;
                }
            }
            //Fire
            if (e.getCode() == KeyCode.K){
                if(!attacking){
                    fire();
                }
            }
            
            //Pause menu
            if (e.getCode() == KeyCode.ENTER) {
                world.pauseGame();
            }
            //Quit
            if (e.getCode() == KeyCode.BACK_SPACE) {
                //Only quit if game is paused
                if (!world.running) {
                    world.quit();
                }
            }
        });
        
        world.addEventHandler(KeyEvent.KEY_RELEASED, (KeyEvent e) -> {
            //Stop jumping
            if (e.getCode() == KeyCode.SPACE){
                jumping = false;
                gliding = false;
                justJumped = false;
            }
            //Stop moving
            if (e.getCode() == KeyCode.A || e.getCode() == KeyCode.D){
                moving = false;
            }
        });
    }
}
