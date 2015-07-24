package website.julianrosser.podcastplayer.bookmarks;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import website.julianrosser.podcastplayer.MainActivity;
import website.julianrosser.podcastplayer.MusicService;
import website.julianrosser.podcastplayer.NavigationDrawerFragment;
import website.julianrosser.podcastplayer.PlayerFragment;
import website.julianrosser.podcastplayer.R;
import website.julianrosser.podcastplayer.classes.Bookmark;
import website.julianrosser.podcastplayer.classes.Song;
import website.julianrosser.podcastplayer.library.DatabaseOpenHelper;


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
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    public BookmarkListAdapter bookmarkListAdapter;
    private OnFragmentInteractionListener mListener;
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

        // Create custom adapter
        bookmarkListAdapter = new BookmarkListAdapter(getActivity()); // todo - delete this??/


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate view - todo but why this audiofile???
        View view = inflater.inflate(R.layout.fragment_audiofile, container, false);

        // Create a cursor for updating bookmark list
        Cursor c = bookmarks(); // todo - cursor should
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.bookmark_list_view, c,
                DatabaseOpenHelper.columnsForCursorAdaptor, new int[] {R.id.songListArtist, R.id.songListTitle, R.id.songListPosition },
                0);

        // Set the custom adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);



        return view;
    }

    // Returns all bookmark records in the database
    private Cursor bookmarks() {
        return MainActivity.mDB.query(DatabaseOpenHelper.TABLE_NAME,
                DatabaseOpenHelper.columns, null, new String[] {}, null, null,
                null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));

        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // todo - Detect if null or empty

        // Find bookmark information in database
        String[] returnedData = MainActivity.mDbHelper.getData(position);

        // find song from list // todo quicker waY? make quick find algorithm
        boolean matched = false;
        int songTrackPos = 0;
        for (Song s : MainActivity.songList) {

            if (s.getIDString().contentEquals(returnedData[0])) {
                matched = true;
                break;
            }
            songTrackPos ++;
        }

        if (matched) {

            MainActivity.firstSongPlayed = true;
            MusicService.loadFromBookmark = false;

            // Load song and start
            MainActivity.musicSrv.setSongAtPos(songTrackPos);

            // Seek to  // todo - need to be SURE that this will work
            MusicService.millisecondToSeekTo = Integer.valueOf(returnedData[1]);

            Log.i("BookmarkFragment", "Song found, now playing");

            NavigationDrawerFragment.mDrawerListView.setItemChecked(0, true);

            // Launch player fragment
            // update the main content by replacing fragments
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, PlayerFragment.newInstance(position + 1))
                    .commit();


            // Update ActionBar title
            getActionBar().setTitle(getString(R.string.title_section1));

        } else {
            Toast.makeText(getActivity(), "Song not found, file may have been moved or renamed", Toast.LENGTH_SHORT).show();
            Log.i("BookmarkFragment", "Song not found, user informed");
        }


        if (returnedData != null && returnedData.length != 0) {
            // todo - Log.i("SQL RETURN", "SIZE: " + returnedData.length);
            //for (int i  = returnedData.length; i > 0; i--) {
            //    Log.i("SQL RETURN", "DATA: " + MainActivity.mDbHelper.getData()[i-1]);
            //}
        } else {
            Log.i("SQL RETURN", "SIZE:0 /OR/ null");
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
