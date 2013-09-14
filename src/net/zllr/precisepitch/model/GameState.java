package net.zllr.precisepitch.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameState implements Serializable {
    public GameState() {
        notesToPlay = new ArrayList<DisplayNote>();
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
    public List<DisplayNote> getMutableNoteModel() { return notesToPlay; }

    public void setNumPlayers(int players) {
        if (playerResults != null)
            throw new IllegalStateException("Setting players after game started.");
        numPlayers = players;
    }
    public int getNumPlayers() { return numPlayers; }

    // Set the collected result for a particular player.
    public void setPlayerResult(int player, PlayerResult result) {
        if (playerResults == null)
            playerResults = new ArrayList<PlayerResult>(numPlayers);
        playerResults.set(player, result);
    }
    public PlayerResult getPlayerResult(int player) {
        return playerResults.get(player);
    }

    private final List<DisplayNote> notesToPlay;
    private int numPlayers;
    private List<PlayerResult> playerResults;
}
