package eden.notebook.ink;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Settings extends ActionBarActivity {

    private SharedPreferences prefs;
    private boolean isEncrypted;
    private String password;
    private boolean isBackedUp;
    private boolean isAskDelete;
    private boolean isAskCombine;
    private int titleFont;
    private int contentFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Initialize all variables.
        prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        isEncrypted = prefs.getBoolean("Encryption",false);
        password = prefs.getString("Password",null);
        isBackedUp = prefs.getBoolean("Backup",false);
        isAskDelete = prefs.getBoolean("Delete",true);
        isAskCombine = prefs.getBoolean("Combine",true);
        titleFont = prefs.getInt("Title",46);
        contentFont = prefs.getInt("Content",25);

        //Initializing encryption switch
        Switch toggle = (Switch) findViewById(R.id.switch1);
        toggle.setChecked(isEncrypted);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Switch s = (Switch) v;
                if (s.isChecked()){
                    //Warn about the password protectability.
                    new AlertDialog.Builder(Settings.this).setMessage(R.string.settings_password_ask)
                            .setTitle(R.string.note)
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                //User decided to encrypt
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    LinearLayout layout = (LinearLayout) Settings.this.getLayoutInflater().inflate(R.layout.dialog_password, null);
                                    final EditText edittext = (EditText) layout.findViewById(R.id.editext);
                                    new AlertDialog.Builder(Settings.this).setTitle(R.string.settings_password_set)
                                            .setView(layout)
                                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                                //User inputed password.
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    password = edittext.getText().toString();
                                                    isEncrypted = true;
                                                    s.setChecked(true);
                                                }
                                            }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                                //User did not enter password.
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {/*Don't do anything if user decided to cancel.*/}
                                            }).create().show();
                                }
                            }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        //User decided to not encrypt.
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    }).create().show();
                    s.setChecked(isEncrypted);
                } else {
                    //User decided to decrypt app.
                    LinearLayout layout = (LinearLayout) Settings.this.getLayoutInflater().inflate(R.layout.dialog_password, null);
                    final EditText edittext = (EditText) layout.findViewById(R.id.editext);
                    new AlertDialog.Builder(Settings.this).setTitle(R.string.settings_password_decrypt)
                            .setView(layout)
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                //User inputed password.
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (edittext.getText().toString()
                                            .equals(password)){
                                        isEncrypted = false;
                                         s.setChecked(false);//Password was correct.
                                    } else {
                                        //Password was wrong.
                                        new AlertDialog.Builder(Settings.this)
                                                .setMessage(R.string.wrong_password)
                                                .setTitle(R.string.error)
                                                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {}
                                                }).create().show();
                                    }
                                }
                            }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        //User did not enter password.
                        @Override
                        public void onClick(DialogInterface dialog, int which) {/*Don't do anything if user decided to cancel.*/}
                    }).create().show();
                    s.setChecked(isEncrypted);
                }
            }
        });

        //Initializing restore button.
        final Button button = (Button) findViewById(R.id.button);
        if (isBackedUp) button.setTextColor(Color.WHITE);
        else            button.setTextColor(Color.GRAY);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBackedUp)
                new AlertDialog.Builder(Settings.this)
                        .setTitle(R.string.caution)
                        .setMessage(R.string.settings_parse_restore)
                        .setPositiveButton(R.string.confirm,new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { restoreBackup(); }
                        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                }).create().show();
            }
        });

        //Initializing backup switch.
        toggle = (Switch) findViewById(R.id.switch2);
        toggle.setChecked(isBackedUp);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch s = (Switch) v;
                isBackedUp = s.isChecked();
                if (isBackedUp) button.setTextColor(Color.WHITE);
                else            button.setTextColor(Color.GRAY);
            }
        });

        //Initializing delete switch.
        toggle = (Switch) findViewById(R.id.switch3);
        toggle.setChecked(isAskDelete);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch s = (Switch) v;
                isAskDelete = s.isChecked();
            }
        });

        //Initializing combine switch.
        toggle = (Switch) findViewById(R.id.switch4);
        toggle.setChecked(isAskCombine);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch s = (Switch) v;
                isAskCombine = s.isChecked();
            }
        });

        //Initializing title seekbar.
        SeekBar seek = (SeekBar) findViewById(R.id.progressBar);
        final TextView title = (TextView) findViewById(R.id.textView11);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, titleFont);
        seek.setProgress( (titleFont-16)*2 );
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, progress/2 + 16);
                titleFont = progress/2 + 16;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //Initializing content seekbar.
        seek = (SeekBar) findViewById(R.id.progressBar2);
        final TextView content = (TextView) findViewById(R.id.textView12);
        content.setTextSize(TypedValue.COMPLEX_UNIT_SP, contentFont);
        seek.setProgress( (contentFont-12)*4 );
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                content.setTextSize(TypedValue.COMPLEX_UNIT_SP, progress / 4 + 12);
                contentFont = progress / 4 + 12;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.action_done) {
            saveSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void saveSettings(){
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("Encryption",isEncrypted);
        editor.putString("Password",password);
        editor.putBoolean("Backup",isBackedUp);
        editor.putBoolean("Delete",isAskDelete);
        editor.putBoolean("Combine",isAskCombine);
        editor.putInt("Title",titleFont);
        editor.putInt("Content",contentFont);

        editor.apply();

        //Moving on.
        super.onBackPressed();
    }

    protected void restoreBackup(){
        //Connect to cloud.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "8rRg8GndjElDiy1UrviRT650VNE0yKN8BcF1AaFH", "6Zy8VRFP2A017iol0AkJyCJRkOM7TGDWatl7ptIE");
        Toast.makeText(this, "Restoring data: Do not close the application", Toast.LENGTH_SHORT).show();

        //Wiping data.
        String[] list = getFilesDir().list();
        for (String aList : list) deleteFile(aList);

        //Restoring data.
        String deviceID = android.provider.Settings.Secure.getString(getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);
        ParseQuery<ParseObject> query =ParseQuery.getQuery("ID" + deviceID);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null)
                    for (ParseObject note : parseObjects){
                        try { FileOutputStream fos = openFileOutput(note.getString("title"), MODE_PRIVATE);
                            fos.write(note.getString("content").getBytes());
                            fos.close();                                                  }
                        catch (FileNotFoundException ex) { /*Do nothing.*/ }
                        catch (IOException ex)           { Toast.makeText(Settings.this, "Storage is full: Could not restore all", Toast.LENGTH_SHORT).show(); }
                    }
                BookAdapter.mCatalog = new ArrayList<>();
                String[] catalog = Settings.this.getFilesDir().list();
                Collections.addAll(BookAdapter.mCatalog, catalog);
                Collections.reverse(BookAdapter.mCatalog);
                Toast.makeText(Settings.this, "Restore finished", Toast.LENGTH_SHORT).show();
            }
        });
    }
}