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

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import net.zllr.precisepitch.model.DisplayNote;
import net.zllr.precisepitch.model.NoteDocument;
import net.zllr.precisepitch.view.CenterOffsetView;
import net.zllr.precisepitch.view.StaffView;

import java.io.Serializable;

public class PracticeActivity extends Activity {
    private static final String BUNDLE_KEY_MODEL = "PracticeActivity.model";
    private static final int kCentThreshold = 20;

    // All the activity state that we need to keep track of between teardown
    // restart.
    private static class ActivityState implements Serializable {
        public ActivityState() {
            noteModel = new NoteDocument();
        }
        final NoteDocument noteModel;
        int followPos = -1;
    };

    private ActivityState istate;
    private TuneChoiceControl tuneChoice;
    private StaffView staff;
    private CenterOffsetView ledview;
    private Button startbutton;
    private TextView instructions;
    private Button restartbutton;
    private NoteFollowRecorder noteFollower;

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

        ledview = (CenterOffsetView) findViewById(R.id.practiceLedDisplay);
        ledview.setQuantization(2.5f);
        ledview.setRange(Math.min(50, kCentThreshold + 10));
        ledview.setKeepScreenOn(true);
        ledview.setMarkAt(kCentThreshold);
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

        tuneChoice = (TuneChoiceControl) findViewById(R.id.tuneChoice);
        tuneChoice.setNoteModel(staff.getNoteModel());
        tuneChoice.setOnChangeListener(new TuneChoiceControl.OnChangeListener() {
            @Override
            public void onChange() {
                staff.ensureNoteInView(0);
                setActivityState(staff.getNoteModel().isEmpty()
                                         ? State.EMPTY_SCALE
                                         : State.WAIT_FOR_START);
                staff.onModelChanged();
            }
        });

        setActivityState(State.EMPTY_SCALE);  // need to walk this state first.
        if (!istate.noteModel.isEmpty()) {
            setActivityState(State.WAIT_FOR_START);
        }
    }

    // Callbacks from the NoteFollowRecorder. We use this to record statistics.
    private class FollowEventListener implements NoteFollowRecorder.EventListener {
        public void onStartModel(NoteDocument model) {
            histogramAnnotators = new HistogramAnnotator[model.size()];
            for (int i = 0; i < histogramAnnotators.length; i++) {
                histogramAnnotators[i] = new HistogramAnnotator();
            }
            startPracticeTime = -1;
        }
        public void onFinishedModel() {
            final long duration = System.currentTimeMillis() - startPracticeTime;
            instructions.setText(String.format("%3.1f seconds; When in range, average %.1f cent off.",
                                               duration / 1000.0,
                                               sumAbsoluteOffset / absoluteOffsetCount));
            setActivityState(State.FINISHED);
        }

        public void onStartNote(int modelPos, DisplayNote note) {
            currentModelPos = modelPos;
            currentNote = note;
        }
        public void onSilence() {
            ledview.setDataValid(false);
        }
        public void onNoteMiss(int diff) {
            ledview.setDataValid(true);
            ledview.setValue(diff * 100);  // displays too low/high arrows
        }
        public boolean isInTune(double cent, int ticksInTuneSoFar) {
            ledview.setDataValid(true);
            ledview.setValue(cent);
            // The following stat can probably go as we can determine a better
            // score out of the histogram data.
            sumAbsoluteOffset += Math.abs(cent);
            absoluteOffsetCount++;

            histogramAnnotators[currentModelPos].hist1.increment((int)(cent + 50.0));

            // Give some instructions depending on ticks in tune.
            if (ticksInTuneSoFar == 0) {
                if (startPracticeTime < 0) {
                    instructions.setText("Time starts with first note.");
                } else {
                    instructions.setText("Find the note and hold.");
                }
            } else if (startPracticeTime < 0 && ticksInTuneSoFar > 5) {
                startPracticeTime = System.currentTimeMillis();
                instructions.setText("Time starts now.");
            }

            return true; // accept everything. Accuracy recorded in histogram.
        }
        public void onFinishedNote() {
            histogramAnnotators[currentModelPos].hist1.filter(20);
            currentNote.annotator = histogramAnnotators[currentModelPos];
        }

        private long startPracticeTime;
        private float sumAbsoluteOffset;
        private long absoluteOffsetCount;
        private int currentModelPos;
        private DisplayNote currentNote;
        private HistogramAnnotator histogramAnnotators[];
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (noteFollower != null)
            istate.followPos = noteFollower.getPosition();
        if (noteFollower != null) {
            // TODO: NoteFollower actually can handle being paused/resumed
            // now. Take this into account.
            noteFollower.pause();
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
        if (noteFollower != null)
            noteFollower.resume(istate.followPos);
    }

    @Override
    protected void onSaveInstanceState(Bundle saveState) {
        saveState.putSerializable(BUNDLE_KEY_MODEL, istate);
    }

    private void doPractice() {
        if (istate.noteModel.isEmpty()) return;
        setActivityState(State.PRACTICE);
        noteFollower = new NoteFollowRecorder(staff, new FollowEventListener());
    }

    private void setActivityState(State state) {
        switch (state) {
            case EMPTY_SCALE:
                startbutton.setVisibility(View.INVISIBLE);
                restartbutton.setVisibility(View.INVISIBLE);
                ledview.setVisibility(View.INVISIBLE);
                tuneChoice.setVisibility(View.VISIBLE);
                instructions.setText("Choose your chant of doom.");
                break;
            case WAIT_FOR_START:
                for (DisplayNote n : istate.noteModel.getNotes()) {
                    n.color = Color.BLACK;
                    n.annotator = null;
                }
                staff.ensureNoteInView(0);
                staff.onModelChanged();
                instructions.setText("Refine or start.");
                startbutton.setVisibility(View.VISIBLE);
                tuneChoice.setVisibility(View.VISIBLE);
                break;
            case PRACTICE:
                startbutton.setVisibility(View.INVISIBLE);
                restartbutton.setVisibility(View.VISIBLE);
                ledview.setVisibility(View.VISIBLE);
                ledview.setDataValid(false);
                tuneChoice.setVisibility(View.INVISIBLE);
                break;
            case FINISHED:
                if (noteFollower != null) {
                    noteFollower.pause();
                    noteFollower = null;
                }
                startbutton.setVisibility(View.INVISIBLE);
                restartbutton.setVisibility(View.VISIBLE);
                ledview.setVisibility(View.INVISIBLE);
                ledview.setDataValid(false);
                tuneChoice.setVisibility(View.INVISIBLE);
        }
    }
}
