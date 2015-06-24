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
import net.zllr.precisepitch.model.Note;
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
    private Button amajor;
    private Button emajor;
    private Button abmajor;
    private Button ebmajor;
    private Button fmajor;
    private Button bbmajor;
    
    
    private Button seq;

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
        final FixedNoteSequenceListener seqCreator = new FixedNoteSequenceListener();
        randomTune = (CheckBox) findViewById(R.id.tcRandomSequence);
        cmajor = (Button) findViewById(R.id.tcNewCMajor);
        cmajor.setOnClickListener(noteCreator);
        gmajor = (Button) findViewById(R.id.tcNewGMajor);
        gmajor.setOnClickListener(noteCreator);
        dmajor = (Button) findViewById(R.id.tcNewDMajor);
        dmajor.setOnClickListener(noteCreator);
        amajor = (Button) findViewById(R.id.tcNewAMajor);
        amajor.setOnClickListener(noteCreator);
        emajor = (Button) findViewById(R.id.tcNewEMajor);
        emajor.setOnClickListener(noteCreator);
        abmajor = (Button) findViewById(R.id.tcNewAbMajor);
        abmajor.setOnClickListener(noteCreator);
        ebmajor = (Button) findViewById(R.id.tcNewEbMajor);
        ebmajor.setOnClickListener(noteCreator);
        fmajor = (Button) findViewById(R.id.tcNewFMajor);
        fmajor.setOnClickListener(noteCreator);
        bbmajor = (Button) findViewById(R.id.tcNewBbMajor);
        bbmajor.setOnClickListener(noteCreator);
        
        seq = (Button) findViewById(R.id.tcNewSeq);
        seq.setOnClickListener(seqCreator);
    }

    private final class FixedNoteSequenceListener implements View.OnClickListener {
        public void onClick(View button) {
            if (model == null) {
                return;
            }
            
            int fifthNote = model.size() >= 5 ? model.get(4).note : 0;
            model.clear();
            // Not sure what this is all about. Is there a particular reason for this sequence?
            // Some peculiar way to select various sequences depending on prior state
            if (button == seq) {
                if (fifthNote == 0 || fifthNote == 7) {
                    model.add(new DisplayNote(19, 4));
                    model.add(new DisplayNote(23, 4));
                    model.add(new DisplayNote(21, 4));
                    model.add(new DisplayNote(23, 4));
                    
                    model.add(new DisplayNote(17, 4));  // 5th, so next #2
                    model.add(new DisplayNote(16, 4));
                    model.add(new DisplayNote(14, 4));
                    model.add(new DisplayNote(16, 4));
                    
                    model.add(new DisplayNote(19, 4));
                    model.add(new DisplayNote(23, 4));
                    model.add(new DisplayNote(21, 4));
                    model.add(new DisplayNote(19, 4));
                    
                    model.add(new DisplayNote(16, 4));
                    model.add(new DisplayNote(12, 4));
                    model.add(new DisplayNote(14, 4));
                    model.add(new DisplayNote(16, 4));
                } else if (fifthNote == 17) {
                    model.add(new DisplayNote(19, 4));
                    model.add(new DisplayNote(23, 4));
                    model.add(new DisplayNote(21, 4));
                    model.add(new DisplayNote(23, 4));
                    
                    model.add(new DisplayNote(19, 4));  // 5th. So next #3
                    model.add(new DisplayNote(16, 4));
                    model.add(new DisplayNote(19, 4));
                    model.add(new DisplayNote(16, 4));
                    
                    model.add(new DisplayNote(14, 4));
                    model.add(new DisplayNote(16, 4));
                    model.add(new DisplayNote(12, 4));
                    model.add(new DisplayNote(16, 4));
                    
                    model.add(new DisplayNote(10, 4));
                    model.add(new DisplayNote(16, 4));
                    model.add(new DisplayNote(14, 4));
                    model.add(new DisplayNote(16, 4));
                } else if (fifthNote == 19) {
                    model.add(new DisplayNote(5, 4));
                    model.add(new DisplayNote(9, 4));
                    model.add(new DisplayNote(12, 4));
                    model.add(new DisplayNote(9, 4));
                    
                    model.add(new DisplayNote(7, 4));  // 5th. So Next #1
                    model.add(new DisplayNote(9, 4));
                    model.add(new DisplayNote(5, 4));
                    model.add(new DisplayNote(9, 4));
                    
                    model.add(new DisplayNote(7, 4));
                    model.add(new DisplayNote(10, 4));
                    model.add(new DisplayNote(16, 4));
                    model.add(new DisplayNote(12, 4));
                    
                    model.add(new DisplayNote(9, 4));
                    model.add(new DisplayNote(5, 4));
                    model.add(new DisplayNote(9, 4));
                    model.add(new DisplayNote(5, 4));
                }
            }
            
            model.setFlat(false);
            if (changeListener != null) {
                changeListener.onChange();
            }
        }
    }
    
    
    // Kinda hardcoded now :)
    private final class NoteGenerationListener implements View.OnClickListener {
        public void onClick(View button) {
            if (model == null)
                return;
            boolean wantsFlat = false;
            int startNote = model.size() > 0 ? model.get(0).note : 0;
            int eighthNote = model.size() >= 8 ? model.get(7).note : -1;
            int ninthNote = model.size() >= 9 ? model.get(8).note : -2;
            model.clear();

            // Use lowest note unless that is already set: then choose one
            // octave higher. That way, we can 'toggle' between two octaves.
            if (button == cmajor) {
                if (startNote == Note.C) {
                    startNote = (ninthNote == eighthNote) ? Note.C : Note.c;
                } else if (startNote == Note.c) {
                    startNote = Note.c$;
                } else {
                    startNote = Note.C;
                }
            } else if (button == gmajor) {
                if (startNote == Note.G) {
                    startNote = (ninthNote == eighthNote) ? Note.G : Note.g;
                } else if (startNote == Note.g) {
                    startNote = Note.g$;
                } else {
                    startNote = Note.G;
                }
            } else if (button == dmajor) {
                if (startNote == Note.D) {
                    startNote = (ninthNote == eighthNote) ? Note.D : Note.d;
                } else if (startNote == Note.d) {
                    startNote = Note.d$;
                } else {
                    startNote = Note.D;
                }
            } else if (button == amajor) {
                if (startNote == Note.A) {
                    startNote = (ninthNote == eighthNote) ? Note.A : Note.a;
                } else if (startNote == Note.a) {
                    startNote = Note.a$;
                } else {
                    startNote = Note.A;
                }
            } else if (button == emajor) {
                if (startNote == Note.E) {
                    startNote = (ninthNote == eighthNote) ? Note.E : Note.e;
                } else if (startNote == Note.e) {
                    startNote = Note.e$;
                } else {
                    startNote = Note.E;
                }
            } else if (button == abmajor) {
                if (startNote == Note.A_b) {
                    startNote = (ninthNote == eighthNote) ? Note.A_b : Note.a_b;
                } else if (startNote == Note.a_b) {
                    startNote = Note.a$_b;
                } else {
                    startNote = Note.A_b;
                }
                wantsFlat = true;
            } else if (button == ebmajor) {
                if (startNote == Note.E_b) {
                    startNote = (ninthNote == eighthNote) ? Note.E_b : Note.e_b;
                } else if (startNote == Note.e_b) {
                    startNote = Note.e$_b;
                } else {
                    startNote = Note.E_b;
                }
                wantsFlat = true;
            } else if (button == fmajor) {
                if (startNote == Note.F) {
                    startNote = (ninthNote == eighthNote) ? Note.F: Note.f;
                } else if (startNote == Note.f) {
                    startNote = Note.f$;
                } else {
                    startNote = Note.F;
                }
                wantsFlat = true;
            } else if (button == bbmajor) {
                //startNote = (startNote == 13) ? 25 : 13;
                if (startNote == Note.B_b) {
                    startNote = (ninthNote == eighthNote) ? Note.B_b : Note.b_b;
                } else if (startNote == Note.b_b) {
                    startNote = Note.b$_b;
                } else {
                    startNote = Note.B_b;
                }
                wantsFlat = true;
            }

            if (randomTune.isChecked()) {
                addRandomMajorSequence(startNote, model, 16);
            } else {
                if (ninthNote == eighthNote) {
                    if (startNote >= Note.c) {
                        addDescTwoOctaveMajorScale(startNote, model);
                    } else {
                        addAscTwoOctaveMajorScale(startNote, model);
                    }
                } else {
                    addAscDescMajorScale(startNote, model);
                }
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
        model.add(new DisplayNote(note, 4));
        for (int i = 0; i < kMajorScaleSequence.length; ++i) {
            if (ascending) {
                note += kMajorScaleSequence[i];
            } else {
                note -= kMajorScaleSequence[kMajorScaleSequence.length - 1 - i ];
            }
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

    private void addAscDescMajorScale(int startNote, NoteDocument model) {
        addMajorScale(addMajorScale(startNote, true, model), false, model);
    }
    
    private void addAscTwoOctaveMajorScale(int startNote, NoteDocument model) {
        int next = addMajorScale(startNote, true, model);
        model.pop();
        addMajorScale(next, true, model);
    }
    
    private void addDescTwoOctaveMajorScale(int startNote, NoteDocument model) {
        int next = addMajorScale(startNote, false, model);
        model.pop();
        addMajorScale(next, false, model);
    }
}
