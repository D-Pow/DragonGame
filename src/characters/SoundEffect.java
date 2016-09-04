package characters;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Contains all the sound effects in the game.
 *
 * @author dPow
 */
public enum SoundEffect {
    JUMP("jump.aiff"),
    FIREBALL("fireball.aiff"),
    SCRATCH("scratch.aiff"),
    GROWL("growl.aiff"),
    SILENCE("silence.aiff");
    
    private Clip clip;

    SoundEffect(String fileName) {
        URL url = this.getClass().getResource("/SFX/" + fileName);
        AudioInputStream ais = null;
        try {
            ais = AudioSystem.getAudioInputStream(url);
        } catch (UnsupportedAudioFileException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            clip = AudioSystem.getClip();
            clip.open(ais);
            //fireSound = new Media(this.getClass().getResource("/SFX/jump.mp3").toURI().toString());
            //firePlayer = new MediaPlayer(fireSound);
        } catch (LineUnavailableException | IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void play() {
        if (clip.isRunning()) {
            clip.stop(); //Cancel the sound if it's playing already
        }
        clip.setMicrosecondPosition(0); //Reset to beginning of sound
        clip.start();
    }

}
