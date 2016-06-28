package characters;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class SpriteManager {
    BufferedImage playerSpritesFile;
    private ArrayList<Image[]> playerSpritesRight;
    private ArrayList<Image[]> playerSpritesLeft;
    private int[] numberOfSpriteFrames;
    
    public static final int IDLE = 0;
    public static final int WALKING = 1;
    public static final int JUMPING = 2;
    public static final int FALLING = 3;
    public static final int GLIDING = 4;
    public static final int FIRING = 5;
    public static final int SCRATCHING = 6;
    
    public SpriteManager(){
    }
    
    /**
     * Initialize the player's sprites. Player sprites are split
     * into left and right images
     */
    private void initPlayerSprites(String s){
        ArrayList<Image[]> playerSprites = new ArrayList<>();
        int spriteSize = 30;
        //Make a list of how many sprite frames per type of movement
        numberOfSpriteFrames = new int[]{2, 8, 1, 2, 4, 2, 5};
        //Load the image from resources and turn into buffered image
        //BufferedImage is needed for subImage method.
        Image player = null;
        if (s.equals("Right")){
            player = new Image("characterimages/PlayerSpritesRight.png");
        }
        else if(s.equals("Left")){
            player = new Image("characterimages/PlayerSpritesLeft.png");
        }
        playerSpritesFile = SwingFXUtils.fromFXImage(player, null);
        
        //For each type of movement (this works since each movement was
        //on a different row)
        for (int row = 0; row < numberOfSpriteFrames.length; row++){
            Image[] currentMovementSprites;
            //The final row contains extra long images, so it doesn't fit perfectly
            //in spriteSize like the first 5 rows
            if (row != 6){
                //Set how large the BufferedImage array will be for the specific
                //type of movement
                currentMovementSprites = new Image[numberOfSpriteFrames[row]];
                //Since each sprite has no padding between them, add each sprite
                //image according to how far it is from the left edge
                for (int col = 0; col < currentMovementSprites.length; col++){
                    BufferedImage sprite = playerSpritesFile.getSubimage(
                            col*spriteSize, row*spriteSize,
                            spriteSize, spriteSize);
                    //SwingFXUtils converts from BufferedImage to FXImage
                    currentMovementSprites[col] = SwingFXUtils.toFXImage(sprite, null);
                }
            }
            //For scratching animation
            else{
                currentMovementSprites = new Image[numberOfSpriteFrames[row]];
                for (int j = 0; j < currentMovementSprites.length; j++){
                    BufferedImage sprite = playerSpritesFile.getSubimage(
                            //Only difference is j*spriteSize*2
                            j*spriteSize*2, row*spriteSize,
                            spriteSize*2, spriteSize);
                    currentMovementSprites[j] = SwingFXUtils.toFXImage(sprite, null);
                }
            }
            playerSprites.add(currentMovementSprites);
        }
        if (s.equals("Right")){
            playerSpritesRight = new ArrayList(playerSprites);
        }
        else if(s.equals("Left")){
            playerSpritesLeft = new ArrayList(playerSprites);
        }
    }
    
    public ArrayList<Image[]> getPlayerSpritesRight(){
        initPlayerSprites("Right");
        return playerSpritesRight;
    }
    
    public ArrayList<Image[]> getPlayerSpritesLeft(){
        initPlayerSprites("Left");
        return playerSpritesLeft;
    }
    
    public Image[] getDeathSprites(){
        Image origDeathImage = new Image("characterimages/DeathSprites.png");
        int size = 30;
        numberOfSpriteFrames = new int[]{6};
        Image[] deathSprites = new Image[numberOfSpriteFrames[0]];
        BufferedImage deathImage = SwingFXUtils.fromFXImage(origDeathImage, null);
        
        for (int col = 0; col < numberOfSpriteFrames[0]; col++){
            BufferedImage sprite = deathImage.getSubimage(
                    col*size, 0, size, size);
            deathSprites[col] = SwingFXUtils.toFXImage(sprite, null);
        }
        
        return deathSprites;
    }
    
    public ArrayList<Image[]> getFireballSprites(){
        int fireballSpriteSize = 16;
        ArrayList<Image[]> fireballSpriteImages = new ArrayList<>();
        Image origFireballImage = new Image("characterimages/Fireball.png");
        
        numberOfSpriteFrames = new int[]{4, 3};
        BufferedImage fireballImage = SwingFXUtils.fromFXImage(origFireballImage, null);
        
        for (int row = 0; row < numberOfSpriteFrames.length; row++){
            Image[] currentTypeOfFireballImage = new Image[numberOfSpriteFrames[row]];
            
            for (int col = 0; col < numberOfSpriteFrames[row]; col++){
                int x = col*fireballSpriteSize;
                int y = row*fireballSpriteSize;
                int w, h;
                w = h = fireballSpriteSize;
                BufferedImage sprite = fireballImage.getSubimage(
                        x, y, w, h);
                currentTypeOfFireballImage[col] = SwingFXUtils.toFXImage(sprite, null);
            }
            
            fireballSpriteImages.add(currentTypeOfFireballImage);
        }
        
        return fireballSpriteImages;
    }
    
    public ArrayList<Image[]> getSnailSprites(){
        ArrayList<Image[]> snailSprites = new ArrayList<>();
        int snailSpriteH = 20;
        int snailSpriteW = 22;
        Image origSnailImage = new Image("characterimages/SnailEnemy.png");
        
        numberOfSpriteFrames = new int[]{3};
        Image[] sprites = new Image[numberOfSpriteFrames[0]];
        BufferedImage snailImage = SwingFXUtils.fromFXImage(origSnailImage, null);
        
        for (int col = 0; col < numberOfSpriteFrames[0]; col++){
            BufferedImage sprite = snailImage.getSubimage(
                    col*snailSpriteW, 0*snailSpriteH,
                    snailSpriteW, snailSpriteH);
            sprites[col] = SwingFXUtils.toFXImage(sprite, null);
        }
        
        snailSprites.add(null); //Placeholder for IDLE action
        snailSprites.add(sprites); //Matches the WALKING action enum
        snailSprites.add(sprites); //Same sprites for the JUMPING action
        snailSprites.add(sprites); //Same sprites for the FALLING action
        
        return snailSprites;
    }
    
}
