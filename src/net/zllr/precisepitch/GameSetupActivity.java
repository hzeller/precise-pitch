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
import android.content.Intent;
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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_setup);

        instructions = (TextView) findViewById(R.id.gameSetupInstructions);
        staff = (StaffView) findViewById(R.id.gameSetupStaff);
        staff.setNotesPerStaff(16);

        if (savedInstanceState != null) {
            gameState = (GameState) savedInstanceState.getSerializable(BUNDLE_GAME_STATE);
        }
        if (gameState == null) {
            gameState = new GameState();
        }

        // Right now, we have two fixed players with default names :)
        gameState.setNumPlayers(2);
        gameState.setPlayer(0, new GameState.Player(Color.parseColor("#5555FF"), "Blue"));
        gameState.setPlayer(1, new GameState.Player(Color.parseColor("#FF9955"), "Orange"));

        // TODO: fill the linear layout from the player array with the right
        // number of buttons (right now, we're kinda fixed to two).
        final View.OnClickListener gameStarter = new SwitchToGameListener();
        player1 = (Button) findViewById(R.id.startPlayer1);
        player1.setBackgroundColor(gameState.getPlayer(0).getColor());
        player1.setText("Start " + gameState.getPlayer(0).getName());
        player1.setOnClickListener(gameStarter);

        player2 = (Button) findViewById(R.id.startPlayer2);
        player2.setBackgroundColor(gameState.getPlayer(1).getColor());
        player2.setText("Start " + gameState.getPlayer(1).getName());
        player2.setOnClickListener(gameStarter);

        staff.setNoteModel(gameState.getMutableNoteModel());
        staff.ensureNoteInView(0);
        setEnableGameButtons(!gameState.getMutableNoteModel().isEmpty());

        TuneChoiceControl tuneChoice = (TuneChoiceControl) findViewById(R.id.tuneChoice);
        tuneChoice.setOnChangeListener(new NoteModelChangeListener());
        tuneChoice.setNoteModel(staff.getNoteModel());
    }

    private class NoteModelChangeListener implements TuneChoiceControl.OnChangeListener {
        public void onChange() {
            staff.ensureNoteInView(0);
            setEnableGameButtons(!staff.getNoteModel().isEmpty());
            staff.onModelChanged();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle saveState) {
        saveState.putSerializable(BUNDLE_GAME_STATE, gameState);
    }

    private final class SwitchToGameListener implements View.OnClickListener {
        public void onClick(View button) {
            Intent toGame = new Intent(getBaseContext(), GamePlayActivity.class);
            int playerIndex = 0;
            if (button == player1) playerIndex = 0;
            if (button == player2) playerIndex = 1;
            toGame.putExtra("player", gameState.getPlayer(playerIndex));
            toGame.putExtra("state", gameState);
            startActivity(toGame);
        }

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
    private Button player1;  // This should be an array of arbitrary number players
    private Button player2;

    private TextView instructions;
    private GameState gameState;
}
