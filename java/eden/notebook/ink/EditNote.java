package eden.notebook.ink;

import android.content.Intent;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

public class EditNote extends ActionBarActivity {

    //Layout info.
    private EditText title;
    private EditText content;

    //Adapter info.
    private int adapterType;

    //File info.
    private String filename;
    private String createdAt;
    private int starred;
    private int colorIndex;

    //Saving process info.
    private boolean deleteSuccessful;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editnote);
        adapterType = getIntent().getIntExtra("AdapterType",1);
        SharedPreferences prefs = getSharedPreferences("EdenNotebookSettings",MODE_PRIVATE);

        title = (EditText) findViewById(R.id.edit_title);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, prefs.getInt("Title", 44));
        content = (EditText) findViewById(R.id.edit_content);
        content.setTextSize(TypedValue.COMPLEX_UNIT_SP, prefs.getInt("Content", 24));
        if (prefs.getBoolean("Serif",false)) { title.setTypeface(Typeface.SERIF);       content.setTypeface(Typeface.SERIF);      }
        else                                 { title.setTypeface(Typeface.SANS_SERIF);  content.setTypeface(Typeface.SANS_SERIF); }

        //Obtaining file id info.
        int mFileIndex = getIntent().getIntExtra("index", 0);
        if (adapterType == 1){
            filename = Library.adapter.mCatalog.get(mFileIndex); //The original file name.
            createdAt = Library.adapter.allCreatedAt.get(mFileIndex);
            starred = Library.adapter.allStars.get(mFileIndex);
            colorIndex = Library.adapter.allColors.get(mFileIndex);
        } else {
            filename = ColorLibrary.adapter.mCatalog.get(mFileIndex); //The original file name.
            createdAt = ColorLibrary.adapter.allCreatedAt.get(mFileIndex);
            starred = ColorLibrary.adapter.allStars.get(mFileIndex);
            colorIndex = ColorLibrary.adapter.allColors.get(mFileIndex);
        }

        //Setting title text.
        title.setText(filename);

        //Setting content text.
        try {
            //Obtaining byte string info from filename.
            FileInputStream fis = openFileInput(filename);
            byte[] data = new byte[fis.available()];
            if (fis.read(data) != 0) // Sometimes if the saved string is nothing (""), then the read()
                                     // will constantly return 0 and fall into the constant while loop.
                                     // Must make sure there is something to read() before proceeding.
                while (fis.read(data) != -1) { /*This loop constantly extracts byte that will
                                                 eventually be converted to a string byte by byte.*/ }

            content.setText(new String(data));
            fis.close();                                                }
        catch (FileNotFoundException e){ Toast.makeText(this, "Error: File does not exist", Toast.LENGTH_SHORT).show(); }
        catch (IOException e)          { Toast.makeText(this, "Error: Failed to extract note from storage", Toast.LENGTH_SHORT).show(); }

        final Spinner spinner = (Spinner) findViewById(R.id.color_code_spinner);
        ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(this,R.array.color_type,android.R.layout.simple_spinner_item);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(colorAdapter);

        spinner.setSelection(colorIndex);
        if (colorIndex == 0) spinner.setBackgroundResource(R.drawable.edittext_borders);
        else                 spinner.setBackgroundColor(BookAdapter.COLOR_ARRAY[colorIndex]);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                colorIndex = position;
                if (position == 0) spinner.setBackgroundResource(R.drawable.edittext_borders);
                else               spinner.setBackgroundColor(BookAdapter.COLOR_ARRAY[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { colorIndex = 0; }
        });

        deleteSuccessful = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editnote, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.action_done) {
            saveFile();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveFile() {

        List<String> existing = new ArrayList<>();
        Collections.addAll(existing, getFilesDir().list());

        //The MAYBE new file name.
        String title = this.title.getText().toString().trim();

        //Make sure a file name does not exist.
        if (title.length() >= 255){
            Toast.makeText(this, "Title is too long: maximum 255 characters", Toast.LENGTH_SHORT).show();
            return;
        } else if (title.length() == 0) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        } else if (existing.contains(title) && !title.equals(filename)){ //Test if file with same NEW title already exists.
            Toast.makeText(this, "Title is already in use", Toast.LENGTH_SHORT).show();
            return;
        }

        try {//Overwriting or newly writing text into file as a byte format.
             FileOutputStream fos = openFileOutput(title, MODE_PRIVATE);
             fos.write(content.getText().toString().trim().getBytes());
             fos.close();                                                       }
        catch (FileNotFoundException e) { return; }
        catch (IOException e) {
            Toast.makeText(this, "Sorry. Storage is full", Toast.LENGTH_SHORT).show();
            //Deleting file with NEW title after failing to save prevents the new title from being taken and unable to use.
            if (!filename.equals(title)) deleteFile(title);
            return;
        }

        //Setting the date strings.
        Calendar c = Calendar.getInstance();
        String month = String.valueOf(c.get(Calendar.MONTH)+1); if (month.length() == 1)
                                                                    month = "0"+month;
        String day = String.valueOf(c.get(Calendar.DATE));      if (day.length() == 1)
                                                                    day = "0"+day;
        String hour = String.valueOf(c.get(Calendar.HOUR));     if (hour.equals("0"))
                                                                    hour = "12";
        String minute = String.valueOf(c.get(Calendar.MINUTE)); if (minute.length() == 1)
                                                                    minute = "0"+minute;
        String second = String.valueOf(c.get(Calendar.SECOND)); if (second.length() == 1)
                                                                    second = "0"+second;
        String ampm; if (c.get(Calendar.AM_PM) == Calendar.AM)  ampm = "a.m. ";
                     else                                       ampm = "p.m. ";
        String editedAt = //Put date info together.
                String.valueOf(c.get(Calendar.YEAR))+"/"+
                        month+"/"+
                            day+" "+
                                hour+":"+
                                    minute+":"+
                                        second+
                                            ampm+
                                                c.getTimeZone().getDisplayName(false, TimeZone.SHORT);

        //Removing the row with original note info
        if (!deleteSuccessful && new NoteDatabaseAdapter(this).deleteRow(filename) < 0){
            //Failed to delete original row.
            Toast.makeText(this, "Error: Failed to remove original data", Toast.LENGTH_SHORT).show();
            if (!filename.equals(title)) deleteFile(title); //Undo save.
            return;
        }
        deleteSuccessful = true;
        if (new NoteDatabaseAdapter(this).insertNewRow(title, createdAt, editedAt, starred, colorIndex) < 0) {
            //Failed to save data on SQL.
            Toast.makeText(this, "Error: Failed to save on SQL - DO NOT LEAVE AND TRY AGAIN", Toast.LENGTH_LONG).show();
            if (!filename.equals(title)) deleteFile(title); //Undo save
            return;
        }

        Library.adapter.deleteNote(filename);
        Library.adapter.addNote(title,createdAt,editedAt,starred,colorIndex);
        Library.updated = false;
        if (adapterType == 2){
            ColorLibrary.adapter.deleteNote(filename);
            ColorLibrary.adapter.addNote(title,createdAt,editedAt,starred,colorIndex);
            ColorLibrary.updated = false;
        }

        //Remove file with old title only if it's different AT THE END. This prevents loosing content when failing to save new content data.
        if (!filename.equals(title)) deleteFile(filename);

        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();

        //Moving on.
        Intent intent = new Intent(this, ViewNote.class);
        intent.putExtra("index", 0); //When the user saves edit, this item will be the first item.
        startActivity(intent);
    }
}
