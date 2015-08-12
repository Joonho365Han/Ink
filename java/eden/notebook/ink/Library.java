package eden.notebook.ink;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.support.v7.widget.SearchView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

//THIS IS THE COPYRIGHT LICENSE FOR THE FloatingActionButton VIEW BEING USED IN THIS ACTIVITY.

/*The MIT License (MIT)

        Copyright (c) 2014 Oleksandr Melnykov

        Permission is hereby granted, free of charge, to any person obtaining a copy
        of this software and associated documentation files (the "Software"), to deal
        in the Software without restriction, including without limitation the rights
        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
        copies of the Software, and to permit persons to whom the Software is
        furnished to do so, subject to the following conditions:

        The above copyright notice and this permission notice shall be included in all
        copies or substantial portions of the Software.

        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
        SOFTWARE.*/

public class Library extends ActionBarActivity {

    // Adapter status variables.
    static boolean updated; //Must be static.
    static boolean restored; //Must be static.

    // Mode of the library.
    private boolean onSearch;
    private boolean onColorType;
    private boolean onFavorites;

    static boolean locked = true; //Must be static. These are initialized here because these are related to class instance, not status.
    static boolean backingUp = false; // Must be static. These are initialized here because these are related to class instance, not status.

    // Layout views.
    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    static BookAdapter adapter;  //Must be static.
    private RecyclerView mNavDrawer;
    static DrawerAdapter mAdapter; // This adapter is static so many editing actions update the drawer.
    static FloatingActionButton deleteButton; //Must be static so adapter can access it and show the delete button.

    private String query;
    private int colorSearch;
    private SearchRecentSuggestions suggestions;
    private SharedPreferences prefs;
    private int columns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_library);
        prefs = getSharedPreferences("EdenNotebookSettings",MODE_PRIVATE);

        updated = true;
        restored = false;

        onSearch = false;
        onColorType = false;
        onFavorites = false;

        // Toolbar initiation.
        toolbar = (Toolbar) findViewById(R.id.library_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.library);

        //Note list initiation.
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.view_recycler_book);
        columns = prefs.getInt("Columns",1);
        adapter = new BookAdapter(this);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, columns));

        // Drawer initiation.
        mNavDrawer = (RecyclerView) findViewById(R.id.left_drawer);
        mAdapter = new DrawerAdapter(this);
        mNavDrawer.setAdapter(mAdapter);
        mNavDrawer.setLayoutManager(new LinearLayoutManager(this));

        //Drawer layout initiation.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Delete button initiation
        deleteButton = (FloatingActionButton) findViewById(R.id.delete_button);
        deleteButton.hide(false);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (adapter.allDeleteList.isEmpty()) {
                    //If there is nothing to delete, notify the user.
                    Toast.makeText(Library.this, R.string.ask_delete_empty, Toast.LENGTH_SHORT).show();
                } else {
                    //If there is something to delete, execute delete code.
                    if (prefs.getBoolean("Delete", true)) {
                        //Show asking dialogue.
                        AlertDialog.Builder builder = new AlertDialog.Builder(Library.this)
                                .setTitle(R.string.caution)
                                .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) { groupDelete(); }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                        if (adapter.allDeleteList.size() == 1)
                            builder.setMessage(R.string.ask_delete);
                        else
                            builder.setMessage(R.string.ask_delete_multiple);
                        builder.create().show();
                    } else {
                        groupDelete();
                    }
                }
            }
        });

        // Search history initiation.
        suggestions = new SearchRecentSuggestions(this,NoteSearchSuggestionProvider.AUTHORITY, NoteSearchSuggestionProvider.MODE);

        //If password protected, query for password.
        if (prefs.getBoolean("Encryption",false) && locked) { unlock(); }
        else {
            if (locked) {
                suggestions.clearHistory();
                backupData(); //This means app will back up data only when it is opened once initially despite activity being recreated.
            }
            locked = false;
        }
    }

    @Override
    protected void onStart(){
        super.onStart();

        //Date is reset regularly because date could change while the app is open.
        adapter.getDate();

        if (restored){ // Notes have been restored. Activity must look like brand new.
            toolbar.setTitle(R.string.library);
            onColorType = false;
            onFavorites = false;
            if (onSearch)
                super.onBackPressed(); //Collapsing the searchview will initialize data set for us.
            else {
                if (adapter.groupDelete) deleteButton.hide(false); // Or we must do it manually
                adapter.initializeDataArray();
                adapter.notifyDataSetChanged();
            }
            mAdapter.notifyDataSetChanged();
            restored = false;
        } else if (!updated) {
            if (adapter.groupDelete) deleteButton.hide(false);
            if (onSearch)
                adapter.initializeSearchResult(query);
            else if (onColorType)
                adapter.initializeColorArray(colorSearch);
            else if (onFavorites)
                adapter.initializeFavorites();
            adapter.notifyDataSetChanged();
            updated = true;
        }
    }

    // When search suggestion is clicked, this method is called.
    // Thus, override this method, even if the QueryTextChangeListener does searching most of the time.
    @Override
    protected void onNewIntent(Intent intent){
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String newQuery = intent.getStringExtra(SearchManager.QUERY);
            if (!newQuery.equals(query)){
                // This means the search suggestion was selected without editing the query text.
                // As a result, this code is executed only when search suggestions are selected.
                if (adapter.groupDelete) deleteButton.hide();
                query = newQuery;
                adapter.initializeSearchResult(query);
                adapter.notifyDataSetChanged();
                suggestions.saveRecentQuery(query, null);
                if (mDrawerLayout.isDrawerOpen(mNavDrawer)) mDrawerLayout.closeDrawer(mNavDrawer);
            }
        } else if (!intent.hasCategory(Intent.CATEGORY_LAUNCHER)){
            // Don't do anything if this activity was already opened and it's opened again by the launcher.
            // Only execute if this is not a launcher intent.
            mDrawerLayout.closeDrawer(mNavDrawer);
            onColorType = intent.hasExtra("Category");
            onFavorites = intent.hasExtra("Favorites");

            colorSearch = intent.getIntExtra("Category", intent.getIntExtra("Favorites", -2)); // Colorsearch will also serve as the index of title.
            toolbar.setTitle(DrawerAdapter.ACTIVITY_TITLE_RES_ID[colorSearch + 2]);

            if (onSearch){
                super.onBackPressed(); // Collapses search view. The CollapseListener will initialize the adapter for us.
            } else { // We cannot initialize by "Collapsing" the searchview if it's not open. Must initialize manually.
                if (adapter.groupDelete) deleteButton.hide();
                if (onColorType)      adapter.initializeColorArray(colorSearch);
                else if (onFavorites) adapter.initializeFavorites();
                else                  adapter.initializeDataArray();
                adapter.notifyDataSetChanged();
            } //At the end of this if else statement, onSearch is false for sure.
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
        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                query = null; //Making sure it's initially null.
                if (adapter.groupDelete) deleteButton.hide();
                adapter.wipeDataArray();
                adapter.notifyDataSetChanged();
                onSearch = true;
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (adapter.groupDelete) deleteButton.hide();
                if (onColorType) adapter.initializeColorArray(colorSearch);
                else if (onFavorites) adapter.initializeFavorites();
                else adapter.initializeDataArray();
                adapter.notifyDataSetChanged();
                onSearch = false;
                if (mDrawerLayout.isDrawerOpen(mNavDrawer)) mDrawerLayout.closeDrawer(mNavDrawer);
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                suggestions.saveRecentQuery(query, null);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (onSearch) { //Only query search when it's in search mode.
                    if (adapter.groupDelete) deleteButton.hide();
                    query = s;
                    adapter.initializeSearchResult(query);
                    adapter.notifyDataSetChanged();
                    if (mDrawerLayout.isDrawerOpen(mNavDrawer)) mDrawerLayout.closeDrawer(mNavDrawer);
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (locked) //Don't do anything if app is still locked.
            return true;

        if (mDrawerLayout.isDrawerOpen(mNavDrawer)) mDrawerLayout.closeDrawer(mNavDrawer);

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, Settings.class));
            if (adapter.groupDelete){
                adapter.groupDelete = false;
                for (String title : adapter.allDeleteList) adapter.notifyItemChanged(adapter.mCatalog.indexOf(title) + columns);
                adapter.allDeleteList = new ArrayList<>();
                deleteButton.hide();
            }
            return true;
        } else if (id == R.id.action_add){
            startActivity(new Intent(this, AddNote.class));
            if (adapter.groupDelete){
                adapter.groupDelete = false;
                for (String title : adapter.allDeleteList) adapter.notifyItemChanged(adapter.mCatalog.indexOf(title) + columns);
                adapter.allDeleteList = new ArrayList<>();
                deleteButton.hide();
            }
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        if (mDrawerLayout.isDrawerOpen(mNavDrawer))
            mDrawerLayout.closeDrawer(mNavDrawer);
        else if (adapter.groupDelete){ //Cancels the group delete mode.
            adapter.groupDelete = false;
            for (String title : adapter.allDeleteList) adapter.notifyItemChanged(adapter.mCatalog.indexOf(title) + columns);
            adapter.allDeleteList = new ArrayList<>();
            deleteButton.hide();
        } else if (onSearch) //Cancels search mode.
            super.onBackPressed();
        else if (onColorType || onFavorites){ //Cancels color search mode or the favorites mode.
            toolbar.setTitle(R.string.library);
            adapter.initializeDataArray();
            adapter.notifyDataSetChanged();
            onColorType = false;
            onFavorites = false;
        } else //Does the default action.
            super.onBackPressed();
    }

    private void unlock(){
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_password, null);
        final EditText edittext = (EditText) layout.findViewById(R.id.editext);
            new AlertDialog.Builder(Library.this)
                    .setView(layout)
                    .setTitle(R.string.settings_password_decrypt)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (edittext.getText().toString().equals(prefs.getString("Password", null))) {
                                //Password Correct.
                                locked = false;
                                suggestions.clearHistory();
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

    private void groupDelete(){

        for (String title : adapter.allDeleteList) {

            //Delete detail info from database.
            while (new NoteDatabaseAdapter(this).deleteRow(title) < 0) {}

            int noteIndex = adapter.mCatalog.indexOf(title);

            //Finally delete the actual saved content of the note.
            deleteFile(title);
            if (adapter.allColors.get(noteIndex) == 8) {
                deleteFile(title+"@#$^23!^");
                deleteFile(title+"AG5463#$1!#$&");
            }

            //Remove note from recyclerview. Then remove note info from adapter.
            adapter.notifyItemRemoved(noteIndex + columns);
            adapter.deleteNote(title);
        }
        //Empty delete candidates.
        adapter.groupDelete = false;
        adapter.allDeleteList = new ArrayList<>();

        //Hide the button after all is well.
        deleteButton.hide();

        // Update the drawer.
        mAdapter.notifyDataSetChanged();

        //Notify the user.
        Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
    }

    protected void backupData(){ //This runs on background.

        if (prefs.getBoolean("Backup",false)) {

            backingUp = true;

            //Connect to cloud.
            Parse.initialize(this, "8rRg8GndjElDiy1UrviRT650VNE0yKN8BcF1AaFH", "6Zy8VRFP2A017iol0AkJyCJRkOM7TGDWatl7ptIE");
            final String deviceID = "ID" + android.provider.Settings.Secure.getString(getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);

            //Remove existing cloud data.
            ParseQuery<ParseObject> query =ParseQuery.getQuery(deviceID);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (e == null){
                        try {
                            for (ParseObject oldNote : parseObjects)
                                oldNote.deleteEventually();
                        } catch (Exception e1) { Toast.makeText(Library.this, getResources().getString(R.string.backup_error)+e1.toString(), Toast.LENGTH_SHORT).show(); return; }
                        //Quit if failed to erase note because nothing is worse than DUPLICATE NOTES.

                        //Backing up AFTER finished deleting original backup: It prevents deleting the notes in cloud that are currently being backed up.
                        for (int i = 0; i < adapter.mCatalog.size(); i++) {

                            ParseObject note = new ParseObject(deviceID);
                            String title = adapter.mCatalog.get(i);
                            String createdAt = adapter.allCreatedAt.get(i);
                            String editedAt = adapter.allEditedAt.get(i);
                            int starred = adapter.allStars.get(i);
                            int color = adapter.allColors.get(i);
                            if (color == 8) color = 0; //Backup does not save photo headers and returns note to default header.

                            note.put("title", title);
                            note.put("DATE_CREATED", createdAt);
                            note.put("DATE_EDITED", editedAt);
                            note.put("Starred", starred);
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
                                Toast.makeText(Library.this, R.string.backup_fail + exp.toString(), Toast.LENGTH_SHORT).show();
                            }
                            note.saveEventually();
                        }
                    } else { // Restore failed. Could not connect to cloud.
                        new AlertDialog.Builder(Library.this)
                                .setTitle(R.string.error)
                                .setMessage(getResources().getString(R.string.parse_failed)+e.toString())
                                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {}
                                }).create().show();
                    }

                    backingUp = false;
                }
            });
        }
    }

    /*

package eden.notebook.ink;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.support.v7.widget.SearchView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

//THIS IS THE COPYRIGHT LICENSE FOR THE FloatingActionButton VIEW BEING USED IN THIS ACTIVITY.

/*The MIT License (MIT)

        Copyright (c) 2014 Oleksandr Melnykov

        Permission is hereby granted, free of charge, to any person obtaining a copy
        of this software and associated documentation files (the "Software"), to deal
        in the Software without restriction, including without limitation the rights
        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
        copies of the Software, and to permit persons to whom the Software is
        furnished to do so, subject to the following conditions:

        The above copyright notice and this permission notice shall be included in all
        copies or substantial portions of the Software.

        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
        SOFTWARE.

    public class Library extends ActionBarActivity {

        // Adapter status variables.
        static boolean updated; //Must be static.
        static boolean restored; //Must be static.

        // Mode of the library.
        private boolean onSearch;
        private boolean onColorType;
        private boolean onFavorites;

        static boolean locked = true; //Must be static. These are initialized here because these are related to class instance, not status.
        static boolean backingUp = false; // Must be static. These are initialized here because these are related to class instance, not status.

        // Layout views.
        private DrawerLayout mDrawerLayout;
        static BookAdapter adapter;  //Must be static.
        private RecyclerView mNavDrawer;
        static DrawerAdapter mAdapter; // This adapter is static so many editing actions update the drawer.
        static FloatingActionButton deleteButton; //Must be static so adapter can access it and show the delete button.

        private String query;
        private int colorSearch;
        private SearchRecentSuggestions suggestions;
        private SharedPreferences prefs;
        private int columns;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.layout_library);
            getSupportActionBar().setTitle(R.string.library);
            prefs = getSharedPreferences("EdenNotebookSettings",MODE_PRIVATE);

            updated = true;
            restored = false;

            onSearch = false;
            onColorType = false;
            onFavorites = false;

            // Toolbar initiation.
            Toolbar toolbar = (Toolbar) findViewById(R.id.library_toolbar);
            setSupportActionBar(toolbar);

            //Note list initiation.
            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.view_recycler_book);
            columns = prefs.getInt("Columns",1);
            adapter = new BookAdapter(this);
            mRecyclerView.setAdapter(adapter);
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, columns));

            // Drawer initiation.
            mNavDrawer = (RecyclerView) findViewById(R.id.left_drawer);
            mAdapter = new DrawerAdapter(this);
            mNavDrawer.setAdapter(mAdapter);
            mNavDrawer.setLayoutManager(new LinearLayoutManager(this));

            //Drawer layout initiation.
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

            // Delete button initiation
            deleteButton = (FloatingActionButton) findViewById(R.id.delete_button);
            deleteButton.hide(false);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (adapter.allDeleteList.isEmpty()) {
                        //If there is nothing to delete, notify the user.
                        Toast.makeText(Library.this, R.string.ask_delete_empty, Toast.LENGTH_SHORT).show();
                    } else {
                        //If there is something to delete, execute delete code.
                        if (prefs.getBoolean("Delete", true)) {
                            //Show asking dialogue.
                            AlertDialog.Builder builder = new AlertDialog.Builder(Library.this)
                                    .setTitle(R.string.caution)
                                    .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) { groupDelete(); }
                                    })
                                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });
                            if (adapter.allDeleteList.size() == 1)
                                builder.setMessage(R.string.ask_delete);
                            else
                                builder.setMessage(R.string.ask_delete_multiple);
                            builder.create().show();
                        } else {
                            groupDelete();
                        }
                    }
                }
            });

            // Search history initiation.
            suggestions = new SearchRecentSuggestions(this,NoteSearchSuggestionProvider.AUTHORITY, NoteSearchSuggestionProvider.MODE);

            //If password protected, query for password.
            if (prefs.getBoolean("Encryption",false) && locked) { unlock(); }
            else {
                if (locked) {
                    suggestions.clearHistory();
                    backupData(); //This means app will back up data only when it is opened once initially despite activity being recreated.
                }
                locked = false;
            }
        }

        @Override
        protected void onStart(){
            super.onStart();

            //Date is reset regularly because date could change while the app is open.
            adapter.getDate();

            if (restored){ // Notes have been restored. Activity must look like brand new.
                getSupportActionBar().setTitle(R.string.library);
                onColorType = false;
                onFavorites = false;
                if (onSearch)
                    super.onBackPressed(); //Collapsing the searchview will initialize data set for us.
                else {
                    if (adapter.groupDelete) deleteButton.hide(false); // Or we must do it manually
                    adapter.initializeDataArray();
                    adapter.notifyDataSetChanged();
                }
                mAdapter.notifyDataSetChanged();
                restored = false;
            } else if (!updated) {
                if (adapter.groupDelete) deleteButton.hide(false);
                if (onSearch)
                    adapter.initializeSearchResult(query);
                else if (onColorType)
                    adapter.initializeColorArray(colorSearch);
                else if (onFavorites)
                    adapter.initializeFavorites();
                adapter.notifyDataSetChanged();
                updated = true;
            }
        }

        // When search suggestion is clicked, this method is called.
        // Thus, override this method, even if the QueryTextChangeListener does searching most of the time.
        @Override
        protected void onNewIntent(Intent intent){
            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                String newQuery = intent.getStringExtra(SearchManager.QUERY);
                if (!newQuery.equals(query)){
                    // This means the search suggestion was selected without editing the query text.
                    // As a result, this code is executed only when search suggestions are selected.
                    if (adapter.groupDelete) deleteButton.hide();
                    query = newQuery;
                    adapter.initializeSearchResult(query);
                    adapter.notifyDataSetChanged();
                    suggestions.saveRecentQuery(query, null);
                    if (mDrawerLayout.isDrawerOpen(mNavDrawer)) mDrawerLayout.closeDrawer(mNavDrawer);
                }
            } else if (!intent.hasCategory(Intent.CATEGORY_LAUNCHER)){
                // Don't do anything if this activity was already opened and it's opened again by the launcher.
                // Only execute if this is not a launcher intent.
                mDrawerLayout.closeDrawer(mNavDrawer);
                onColorType = intent.hasExtra("Category");
                onFavorites = intent.hasExtra("Favorites");

                colorSearch = intent.getIntExtra("Category", intent.getIntExtra("Favorites", -2)); // Colorsearch will also serve as the index of title.
                getSupportActionBar().setTitle(DrawerAdapter.ACTIVITY_TITLE_RES_ID[colorSearch + 2]);

                if (onSearch){
                    super.onBackPressed(); // Collapses search view. The CollapseListener will initialize the adapter for us.
                } else { // We cannot initialize by "Collapsing" the searchview if it's not open. Must initialize manually.
                    if (adapter.groupDelete) deleteButton.hide();
                    if (onColorType)      adapter.initializeColorArray(colorSearch);
                    else if (onFavorites) adapter.initializeFavorites();
                    else                  adapter.initializeDataArray();
                    adapter.notifyDataSetChanged();
                } //At the end of this if else statement, onSearch is false for sure.
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
            MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search), new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    query = null; //Making sure it's initially null.
                    if (adapter.groupDelete) deleteButton.hide();
                    adapter.wipeDataArray();
                    adapter.notifyDataSetChanged();
                    onSearch = true;
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    if (adapter.groupDelete) deleteButton.hide();
                    if (onColorType) adapter.initializeColorArray(colorSearch);
                    else if (onFavorites) adapter.initializeFavorites();
                    else adapter.initializeDataArray();
                    adapter.notifyDataSetChanged();
                    onSearch = false;
                    if (mDrawerLayout.isDrawerOpen(mNavDrawer)) mDrawerLayout.closeDrawer(mNavDrawer);
                    return true;
                }
            });
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    suggestions.saveRecentQuery(query, null);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    if (onSearch) { //Only query search when it's in search mode.
                        if (adapter.groupDelete) deleteButton.hide();
                        query = s;
                        adapter.initializeSearchResult(query);
                        adapter.notifyDataSetChanged();
                        if (mDrawerLayout.isDrawerOpen(mNavDrawer)) mDrawerLayout.closeDrawer(mNavDrawer);
                    }
                    return false;
                }
            });
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.

            if (locked) //Don't do anything if app is still locked.
                return true;

            if (mDrawerLayout.isDrawerOpen(mNavDrawer)) mDrawerLayout.closeDrawer(mNavDrawer);

            int id = item.getItemId();
            if (id == R.id.action_settings) {
                startActivity(new Intent(this, Settings.class));
                if (adapter.groupDelete){
                    adapter.groupDelete = false;
                    for (String title : adapter.allDeleteList) adapter.notifyItemChanged(adapter.mCatalog.indexOf(title) + columns);
                    adapter.allDeleteList = new ArrayList<>();
                    deleteButton.hide();
                }
                return true;
            } else if (id == R.id.action_add){
                startActivity(new Intent(this, AddNote.class));
                if (adapter.groupDelete){
                    adapter.groupDelete = false;
                    for (String title : adapter.allDeleteList) adapter.notifyItemChanged(adapter.mCatalog.indexOf(title) + columns);
                    adapter.allDeleteList = new ArrayList<>();
                    deleteButton.hide();
                }
                return true;
            } else
                return super.onOptionsItemSelected(item);
        }

        @Override
        public void onBackPressed(){
            if (mDrawerLayout.isDrawerOpen(mNavDrawer))
                mDrawerLayout.closeDrawer(mNavDrawer);
            else if (adapter.groupDelete){ //Cancels the group delete mode.
                adapter.groupDelete = false;
                for (String title : adapter.allDeleteList) adapter.notifyItemChanged(adapter.mCatalog.indexOf(title) + columns);
                adapter.allDeleteList = new ArrayList<>();
                deleteButton.hide();
            } else if (onSearch) //Cancels search mode.
                super.onBackPressed();
            else if (onColorType || onFavorites){ //Cancels color search mode or the favorites mode.
                getSupportActionBar().setTitle(R.string.library);
                adapter.initializeDataArray();
                adapter.notifyDataSetChanged();
                onColorType = false;
                onFavorites = false;
            } else //Does the default action.
                super.onBackPressed();
        }

        private void unlock(){
            LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_password, null);
            final EditText edittext = (EditText) layout.findViewById(R.id.editext);
            new AlertDialog.Builder(Library.this)
                    .setView(layout)
                    .setTitle(R.string.settings_password_decrypt)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (edittext.getText().toString().equals(prefs.getString("Password", null))) {
                                //Password Correct.
                                locked = false;
                                suggestions.clearHistory();
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

        private void groupDelete(){

            for (String title : adapter.allDeleteList) {

                //Delete detail info from database.
                while (new NoteDatabaseAdapter(this).deleteRow(title) < 0) {}

                int noteIndex = adapter.mCatalog.indexOf(title);

                //Finally delete the actual saved content of the note.
                deleteFile(title);
                if (adapter.allColors.get(noteIndex) == 8) {
                    deleteFile(title+"@#$^23!^");
                    deleteFile(title+"AG5463#$1!#$&");
                }

                //Remove note from recyclerview. Then remove note info from adapter.
                adapter.notifyItemRemoved(noteIndex + columns);
                adapter.deleteNote(title);
            }
            //Empty delete candidates.
            adapter.groupDelete = false;
            adapter.allDeleteList = new ArrayList<>();

            //Hide the button after all is well.
            deleteButton.hide();

            // Update the drawer.
            mAdapter.notifyDataSetChanged();

            //Notify the user.
            Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
        }

        protected void backupData(){ //This runs on background.

            if (prefs.getBoolean("Backup",false)) {

                backingUp = true;

                //Connect to cloud.
                Parse.initialize(this, "8rRg8GndjElDiy1UrviRT650VNE0yKN8BcF1AaFH", "6Zy8VRFP2A017iol0AkJyCJRkOM7TGDWatl7ptIE");
                final String deviceID = "ID" + android.provider.Settings.Secure.getString(getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);

                //Remove existing cloud data.
                ParseQuery<ParseObject> query =ParseQuery.getQuery(deviceID);
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        if (e == null){
                            try {
                                for (ParseObject oldNote : parseObjects)
                                    oldNote.deleteEventually();
                            } catch (Exception e1) { Toast.makeText(Library.this, getResources().getString(R.string.backup_error)+e1.toString(), Toast.LENGTH_SHORT).show(); return; }
                            //Quit if failed to erase note because nothing is worse than DUPLICATE NOTES.

                            //Backing up AFTER finished deleting original backup: It prevents deleting the notes in cloud that are currently being backed up.
                            for (int i = 0; i < adapter.mCatalog.size(); i++) {

                                ParseObject note = new ParseObject(deviceID);
                                String title = adapter.mCatalog.get(i);
                                String createdAt = adapter.allCreatedAt.get(i);
                                String editedAt = adapter.allEditedAt.get(i);
                                int starred = adapter.allStars.get(i);
                                int color = adapter.allColors.get(i);
                                if (color == 8) color = 0; //Backup does not save photo headers and returns note to default header.

                                note.put("title", title);
                                note.put("DATE_CREATED", createdAt);
                                note.put("DATE_EDITED", editedAt);
                                note.put("Starred", starred);
                                note.put("Color", color);
                                try {
                                    //Obtaining byte string info from filename.
                                    FileInputStream fis = openFileInput(title);
                                    byte[] data = new byte[fis.available()];
                                    if (fis.read(data) != 0) // Sometimes if the saved string is nothing (""), then the read()
                                        // will constantly return 0 and fall into the constant while loop.
                                        // Must make sure there is something to read() before proceeding.
                                        while (fis.read(data) != -1) { /*This loop constantly extracts byte that will
                                                 eventually be converted to a string byte by byte.
                                        }

                                    note.put("content", new String(data));
                                    fis.close();
                                } catch (Exception exp) {
                                    Toast.makeText(Library.this, R.string.backup_fail + exp.toString(), Toast.LENGTH_SHORT).show();
                                }
                                note.saveEventually();
                            }
                        } else { // Restore failed. Could not connect to cloud.
                            new AlertDialog.Builder(Library.this)
                                    .setTitle(R.string.error)
                                    .setMessage(getResources().getString(R.string.parse_failed)+e.toString())
                                    .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {}
                                    }).create().show();
                        }

                        backingUp = false;
                    }
                });
            }
        }
    }*/
}