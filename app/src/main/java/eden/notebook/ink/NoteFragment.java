package eden.notebook.ink;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;

public class NoteFragment extends Fragment {

    //Info of which adapter it should retrieve data from.
    int mFileIndex;

    //Layout info.
    private RelativeLayout colorButton;
    private ImageView cloudHeader;
    private TextView title;
    private TextView content;
    private TextView edited;
    private FrameLayout elevation;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ScrollView layout = (ScrollView) inflater.inflate(R.layout.fragment_viewnote, container, false);
        context = container.getContext();

        colorButton = (RelativeLayout) layout.findViewById(R.id.color_button);
        cloudHeader = (ImageView) layout.findViewById(R.id.cloud_header);
        title = (TextView) layout.findViewById(R.id.view_title);
        content = (TextView) layout.findViewById(R.id.view_content);
        edited = (TextView) layout.findViewById(R.id.view_edited);
        elevation = (FrameLayout) layout.findViewById(R.id.elevation);

        TextView created = (TextView) layout.findViewById(R.id.view_created);
        created.setText(getResources().getString(R.string.created_at) + BookAdapter.allCreatedAt.get(mFileIndex));

        return layout;
    }

    @Override
    public void onResume(){
        super.onResume();
        SharedPreferences preferences = context.getSharedPreferences("EdenNotebookSettings",Context.MODE_PRIVATE);

        int colorIndex;
        final String filename;

        //Retrieving note info.
        filename = BookAdapter.mCatalog.get(mFileIndex);
        edited.setText( getResources().getString(R.string.last_edited) + BookAdapter.allEditedAt.get(mFileIndex));
        colorIndex = BookAdapter.allColors.get(mFileIndex);

        //Setting header style.
        if (colorIndex == 0) { //Default background.

            //THE HEADER
            colorButton.setBackground(null);
            colorButton.setClickable(false);
            elevation.setBackground(null);

            cloudHeader.setVisibility(View.VISIBLE);
            title.setTextColor(BookAdapter.COLOR_ARRAY[9]);
        } else {

            //THE HEADER
            if (colorIndex == 8){ //Image as background

                try {
                    colorButton.setBackground(new BitmapDrawable(getResources(), BitmapFactory
                            .decodeFile(new File(context.getFilesDir(), filename + "AG5463#$1!#$&")
                                            .getAbsolutePath(),
                                    new BitmapFactory.Options())/*<-Creating bitmap image*/) /*<-Creating drawable(int id) from bitmap*/);
                    colorButton.setClickable(true);
                    colorButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, PhotoViewer.class);
                            intent.putExtra("filename", filename);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                v.setTransitionName("enlargePhoto");
                                ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), v, "enlargePhoto");
                                context.startActivity(intent, compat.toBundle());
                            } else {
                                startActivity(intent);
                            }
                        }
                    });
                    elevation.setBackgroundResource(R.drawable.elevation);

                } catch (Exception e) {
                    colorButton.setBackgroundColor(BookAdapter.COLOR_ARRAY[9]);
                    colorButton.setClickable(false);
                    elevation.setBackgroundResource(R.drawable.elevation);
                    Toast.makeText(context, getResources().getString(R.string.image_lookup_fail) + e.toString() , Toast.LENGTH_SHORT).show();
                }

            } else { //Color background
                colorButton.setBackgroundColor(BookAdapter.COLOR_ARRAY[colorIndex]);
                colorButton.setClickable(false);
                elevation.setBackground(null);
            }

            cloudHeader.setVisibility(View.GONE);
            title.setTextColor(Color.WHITE);
        }

        //Setting text style.
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, preferences.getInt("Title", 44));
        content.setTextSize(TypedValue.COMPLEX_UNIT_SP, preferences.getInt("Content", 24));
        if (preferences.getBoolean("Serif",false)) { title.setTypeface(Typeface.SERIF);       content.setTypeface(Typeface.SERIF);      }
        else                                       { title.setTypeface(Typeface.SANS_SERIF);  content.setTypeface(Typeface.SANS_SERIF); }

        //Setting title text.
        title.setText(filename);

        //Setting content text.
        try {
            //Obtaining byte string info from filename.
            FileInputStream fis = context.openFileInput(filename);
            byte[] data = new byte[fis.available()];
            if (fis.read(data) != 0) // Sometimes if the saved string is nothing (""), then the read()
                // will constantly return 0 and fall into the constant while loop.
                // Must make sure there is something to read() before proceeding.
                while (fis.read(data) != -1) { /*This loop constantly extracts byte that will
                                                 eventually be converted to a string byte by byte.*/ }

            content.setText(new String(data));
            fis.close();                                                                       }
        catch (Exception e){ Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show(); }
    }
}
