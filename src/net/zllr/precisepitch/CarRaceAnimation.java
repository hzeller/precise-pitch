package net.zllr.precisepitch;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class CarRaceAnimation extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_race_animation);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.car_race_animation, menu);
        return true;
    }

}
