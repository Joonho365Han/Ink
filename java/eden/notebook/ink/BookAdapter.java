package eden.notebook.ink;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.NoteHolder> {

    static final int[] COLOR_ARRAY = new int[]{
            Color.parseColor("#EFEFEF"), //Default cloud
            Color.parseColor("#FFC6D9"), //Pink
            Color.parseColor("#FFCC80"), //Orange
            Color.parseColor("#FFF59D"), //Yellow
            Color.parseColor("#DCEDC8"), //Green
            Color.parseColor("#B3E5FC"), //Blue
            Color.parseColor("#C5CAE9"), //Indigo
            Color.parseColor("#E1BEE7"), //Purple
            Color.parseColor("#888888"), //Photo
            Color.parseColor("#333333")};//Dark font
    private Activity context;
    private LayoutInflater inflater;
    NoteDatabaseAdapter dataAdapter;
    int adapterType;

    List<String> mCatalog;
    List<String> allCreatedAt;
    List<String> allEditedAt;
    List<Integer> allStars;
    List<Integer> allColors;

    private static String date = null;
    static boolean updated = true;

    //Generic constructor.
    public BookAdapter (Activity context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        dataAdapter = new NoteDatabaseAdapter(context);

        initializeDataArray();
    }

    //Constructor for color categories.
    public BookAdapter (Activity context, int colorIndex) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        dataAdapter = new NoteDatabaseAdapter(context);

        initializeColorArray(colorIndex);
    }

    public void initializeDataArray(){
        mCatalog = new ArrayList<>();
        allCreatedAt = new ArrayList<>();
        allEditedAt = new ArrayList<>();
        allStars = new ArrayList<>();
        allColors = new ArrayList<>();

        mCatalog = dataAdapter.getAllTitles();
        allCreatedAt = dataAdapter.getAllCreatedAt();
        allEditedAt = dataAdapter.getAllEditedAt();
        allStars = dataAdapter.getAllStars();
        allColors = dataAdapter.getAllColors();
    }

    public void initializeColorArray(int colorIndex){

        List<String> titles = dataAdapter.getAllTitles();
        List<String> createdAt = dataAdapter.getAllCreatedAt();
        List<String> editedAt = dataAdapter.getAllEditedAt();
        List<Integer> stars = dataAdapter.getAllStars();
        List<Integer> colors = dataAdapter.getAllColors();
        getDate();

        mCatalog = new ArrayList<>();
        allCreatedAt = new ArrayList<>();
        allEditedAt = new ArrayList<>();
        allStars = new ArrayList<>();
        allColors = new ArrayList<>();

        for (int i = 0 ; i<titles.size() ; i++){
            if (colors.get(i) == colorIndex){
                mCatalog.add(titles.get(i));
                allCreatedAt.add(createdAt.get(i));
                allEditedAt.add(editedAt.get(i));
                allStars.add(stars.get(i));
                allColors.add(colors.get(i));
            }
        }
    }

    public void initializeSearchResult(String query){
        String uncappedQuery = query.toLowerCase();

        List<String> titles = dataAdapter.getAllTitles();
        List<String> createdAt = dataAdapter.getAllCreatedAt();
        List<String> editedAt = dataAdapter.getAllEditedAt();
        List<Integer> stars = dataAdapter.getAllStars();
        List<Integer> colors = dataAdapter.getAllColors();

        mCatalog = new ArrayList<>();
        allCreatedAt = new ArrayList<>();
        allEditedAt = new ArrayList<>();
        allStars = new ArrayList<>();
        allColors = new ArrayList<>();

        for (int i = 0 ; i<titles.size() ; i++){
            String filename = titles.get(i);
            String content = null;
            try {
                //Obtaining byte string info from filename.
                FileInputStream fis = context.openFileInput(filename);
                byte[] data = new byte[fis.available()];
                if (fis.read(data) != 0) // Sometimes if the saved string is nothing (""), then the read()
                    // will constantly return 0 and fall into the constant while loop.
                    // Must make sure there is something to read() before proceeding.
                    while (fis.read(data) != -1) { /*This loop constantly extracts byte that will
                                                 eventually be converted to a string byte by byte.*/ }

                content = new String(data);
                fis.close();                                                }
            catch (Exception e) {/*Don't do anything. Just a query.*/}
            if (filename.contains(query) || content.contains(query) || filename.contains(uncappedQuery) || content.contains(uncappedQuery)){
                mCatalog.add(titles.get(i));
                allCreatedAt.add(createdAt.get(i));
                allEditedAt.add(editedAt.get(i));
                allStars.add(stars.get(i));
                allColors.add(colors.get(i));
            }
        }
    }

    public void wipeDataArray(){
        mCatalog = new ArrayList<>();
        allCreatedAt = new ArrayList<>();
        allEditedAt = new ArrayList<>();
        allStars = new ArrayList<>();
        allColors = new ArrayList<>();
    }

    public void getDate(){

        Calendar c = Calendar.getInstance();
        String month = String.valueOf(c.get(Calendar.MONTH)+1);
        String day = String.valueOf(c.get(Calendar.DATE));

        if (month.length() == 1)
            month = "0"+month;
        if (day.length() == 1)
            day = "0"+day;

        date = String.valueOf(c.get(Calendar.YEAR))+"/"+
                month+"/"+
                day;
    }

    @Override
    public NoteHolder onCreateViewHolder(ViewGroup viewGroup, int i)
        {return new NoteHolder(inflater.inflate(R.layout.film_holder, viewGroup, false));}

    @Override
    public void onBindViewHolder(NoteHolder noteHolder, int index) {

        if (index == 0){
            noteHolder.mBackground.setVisibility(View.INVISIBLE);
            return;
        } else {
            noteHolder.mBackground.setVisibility(View.VISIBLE);
        }

        int colorIndex = allColors.get(index-1);
        if (colorIndex == 8) { //Image background.
            noteHolder.mBackground.setBackgroundColor(Color.parseColor("#333333"));
            //////////////////////////////////////////////////////////////////////////////////////////////Change this later.
            noteHolder.mTitle.setTextColor(Color.WHITE);
            noteHolder.mEdited.setTextColor(Color.WHITE);
        }
        else {
            noteHolder.mBackground.setBackgroundColor(COLOR_ARRAY[colorIndex]);
            noteHolder.mTitle.setTextColor(COLOR_ARRAY[9]);
            noteHolder.mEdited.setTextColor(COLOR_ARRAY[9]);
        }

        noteHolder.mTitle.setText(mCatalog.get(index-1));

        //If edited today, show the time. If edited before yesterday, show date.
        String editedAt = allEditedAt.get(index-1);
        String editDate = editedAt.substring(0,editedAt.indexOf(" "));
        if (editDate.equals(date)) noteHolder.mEdited.setText("Today, "+editedAt.substring(editDate.length()).trim());
        else                       noteHolder.mEdited.setText(editDate);

        if (allStars.get(index-1)==1) noteHolder.mStar.setBackgroundResource(R.drawable.ic_stars_white_36dp);
        else                        noteHolder.mStar.setBackgroundResource(R.drawable.ic_star_border_white_24dp);
    }

    @Override
    public int getItemCount() { return mCatalog.size() + 1; }

    class NoteHolder extends RecyclerView.ViewHolder{

        RelativeLayout mBackground;
        TextView mTitle;
        TextView mEdited;
        ImageView mStar;

        public NoteHolder(View itemView) {
            super(itemView);

            mBackground = (RelativeLayout) itemView.findViewById(R.id.note_background);
            mBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ViewNote.class);
                    intent.putExtra("index", getAdapterPosition()-1);
                    intent.putExtra("AdapterType",adapterType);
                    if (!Library.locked)
                        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP )
                            context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(context).toBundle());
                        else
                            context.startActivity(intent);
                }
            });

            mTitle = (TextView) itemView.findViewById(R.id.note_title);
            mEdited = (TextView) itemView.findViewById(R.id.textView9);

            mStar = (ImageView) itemView.findViewById(R.id.imageView4);
            mStar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = getAdapterPosition()-1;
                    dataAdapter.updateFavorites(index);
                    if (allStars.get(index)==1){
                        mStar.setBackgroundResource(R.drawable.ic_star_border_white_24dp);
                        allStars.set(index,0);
                    }
                    else {
                        mStar.setBackgroundResource(R.drawable.ic_stars_white_36dp);
                        allStars.set(index,1);
                    }
                }
            });
        }
    }
}