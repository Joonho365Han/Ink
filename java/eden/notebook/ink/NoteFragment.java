package eden.notebook.ink;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class NoteFragment extends Fragment {

    int mFileIndex;
    int adapterType;
    private LinearLayout colorButton;
    private TextView title;
    private TextView content;
    private TextView edited;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ScrollView layout = (ScrollView) inflater.inflate(R.layout.fragment_viewnote, container, false);
        context = container.getContext();

        colorButton = (LinearLayout) layout.findViewById(R.id.color_button);
        title = (TextView) layout.findViewById(R.id.view_title);
        content = (TextView) layout.findViewById(R.id.view_content);
        edited = (TextView) layout.findViewById(R.id.view_edited);

        TextView created = (TextView) layout.findViewById(R.id.view_created);
        if (adapterType == 1)
            created.setText("Created at    "+Library.adapter.allCreatedAt.get(mFileIndex));
        else
            created.setText("Created at    "+ColorLibrary.adapter.allCreatedAt.get(mFileIndex));

        return layout;
    }

    @Override
    public void onStart(){
        super.onStart();
        SharedPreferences preferences = context.getSharedPreferences("EdenNotebookSettings",Context.MODE_PRIVATE);

        int colorIndex;
        String filename;

        if (adapterType == 1){
            //Setting edit date text.
            edited.setText("Last edited    "+Library.adapter.allEditedAt.get(mFileIndex));
            colorIndex = Library.adapter.allColors.get(mFileIndex);
            filename = Library.adapter.mCatalog.get(mFileIndex);
        } else {
            //Setting edit date text.
            edited.setText("Last edited    "+ColorLibrary.adapter.allEditedAt.get(mFileIndex));
            colorIndex = ColorLibrary.adapter.allColors.get(mFileIndex);
            filename = ColorLibrary.adapter.mCatalog.get(mFileIndex);
        }

        if (colorIndex == 0) {
            colorButton.setBackgroundResource(R.drawable.cloud_header);
            ////////////////////////////////////////////////////////////////////////////////////Change picture later.
            title.setTextColor(BookAdapter.COLOR_ARRAY[9]);
            title.setBackground(null);
        }
        else if (colorIndex == 8) { //Image as background
            colorButton.setBackgroundColor(Color.parseColor("#333333"));
            //////////////////////////////////////////////////////////////////////////////////// Change this later.
            colorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Focus on the image.
                }
            });
            title.setTextColor(Color.WHITE);
            title.setBackgroundResource(R.drawable.shadow_gradation);
        }
        else { //Color background
            colorButton.setBackgroundColor(BookAdapter.COLOR_ARRAY[colorIndex]);
            title.setTextColor(Color.WHITE);
            title.setBackgroundResource(R.drawable.shadow_gradation);
        }

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
            fis.close();                                                }
        catch (FileNotFoundException e){ Toast.makeText(context, "Error: File does not exist", Toast.LENGTH_SHORT).show(); }
        catch (IOException e)          { Toast.makeText(context, "Error: Failed to extract note from storage", Toast.LENGTH_SHORT).show(); }
    }
}
