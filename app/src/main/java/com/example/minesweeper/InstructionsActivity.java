package com.example.minesweeper;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

public class InstructionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        setupActionBar();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            // Use our toolbar specified in XML as the ActionBar.
            // It will appear beneath the theme's built-in ActionBar, unless that AB is hidden
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            getDelegate().setSupportActionBar(toolbar);

            // Show the Up/back button in the action bar.
            ActionBar currentActionBar = getDelegate().getSupportActionBar();
            assert currentActionBar != null;

            currentActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // The home button was added in setupActionBar() above.
        // The Setting menu item was automatically added to the menu XML.
        // We handle both of these here.

        // The other item we added to the XML was the About item.
        // About has its own handler specified in its onClick.
        // So we do not handle that here; otherwise, we would do so.

        // This ID represents the Home or Up button. In the case of this
        // activity, the Up button is shown. Use NavUtils to allow users
        // to navigate up one level in the application structure. For
        // more details, see the Navigation pattern on Android Design:
        //
        // http://developer.android.com/design/patterns/navigation.html#up-vs-back
        //
        // auto_generated_TODO: If Settings has multiple levels, Up should navigate up that hierarchy.
        // This to do above can be ignored here as there is only one level.

        // I commented out the following line in favor of my closeActivity() alternative below.
        // NavUtils.navigateUpFromSameTask(this);

        // The transition used with this command is better-suited than the default
        // This is due to the Dialog theme we chose.
        // As well, this way seems to ensure that the data in the EditTexts in main will stay as is.

        // Note that the id for the home/back button is in Android's "R", not the Applications's "R"
        if (id == android.R.id.home) {
            closeActivity();
            return true;
        } else if (id == R.id.action_how_to_play) {
            showSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings({"UnusedParameters", "unused"})
    public void showAbout(MenuItem item) {
        // call our showAbout method to show our About dialog
        showAbout();
    }

    private void showAbout() {

        // Create listener for use with dialog window (could also be created anonymously in setButton...
        DialogInterface.OnClickListener dialogOnClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Nothing needed to do here
                    }
                };

        // Create dialog window
        AlertDialog alertDialogAbout = new AlertDialog.Builder(InstructionsActivity.this).create();
        alertDialogAbout.setTitle(getString(R.string.aboutDialog_title));
        alertDialogAbout.setIcon(R.drawable.ic_launch_white_24dp);
        alertDialogAbout.setMessage(getString(R.string.aboutDialog_banner));
        alertDialogAbout.setButton(DialogInterface.BUTTON_NEUTRAL,
                getString(R.string.OK), dialogOnClickListener);

        // Show the dialog window
        alertDialogAbout.show();
    }

    // This would be called from the onOptionsItemSelected method
    @SuppressWarnings("unused")
    private void showSettings() {
/*        // Here, we open up our settings activity
        Intent intent = new Intent (getApplicationContext (), SettingsActivity.class);
        startActivity (intent);*/
    }

    @SuppressWarnings({"UnusedParameters", "unused"})
    public void cmdOK(View view) {
        closeActivity();
    }

    private void closeActivity() {
        finish();
    }
}