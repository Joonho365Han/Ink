package eden.notebook.ink;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

public class ColorLibrary extends ActionBarActivity{

    static BookAdapter adapter;
    private static int colorIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_library);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.view_recycler_book);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        colorIndex = getIntent().getIntExtra("ColorCategory",1);
        adapter = new BookAdapter(this, colorIndex);
        adapter.adapterType = 2;
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onNewIntent(Intent intent){
        colorIndex = getIntent().getIntExtra("ColorCategory",1);
        adapter.initializeColorArray(colorIndex);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart(){
        super.onStart();

        adapter.getDate();

        if (!BookAdapter.updated) {
            adapter.initializeColorArray(colorIndex);
            adapter.notifyDataSetChanged();
            BookAdapter.updated = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_color_library, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
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
        }

        return super.onOptionsItemSelected(item);
    }
}
