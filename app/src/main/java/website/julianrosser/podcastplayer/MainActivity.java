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
import android.widget.TextView;

import java.util.ArrayList;

/**
 * LOG
 *
 * 11/06 - Opened project for the first time in weeks, NOT a good idea to leave bugs for a long time,
 * took me a while to work out what the problem was then debug.
 * 12/06 - Built library listview fragment, layout view, added title and activity callbacks. Duration textview and code.
 * 13/06 - Worked on converting time for display. Put method in service, so it's called when needed, no need to do for every song.
 */

/**
 * TODO
 * - Override back button
 * - play button when paused and click next
 * - touch seek bar to expand
 * - only update seek bar when in view
 * - load files on refresh normally Including button! ????????????? done?
 */

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, LibraryFragment.OnFragmentInteractionListener {

    public static ArrayList<Song> songList;
    public static MusicService musicSrv;
    // Fragment managing the behaviors, interactions and presentation of the navigation drawer.
    private NavigationDrawerFragment mNavigationDrawerFragment;
    // Used to store the last screen title. For use in {@link #restoreActionBar()}.
    private CharSequence mTitle;
    private String TAG = getClass().getSimpleName();
    private Intent playIntent;
    static boolean musicBound = false;

    static PlayerFragment playerFragment;

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(MainActivity.songList);
            musicBound = true;

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

        songList = new ArrayList<Song>();

        getSongList();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Get Navigation Drawer
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        mTitle = getTitle();

        // Create and start service, don't play yet
        Log.i(TAG, "Starting Service...");
        //Intent mAudioPlayerService = new Intent(this, AudioPlayerService.class);
        //mAudioPlayerService.setAction(AudioPlayerService.ACTION_INIT);
        //startService(mAudioPlayerService);

        //startService(new Intent(AudioPlayerService.ACTION_FOREGROUND).setClass(this, AudioPlayerService.class));

        // Set up Media Controller Widget
        //setController();

        /**
         * Stop service
         *
         * stopService(new Intent(this, AudioPlayerService.class));
         *
         */


    }

    @Override
    public void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }

    }

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    public void getSongList() {
        Log.i(TAG, "getSongList");
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int songDuration = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisDuration = musicCursor.getString(songDuration);

                if (thisDuration != null) {
                    songList.add(new Song(thisId, thisTitle, thisArtist, thisDuration));
                }
                //if (Double.valueOf(thisDuration) < 10000) {
                //    Log.i(TAG, "Y");
                //} else {
                //    Log.i(TAG, "N");
                //}


            }
            while (musicCursor.moveToNext());
        }
        if (musicCursor != null) {
            musicCursor.close();
        }

        // If empty, don't play??
        if (songList.size() == 0){
            // todo do somthing
        }

        Log.i(TAG, "Song list size: " + songList.size());

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
        } else if (position == 3) {
            getNewPlaylistFragment(position);
        } else {
            Log.e(TAG, "Nav drawer id error");
        }
    }

    public void getNewPlayerFragment(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();


        playerFragment = PlayerFragment.newInstance(position + 1);

        fragmentManager.beginTransaction()
                .replace(R.id.container, playerFragment)
                .commit();
    }

    public void getNewLibraryFragment(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        LibraryFragment libraryFragment = LibraryFragment.newInstance(position + 1);

        fragmentManager.beginTransaction()
                .replace(R.id.container, libraryFragment)
                .commit();
    }

    private void getNewBookmarkFragment(int position) {
        // do nothing yet
    }

    private void getNewPlaylistFragment(int position) {
        // do nothing yet
    }

    /**
     * For each choice, if not currently displayed, display fragment and set title.
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
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_random) {
            musicSrv.playRandom();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //play next
    static void playNext() {
        musicSrv.playNext();
    }

    //play previous
    static public void playPrev() {
        musicSrv.playPrev();
    }

    @Override
    public void onFragmentInteraction(String id) {
        Log.i(TAG, "onFragInter: " + id);
    }

    /**
     * MediaPlayer controller methods


    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public void pause() {
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (musicSrv != null && musicBound && musicSrv.isPng()) {
            return musicSrv.getDur();
        } else {
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        if (musicSrv != null && musicBound && musicSrv.isPng()) {
            return musicSrv.getPosn();
        } else {
            return 0;
        }
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        return musicSrv != null && musicBound && musicSrv.isPng();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    static boolean canSeekForward() {
        return true;
    }

    @Override
    static int getAudioSessionId() {
        return 0;
    }
     */




}

