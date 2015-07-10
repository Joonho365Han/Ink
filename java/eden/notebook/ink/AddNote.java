package eden.notebook.ink;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AddNote extends ActionBarActivity {

    private EditText title;
    private EditText content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnote);

        title = (EditText) findViewById(R.id.add_title);
        content = (EditText) findViewById(R.id.add_content);
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

    private void saveFile(){

        String title = this.title.getText().toString();

        //Make sure a file name exists.
        if (title.equals("")) {
            Toast.makeText(this, "Please enter a title.", Toast.LENGTH_SHORT).show();
            return;
        } else if (BookAdapter.mCatalog.contains(title)){ //Test if file with same title already exists.
            Toast.makeText(this, "Oops! Already existing title.", Toast.LENGTH_SHORT).show();
            return;
        }

        //Writing text into file as a byte format.
        try { FileOutputStream fos = openFileOutput(title, MODE_PRIVATE);
              fos.write(content.getText().toString().getBytes());
              fos.close();                                                  }
        catch (FileNotFoundException e) { /*Do nothing.*/ }
        catch (IOException e)           { Toast.makeText(this, "Sorry. Storage is full.", Toast.LENGTH_SHORT).show(); }

        //Notifying the user and the list adapter.
        Toast.makeText(this, "Note Saved.", Toast.LENGTH_SHORT).show();
        BookAdapter.mCatalog.add(0, title);

        //Moving on.
        startActivity(new Intent(this, Library.class));

    }

}
