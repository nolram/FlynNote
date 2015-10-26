package com.lab11.nolram.cadernocamera;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.lab11.nolram.Constants;


public class CriarFolhaActivity extends AppCompatActivity {

    private Callbacks mCallbacks;
    private CriarFolhaActivityFragment fragment;

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        if(!Constants.DEBUG) {
            GoogleAnalytics.getInstance(CriarFolhaActivity.this).reportActivityStart(this);
        }
    }


    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        if(!Constants.DEBUG) {
            GoogleAnalytics.getInstance(CriarFolhaActivity.this).reportActivityStop(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_folha);

        fragment = (CriarFolhaActivityFragment) getSupportFragmentManager().findFragmentById(
                R.id.fragment_criar_folha);
        mCallbacks = (Callbacks) fragment;

        if(!Constants.DEBUG) {
        /*  Analytics */
            Tracker t = ((FlynNoteApp) getApplication()).getTracker(FlynNoteApp.TrackerName.APP_TRACKER);
            t.setScreenName(CriarFolhaActivity.class.getName());
            t.send(new HitBuilders.AppViewBuilder().build());
        /* Fim Analytics */
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_criar_folha, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mCallbacks != null)
            mCallbacks.onBackPressedCallback();
    }

    public interface Callbacks {
        void onBackPressedCallback();
    }
}
