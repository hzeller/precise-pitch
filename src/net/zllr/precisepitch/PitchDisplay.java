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
import android.widget.Spinner;
import android.widget.TextView;

public class PitchDisplay extends Activity {
    private TextView frequencyDisplay;
    private TextView noteDisplay;
    private TextView flatDisplay;
    private TextView sharpDisplay;
    private TextView decibelView;
    private TextView prevNote;
    private TextView nextNote;
    private CenterOffsetView offsetCentView;

    private int centThreshold = 20;
    private MicrophonePitchPoster pitchPoster;

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
        noteDisplay.setText(".");
        offsetCentView = (CenterOffsetView) findViewById(R.id.centView);
        offsetCentView.setRange(50);
        offsetCentView.setQuantization(10);
        addAccidentalListener();        
        ensurePosterRunning();
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
                        break;
                    case R.id.sharpRadio:
                        keyDisplay = KeyDisplay.DISPLAY_SHARP;
                        break;
                }
            }
        });
        ((RadioButton) findViewById(R.id.sharpRadio)).setChecked(true);
    }

    private void ensurePosterRunning() {
        MicrophonePitchPoster poster = (MicrophonePitchPoster) getLastNonConfigurationInstance();
        if (poster == null || !poster.isAlive()) {
            pitchPoster = new MicrophonePitchPoster(30);
            pitchPoster.start();
        } else {
            pitchPoster = poster;
        }
        final Handler handler = new UIUpdateHandler();
        pitchPoster.setHandler(handler);
    }

    public Object onRetainNonConfigurationInstance() {
        return pitchPoster;  // some magic to make certain things survive lifecycle changes.
    }

    // Whenever MicrophonePitchPoster has a new note value available, it will post it to the
    // message queue, received here.
    private final class UIUpdateHandler extends Handler {
        public void handleMessage(Message msg) {
            final MicrophonePitchPoster.PitchData data
                = (MicrophonePitchPoster.PitchData) msg.obj;

            if (data != null && data.decibel > -20) {
                frequencyDisplay.setText(String.format(data.frequency < 200 ? "%.1fHz" : "%.0fHz",
                                                       data.frequency));
                final String printNote = noteNames[keyDisplay.ordinal()][data.note];
                noteDisplay.setText(printNote.substring(0, 1));
                final String accidental = printNote.length() > 1 ? printNote.substring(1) : "";
                flatDisplay.setVisibility("b".equals(accidental) ? View.VISIBLE : View.INVISIBLE);
                sharpDisplay.setVisibility("#".equals(accidental) ? View.VISIBLE : View.INVISIBLE);
                nextNote.setText(noteNames[keyDisplay.ordinal()][(data.note + 1) % 12]);
                prevNote.setText(noteNames[keyDisplay.ordinal()][(data.note + 11) % 12]);
                final int c = Math.abs(data.cent) > centThreshold
                        ? Color.rgb(255, 50, 50)
                        : Color.rgb(50, 255, 50);
                noteDisplay.setTextColor(c);
                flatDisplay.setTextColor(c);
                sharpDisplay.setTextColor(c);
                offsetCentView.setValue((int) data.cent);
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
            }
            if (data != null) {
                decibelView.setVisibility(data.decibel > -60 ? View.VISIBLE : View.INVISIBLE);
                decibelView.setText(String.format("%.0fdB", data.decibel));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.pitch_display, menu);
        return true;
    }
}
