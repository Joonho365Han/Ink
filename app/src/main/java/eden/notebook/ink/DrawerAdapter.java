package eden.notebook.ink;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ItemHolder> {

    Activity context;
    private LayoutInflater inflater;
    private NoteDatabaseAdapter dataAdapter;

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
    static final int[] ACTIVITY_TITLE_RES_ID = new int[]{
            R.string.library,
            R.string.favorites,
            R.string.cloud,
            R.string.pink,
            R.string.orange,
            R.string.yellow,
            R.string.green,
            R.string.blue,
            R.string.indigo,
            R.string.purple,
            R.string.image};

    public DrawerAdapter (Activity activity) {
        context = activity;
        inflater = LayoutInflater.from(activity);
        dataAdapter = new NoteDatabaseAdapter(context);

        ITEM_HEIGHT_PX = (int) (context.getResources().getDisplayMetrics().density * 48);
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i)
        {return new ItemHolder(inflater.inflate(R.layout.drawer_item_holder, viewGroup, false));}

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
        noteHolder.mTitle.setText(ACTIVITY_TITLE_RES_ID[index]);
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

                    int index = getAdapterPosition() - 1;
                    if (index == 2)
                        return true; // Don't do anything with the category title.

                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                        view.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        Intent intent = new Intent(context, Library.class);
                        if (index == 1)
                            intent.putExtra("Favorites", -1);
                        else if (index != 0)
                            intent.putExtra("Category", index - 3);
                        context.startActivity(intent);
                        view.setBackground(null);
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_CANCEL)
                        view.setBackground(null);
                    return true;
                }
            });
        }
    }
}