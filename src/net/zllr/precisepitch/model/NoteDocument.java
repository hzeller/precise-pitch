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
import java.util.ArrayList;
import java.util.List;

// Model for a sequence of notes. Also the key it is in, right now it only
// remembers if it needs flat.
public class NoteDocument implements Serializable {
    public NoteDocument() {
        notes = new ArrayList<DisplayNote>();
    }

    public boolean isEmpty() { return notes.isEmpty(); }
    public int size() { return notes.size(); }
    public void clear() { notes.clear(); }
    public void pop() { notes.remove(notes.size() - 1); }
    public DisplayNote get(int i) { return notes.get(i); }
    public void add(DisplayNote n) { notes.add(n); }

    // Access to list representation of notes.
    public List<DisplayNote> getNotes() { return notes; }

    // Set if semi-tones are shown as flat or sharp.
    public boolean isFlat() { return wantsFlat; }
    public void setFlat(boolean f) { wantsFlat = f; }

    private final List<DisplayNote> notes;  // TODO: separate in measures.
    private boolean wantsFlat;
}
