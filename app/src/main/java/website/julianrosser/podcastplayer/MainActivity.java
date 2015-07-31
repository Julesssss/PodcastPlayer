package website.julianrosser.podcastplayer;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import website.julianrosser.podcastplayer.fragments.BookmarkFragment;
import website.julianrosser.podcastplayer.fragments.LibraryFragment;
import website.julianrosser.podcastplayer.fragments.NavigationDrawerFragment;
import website.julianrosser.podcastplayer.fragments.PlayerFragment;
import website.julianrosser.podcastplayer.helpers.DatabaseOpenHelper;
import website.julianrosser.podcastplayer.objects.Song;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, LibraryFragment.OnFragmentInteractionListener, BookmarkFragment.OnFragmentInteractionListener {


    // Array of songs
    public static ArrayList<Song> songList;
    // To check if MusicService is bound to Activity
    public static boolean musicBound = false;
    // To prevent song starting on first play
    public static boolean firstPreparedSong = true;
    // to check if first song has played yet
    public static int seekbarPosition;
    public static String textCurrentPos;
    public static boolean firstSongPlayed = false;
    // Shuffle mode boolean
    public static boolean shuffleMode = false;
    // Reference to music service
    public static MusicService musicSrv;
    // Reference to PlayerFragment
    public static PlayerFragment playerFragment;
    // Used to store the last screen title. For use in {@link #restoreActionBar()}.
    public static CharSequence mTitle;
    // SQL Database reference
    public static SQLiteDatabase mDB = null;
    // Reference to Database helper class
    public static DatabaseOpenHelper mDbHelper;
    // Seekbar ratio reference
    public static int SEEKBAR_RATIO = 1000;
    // Bundle
    public int lastPlayedListPosition;
    public int lastPlayedCurrentPosition;
    public String SPREF_INT_LIST_POSITION = "songListPosition";
    public String SPREF_INT_CURRENT_POSITION = "songCurrentPosition";
    // For loading last played in correct position
    private String SPREF_KEY = "your_prefs";
    // For logging purposes
    private String TAG = getClass().getSimpleName();
    // Intent used for binding service to Activity
    private Intent playIntent;
    // Fragment managing the behaviors, interactions and presentation of the navigation drawer.
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * connect to the service
     */
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            // Get service
            musicSrv = binder.getService();
            // Pass song list to - Should I check for empty list here?
            musicSrv.setList(MainActivity.songList);
            // update boolean to show service is bound
            musicBound = true;


            // Check that songs exist on device
            if (songList.size() > 0 && MusicService.mPlayer != null) {

                // Check if this is first open (Or error with last played song)
                if (lastPlayedListPosition == -1 || lastPlayedCurrentPosition == -1) {
                    // First time
                    MainActivity.musicSrv.setSongAtPosButDontPlay(0);
                } else {
                    if (!MusicService.mPlayer.isPlaying()) {

                        MainActivity.firstPreparedSong = true;

                        MusicService.millisecondToSeekTo = lastPlayedCurrentPosition;
                        MainActivity.musicSrv.setSongAtPosButDontPlay(lastPlayedListPosition);

                        if (MainActivity.musicSrv != null && PlayerFragment.seekBar != null) {
                            PlayerFragment.seekBar.setProgress((int) MusicService.songBookmarkSeekPosition);
                            PlayerFragment.textSongCurrent.setText(Song.convertTime(String.valueOf(MusicService.millisecondToSeekTo)));
                        }
                    } else {
                        // Already playing, so just set TextViews
                        MusicService.updateTextViews();
                    }
                }
            }
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
        Log.i(TAG, "onCreate()");

        // Initialize song list
        songList = new ArrayList<>();

        // Search for songs and update list, if songs are available
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

        // Get current Fragment title
        //mTitle = getTitle();

        // Create a new DatabaseHelper
        mDbHelper = new DatabaseOpenHelper(this);

        // Get the underlying database for writing
        mDB = mDbHelper.getWritableDatabase();

        // Load position and id of last played
        SharedPreferences sp = getSharedPreferences(SPREF_KEY, Activity.MODE_PRIVATE);
        lastPlayedListPosition = sp.getInt(SPREF_INT_LIST_POSITION, -1);
        lastPlayedCurrentPosition = sp.getInt(SPREF_INT_CURRENT_POSITION, -1);
        // todo - Songlist may have changed, this would make list position incorrect. Maybe should use different ID?

        MusicService.exiting = false;

        // If not already using Matirial theme, set ActionBar color

    }

    /**
     * Create player intent if currently null, bind to service  and start.
     */
    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");

        // if already alive, just bind

        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        } else {
            Log.i(TAG, "playIntent != null");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");

        if (!MusicService.exiting) {
            SharedPreferences sp = getSharedPreferences(SPREF_KEY, Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(SPREF_INT_CURRENT_POSITION, MusicService.mPlayer.getCurrentPosition());
            editor.putInt(SPREF_INT_LIST_POSITION, MusicService.songPosition);
            editor.apply();
        }

    }

    /**
     * Unbind MusicService if app is closed, then stop and nullify MusicService. Also close bookmark Database
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");

        if (musicConnection != null && musicBound) {
            unbindService(musicConnection);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(PlayerFragment.mNotificationId);
    }

    /**
     * Ensure that back button has same function as home button, to prevent crash.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    /**
     * Search phone for music tracks and add to the newly created song array list. Return false if no tracks
     */
    public boolean getSongList() {
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
            int songPodcast = musicCursor.getColumnIndex(MediaStore.Audio.Media.IS_PODCAST);
            int songMusic = musicCursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC);
            // Add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisDuration = musicCursor.getString(songDuration);

                // Workaround for filtering invalid tracks and podcasts todo - invert to get podcasts
                if (thisDuration != null && musicCursor.getString(songPodcast) != null) {
                    songList.add(new Song(thisId, thisTitle, thisArtist, thisDuration, pos));
                    pos += 1;
                }
            }
            while (musicCursor.moveToNext());
        }

        if (musicCursor != null) {
            musicCursor.close();
        }

        if (songList.size() == 0) {
            Toast.makeText(this, "No audio files found on device", Toast.LENGTH_LONG).show();
            finish();
            return false;
        } else {
            Log.i(TAG, "Song array created with " + songList.size() + " songs.");
            return true;
        }
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
     * To set ActionBar title to current Fragment name
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

        if (id == R.id.action_settings) {
            Log.i(TAG, "Settings button");
            return true;
        } else if (id == R.id.action_exit) {

            if (musicConnection != null && musicBound) {
                musicSrv.onDestroy();
                unbindService(musicConnection);
                musicBound = false;
            }

            if (playIntent != null) {
                stopService(playIntent);
            }

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(PlayerFragment.mNotificationId);


            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void onFragmentInteraction(String id) {
        Log.i(TAG, "onFragInteraction: " + id);
    }
}

