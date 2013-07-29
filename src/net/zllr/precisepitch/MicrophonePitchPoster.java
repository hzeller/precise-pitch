package net.zllr.precisepitch;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;

import java.io.Serializable;

class MicrophonePitchPoster extends Thread {
    public static final class PitchData implements Serializable {
        public PitchData(double f, int n, double c, double d) {
            frequency = f;
            note = n;
            cent = c;
            decibel = d;
        }
        public final double frequency;
        public final int note;        // 0 == 'A', 11 = 'Ab'/'G#'; 12: unknown.
        public final double cent;     // how far off we are in cent.
        public final double decibel;  // input level
    }

    public MicrophonePitchPoster(int minFrequency) {
        sampleCount = DyWaPitchTrack.suggestedSamplecount(minFrequency);
        pitchTracker = new DyWaPitchTrack(sampleCount);
        final int internalBufferSize = 2 * Math.max(
                AudioRecord.getMinBufferSize(DyWaPitchTrack.kSampleRateHz,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT),
                sampleCount);
        audiorecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                                        DyWaPitchTrack.kSampleRateHz,
                                        AudioFormat.CHANNEL_IN_MONO,
                                        AudioFormat.ENCODING_PCM_16BIT,
                                        internalBufferSize);
        doSampling = true;
    }

    // Set handler for messages generated from this runnable.
    public void setHandler(Handler handler) { this.handler = handler; }

    public synchronized void stopSampling() {
        doSampling = false;
    }

    // Runnable main method.
    public void run() {
        final short buffer[] = new short[sampleCount];
        final double samples[] = new double[sampleCount];
        audiorecorder.startRecording();
        while (shouldSample()) {
            int read = 0;
            while (read < buffer.length) {
                int r = audiorecorder.read(buffer, read, buffer.length - read);
                if (r > 0) read += r;
            }
            int maxValue = 0;
            for (int i = 0; i < sampleCount; ++i) {
                samples[i] = buffer[i] / 32768.0;
                int localMax = Math.abs(buffer[i]);
                maxValue = maxValue < localMax ? localMax : maxValue;
            }
            final double pitch = pitchTracker.computePitch(samples);
            final PitchData nc = createPitchData(pitch, maxValue);
            if (handler != null) {
                handler.sendMessage(handler.obtainMessage(0, nc));
            }
        }
    }

    private synchronized boolean shouldSample() { return doSampling; }

    private PitchData createPitchData(double frequency, int maxValue) {
        final double base = kPitchA / 8; // The A just below our C string (55Hz)
        final double d = Math.exp(Math.log(2) / 1200);
        final double cent_above_base = Math.log(frequency / base) / Math.log(d);
        final int scale_above_C = (int)Math.round(cent_above_base / 100.0) - 3;
        if (scale_above_C < 0) {
            return null;
        }

        // Press into regular scale
        double scale = cent_above_base % 1200.0;
        scale /= 100.0;
        final int rounded = (int) Math.round(scale);
        final double cent = 100 * (scale - rounded);
        final double vu_db = 20 * (Math.log(maxValue / 32768.0) / Math.log(10));
        return new PitchData(frequency, rounded % 12, cent, vu_db);
    }

    private static final double kPitchA = 440.0; // Hz.
    private int sampleCount;
    private DyWaPitchTrack pitchTracker;
    private AudioRecord audiorecorder;
    private Handler handler;   // This is the handler we post NoteCents to.
    private boolean doSampling;
}
