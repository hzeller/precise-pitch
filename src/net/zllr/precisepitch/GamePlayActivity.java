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
import java.util.List;

public class GamePlayActivity extends Activity {
    private static final String BUNDLE_ACTIVITY_STATE = "GamePlayActivity.state";

    private StaffView staff;
    private GameState.Player player;
    private GameState gameState;
    private Button nextPlayer;
    private NoteFollowRecorder follower;

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
            staff.setNoteModel(gameState.getMutableNoteModel());
        }

        TextView playerText = (TextView) findViewById(R.id.playerHeadline);
        playerText.setText("Player " + player.getName());
        playerText.setBackgroundColor(player.getColor());

        if (savedInstanceState != null) {
            istate = (ActivityState) savedInstanceState.getSerializable(BUNDLE_ACTIVITY_STATE);
        }
        if (istate == null) {
            istate = new ActivityState();
        }

        nextPlayer = (Button) findViewById(R.id.nextPlayer);
        // Right now, we only have two players, so this is simple:
        int otherPlayerIndex = player.getIndex() == 0 ? 1 : 0;

        // TODO: when we are done with all players, then there is no next player
        // but we move on to the game result activity (race/histogram - view).
        GameState.Player otherPlayer = gameState.getPlayer(otherPlayerIndex);
        nextPlayer.setBackgroundColor(otherPlayer.getColor());
        nextPlayer.setText("Next: " + otherPlayer.getName());
        nextPlayer.setVisibility(View.INVISIBLE);
        // TODO: register listener that starts GamePlayActivity with 'otherPlayer'.
        follower = new NoteFollowRecorder(staff, new FollowEventListener());
    }

    private class FollowEventListener implements NoteFollowRecorder.EventListener {
        public void onStartModel(NoteDocument model) { }
        public void onFinishedModel() {
            nextPlayer.setVisibility(View.VISIBLE);
        }

        public void onStartNote(int modelPos, DisplayNote note) {
            // TODO: create new histogram.
        }
        public void onSilence() {}
        public void onNoteMiss(int diff) { }
        public boolean isInTune(double cent, int ticksInTuneSoFar) {
            // TODO: update histogram.
            return true;
        }
        public void onFinishedNote() {
            // TODO: store histogram for this note in PlayerResult
        }
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
