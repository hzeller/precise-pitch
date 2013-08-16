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
package net.zllr.precisepitch.model;

public final class MeasuredPitch {
    public MeasuredPitch(double f, int n, double c, double d) {
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
