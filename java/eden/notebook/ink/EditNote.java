package eden.notebook.ink;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class EditNote extends ActionBarActivity {

    private EditText title;
    private EditText content;
    private int mFileIndex;
    private String filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editnote);
        title = (EditText) findViewById(R.id.edit_title);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, getSharedPreferences("Settings", Context.MODE_PRIVATE).getInt("Title",46));
        content = (EditText) findViewById(R.id.edit_content);
        content.setTextSize(TypedValue.COMPLEX_UNIT_SP, getSharedPreferences("Settings", Context.MODE_PRIVATE).getInt("Content",25));

        //Obtaining file id info.
        mFileIndex = getIntent().getIntExtra("index", 0);
        filename = BookAdapter.mCatalog.get(mFileIndex); //The original file name.

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

    protected void saveFile(){

        String title = this.title.getText().toString();

        //Make sure a file name exists.
        if (title.equals("")) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (BookAdapter.mCatalog.contains(title) && !title.equals(filename)){ //Test if file with same NEW title already exists.
            Toast.makeText(this, "Title is already in use", Toast.LENGTH_SHORT).show();
            return;
        }

        String content = this.content.getText().toString();

        try {
            //remove existing file
             deleteFile(filename);

             //Saving text into file as a byte format.
             FileOutputStream fos = openFileOutput(title, MODE_PRIVATE);
             fos.write(content.getBytes());
             fos.close();                                              }
        catch (FileNotFoundException e) { /*Do nothing.*/ }
        catch (IOException e)           { Toast.makeText(this, "Sorry. Storage is full", Toast.LENGTH_SHORT).show(); }

        //Notifying the user and the list adapter.
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        BookAdapter.mCatalog.remove(mFileIndex);
        BookAdapter.mCatalog.add(0, title);

        //Moving on.
        Intent intent = new Intent(this, ViewNote.class);
        intent.putExtra("index", 0); //When the user saves edit, this item will be the first item.
        startActivity(intent);
    }
}
