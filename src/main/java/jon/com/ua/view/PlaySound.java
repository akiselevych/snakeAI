package jon.com.ua.view;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: al1
 * Date: 11/16/13
 */
public class PlaySound extends Thread {
    private static File soundFile;
    private static AudioInputStream ais;
    public static Clip clip;

    public PlaySound() {
        try {
            String path = PlaySound.class.getResource("/sound/smb_vine.wav").getPath();
            soundFile = new File(path);
            ais = AudioSystem.getAudioInputStream(soundFile);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}
