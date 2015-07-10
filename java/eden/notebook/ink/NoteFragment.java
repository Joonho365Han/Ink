package eden.notebook.ink;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class NoteFragment extends Fragment {

    int mFileIndex;
    private TextView title;
    private TextView content;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_viewnote, container, false);
        title = (TextView) layout.findViewById(R.id.view_title);
        content = (TextView) layout.findViewById(R.id.view_content);
        context = container.getContext();
        TextView count = (TextView) layout.findViewById(R.id.fragment_page);
        count.setText(String.valueOf(mFileIndex+1) + "/" + String.valueOf(BookAdapter.mCatalog.size()));
        return layout;
    }

    @Override
    public void onStart(){
        super.onStart();

        //Setting title text.
        String filename = BookAdapter.mCatalog.get(mFileIndex);
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
        catch (FileNotFoundException e){ Toast.makeText(context, "Error: File does not exist.", Toast.LENGTH_SHORT).show(); }
        catch (IOException e)          { Toast.makeText(context, "Error: Failed to extract note from storage.", Toast.LENGTH_SHORT).show(); }
    }
}
