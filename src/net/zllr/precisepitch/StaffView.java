// A view of notes that are displayed as staff.
package net.zllr.precisepitch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

class StaffView extends View {
    // TODO: the following as result of measuring.
    private static int staffHeight = 300;
    private static int lineDistance = staffHeight / 7;

    // The note name is essentially encoding the 8 positions on the staff, with an additional
    // character describing if this is sharp or flat. For the 'flat' version, we encode
    // 9 positions on the staff as it is essentially the first note of the next octave.
    // The letters are purely encoding these positions and are choosen as letters for easier
    // debug output, but they don't have any other meaning otherwise.
    private static final String noteNames[][] = {
        { "A", "Bb", "B", "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Hb" /* H = G + 1 */},
        { "A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#" },
    };
    private int keyDisplay;

    public StaffView(Context context) {
        this(context, null);
    }

    public StaffView(Context context, AttributeSet attributes) {
        super(context, attributes);
        staffPaint = new Paint();
        staffPaint.setColor(Color.BLACK);
        staffPaint.setStrokeWidth(staffHeight / (5 * 10));  // 10% between lines

        notePaint = new Paint();
        notePaint.setColor(Color.BLACK);
        notePaint.setStrokeWidth(staffHeight / 100);
        notePaint.setStyle(Paint.Style.FILL);
        notePaint.setTextSize(2.5f * lineDistance);

        backgroundColor = new Paint();
        backgroundColor.setColor(Color.WHITE);
        currentNote = 10;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We want to fill everything.
        int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (measureHeight > 3 * staffHeight)
            measureHeight = 3 * staffHeight;
        setMeasuredDimension(measureWidth, measureHeight);
    }

    public void setKeyDisplay(int k) {
        if (k != keyDisplay) {
            keyDisplay = k;
            invalidate();
        }
    }
    // Push a note, 0 being 55Hz A, -1 invalid.
    public void pushNote(int note) {
        if (note != currentNote) {
            currentNote = note;  // TODO: keep short history.
            invalidate();
        }
    }

    private int getNotePosition() {
        final int octave = currentNote / 12;
        final String notename = noteNames[keyDisplay][currentNote % 12];
        final int position = (notename.charAt(0) - 'A') + 7 * octave;
        return position - 6;  // relative to lowest line.
    }

    protected void onDraw(Canvas canvas) {
        canvas.drawPaint(backgroundColor);
        final int lineDistance = staffHeight / 7;
        final int origin = (canvas.getHeight() - 4 * lineDistance) / 2
                + lineDistance;  // need more space at the top.
        for (int i = 0; i < 5; ++i) {
            final int posY = origin + i * lineDistance;
            canvas.drawLine(0, posY, canvas.getWidth(), posY, staffPaint);
        }
        if (currentNote < 0)
            return;
        final int centerY = origin + 4 * lineDistance
                - (getNotePosition() * lineDistance/2);
        final int centerX = canvas.getWidth() / 3;
        RectF oval = new RectF(centerX - 0.7f * lineDistance, centerY - 0.5f * lineDistance,
                               centerX + 0.7f * lineDistance, centerY + 0.5f * lineDistance);
        canvas.drawOval(oval, notePaint);
        String notename = noteNames[keyDisplay][currentNote % 12];
        if (notename.length() > 1) {
            String accidental = "";
            switch (notename.charAt(1)) {
                case '#': accidental = "♯"; break;
                case 'b': accidental = "♭"; break;
            }
            canvas.drawText(accidental,
                            centerX + 0.7f * lineDistance,
                            centerY + 0.5f * lineDistance,
                            notePaint);
        }
    }

    private final Paint staffPaint;
    private final Paint notePaint;
    private final Paint backgroundColor;
    private int currentNote;
}

