package website.julianrosser.podcastplayer.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import website.julianrosser.podcastplayer.objects.Bookmark;

/**
 * Class for helping with DataBase operations and requests
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {

    // Names for SQL table & columns
    final public static String TABLE_NAME = "bookmarks";
    final public static String ARTIST_NAME = "artist_name";
    final public static String TRACK_NAME = "track_name";
    final public static String UNIQUE_ID = "unique_id";
    final public static String BOOKMARK_FORMATTED = "bookmark_formatted";
    final public static String BOOKMARK_MILLIS = "bookmark_millis";
    final public static String BOOKMARK_NOTE = "bookmark_note";
    final public static String BOOKMARK_PERCENT = "bookmark_percent";
    final public static String _ID = "_id";
    final public static String[] columns = {_ID, UNIQUE_ID, ARTIST_NAME, TRACK_NAME, BOOKMARK_FORMATTED, BOOKMARK_MILLIS, BOOKMARK_NOTE, BOOKMARK_PERCENT};

    // Only the columns that are to be passed to ListView for display
    final public static String[] columnsForCursorAdaptor = {ARTIST_NAME, TRACK_NAME, BOOKMARK_FORMATTED, BOOKMARK_NOTE, BOOKMARK_PERCENT};
    // Command for initializing database
    final private static String CREATE_CMD =
            "CREATE TABLE bookmarks (" + _ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + UNIQUE_ID + " TEXT NOT NULL, "
                    + ARTIST_NAME + " TEXT NOT NULL, " + TRACK_NAME + " TEXT NOT NULL, " + BOOKMARK_FORMATTED
                    + " TEXT NOT NULL," + BOOKMARK_MILLIS + " TEXT NOT NULL," + BOOKMARK_NOTE + " TEXT NOT NULL," + BOOKMARK_PERCENT + " INTEGER)";
    final private static String NAME = "bookmark_db";
    final private static Integer VERSION = 1;
    // Sting array of current bookmarks, to be deleted
    public static ArrayList<String> bookmarksToDelete;

    public DatabaseOpenHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_CMD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Required, but unused
    }

    public int countEntries() {
        Cursor mCount = this.getReadableDatabase().rawQuery("select count(*) from " + TABLE_NAME, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }

    /**
     * Return the requested track's info
     */
    public String[] getData(int position) {

        int STRING_ARGS = 2;

        String selectQuery = "SELECT " + UNIQUE_ID + ", " + BOOKMARK_MILLIS
                + " FROM " + TABLE_NAME + " LIMIT 1 OFFSET " + position;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        String[] data = new String[STRING_ARGS];

        while (cursor.moveToNext()) {
            for (int i = 0; i < STRING_ARGS; i++) {
                data[i] = cursor.getString(i);
            }
        }
        cursor.close();

        return data;
    }

    /**
     * Return the requested track's id
     */
    public String getID(int position) {

        int STRING_ARGS = 1;

        String selectQuery = "SELECT " + _ID + " FROM " + TABLE_NAME + " LIMIT 1 OFFSET " + position;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        String data = "";

        while (cursor.moveToNext()) {
            for (int i = 0; i < STRING_ARGS; i++) {
                data = cursor.getString(i);
            }
        }
        cursor.close();

        return data;
    }

    public String[] getLast() {

        int STRING_ARGS = 4;

        String selectQuery = "SELECT " + UNIQUE_ID + ", " + BOOKMARK_MILLIS + ", " + ARTIST_NAME + ", " + TRACK_NAME
                + " FROM " + TABLE_NAME + " ORDER BY " + _ID + " DESC LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        String[] data = new String[STRING_ARGS];

        while (cursor.moveToNext()) {
            for (int i = 0; i < STRING_ARGS; i++) {
                data[i] = cursor.getString(i);
            }
        }
        cursor.close();

        return data;
    }

    /**
     * Delete Bookmark entry from Database
     */
    public void deleteEntry(int position) {

        SQLiteDatabase db = this.getReadableDatabase();

        // get row and _ID
        String id = getID(position);

        Log.i("TAG", "INFO: " + id);

        // delete using _ID
        db.delete(DatabaseOpenHelper.TABLE_NAME,
                DatabaseOpenHelper._ID + "=?",
                new String[]{id});
    }

    /**
     * Delete Bookmark entry from Database
     */
    public void deleteEntryFromID(String id) {

        SQLiteDatabase db = this.getReadableDatabase();

        Log.i("deleteEntryFromID", "ID: " + id);

        // delete using _ID
        db.delete(DatabaseOpenHelper.TABLE_NAME,
                DatabaseOpenHelper._ID + "=?",
                new String[]{id});
    }

    public ArrayList<Bookmark> getBookmarksForCurrentTrack(long id) {

        ArrayList<Bookmark> bookmarks = new ArrayList<>();

        String selectQuery = "SELECT " + UNIQUE_ID + ", " + BOOKMARK_PERCENT + ", " + BOOKMARK_MILLIS + ", " + BOOKMARK_NOTE + " FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        while (cursor.moveToNext()) {

            if (String.valueOf(id).equals(cursor.getString(0))) {
                // match, so add to array

                Bookmark newBookmark = new Bookmark(cursor.getString(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3));

                bookmarks.add(newBookmark);
            }
        }

        cursor.close();

        return bookmarks;

    }


    public boolean bookmarkAlreadyExists(long id) {

        int STRING_ARGS = 1;
        boolean foundBookmark = false;

        String selectQuery = "SELECT " + UNIQUE_ID + " FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        String[] data = new String[STRING_ARGS];

        int j = 0;
        bookmarksToDelete = new ArrayList<>(); // todo, might be to show, not delete

        while (cursor.moveToNext()) {
            for (int i = 0; i < STRING_ARGS; i++) {
                data[i] = cursor.getString(i);

                if (String.valueOf(id).equals(data[0])) {
                    foundBookmark = true;
                    Log.i(getClass().getSimpleName(), "FOUND AT POSITION: " + j);

                    bookmarksToDelete.add(getID(j));
                }
            }
            j++;
        }

        Log.i("DBH", "bookmarks to delete: " + bookmarksToDelete.size()); // todo - not delet???  ---> new method!!!!!!!!!!!

        /// todo - keep reference to foundBookmark position

        cursor.close();

        return foundBookmark;
    }

}
