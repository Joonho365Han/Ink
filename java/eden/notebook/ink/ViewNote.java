package eden.notebook.ink;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
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

    @Override
    protected void onNewIntent(Intent intent){
        if (intent.hasExtra("index")) {
            mFileIndex = intent.getIntExtra("index", mFileIndex);
            pager.setCurrentItem(mFileIndex, false);
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
            intent.putExtra( "index",  pager.getCurrentItem() );
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_settings){
            startActivity(new Intent(this, Settings.class));
            return true;
        }
        else if (id == R.id.action_delete){
            if (1 == 1)
                return true;
            try {
                //Deleting file.
                int currentItem = pager.getCurrentItem();
                deleteFile(BookAdapter.mCatalog.get(currentItem));
                BookAdapter.mCatalog.remove(currentItem);
                Toast.makeText(this, "Deleted.", Toast.LENGTH_SHORT).show();

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
