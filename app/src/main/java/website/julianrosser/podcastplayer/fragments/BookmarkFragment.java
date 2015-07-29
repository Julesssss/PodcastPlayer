package website.julianrosser.podcastplayer.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import website.julianrosser.podcastplayer.MainActivity;
import website.julianrosser.podcastplayer.MusicService;
import website.julianrosser.podcastplayer.R;
import website.julianrosser.podcastplayer.helpers.DatabaseOpenHelper;
import website.julianrosser.podcastplayer.objects.Song;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class BookmarkFragment extends android.support.v4.app.Fragment implements AbsListView.OnItemClickListener {

    private static final String ARG_SECTION_NUMBER = "bookmark";
    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    // SQL
    private SimpleCursorAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BookmarkFragment() {
    }

    public static BookmarkFragment newInstance(int sectionNumber) {
        BookmarkFragment fragment = new BookmarkFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate view
        View view = inflater.inflate(R.layout.fragment_audiofile, container, false);

        final Typeface fontRobotoRegular = Typeface.createFromAsset(
                getActivity().getAssets(),
                "Roboto-Regular.ttf");

        // Create a cursor for updating bookmark list
        Cursor c = bookmarks();
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.listview_bookmark, c,
                DatabaseOpenHelper.columnsForCursorAdaptor, new int[]{R.id.songListArtist, R.id.songListTitle, R.id.songListPosition, R.id.bookmarkNote},
                0);

        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                TextView textView = (TextView) view;
                textView.setText(cursor.getString(columnIndex));
                textView.setTypeface(fontRobotoRegular);

                // If note empty, hide. Add ""
                if (columnIndex == 6) {
                    if (cursor.getString(columnIndex).length() == 0) {
                        textView.setVisibility(View.GONE);
                    } else {
                        textView.setText("Note - '" + cursor.getString(columnIndex) + "'");
                    }
                }

                return true;
            }
        });

        // Set the custom adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);
        mListView.setScrollbarFadingEnabled(false);

        //mListView.setBackgroundColor(getResources().getColor(R.color.mat_grey_mid));

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        //mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        registerForContextMenu(mListView);

        return view;
    }

    // Returns all bookmark records in the database
    private Cursor bookmarks() {
        return MainActivity.mDB.query(DatabaseOpenHelper.TABLE_NAME,
                DatabaseOpenHelper.columns, null, new String[]{}, null, null,
                null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));

        try {
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // Find bookmark information in database
        String[] returnedData = MainActivity.mDbHelper.getData(position);

        // find song from list
        boolean matched = false;
        int songTrackPos = 0;
        for (Song s : MainActivity.songList) {

            if (s.getIDString().contentEquals(returnedData[0])) {
                matched = true;
                break;
            }
            songTrackPos++;
        }

        if (matched) {

            MainActivity.firstSongPlayed = true;
            MusicService.loadFromBookmark = false;

            // Load song and start
            MainActivity.musicSrv.setSongAtPos(songTrackPos);

            // Seek to
            MusicService.millisecondToSeekTo = Integer.valueOf(returnedData[1]);

            Log.i("BookmarkFragment", "Song found, now playing");

            NavigationDrawerFragment.mDrawerListView.setItemChecked(0, true);

            // Launch player fragment todo - necessary??
            // update the main content by replacing fragments
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, PlayerFragment.newInstance(position + 1))
                    .commit();

            // Update ActionBar title
            getActionBar().setTitle(getString(R.string.title_section1));

            // update textviews
            MusicService.updateTextViews();

        } else {
            Toast.makeText(getActivity(), "Song not found, file may have been moved or renamed", Toast.LENGTH_SHORT).show();
            Log.i("BookmarkFragment", "Song not found, user informed");
        }
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }


    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.bookmark_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;

        switch (item.getItemId()) {
            case R.id.action_context_delete:
                MainActivity.mDbHelper.deleteEntry(position);
                mAdapter.swapCursor(bookmarks());
                return true;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity
     */
    public interface OnFragmentInteractionListener {
    }
}
