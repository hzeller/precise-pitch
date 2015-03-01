package net.zllr.precisepitch.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import net.zllr.precisepitch.model.DisplayNote;

// An annotator that highlights a particular note.
public class HighlightAnnotator implements DisplayNote.Annotator {
    private final Paint highlightPaint;
    private final Paint borderPaint;

    public HighlightAnnotator(int highlightColor) {
        highlightPaint = new Paint();
        highlightPaint.setColor(highlightColor == -1 ? Color.argb(70, 0xff, 0xff, 0) : highlightColor);
        highlightPaint.setStrokeWidth(0);
        borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStrokeWidth(0);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);
    }

    public void draw(DisplayNote note,
                     Canvas canvas, RectF staffBoundingBox,
                     RectF noteBoundingBox) {
        float lineWidth = (staffBoundingBox.bottom - staffBoundingBox.top)/4;
        RectF drawBox = new RectF(noteBoundingBox);
        // If note does not go outside staff, make the box a bit larger.
        drawBox.union(drawBox.left - 0.2f * lineWidth,
                staffBoundingBox.top - lineWidth);
        drawBox.union(drawBox.right + 0.2f * lineWidth,
                staffBoundingBox.bottom + lineWidth);
        float radius = drawBox.width() / 3;
        canvas.drawRoundRect(drawBox, radius, radius, highlightPaint);
        canvas.drawRoundRect(drawBox, radius, radius, borderPaint);
    }
}

