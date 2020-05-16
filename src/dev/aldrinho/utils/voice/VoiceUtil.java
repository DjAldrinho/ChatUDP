package dev.aldrinho.utils.voice;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;

public class VoiceUtil {


    static boolean isRecording = false;
    static ByteArrayOutputStream out;

    /**
     * Defines an audio format
     */
    static AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 8;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                channels, signed, bigEndian);
        return format;
    }

    public static void setRecording(boolean flag) {
        isRecording = flag;
    }

    public static boolean isRecording() {
        return isRecording;
    }
}
