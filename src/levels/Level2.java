package levels;

import characters.Player;
import platformcontrol.GameState;
import platformcontrol.GameStateManager;

public class Level2 extends GameState{
    
    public Level2(GameStateManager gsm){
        super(gsm);
    }
    
    @Override
    public void initObjects(){
        //Set start location
        double playerStartX = GameState.PLAYER_SIZE*1.5;
        double playerStartY = h - GameState.PLAYER_SIZE*3.5;
        //Making the player also adds the keyListener to the gameState
        player = new Player(this, playerStartX, playerStartY);
        entities.getChildren().add(player);
        
        this.getChildren().add(entities);
    }
    
    public void runGame(){
        player.updateEntity();
        //moveEnemies();
    }
}