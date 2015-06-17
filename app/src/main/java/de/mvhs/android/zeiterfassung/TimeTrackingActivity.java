package de.mvhs.android.zeiterfassung;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import de.mvhs.android.zeiterfassung.db.ZeitContract;
import de.mvhs.android.zeiterfassung.utils.Converter;


public class TimeTrackingActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_tracking);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Menü aus XML laden
        getMenuInflater().inflate(R.menu.menu_time_tracking, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int menuId = item.getItemId();

        boolean handled = false;

        switch (menuId) {
            case R.id.action_list:

                // Starten der Auflistung
                Intent listIntent = new Intent(this, RecordListActivity.class);
                startActivity(listIntent);

                handled = true;
                break;

            case R.id.action_new:
                // Starten manueller Eintrag
                Intent newIntent = new Intent(this, EditRecordActivity.class);
                newIntent.putExtra(EditRecordFragment.IS_EDITABLE_KEY, true);
                startActivity(newIntent);
                break;

            default:
                handled = super.onOptionsItemSelected(item);
                break;
        }

        return handled;
    }
}
