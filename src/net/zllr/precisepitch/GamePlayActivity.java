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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import net.zllr.precisepitch.model.DisplayNote;
import net.zllr.precisepitch.model.GameState;
import net.zllr.precisepitch.model.NoteDocument;
import net.zllr.precisepitch.view.StaffView;

import java.io.Serializable;

public class GamePlayActivity extends Activity {
    private static final String BUNDLE_ACTIVITY_STATE = "GamePlayActivity.state";

    private StaffView staff;
    private GameState.Player player;
    private GameState gameState;
    private Button nextInGame;
    private NoteFollowRecorder follower;
    private TextView instructions;

    private static class ActivityState implements Serializable {
        int modelPosition;
    }
    private ActivityState istate;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_play);

        staff = (StaffView) findViewById(R.id.gameStaff);
        staff.setNotesPerStaff(16);
        staff.setKeepScreenOn(true);

        Intent intent = getIntent();
        if (intent != null) {
            player = (GameState.Player) intent.getSerializableExtra("player");
            gameState = (GameState) intent.getSerializableExtra("state");
            staff.setNoteModel(gameState.getMutableNoteDocument());
        }

        TextView playerText = (TextView) findViewById(R.id.playerHeadline);
        playerText.setText("Player " + player.getName());
        playerText.setBackgroundColor(player.getColor());

        instructions = (TextView) findViewById(R.id.gameInstructions);

        if (savedInstanceState != null) {
            istate = (ActivityState) savedInstanceState.getSerializable(BUNDLE_ACTIVITY_STATE);
        }
        if (istate == null) {
            istate = new ActivityState();
        }

        nextInGame = (Button) findViewById(R.id.nextInGame);
        // Right now, we only have two players, so this is simple:
        int otherPlayerIndex = player.getIndex() == 0 ? 1 : 0;

        // TODO: when we are done with all players, then there is no next player
        // but we move on to the game result activity (race/histogram - view).
        final GameState.Player otherPlayer = gameState.getPlayer(otherPlayerIndex);
        if (gameState.getPlayerResult(otherPlayer) == null) {
            nextInGame.setBackgroundColor(otherPlayer.getColor());
            nextInGame.setText("Next: " + otherPlayer.getName());
            nextInGame.setOnClickListener(new View.OnClickListener() {
                public void onClick(View button) {
                    Intent toGame = new Intent(getBaseContext(), GamePlayActivity.class);
                    toGame.putExtra("player", otherPlayer);
                    toGame.putExtra("state", gameState);
                    startActivity(toGame);
                }
            });
        } else {
            nextInGame.setText("To Results");
            nextInGame.setOnClickListener(new View.OnClickListener() {
                public void onClick(View button) {
                    Intent toGame = new Intent(getBaseContext(), GameResultActivity.class);
                    toGame.putExtra("state", gameState);
                    startActivity(toGame);
                }
            });
        }
        nextInGame.setVisibility(View.INVISIBLE);

        follower = new NoteFollowRecorder(staff, new FollowEventListener());
    }

    // The NoteFollowRecorder returns key events while following the given
    // staff. We use that to fill in the game state for our user.
    private class FollowEventListener implements NoteFollowRecorder.EventListener {
        public void onStartModel(NoteDocument model) {
            playerResult = new GameState.PlayerResult(model.size());
            startPracticeTime = -1;
            instructions.setText("Time starts with first note.");
        }

        public void onFinishedModel(NoteDocument model) {
            playerResult.setPlayMillis(System.currentTimeMillis() - startPracticeTime);
            gameState.setPlayerResult(player, playerResult);
            nextInGame.setVisibility(View.VISIBLE);
        }

        public void onStartNote(int modelPos, DisplayNote note) {
            currentModelPos = modelPos;
            currentHistogram = new Histogram(100);
        }
        public void onSilence() {}
        public void onNoteMiss(int diff) { }
        public boolean isInTune(double cent, int ticksInTuneSoFar) {
            if (ticksInTuneSoFar == 0) {
                if (startPracticeTime > 0) {
                    instructions.setText("Find the note and hold.");
                }
            } else if (startPracticeTime < 0 && ticksInTuneSoFar > 5) {
                startPracticeTime = System.currentTimeMillis();
                instructions.setText("Time starts now.");
            }
            currentHistogram.increment((int) (cent + 50));
            return true;  // accepting everything. Histogram counts.
        }

        public void onFinishedNote() {
            playerResult.setPitchHistogram(currentModelPos, currentHistogram);
            currentHistogram = null;
        }

        int currentModelPos;
        Histogram currentHistogram;
        long startPracticeTime;
        GameState.PlayerResult playerResult;
    };

    @Override
    protected void onPause() {
        super.onPause();
        istate.modelPosition = follower.getPosition();
        follower.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        follower.resume(istate.modelPosition);
    }

    @Override
    protected void onSaveInstanceState(Bundle saveState) {
        saveState.putSerializable(BUNDLE_ACTIVITY_STATE, istate);
    }
}
