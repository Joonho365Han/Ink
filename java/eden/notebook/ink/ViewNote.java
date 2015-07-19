package eden.notebook.ink;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.transition.Explode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class ViewNote extends ActionBarActivity {

    private int mAdapterIndex;
    private ViewPager pager;
    private int AdapterType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewnote);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getWindow().setEnterTransition(new Explode());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //The index of the item clicked.
        mAdapterIndex = getIntent().getIntExtra("index",0);
        AdapterType = getIntent().getIntExtra("AdapterType",1);

        pager = (ViewPager) findViewById(R.id.note_pager);
        pager.setAdapter(new NoteAdapter(getSupportFragmentManager(),AdapterType));
        pager.setCurrentItem(mAdapterIndex, false);
    }

    private static class NoteAdapter extends FragmentStatePagerAdapter {

        private int AdapterType;

        public NoteAdapter(FragmentManager fm, int type) {
            super(fm);
            AdapterType = type;
        }

        @Override
        public Fragment getItem(int position) {
            NoteFragment page = new NoteFragment();
            page.mFileIndex = position;
            page.adapterType = AdapterType;
            return page;
        }

        @Override
        public int getCount() {
            if (AdapterType == 1)      return Library.adapter.mCatalog.size();
            else                       return ColorLibrary.adapter.mCatalog.size();
        }
    }

    @Override
    protected void onNewIntent(Intent intent){
        if (intent.hasExtra("index")) {
            mAdapterIndex = intent.getIntExtra("index", mAdapterIndex);
            AdapterType = intent.getIntExtra("AdapterType",AdapterType);
            pager.setCurrentItem(mAdapterIndex, false);
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
            intent.putExtra( "AdapterType", AdapterType );
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
                        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                }).create().show();
            }
            else { deleteNote(); }
            return true;
        }
        else if (id == android.R.id.home){
            super.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void deleteNote(){
        try {
            //Deleting file.
            int currentItem = pager.getCurrentItem();
            if (AdapterType == 1) {
                String title = Library.adapter.mCatalog.get(currentItem);
                deleteFile(title);
                if (Library.adapter.dataAdapter.deleteRow(title) < 0){
                    Toast.makeText(this, "Failed: database error", Toast.LENGTH_SHORT).show();
                    return;
                }
                Library.adapter.mCatalog.remove(currentItem);
                Library.adapter.allCreatedAt.remove(currentItem);
                Library.adapter.allEditedAt.remove(currentItem);
                Library.adapter.allStars.remove(currentItem);
                Library.adapter.allColors.remove(currentItem);
            } else {
                String title = ColorLibrary.adapter.mCatalog.get(currentItem);
                deleteFile(title);
                if (ColorLibrary.adapter.dataAdapter.deleteRow(title) < 0){
                    Toast.makeText(this, "Failed: database error", Toast.LENGTH_SHORT).show();
                    return;
                }
                ColorLibrary.adapter.mCatalog.remove(currentItem);
                ColorLibrary.adapter.allCreatedAt.remove(currentItem);
                ColorLibrary.adapter.allEditedAt.remove(currentItem);
                ColorLibrary.adapter.allStars.remove(currentItem);
                ColorLibrary.adapter.allColors.remove(currentItem);
            }
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();

            //Get out of here.
            BookAdapter.updated = false;
            super.onBackPressed();
        } catch (Exception e){
            Toast.makeText(this, "Cannot delete: File does not exist", Toast.LENGTH_SHORT).show();
        }
    }
}
