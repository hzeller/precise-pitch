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
import net.zllr.precisepitch.model.MeasuredPitch;

// Samples the microphone continuously and provides PitchData updates to the
// handler.
class MicrophonePitchSource extends Thread implements PitchSource {
    public MicrophonePitchSource() {
        this(60);  // default frequency. Lower: wider window.
    }

    public MicrophonePitchSource(int minFrequency) {
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
    @Override
    public void setHandler(Handler handler) { this.handler = handler; }

    @Override
    public void startSampling() {
        start();
    }

    @Override
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
        audiorecorder.startRecording();

        samplingLoop();

        audiorecorder.stop();
        audiorecorder.release();
        audiorecorder = null;
        synchronized (stateLock) {
            state = SamplingState.STOPPED;
            stateLock.notify();
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
            final MeasuredPitch nc = MeasuredPitch.createPitchData(pitch, maxValue/32768.0);
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
