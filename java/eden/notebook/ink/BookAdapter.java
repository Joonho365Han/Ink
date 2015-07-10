package eden.notebook.ink;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.NoteHolder> {

    private Context context;
    private LayoutInflater inflater;
    public static List<String> mCatalog;

    public BookAdapter (Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);

        mCatalog = new ArrayList<>();
        String[] catalog = context.getFilesDir().list();
        Collections.addAll(mCatalog, catalog);
        Collections.reverse(mCatalog);
    }

    @Override
    public NoteHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        NoteHolder noteHolder = new NoteHolder(inflater.inflate(R.layout.note_holder, viewGroup, false));
        noteHolder.mBackground.setBackgroundResource(R.drawable.book_note_holder);
        return noteHolder;
    }

    @Override
    public void onBindViewHolder(NoteHolder noteHolder, int index) {
        noteHolder.mTitle.setText(mCatalog.get(index));
        noteHolder.mDate.setText("Unknown");
    }

    @Override
    public int getItemCount() { return mCatalog.size(); }

    protected class NoteHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        LinearLayout mBackground;
        TextView mTitle;
        TextView mDate;

        public NoteHolder(View itemView) {
            super(itemView);
            mBackground = (LinearLayout) itemView.findViewById(R.id.note_background);
            mTitle = (TextView) itemView.findViewById(R.id.note_title);
            mDate = (TextView) itemView.findViewById(R.id.note_date);
            mBackground.setOnClickListener(this);

            //Make adjustments to shape the size and height of the note holder.
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, ViewNote.class);
            intent.putExtra("index",getAdapterPosition());
            context.startActivity(intent);
        }
    }

}
