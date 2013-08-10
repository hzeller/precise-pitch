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

// A view of notes that are displayed as staff.
package net.zllr.precisepitch;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;

class StaffView extends View {
    public static final class Note {
        // Immutable struct to represent a note to display.
        public Note(int pitch, int duration, int color) {
            this.pitch = pitch;
            this.duration = duration;
            this.color = color;
        }

        @Override
        public boolean equals(Object other) {
            if (other == null || !(other instanceof Note))
                return false;
            Note on = (Note) other;
            return on.pitch == pitch && on.duration == pitch && on.color == color;
        }

        // 0 for low A at 55Hz, 1 for A#.. 36 for A at 440Hz
        public final int pitch;

        // 1=full note 4=1/4 note ... Ignored for now, only 1/4 work.
        public final int duration;

        // Color to display. Standard Android color representation.
        public final int color;
    }

    // The note name is essentially encoding the 8 positions on the staff, with an additional
    // character describing if this is sharp or flat. For the 'flat' version, we encode
    // 9 positions on the staff as it is essentially the first note of the next octave.
    // The letters are purely encoding these positions and are chosen as letters for easier
    // debug output, but they don't have any other meaning otherwise.
    private static final String noteNames[][] = {
        { "A", "Bb", "B", "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Hb" /* H = G + 1 */},
        { "A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#" },
    };

    // The range we want to display. We want to display from 65Hz low C (2.5 lines
    // below staff to 440Hz A (4 lines above staff)
    private static final int kLowDisplayRange  = 3;  // below lowest line
    private static final int kHighDisplayRange = 4;  // above highest line
    private static final int kTotalDisplayRange = kLowDisplayRange + 4 + kHighDisplayRange;

    public StaffView(Context context) {
        this(context, null);
    }

    public StaffView(Context context, AttributeSet attributes) {
        super(context, attributes);
        staffPaint = new Paint();
        staffPaint.setColor(Color.BLACK);

        backgroundColor = new Paint();
        backgroundColor.setColor(Color.WHITE);

        notes = new ArrayList<Note>();
        setNotesPerStaff(4);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int lineDistance = h / kTotalDisplayRange;
        noteRenderer = new NoteRenderer(lineDistance);
        staffPaint.setStrokeWidth(lineDistance / 10);  // 10% between lines
    }

    public void setKeyDisplay(int k) {
        if (k != keyDisplay) {
            keyDisplay = k;
            invalidate();
        }
    }

    public void pushNote(Note note) {
        notes.add(note);
        int tooMany = notes.size() - notesPerStaff;
        while (tooMany > 1) {  // we allow for one more to do animations.
            notes.remove(0);
            tooMany--;
        }
        invalidate();
    }

    public void setNotesPerStaff(int maxnotes) {
        this.notesPerStaff = maxnotes;
    }

    public void setNotes(Collection<Note> newNotes) {
        notes.clear();
        notes.addAll(newNotes);
        invalidate();
    }

    private int getNotePosition(Note n) {
        final int octave = n.pitch / 12;
        final String noteName = noteNames[keyDisplay][n.pitch % 12];
        final int position = (noteName.charAt(0) - 'A') + 7 * octave;
        return position - 6;  // relative to lowest line.
    }

    protected void onDraw(Canvas canvas) {
        canvas.drawPaint(backgroundColor);
        // Interesting thing to find out: for some reason canvas.getHeight()
        // and this.getHeight() are different on the Android 2.3 device.
        // I would've expected that we get a canvas of the same size ?
        final int lineDistance = getHeight() / kTotalDisplayRange;
        final int originY = (getHeight() - 4 * lineDistance
                                     - kLowDisplayRange * lineDistance);

        // Draw staff.
        for (int i = 0; i < 5; ++i) {
            final int posY = originY + i * lineDistance;
            canvas.drawLine(0, posY, canvas.getWidth(), posY, staffPaint);
        }

        if (notesPerStaff == 0)
            return;

        // We want notes not to be spaced too much apart
        int maxNoteDistance = noteRenderer.getWidth() * 3;
        int noteDistance = canvas.getWidth() / notesPerStaff;
        if (noteDistance > maxNoteDistance)
            noteDistance = maxNoteDistance;
        int posX = noteDistance;
        // TODO: add animation offset.
        int animationDistance = noteDistance;
        if (notes.size() > notesPerStaff) {
            posX -= animationDistance + 20;
        }
        for (Note n : notes) {
            final int centerX = posX;
            posX += noteDistance;
            if (n == null)
                continue;
            final int notePos = getNotePosition(n);
            final int centerY = originY + 4 * lineDistance
                    - (notePos * lineDistance/2);
            final float barLength = 3.2f * lineDistance;
            final String noteName = noteNames[keyDisplay][n.pitch % 12];
            final float noteOffset
                    = noteRenderer.draw(canvas, centerX, centerY,
                                    noteName, notePos < 4 ? barLength : -barLength,
                                    n.color);

            // The help-lines.
            final Paint helpLinePaint = new Paint(staffPaint);
            helpLinePaint.setColor(n.color);
            final float helpLeft = centerX - 1.8f * noteOffset;
            final float helpRight = centerX + 1.8f * noteOffset;
            for (int i = notePos / 2; i < 0; ++i) {  // below lowest line
                canvas.drawLine(helpLeft, originY + 4 * lineDistance - i * lineDistance,
                                helpRight, originY + 4 * lineDistance - i * lineDistance,
                                helpLinePaint);
            }
            for (int i = 5; i <= notePos / 2; ++i) {  // above highest line
                canvas.drawLine(helpLeft, originY + 4 * lineDistance - i * lineDistance,
                                helpRight, originY + 4 * lineDistance - i * lineDistance,
                                helpLinePaint);
            }
        }
    }

    private static class NoteRenderer {
        public NoteRenderer(float height) {
            notePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            notePaint.setColor(Color.BLACK);
            notePaint.setStyle(Paint.Style.FILL);
            notePaint.setStrokeWidth(height / 10);
            notePaint.setTextSize(1.8f * height);

            // Drawing some oval in a bitmap. We use that later for the note.
            final Paint ovalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            ovalPaint.setColor(Color.BLACK);
            ovalPaint.setStrokeWidth(0);
            ovalPaint.setStyle(Paint.Style.FILL);

            // Unscientific attempt to make it look pleasing.
            float ovalWidth = 1.4f * height;
            float ovalHeight = 0.95f * height;
            float angle = -30.0f;
            noteOffsetX = (0.85f * ovalWidth) / 2.0f;
            noteOffsetY = (0.3f * ovalHeight) / 2.0f;

            Bitmap ovalTemplate = Bitmap.createBitmap((int)ovalWidth,
                                                      (int)ovalWidth,
                                                      Bitmap.Config.ALPHA_8);
            Canvas c = new Canvas(ovalTemplate);

            Matrix tiltMatrix = new Matrix();
            tiltMatrix.postRotate(angle, ovalTemplate.getWidth()/2.0f,
                                  ovalTemplate.getHeight()/2.0f);
            c.setMatrix(tiltMatrix);
            float offsetY = (ovalTemplate.getHeight() - ovalHeight)/2.0f;
            RectF r = new RectF(0, offsetY, ovalWidth, ovalHeight + offsetY);
            c.drawOval(r, ovalPaint);
            noteBitmap = ovalTemplate;
        }

        // Rough box a raw note fits into (but without accidental.)
        public int getWidth() { return noteBitmap.getWidth(); }

        // Draw note body into canvas, centered around "centerX" and "centerY".
        // The length of the bar to drawl is given in "barLength";
        // pointing upwards if positive, downwards if negative.
        // Returns width of head.
        public float draw(Canvas c, float centerX, float centerY,
                          String noteName, float barLength,
                          int color) {
            final float noteLeft = centerX - noteOffsetX;
            final float noteRight = centerX + noteOffsetX;

            final Paint localNotePaint = new Paint(notePaint);
            localNotePaint.setColor(color);
            c.drawBitmap(noteBitmap,
                         centerX - 0.5f * noteBitmap.getWidth(),
                         centerY - 0.5f * noteBitmap.getHeight(),
                         localNotePaint);

            if (barLength > 0) {
                c.drawLine(noteRight, centerY - noteOffsetY,
                           noteRight, centerY - barLength, localNotePaint);
            } else {
                c.drawLine(noteLeft, centerY + noteOffsetY,
                           noteLeft, centerY - barLength, localNotePaint);
            }
            if (noteName.length() > 1) {
                float accidentalOffsetY = 0.0f;
                String accidental = "";
                switch (noteName.charAt(1)) {
                    case '#':
                        accidental = "♯";
                        accidentalOffsetY = 0.5f * noteBitmap.getHeight();
                        break;
                    case 'b':
                        accidental = "♭";
                        accidentalOffsetY = 0.3f * noteBitmap.getHeight();
                        break;
                }
                c.drawText(accidental,
                           centerX - 4.0f * noteOffsetX,
                           centerY + accidentalOffsetY,
                           localNotePaint);
            }
            return noteOffsetX;
        }

        private final Paint notePaint;
        private final Bitmap noteBitmap;
        private final float noteOffsetX;
        private final float noteOffsetY;
    }

    private final Paint staffPaint;
    private final Paint backgroundColor;

    private NoteRenderer noteRenderer;   // changes when size changes.
    private int keyDisplay;
    private int notesPerStaff;
    private ArrayList<Note> notes;
}

