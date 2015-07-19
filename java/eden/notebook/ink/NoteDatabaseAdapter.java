package eden.notebook.ink;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class NoteDatabaseAdapter {

    private NoteSQLHelper helper;

    public NoteDatabaseAdapter(Context context){ helper = new NoteSQLHelper(context); }

    public long insertNewRow(String title, String createdAt, String editedAt, int star, int colorIndex){
        ContentValues contentValues = new ContentValues();
        contentValues.put(NoteSQLHelper.TITLE, title);
        contentValues.put(NoteSQLHelper.DATE_CREATED, createdAt);
        contentValues.put(NoteSQLHelper.DATE_EDITED, editedAt);
        contentValues.put(NoteSQLHelper.STAR, star);
        contentValues.put(NoteSQLHelper.COLOR, colorIndex);

        SQLiteDatabase db = helper.getWritableDatabase();
        return db.insert(NoteSQLHelper.TABLE_NAME, null, contentValues);
    }

    public int deleteRow(String title){
        SQLiteDatabase db = helper.getWritableDatabase();
        if (title == null)
            return db.delete(NoteSQLHelper.TABLE_NAME, null, null);
        else
            return db.delete(NoteSQLHelper.TABLE_NAME, NoteSQLHelper.TITLE+"=?",new String[]{title});
    }

    public List<String> getAllTitles(){
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query(NoteSQLHelper.TABLE_NAME, new String[]{NoteSQLHelper.TITLE}, null, null, null, null, null);
        List<String> list = new ArrayList<>();
        while (cursor.moveToNext()){
            list.add(0, cursor.getString(0));
        }
        cursor.close();
        return list;
    }

    public List<String> getAllCreatedAt(){
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query(NoteSQLHelper.TABLE_NAME, new String[]{NoteSQLHelper.DATE_CREATED}, null, null, null, null, null);
        List<String> list = new ArrayList<>();
        while (cursor.moveToNext()){
            list.add(0, cursor.getString(0));
        }
        cursor.close();
        return list;
    }

    public List<String> getAllEditedAt(){
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query(NoteSQLHelper.TABLE_NAME, new String[]{NoteSQLHelper.DATE_EDITED}, null, null, null, null, null);
        List<String> list = new ArrayList<>();
        while (cursor.moveToNext()){
            list.add(0, cursor.getString(0));
        }
        cursor.close();
        return list;
    }

    public List<Integer> getAllColors(){
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query(NoteSQLHelper.TABLE_NAME, new String[]{NoteSQLHelper.COLOR}, null, null, null, null, null);
        List<Integer> list = new ArrayList<>();
        while (cursor.moveToNext()){
            list.add(0, cursor.getInt(0));
        }
        cursor.close();
        return list;
    }

    public List<Integer> getAllStars(){
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query(NoteSQLHelper.TABLE_NAME, new String[]{NoteSQLHelper.STAR}, null, null, null, null, null);
        List<Integer> list = new ArrayList<>();
        while (cursor.moveToNext()){
            list.add(0, cursor.getInt(0));
        }
        cursor.close();
        return list;
    }

    public void updateFavorites(int index){
        ContentValues contentValues = new ContentValues();
        contentValues.put(NoteSQLHelper.STAR, 1 - Library.adapter.allStars.get(index));
        SQLiteDatabase db = helper.getWritableDatabase();
        db.update(NoteSQLHelper.TABLE_NAME, contentValues, NoteSQLHelper.TITLE+"=?", new String[]{Library.adapter.mCatalog.get(index)});
    }

    static class NoteSQLHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "eden_theater_archive";
        private static final int VERSION_NUMBER = 1;
        private Context deez_nuts;

        private static final String TABLE_NAME = "NOTES";
        private static final String TITLE = "Title";
        private static final String DATE_CREATED = "Created";
        private static final String DATE_EDITED = "Edited";
        private static final String STAR = "Star";
        private static final String COLOR = "Color";
        private static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+" ( " +
                ""+TITLE+" VARCHAR(255), " +
                ""+DATE_CREATED+" VARCHAR(255), " +
                ""+DATE_EDITED+" VARCHAR(255), " +
                ""+STAR+" INTEGER, " +
                ""+COLOR+" INTEGER);";
        private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public NoteSQLHelper(Context context) {
            super(context, DATABASE_NAME, null, VERSION_NUMBER);
            deez_nuts = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_TABLE);
            } catch (Exception e){
                //MOST LIKELY AN SQLEXCEPTION.
                Toast.makeText(deez_nuts, "ERROR: Could not establish database", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { //Only gets called if version changes.
            try{
                db.execSQL(DROP_TABLE);
                onCreate(db);
            } catch (Exception e){
                //MOST LIKELY AN SQLEXCEPTION.
                Toast.makeText(deez_nuts, "ERROR: Could not update database", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
