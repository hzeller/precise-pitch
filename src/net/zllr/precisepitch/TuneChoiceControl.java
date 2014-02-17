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
            
            if (button == seq) {
            
                if (fifthNote == 0 || fifthNote == 7) {
                    model.add(new DisplayNote(19, 4, Color.BLACK));
                    model.add(new DisplayNote(23, 4, Color.BLACK));
                    model.add(new DisplayNote(20, 4, Color.BLACK));
                    model.add(new DisplayNote(23, 4, Color.BLACK));
                    
                    model.add(new DisplayNote(17, 4, Color.BLACK));
                    model.add(new DisplayNote(16, 4, Color.BLACK));
                    model.add(new DisplayNote(14, 4, Color.BLACK));
                    model.add(new DisplayNote(16, 4, Color.BLACK));
                    
                    model.add(new DisplayNote(19, 4, Color.BLACK));
                    model.add(new DisplayNote(23, 4, Color.BLACK));
                    model.add(new DisplayNote(20, 4, Color.BLACK));
                    model.add(new DisplayNote(19, 4, Color.BLACK));
                    
                    model.add(new DisplayNote(16, 4, Color.BLACK));
                    model.add(new DisplayNote(12, 4, Color.BLACK));
                    model.add(new DisplayNote(14, 4, Color.BLACK));
                    model.add(new DisplayNote(16, 4, Color.BLACK));
                } else if (fifthNote == 17) {
                    model.add(new DisplayNote(19, 4, Color.BLACK));
                    model.add(new DisplayNote(23, 4, Color.BLACK));
                    model.add(new DisplayNote(20, 4, Color.BLACK));
                    model.add(new DisplayNote(23, 4, Color.BLACK));
                    
                    model.add(new DisplayNote(19, 4, Color.BLACK));
                    model.add(new DisplayNote(16, 4, Color.BLACK));
                    model.add(new DisplayNote(19, 4, Color.BLACK));
                    model.add(new DisplayNote(16, 4, Color.BLACK));
                    
                    model.add(new DisplayNote(14, 4, Color.BLACK));
                    model.add(new DisplayNote(16, 4, Color.BLACK));
                    model.add(new DisplayNote(12, 4, Color.BLACK));
                    model.add(new DisplayNote(16, 4, Color.BLACK));
                    
                    model.add(new DisplayNote(10, 4, Color.BLACK));
                    model.add(new DisplayNote(16, 4, Color.BLACK));
                    model.add(new DisplayNote(14, 4, Color.BLACK));
                    model.add(new DisplayNote(16, 4, Color.BLACK));
                } else if (fifthNote == 19) {
                    model.add(new DisplayNote(5, 4, Color.BLACK));
                    model.add(new DisplayNote(9, 4, Color.BLACK));
                    model.add(new DisplayNote(12, 4, Color.BLACK));
                    model.add(new DisplayNote(9, 4, Color.BLACK));
                    
                    model.add(new DisplayNote(7, 4, Color.BLACK));
                    model.add(new DisplayNote(9, 4, Color.BLACK));
                    model.add(new DisplayNote(5, 4, Color.BLACK));
                    model.add(new DisplayNote(9, 4, Color.BLACK));
                    
                    model.add(new DisplayNote(7, 4, Color.BLACK));
                    model.add(new DisplayNote(10, 4, Color.BLACK));
                    model.add(new DisplayNote(16, 4, Color.BLACK));
                    model.add(new DisplayNote(12, 4, Color.BLACK));
                    
                    model.add(new DisplayNote(9, 4, Color.BLACK));
                    model.add(new DisplayNote(5, 4, Color.BLACK));
                    model.add(new DisplayNote(9, 4, Color.BLACK));
                    model.add(new DisplayNote(5, 4, Color.BLACK));
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
                //startNote = (startNote == 3) ? 15 : 3;
                if (startNote == 3) {
                    startNote = (ninthNote == eighthNote) ? 3 : 15;
                } else if (startNote == 15) {
                    startNote = 15 + 12;
                } else {
                    startNote = 3;
                }
            } else if (button == gmajor) {
                //startNote = (startNote == 10) ? 22 : 10;
                if (startNote == 10) {
                    startNote = (ninthNote == eighthNote) ? 10 : 22;
                } else if (startNote == 22) {
                    startNote = 22 + 12;
                } else {
                    startNote = 10;
                }
            } else if (button == dmajor) {
                //startNote = (startNote == 5) ? 17 : 5;
                if (startNote == 5) {
                    startNote = (ninthNote == eighthNote) ? 5 : 17;
                } else if (startNote == 17) {
                    startNote = 17 + 12;
                } else {
                    startNote = 5;
                }
            } else if (button == fmajor) {
                //startNote = (startNote == 8) ? 20 : 8;
                if (startNote == 8) {
                    startNote = (ninthNote == eighthNote) ? 8 : 20;
                } else if (startNote == 20) {
                    startNote = 20 + 12;
                } else {
                    startNote = 8;
                }
                wantsFlat = true;
            } else if (button == bbmajor) {
                //startNote = (startNote == 13) ? 25 : 13;
                if (startNote == 13) {
                    startNote = (ninthNote == eighthNote) ? 13 : 25;
                } else if (startNote == 25) {
                    startNote = 25 + 12;
                } else {
                    startNote = 13;
                }
                wantsFlat = true;
            }

            if (randomTune.isChecked()) {
                addRandomMajorSequence(startNote, model, 16);
            } else {
                if (ninthNote == eighthNote) {
                    if (startNote >= 15) {
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
