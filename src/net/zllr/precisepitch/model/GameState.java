package net.zllr.precisepitch.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameState implements Serializable {
    public GameState(int numPlayers) {
        notesToPlay = new ArrayList<DisplayNote>();
        playerResult = new ArrayList<PlayerResult>(numPlayers);
    }

    public static final class PlayerResult implements Serializable {
        public int playTimeMilliseconds;
        // other states, such as Histograms of notes. Could just be an array
        // of the same size as notesToPlay.
    }

    // The model of notes to be played. This is initialized once with the
    // notes and is to be played by each player.
    // The DisplayNotes contain annotators, that might be replaced for different
    // display situations.
    List<DisplayNote> getMutableNoteModel() { return notesToPlay; }

    int getNumPlayers() { return playerResult.size(); }

    // Set the collected result for a particular player.
    void setPlayerResult(int player, PlayerResult result) {
        playerResult.set(player, result);
    }
    PlayerResult getPlayerResult(int player) { return playerResult.get(player); }

    private final List<DisplayNote> notesToPlay;
    private final List<PlayerResult> playerResult;
}
