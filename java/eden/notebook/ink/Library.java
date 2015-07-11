package eden.notebook.ink;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.FileInputStream;
import java.util.List;

public class Library extends ActionBarActivity {

    private RecyclerView mRecyclerView;
    private static boolean locked = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_library);

        mRecyclerView = (RecyclerView) findViewById(R.id.view_recycler_book);
        mRecyclerView.setAdapter(new BookAdapter(this));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //If password protected, query for password.
        if (getSharedPreferences("Settings", MODE_PRIVATE).getBoolean("Encryption",false) && locked) { unlock(); }
    }

    @Override
    protected void onStart(){
        super.onStart();
        mRecyclerView.getAdapter().notifyDataSetChanged();
        //This is a very expensive operation, so exchange it with something else if possible.
        //However, this definitely is the quickest in updating the views.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_library, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, Settings.class));
            return true;
        } else if (id == R.id.action_search){
            return true;
        } else if (id == R.id.action_add){
            startActivity(new Intent(this, AddNote.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void unlock(){LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_password, null);
        final EditText edittext = (EditText) layout.findViewById(R.id.editext);
            new AlertDialog.Builder(Library.this)
                    .setView(layout)
                    .setTitle(R.string.settings_password_decrypt)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (edittext.getText().toString()
                                    .equals(getSharedPreferences("Settings", MODE_PRIVATE).getString("Password", null))) {
                                //Password Correct.
                                locked = false;
                                backupData();
                            } else {
                                //Password was wrong.
                                new AlertDialog.Builder(Library.this)
                                        .setMessage(R.string.wrong_password)
                                        .setTitle(R.string.error)
                                        .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                unlock();
                                            }
                                        }).setCancelable(false).create().show();
                            }
                        }
                    }).setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).setCancelable(false).create().show();
    }

    protected void backupData(){
        if (getSharedPreferences("Settings", MODE_PRIVATE).getBoolean("Backup",false)) {

            //Connect to cloud.
            Parse.enableLocalDatastore(this);
            Parse.initialize(this, "8rRg8GndjElDiy1UrviRT650VNE0yKN8BcF1AaFH", "6Zy8VRFP2A017iol0AkJyCJRkOM7TGDWatl7ptIE");
            String deviceID = android.provider.Settings.Secure.getString(getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);

            //Remove existing cloud data.
            ParseQuery<ParseObject> query =ParseQuery.getQuery("ID" + deviceID);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (e == null)
                        try {
                            ParseObject.deleteAll(parseObjects);
                        } catch (ParseException e1) { /*Do Nothing*/ }
                }
            });

            //Backing up.
            for (int i = 0; i < BookAdapter.mCatalog.size(); i++) {
                ParseObject note = new ParseObject("ID" + deviceID);
                String title = BookAdapter.mCatalog.get(i);
                note.put("title", title);
                try {
                    //Obtaining byte string info from filename.
                    FileInputStream fis = openFileInput(title);
                    byte[] data = new byte[fis.available()];
                    if (fis.read(data) != 0) // Sometimes if the saved string is nothing (""), then the read()
                        // will constantly return 0 and fall into the constant while loop.
                        // Must make sure there is something to read() before proceeding.
                        while (fis.read(data) != -1) { /*This loop constantly extracts byte that will
                                                 eventually be converted to a string byte by byte.*/ }

                    note.put("content", new String(data));
                    fis.close();                                                }
                catch (Exception e){ Toast.makeText(this, "Failed to backup some files", Toast.LENGTH_SHORT).show(); }
                note.saveEventually();
            }
        }
    }
}
