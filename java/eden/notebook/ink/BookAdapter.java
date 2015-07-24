package eden.notebook.ink;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.NoteHolder> {

    int adapterType; //This number tells which Activity's recyclerview this instance is associated with.
    //Set by the Activities' constructor.
    static final int[] COLOR_ARRAY = new int[]{
            Color.parseColor("#F5F5F5"),   //0 Default cloud (= dark tint)
            Color.parseColor("#FFC6D9"),   //1 Pink
            Color.parseColor("#FFCC80"),   //2 Orange
            Color.parseColor("#FFF59D"),   //3 Yellow
            Color.parseColor("#DCEDC8"),   //4 Green
            Color.parseColor("#B3E5FC"),   //5 Blue
            Color.parseColor("#C5CAE9"),   //6 Indigo
            Color.parseColor("#E1BEE7"),   //7 Purple
            Color.parseColor("#888888"),   //8 Photo (This color is used by AddNote and EditNote.)
            Color.parseColor("#333333"),   //9 Dark font
            Color.parseColor("#666666")};  //10 Delete selection
    private Activity context;
    private LayoutInflater inflater;
    private NoteDatabaseAdapter dataAdapter;
    private String date;

    boolean groupDelete; //Don't make it private. Library must access it.
    //Don't make it static. Every different adapters must be in different state.
    List<String> allDeleteList;

    List<String> mCatalog;
    List<String> allCreatedAt;
    List<String> allEditedAt;
    List<Integer> allStars;
    List<Integer> allColors;

    //Generic constructor.
    public BookAdapter (Activity context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        dataAdapter = new NoteDatabaseAdapter(context);
        getDate();
    }

    public void initializeDataArray(){
        groupDelete = false;
        allDeleteList = new ArrayList<>();

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

        if (colorIndex == 0) Toast.makeText(context, "Error: failed to receive color type", Toast.LENGTH_SHORT).show();

        groupDelete = false;
        allDeleteList = new ArrayList<>();

        mCatalog = new ArrayList<>();
        allCreatedAt = new ArrayList<>();
        allEditedAt = new ArrayList<>();
        allStars = new ArrayList<>();
        allColors = new ArrayList<>();

        List<String> titles = dataAdapter.getAllTitles();
        List<String> createdAt = dataAdapter.getAllCreatedAt();
        List<String> editedAt = dataAdapter.getAllEditedAt();
        List<Integer> stars = dataAdapter.getAllStars();
        List<Integer> colors = dataAdapter.getAllColors();
        getDate();

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
        groupDelete = false;
        allDeleteList = new ArrayList<>();

        mCatalog = new ArrayList<>();
        allCreatedAt = new ArrayList<>();
        allEditedAt = new ArrayList<>();
        allStars = new ArrayList<>();
        allColors = new ArrayList<>();

        if (query != null){
            String uncappedQuery = query.toLowerCase();

            List<String> titles = dataAdapter.getAllTitles();
            List<String> createdAt = dataAdapter.getAllCreatedAt();
            List<String> editedAt = dataAdapter.getAllEditedAt();
            List<Integer> stars = dataAdapter.getAllStars();
            List<Integer> colors = dataAdapter.getAllColors();

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
    }

    public void wipeDataArray(){
        groupDelete = false;
        allDeleteList = new ArrayList<>();

        mCatalog = new ArrayList<>();
        allCreatedAt = new ArrayList<>();
        allEditedAt = new ArrayList<>();
        allStars = new ArrayList<>();
        allColors = new ArrayList<>();
    }

    public void deleteNote(String title){
        int index = mCatalog.indexOf(title);
        if (index == -1) return; //This occurs when the passed string does not exist within the list.
        mCatalog.remove(index);
        allCreatedAt.remove(index);
        allEditedAt.remove(index);
        allStars.remove(index);
        allColors.remove(index);
    }

    public void addNote(String title, String createdAt, String editedAt, int starred, int colorIndex){
        mCatalog.add(0, title);
        allCreatedAt.add(0, createdAt);
        allEditedAt.add(0, editedAt);
        allStars.add(0, starred);
        allColors.add(0, colorIndex);
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
        //The very first holder is a placeholder for the recyclerview. It makes the notes show up a little below from the Action Bar.

            noteHolder.mBackground.setVisibility(View.INVISIBLE);
            noteHolder.mCloud.setVisibility(View.INVISIBLE);
            return; //Make it invisible and just stop. No need to customize rest of holder.
        } else if (noteHolder.mBackground.getVisibility() == View.INVISIBLE){
        //Other indices: make sure it is visible.

            noteHolder.mBackground.setVisibility(View.VISIBLE);
            //The mCloud is only need to be visible if the note header is a cloud header.
        }

        //Setting title.
        String title = mCatalog.get(index-1);
        noteHolder.mTitle.setText(title);

        //If edited today, show the time. If edited before yesterday, show date.
        String editedAt = allEditedAt.get(index-1);
        String editDate = editedAt.substring(0,editedAt.indexOf(" "));
        if (editDate.equals(date)) noteHolder.mEdited.setText("Today, "+editedAt.substring(editDate.length()).trim());
        else                       noteHolder.mEdited.setText(editDate);

        //Setting favorites status.
        if (allStars.get(index-1)==1)
            if (allColors.get(index-1)==0) noteHolder.mStar.setBackgroundResource(R.drawable.ic_stars_white_36dp_alternative);
            else                           noteHolder.mStar.setBackgroundResource(R.drawable.ic_stars_white_36dp);
        else                        noteHolder.mStar.setBackgroundResource(R.drawable.ic_star_border_white_24dp);


        //Setting style depending on group delete modes.
        if (groupDelete && allDeleteList.contains(title)){
        //Currently in group delete mode AND This note is candidate for group delete

                noteHolder.mBackground.setBackgroundColor(COLOR_ARRAY[10]);
                noteHolder.mCloud.setVisibility(View.INVISIBLE);
                noteHolder.mTitle.setTextColor(Color.WHITE);
                noteHolder.mEdited.setTextColor(Color.WHITE);

                noteHolder.mBackground.setScaleX((float) .9);
                noteHolder.mBackground.setScaleY((float) .9);

        } else {
        //Not a group delete mode NOR a candidate for deletion

                if (noteHolder.mBackground.getScaleX() != 1){
                    //Always be full size when not in group delete mode or is not selected.
                    noteHolder.mBackground.setScaleX(1);
                    noteHolder.mBackground.setScaleY(1);
                }

                int colorIndex = allColors.get(index-1);
                if (colorIndex == 8) { //Image background.
                    noteHolder.mBackground.setBackgroundColor(Color.parseColor("#333333"));
                    //////////////////////////////////////////////////////////////////////////////////////////////Change this to photo later.
                    noteHolder.mTitle.setTextColor(Color.WHITE);
                    noteHolder.mEdited.setTextColor(Color.WHITE);
                } else { //Other background
                    noteHolder.mBackground.setBackgroundColor(COLOR_ARRAY[colorIndex]);
                    noteHolder.mTitle.setTextColor(COLOR_ARRAY[9]);
                    noteHolder.mEdited.setTextColor(COLOR_ARRAY[9]);
                }
                //The mCloud is only need to be visible if the note header is a cloud header.
                if (colorIndex == 0) noteHolder.mCloud.setVisibility(View.VISIBLE);
                else                 noteHolder.mCloud.setVisibility(View.INVISIBLE);

            //End of styling
        }
    }

    @Override
    public int getItemCount() { return mCatalog.size() + 1; }

    class NoteHolder extends RecyclerView.ViewHolder{ //Don't make it private since it must access the upper class' variables.

        ImageView mCloud;
        RelativeLayout mBackground;
        TextView mTitle;
        TextView mEdited;
        ImageView mStar;

        public NoteHolder(View itemView) {
            super(itemView);

            mCloud = (ImageView) itemView.findViewById(R.id.default_image);
            mBackground = (RelativeLayout) itemView.findViewById(R.id.note_background);
            mTitle = (TextView) itemView.findViewById(R.id.note_title);
            mEdited = (TextView) itemView.findViewById(R.id.textView9);
            mStar = (ImageView) itemView.findViewById(R.id.imageView4);

            mBackground.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    //Make the touch effect depending on API level.
                    return false;
                }
            });
            mBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (groupDelete) {
                    //Group-delete mode makes notes go checked or unchecked.

                                    String title = mTitle.getText().toString();
                                    if (allDeleteList.contains(title)){
                                    //Uncheck
                                                allDeleteList.remove(title);

                                                int colorIndex = allColors.get(getAdapterPosition()-1);
                                                if (colorIndex == 8) { //Image background.
                                                    mBackground.setBackgroundColor(Color.parseColor("#333333"));
                                                    //////////////////////////////////////////////////////////////////////Change this to photo later.
                                                } else {
                                                    mBackground.setBackgroundColor(COLOR_ARRAY[colorIndex]);
                                                    mTitle.setTextColor(COLOR_ARRAY[9]);
                                                    mEdited.setTextColor(COLOR_ARRAY[9]);
                                                }
                                                if (colorIndex == 0) mCloud.setVisibility(View.VISIBLE);
                                                mBackground.setScaleX(1);
                                                mBackground.setScaleY(1);
                                                mBackground.startAnimation(AnimationUtils.loadAnimation(context, R.anim.group_delete_disselect));
                                    } else {
                                    //Check
                                                allDeleteList.add(title);
                                                mBackground.setBackgroundColor(COLOR_ARRAY[10]);
                                                mTitle.setTextColor(Color.WHITE);
                                                mEdited.setTextColor(Color.WHITE);
                                                mCloud.setVisibility(View.INVISIBLE);

                                                mBackground.setScaleX((float) .9);
                                                mBackground.setScaleY((float) .9);
                                                mBackground.startAnimation(AnimationUtils.loadAnimation(context, R.anim.group_delete_candidate));
                                    }

                    } else {
                    //Non group-delete mode simply takes the user to the next activity
                                    if (Library.locked) return; //But don't do ANYTHING if app is still locked.
                                    Intent intent = new Intent(context, ViewNote.class);
                                    intent.putExtra("index", getAdapterPosition()-1);
                                    intent.putExtra("AdapterType",adapterType);
                                    context.startActivity(intent);
                    }
                }
            });
            mBackground.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {//Initiates group delete mode.
                    if (Library.locked) return true; //But don't do ANYTHING if app is still locked.

                    //If it's first time initiating delete mode, show delete button.
                    if (!groupDelete) if (adapterType == 1) Library.deleteButton.show();
                                      else                  ColorLibrary.deleteButton.show();
                    groupDelete = true;

                    String title = mTitle.getText().toString();
                    if (!allDeleteList.contains(title)){ //First time being a delete candidate? Execute following code.
                        allDeleteList.add(title);
                        mBackground.setBackgroundColor(COLOR_ARRAY[10]);
                        mTitle.setTextColor(Color.WHITE);
                        mEdited.setTextColor(Color.WHITE);
                        mCloud.setVisibility(View.INVISIBLE);

                        mBackground.setScaleX((float) .9);
                        mBackground.setScaleY((float) .9);
                        mBackground.startAnimation(AnimationUtils.loadAnimation(context, R.anim.group_delete_candidate));
                    }
                    return true; //Return true to prevent onClick from executing.
                }
            });

            mStar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Library.locked || groupDelete) return ; //Users cannot change favorites while app is locked or group selecting.

                    int index = getAdapterPosition()-1;
                    int starred = allStars.get(index);

                    //Update adapter and database favorites
                    dataAdapter.updateFavorites(mTitle.getText().toString(), 1 - starred);
                    allStars.set(index, 1 - starred);

                    if (starred==1){ //Unfavoriting
                        mStar.setBackgroundResource(R.drawable.ic_star_border_white_24dp);
                    }
                    else {           //Favoriting
                        if (allColors.get(index)==0) mStar.setBackgroundResource(R.drawable.ic_stars_white_36dp_alternative);
                        else                         mStar.setBackgroundResource(R.drawable.ic_stars_white_36dp);
                    }

                }
            });
        }
    }
}