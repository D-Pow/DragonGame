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
    
    public static final int spriteSize = 30;
    
    public static final int IDLE = 0;
    public static final int WALKING = 1;
    public static final int JUMPING = 2;
    public static final int FALLING = 3;
    public static final int GLIDING = 4;
    public static final int FIRING = 5;
    public static final int SCRATCHING = 6;
    
    public SpriteManager(String s){
        switch(s){
            case "Player":
                initPlayerSprites("Right");
                initPlayerSprites("Left");
                break;
            case "Enemy":
                initEnemySprites();
                break;
            default:
                System.err.println("Must input correct sprite type");
                break;
        }
    }
    
    /**
     * Initialize the player's sprites.
     */
    private void initPlayerSprites(String s){
        ArrayList<Image[]> playerSprites = new ArrayList<>();
        //Make a list of how many sprite frames per type of movement
        numberOfSpriteFrames = new int[]{2, 8, 1, 2, 4, 2, 5};
        //Load the image from resources and turn into buffered image
        //BufferedImage is needed for subImage method.
        Image player = null;
        if (s.equals("Right")){
            player = new Image("characterimages/PlayerSpritesRight.gif");
        }
        else if(s.equals("Left")){
            player = new Image("characterimages/PlayerSpritesLeft.gif");
        }
        playerSpritesFile = SwingFXUtils.fromFXImage(player, null);
        
        //For each type of movement (this works since each movement was
        //on a different row)
        for (int i = 0; i < numberOfSpriteFrames.length; i++){
            Image[] temp;
            //The final row is extra long, so it doesn't fit perfectly
            //in spriteSize like the first 5 rows
            if (i != 6){
                //Set how large the BufferedImage array will be for the specific
                //type of movement
                temp = new Image[numberOfSpriteFrames[i]];
                //Since each sprite has no padding between them, add each sprite
                //image according to how far it is from the left edge
                for (int j = 0; j < temp.length; j++){
                    BufferedImage sprite = playerSpritesFile.getSubimage(
                            j*spriteSize, i*spriteSize,
                            spriteSize, spriteSize);
                    //SwingFXUtils converts from BufferedImage to FXImage
                    temp[j] = SwingFXUtils.toFXImage(sprite, null);
                }
            }
            //For scratching animation
            else{
                temp = new Image[numberOfSpriteFrames[i]];
                for (int j = 0; j < temp.length; j++){
                    BufferedImage sprite = playerSpritesFile.getSubimage(
                            //Only difference is j*spriteSize*2
                            j*spriteSize*2, i*spriteSize,
                            spriteSize*2, spriteSize);
                    temp[j] = SwingFXUtils.toFXImage(sprite, null);
                }
            }
            playerSprites.add(temp);
        }
        if (s.equals("Right")){
            playerSpritesRight = new ArrayList(playerSprites);
        }
        else if(s.equals("Left")){
            playerSpritesLeft = new ArrayList(playerSprites);
        }
    }
    
    public ArrayList<Image[]> getPlayerSpritesRight(){
        return playerSpritesRight;
    }
    
    public ArrayList<Image[]> getPlayerSpritesLeft(){
        return playerSpritesLeft;
    }
    
    private void initEnemySprites(){
        
    }
}