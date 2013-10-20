package net.zllr.precisepitch;

import android.os.Handler;
import net.zllr.precisepitch.model.MeasuredPitch;

// A PitchSource runs a thread to asynchronously gather pitch information. This
// could be for instance listening on a microphone.
public interface PitchSource {
    // Set handler for messages generated from this PitchSource.
    // This sends 'MeasuredPitch' messages.
    void setHandler(Handler handler);

    // Start the thread doing the sampling.
    void startSampling();

    // Stop the thread. This is important when activity goes out of focus,
    // otherwise it might burn resources.
    void stopSampling();
}
