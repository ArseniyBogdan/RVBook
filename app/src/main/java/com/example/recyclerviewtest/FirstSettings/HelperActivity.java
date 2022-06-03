package com.example.recyclerviewtest.FirstSettings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.recyclerviewtest.MainActivity;

public class HelperActivity extends Activity {

    SharedPreferences prefs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Perhaps set content view here
//        this.deleteDatabase("database_for_diary");

        prefs = getSharedPreferences("pref", MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!prefs.getBoolean("firstRun", false)) {
            // Do first run stuff here then set 'firstrun' as false
            //strat  DataActivity beacuase its your app first run
            // using the following line to edit/commit prefs
            startActivity(new Intent(HelperActivity.this , SettingsActivity.class));
        }
        else if(!prefs.getBoolean("Start_Login_activity", false)){
            startActivity(new Intent(HelperActivity.this , LoginActivity.class));
        }
        else {
            startActivity(new Intent(HelperActivity.this , MainActivity.class));
        }
        finish();
    }

}