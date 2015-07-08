package eden.notebook.ink;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;

public class Library extends ActionBarActivity {

    public static int mDefaultPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_library);

        ViewPager mNotePager = (ViewPager) findViewById(R.id.note_view);
        mNotePager.setAdapter(new NotesAdapter(getSupportFragmentManager()));
        mNotePager.setCurrentItem(mDefaultPage);
    }

    private static class NotesAdapter extends FragmentStatePagerAdapter {
        public NotesAdapter(FragmentManager fm){ super(fm); }

        @Override
        public Fragment getItem(int page) {
            if (page == 0)  return new BookListFragment();
            else            return new ScrollListFragment();
        }

        @Override
        public int getCount() { return 2; }
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
}
