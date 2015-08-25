package eden.notebook.ink;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

public class EditNote extends ActionBarActivity {

    //Layout info.
    private EditText title;
    private EditText content;
    private Spinner spinner;

    //File info.
    String newTitle;
    private String filename;
    private String createdAt;
    private int starred;
    private int colorIndex;
    private int pastColorIndex;
    private Uri photoUri;

    // List of existing notes (Will be used on saveFile())
    private List<String> existing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnote);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences prefs = getSharedPreferences("EdenNotebookSettings", MODE_PRIVATE);

        //Obtaining file id info.
        int mFileIndex = getIntent().getIntExtra("index", 0);
        filename = BookAdapter.mCatalog.get(mFileIndex); //The original file name.
        createdAt = BookAdapter.allCreatedAt.get(mFileIndex);
        starred = BookAdapter.allStars.get(mFileIndex);
        colorIndex = BookAdapter.allColors.get(mFileIndex);

        //Stylizing note text.
        title = (EditText) findViewById(R.id.add_title);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, prefs.getInt("Title", 44));
        content = (EditText) findViewById(R.id.add_content);
        content.setTextSize(TypedValue.COMPLEX_UNIT_SP, prefs.getInt("Content", 24));
        if (prefs.getBoolean("Serif",false)) { title.setTypeface(Typeface.SERIF);       content.setTypeface(Typeface.SERIF);      }
        else                                 { title.setTypeface(Typeface.SANS_SERIF);  content.setTypeface(Typeface.SANS_SERIF); }

        //Setting title text.
        title.setText(filename);

        //Setting content text and photo.
        try {
            //Obtaining byte string info from filename.
            FileInputStream fis = openFileInput(filename);
            byte[]         data = new byte[fis.available()];
            if (fis.read(data) != 0)  while (fis.read(data) != -1) {} // Check the NoteFragment class for comments about this line.

            content.setText(new String(data));
            fis.close();
        }
        catch (Exception e){ Toast.makeText(this, e.toString() , Toast.LENGTH_SHORT).show(); }

        //Initializing color spinner.
        spinner = (Spinner) findViewById(R.id.color_code_spinner);
        ArrayList<String> categories = new ArrayList<>();
        Resources res = getResources();
        categories.add(prefs.getString("Cloud",res.getString(R.string.cloud_header)));
        categories.add(prefs.getString("Pink",res.getString(R.string.pink)));
        categories.add(prefs.getString("Orange",res.getString(R.string.orange)));
        categories.add(prefs.getString("Yellow",res.getString(R.string.yellow)));
        categories.add(prefs.getString("Green",res.getString(R.string.green)));
        categories.add(prefs.getString("Blue",res.getString(R.string.blue)));
        categories.add(prefs.getString("Indigo",res.getString(R.string.indigo)));
        categories.add(prefs.getString("Purple",res.getString(R.string.purple)));
        categories.add(prefs.getString("Image",res.getString(R.string.select_from_album)));

        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,categories);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(colorAdapter);

        spinner.setSelection(colorIndex);
        if (colorIndex == 0) spinner.setBackgroundResource(R.drawable.edittext_borders);
        else                 spinner.setBackgroundColor(BookAdapter.COLOR_ARRAY[colorIndex]);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            boolean firstTime = true;  // This variable prevents the startActivity for Result if the header is photo header.

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (firstTime) {
                    firstTime = false;
                    return;
                }

                if      (position == 0) spinner.setBackgroundResource(R.drawable.edittext_borders);
                else if (position == 8) { //User selects image.
                                        startActivityForResult( new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI) , 0 );
                                        return;} //We do not modify the colorIndex or the spinner while we are selecting a photo.
                else                    spinner.setBackgroundColor(BookAdapter.COLOR_ARRAY[position]);

                colorIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { colorIndex = 0; }
        });

        pastColorIndex = colorIndex;
        photoUri = null;

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

            } else { //User did not pick a photo: Then revert user selection
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
        // noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.action_done) {
            saveFile();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveFile() {

        //The MAYBE new file name.
        newTitle = title.getText().toString().trim();

        //Make sure the file name is valid.
        if (newTitle.length() > 255){
            Toast.makeText(this, R.string.long_title, Toast.LENGTH_SHORT).show();
        } else if (newTitle.length() == 0) {
            Toast.makeText(this, R.string.empty_title, Toast.LENGTH_SHORT).show();
        } else if (existing.contains(newTitle) && !newTitle.equals(filename)){
            //Test if file with same NEW title already exists.
            Toast.makeText(this, R.string.existing_title, Toast.LENGTH_SHORT).show();
        } else if (newTitle.contains("@#$^23!^") || newTitle.contains("AG5463#$1!#$&")) {
            //Test if title has an extension string.
            Toast.makeText(this, R.string.invalid_title, Toast.LENGTH_SHORT).show();
        } else { //If none of the above apply, save the file.

            //Saving big note info
            try {FileOutputStream fos;

                //Overwriting or newly writing text into file as a byte format.
                fos = openFileOutput(newTitle, MODE_PRIVATE);
                fos.write(content.getText().toString().getBytes());
                fos.close();

                //Save photo image if set as header.
                if (colorIndex == 8){

                    //Creating bitmaps.
                    Bitmap originalPhoto;
                    if (photoUri == null){
                        //If photo has not changed, get original photo.
                        originalPhoto = BitmapFactory.decodeFile(new File(getFilesDir(), filename + "@#$^23!^").getAbsolutePath());
                        if (originalPhoto == null){ // origianlPhoto is null if previous save hasn't been finished.
                            Toast.makeText(this, R.string.previous_save, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    else{
                        //If photo has changed, get new photo and shrink its size if necessary.
                        BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(getContentResolver().openInputStream(photoUri), null, options);
                        int width=options.outWidth, height=options.outHeight;

                        //Get display dimensions.
                        Point size = new Point();
                        getWindowManager().getDefaultDisplay().getSize(size);

                        //decode with inSampleSize
                        //We make the the longer side of the bitmap fits within the longer side of the display. That's all.
                            options.inJustDecodeBounds = false;
                            options.inSampleSize = Math.max(width, height) / Math.max(size.x,size.y);
                        originalPhoto = BitmapFactory.decodeStream(getContentResolver().openInputStream(photoUri),null,options);
                    }

                    new SaveBitmapTask().execute(originalPhoto);

                    //Resizing and saving to note holder size. We do not crop the photo just in case the cropped part has only one color. We want photo headers to have dynamic images.
                    fos = openFileOutput(newTitle + "AG5463#$1!#$&", MODE_PRIVATE);
                    StackBlur.blur(Bitmap.createScaledBitmap(originalPhoto, 200, 40, true),10).compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                }                                                                                        }
            catch (Exception e){
                new AlertDialog.Builder(this)
                        .setTitle(R.string.error)
                        .setMessage(getResources().getString(R.string.saving_file_fail)+e.toString())
                        .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create().show();
                //Deleting file with NEW title after failing to save prevents the new title from being taken and unable to use.
                if (!filename.equals(newTitle)) {
                    deleteFile(newTitle);
                    if (colorIndex == 8) {
                        new DeleteBitmapTask().execute(newTitle);
                        deleteFile(newTitle+"AG5463#$1!#$&");
                    }
                }
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

            //These loops make sure the note info is updated, although it could fall into a dangerous infinite loop.
            while(new NoteDatabaseAdapter(this).deleteRow(filename) < 0){}
            while(new NoteDatabaseAdapter(this).insertNewRow(newTitle, createdAt, editedAt, starred, colorIndex) < 0){}

            BookAdapter.deleteNote(filename);
            BookAdapter.addNote(newTitle,createdAt,editedAt,starred,colorIndex);
            if (pastColorIndex != colorIndex){ // If category has changed, notify the drawer.
                Library.mAdapter.notifyItemChanged(pastColorIndex + 4);
                Library.mAdapter.notifyItemChanged(colorIndex + 4);
            }

            //Remove file with old title only if it's different AT THE END. This prevents loosing content when failing to save new content data.
            if (!filename.equals(newTitle)) {
                deleteFile(filename);
                if (pastColorIndex == 8) {
                    new DeleteBitmapTask().execute(filename);
                    deleteFile(filename+"AG5463#$1!#$&");
                }
            } else if (pastColorIndex == 8 && colorIndex != 8){
                new DeleteBitmapTask().execute(filename);
                deleteFile(filename+"AG5463#$1!#$&");
            }

            Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
            Library.updated = false;

            //Moving on.
            Intent intent = new Intent(this, ViewNote.class);
            intent.putExtra("index", 0); //When the user saves edit, this item will be the first item.
            startActivity(intent);

        }
    }

    class SaveBitmapTask extends AsyncTask<Bitmap, Void, Void>{

        @Override
        protected Void doInBackground(Bitmap... bitmaps) {
            try {
                FileOutputStream fos = openFileOutput(newTitle+"@#$^23!^", MODE_PRIVATE);
                bitmaps[0].compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            } catch (Exception e) {
                Toast.makeText(EditNote.this, "SaveBitmapTask: " + e.toString(), Toast.LENGTH_SHORT).show();
            }
            return null;
        }
    }

    class DeleteBitmapTask extends AsyncTask<String, Void, Void>{
        // We use a background task to delete large bitmaps because if the deletion was done on main thread before the past note save was done,
        // the deletion process will not work and the past saving process will save and create the image file within the storage.
        // This takes up storage.

        @Override
        protected Void doInBackground(String... strings) {
            deleteFile(strings[0]+"@#$^23!^");
            return null;
        }
    }
}