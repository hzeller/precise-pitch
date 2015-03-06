package net.zllr.precisepitch.view;

import android.graphics.Canvas;
import android.graphics.RectF;

import net.zllr.precisepitch.model.DisplayNote;

import java.util.ArrayList;
import java.util.List;

public class CombineAnnotator implements DisplayNote.Annotator {
    private List<DisplayNote.Annotator> annotators;
    public CombineAnnotator() {
        annotators = new ArrayList<DisplayNote.Annotator>();
    }

    public void addAnnotator(DisplayNote.Annotator a) {
        annotators.add(a);
    }

    @Override
    public void draw(DisplayNote note, Canvas canvas, RectF staffBoundingBox, RectF noteBoundingBox) {
        for (DisplayNote.Annotator a : annotators) {
            if (a == null) continue;
            a.draw(note, canvas, staffBoundingBox, noteBoundingBox);
        }
    }
}
