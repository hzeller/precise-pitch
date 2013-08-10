/*
 * Copyright 2013 Henner Zeller <h.zeller@acm.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.zllr.precisepitch;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;

import java.io.Serializable;

// Samples the microphone continuously and provides PitchData updates to the
// handler.
class MicrophonePitchPoster extends Thread {
    // If positive, create notes with that time in-between. If negative,
    // do the real pitch detection.
    static final int kDebugMs = -1;

    public static final class PitchData {
        public PitchData(double f, int n, double c, double d) {
            frequency = f;
            note = n;
            cent = c;
            decibel = d;
        }

        // Raw frequency.
        public final double frequency;

        // (note % 12) returns a range from 0 (A) to 11 (Ab/G#).
        // The absolute range starts with 0 (low A, 55Hz), 12 = 110Hz ...
        public final int note;

        public final double cent;     // How far off we are in cent.
        public final double decibel;  // input level in decibel.
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
        state = SamplingState.RUNNING;
        stateLock = new Object();
    }

    // Set handler for messages generated from this runnable.
    public void setHandler(Handler handler) { this.handler = handler; }

    // Returns information how many samples are gathered.
    public int getSampleCount() { return sampleCount; }

    public void stopSampling() {
    	try {
            synchronized (stateLock) {
                state = SamplingState.STOP_REQUESTED;
                while (state != SamplingState.STOPPED)
                    stateLock.wait();  // This should be fairly quick.
            }
        } catch (InterruptedException e) {
            // So be it.
        }
    }

    // Runnable/Thread main method.
    @Override
    public void run() {
        if (kDebugMs > 0) {
            dummyLoop(kDebugMs);
        } else {
            audiorecorder.startRecording();

            samplingLoop();

            audiorecorder.stop();
            audiorecorder.release();
            audiorecorder = null;
        }
        synchronized (stateLock) {
            state = SamplingState.STOPPED;
            stateLock.notify();
        }
    }

    private void dummyLoop(int waitMs) {
        final float minPitch = 65.2f;
        final float maxPitch = 440f;
        float pitch = minPitch;
        while (isSamplingRunning()) {
            try { Thread.sleep(waitMs); } catch (InterruptedException e) {}
            final PitchData nc = createPitchData(pitch, 32766);
            if (handler != null) {
                handler.sendMessage(handler.obtainMessage(0, nc));
            }
            pitch *= 1.059464;
            if (pitch > maxPitch) pitch = minPitch;
        }
    }
    private void samplingLoop() {
        final short buffer[] = new short[sampleCount];
        final double samples[] = new double[sampleCount];
        while (isSamplingRunning()) {
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
        handler = null;
    }

    private boolean isSamplingRunning() {
    	synchronized (stateLock) {
            return state == SamplingState.RUNNING;
        }
    }

    private PitchData createPitchData(double frequency, int maxValue) {
        final double base = kPitchA / 8; // The A just below our C string (55Hz)
        final double d = Math.exp(Math.log(2) / 1200);
        final double cent_above_base = Math.log(frequency / base) / Math.log(d);
        final int scale_above_C = (int)Math.round(cent_above_base / 100.0) - 3;
        if (scale_above_C < 0) {
            return null;
        }

        // Press into regular scale
        double scale = cent_above_base / 100.0;
        final int rounded = (int) Math.round(scale);
        final double cent = 100 * (scale - rounded);
        final double vu_db = 20 * (Math.log(maxValue / 32768.0) / Math.log(10));
        return new PitchData(frequency, rounded, cent, vu_db);
    }

    private static final double kPitchA = 440.0; // Hz.
    private int sampleCount;
    private DyWaPitchTrack pitchTracker;
    private AudioRecord audiorecorder;
    private Handler handler;   // This is the handler we post NoteCents to.
    enum SamplingState {
        RUNNING,
        STOP_REQUESTED,
        STOPPED;
    }
    private SamplingState state;
    private final Object stateLock;
}
