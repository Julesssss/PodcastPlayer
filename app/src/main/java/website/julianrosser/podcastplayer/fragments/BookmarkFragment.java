package website.julianrosser.podcastplayer.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import website.julianrosser.podcastplayer.dialogs.BookmarkSortDialog;
import website.julianrosser.podcastplayer.objects.AudioFile;


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

    public static final int DIALOG_SORT_BOOKMARK = 300;
    private static final String ARG_SECTION_NUMBER = "bookmark";
    private final String TAG = getClass().getSimpleName();
    // SQL
    public static SimpleCursorAdapter mAdapter;

    int mStackLevel = 0;
    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

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

    // Returns all bookmark records in the database
    public static Cursor bookmarksByDate() {
        return MainActivity.mDB.query(DatabaseOpenHelper.TABLE_NAME,
                DatabaseOpenHelper.columns, null, new String[]{}, null, null,
                DatabaseOpenHelper._ID + " ASC");
    }

    // Returns all bookmark records in the database
    public static Cursor bookmarksByTitle() {
        return MainActivity.mDB.query(DatabaseOpenHelper.TABLE_NAME,
                DatabaseOpenHelper.columns, null, new String[]{}, null, null,
                DatabaseOpenHelper.TRACK_NAME + " ASC");
    }

    // Returns all bookmark records in the database
    public static Cursor bookmarksByArtist() {
        return MainActivity.mDB.query(DatabaseOpenHelper.TABLE_NAME,
                DatabaseOpenHelper.columns, null, new String[]{}, null, null,
                DatabaseOpenHelper.ARTIST_NAME + " ASC");
    }

    // Returns all bookmark records in the database
    public static Cursor bookmarksByPercent() {
        return MainActivity.mDB.query(DatabaseOpenHelper.TABLE_NAME,
                DatabaseOpenHelper.columns, null, new String[]{}, null, null,
                DatabaseOpenHelper.BOOKMARK_PERCENT + " ASC");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            mStackLevel = savedInstanceState.getInt("level");
        }
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
        Cursor c;

        // If first use, set sorting to date added
        if (MainActivity.bookmarkSortInt == -1) {
            MainActivity.bookmarkSortInt = 0;
        }

        // Set Cursor depending on preference // todo - combine this with other method, below
        if (MainActivity.bookmarkSortInt == 0) {
            c = bookmarksByDate();
        } else if (MainActivity.bookmarkSortInt == 1) {
            c = bookmarksByTitle();
        } else if (MainActivity.bookmarkSortInt == 2) {
            c = bookmarksByArtist();
        } else {
            c = bookmarksByPercent();
        }

        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.listview_bookmark, c,
                DatabaseOpenHelper.columnsForCursorAdaptor, new int[]{R.id.songListArtist, R.id.songListTitle,
                R.id.songListPosition, R.id.bookmarkNote, R.id.text_percent}, 0);

        // Custom views, set typeface, hide view if not needed
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                TextView textView = (TextView) view;
                textView.setText(cursor.getString(columnIndex));
                textView.setTypeface(fontRobotoRegular);

                // If percentage, set to
                if (view.getId() == R.id.text_percent) {
                    // view.getHeight();
                    ((TextView) view).setText(cursor.getString(columnIndex) + "%");
                }

                if (view.getId() == R.id.songListTitle) {
                    ((TextView) view).setText(cursor.getString(columnIndex) + " - ");
                }

                // If note empty, hide. Add ""
                if (view.getId() == R.id.bookmarkNote) {
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

        TextView emptyText = (TextView) view.findViewById(android.R.id.empty);
        mListView.setEmptyView(emptyText);

        // This is to ensure 5.0+ works
        mListView.setBackgroundColor(getResources().getColor(R.color.mat_grey_mid));

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        //mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        registerForContextMenu(mListView);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("level", mStackLevel);
    }

    public void showDialog(int type) {

        mStackLevel++;

        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
        Fragment prev = getActivity().getFragmentManager().findFragmentByTag("dialogSort");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        switch (type) {

            case DIALOG_SORT_BOOKMARK:

                DialogFragment dialogFrag = BookmarkSortDialog.newInstance(123, getActivity());
                dialogFrag.setTargetFragment(this, DIALOG_SORT_BOOKMARK);
                dialogFrag.show(getFragmentManager().beginTransaction(), "dialogSort");

                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case DIALOG_SORT_BOOKMARK:

                if (resultCode == Activity.RESULT_OK) {

                    changeBookmarkSorting(data.getExtras().getInt(BookmarkSortDialog.DATA_SORTING_KEY));

                } else if (resultCode == Activity.RESULT_CANCELED) {

                    Log.d(getClass().getSimpleName(), "ActivityResult: CANCELED");
                }

                break;
        }
    }

    public void changeBookmarkSorting(int sortKey) {

        MainActivity.bookmarkSortInt = sortKey;

        switch(sortKey) {
            case 0:
                mAdapter.swapCursor(bookmarksByDate());
                break;

            case 1:
                mAdapter.swapCursor(bookmarksByTitle());
                break;

            case 2:
                mAdapter.swapCursor(bookmarksByArtist());
                break;

            case 3:
                mAdapter.swapCursor(bookmarksByPercent());
                break;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // Find bookmark information in database
        String[] returnedData = MainActivity.mDbHelper.getData(position);

        // todo use view to find place in db

        // find song from list
        boolean matched = false;
        int songTrackPos = 0;
        for (AudioFile s : MainActivity.audioFileList) {

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

            NavDrawerFragment.mDrawerListView.setItemChecked(0, true);



            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

            if (MainActivity.playerFragment == null) {
                MainActivity.playerFragment = PlayerFragment.newInstance(position + 1);
            } else {
                MainActivity.mTitle = "Now Playing";
            }

            fragmentManager.beginTransaction()
                    .replace(R.id.container, MainActivity.playerFragment)
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
                changeBookmarkSorting(MainActivity.bookmarkSortInt);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_fragment_bookmark, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_order) {

            showDialog(DIALOG_SORT_BOOKMARK);
        }

            /*

            if (MainActivity.bookmarkSortInt == 0) {
                mAdapter.swapCursor(bookmarksByTitle());
                MainActivity.bookmarkSortInt = 1;
                Toast.makeText(getActivity(), "Sorted by Title", Toast.LENGTH_SHORT).show();

            } else if (MainActivity.bookmarkSortInt == 1) {
                mAdapter.swapCursor(bookmarksByArtist());
                MainActivity.bookmarkSortInt = 2;
                Toast.makeText(getActivity(), "Sorted by Artist", Toast.LENGTH_SHORT).show();

            } else if (MainActivity.bookmarkSortInt == 2) {
                mAdapter.swapCursor(bookmarksByPercent());
                MainActivity.bookmarkSortInt = 3;
                Toast.makeText(getActivity(), "Sorted by Progress", Toast.LENGTH_SHORT).show();

            } else if (MainActivity.bookmarkSortInt == 3) {
                mAdapter.swapCursor(bookmarksByDate());
                MainActivity.bookmarkSortInt = 0;
                Toast.makeText(getActivity(), "Sorted by Date", Toast.LENGTH_SHORT).show();
            } */

        return super.onOptionsItemSelected(item);


    }
    public boolean isFragmentUIActive() {
        return isAdded() && !isDetached() && !isRemoving();
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
