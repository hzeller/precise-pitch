package net.zllr.precisepitch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import net.zllr.precisepitch.model.GameState;
import net.zllr.precisepitch.view.StaffView;

public class GamePlayActivity extends Activity {
    private StaffView staff;
    private GameState.Player player;
    private GameState gameState;
    private Button nextPlayer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_play);

        staff = (StaffView) findViewById(R.id.gameStaff);
        staff.setNotesPerStaff(16);

        Intent intent = getIntent();
        if (intent != null) {
            player = (GameState.Player) intent.getSerializableExtra("player");
            gameState = (GameState) intent.getSerializableExtra("state");
            staff.setNoteModel(gameState.getMutableNoteModel());
            staff.ensureNoteInView(0);
        }

        TextView playerText = (TextView) findViewById(R.id.playerHeadline);
        playerText.setText("Player " + player.getName());
        playerText.setBackgroundColor(player.getColor());

        nextPlayer = (Button) findViewById(R.id.nextPlayer);
        // Right now, we only have two players, so this is simple:
        int otherPlayerIndex = player.getIndex() == 0 ? 1 : 0;
        GameState.Player otherPlayer = gameState.getPlayer(otherPlayerIndex);
        nextPlayer.setBackgroundColor(otherPlayer.getColor());
        nextPlayer.setText("Next: " + otherPlayer.getName());
    }
}
