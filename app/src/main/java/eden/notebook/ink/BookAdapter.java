package eden.notebook.ink;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.NoteHolder> {

    static final int[] COLOR_ARRAY = new int[]{
            Color.parseColor("#F5F5F5"),   //0 Default cloud (= dark tint)
            Color.parseColor("#FFC6D9"),   //1 Pink
            Color.parseColor("#FFCC80"),   //2 Orange
            Color.parseColor("#FFF59D"),   //3 Yellow
            Color.parseColor("#DCEDC8"),   //4 Green
            Color.parseColor("#B3E5FC"),   //5 Blue
            Color.parseColor("#C5CAE9"),   //6 Indigo
            Color.parseColor("#E1BEE7"),   //7 Purple
            Color.parseColor("#888888"),   //8 Image
            Color.parseColor("#333333"),   //9 Dark font
            Color.parseColor("#666666")};  //10 Delete selection
    Activity context;
    NoteDatabaseAdapter dataAdapter;
    private LayoutInflater inflater;

    // Small holder layout info.
    private Calendar calendar;
    private String date;
    int columns;
    private int holderType;

    // Group delete mode variables
    boolean groupDelete;
    List<String> allDeleteList;

    // Note detail info variables.
    static List<String> mCatalog;
    static List<String> allCreatedAt;
    static List<String> allEditedAt;
    static List<Integer> allStars;
    static List<Integer> allColors;

    public BookAdapter (Activity activity) {

        context = activity;
        dataAdapter = new NoteDatabaseAdapter(activity);
        inflater = LayoutInflater.from(activity);

        calendar = Calendar.getInstance();
        getDate();
        columns = context.getSharedPreferences("EdenNotebookSettings",Context.MODE_PRIVATE).getInt("Columns",1);
        holderType = context.getSharedPreferences("EdenNotebookSettings",Context.MODE_PRIVATE).getInt("Holder",0);

        initializeDataArray();
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

        if (colorIndex < 0 || colorIndex > 8) {
            Toast.makeText(context, R.string.no_color_type, Toast.LENGTH_SHORT).show();
            colorIndex = 0;
        }

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

        if (!query.isEmpty()){ // Checks for null strings.
            query = query.trim(); // Checks for whitespaces.
                if (query.length() == 0) return;
            String uncappedQuery = query.toLowerCase();
            String cappedQuery = query.toUpperCase();

            List<String> titles = dataAdapter.getAllTitles();
            List<String> createdAt = dataAdapter.getAllCreatedAt();
            List<String> editedAt = dataAdapter.getAllEditedAt();
            List<Integer> stars = dataAdapter.getAllStars();
            List<Integer> colors = dataAdapter.getAllColors();

            for (int i = 0 ; i<titles.size() ; i++){
                String filename = titles.get(i);
                String content = "";
                try { // Obtaining byte string info from filename.
                    FileInputStream fis = context.openFileInput(filename);
                    byte[] data         = new byte[fis.available()];

                    if (fis.read(data) != 0) while (fis.read(data) != -1) {} // For comments about this line, check the NoteFragment class.
                    content = new String(data);
                    fis.close();                                                }
                catch (Exception e) {/*Don't do anything. Just a query.*/}
                if (filename.contains(query) || content.contains(query)
                        || filename.contains(uncappedQuery) || content.contains(uncappedQuery)
                            || filename.contains(cappedQuery) || content.contains(cappedQuery)){
                    mCatalog.add(titles.get(i));
                    allCreatedAt.add(createdAt.get(i));
                    allEditedAt.add(editedAt.get(i));
                    allStars.add(stars.get(i));
                    allColors.add(colors.get(i));
                }
            }
        }
    }

    public void initializeFavorites(){

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

        for (int i = 0 ; i<titles.size() ; i++){
            if (stars.get(i) == 1){
                mCatalog.add(titles.get(i));
                allCreatedAt.add(createdAt.get(i));
                allEditedAt.add(editedAt.get(i));
                allStars.add(stars.get(i));
                allColors.add(colors.get(i));
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

    public static void deleteNote(final String title){
        int index = mCatalog.indexOf(title);
        if (index == -1) return; //This occurs when the passed string does not exist within the list.
        mCatalog.remove(index);
        allCreatedAt.remove(index);
        allEditedAt.remove(index);
        allStars.remove(index);
        allColors.remove(index);
    }

    public static void addNote(String title, String createdAt, String editedAt, int starred, int colorIndex){
        mCatalog.add(0, title);
        allCreatedAt.add(0, createdAt);
        allEditedAt.add(0, editedAt);
        allStars.add(0, starred);
        allColors.add(0, colorIndex);
    }

    public void getDate(){

        String month = String.valueOf(calendar.get(Calendar.MONTH)+1);
        String day = String.valueOf(calendar.get(Calendar.DATE));

        if (month.length() == 1)
            month = "0"+month;
        if (day.length() == 1)
            day = "0"+day;

        date = String.valueOf(calendar.get(Calendar.YEAR))+"/"+
                month+"/"+
                day;
    }

    @Override
    public NoteHolder onCreateViewHolder(ViewGroup viewGroup, int i){
        if (holderType == 0)
            return new NoteHolder(inflater.inflate(R.layout.film_holder, viewGroup, false));
        else
            return new NoteHolder(inflater.inflate(R.layout.film_holder_filled, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(NoteHolder noteHolder, int index) {

        if (index < columns){ // The very first holder is a placeholder for the recyclerview. It makes the notes show up a little below from the Action Bar.
            noteHolder.mBackground.setVisibility(View.GONE); return; //Make it invisible and just stop. No need to customize rest of holder.
        } else // Other indices: make sure it is visible.
            noteHolder.mBackground.setVisibility(View.VISIBLE);

        //Setting title.
        index = index - columns;
        String title = mCatalog.get(index);
        noteHolder.mTitle.setText(title);

        //If edited today, show the time. If edited before yesterday, show date.
        String editedAt = allEditedAt.get(index);
        String editDate = editedAt.substring(0,editedAt.indexOf(" "));
        if (editDate.equals(date)) noteHolder.mEdited.setText(context.getResources().getString(R.string.today) + editedAt.substring(editDate.length() + 1));
        else                       noteHolder.mEdited.setText(editDate);

        //Setting style depending on group delete modes.
        if (groupDelete && allDeleteList.contains(title)){
        //Currently in group delete mode AND This note is candidate for group delete

                noteHolder.mBackground.setBackgroundColor(COLOR_ARRAY[10]);
                noteHolder.mPhotoTint.setVisibility(View.GONE);
                noteHolder.mCloud.setVisibility(View.GONE);
                noteHolder.mTitle.setTextColor(Color.WHITE);
                noteHolder.mEdited.setTextColor(Color.WHITE);
                noteHolder.mStar.setBackgroundResource(R.drawable.ic_star_border_white_24dp);

                noteHolder.mBackground.setScaleX((float) .9);
                noteHolder.mBackground.setScaleY((float) .9);

        } else {
        //Not a group delete mode NOR a candidate for deletion (= show color category)

                //Always be full size when not in group delete mode nor is not selected.
                noteHolder.mBackground.setScaleX(1);
                noteHolder.mBackground.setScaleY(1);

                int colorIndex = allColors.get(index);
                if (colorIndex == 8) { //Image background.
                    try {
                    noteHolder.mBackground.setBackground(new BitmapDrawable(context.getResources(), BitmapFactory
                            .decodeFile(new File(context.getFilesDir(), title + "AG5463#$1!#$&").getAbsolutePath(),
                                    new BitmapFactory.Options())/*<-Creating bitmap image*/) /*<-Creating drawable(int id) from bitmap*/ );
                    noteHolder.mPhotoTint.setVisibility(View.VISIBLE); }
                        catch (Exception e) {
                            noteHolder.mBackground.setBackgroundColor(BookAdapter.COLOR_ARRAY[9]);
                            noteHolder.mPhotoTint.setVisibility(View.GONE);
                            Toast.makeText(context, "onBindViewHolder: "+e.toString() , Toast.LENGTH_SHORT).show(); }
                    noteHolder.mTitle.setTextColor(Color.WHITE);
                    noteHolder.mEdited.setTextColor(Color.WHITE);
                } else { //Other background
                    noteHolder.mBackground.setBackgroundColor(COLOR_ARRAY[colorIndex]);
                    noteHolder.mPhotoTint.setVisibility(View.GONE);
                    noteHolder.mTitle.setTextColor(COLOR_ARRAY[9]);
                    noteHolder.mEdited.setTextColor(COLOR_ARRAY[9]);
                }

                //Setting favorites status.
                if (allStars.get(index)==1)
                    if (colorIndex==0) noteHolder.mStar.setBackgroundResource(R.drawable.ic_stars_white_36dp_alternative);
                    else               noteHolder.mStar.setBackgroundResource(R.drawable.ic_stars_white_36dp);
                else              noteHolder.mStar.setBackgroundResource(R.drawable.ic_star_border_white_24dp);

                //The mCloud is only need to be visible if the note header is a cloud header.
                if (colorIndex == 0) noteHolder.mCloud.setVisibility(View.VISIBLE);
                else                 noteHolder.mCloud.setVisibility(View.GONE);

            //End of styling
        }
    }

    @Override
    public int getItemCount() { return mCatalog.size() + columns; }

    class NoteHolder extends RecyclerView.ViewHolder{ //Don't make it private since it must access the upper class' variables.

        RelativeLayout mBackground;
        ImageView mPhotoTint;
        ImageView mCloud;
        TextView mTitle;
        TextView mEdited;
        ImageView mStar;

        public NoteHolder(View itemView) {
            super(itemView);

            mBackground = (RelativeLayout) itemView.findViewById(R.id.note_background);
            mPhotoTint = (ImageView) itemView.findViewById(R.id.photo_tint);
            mCloud = (ImageView) itemView.findViewById(R.id.default_image);
            mTitle = (TextView) itemView.findViewById(R.id.note_title);
            mEdited = (TextView) itemView.findViewById(R.id.textView9);
            mStar = (ImageView) itemView.findViewById(R.id.imageView4);

            mBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int adapterIndex = getAdapterPosition() - columns;

                    if (groupDelete) {
                    //Group-delete mode makes notes go checked or unchecked.

                                    String title = mTitle.getText().toString();
                                    if (allDeleteList.contains(title)){
                                    //Uncheck
                                                allDeleteList.remove(title);

                                                int colorIndex = allColors.get(adapterIndex);
                                                if (colorIndex == 8) { //Image background.
                                                    try {
                                                    mBackground.setBackground(new BitmapDrawable(context.getResources(),/*<-creating drawable*/
                                                                                                 BitmapFactory /*<-creating bitmap*/
                                                                                                         .decodeFile(new File(context.getFilesDir(), title + "AG5463#$1!#$&")
                                                                                                                 .getAbsolutePath(), new BitmapFactory.Options())));
                                                    mPhotoTint.setVisibility(View.VISIBLE); }
                                                            catch (Exception e) {
                                                            mBackground.setBackgroundColor(BookAdapter.COLOR_ARRAY[9]);
                                                            Toast.makeText(context, e.toString() , Toast.LENGTH_SHORT).show(); }
                                                } else { //Color Background
                                                    mBackground.setBackgroundColor(COLOR_ARRAY[colorIndex]);
                                                    mTitle.setTextColor(COLOR_ARRAY[9]);
                                                    mEdited.setTextColor(COLOR_ARRAY[9]);
                                                }

                                                //Setting favorites status.
                                                if (allStars.get(adapterIndex)==1)
                                                    if (colorIndex==0) mStar.setBackgroundResource(R.drawable.ic_stars_white_36dp_alternative);
                                                    else               mStar.setBackgroundResource(R.drawable.ic_stars_white_36dp);
                                                else               mStar.setBackgroundResource(R.drawable.ic_star_border_white_24dp);

                                                if (colorIndex == 0) mCloud.setVisibility(View.VISIBLE);
                                                mBackground.setScaleX(1);
                                                mBackground.setScaleY(1);
                                                mBackground.startAnimation(AnimationUtils.loadAnimation(context, R.anim.group_delete_disselect));
                                    } else {
                                    //Check
                                                allDeleteList.add(title);
                                                mBackground.setBackgroundColor(COLOR_ARRAY[10]);
                                                mPhotoTint.setVisibility(View.GONE);
                                                mCloud.setVisibility(View.GONE);
                                                mTitle.setTextColor(Color.WHITE);
                                                mEdited.setTextColor(Color.WHITE);
                                                mStar.setBackgroundResource(R.drawable.ic_star_border_white_24dp);

                                                mBackground.setScaleX((float) .9);
                                                mBackground.setScaleY((float) .9);
                                                mBackground.startAnimation(AnimationUtils.loadAnimation(context, R.anim.group_delete_candidate));
                                    }

                    } else {
                    //Non-group-delete mode simply takes the user to the next activity
                                    if (Library.locked) return; //But don't do ANYTHING if app is still locked.
                                    Intent intent = new Intent(context, ViewNote.class);
                                    intent.putExtra("index", adapterIndex);
                                    context.startActivity(intent);
                    }
                }
            });
            mBackground.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {//Initiates group delete mode.
                    if (Library.locked) return true; //But don't do ANYTHING if app is still locked.

                    if (!groupDelete) Library.deleteButton.show();
                    groupDelete = true;

                    String title = mTitle.getText().toString();
                    if (!allDeleteList.contains(title)){ //First time being a delete candidate? Execute following code.
                        allDeleteList.add(title);
                        mBackground.setBackgroundColor(COLOR_ARRAY[10]);
                        mPhotoTint.setVisibility(View.GONE);
                        mCloud.setVisibility(View.GONE);
                        mTitle.setTextColor(Color.WHITE);
                        mEdited.setTextColor(Color.WHITE);
                        mStar.setBackgroundResource(R.drawable.ic_star_border_white_24dp);

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
                    if (Library.locked || groupDelete)
                        return; //Users cannot change favorites while app is locked or group selecting.

                    int index   = getAdapterPosition() - columns;
                    int starred = allStars.get(index);

                    if (starred == 1) { //Unfavoriting

                        //Update adapter and database favorites
                        dataAdapter.updateFavorites(mTitle.getText().toString(), 1 - starred);
                        allStars.set(index, 1 - starred);
                        if (!dataAdapter.hasFavorites()) // If this was the last item unfavorited, remove the "Favorites" Category from the drawer.
                            Library.mAdapter.notifyItemChanged(2);

                        mStar.setBackgroundResource(R.drawable.ic_star_border_white_24dp);

                    } else { //Favoriting

                        //Update adapter and database favorites
                        if (!dataAdapter.hasFavorites()) // If this was the first item favorited, add the "Favorites" Category on the drawer.
                            Library.mAdapter.notifyItemChanged(2);
                        dataAdapter.updateFavorites(mTitle.getText().toString(), 1 - starred);
                        allStars.set(index, 1 - starred);

                        if (allColors.get(index) == 0)
                            mStar.setBackgroundResource(R.drawable.ic_stars_white_36dp_alternative);
                        else
                            mStar.setBackgroundResource(R.drawable.ic_stars_white_36dp);
                    }
                }
            });
        }
    }
}