package eden.notebook.ink;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import java.io.File;

public class PhotoViewer extends Activity{

    ImageButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getWindow().setSharedElementEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.enlarge_photo_transition));
            getWindow().setSharedElementReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.shrink_photo_transition));
        }
        setContentView(R.layout.activity_photo_viewer);

        button = (ImageButton) findViewById(R.id.imageButton);
        button.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
                if (i == View.SYSTEM_UI_FLAG_VISIBLE)
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                                button.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                            else
                                button.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                        }
                    }, 3000);
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE); }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() { new SetBitmapTask().execute(getIntent().getStringExtra("filename")); }
        }, 500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            button.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        else
            button.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @Override
    public void onBackPressed(){
        button.setImageDrawable(null);
        super.onBackPressed();
    }

    class SetBitmapTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            return BitmapFactory.decodeFile(new File(getFilesDir(), strings[0] + "@#$^23!^").getAbsolutePath());
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null)
                new AlertDialog.Builder(PhotoViewer.this)
                        .setTitle(R.string.error)
                        .setMessage(R.string.empty_photo)
                        .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        }).create().show();
            else if (button != null){
                button.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
                button.setBackground(new BitmapDrawable(getResources(),StackBlur.blur(Bitmap.createScaledBitmap(bitmap, 200, 40, true),10)));
            }
        }
    }
}