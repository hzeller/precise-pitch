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

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import net.zllr.precisepitch.model.DisplayNote;
import net.zllr.precisepitch.model.Note;
import net.zllr.precisepitch.model.NoteDocument;

import java.util.List;

public class TuneChoiceControl extends LinearLayout {
    private static final int kMajorScaleSequence[] = { 2, 2, 1, 2, 2, 2, 1 };

    private NoteDocument model;
    private OnChangeListener changeListener;
    private CheckBox randomTune;

    public interface OnChangeListener {
        void onChange();
    }

    public TuneChoiceControl(Context context) {
        super(context);
    }

    public TuneChoiceControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.tune_choice_component, this);
    }

    public void setOnChangeListener(OnChangeListener listener) {
        changeListener = listener;
    }
    public void setNoteModel(NoteDocument newModel) {
        if (this.model == null)
            InitializeListeners();
        this.model = newModel;
    }

    private void InitializeListeners() {
        final FixedNoteSequenceListener seqCreator = new FixedNoteSequenceListener();
        randomTune = (CheckBox) findViewById(R.id.tcRandomSequence);
        Button cmajor = (Button) findViewById(R.id.tcNewCMajor);
        cmajor.setOnClickListener(new NoteGenerationListener(Note.C, false));
        Button gmajor = (Button) findViewById(R.id.tcNewGMajor);
        gmajor.setOnClickListener(new NoteGenerationListener(Note.G, false));
        Button dmajor = (Button) findViewById(R.id.tcNewDMajor);
        dmajor.setOnClickListener(new NoteGenerationListener(Note.D, false));
        Button amajor = (Button) findViewById(R.id.tcNewAMajor);
        amajor.setOnClickListener(new NoteGenerationListener(Note.A, false));
        Button emajor = (Button) findViewById(R.id.tcNewEMajor);
        emajor.setOnClickListener(new NoteGenerationListener(Note.E, false));
        Button abmajor = (Button) findViewById(R.id.tcNewAbMajor);
        abmajor.setOnClickListener(new NoteGenerationListener(Note.A_b, true));
        Button ebmajor = (Button) findViewById(R.id.tcNewEbMajor);
        ebmajor.setOnClickListener(new NoteGenerationListener(Note.E_b, true));
        Button fmajor = (Button) findViewById(R.id.tcNewFMajor);
        fmajor.setOnClickListener(new NoteGenerationListener(Note.F, true));
        Button bbmajor = (Button) findViewById(R.id.tcNewBbMajor);
        bbmajor.setOnClickListener(new NoteGenerationListener(Note.B_b, true));
        
        Button seq = (Button) findViewById(R.id.tcNewSeq);
        seq.setOnClickListener(seqCreator);
    }

    private final class FixedNoteSequenceListener implements View.OnClickListener {
        int sequenceNumber = 0;
        public void onClick(View button) {
            if (model == null) {
                return;
            }
            final int[][] sequences = {
                    {19, 23, 21, 23, 17, 16, 14, 16, 19, 23, 21, 19, 16, 12, 14, 16},
                    {19, 23, 21, 23, 19, 16, 19, 16, 14, 16, 12, 16, 10, 16, 14, 16},
                    { 5,  9, 12,  9,  7,  9,  5,  9,  7, 10, 16, 12,  9,  5,  9,  5}};
            model.clear();
            model.setFlat(false);
            for (int note : sequences[sequenceNumber]) {
                model.add(new DisplayNote(note, 4));
            }
            sequenceNumber = (sequenceNumber + 1) % sequences.length;
            if (changeListener != null) {
                changeListener.onChange();
            }
        }
    }
    
    private static enum State {
        BASE_OCTAVE,
        HIGH_OCTAVE,
        TWO_OCTAVE,
    };

    private final class NoteGenerationListener implements View.OnClickListener {
        NoteGenerationListener(int baseNote, boolean wantsFlat) {
            this.baseNote = baseNote;
            this.wantsFlat = wantsFlat;
        }

        final boolean wantsFlat;
        final int baseNote;

        State state = State.BASE_OCTAVE;

        public void onClick(View button) {
            if (model == null)
                return;
            model.setFlat(wantsFlat);
            model.clear();
            if (randomTune.isChecked()) {
                if (state == State.BASE_OCTAVE) {
                    addRandomMajorSequence(baseNote, model, 16);
                    state = State.HIGH_OCTAVE;
                } else {
                    addRandomMajorSequence(baseNote + 12, model, 16);
                    state = State.BASE_OCTAVE;
                }
            } else {
                switch (state) {
                    case BASE_OCTAVE:
                        addAscDescMajorScale(baseNote, 1, model);
                        state = State.HIGH_OCTAVE;
                        break;
                    case HIGH_OCTAVE:
                        addAscDescMajorScale(baseNote + 12, 1, model);
                        state = State.TWO_OCTAVE;
                        break;
                    case TWO_OCTAVE:
                        addAscDescMajorScale(baseNote, 2, model);
                        state = State.BASE_OCTAVE;
                        break;
                }
            }
            if (changeListener != null) {
                changeListener.onChange();
            }
        }
    }

    // Add a major scale to the model except the last note. Returns last note pitch.
    private int addMajorScale(int startNote, boolean ascending, NoteDocument model) {
        int note = startNote;
        model.add(new DisplayNote(note, 4));
        for (int i = 0; i < kMajorScaleSequence.length; ++i) {
            if (ascending) {
                note += kMajorScaleSequence[i];
            } else {
                note -= kMajorScaleSequence[kMajorScaleSequence.length - 1 - i ];
            }
            if (i == kMajorScaleSequence.length - 1)
                break;
            model.add(new DisplayNote(note, 4));
        }
        return note;
    }

    // Add a random sequence in a particular Major scale to the model.
    private void addRandomMajorSequence(int baseNote,
                                        NoteDocument model,
                                        int count) {
       while (baseNote > Note.a)
            baseNote -= 12;
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
                // Don't do the same note twice in a row.
                randomIndex = (int) Math.round((seq.length-1)* Math.random());
            } while (randomIndex == previousIndex);
            previousIndex = randomIndex;
            model.add(new DisplayNote(seq[randomIndex], 4));
        }
    }

    private void addAscDescMajorScale(int startNote, int octaves, NoteDocument model) {
        for (int octave = 0; octave < octaves; ++octave) {
            startNote = addMajorScale(startNote, true, model);
        }
        model.add(new DisplayNote(startNote, 4));
        for (int octave = 0; octave < octaves; ++octave) {
            startNote = addMajorScale(startNote, false, model);
        }
        model.add(new DisplayNote(startNote, 4));
    }
}
