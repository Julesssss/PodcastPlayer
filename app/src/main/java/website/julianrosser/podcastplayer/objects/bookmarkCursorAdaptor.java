package website.julianrosser.podcastplayer.objects;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;

public class bookmarkCursorAdaptor extends SimpleCursorAdapter {



    public bookmarkCursorAdaptor(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }
}

/**
 * private Cursor messageCursor;
 * private Context context;
 * private final LayoutInflater inflater;
 * <p/>
 * public BookmarkCursorAdapter(Context context, Cursor cursor) {
 * super(context, cursor);
 * this.inflater = LayoutInflater.from(context);
 * this.context = context;
 * }
 *
 * @Override public View newView(Context context, Cursor cursor, ViewGroup parent) {
 * return null;
 * }
 * @Override public void bindView(View view, Context context, Cursor cursor) {
 * <p/>
 * TextView messageTextView = (TextView) view.findViewById(R.id.message_item_text);
 * <p/>
 * String messageFont = cursor.getString(cursor.getColumnIndex("name_of_database_column"));
 * if (messageFont.equals("Epimodem")) {
 * Typeface face = Typeface.createFromAsset(getAssets(), "fonts/epimodem.ttf");
 * messageTextView.setTypeface(face);
 * }
 * @Override public View newView (Context context, Cursor cursor, ViewGroup parent){
 * final View view = this.inflater.inflate(R.layout.message_item, parent, false);
 * return view;
 * }
 * }
 * }
 */