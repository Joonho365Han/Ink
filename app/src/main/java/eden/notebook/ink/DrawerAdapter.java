package eden.notebook.ink;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ItemHolder> {

    Activity context;
    private LayoutInflater inflater;
    private NoteDatabaseAdapter dataAdapter;
    private SharedPreferences prefs;

    private int ITEM_HEIGHT_PX;
    private static final int[] CATEGORY_ICON_RES_ID = new int[]{
            R.drawable.ic_dashboard_white_24dp,
            R.drawable.ic_star_white_24dp,
            R.drawable.ic_cloud_category,
            R.drawable.ic_pink_category,
            R.drawable.ic_orange_category,
            R.drawable.ic_yellow_category,
            R.drawable.ic_green_category,
            R.drawable.ic_blue_category,
            R.drawable.ic_indigo_category,
            R.drawable.ic_purple_category,
            R.drawable.ic_image_category};
    private static final String[] COLOR_CATEGORY_TITLE = new String[]{"Cloud","Pink","Orange","Yellow","Green","Blue","Indigo","Purple","Image"};
    static final String[] ACTIVITY_TITLE = new String[11];

    public DrawerAdapter (Activity activity) {
        context = activity;
        inflater = LayoutInflater.from(activity);
        dataAdapter = new NoteDatabaseAdapter(context);
        prefs = context.getSharedPreferences("EdenNotebookSettings", Context.MODE_PRIVATE);

        ITEM_HEIGHT_PX = (int) (context.getResources().getDisplayMetrics().density * 48);
        Resources res = context.getResources();
        ACTIVITY_TITLE[0] = res.getString(R.string.library);
        ACTIVITY_TITLE[1] = res.getString(R.string.favorites);
        ACTIVITY_TITLE[2] = prefs.getString("Cloud",res.getString(R.string.cloud));
        ACTIVITY_TITLE[3] = prefs.getString("Pink",res.getString(R.string.pink));
        ACTIVITY_TITLE[4] = prefs.getString("Orange",res.getString(R.string.orange));
        ACTIVITY_TITLE[5] = prefs.getString("Yellow",res.getString(R.string.yellow));
        ACTIVITY_TITLE[6] = prefs.getString("Green",res.getString(R.string.green));
        ACTIVITY_TITLE[7] = prefs.getString("Blue",res.getString(R.string.blue));
        ACTIVITY_TITLE[8] = prefs.getString("Indigo",res.getString(R.string.indigo));
        ACTIVITY_TITLE[9] = prefs.getString("Purple",res.getString(R.string.purple));
        ACTIVITY_TITLE[10] = prefs.getString("Image",res.getString(R.string.image));
    }

    public void setCategoryTitle(final String oldTitle, final String newTitle, final int colorIndex){

        //User decided to decrypt app.
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_new_category_title, null);
        final EditText edittext = (EditText) layout.findViewById(R.id.editext);

        edittext.setText(newTitle);

        new AlertDialog.Builder(context)
                .setTitle(R.string.new_title)
                .setView(layout)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        List<String> existing = new ArrayList<>();
                        Collections.addAll(existing, ACTIVITY_TITLE);

                        String newTitle = edittext.getText().toString().trim();

                        if (newTitle.length() > 25){
                            Toast.makeText(context, R.string.long_category_title, Toast.LENGTH_SHORT).show();
                            setCategoryTitle(oldTitle,newTitle,colorIndex);
                        } else if (newTitle.length() == 0) {
                            Toast.makeText(context, R.string.empty_title, Toast.LENGTH_SHORT).show();
                            setCategoryTitle(oldTitle,newTitle,colorIndex);
                        } else if (existing.contains(newTitle) && !newTitle.equals(oldTitle)) {
                            //Test if file with same title already exists.
                            Toast.makeText(context, R.string.existing_title, Toast.LENGTH_SHORT).show();
                            setCategoryTitle(oldTitle,newTitle,colorIndex);
                        } else {
                            ACTIVITY_TITLE[colorIndex+2] = newTitle;
                            notifyItemChanged(colorIndex + 4);

                            if (!newTitle.equals(oldTitle)){
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString(COLOR_CATEGORY_TITLE[colorIndex], newTitle);
                                editor.apply();
                            }
                        }
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            //User did not enter password.
            @Override
            public void onClick(DialogInterface dialog, int which) {/*Don't do anything if user decided to cancel.*/}
        }).create().show();
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {return new ItemHolder(inflater.inflate(R.layout.drawer_item_holder, viewGroup, false));}

    @Override
    public void onBindViewHolder(ItemHolder noteHolder, int index) {
    // Make item visibility depending on existence of note type.

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) noteHolder.mBackground.getLayoutParams();

        if (index == 0) { // this just makes the upper part of recyclerview blank.
            params.height = ITEM_HEIGHT_PX * 7 / 6;
            noteHolder.mBackground.setLayoutParams(params);
            noteHolder.mBackground.setVisibility(View.GONE);
            return;
        }
        if (index == 13) { // bottom space holder. Make it blank.
            params.height = ITEM_HEIGHT_PX / 4;
            noteHolder.mBackground.setLayoutParams(params);
            noteHolder.mBackground.setVisibility(View.GONE);
            return;
        }

        // If non of above apply, make holder visible.
        noteHolder.mBackground.setVisibility(View.VISIBLE);

        index--;

        if (index == 2) { // Binding category title.
            params.height = ITEM_HEIGHT_PX * 3 / 4;
            noteHolder.mBackground.setLayoutParams(params);
            noteHolder.mBackground.setBackgroundResource(R.drawable.category_border);
            noteHolder.mIcon.setVisibility(View.GONE);
            noteHolder.mTitle.setText(R.string.category);
            noteHolder.mTitle.setTextColor(Color.GRAY);
            noteHolder.mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            return;
        } else { // Binding normal items.
            if (noteHolder.mIcon.getVisibility() == View.GONE)
                noteHolder.mIcon.setVisibility(View.VISIBLE);

            if (index == 1) { //Favorites
                if (!dataAdapter.hasFavorites()){ // This if statement must be separate.
                    params.height = 0;
                    noteHolder.mBackground.setLayoutParams(params);
                    return; // Skip binding content.
                }
            } else if (index != 0) {  // Header types.
                index--;
                if (!dataAdapter.hasColor(index-2)){
                    params.height = 0;
                    noteHolder.mBackground.setLayoutParams(params);
                    return; // Skip binding content.
                }
            }
        }

        // Making item visible.
        params.height = ITEM_HEIGHT_PX;
        noteHolder.mBackground.setLayoutParams(params);
        noteHolder.mBackground.setBackground(null);

        // Binding category items.
        noteHolder.mIcon.setBackgroundResource(CATEGORY_ICON_RES_ID[index]);
        noteHolder.mTitle.setText(ACTIVITY_TITLE[index]);
        noteHolder.mTitle.setTextColor(BookAdapter.COLOR_ARRAY[9]);
        noteHolder.mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
    }

    @Override
    public int getItemCount() { return 14; }

    class ItemHolder extends RecyclerView.ViewHolder{ //Don't make it private since it must access the upper class' variables.

        LinearLayout mBackground;
        ImageView mIcon;
        TextView mTitle;

        public ItemHolder(View itemView) {
            super(itemView);

            mBackground = (LinearLayout) itemView.findViewById(R.id.item_background);
            mIcon = (ImageView) itemView.findViewById(R.id.item_icon);
            mTitle = (TextView) itemView.findViewById(R.id.item_text);

            mBackground.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    int action = motionEvent.getAction();

                    int index = getAdapterPosition() - 1;
                    if (index == 2)
                        return true; // Consume the touch event. Don't do anything with the category title.

                    if (action == MotionEvent.ACTION_DOWN)
                        view.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP)
                        view.setBackground(null);
                    return false;
                }
            });
            mBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int index = getAdapterPosition() - 1;

                    Intent intent = new Intent(context, Library.class);
                    if (index == 1)
                        intent.putExtra("Favorites", -1);
                    else if (index != 0)
                        intent.putExtra("Category", index - 3);

                    context.startActivity(intent);
                }
            });
            mBackground.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    int colorIndex = getAdapterPosition() - 4;
                    if (colorIndex < 0) // Don't change the titles of library and favorites.
                        return false; // But don't consume the event.


                    setCategoryTitle(mTitle.getText().toString(), mTitle.getText().toString(), colorIndex);
                    return true; // Always consume the event when title changed.
                }
            });
        }
    }
}