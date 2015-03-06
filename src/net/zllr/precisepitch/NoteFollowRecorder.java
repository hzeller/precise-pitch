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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import net.zllr.precisepitch.model.DisplayNote;
import net.zllr.precisepitch.model.MeasuredPitch;
import net.zllr.precisepitch.model.NoteDocument;
import net.zllr.precisepitch.view.CombineAnnotator;
import net.zllr.precisepitch.view.HighlightAnnotator;
import net.zllr.precisepitch.view.StaffView;

// Given a note model and a staff view, listen to the player and make them
// follow the notes.
//  - Highlights the current note to be played.
//  - displays a 'clock' as long as note is in-tune
//  - moves on until it reaches the end.
public class NoteFollowRecorder {
    // For debugging: less whisteling needed :)
    private static boolean isAutoFollow = false;

    // Color of notes we already have finished (green).
    private static final int kFinishedNoteColor = Color.rgb(0, 180, 0);
    // Color of the current note to be played.
    private static final int kCurrentNoteColor = Color.BLACK;
    // Color of notes to play later (grayed out).
    private static final int kFutureNoteColor = Color.rgb(200, 200, 200);

    private static final int kHightlightColor = Color.argb(70, 0xff, 0xff, 0);
    private final static int kHoldTime = 15;

    private final StaffView staff;
    private final NoteDocument model;
    private final EventListener eventListener;
    private final CombineAnnotator highlightAnnotator;
    private final PitchReceiver handler;
    private PitchSource pitchPoster;
    private int modelPos;
    private int ticksInTune;
    private boolean running;

    // A eventListener for events happening while following notes.
    public interface EventListener {
        // Start following the given model.
        void onStartModel(NoteDocument model);

        // We're done following. Users might consider changing the colors of
        // the notes back.
        void onFinishedModel(NoteDocument model);

        // Start a new note. The following calls until onFinishNote() will
        // be in this context.
        void onStartNote(int modelPos, DisplayNote note);

        // Received note is not the one currently expected in the model.
        // This is the number of half-notes it is off from what we expect.
        // Since we fold all octaves, this can be in the range of +/- 6
        void onNoteMiss(int diff);

        // Didn't receive pitch data.
        void onSilence();

        // The note was found, this callback needs to decide if the given range
        // of cents is acceptable (-50..+50)
        boolean isInTune(double cent, int ticksInTuneSoFar);

        // Done with the current note started in onStartNote()
        void onFinishedNote();
    }

    public NoteFollowRecorder(StaffView staff, EventListener eventListener) {
        this.staff = staff;
        this.eventListener = eventListener;
        this.model = staff.getNoteModel();
        running = true;
        modelPos = -1;
        advanceNote();

        handler = new PitchReceiver();
        highlightAnnotator = new CombineAnnotator();
        highlightAnnotator.addAnnotator(new ClockAnnotator(handler));
        highlightAnnotator.addAnnotator(new HighlightAnnotator(kHightlightColor));
        eventListener.onStartModel(model);
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
                n.color = kCurrentNoteColor;
                n.annotator = highlightAnnotator;
            }
            else {
                n.color = kFutureNoteColor;
            }
        }
        staff.ensureNoteInView(modelPos);
        if (running && pitchPoster == null) {
            if (isAutoFollow) {
                pitchPoster = new DebugPitchSource();
                ((DebugPitchSource)pitchPoster).setExpectedPitch(model.get(modelPos).getFrequency());
            } else {
                pitchPoster = new MicrophonePitchSource();
            }
            pitchPoster.setHandler(handler);
            pitchPoster.startSampling();
        }
    }

    private void advanceNote() {
        DisplayNote currentNote;
        if (modelPos >= 0) {
            currentNote = model.get(modelPos);
            currentNote.color = kFinishedNoteColor;
            currentNote.annotator = null;
            eventListener.onFinishedNote();
        }

        ++modelPos;
        if (modelPos >= model.size()) {
            pause();
            running = false;
            eventListener.onFinishedModel(model);
            staff.onModelChanged();  // post the last change.
            return;
        }
        currentNote = model.get(modelPos);
        currentNote.color = kCurrentNoteColor;
        currentNote.annotator = highlightAnnotator;
        eventListener.onStartNote(modelPos, currentNote);
        if (isAutoFollow && pitchPoster instanceof DebugPitchSource) {
            ((DebugPitchSource) pitchPoster).setExpectedPitch(currentNote.getFrequency());
        }
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

    // The PitchReceiver receives the messages from the MicrophonePitchSource.
    private class PitchReceiver extends Handler implements ProgressProvider {
        // --- interface ProgressProvider
        public int getMaxProgress() { return kHoldTime; }
        public int getCurrentProgress() { return ticksInTune; }

        public void handleMessage(Message msg) {
            if (!running)
                return;  // Received a sample, but we're done already.
            if (msg.obj == null) {
                eventListener.onSilence();
                return;
            }
            final MeasuredPitch data = (MeasuredPitch) msg.obj;
            final int beforeTicks = ticksInTune;

            final DisplayNote expectedNote = model.get(modelPos);
            int gotNote = data.note;
            int wantNote = expectedNote.note;
            // Ignore harmonics for now: as long as it is the same note, no
            // matter the octave: good.
            int noteDiff = (gotNote + 12 - wantNote + 6) % 12 - 6;
            if (noteDiff == 0) {
                if (eventListener.isInTune(data.cent, ticksInTune)) {
                    ++ticksInTune;
                } else {
                    --ticksInTune;  // wrong cent: one penalty
                }
            } else {
                ticksInTune -= 2;  // different note: two penalty
                eventListener.onNoteMiss(noteDiff);
            }
            if (ticksInTune < 0)   // too much penalty accrued :)
                ticksInTune = 0;

            if (ticksInTune >= kHoldTime) {
                advanceNote();
            }
            if (beforeTicks != ticksInTune) {
                staff.onModelChanged();  // force redraw ('clock')
            }
        }
    }

    // We provide our own annotator to display the timing information.
    private static final class ClockAnnotator implements DisplayNote.Annotator {
        private final Paint progressPaint;
        private final Paint borderPaint;
        private final ProgressProvider progressProvider;

        public ClockAnnotator(ProgressProvider progress) {
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
