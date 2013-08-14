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

// Beware, this is a bit hacky right now

package net.zllr.precisepitch;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PracticeActivity extends Activity {
    private static final String BUNDLE_KEY_MODEL = "PracticeActivity.model";

    private static final int kMajorScaleSequence[] = { 2, 2, 1, 2, 2, 2, 1 };
    private static final int kCentThreshold = 20;

    private static final int futureNoteColor = Color.rgb(200, 200, 200);
    private static final int successNoteColor = Color.rgb(0, 180, 0);
    private static final int playNoteColor  = Color.BLACK;

    // All the activity state that we need to keep track of between teardown
    // restart.
    private static class ActivityState implements Serializable {
        public ActivityState() {
            noteModel = new ArrayList<StaffView.Note>();
        }
        final ArrayList<StaffView.Note> noteModel;
        int keyDisplay = 1;
        boolean checkedRandom;
    };
    private ActivityState istate;

    private StaffView staff;
    private CheckBox randomTune;
    private Button cmajor;
    private Button gmajor;
    private Button dmajor;
    private Button fmajor;
    private Button bbmajor;
    private CenterOffsetView ledview;
    private Button startbutton;
    private TextView instructions;
    private Button restartbutton;
    private PitchFollowGame game;

    private enum State {
        EMPTY_SCALE,     // initial state or after 'clear'
        WAIT_FOR_START,  // when notes are visible. Start button visible.
                         // TODO: should be automatic when !isEmpty()
        PRACTICE,        // The game.
        FINISHED         // finished assignment.
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.practice);

        staff = (StaffView) findViewById(R.id.practiceStaff);
        staff.setNotesPerStaff(16);

        // For now we have a couple of buttons to create some basics, but
        // these should be replaced by: (a) Spinner (for choosing scales and randomTune)
        // and (b) direct editing.

        final NoteGenerationListener noteCreator = new NoteGenerationListener();
        randomTune = (CheckBox) findViewById(R.id.randomSequence);
        cmajor = (Button) findViewById(R.id.newCMajor);
        cmajor.setOnClickListener(noteCreator);
        gmajor = (Button) findViewById(R.id.newGMajor);
        gmajor.setOnClickListener(noteCreator);
        dmajor = (Button) findViewById(R.id.newDMajor);
        dmajor.setOnClickListener(noteCreator);
        fmajor = (Button) findViewById(R.id.newFMajor);
        fmajor.setOnClickListener(noteCreator);
        bbmajor = (Button) findViewById(R.id.newBbMajor);
        bbmajor.setOnClickListener(noteCreator);
        ledview = (CenterOffsetView) findViewById(R.id.practiceLedDisplay);
        ledview.setQuantization(3);
        ledview.setRange(Math.min(50, kCentThreshold + 10));
        ledview.setKeepScreenOn(true);
        startbutton = (Button) findViewById(R.id.practiceStartButton);
        startbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doPractice();
            }
        });
        restartbutton = (Button) findViewById(R.id.practiceRestartButton);
        restartbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setActivityState(State.FINISHED);
                setActivityState(State.EMPTY_SCALE);
                setActivityState(State.WAIT_FOR_START);  // We already have notes
            }
        });
        instructions = (TextView) findViewById(R.id.practiceInstructions);

        if (savedInstanceState != null) {
            istate = (ActivityState) savedInstanceState.getSerializable(BUNDLE_KEY_MODEL);
        }
        if (istate == null) {
            istate = new ActivityState();
        }
        staff.setNoteModel(istate.noteModel);

        staff.ensureNoteInView(0);
        setActivityState(State.EMPTY_SCALE);  // need to walk this state first.
        if (!istate.noteModel.isEmpty()) {
            setActivityState(State.WAIT_FOR_START);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        istate.keyDisplay = staff.getKeyDisplay();
        istate.checkedRandom = randomTune.isChecked();
        if (game != null) {
            game.stop();  // TODO: store game state.
            setActivityState(State.FINISHED);
            // Now prepare for a new game, with properly reset notes before
            // the state is serialized (the Paint in note-annotators doesn't serialize).
            setActivityState(State.EMPTY_SCALE);
            setActivityState(State.WAIT_FOR_START);  // We already have notes
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Do I really have to remember state for Views ?
        staff.setKeyDisplay(istate.keyDisplay);
        randomTune.setChecked(istate.checkedRandom);
    }

    @Override
    protected void onSaveInstanceState(Bundle saveState) {
        saveState.putSerializable(BUNDLE_KEY_MODEL, istate);
    }

    // Kinda hardcoded now :)
    private final class NoteGenerationListener implements View.OnClickListener {
        public void onClick(View button) {
            boolean wantsFlat = false;
            List<StaffView.Note> model = istate.noteModel;
            int startNote = model.size() > 0 ? model.get(0).pitch : 0;
            model.clear();

            // Use lowest note unless that is already set: then choose one
            // octave higher. That way, we can 'toggle' between two octaves.
            if (button == cmajor) {
                startNote = (startNote == 3) ? 15 : 3;
            } else if (button == gmajor) {
                startNote = (startNote == 10) ? 22 : 10;
            } else if (button == dmajor) {
                startNote = (startNote == 5) ? 17 : 5;
            } else if (button == fmajor) {
                startNote = (startNote == 8) ? 20 : 8;
                wantsFlat = true;
            } else if (button == bbmajor) {
                startNote = (startNote == 13) ? 25 : 13;
                wantsFlat = true;
            }

            if (randomTune.isChecked()) {
                addRandomMajorSequence(startNote, model, 16);
            } else {
                addAscDescMajorScale(startNote, model);
            }
            staff.ensureNoteInView(0);
            staff.setKeyDisplay(wantsFlat ? 0 : 1);
            istate.keyDisplay = staff.getKeyDisplay();
            setActivityState(model.size() > 0
                                     ? State.WAIT_FOR_START
                                     : State.EMPTY_SCALE);
            staff.onModelChanged();
        }
    }

    private void doPractice() {
        if (istate.noteModel.isEmpty()) return;
        setActivityState(State.PRACTICE);
        game = new PitchFollowGame(istate.noteModel);
    }

    // Some abstraction of progress.
    private interface ProgressProvider {
        int getMaxProgress();
        int getCurrentProgress();
    }

    private class PitchFollowGame extends Handler implements ProgressProvider {
        PitchFollowGame(List<StaffView.Note> model) {
            highlightAnnotator = new HighlightAnnotator(this);
            running = true;
            this.model = model;
            for (StaffView.Note n : model) {
                n.color = futureNoteColor;
            }
            modelPos = -1;
            checkNextNote();

            pitchPoster = new MicrophonePitchPoster(60);
            pitchPoster.setHandler(this);
            pitchPoster.start();
            startPracticeTime = System.currentTimeMillis();
        }

        // --- interface ProgressProvider
        public int getMaxProgress() { return kHoldTime; }
        public int getCurrentProgress() { return ticksInTune; }
        public void stop() {
            if (!running) return;
            running = false;
            pitchPoster.stopSampling();
        }

        public void handleMessage(Message msg) {
            if (!running)
                return;  // Received a sample, but we're done already.
            final MicrophonePitchPoster.PitchData data
                    = (MicrophonePitchPoster.PitchData) msg.obj;
            int beforeTicks = ticksInTune;

            boolean noteOk = data != null
                    && (data.note % 12 == model.get(modelPos).pitch % 12);
            ledview.setVisibility(noteOk ? View.VISIBLE : View.INVISIBLE);
            if (noteOk) {
                ledview.setValue(data.cent);
            }
            if (noteOk && Math.abs(data.cent) < kCentThreshold) {
                ++ticksInTune;
            } else if (data != null) {   // No data is not a penalty.
                --ticksInTune;
                if (ticksInTune < 0) ticksInTune = 0;
            }

            if (ticksInTune == 0) {
                instructions.setText("Find the note.");
            }
            else if (ticksInTune > 0 && ticksInTune < kHoldTime) {
                instructions.setText("Alright, now hold..");
            }
            else if (ticksInTune >= kHoldTime) {
                instructions.setText("Yay, continue!");
                checkNextNote();
            }
            if (beforeTicks != ticksInTune) {
                staff.onModelChanged();  // force redraw ('clock')
            }
        }

        private void checkNextNote() {
            StaffView.Note currentNote;
            if (modelPos >= 0) {
                currentNote = model.get(modelPos);
                currentNote.color = successNoteColor;
                currentNote.annotator = null;
            }

            ++modelPos;
            if (modelPos >= model.size()) {
                stop();
                showPracticeResults();
                staff.onModelChanged();  // post the last change.
                return;
            }
            currentNote = model.get(modelPos);
            currentNote.color = playNoteColor;
            currentNote.annotator = highlightAnnotator;
            ticksInTune = 0;
            staff.ensureNoteInView(modelPos);
            staff.onModelChanged();
        }

        private void showPracticeResults() {
            final long duration = System.currentTimeMillis() - startPracticeTime;
            instructions.setText(String.format("%3.1f seconds!", duration / 1000.0));
            setActivityState(State.FINISHED);
        }

        private final static int kHoldTime = 15;
        private final MicrophonePitchPoster pitchPoster;
        private final long startPracticeTime;
        private final HighlightAnnotator highlightAnnotator;
        private final List<StaffView.Note> model;
        private int modelPos;
        private int ticksInTune;
        private boolean running;
    }

    private void setGeneratorButtonsVisibility(int visibility) {
        randomTune.setVisibility(visibility);
        gmajor.setVisibility(visibility);
        cmajor.setVisibility(visibility);
        dmajor.setVisibility(visibility);
        fmajor.setVisibility(visibility);
        bbmajor.setVisibility(visibility);
    }

    private void setActivityState(State state) {
        switch (state) {
            case EMPTY_SCALE:
                startbutton.setVisibility(View.INVISIBLE);
                restartbutton.setVisibility(View.INVISIBLE);
                ledview.setVisibility(View.INVISIBLE);
                setGeneratorButtonsVisibility(View.VISIBLE);
                instructions.setText("Choose your chant of doom.");
                break;
            case WAIT_FOR_START:
                for (StaffView.Note n : istate.noteModel) {
                    n.color = Color.BLACK;
                    n.annotator = null;
                }
                staff.ensureNoteInView(0);
                staff.onModelChanged();
                instructions.setText("Refine or start.");
                startbutton.setVisibility(View.VISIBLE);
                setGeneratorButtonsVisibility(View.VISIBLE);
                break;
            case PRACTICE:
                startbutton.setVisibility(View.INVISIBLE);
                restartbutton.setVisibility(View.VISIBLE);
                ledview.setVisibility(View.VISIBLE);
                setGeneratorButtonsVisibility(View.INVISIBLE);
                break;
            case FINISHED:
                if (game != null) {
                    game.stop();
                    game = null;
                }
                startbutton.setVisibility(View.INVISIBLE);
                restartbutton.setVisibility(View.VISIBLE);
                ledview.setVisibility(View.INVISIBLE);
                setGeneratorButtonsVisibility(View.INVISIBLE);
        }
    }
    // Add a major scale to the model.
    private int addMajorScale(int startNote, boolean ascending,
                              List<StaffView.Note> model) {
        int note = startNote;
        model.add(new StaffView.Note(note, 4, Color.BLACK));
        for (int i = 0; i < kMajorScaleSequence.length; ++i) {
            if (ascending) {
                note += kMajorScaleSequence[i];
            } else {
                note -= kMajorScaleSequence[kMajorScaleSequence.length - 1 - i ];
            }
            model.add(new StaffView.Note(note, 4, Color.BLACK));
        }
        return note;
    }

    // Add a random sequence in a particular Major scale to the model.
    private void addRandomMajorSequence(int baseNote,
                                        List<StaffView.Note> model,
                                        int count) {
        int seq[] = new int[kMajorScaleSequence.length + 1];
        seq[0] = baseNote;
        for (int i = 0; i < kMajorScaleSequence.length; ++i) {
            seq[i+1] = seq[i] + kMajorScaleSequence[i];
        }
        seq[seq.length-1] = baseNote + 12;
        int previousIndex = -1;
        int randomIndex;
        for (int i = 0; i < count; ++i) {
            do {
                // Don't do the same note twice in a sequence.
                randomIndex = (int) Math.round((seq.length-1)* Math.random());
            } while (randomIndex == previousIndex);
            previousIndex = randomIndex;
            model.add(new StaffView.Note(seq[randomIndex], 4, Color.BLACK));
        }
    }

    private void addAscDescMajorScale(int startNote, List<StaffView.Note> model) {
        addMajorScale(addMajorScale(startNote, true, model), false, model);
    }

    private static final class HighlightAnnotator
            implements StaffView.Note.Annotator {
        private final Paint highlightPaint;
        private final Paint borderPaint;
        private final Paint progressPaint;
        private final ProgressProvider progressProvider;

        public HighlightAnnotator(ProgressProvider progress) {
            highlightPaint = new Paint();
            highlightPaint.setColor(Color.argb(70, 0xff, 0xff, 0));
            highlightPaint.setStrokeWidth(0);
            borderPaint = new Paint();
            borderPaint.setColor(Color.BLACK);
            borderPaint.setStrokeWidth(0);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setAntiAlias(true);
            progressPaint = new Paint();
            progressPaint.setColor(successNoteColor);
            progressProvider = progress;
        }

        public void draw(Canvas canvas, RectF staffBoundingBox,
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
            float timerRadius = lineWidth;
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
