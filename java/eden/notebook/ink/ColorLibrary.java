package eden.notebook.ink;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;

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

public class ColorLibrary extends ActionBarActivity{

    static boolean updated;
    private int colorIndex;
    static BookAdapter adapter; //Must be static.
    static FloatingActionButton deleteButton; //Must be static so adapter can access it.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_library);
        updated = true;

        //Setting up variables and elements.
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.view_recycler_book);
        colorIndex = getIntent().getIntExtra("ColorCategory",0);
        adapter = new BookAdapter(this);
        adapter.initializeColorArray(colorIndex);
        adapter.adapterType = 2;
        mRecyclerView.setAdapter(adapter);
        if ((getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_LARGE && (getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) !=
                Configuration.SCREENLAYOUT_SIZE_UNDEFINED)  //Big screen shows two notes per line.
            mRecyclerView.setLayoutManager(new GridLayoutManager(this,2));
        else                                                //Phone screen shows one note per line.
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        deleteButton = (FloatingActionButton) findViewById(R.id.delete_button);
        deleteButton.hide(false);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (adapter.allDeleteList.isEmpty()){
                    //If there is nothing to delete, notify the user.
                    new AlertDialog.Builder(ColorLibrary.this)
                            .setMessage(R.string.nothing_to_delete)
                            .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {}
                            }).create().show();
                } else {
                    //If there is something to delete, execute delete code.
                    if (getSharedPreferences("EdenNotebookSettings", MODE_PRIVATE).getBoolean("Delete",true)) {
                        //Show asking dialogue.
                        AlertDialog.Builder builder = new AlertDialog.Builder(ColorLibrary.this)
                                .setTitle(R.string.caution)
                                .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) { groupDelete();}
                                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {}
                                });
                        if (adapter.allDeleteList.size() == 1) builder.setMessage(R.string.ask_delete);
                        else                                   builder.setMessage(R.string.ask_delete_multiple);
                        builder.create().show();
                    }
                    else { groupDelete(); }
                }
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();

        adapter.getDate(); //Date is reset regularly because date could change while the app is open.

        if (!updated) {
            adapter.initializeColorArray(colorIndex); //Just like search, except you're always searching a color. So always update.
            adapter.notifyDataSetChanged();
            deleteButton.hide(false);
            updated = true;
        }
    }

    @Override
    protected void onNewIntent(Intent intent){
        if (adapter.groupDelete) deleteButton.hide();
        colorIndex = getIntent().getIntExtra("ColorCategory",0);
        adapter.initializeColorArray(colorIndex);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_color_library, menu);
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
            if (adapter.groupDelete){ //Cancels the group delete mode.
                adapter.groupDelete = false;
                adapter.allDeleteList = new ArrayList<>();
                updated = false;
            }
            Intent intent = new Intent(this, Settings.class);
            intent.putExtra( "AdapterType", 2 );
            startActivity(intent);
            return true;
        } else if (id == R.id.action_add){
            if (adapter.groupDelete){ //Cancels the group delete mode.
                adapter.groupDelete = false;
                adapter.allDeleteList = new ArrayList<>();
                updated = false;
            }
            Intent intent = new Intent(this, AddNote.class);
            intent.putExtra( "AdapterType", 2 );
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        if (adapter.groupDelete){ //Cancels the group delete mode.
            adapter.groupDelete = false;
            for (String title : adapter.allDeleteList) adapter.notifyItemChanged(adapter.mCatalog.indexOf(title) + 1);
            adapter.allDeleteList = new ArrayList<>();
            deleteButton.hide();
        } else {
            super.onBackPressed();
        }
    }

    private void groupDelete(){

        for (String title : adapter.allDeleteList) {

            //Delete detail info from database.
            if (new NoteDatabaseAdapter(this).deleteRow(title) < 0) {
                //Failed to delete data from SQL.
                Toast.makeText(this, "Unclean Delete: database error", Toast.LENGTH_SHORT).show();
                return;
            }

            //Finally delete the actual saved content of the note.
            deleteFile(title);

            //Remove note from recyclerview. Then remove note info from adapter.
            adapter.notifyItemRemoved(adapter.mCatalog.indexOf(title) + 1);
            adapter.deleteNote(title);
        }
        //Empty delete candidates.
        adapter.groupDelete = false;
        adapter.allDeleteList = new ArrayList<>();

        //Hide the button after all is well.
        deleteButton.hide();

        //Notify the user.
        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
    }
}
