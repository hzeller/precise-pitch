package net.zllr.precisepitch;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import net.zllr.precisepitch.model.DisplayNote;
import net.zllr.precisepitch.model.MeasuredPitch;
import net.zllr.precisepitch.view.StaffView;

import java.util.List;

// Given a note model and a staff view, listen to the player and make them
// follow the notes.
//  - Highlights the current note to be played.
//  - displays a 'clock' as long as note is in-tune
//  - moves on until it reaches the end.
public class NoteFollowRecorder {
    private static final int kFinishedNoteColor = Color.rgb(0, 180, 0);
    private static final int kCurrentNoteColor = Color.BLACK;
    private static final int kFutureNoteColor = Color.rgb(200, 200, 200);

    private final static int kHoldTime = 15;

    private final StaffView staff;
    private final List<DisplayNote> model;
    private final Listener listener;
    private final HighlightAndClockAnnotator highlightAnnotator;
    private final HandlerImplementation handler;
    private MicrophonePitchPoster pitchPoster;
    private int modelPos;
    private int ticksInTune;
    private boolean running;

    // A listener for events happening while following notes.
    public interface Listener {
        // Received note is not the one expected in the model.
        void onNoteMiss();
        // Return if the given cent are accepted to be in-tune.
        boolean isInTune(DisplayNote note, double cent);
        void onFinishedNote(DisplayNote note);
        void onFinishedModel();
    }

    public NoteFollowRecorder(StaffView staff, Listener listener) {
        this.staff = staff;
        this.listener = listener;
        this.model = staff.getNoteModel();
        running = true;
        modelPos = -1;
        advanceNote();

        handler = new HandlerImplementation();
        highlightAnnotator = new HighlightAndClockAnnotator(handler);
        resume(0);
    }

    public int getPosition() { return modelPos; }

    public void pause() {
        if (pitchPoster == null) return;
        pitchPoster.stopSampling();
        pitchPoster = null;
    }

    public void resume(int position) {
        modelPos = position;
        for (int i = 0; i < model.size(); ++i) {
            DisplayNote n = model.get(i);
            n.annotator = null;
            if (i < modelPos) {
                n.color = kFinishedNoteColor;
            }
            else if (i == modelPos) {
                n.color = kFinishedNoteColor;
                n.annotator = highlightAnnotator;
            }
            else {
                n.color = kFutureNoteColor;
            }
        }
        if (running && pitchPoster == null) {
            pitchPoster = new MicrophonePitchPoster(60 /*Hz*/);
            pitchPoster.setHandler(handler);
            pitchPoster.start();
        }
    }

    private void advanceNote() {
        DisplayNote currentNote;
        if (modelPos >= 0) {
            currentNote = model.get(modelPos);
            currentNote.color = kFinishedNoteColor;
            currentNote.annotator = null;
        }

        ++modelPos;
        if (modelPos >= model.size()) {
            pause();
            running = false;
            listener.onFinishedModel();
            staff.onModelChanged();  // post the last change.
            return;
        }
        currentNote = model.get(modelPos);
        currentNote.color = kCurrentNoteColor;
        currentNote.annotator = highlightAnnotator;
        ticksInTune = 0;
        staff.ensureNoteInView(modelPos);
        staff.onModelChanged();
    }

    // Some abstraction of progress.
    private interface ProgressProvider {
        int getMaxProgress();
        int getCurrentProgress();
    }

    // Most of our implementation is in this inner class, extending and
    // implementing other interfaces that we don't want to leak into the public
    // interface.
    private class HandlerImplementation extends Handler implements ProgressProvider {
        // --- interface ProgressProvider
        public int getMaxProgress() { return kHoldTime; }
        public int getCurrentProgress() { return ticksInTune; }

        public void handleMessage(Message msg) {
            if (!running || msg.obj == null)
                return;  // Received a sample, but we're done already.
            final MeasuredPitch data = (MeasuredPitch) msg.obj;
            final int beforeTicks = ticksInTune;

            final DisplayNote expectedNote = model.get(modelPos);
            int gotNote = data.note;
            int wantNote = expectedNote.note;
            // Ignore harmonics for now: as long as it is the same note, no
            // matter the octave: good.
            int noteDiff = (gotNote + 12 - wantNote + 6) % 12 - 6;
            if (noteDiff == 0) {
                if (listener.isInTune(expectedNote, data.cent)) {
                    ++ticksInTune;
                } else {
                    --ticksInTune;  // wrong cent: one penalty
                }
            }
            else {
                // Negative or positive: exhaust range, so show arrows.
                ticksInTune -= 2;  // different note: two penalty
                listener.onNoteMiss();
            }
            if (ticksInTune < 0)
                ticksInTune = 0;

            if (ticksInTune >= kHoldTime) {
                listener.onFinishedNote(expectedNote);
                advanceNote();
            }
            if (beforeTicks != ticksInTune) {
                staff.onModelChanged();  // force redraw ('clock')
            }
        }
    }

    private static final class HighlightAndClockAnnotator implements DisplayNote.Annotator {
        private final Paint highlightPaint;
        private final Paint borderPaint;
        private final Paint progressPaint;
        private final ProgressProvider progressProvider;

        public HighlightAndClockAnnotator(ProgressProvider progress) {
            highlightPaint = new Paint();
            highlightPaint.setColor(Color.argb(70, 0xff, 0xff, 0));
            highlightPaint.setStrokeWidth(0);
            borderPaint = new Paint();
            borderPaint.setColor(Color.BLACK);
            borderPaint.setStrokeWidth(0);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setAntiAlias(true);
            progressPaint = new Paint();
            progressPaint.setColor(kFinishedNoteColor);
            progressProvider = progress;
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

            float centerY;
            float clearanceBottom = canvas.getHeight() - drawBox.bottom;
            if (clearanceBottom > drawBox.top) {
                centerY = drawBox.bottom + clearanceBottom / 2;
            } else {
                centerY = drawBox.top / 2;
            }
            final float timerRadius = lineWidth;
            float centerX = drawBox.left + (drawBox.right - drawBox.left) / 2;
            RectF timerBox = new RectF(centerX - timerRadius, centerY - timerRadius,
                                       centerX + timerRadius, centerY + timerRadius);
            float clockDegrees = 360.0f * progressProvider.getCurrentProgress()
                    / progressProvider.getMaxProgress();
            canvas.drawArc(timerBox, -90, clockDegrees, true, progressPaint);
            canvas.drawOval(timerBox, borderPaint);
        }
    }
}
