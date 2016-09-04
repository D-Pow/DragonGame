package levels;

import java.net.URISyntaxException;
import java.net.URL;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * Plays background music for the game.
 *
 * @author dPow
 */
public enum MusicPlayer {
    outdoorSong("outdoor_song.mp3"),
    bossSong("boss_song.mp3"),
    happySong("happy_song.mp3");

    Media media;
    MediaPlayer player;
    
    /**
     * Constructor to initialize the given song in the game.
     * 
     * @param songName 
     *          Name of the song to be played during that level
     */
    private MusicPlayer(String songName) {
        URL song = this.getClass().getResource("/music/" + songName);
        try {
            media = new Media(song.toURI().toString());
            player = new MediaPlayer(media);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Play the specified song.
     */
    public void playSong() {
        player.play();
        //When song finishes, loop it by seeking to the beginning
        player.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                player.seek(Duration.ZERO);
            }
        });
    }
    
    /**
     * Stop the specified songs.
     */
    public void stopSong() {
        player.stop();
    }
    
    /**
     * Stop all songs from playing.
     * This is mainly only used when changing/restarting
     * a level.
     */
    public static void stopAllSongs() {
        for (MusicPlayer songPlayer : MusicPlayer.values()) {
            songPlayer.stopSong();
        }
    }
    
}
