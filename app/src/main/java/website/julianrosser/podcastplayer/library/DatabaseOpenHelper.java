package website.julianrosser.podcastplayer.library;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

	// Names for SQL table & columns
	final public static String TABLE_NAME = "bookmarks";
	final public static String ARTIST_NAME = "artist_name";
	final public static String TRACK_NAME = "track_name";
	final public static String UNIQUE_ID = "unique_id";
	final public static String BOOKMARK_FORMATTED = "bookmark_formatted";
	final public static String BOOKMARK_MILLIS = "bookmark_millis";
	final public static String _ID = "_id";
	final public static String[] columns = { _ID, UNIQUE_ID, ARTIST_NAME, TRACK_NAME, BOOKMARK_FORMATTED, BOOKMARK_MILLIS};

	// Only the columns that are to be passed to ListView for display
	final public static String[] columnsForCursorAdaptor = {ARTIST_NAME, TRACK_NAME, BOOKMARK_FORMATTED };

	// Command for initializing database
	final private static String CREATE_CMD =
			"CREATE TABLE bookmarks (" + _ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + UNIQUE_ID + " TEXT NOT NULL, "
					+ ARTIST_NAME + " TEXT NOT NULL, " + TRACK_NAME + " TEXT NOT NULL, " + BOOKMARK_FORMATTED
					+ " TEXT NOT NULL," + BOOKMARK_MILLIS + " TEXT NOT NULL)";

	final private static String NAME = "bookmark_db";
	final private static Integer VERSION = 1;
	final private Context mContext;

	public DatabaseOpenHelper(Context context) {
		super(context, NAME, null, VERSION);
		this.mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_CMD);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// N/A
	}

	/**
	 * Return the requested track's info
	 */
	public String[] getData(int position) {

		String selectQuery = "SELECT " + UNIQUE_ID + ", " + BOOKMARK_MILLIS
				+ " FROM " + TABLE_NAME + " LIMIT 1 OFFSET " + position;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		String[] data = new String[2]; // todo - change 2 to sting ref

		while (cursor.moveToNext()) {
			for (int i = 0; i < 2; i++) {
				data[i] = cursor.getString(i);
			}
		}

		cursor.close();

		return data;
	}

	public String[] getLast() {

		String selectQuery = "SELECT " + UNIQUE_ID + ", " + BOOKMARK_MILLIS + ", " + ARTIST_NAME + ", " + TRACK_NAME
				+ " FROM " + TABLE_NAME + " ORDER BY " + _ID +  " DESC LIMIT 1";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		String[] data = new String[4]; // todo - change 2 to sting ref

		while (cursor.moveToNext()) {
			for (int i = 0; i < 4; i++) {
				data[i] = cursor.getString(i);
			}
		}

		cursor.close();

		Log.i("DBH", "DATA: " + data[1] + " / " + data[2] + " / " + data[3]);
		return  data;

	}

	void deleteDatabase() {
		mContext.deleteDatabase(NAME);
	}
}
