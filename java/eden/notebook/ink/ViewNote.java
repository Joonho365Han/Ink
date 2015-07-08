package eden.notebook.ink;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;

public class ViewNote extends ActionBarActivity {

    private TextView title;
    private int mFileIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewnote);

        title = (TextView) findViewById(R.id.view_title);
        TextView content = (TextView) findViewById(R.id.view_content);

        mFileIndex = getIntent().getIntExtra("index",0);
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
        getMenuInflater().inflate(R.menu.menu_viewnote, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_edit) {
            Intent intent = new Intent(this, EditNote.class);
            intent.putExtra( "filename",  title.getText().toString() );
            startActivity(intent);
            return true;
        } else if (id == R.id.action_settings){
            startActivity(new Intent(this, Settings.class));
            return true;
        } else if (id == R.id.action_delete){

            try {
                //Deleting file.
                deleteFile(title.getText().toString());
                ScrollAdapter.mCatalog.remove(mFileIndex);
                //ScrollAdapter.notify
                Toast.makeText(this, "File Deleted.", Toast.LENGTH_SHORT).show();

                //Get out of here.
                startActivity(new Intent(this, Library.class));
            } catch (Exception e){
                Toast.makeText(this, "Cannot delete: File does not exist.", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
