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
 *
 */
package net.zllr.precisepitch;

import android.os.Handler;

// A PitchSource runs a thread to asynchronously gather pitch information and
// sends it to the message handler queue to process.
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
