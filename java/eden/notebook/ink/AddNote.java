package eden.notebook.ink;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

public class AddNote extends ActionBarActivity {

    private EditText title;
    private EditText content;
    private int colorIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnote);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences prefs = getSharedPreferences("EdenNotebookSettings",MODE_PRIVATE);

        title = (EditText) findViewById(R.id.add_title);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, prefs.getInt("Title",44));
        content = (EditText) findViewById(R.id.add_content);
        content.setTextSize(TypedValue.COMPLEX_UNIT_SP, prefs.getInt("Content",24));
        if (prefs.getBoolean("Serif",false)) { title.setTypeface(Typeface.SERIF);       content.setTypeface(Typeface.SERIF);      }
        else                                 { title.setTypeface(Typeface.SANS_SERIF);  content.setTypeface(Typeface.SANS_SERIF); }

        final Spinner spinner = (Spinner) findViewById(R.id.color_code_spinner);
        ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(this,R.array.color_type,android.R.layout.simple_spinner_item);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(colorAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                colorIndex = position;
                if (position == 0) spinner.setBackgroundResource(R.drawable.edittext_borders);
                else               spinner.setBackgroundColor(BookAdapter.COLOR_ARRAY[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                colorIndex = 0;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_addnote, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // noinspection SimplifiableIfStatement
        int id = item.getItemId();

        if (id == R.id.action_done) {
            saveFile();
            return true;
        } else if (id == android.R.id.home){
            super.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void saveFile(){

        List<String> existing = new ArrayList<>();
        Collections.addAll(existing, getFilesDir().list());

        String title = this.title.getText().toString();

        //Make sure a file name  does not exist.
        if (title.length() >= 255){
            Toast.makeText(this, "Title is too long: maximum 255 characters", Toast.LENGTH_SHORT).show();
            return;
        } else if (title.equals("")) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        } else if (existing.contains(title)){ //Test if file with same title already exists.
            Toast.makeText(this, "Oops! Already existing title", Toast.LENGTH_SHORT).show();
            return;
        }

        //Writing text into file as a byte format.
        try { FileOutputStream fos = openFileOutput(title, MODE_PRIVATE);
              fos.write(content.getText().toString().getBytes());
              fos.close();                                                  }
        catch (FileNotFoundException e) { return; }
        catch (IOException e)           { Toast.makeText(this, "Sorry. Storage is full", Toast.LENGTH_SHORT).show(); return; }

        //Saving detailed info on the SQL database.
        Calendar c = Calendar.getInstance();

        String ampm;
        if (c.get(Calendar.AM_PM) == Calendar.AM)  ampm = "a.m. ";
        else                                       ampm = "p.m. ";

        String month = String.valueOf(c.get(Calendar.MONTH)+1);
        String day = String.valueOf(c.get(Calendar.DATE));
        String hour = String.valueOf(c.get(Calendar.HOUR));
        String minute = String.valueOf(c.get(Calendar.MINUTE));
        String second = String.valueOf(c.get(Calendar.SECOND));

        if (month.length() == 1)
            month = "0"+month;
        if (day.length() == 1)
            day = "0"+day;
        if (hour.equals("0"))
            hour = "12";
        if (minute.length() == 1)
            minute = "0"+minute;
        if (second.length() == 1)
            second = "0"+second;

        String createdAt =
                String.valueOf(c.get(Calendar.YEAR))+"/"+
                        month+"/"+
                        day+" "+
                        hour+":"+
                        minute+":"+
                        second+
                        ampm+
                        c.getTimeZone().getDisplayName(false, TimeZone.SHORT);

        if (Library.adapter.dataAdapter.insertNewRow(title,createdAt,createdAt,0,colorIndex) < 0) {
            //Failed to save data on SQL.
            Toast.makeText(this, "Error: Failed to save data", Toast.LENGTH_SHORT).show();
            return;
        }

        //Notifying the user and the list adapter.
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        Library.adapter.mCatalog.add(0, title);
        Library.adapter.allCreatedAt.add(0, createdAt);
        Library.adapter.allEditedAt.add(0, createdAt);
        Library.adapter.allStars.add(0, 0);
        Library.adapter.allColors.add(0,colorIndex);

        //Moving on.
        BookAdapter.updated = false;
        super.onBackPressed();
    }
}
