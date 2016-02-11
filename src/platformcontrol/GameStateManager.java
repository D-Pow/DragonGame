package platformcontrol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javafx.scene.Scene;
import javafx.stage.Stage;
import levels.Level1;
import levels.Level2;

public final class GameStateManager{
    private StateType currentState;
    private GameState currentLevel;
    private final Stage stage;
    public double width;
    public double height;
    
    public enum StateType {MENU, LOAD, LEVEL1, LEVEL2, DONE;}
    
    public GameStateManager(Stage stage){
        this.stage = stage;
        currentState = StateType.MENU;
        changeState(currentState);
    }
    
    /**
     * A factory method that changes the stage's
     * scene to a new game state.
     * 
     * @param state The game state the manager switches to.
     */
    public void changeState(StateType state){
        //StateType[] st = StateType.values();
        //Add an overloaded method that takes
        //no arguments such that the state type
        //just cycles through to the next state
        //i.e. finishing level 1 automatically
        //loads level 2 without having to specify
        //it.
        width = stage.getWidth();
        height = stage.getHeight();
        currentState = state;
        switch(currentState){
            case MENU:
                currentLevel = new MenuScreen(this);
                break;
            case LOAD:
                currentLevel = new LoadScreen(this);
                break;
            case LEVEL1:
                currentLevel = new Level1(this);
                break;
            case LEVEL2:
                currentLevel = new Level2(this);
                break;
            default:
                //Do nothing
        }
        Scene scene = new Scene(currentLevel,
                width, height);
        stage.setScene(scene);
        currentLevel.setFocusTraversable(true);
        currentLevel.requestFocus();
        saveGame();
    }
    
    /**
     * Changes the state to the next available state
     */
    public void changeState(){
        StateType[] states = StateType.values();
        int i = 0;
        while (!states[i].equals(currentState)){
            i++;
        }
        changeState(states[i+1]);
    }
    
    private void saveGame(){
        File file = new File("./DragonSave.data");
        //If the file exists, compare if current level is higher than
        //the saved level. If it is, overwrite it.
        if (file.exists() && 
                currentState != StateType.MENU && currentState != StateType.LOAD){
            try(BufferedWriter writer = new BufferedWriter(
                    new FileWriter(file))){
                int oldLevel = loadSave();
                
                String len = "LEVEL";
                String currentLevelString = currentState.toString();
                String levelNumber = currentLevelString.substring(len.length());
                System.out.printf("len: %d%nlevelNumber: %s%n", len.length(),levelNumber);
                int newLevel = Integer.valueOf(levelNumber);
                
                if (newLevel > oldLevel){
                    writer.write(currentState.toString());
                }
            } catch (IOException ex){
                ex.getCause();
            }
        }
        else if (!file.exists()){
            System.out.println("New File!");
            try(BufferedWriter writer = new BufferedWriter(
                    new FileWriter("./DragonSave.data"))){
                writer.write(StateType.LEVEL1.toString());
            } catch (IOException ex){
                ex.getCause();
            }
        }
    }
    
    public int loadSave(){
        String level = null;
        try(BufferedReader reader = new BufferedReader(new FileReader("./DragonSave.data"))){
            level = reader.readLine();
        } catch (IOException ex) {
            //Do nothing
        }
        
        if (level != null){
            String len = "LEVEL";
            String levelNumber = level.substring(len.length());
            int number = Integer.valueOf(levelNumber);
            return number;
        }
        else{
            return 0;
        }
    }
    
    public StateType getCurrentState(){
        return currentState;
    }
}