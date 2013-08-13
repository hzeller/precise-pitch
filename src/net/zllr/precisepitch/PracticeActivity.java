package net.zllr.precisepitch;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PracticeActivity extends Activity {
    private static final int kMajorScaleSequence[] = { 2, 2, 1, 2, 2, 2, 1 };
    private static final int kCentThreshold = 20;

    private static final int futureNoteColor = Color.rgb(200, 200, 200);
    private static final int successNoteColor = Color.rgb(0, 180, 0);
    private static final int playNoteColor  = Color.BLACK;

    private final ArrayList<StaffView.Note> noteModel;
    private StaffView staff;
    private Button randomTune;
    private Button cmajor;
    private Button gmajor;
    private Button dmajor;
    private Button fmajor;
    private Button bbmajor;
    private CenterOffsetView ledview;
    private Button startbutton;
    private TextView instructions;
    private Button restartbutton;
    private MicrophonePitchPoster pitchPoster;
    private long startPracticeTime;

    private enum State {
        EMPTY_SCALE,     // initial state or after 'clear'
        WAIT_FOR_START,  // when notes are visible. Start button visible.
        PRACTICE,        // The game.
        FINISHED         // finished assignment.
    }

    public PracticeActivity() {
        noteModel = new ArrayList<StaffView.Note>();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.practice);

        staff = (StaffView) findViewById(R.id.practiceStaff);
        staff.setNotesPerStaff(8);

        // For now we have a couple of buttons to create some basics, but
        // these should be replaced by: (a) Spinner (for choosing scales and randomTune)
        // and (b) direct editing.

        final NoteGenerationListener noteCreator = new NoteGenerationListener();
        randomTune = (Button) findViewById(R.id.newRandom);
        randomTune.setOnClickListener(noteCreator);
        cmajor = (Button) findViewById(R.id.newCMajor);
        cmajor.setOnClickListener(noteCreator);
        gmajor = (Button) findViewById(R.id.newGMajor);
        gmajor.setOnClickListener(noteCreator);
        dmajor = (Button) findViewById(R.id.newDMajor);
        dmajor.setOnClickListener(noteCreator);
        fmajor = (Button) findViewById(R.id.newFMajor);
        fmajor.setOnClickListener(noteCreator);
        bbmajor = (Button) findViewById(R.id.newBbMajor);
        bbmajor.setOnClickListener(noteCreator);
        ledview = (CenterOffsetView) findViewById(R.id.practiceLedDisplay);
        ledview.setKeepScreenOn(true);
        startbutton = (Button) findViewById(R.id.practiceStartButton);
        startbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doPractice();
            }
        });
        restartbutton = (Button) findViewById(R.id.practiceRestartButton);
        restartbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSampling();
                setActivityState(State.EMPTY_SCALE);
                setActivityState(State.WAIT_FOR_START);  // We already have
            }
        });
        instructions = (TextView) findViewById(R.id.practiceInstructions);

        staff.setNoteModel(noteModel);
        staff.setKeyDisplay(1);
        setActivityState(State.EMPTY_SCALE);
    }

    // Kinda hardcoded now :)
    private final class NoteGenerationListener implements View.OnClickListener {
        public void onClick(View button) {
            boolean wantsFlat = false;
            int currentStartNote = 0;
            // We use the current start note to help toggle major scales.
            if (noteModel.size() > 0)
                currentStartNote = noteModel.get(0).pitch;
            noteModel.clear();
            if (button == randomTune) {
                for (int i = 0; i < 8; ++i) {
                    noteModel.add(new StaffView.Note(
                            3 + (int) (16 * Math.random()),
                            4, Color.BLACK));
                }
            } else if (button == cmajor) {
                addMajorScale(currentStartNote == 3 ? 15 : 3, true, noteModel);
            } else if (button == gmajor) {
                addMajorScale(currentStartNote == 10 ? 22 : 10, true, noteModel);
            } else if (button == dmajor) {
                addMajorScale(currentStartNote == 5 ? 17 : 5, true, noteModel);
            } else if (button == fmajor) {
                addMajorScale(currentStartNote == 8 ? 20 : 8, true, noteModel);
                wantsFlat = true;
            } else if (button == bbmajor) {
                addMajorScale(currentStartNote == 13 ? 25 : 13, true, noteModel);
                wantsFlat = true;
            }

        staff.onModelChanged();
            staff.setKeyDisplay(wantsFlat ? 0 : 1);
            setActivityState(noteModel.size() > 0
                                     ? State.WAIT_FOR_START
                                     : State.EMPTY_SCALE);
        }
    }

    private void doPractice() {
        if (noteModel.size() == 0) return;
        setActivityState(State.PRACTICE);
        // Todo: put all things related to the game into thing.
        pitchPoster = new MicrophonePitchPoster(60);
        pitchPoster.setHandler(new PitchFollowHandler(noteModel));
        pitchPoster.start();
        startPracticeTime = System.currentTimeMillis();
    }

    private void endPractice() {
        stopSampling();
        long duration = System.currentTimeMillis() - startPracticeTime;
        instructions.setText(String.format("%3.1f seconds!", duration / 1000.0));
        setActivityState(State.FINISHED);
    }

    private void stopSampling() {
        if (pitchPoster != null) {
            pitchPoster.stopSampling();
            pitchPoster = null;
        }
    }

    private class PitchFollowHandler extends Handler {
        PitchFollowHandler(List<StaffView.Note> model) {
            iterator = model.iterator();
            for (StaffView.Note n : model) {
                n.color = futureNoteColor;
            }
            ellipsis = "";
            checkNextNote();
        }

        public void handleMessage(Message msg) {
            final MicrophonePitchPoster.PitchData data
                    = (MicrophonePitchPoster.PitchData) msg.obj;
            if (data == null)
                return;

            ledview.setVisibility(data.note == currentNote.pitch
                                          ? View.VISIBLE : View.INVISIBLE);
            ledview.setValue(data.cent);
            if (data.note == currentNote.pitch
                    && Math.abs(data.cent) < kCentThreshold) {
                ++ticksInTune;
            } else {
                ticksInTune = 0;
            }

            if (ticksInTune == 0) {
                instructions.setText("Find the note.");
            }
            else if (ticksInTune > 0 && ticksInTune < kHoldTime) {
                instructions.setText("Alright, now hold.." +
                                    String.valueOf(kHoldTime - ticksInTune));
            }
            else if (ticksInTune >= kHoldTime) {
                instructions.setText("Yay, continue!" + ellipsis);
                ellipsis += ".";
                checkNextNote();
            }
        }

        private void checkNextNote() {
            if (currentNote != null)
                currentNote.color = successNoteColor;
            if (!iterator.hasNext()) {
                endPractice();
                return;
            }
            currentNote = iterator.next();
            currentNote.color = playNoteColor;
            ticksInTune = 0;
            staff.onModelChanged();
        }

        private final static int kHoldTime = 15;
        final Iterator<StaffView.Note> iterator;
        StaffView.Note currentNote;
        int ticksInTune;
        String ellipsis;
    }

    private void setGeneratorButtonsVisibility(int visibility) {
        randomTune.setVisibility(visibility);
        gmajor.setVisibility(visibility);
        cmajor.setVisibility(visibility);
        dmajor.setVisibility(visibility);
        fmajor.setVisibility(visibility);
        bbmajor.setVisibility(visibility);
    }

    private void setActivityState(State state) {
        switch (state) {
            case EMPTY_SCALE:
                startbutton.setVisibility(View.INVISIBLE);
                restartbutton.setVisibility(View.INVISIBLE);
                ledview.setVisibility(View.INVISIBLE);
                setGeneratorButtonsVisibility(View.VISIBLE);
                instructions.setText("Choose your chant of doom.");
                break;
            case WAIT_FOR_START:
                for (StaffView.Note n : noteModel) {
                    n.color = Color.BLACK;
                }
                staff.onModelChanged();
                instructions.setText("Refine or start.");
                startbutton.setVisibility(View.VISIBLE);
                setGeneratorButtonsVisibility(View.VISIBLE);
                break;
            case PRACTICE:
                instructions.setText("Play notes. Hold when in tune.");
                startbutton.setVisibility(View.INVISIBLE);
                restartbutton.setVisibility(View.VISIBLE);
                ledview.setVisibility(View.VISIBLE);
                setGeneratorButtonsVisibility(View.INVISIBLE);
                break;
            case FINISHED:
                stopSampling();
                startbutton.setVisibility(View.INVISIBLE);
                restartbutton.setVisibility(View.VISIBLE);
                ledview.setVisibility(View.INVISIBLE);
                setGeneratorButtonsVisibility(View.INVISIBLE);
        }
    }
    // Add a major scale to the model.
    private int addMajorScale(int startNote, boolean ascending,
                              List<StaffView.Note> model) {
        int note = startNote;
        model.add(new StaffView.Note(note, 4, Color.BLACK));
        for (int i = 0; i < kMajorScaleSequence.length; ++i) {
            if (ascending) {
                note += kMajorScaleSequence[i];
            } else {
                note -= kMajorScaleSequence[kMajorScaleSequence.length - 1 - i ];
            }
            model.add(new StaffView.Note(note, 4, Color.BLACK));
        }
        return note;
    }
}
