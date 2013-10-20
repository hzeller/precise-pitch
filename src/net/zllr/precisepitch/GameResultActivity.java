package net.zllr.precisepitch;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import net.zllr.precisepitch.model.DisplayNote;
import net.zllr.precisepitch.model.GameState;
import net.zllr.precisepitch.model.NoteDocument;
import net.zllr.precisepitch.view.StaffView;


public class GameResultActivity extends Activity {
    private StaffView staff;
    private GameState gameState;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_result);

        staff = (StaffView) findViewById(R.id.gameStaff);
        staff.setNotesPerStaff(16);
        staff.setKeepScreenOn(true);

        Intent intent = getIntent();
        if (intent == null)
            return;  // mmh, shouldn't happen.

        gameState = (GameState) intent.getSerializableExtra("state");
        staff.setNoteModel(gameState.getMutableNoteDocument());

        // Right now, we only have two players.
        GameState.PlayerResult results[] = {
                gameState.getPlayerResult(gameState.getPlayer(0)),
                gameState.getPlayerResult(gameState.getPlayer(1)) };
        final NoteDocument doc = gameState.getMutableNoteDocument();
        for (int i = 0; i < doc.size(); ++i) {
            DisplayNote note = doc.get(i);
            note.color = Color.BLACK;
            note.annotator = new HistogramAnnotator(results[0].getPitchHistogram(i),
                                                    results[1].getPitchHistogram(i));
        }
    }
}