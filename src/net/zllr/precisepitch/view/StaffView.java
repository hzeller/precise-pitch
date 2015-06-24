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
package net.zllr.precisepitch.view;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;

import net.zllr.precisepitch.model.DisplayNote;
import net.zllr.precisepitch.model.NoteDocument;

import java.util.*;

public class StaffView extends View {

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
    private static final int kHighDisplayRange = 5;  // above highest line
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

        setNotesPerStaff(4);
        noteInView = -1;
    }

    public int getNoteWidth() { return noteRenderer != null ? noteRenderer.getWidth() : 0; }

    // Set model. A list of notes to display.
    public void setNoteModel(NoteDocument model) {
        if (model != this.model) {
            this.model = model;
            onModelChanged();
        }
    }

    // Returns the model used. If you ever modify it or the contents, you need
    // to call onModelChanted()
    public NoteDocument getNoteModel() {
        return model;
    }

    // Call this method whenever the model changed (number of notes or
    // any element in the model.
    public void onModelChanged() {
        invalidate();
        requestLayout();
    }

    public void ensureNoteInView(int n) {
        if (model == null || model.size() == 0) return;
        // The first couple of notes we show directly, then start to scroll gently.
        final int kStartScrollingAt = 1;
        final int kEndScrollingAt = 2;
        n -= kStartScrollingAt;
        if (n < 0) n = 0;
        int totalScroll = model.size() - kStartScrollingAt - kEndScrollingAt;
        if (totalScroll <= 0) totalScroll = 1;  // Uh, short model, heh?
        // We know that we're embedded in a HorizontalScrollView, cast is safe.
        HorizontalScrollView scroller = (HorizontalScrollView) getParent();
        int scrollLongerThanScreen = getWidth() - scroller.getWidth();
        scroller.smoothScrollTo(scrollLongerThanScreen * n / totalScroll, 0);
    }

    // Set number of notes to be displayed along the length of the staff.
    public void setNotesPerStaff(int maxnotes) {
        this.notesPerStaff = maxnotes;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (model != null && model.size() > 0) {
            width = model.size() * getNoteDistance() + getNoteDistance()/2;
        }
        if (width < getSuggestedMinimumWidth()) width = getSuggestedMinimumWidth();
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (height < getSuggestedMinimumHeight()) height = getSuggestedMinimumHeight();
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (h <= 0) return;
        int lineDistance = h / kTotalDisplayRange;
        noteRenderer = new NoteRenderer(lineDistance);
        staffPaint.setStrokeWidth(lineDistance / 10);  // 10% between lines
    }

    private int getNotePosition(DisplayNote n) {
        final int octave = n.note / 12;
        final String noteName = noteNames[model.isFlat() ? 0 : 1][n.note % 12];
        final int position = (noteName.charAt(0) - 'A') + 7 * octave;
        return position - 6;  // relative to lowest line.
    }

    private int getNoteDistance() {
        return getHeight() / 4;
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
        RectF staffBoundingBox = new RectF(0, originY, canvas.getWidth(),
                                           originY + 4 * lineDistance);

        if (notesPerStaff == 0 || model == null)
            return;

        int noteDistance = getNoteDistance();
        int notesToDisplay = model.size();

        int lastNoteInModelToDisplay = model.size() - 1;
        if (noteInView >= 0) {
            // place it in the middle of the view.
            lastNoteInModelToDisplay = noteInView + (notesToDisplay / 2) - 1;
        }
        lastNoteInModelToDisplay = Math.max(notesToDisplay-1, lastNoteInModelToDisplay);
        lastNoteInModelToDisplay = Math.min(lastNoteInModelToDisplay, model.size() - 1);

        // Rightmost position to display.
        int posX = (noteDistance * Math.min(notesToDisplay, model.size()) // rightmost note.
                - (noteDistance / 2)  // center of note is here
                + (getWidth() - notesToDisplay * noteDistance)/2);  // center globally

        // TODO: add animation offset (should be a float-value 0..1).
        ListIterator<DisplayNote> it = model.getNotes().listIterator(lastNoteInModelToDisplay + 1);
        while (it.hasPrevious() && posX > -noteDistance) {
            DisplayNote n = it.previous();
            final int centerX = posX;
            posX -= noteDistance;
            if (n == null)
                continue;
            final int notePos = getNotePosition(n);
            final int centerY = originY + 4 * lineDistance
                    - (notePos * lineDistance/2);

            float barLength = 3.2f * lineDistance;
            barLength = Math.max(barLength, (notePos - 4) * lineDistance / 2);
            barLength = Math.max(barLength, (4 - notePos) * lineDistance / 2);
            final String noteName = noteNames[model.isFlat() ? 0 : 1][n.note % 12];
            RectF noteBoundingBox = new RectF();
            final float noteOffset
                    = noteRenderer.draw(canvas, centerX, centerY,
                                    noteName, notePos < 4 ? barLength : -barLength,
                                    n.color, noteBoundingBox);

            // The help-lines.
            final Paint helpLinePaint = new Paint(staffPaint);
            helpLinePaint.setColor(n.color);
            final float helpLeft = centerX - 1.8f * noteOffset;
            final float helpRight = centerX + 1.8f * noteOffset;
            noteBoundingBox.union(helpLeft, centerY);
            noteBoundingBox.union(helpRight, centerY);
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

            if (n.annotator != null) {
                n.annotator.draw(n, canvas, staffBoundingBox, noteBoundingBox);
            }
        }
    }

    private static class NoteRenderer {
        public NoteRenderer(float height) {
            notePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            notePaint.setColor(Color.BLACK);
            notePaint.setStyle(Paint.Style.FILL);
            notePaint.setStrokeWidth(height / 10);

            // With Android L, the font started to have different size for b and #
            sharpNotePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            sharpNotePaint.setColor(Color.BLACK);
            sharpNotePaint.setTextSize(2.0f * height);
            sharpNotePaint.setStyle(Paint.Style.STROKE);

            flatNotePaint = new Paint(sharpNotePaint);
            // Old and new Androids are different here. Choose something in the middle.
            flatNotePaint.setTextSize(2.2f * height);

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
                          int color, RectF boundingBox) {
            final float noteLeft = centerX - noteOffsetX;
            final float noteRight = centerX + noteOffsetX;

            final Paint localNotePaint = new Paint(notePaint);
            localNotePaint.setColor(color);
            c.drawBitmap(noteBitmap,
                         centerX - 0.5f * noteBitmap.getWidth(),
                         centerY - 0.5f * noteBitmap.getHeight(),
                         localNotePaint);
            boundingBox.union(new RectF(centerX - 0.5f * noteBitmap.getWidth(),
                                        centerY - 0.5f * noteBitmap.getHeight(),
                                        centerX + 0.5f * noteBitmap.getWidth(),
                                        centerY + 0.5f * noteBitmap.getHeight()));
            if (barLength > 0) {
                c.drawLine(noteRight, centerY - noteOffsetY,
                           noteRight, centerY - barLength, localNotePaint);
            } else {
                c.drawLine(noteLeft, centerY + noteOffsetY,
                           noteLeft, centerY - barLength, localNotePaint);
            }
            boundingBox.union(centerX, centerY - barLength);

            if (noteName.length() > 1) {
                // Accidental writing is currently only tested on Android L. Might be off with
                // earlier versions :/
                float accidentalOffsetYFactor = 0.0f;
                Paint textPaint = null;
                String accidental = "";
                switch (noteName.charAt(1)) {
                    case '#':
                        accidental = "\u266F";  // ♯
                        accidentalOffsetYFactor = 0.5f;
                        textPaint = new Paint(sharpNotePaint);
                        break;
                    case 'b':
                        accidental = "\u266D";  // ♭
                        accidentalOffsetYFactor = 0.30f;
                        textPaint = new Paint(flatNotePaint);
                        break;
                }
                if (textPaint != null) {
                    textPaint.setColor(color);
                    Rect tb = new Rect();
                    textPaint.getTextBounds(accidental, 0, 1, tb);
                    // Older Android versions seem to have an offset != (0,0)
                    int tOffsetX = tb.left;
                    int tOffsetY = tb.bottom;
                    tb.offset((int) (centerX-0.5f * noteBitmap.getWidth() - tb.width() - tOffsetX),
                            (int) (centerY + accidentalOffsetYFactor * tb.height() - tOffsetY));
                    //c.drawRect(tb, textPaint);
                    c.drawText(accidental, tb.left - tOffsetX, tb.bottom - tOffsetY, textPaint);
                    RectF noteBoundingExtension = new RectF(tb);
                    noteBoundingExtension.offset(-tb.width()/3, 0);
                    boundingBox.union(noteBoundingExtension);
                }
            }
            return noteOffsetX;
        }

        private final Paint notePaint;
        private final Paint sharpNotePaint;
        private final Paint flatNotePaint;
        private final Bitmap noteBitmap;
        private final float noteOffsetX;
        private final float noteOffsetY;
    }

    private final Paint staffPaint;
    private final Paint backgroundColor;

    private NoteRenderer noteRenderer;   // changes when size changes.
    private int notesPerStaff;
    private NoteDocument model;
    private int noteInView;
}

