package eden.notebook.ink;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editnote);

        title = (EditText) findViewById(R.id.add_title);
        content = (EditText) findViewById(R.id.add_content);

        mFileIndex = getIntent().getFlags();
        String filename = ScrollAdapter.mCatalog.get(mFileIndex);
        title.setText(filename);

        try {
            //Obtaining byte string info from filename.
            FileInputStream fis = openFileInput(filename);
            byte[] data = new byte[fis.available()];
            String collected = null;

            //Converting byte text into string.
            while (fis.read(data) != -1){
                collected = new String(data);
            }
            content.setText(collected);

            fis.close();
        } catch (Exception e){
            Toast.makeText(this, "Error: File does not exist.", Toast.LENGTH_SHORT).show();
        }
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

    private void saveFile(){

        String title = this.title.getText().toString();

        //Make sure a file name exists.
        if (title.equals("")) {
            Toast.makeText(this, "Please enter a title.", Toast.LENGTH_SHORT).show();
            return;
        }

        //Test if file with new title already exists. Ignore if same title.

        try {
            //Writing text into file as a byte format.
            FileOutputStream fos = openFileOutput(title, MODE_PRIVATE);
            fos.write(content.getText().toString().getBytes());
            fos.close();
            Toast.makeText(this, "Note Saved.", Toast.LENGTH_SHORT).show();

            //Moving on.
            Intent intent = new Intent(this, ViewNote.class);
            intent.addFlags(mFileIndex);
            startActivity(intent);
        } catch (FileNotFoundException e) {
            //Do nothing.
        } catch (IOException e) {
            Toast.makeText(this, "Sorry. Storage is full.", Toast.LENGTH_SHORT).show();
        }

    }

}
