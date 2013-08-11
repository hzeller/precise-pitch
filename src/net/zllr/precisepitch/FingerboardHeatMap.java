package net.zllr.precisepitch;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class FingerboardHeatMap extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerboard_heat_map);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fingerboard_heat_map, menu);
        return true;
    }

}
