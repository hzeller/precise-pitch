package net.zllr.precisepitch;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PrecisePitchHome extends Activity implements OnItemClickListener {
    private static final String entries[][] = {
        {"Tuner", "Live pitch view", "net.zllr.precisepitch.TunerActivity"},
        {"Practice", "Play a sequence of notes", "net.zllr.precisepitch.PracticeActivity"},
    };

    private ListView homeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_precise_pitch_home);

        homeList = (ListView) findViewById(R.id.homeList);
        homeList.setOnItemClickListener(this);

        HomeListAdapter adapter = new HomeListAdapter(this, R.layout.home_list_entry, entries);

        homeList.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        Class<?> c = null;
        String name = entries[position][2];
        if (name != null) {
            try {
                c = Class.forName(name);
                Intent intent = new Intent(this, c);
                startActivity(intent);
            } catch (ClassNotFoundException e) {
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.precise_pitch_home, menu);
        return true;
    }

    private static class HomeListAdapter extends ArrayAdapter<String[]> {
        Context context;
        int layoutResourceId;

        String data[][] = null;

        public HomeListAdapter(Context context, int layoutResourceId, String[][] data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            HomeEntryHolder holder = null;

            if (row == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                holder = new HomeEntryHolder();
                holder.homeListEntryText1 = (TextView)row.findViewById(R.id.homeListEntryText1);
                holder.homeListEntryText2 = (TextView)row.findViewById(R.id.homeListEntryText2);

                row.setTag(holder);
            } else {
                holder = (HomeEntryHolder)row.getTag();
            }

            holder.homeListEntryText1.setText(data[position][0]);
            holder.homeListEntryText2.setText(data[position][1]);

            return row;
        }

        static class HomeEntryHolder {
            TextView homeListEntryText1;
            TextView homeListEntryText2;
        }
    }
}
