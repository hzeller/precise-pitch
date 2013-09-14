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
 *
 * Rough sequence:
 *   - choose 1 or 2 players
 *   - display practice activity
 *         o choose chant of doom. Right now: pre-select
 *           Game data: Staff model with DisplayNotes, #players game result data.
 *         o have two colored buttons. Let first player choose.
 *         o Game goes on with practice activity, accepting +/- 45 cent, displays
 *           time. Records results for player #1
 *         o When done, the other button shows up. Pressing that goes to the
 *           practice activity, records results for player #2
 *         o When all results are collected, gets to the result display page
 *             - display raw scores at first.
 *             - next: toggle between car-race and histogram annotated notes.
 */
package net.zllr.precisepitch;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import net.zllr.precisepitch.model.DisplayNote;
import net.zllr.precisepitch.model.GameState;
import net.zllr.precisepitch.view.StaffView;

import java.util.List;

// Set up the game: choose notes to play and number of players.
// (right now, a lot of stuff is copied from PracticeActivity; consolidate)
public class GameSetupActivity extends Activity {
    private static final String BUNDLE_GAME_STATE = "GameSetupActivity.gameState";
    private static final int kMajorScaleSequence[] = { 2, 2, 1, 2, 2, 2, 1 };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_setup);

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

        instructions = (TextView) findViewById(R.id.gameSetupInstructions);
        player1 = (Button) findViewById(R.id.startPlayer1);
        player2 = (Button) findViewById(R.id.startPlayer2);

        staff = (StaffView) findViewById(R.id.gameSetupStaff);
        staff.setNotesPerStaff(16);

        if (savedInstanceState != null) {
            gameState = (GameState) savedInstanceState.getSerializable(BUNDLE_GAME_STATE);
        }
        if (gameState == null) {
            gameState = new GameState();
        }
        gameState.setNumPlayers(2);
        staff.setNoteModel(gameState.getMutableNoteModel());
        staff.ensureNoteInView(0);
        setEnableGameButtons(gameState.getMutableNoteModel().size() > 0);
    }

    @Override
    protected void onSaveInstanceState(Bundle saveState) {
        saveState.putSerializable(BUNDLE_GAME_STATE, gameState);
    }

    // Kinda hardcoded now :)
    private final class NoteGenerationListener implements View.OnClickListener {
        public void onClick(View button) {
            boolean wantsFlat = false;
            List<DisplayNote> model = gameState.getMutableNoteModel();
            int startNote = model.size() > 0 ? model.get(0).note : 0;
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
            setEnableGameButtons(model.size() > 0);
            staff.onModelChanged();
        }
    }

    // Add a major scale to the model. Returns last note.
    private int addMajorScale(int startNote, boolean ascending,
                              List<DisplayNote> model) {
        int note = startNote;
        model.add(new DisplayNote(note, 4, Color.BLACK));
        for (int i = 0; i < kMajorScaleSequence.length; ++i) {
            if (ascending) {
                note += kMajorScaleSequence[i];
            } else {
                note -= kMajorScaleSequence[kMajorScaleSequence.length - 1 - i ];
            }
            model.add(new DisplayNote(note, 4, Color.BLACK));
        }
        return note;
    }

    // Add a random sequence in a particular Major scale to the model.
    private void addRandomMajorSequence(int baseNote,
                                        List<DisplayNote> model,
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
            model.add(new DisplayNote(seq[randomIndex], 4, Color.BLACK));
        }
    }

    private void addAscDescMajorScale(int startNote, List<DisplayNote> model) {
        addMajorScale(addMajorScale(startNote, true, model), false, model);
    }

    private void setEnableGameButtons(boolean on) {
        player1.setEnabled(on);
        player1.setVisibility(on ? View.VISIBLE : View.INVISIBLE);
        player2.setEnabled(on);
        player2.setVisibility(on ? View.VISIBLE : View.INVISIBLE);
        if (on) {
            instructions.setText("Start or keep choosing.");
        } else {
            instructions.setText("Choose tune.");
        }
    }

    private StaffView staff;
    private CheckBox randomTune;
    private Button cmajor;
    private Button gmajor;
    private Button dmajor;
    private Button fmajor;
    private Button bbmajor;

    private Button player1;  // This should be an array of arbitrary number players
    private Button player2;

    private TextView instructions;
    private GameState gameState;
}
