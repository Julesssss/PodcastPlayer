package website.julianrosser.podcastplayer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import website.julianrosser.podcastplayer.bookmarks.BookmarkFragment;
import website.julianrosser.podcastplayer.classes.Bookmark;
import website.julianrosser.podcastplayer.classes.Song;
import website.julianrosser.podcastplayer.library.LibraryFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, LibraryFragment.OnFragmentInteractionListener, BookmarkFragment.OnFragmentInteractionListener{

    // Array of songs

    public static ArrayList<Song> songList;
    // Array of bookmarks
    public static ArrayList<Bookmark> bookmarkList;
    // To check if MusicService is bound to Activity
    public static boolean musicBound = false;
    // To prevent song starting on first play
    public static boolean firstPreparedSong = true;
    // Shuffle mode boolean
    public static boolean shuffleMode = true;
    // Reference to music service
    public static MusicService musicSrv;
    // Reference to PlayerFragment
    public static PlayerFragment playerFragment;

    // Used to store the last screen title. For use in {@link #restoreActionBar()}.
    private CharSequence mTitle;
    // Intent used for binding service to Activity
    private Intent playIntent;
    // Fragment managing the behaviors, interactions and presentation of the navigation drawer.
    private NavigationDrawerFragment mNavigationDrawerFragment;
    // For logging purposes
    private String TAG = getClass().getSimpleName();

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            // Get service
            musicSrv = binder.getService();
            // Pass song list to - Should I check for empty list here?
            musicSrv.setList(MainActivity.songList);
            // update boolean to show service is bound
            musicBound = true;
            /**
             * TODO
             * This below is the second Fragment and shouldn't be needed. But when removed
             * textviews aren't updated. Why does this happen?
             */
            getNewPlayerFragment(0);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize song and bookmark lists
        songList = new ArrayList<>();
        bookmarkList = new ArrayList<>();

        // Search for songs and update list
        getSongList();

        // Ensure volume buttons work correctly
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Get Navigation Drawer reference
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the navigation drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Get current Fragment title - todo - is this needed?
        mTitle = getTitle();
    }

    /**
     * Create player intent if currently null, bind to service  and start.
     */
    @Override
    public void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }

    }

    /**
     * Unbind MusicService if app is closed, then stop and nullify MusicService
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (musicConnection != null) {
            unbindService(musicConnection);
        }

        if (playIntent != null) {
            stopService(playIntent);
        }

        musicSrv = null;
    }

    /**
     * TODO
     * Ensure that back button has same function as home, and doesn't throw error.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    /**
     * Search phone for music tracks and add to the newly created song array list
     */
    public void getSongList() {
        int pos = 0;
        // Retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            // Get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int songDuration = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            // Add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisDuration = musicCursor.getString(songDuration);

                // Workaround for filtering invalid tracks
                if (thisDuration != null) {
                    songList.add(new Song(thisId, thisTitle, thisArtist, thisDuration, pos));
                    pos += 1;
                }
            }
            while (musicCursor.moveToNext());
        }

        if (musicCursor != null) {
            musicCursor.close();
        }

        /** TODO - Do something to prevent empty playlist crash
        if (songList.size() == 0){
        }
         */

        Log.i(TAG, "Song array created with " + songList.size() + " songs.");
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Log.i(TAG, "Position " + position);
        if (position == 0) {
            getNewPlayerFragment(position);
        } else if (position == 1) {
            getNewBookmarkFragment(position);
        } else if (position == 2) {
            getNewLibraryFragment(position);
        } else //noinspection StatementWithEmptyBody
            if (position == 3) {
            // TODO - create getNewPlaylistFragment(position);
        } else {
            Log.e(TAG, "Nav drawer id error");
        }
    }

    /**
     * Create a new PlayerFragment and start with the FragmentManager
     */
    public void getNewPlayerFragment(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        playerFragment = PlayerFragment.newInstance(position + 1);

        fragmentManager.beginTransaction()
                .replace(R.id.container, playerFragment)
                .commit();
    }

    /**
     * Create a new BookmarkFragment and start with the FragmentManager
     */
    public void getNewBookmarkFragment(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        BookmarkFragment bookmarkFragment = BookmarkFragment.newInstance(position + 1);

        fragmentManager.beginTransaction()
                .replace(R.id.container, bookmarkFragment)
                .commit();
    }

    /**
     * Create a new LibraryFragment and start with the FragmentManager
     */
    private void getNewLibraryFragment(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        LibraryFragment libraryFragment = LibraryFragment.newInstance(position + 1);

        fragmentManager.beginTransaction()
                .replace(R.id.container, libraryFragment)
                .commit();
    }

    /**
     * Update ActionBar title when a Fragment is attatched.
     */
    public void onSectionAttached(int number) {
        Log.i(TAG, "Fragment Attatched: " + number);
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
        }
    }

    /**
     * TODO - Is this needed? Why?
     */
    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_bookmark) {

            // Create new bookmark object for current song
            Bookmark b = new Bookmark(songList.get(MusicService.songPosition), String.valueOf(MusicService.mPlayer.getCurrentPosition()));

            // Add bookmark to list
            bookmarkList.add(b);

            Toast.makeText(this, "Bookmark saved at "  + b.getCurrentPosition(), Toast.LENGTH_LONG).show();

            return true;

        } else if (id == R.id.action_random) {

            if (MainActivity.shuffleMode) {
                Toast.makeText(this, "Shuffle Mode OFF" , Toast.LENGTH_LONG).show();
                MainActivity.shuffleMode = false;

            } else {
                Toast.makeText(this, "Shuffle Mode ON" , Toast.LENGTH_LONG).show();
                MainActivity.shuffleMode = true;
            }

            return true;

        } else if (id == R.id.action_settings) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(String id) {
        // todo - why is this needed?
        Log.i(TAG, "onFragInteraction: " + id);
    }
}

