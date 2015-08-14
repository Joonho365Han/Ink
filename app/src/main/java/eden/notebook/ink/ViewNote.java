package eden.notebook.ink;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.FileInputStream;

public class ViewNote extends ActionBarActivity {

    private int mAdapterIndex;
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setSharedElementExitTransition(TransitionInflater.from(this).inflateTransition(R.transition.enlarge_photo_transition));
            getWindow().setSharedElementReenterTransition(TransitionInflater.from(this).inflateTransition(R.transition.shrink_photo_transition));
        }
        setContentView(R.layout.activity_viewnote);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //The index of the item clicked.
        mAdapterIndex = getIntent().getIntExtra("index",0);

        pager = (ViewPager) findViewById(R.id.note_pager);
        pager.setAdapter(new NoteAdapter(getSupportFragmentManager()));
        pager.setCurrentItem(mAdapterIndex, false);
    }

    private static class NoteAdapter extends FragmentStatePagerAdapter {

        public NoteAdapter(FragmentManager fm) { super(fm); }

        @Override
        public Fragment getItem(int position) {
            NoteFragment page = new NoteFragment();
            page.mFileIndex = position;
            return page;
        }

        @Override
        public int getCount() { return BookAdapter.mCatalog.size(); }
    }

    @Override
    protected void onNewIntent(Intent intent){
        if (intent.hasExtra("index")) {
            mAdapterIndex = intent.getIntExtra("index", mAdapterIndex);
            pager.setCurrentItem(mAdapterIndex, false);
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        if (Library.restored) {
            // This condition is only met when we restore notes from settings.
            // If Notes were restored, skip to the library immediately.
            pager.setAdapter(null); //We do this just to prevent IllegalStateException.
            super.onBackPressed();
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

        if (id == R.id.action_edit) {
            Intent intent = new Intent(this, EditNote.class);
            intent.putExtra( "index",  pager.getCurrentItem() );
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_settings){
            startActivity(new Intent(this, Settings.class));
            return true;
        }
        else if (id == R.id.action_delete){
            if (getSharedPreferences("EdenNotebookSettings", Context.MODE_PRIVATE).getBoolean("Delete",true)) {
                //Show asking dialogue.
                new AlertDialog.Builder(this).setMessage(R.string.ask_delete)
                        .setTitle(R.string.caution)
                        .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { deleteNote();}
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                }).create().show(); }
            else
                deleteNote();
            return true;
        }
        else if (id == R.id.action_email){
            int currentItem = pager.getCurrentItem();
            final String title = BookAdapter.mCatalog.get(currentItem);
            int colorIndex = BookAdapter.allColors.get(currentItem);

            if (colorIndex == 8)
                new AlertDialog.Builder(this)
                        .setTitle(R.string.note)
                        .setMessage(R.string.no_photo_attachable)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { sendNoteMail(title);}
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {}
                        }).create().show();
            else
                sendNoteMail(title);
            return true;
        }
        else if (id == android.R.id.home){
            super.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void deleteNote() {
        //Retrieve note title.
        int currentItem = pager.getCurrentItem();
        String title = BookAdapter.mCatalog.get(currentItem);
        int colorIndex = BookAdapter.allColors.get(currentItem);

        //Delete info from database and adapter.
        while (new NoteDatabaseAdapter(this).deleteRow(title) < 0) {}
        BookAdapter.deleteNote(title);

        //Finally delete the actual saved content of the note.
        deleteFile(title);
        if (colorIndex == 8){ // Delete photo headers too if they exist.
            new DeleteBitmapTask().execute(title+"@#$^23!^");
            deleteFile(title +"AG5463#$1!#$&");
        }
        Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();

        //Get out of here.
        Library.updated = false;
        Library.mAdapter.notifyItemChanged(colorIndex + 4);
        Library.mAdapter.notifyItemChanged(2);
        super.onBackPressed();
    }

    protected void sendNoteMail(String title){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_SUBJECT, title);
        try { // Obtaining byte string info from filename.
            FileInputStream fis = openFileInput(title);
            byte[] data = new byte[fis.available()];
            if (fis.read(data) != 0) while (fis.read(data) != -1) {}

            i.putExtra(Intent.EXTRA_TEXT, new String(data));
            fis.close();                                                                       }
        catch (Exception e) { Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show(); return; }
        try {
            startActivity(Intent.createChooser(i,getResources().getString(R.string.send_mail)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, ex.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    class DeleteBitmapTask extends AsyncTask<String, Void, Void> {
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
