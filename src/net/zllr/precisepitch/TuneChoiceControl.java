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
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import net.zllr.precisepitch.model.DisplayNote;
import net.zllr.precisepitch.model.NoteDocument;

import java.util.List;

public class TuneChoiceControl extends LinearLayout {
    private static final int kMajorScaleSequence[] = { 2, 2, 1, 2, 2, 2, 1 };

    private NoteDocument model;
    private OnChangeListener changeListener;
    private CheckBox randomTune;
    private Button cmajor;
    private Button gmajor;
    private Button dmajor;
    private Button fmajor;
    private Button bbmajor;

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
        final NoteGenerationListener noteCreator = new NoteGenerationListener();
        randomTune = (CheckBox) findViewById(R.id.tcRandomSequence);
        cmajor = (Button) findViewById(R.id.tcNewCMajor);
        cmajor.setOnClickListener(noteCreator);
        gmajor = (Button) findViewById(R.id.tcNewGMajor);
        gmajor.setOnClickListener(noteCreator);
        dmajor = (Button) findViewById(R.id.tcNewDMajor);
        dmajor.setOnClickListener(noteCreator);
        fmajor = (Button) findViewById(R.id.tcNewFMajor);
        fmajor.setOnClickListener(noteCreator);
        bbmajor = (Button) findViewById(R.id.tcNewBbMajor);
        bbmajor.setOnClickListener(noteCreator);
    }

    // Kinda hardcoded now :)
    private final class NoteGenerationListener implements View.OnClickListener {
        public void onClick(View button) {
            if (model == null)
                return;
            boolean wantsFlat = false;
            int startNote = model.size() > 0 ? model.get(0).note : 0;
            model.clear();

            // Use lowest note unless that is already set: then choose one
            // octave higher. That way, we can 'toggle' between two octaves.
            if (button == cmajor) {
                startNote = (startNote == 3) ? 15 : 3;
            } else if (button == gmajor) {
                startNote = (startNote == 10) ? 22 : 10;
            } else if (button == dmajor) {
                startNote = (startNote == 5) ? 17 : 5;
            } else if (button == fmajor) {
                startNote = (startNote == 8) ? 20 : 8;
                wantsFlat = true;
            } else if (button == bbmajor) {
                startNote = (startNote == 13) ? 25 : 13;
                wantsFlat = true;
            }

            if (randomTune.isChecked()) {
                addRandomMajorSequence(startNote, model, 16);
            } else {
                addAscDescMajorScale(startNote, model);
            }
            model.setFlat(wantsFlat);
            if (changeListener != null) {
                changeListener.onChange();
            }
        }
    }

    // Add a major scale to the model. Returns last note.
    private int addMajorScale(int startNote, boolean ascending,
                              NoteDocument model) {
        int note = startNote;
        model.add(new DisplayNote(note, 4, Color.BLACK));
        for (int i = 0; i < kMajorScaleSequence.length; ++i) {
            if (ascending) {
                note += kMajorScaleSequence[i];
            } else {
                note -= kMajorScaleSequence[kMajorScaleSequence.length - 1 - i ];
            }
            model.add(new DisplayNote(note, 4, Color.BLACK));
        }
        return note;
    }

    // Add a random sequence in a particular Major scale to the model.
    private void addRandomMajorSequence(int baseNote,
                                        NoteDocument model,
                                        int count) {
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
                // Don't do the same note twice in a sequence.
                randomIndex = (int) Math.round((seq.length-1)* Math.random());
            } while (randomIndex == previousIndex);
            previousIndex = randomIndex;
            model.add(new DisplayNote(seq[randomIndex], 4, Color.BLACK));
        }
    }

    private void addAscDescMajorScale(int startNote, NoteDocument model) {
        addMajorScale(addMajorScale(startNote, true, model), false, model);
    }
}
