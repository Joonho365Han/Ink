package eden.notebook.ink;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class ViewNote extends ActionBarActivity {

    private int mFileIndex;
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewnote);

        //The index of the item clicked.
        mFileIndex = getIntent().getIntExtra("index",0);

        pager = (ViewPager) findViewById(R.id.note_pager);
        pager.setAdapter(new NoteAdapter(getSupportFragmentManager()));
        pager.setPageTransformer(true, new DepthPageTransformer());
        pager.setCurrentItem(mFileIndex, false);
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

    private class DepthPageTransformer implements ViewPager.PageTransformer {

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);
            } else if (position < 0) { // [-1,0]
                view.setAlpha(1);        // Fade the page out.
                view.setTranslationX(0); // Counteract the default slide transition
            } else if (position <= 1) {  // (0,1]
                view.setAlpha(1 - position / 3);
                view.setTranslationX(pageWidth * -position);
            } else { // (1,+Infinity]
                     // This page is way off-screen to the right.
                view.setAlpha(0);
            }

        }
    }

    @Override
    protected void onNewIntent(Intent intent){
        if (intent.hasExtra("index")) {
            mFileIndex = intent.getIntExtra("index", mFileIndex);
            pager.setCurrentItem(mFileIndex, false);
            try { pager.getChildAt(mFileIndex).setAlpha(1); } catch (Exception e) { /*Do nothing*/ }
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
            if (getSharedPreferences("Settings", Context.MODE_PRIVATE).getBoolean("Delete",true)) {
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

        return super.onOptionsItemSelected(item);
    }

    protected void deleteNote(){
        try {
            //Deleting file.
            int currentItem = pager.getCurrentItem();
            deleteFile(BookAdapter.mCatalog.get(currentItem));
            BookAdapter.mCatalog.remove(currentItem);
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();

            //Get out of here.
            super.onBackPressed();
        } catch (Exception e){
            Toast.makeText(this, "Cannot delete: File does not exist", Toast.LENGTH_SHORT).show();
        }
    }
}
