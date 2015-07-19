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

    private EditText title;
    private EditText content;
    private int mFileIndex;
    private String filename;
    private int colorIndex;
    private int adapterType;

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
        mFileIndex = getIntent().getIntExtra("index", 0);
        if (adapterType == 1){
            filename = Library.adapter.mCatalog.get(mFileIndex); //The original file name.
            colorIndex = Library.adapter.allColors.get(mFileIndex);
        } else {
            filename = ColorLibrary.adapter.mCatalog.get(mFileIndex); //The original file name.
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
            public void onNothingSelected(AdapterView<?> parent) {
                colorIndex = 0;
            }
        });
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

    protected void saveFile() {

        List<String> existing = new ArrayList<>();
        Collections.addAll(existing, getFilesDir().list());

        String title = this.title.getText().toString();

        //Make sure a file name does not exist.
        if (title.length() >= 255){
            Toast.makeText(this, "Title is too long: maximum 255 characters", Toast.LENGTH_SHORT).show();
            return;
        } else if (title.equals("")) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        } else if (existing.contains(title) && !title.equals(filename)){ //Test if file with same NEW title already exists.
            Toast.makeText(this, "Title is already in use", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
             //remove file with old title
             deleteFile(filename);

             //Overwriting or newly writing text into file as a byte format.
             FileOutputStream fos = openFileOutput(title, MODE_PRIVATE);
             fos.write(content.getText().toString().getBytes());
             fos.close();                                              }
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

        String editedAt =
                String.valueOf(c.get(Calendar.YEAR))+"/"+
                        month+"/"+
                        day+" "+
                        hour+":"+
                        minute+":"+
                        second+
                        ampm+
                        c.getTimeZone().getDisplayName(false, TimeZone.SHORT);


        if (adapterType == 1){
            String createdAt = Library.adapter.allCreatedAt.get(mFileIndex);
            int stared = Library.adapter.allStars.get(mFileIndex);

            //Removing the row with original note info
            if (Library.adapter.dataAdapter.deleteRow(filename) < 0){
                //Failed to delete original row.
                Toast.makeText(this, "Error: Failed to remove original data", Toast.LENGTH_SHORT).show();
                return;
            }
            if (Library.adapter.dataAdapter.insertNewRow(title, createdAt, editedAt, stared, colorIndex) < 0) {
                //Failed to save data on SQL.
                Toast.makeText(this, "Error: Failed to save data - Database corrupted", Toast.LENGTH_SHORT).show();
                return;
            }

            //Notifying the user and the list adapter.
            Library.adapter.mCatalog.remove(mFileIndex);
            Library.adapter.allCreatedAt.remove(mFileIndex);
            Library.adapter.allEditedAt.remove(mFileIndex);
            Library.adapter.allStars.remove(mFileIndex);
            Library.adapter.allColors.remove(mFileIndex);

            Library.adapter.mCatalog.add(0, title);
            Library.adapter.allCreatedAt.add(0, createdAt);
            Library.adapter.allEditedAt.add(0, editedAt);
            Library.adapter.allStars.add(0, stared);
            Library.adapter.allColors.add(0,colorIndex);

        } else {
            String createdAt = ColorLibrary.adapter.allCreatedAt.get(mFileIndex);
            int stared = ColorLibrary.adapter.allStars.get(mFileIndex);

            //Removing the row with original note info
            if (ColorLibrary.adapter.dataAdapter.deleteRow(filename) < 0){
                //Failed to delete original row.
                Toast.makeText(this, "Error: Failed to remove original data", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ColorLibrary.adapter.dataAdapter.insertNewRow(title, createdAt, editedAt, stared, colorIndex) < 0) {
                //Failed to save data on SQL.
                Toast.makeText(this, "Error: Failed to save data - Database corrupted", Toast.LENGTH_SHORT).show();
                return;
            }

            //Notifying the user and the list adapter.
            ColorLibrary.adapter.mCatalog.remove(mFileIndex);
            ColorLibrary.adapter.allCreatedAt.remove(mFileIndex);
            ColorLibrary.adapter.allEditedAt.remove(mFileIndex);
            ColorLibrary.adapter.allStars.remove(mFileIndex);
            ColorLibrary.adapter.allColors.remove(mFileIndex);

            ColorLibrary.adapter.mCatalog.add(0, title);
            ColorLibrary.adapter.allCreatedAt.add(0, createdAt);
            ColorLibrary.adapter.allEditedAt.add(0, editedAt);
            ColorLibrary.adapter.allStars.add(0, stared);
            ColorLibrary.adapter.allColors.add(0,colorIndex);
        }

        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        BookAdapter.updated = false;

        //Moving on.
        Intent intent = new Intent(this, ViewNote.class);
        intent.putExtra("index", 0); //When the user saves edit, this item will be the first item.
        startActivity(intent);
    }
}
