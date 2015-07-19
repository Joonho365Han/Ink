package eden.notebook.ink;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.support.v7.widget.SearchView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.FileInputStream;
import java.util.List;

public class Library extends ActionBarActivity {

    static BookAdapter adapter;
    static boolean locked = true;
    private SearchRecentSuggestions suggestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_library);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.view_recycler_book);
        adapter = new BookAdapter(this);
        adapter.adapterType = 1;
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        suggestions = new SearchRecentSuggestions(this,NoteSearchSuggestionProvider.AUTHORITY, NoteSearchSuggestionProvider.MODE);

        //If password protected, query for password.
        if (getSharedPreferences("EdenNotebookSettings", MODE_PRIVATE).getBoolean("Encryption",false) && locked) { unlock(); }
        else {
            if (locked) backupData(); //This means app will back up data only when it is opened once initially despite activity being recreated.
            locked = false;
        }
    }

    @Override
    protected void onStart(){
        super.onStart();

        //Date is reset regularly because date could change while the app is open.
        adapter.getDate();

        if (!BookAdapter.updated) {
            adapter.notifyDataSetChanged();
            BookAdapter.updated = true;
        }
    }

    @Override
    protected void onNewIntent(Intent intent){
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            adapter.initializeSearchResult(query);
            adapter.notifyDataSetChanged();
            suggestions.saveRecentQuery(query, null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_library, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //Must use the compat object library because the app uses support library.
        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search),new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                adapter.wipeDataArray();
                adapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                adapter.initializeDataArray();
                adapter.notifyDataSetChanged();
                suggestions.clearHistory();
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (locked) //Don't do anything if app is still locked.
            return true;

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, Settings.class));
            return true;
        } else if (id == R.id.action_add){
            startActivity(new Intent(this, AddNote.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void unlock(){
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_password, null);
        final EditText edittext = (EditText) layout.findViewById(R.id.editext);
            new AlertDialog.Builder(Library.this)
                    .setView(layout)
                    .setTitle(R.string.settings_password_decrypt)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (edittext.getText().toString()
                                    .equals(getSharedPreferences("EdenNotebookSettings", MODE_PRIVATE).getString("Password", null))) {
                                //Password Correct.
                                locked = false;
                                backupData();
                            } else {
                                //Password was wrong.
                                unlock();
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

        if (getSharedPreferences("EdenNotebookSettings", MODE_PRIVATE).getBoolean("Backup",false)) {
            //Connect to cloud.
            Parse.initialize(this, "8rRg8GndjElDiy1UrviRT650VNE0yKN8BcF1AaFH", "6Zy8VRFP2A017iol0AkJyCJRkOM7TGDWatl7ptIE");
            final String deviceID = android.provider.Settings.Secure.getString(getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);

            //Remove existing cloud data.
            ParseQuery<ParseObject> query =ParseQuery.getQuery("ID" + deviceID);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (e == null)
                        try {
                            ParseObject.deleteAll(parseObjects);
                        } catch (ParseException e1) { /*Do Nothing*/ }

                    //Backing up AFTER deleting.
                    for (int i = 0; i < adapter.mCatalog.size(); i++) {
                        ParseObject note = new ParseObject("ID" + deviceID);
                        String title = adapter.mCatalog.get(i);
                        String createdAt = adapter.allCreatedAt.get(i);
                        String editedAt = adapter.allEditedAt.get(i);
                        int stared = adapter.allStars.get(i);
                        int color = adapter.allColors.get(i);
                        note.put("title", title);
                        note.put("DATE_CREATED", createdAt);
                        note.put("DATE_EDITED", editedAt);
                        note.put("Stared", stared);
                        note.put("Color", color);
                        try {
                            //Obtaining byte string info from filename.
                            FileInputStream fis = openFileInput(title);
                            byte[] data = new byte[fis.available()];
                            if (fis.read(data) != 0) // Sometimes if the saved string is nothing (""), then the read()
                                // will constantly return 0 and fall into the constant while loop.
                                // Must make sure there is something to read() before proceeding.
                                while (fis.read(data) != -1) { /*This loop constantly extracts byte that will
                                                 eventually be converted to a string byte by byte.*/
                                }

                            note.put("content", new String(data));
                            fis.close();
                        } catch (Exception exp) {
                            Toast.makeText(Library.this, "Failed to back up some files", Toast.LENGTH_SHORT).show();
                        }
                        note.saveEventually();
                    }
                }
            });
        }
    }
}
