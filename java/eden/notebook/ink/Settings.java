package eden.notebook.ink;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import java.util.List;

public class Settings extends ActionBarActivity {

    //User option info.
    private SharedPreferences prefs;
    private boolean isEncrypted;
    private String password;
    private boolean isBackedUp;
    private boolean isAskDelete;
    private int titleFont;
    private int contentFont;
    private boolean serif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Initialize all variables.
        prefs = getSharedPreferences("EdenNotebookSettings", Context.MODE_PRIVATE);
        isEncrypted = prefs.getBoolean("Encryption",false);
        password = prefs.getString("Password",null);
        isBackedUp = prefs.getBoolean("Backup",false);
        isAskDelete = prefs.getBoolean("Delete",true);
        titleFont = prefs.getInt("Title",44);
        contentFont = prefs.getInt("Content",24);
        serif = prefs.getBoolean("Serif", false);

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
                                    setPassword(s);
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

        //Initializing title seekbar.
        SeekBar seek = (SeekBar) findViewById(R.id.progressBar);
        final TextView title = (TextView) findViewById(R.id.textView11);
        if (serif) title.setTypeface(Typeface.SERIF);
        else       title.setTypeface(Typeface.SANS_SERIF);
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
        if (serif) content.setTypeface(Typeface.SERIF);
        else       content.setTypeface(Typeface.SANS_SERIF);
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

        CheckBox check = (CheckBox) findViewById(R.id.checkBox);
        check.setChecked(serif);
        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                serif = isChecked;
                if (isChecked) { title.setTypeface(Typeface.SERIF);       content.setTypeface(Typeface.SERIF);      }
                else           { title.setTypeface(Typeface.SANS_SERIF);  content.setTypeface(Typeface.SANS_SERIF); }
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
        editor.putInt("Title",titleFont);
        editor.putInt("Content",contentFont);
        editor.putBoolean("Serif",serif);

        editor.apply();

        //Moving on.
        super.onBackPressed();
    }

    protected void restoreBackup(){
        //Connect to cloud.
        Parse.initialize(this, "8rRg8GndjElDiy1UrviRT650VNE0yKN8BcF1AaFH", "6Zy8VRFP2A017iol0AkJyCJRkOM7TGDWatl7ptIE");
        Toast.makeText(this, "Restoring data: Do not close the application", Toast.LENGTH_SHORT).show();

        //Wipe all existing files.
        String[] filenames = getFilesDir().list();
        for (String filename : filenames) deleteFile(filename);

        //Wipe all existing database data.
        new NoteDatabaseAdapter(this).deleteRow(null);

        //Restoring data.
        String deviceID = android.provider.Settings.Secure.getString(getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);
        ParseQuery<ParseObject> query =ParseQuery.getQuery("ID" + deviceID);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null){
                    for (int i = parseObjects.size() ; i>0 ; i--){
                        //Saving note content.
                        ParseObject note = parseObjects.get(i-1);
                        String title = note.getString("title");
                        try { FileOutputStream fos = openFileOutput(title, MODE_PRIVATE);
                            fos.write(note.getString("content").getBytes());
                            fos.close();                                                }
                        catch (FileNotFoundException ex) { Toast.makeText(Settings.this, "Error: FileNotFoundException. Abort restore.", Toast.LENGTH_SHORT).show(); return; }
                        catch (IOException ex)           { Toast.makeText(Settings.this, "Storage is full: Could not restore all", Toast.LENGTH_SHORT).show(); break; }

                        //Saving note details.
                        String createdAt = note.getString("DATE_CREATED");
                        String editedAt = note.getString("DATE_EDITED");
                        int stared = note.getInt("Stared");
                        int color = note.getInt("Color");
                        if (new NoteDatabaseAdapter(Settings.this).insertNewRow(title,createdAt,editedAt,stared,color) < 0) {
                            //Failed to save data on SQL.
                            Toast.makeText(Settings.this, "Error: Failed to save SQL data. Abort restore.\nDO NOT CLOSE APP AND TRY AGAIN", Toast.LENGTH_LONG).show();
                            deleteFile(title); //Undo saving note content.
                            return;
                        }
                    }

                    //Set adapter data.
                    Library.adapter.initializeDataArray();
                    Library.updated = false;
                    if (getIntent().getIntExtra("AdapterType",1) == 2)
                        ColorLibrary.updated = false;
                    Toast.makeText(Settings.this, "Restore finished", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Settings.this, "Error: Failed to retrieve data from cloud\nDO NOT CLOSE APP AND TRY AGAIN", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected void setPassword(final Switch s){
        LinearLayout layout = (LinearLayout) Settings.this.getLayoutInflater().inflate(R.layout.dialog_password, null);
        final EditText edittext = (EditText) layout.findViewById(R.id.editext);
        new AlertDialog.Builder(Settings.this)
                .setTitle(R.string.settings_password_set)
                .setView(layout)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    //User inputed password.
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        password = edittext.getText().toString();
                        LinearLayout layout = (LinearLayout) Settings.this.getLayoutInflater().inflate(R.layout.dialog_password, null);
                        final EditText edittext = (EditText) layout.findViewById(R.id.editext);
                        new AlertDialog.Builder(Settings.this)
                                .setTitle(R.string.settings_password_reset)
                                .setView(layout)
                                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                    //User re-inputed password.
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (password.equals(edittext.getText().toString())) {
                                            //User input same password twice.
                                            isEncrypted = true;
                                            s.setChecked(true);
                                        }
                                        else {//wrong password
                                            new AlertDialog.Builder(Settings.this)
                                                    .setMessage(R.string.do_not_match)
                                                    .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) { setPassword(s); /*Retry.*/ }
                                                    }).create().show();
                                        }
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    //User decided to not encrypt.
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {/*Don't do anything if user decided to cancel.*/}
                                }).create().show();
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    //User decided to not encrypt.
                    @Override
                    public void onClick(DialogInterface dialog, int which) {/*Don't do anything if user decided to cancel.*/}
                }).create().show();
    }
}