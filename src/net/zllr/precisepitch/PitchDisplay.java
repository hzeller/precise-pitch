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

import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class PitchDisplay extends Activity {
    private TextView frequencyDisplay;
    private TextView noteDisplay;
    private TextView flatDisplay;
    private TextView sharpDisplay;
    private TextView decibelView;
    private TextView prevNote;
    private TextView nextNote;
    private CenterOffsetView offsetCentView;
    private StaffView staff;

    private int centThreshold = 20;
    private MicrophonePitchPoster pitchPoster;
    private StaffView staffView;

    private enum KeyDisplay {
        DISPLAY_FLAT,
        DISPLAY_SHARP,
    }
    private KeyDisplay keyDisplay = KeyDisplay.DISPLAY_SHARP;
    private static final String noteNames[][] = {
        { "A", "Bb", "B", "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab" },
        { "A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#" },
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        frequencyDisplay = (TextView) findViewById(R.id.frequencyDisplay);
        flatDisplay = (TextView) findViewById(R.id.flatText);
        sharpDisplay = (TextView) findViewById(R.id.sharpText);
        prevNote = (TextView) findViewById(R.id.nextLower);
        nextNote = (TextView) findViewById(R.id.nextHigher);
        decibelView = (TextView) findViewById(R.id.decibelView);
        noteDisplay = (TextView) findViewById(R.id.noteDisplay);
        noteDisplay.setKeepScreenOn(true);
        noteDisplay.setText("");
        staffView = (StaffView) findViewById(R.id.staffView);
        offsetCentView = (CenterOffsetView) findViewById(R.id.centView);
        offsetCentView.setRange(50);
        offsetCentView.setQuantization(10);
        addAccidentalListener();        
    }
    
    private void addAccidentalListener() {
        final RadioGroup accidentalGroup = (RadioGroup) findViewById(R.id.accidentalSelection);
        accidentalGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
            	flatDisplay.setVisibility(View.INVISIBLE);
            	sharpDisplay.setVisibility(View.INVISIBLE);
                switch (checkedId) {
                    case R.id.flatRadio:
                        keyDisplay = KeyDisplay.DISPLAY_FLAT;
                        staffView.setKeyDisplay(0);
                        break;
                    case R.id.sharpRadio:
                        keyDisplay = KeyDisplay.DISPLAY_SHARP;
                        staffView.setKeyDisplay(1);
                        break;
                }
            }
        });
        ((RadioButton) findViewById(R.id.sharpRadio)).setChecked(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pitchPoster.stopSampling();
        pitchPoster = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        pitchPoster = new MicrophonePitchPoster(60);
        pitchPoster.setHandler(new UIUpdateHandler());
        pitchPoster.start();
        Toast.makeText(this, pitchPoster.getSampleCount() + " Samples",
                       Toast.LENGTH_SHORT).show();
    }

    // Whenever MicrophonePitchPoster has a new note value available, it will
    // post it to the message queue, received here.
    private final class UIUpdateHandler extends Handler {
        public void handleMessage(Message msg) {
            final MicrophonePitchPoster.PitchData data
                = (MicrophonePitchPoster.PitchData) msg.obj;

            if (data != null && data.decibel > -20) {
                frequencyDisplay.setText(String.format(data.frequency < 200 ? "%.1fHz" : "%.0fHz",
                                                       data.frequency));
                final String printNote = noteNames[keyDisplay.ordinal()][data.note % 12];
                noteDisplay.setText(printNote.substring(0, 1));
                final String accidental = printNote.length() > 1 ? printNote.substring(1) : "";
                flatDisplay.setVisibility("b".equals(accidental) ? View.VISIBLE : View.INVISIBLE);
                sharpDisplay.setVisibility("#".equals(accidental) ? View.VISIBLE : View.INVISIBLE);
                nextNote.setText(noteNames[keyDisplay.ordinal()][(data.note + 1) % 12]);
                prevNote.setText(noteNames[keyDisplay.ordinal()][(data.note + 11) % 12]);
                final boolean inTune = Math.abs(data.cent) <= centThreshold;
                final int c = inTune ? Color.rgb(50, 255, 50) : Color.rgb(255,50, 50);
                noteDisplay.setTextColor(c);
                flatDisplay.setTextColor(c);
                sharpDisplay.setTextColor(c);
                offsetCentView.setValue((int) data.cent);
                staffView.pushNote(new StaffView.Note(data.note, 4,
                                                      inTune ? Color.BLACK : Color.rgb(200, 0, 0)));
            } else {
                // No valid data to display. Set most elements invisible.
                frequencyDisplay.setText("");
                final int ghostColor = Color.rgb(40,  40,  40);
                noteDisplay.setTextColor(ghostColor);
                flatDisplay.setTextColor(ghostColor);
                sharpDisplay.setTextColor(ghostColor);
                prevNote.setText("");
                nextNote.setText("");
                offsetCentView.setValue(100);  // out of range, not displayed.
                staffView.pushNote(null);
            }
            if (data != null && data.decibel > -60) {
                decibelView.setVisibility(View.VISIBLE);
                decibelView.setText(String.format("%.0fdB", data.decibel));
            } else {
                decibelView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
