package jon.com.ua.view;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: al1
 * Date: 11/16/13
 */
public class PlayBackground extends Thread {
    private static File soundFile;
    private static AudioInputStream ais;
    public static Clip clip;
    private boolean isPaused;

    public PlayBackground() {
        String path = PlayBackground.class.getResource("/sound/smb-overworld.wav").getPath();
        soundFile = new File(path);
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                if (isPaused) {
                    continue;
                }
                ais = AudioSystem.getAudioInputStream(soundFile);
                clip = AudioSystem.getClip();
                clip.open(ais);

                clip.setFramePosition(0);
                clip.start();

                Thread.sleep(clip.getMicrosecondLength() / 1000);
                clip.stop();
                clip.close();
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }
}
