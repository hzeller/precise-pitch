package net.zllr.precisepitch;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import java.util.ArrayList;

public class PracticeActivity extends Activity {

    private StaffView staff;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.practice);

        staff = (StaffView) findViewById(R.id.practiceStaff);
        staff.setNotesPerStaff(5);
        ArrayList<StaffView.Note> notes = new ArrayList<StaffView.Note>();
        notes.add(new StaffView.Note(7, 4, Color.BLACK));
        notes.add(new StaffView.Note(17, 4, Color.RED));
        notes.add(new StaffView.Note(12, 4, Color.BLUE));
        staff.setNoteModel(notes);
        staff.onModelChanged();
    }

}
