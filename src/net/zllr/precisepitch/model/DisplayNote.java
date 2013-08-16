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

import android.graphics.Canvas;
import android.graphics.RectF;

import java.io.Serializable;

public final class DisplayNote implements Serializable {
    // Optional annotator for users to implement to add arbitrary
    // annotations to the note.
    public interface Annotator extends Serializable {
        void draw(DisplayNote note, Canvas canvas,
                  RectF staffBoundingBox, RectF noteBoundingBox);
    }

    // Immutable struct to represent a note to display.
    public DisplayNote(int note, int duration, int color) {
        this.note = note;
        this.duration = duration;
        this.color = color;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof DisplayNote))
            return false;
        DisplayNote on = (DisplayNote) other;
        return on.note == note && on.duration == duration && on.color == color;
    }

    // -- avoiding chatty setters and getters here, but should probably be
    // here for a self-respecting Java program :)

    // 0 for low A at 55Hz, 1 for A#.. 36 for A at 440Hz
    public final int note;

    // 1=full note 4=1/4 note ... Ignored for now, only 1/4 work.
    public final int duration;

    // Color to display. Standard Android color representation.
    // (should have a setter)
    public int color;

    // User provided annotator. If not null, is called when the note is
    // drawn.
    public Annotator annotator;
}
