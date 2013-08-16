package net.zllr.precisepitch.model;

import android.graphics.Canvas;
import android.graphics.RectF;
import net.zllr.precisepitch.view.StaffView;

import java.io.Serializable;

public final class DisplayNote implements Serializable {
    // Optional annotator for users to implement to add arbitrary
    // annotations to the note.
    public interface Annotator extends Serializable {
        void draw(Canvas canvas, RectF staffBoundingBox,
                  RectF noteBoundingBox);
    }

    // Immutable struct to represent a note to display.
    public DisplayNote(int pitch, int duration, int color) {
        this.pitch = pitch;
        this.duration = duration;
        this.color = color;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof DisplayNote))
            return false;
        DisplayNote on = (DisplayNote) other;
        return on.pitch == pitch && on.duration == duration && on.color == color;
    }

    // -- avoiding chatty setters and getters here, but should probably be
    // here for a self-respecting Java program :)

    // 0 for low A at 55Hz, 1 for A#.. 36 for A at 440Hz
    public final int pitch;

    // 1=full note 4=1/4 note ... Ignored for now, only 1/4 work.
    public final int duration;

    // Color to display. Standard Android color representation.
    // (should have a setter)
    public int color;

    // User provided annotator. If not null, is called when the note is
    // drawn.
    public Annotator annotator;
}
