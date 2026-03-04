package snake_game;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SoundManager {

    public static void playEatSound() {
        new Thread(() -> {
            try {
                generateTone(800, 100, 0.5); // high beep
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void playGameOverSound() {
        new Thread(() -> {
            try {
                generateTone(300, 200, 0.5); // low beep
                generateTone(200, 400, 0.5); // lower beep
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void generateTone(int hz, int msecs, double vol) throws LineUnavailableException {
        float sampleRate = 8000f;
        byte[] buf = new byte[1];
        AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
        SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
        sdl.open(af);
        sdl.start();

        for (int i = 0; i < msecs * 8; i++) {
            double angle = i / (sampleRate / hz) * 2.0 * Math.PI;
            buf[0] = (byte) (Math.sin(angle) * 127.0 * vol);
            sdl.write(buf, 0, 1);
        }

        sdl.drain();
        sdl.stop();
        sdl.close();
    }
}
