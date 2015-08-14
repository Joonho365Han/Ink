package eden.notebook.ink;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
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

    //Layout info
    private EditText title;
    private EditText content;
    private Spinner spinner;

    //File info
    String newTitle;
    private int colorIndex;
    private Uri photoUri;

    // List of existing notes (Will be used on saveFile())
    private List<String> existing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnote);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences prefs = getSharedPreferences("EdenNotebookSettings",MODE_PRIVATE);

        title = (EditText) findViewById(R.id.add_title);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, prefs.getInt("Title", 44));
        content = (EditText) findViewById(R.id.add_content);
        content.setTextSize(TypedValue.COMPLEX_UNIT_SP, prefs.getInt("Content",24));
        if (prefs.getBoolean("Serif",false)) { title.setTypeface(Typeface.SERIF);       content.setTypeface(Typeface.SERIF);      }
        else                                 { title.setTypeface(Typeface.SANS_SERIF);  content.setTypeface(Typeface.SANS_SERIF); }

        spinner = (Spinner) findViewById(R.id.color_code_spinner);
        ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(this,R.array.color_type,android.R.layout.simple_spinner_item);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(colorAdapter);
        spinner.setBackgroundResource(R.drawable.edittext_borders);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) spinner.setBackgroundResource(R.drawable.edittext_borders);
                else if (position == 8) { // User selects image.
                    startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), 0);
                    return;
                }//Stop here. Do not update spinner until user selects photo.
                else spinner.setBackgroundColor(BookAdapter.COLOR_ARRAY[position]);

                colorIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                colorIndex = 0;
            }
        });

        existing = new ArrayList<>();
        Collections.addAll(existing, getFilesDir().list());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == 0){
            if (resultCode == RESULT_OK){ //User picked a photo.

                //Save photo uri. Don't manipulate bitmap yet. That takes time.
                photoUri = data.getData();

                //Set spinner style to photo theme.
                colorIndex = 8;
                spinner.setBackgroundColor(BookAdapter.COLOR_ARRAY[8]);

            } else { //User did not pick a photo: Revert user selection.
                spinner.setSelection(colorIndex);
            }
        }
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
        int id = item.getItemId();

        if (id == R.id.action_done) {
            saveFile();
            return true;
        } else if (id == android.R.id.home){ // This is inplemented because the default parent-child activity relation start a new parent when home is pressed.
            super.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveFile(){

        newTitle = title.getText().toString().trim();

        //Make sure a file name  does not exist.
        if (newTitle.length() >= 255){
            Toast.makeText(this, R.string.long_title, Toast.LENGTH_SHORT).show();
        } else if (newTitle.length() == 0) {
            Toast.makeText(this, R.string.empty_title, Toast.LENGTH_SHORT).show();
        } else if (existing.contains(newTitle)) {
            //Test if file with same title already exists.
            Toast.makeText(this, R.string.existing_title, Toast.LENGTH_SHORT).show();
        } else if (newTitle.contains("@#$^23!^") || newTitle.contains("AG5463#$1!#$&")) {
            //Test if title has an extension string.
            Toast.makeText(this, R.string.invalid_title, Toast.LENGTH_SHORT).show();
        } else { //If none of the above apply, save the file.

            ////////////////Saving note content to internal storage/////////////////////////
            try {FileOutputStream fos;

                //Save photo image if set as header.
                if (colorIndex == 8){

                    //Retrieving bitmap size.
                    BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(getContentResolver().openInputStream(photoUri), null, options);
                    int width=options.outWidth, height=options.outHeight; //These are the bitmap dimensions.

                    //Get display dimensions.
                    Point size = new Point();
                    getWindowManager().getDefaultDisplay().getSize(size);

                    //Decode with inSampleSize and shrinking bitmap if necessary.
                    //We make the the longer side of the bitmap fits within the longer side of the display. That's all.
                        options.inJustDecodeBounds = false;
                        options.inSampleSize = Math.max(width, height) / Math.max(size.x,size.y);
                    Bitmap originalPhoto = BitmapFactory.decodeStream(getContentResolver().openInputStream(photoUri),null,options);

                    new SaveBitmapTask().execute(originalPhoto);

                    //Resizing and saving to note holder size. We do not crop the photo just in case the cropped part has only one color. We want photo headers to have dynamic images.
                    fos = openFileOutput(newTitle + "AG5463#$1!#$&", MODE_PRIVATE);
                    StackBlur.blur(Bitmap.createScaledBitmap(originalPhoto, 200, 40, true),10).compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                }

                //Newly writing text into file as a byte format.
                fos = openFileOutput(newTitle, MODE_PRIVATE);
                fos.write(content.getText().toString().getBytes());
                fos.close();
                                                                                        }
            catch (FileNotFoundException | OutOfMemoryError e) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.error)
                        .setMessage(getResources().getString(R.string.saving_file_fail)+e.toString())
                        .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create().show();
                return; }
            catch (IOException e)           {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.error)
                        .setMessage(getResources().getString(R.string.saving_file_fail)+e.toString())
                        .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create().show();
                deleteFile(newTitle);
                if (colorIndex == 8) {
                    deleteFile(newTitle+"@#$^23!^");
                    deleteFile(newTitle+"AG5463#$1!#$&");
                }
                return; }
            catch (Exception e){
                new AlertDialog.Builder(this)
                        .setTitle(R.string.error)
                        .setMessage(getResources().getString(R.string.saving_file_fail)+e.toString())
                        .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create().show();
                return;
            }

            //Saving details in database.
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
            String createdAt = //Put date info together.
                    String.valueOf(c.get(Calendar.YEAR))+"/"+
                            month+"/"+
                                day+" "+
                                    hour+":"+
                                        minute+":"+
                                            second+
                                                ampm+
                                                    c.getTimeZone().getDisplayName(false, TimeZone.SHORT);

            // Inserting note detail info on database.
            // This loop makes sure the database is updated, but also can be dangerous.
            while (new NoteDatabaseAdapter(this).insertNewRow(newTitle,createdAt,createdAt,0,colorIndex) < 0) {}

            BookAdapter.addNote(newTitle,createdAt,createdAt,0,colorIndex);
            Library.mAdapter.notifyItemChanged(colorIndex + 4);
            Library.updated = false;

            //Moving on.
            Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
            super.onBackPressed();

        }
    }

    class SaveBitmapTask extends AsyncTask<Bitmap, Void, Void> {

        @Override
        protected Void doInBackground(Bitmap... bitmaps) {
            try {
                FileOutputStream fos = openFileOutput(newTitle+"@#$^23!^", MODE_PRIVATE);
                bitmaps[0].compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            } catch (Exception e) {
                Toast.makeText(AddNote.this, "SaveBitmapTask: "+e.toString(), Toast.LENGTH_SHORT).show();
            }
            return null;
        }
    }
}