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
package net.zllr.precisepitch.model;

import java.io.Serializable;

public class GameState implements Serializable {
    public GameState() {
        notesToPlay = new NoteDocument();
    }

    public static final class Player implements Serializable {
        public Player(int color, String name) {
            this.color = color;
            this.name = name;
        }

        public int getColor() { return color; }
        public String getName() { return name; }
        public int getIndex() { return index; }

        private final int color;
        private final String name;

        // We use the index as mapping property rather than relying on hashCode
        // or similar (more compact memory representation and less confusion
        // if objects get serialized in their lifetime).
        private int index;

        // more things: icon ?
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
    public NoteDocument getMutableNoteModel() { return notesToPlay; }

    public void setNumPlayers(int numPlayers) {
        if (playerResults != null)
            throw new IllegalStateException("Setting players after game started.");
        players = new Player[numPlayers];
    }
    public int getNumPlayers() { return players.length; }

    public void setPlayer(int p, Player player) {
        player.index = p;
        players[p] = player;
    }
    public Player getPlayer(int p) { return players[p]; }

    // Set the collected result for a particular player.
    public void setPlayerResult(Player player, PlayerResult result) {
        if (playerResults == null)
            playerResults = new PlayerResult[getNumPlayers()];
        playerResults[player.index] = result;
    }
    public PlayerResult getPlayerResult(Player player) {
        return playerResults[player.index];
    }

    private final NoteDocument notesToPlay;
    private PlayerResult playerResults[];
    private Player players[];
}
