package website.julianrosser.podcastplayer.activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import website.julianrosser.podcastplayer.R;
import website.julianrosser.podcastplayer.fragments.FragmentBookmark;
import website.julianrosser.podcastplayer.fragments.FragmentHelp;
import website.julianrosser.podcastplayer.fragments.FragmentLibrary;
import website.julianrosser.podcastplayer.fragments.FragmentNavigationDrawer;
import website.julianrosser.podcastplayer.fragments.FragmentPlayer;
import website.julianrosser.podcastplayer.fragments.FragmentPreferences;
import website.julianrosser.podcastplayer.helpers.DatabaseOpenHelper;
import website.julianrosser.podcastplayer.objects.AudioFile;
import website.julianrosser.podcastplayer.services.ServiceMusic;

public class MainActivity extends AppCompatActivity
        implements FragmentNavigationDrawer.NavigationDrawerCallbacks, FragmentHelp.OnFragmentInteractionListener, FragmentPreferences.OnFragmentInteractionListener, FragmentLibrary.OnFragmentInteractionListener, FragmentBookmark.OnFragmentInteractionListener {


    private static final BitmapFactory.Options sBitmapOptionsCache = new BitmapFactory.Options();
    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

    // Array of songs
    public static ArrayList<AudioFile> audioFileList;
    // To check if MusicService is bound to Activity
    public static boolean musicBound = false;
    // To prevent song starting on first play
    public static boolean firstPreparedSong = true;
    // to check if first song has played yet


    public static boolean firstSongPlayed = false;
    // Reference to music service
    public static ServiceMusic musicSrv;
    // Reference to PlayerFragment
    public static FragmentPlayer fragmentPlayer;
    public static FragmentBookmark fragmentBookmark;
    public static FragmentLibrary fragmentLibrary;
    public static FragmentPreferences fragmentPreferences;
    public static FragmentHelp fragmentHelp;


    // Used to store the last screen title. For use in {@link #restoreActionBar()}.
    public static CharSequence mTitle;
    // SQL Database reference
    public static SQLiteDatabase mDB = null;
    // Reference to Database helper class
    public static DatabaseOpenHelper mDbHelper;
    // Seekbar ratio reference
    public static int SEEKBAR_RATIO = 1000;
    public static int bookmarkSortInt;
    // For loading last played in correct position
    public static String SPREF_KEY = "your_prefs";
    // Bundle
    public int lastPlayedListPosition;
    public int lastPlayedCurrentPosition;
    public String SPREF_INT_LIST_POSITION = "songListPosition";
    public String SPREF_INT_CURRENT_POSITION = "songCurrentPosition";
    public String SPREF_INT_BOOKMARK_ORDER = "bookmarkOrder";
    // For logging purposes
    private String TAG = getClass().getSimpleName();
    // Intent used for binding service to Activity
    private Intent playIntent;
    // Fragment managing the behaviors, interactions and presentation of the navigation drawer.
    private FragmentNavigationDrawer mFragmentNavigationDrawer;
    /**
     * connect to the service
     */
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            ServiceMusic.MusicBinder binder = (ServiceMusic.MusicBinder) service;
            // Get service
            musicSrv = binder.getService();
            // Pass song list to - Should I check for empty list here?
            musicSrv.setList(MainActivity.audioFileList);
            // update boolean to show service is bound
            musicBound = true;


            // Check that songs exist on device
            if (audioFileList.size() > 0 && ServiceMusic.mPlayer != null) {

                // Check if this is first open (Or error with last played song)
                if (lastPlayedListPosition == -1 || lastPlayedCurrentPosition == -1) {
                    // First time
                    MainActivity.musicSrv.setSongAtPosButDontPlay(0);
                } else {
                    if (!ServiceMusic.mPlayer.isPlaying()) {

                        MainActivity.firstPreparedSong = true;

                        ServiceMusic.millisecondToSeekTo = lastPlayedCurrentPosition;

                        MainActivity.musicSrv.setSongAtPosButDontPlay(lastPlayedListPosition);

                        if (MainActivity.musicSrv != null && FragmentPlayer.seekBar != null) {
                            FragmentPlayer.seekBar.setProgress((int) ServiceMusic.songBookmarkSeekPosition);
                            FragmentPlayer.textSongCurrent.setText(AudioFile.convertTime(String.valueOf(ServiceMusic.millisecondToSeekTo)));
                        }
                    } else {
                        // Already playing, so just set TextViews
                        ServiceMusic.updateTextViews();
                    }
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    // Get album art for specified album. This method will not try to
    // fall back to getting artwork directly from the file, nor will
    // it attempt to repair the database.
    public static Bitmap getArtworkQuick(Context context, long album_id, Uri artUri, int w, int h) {
        // NOTE: There is in fact a 1 pixel border on the right side in the ImageView
        // used to display this drawable. Take it into account now, so we don't have to
        // scale later.

        w -= 1;
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(artUri, album_id);
        if (uri != null) {
            ParcelFileDescriptor fd = null;
            try {
                fd = res.openFileDescriptor(uri, "r");
                int sampleSize = 1;

                // Compute the closest power-of-two scale factor
                // and pass that to sBitmapOptionsCache.inSampleSize, which will
                // result in faster decoding and better quality
                sBitmapOptionsCache.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, sBitmapOptionsCache);
                int nextWidth = sBitmapOptionsCache.outWidth >> 1;
                int nextHeight = sBitmapOptionsCache.outHeight >> 1;
                while (nextWidth > w && nextHeight > h) {
                    sampleSize <<= 1;
                    nextWidth >>= 1;
                    nextHeight >>= 1;
                }

                sBitmapOptionsCache.inSampleSize = sampleSize;
                sBitmapOptionsCache.inJustDecodeBounds = false;
                Bitmap b = BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, sBitmapOptionsCache);

                if (b != null) {
                    // finally rescale to exactly the size we need
                    if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
                        Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
                        // Bitmap.createScaledBitmap() can return the same bitmap
                        if (tmp != b) b.recycle();
                        b = tmp;
                    }
                }

                return b;
            } catch (FileNotFoundException e) {
            } finally {
                try {
                    if (fd != null)
                        fd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.i("BITMAP FAC", "NULL URI");
        return null;
    }

    static public Bitmap albumArt(Context c, long albumId) {
        Bitmap artwork = BitmapFactory.decodeResource(c.getResources(),
                R.drawable.audio_icon);
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, albumId);
        ContentResolver res = c.getContentResolver();
        InputStream in;
        try {
            in = res.openInputStream(uri);
            artwork = BitmapFactory.decodeStream(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return artwork;
    }

    public static Bitmap getAlbumart(Long album_id, Context c) {
        Bitmap bm = BitmapFactory.decodeResource(c.getResources(),
                R.drawable.audio_icon);

        try {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

            Log.i("ART", "URI; " + uri.toString() + "  /  " + uri.getPath());

            ParcelFileDescriptor pfd = c.getContentResolver()
                    .openFileDescriptor(uri, "r");


            if (pfd != null) {
                Log.i("ART", "pfd: " + pfd.toString());
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            } else {
                Log.i("ART", "NOOOOO");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Pass toolbar as ActionBar for functionality
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        Log.i(TAG, "onCreate()");

        // Initialize song list
        audioFileList = new ArrayList<>();

        // Search for songs and update list, if songs are available
        getSongList();

        // Ensure volume buttons work correctly
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Get Navigation Drawer reference
        mFragmentNavigationDrawer = (FragmentNavigationDrawer)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the navigation drawer.
        mFragmentNavigationDrawer.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Get current Fragment title
        mTitle = getTitle();

        // Create a new DatabaseHelper
        mDbHelper = new DatabaseOpenHelper(this);

        // Get the underlying database for writing
        mDB = mDbHelper.getWritableDatabase();

        // Load position and id of last played
        SharedPreferences sp = getSharedPreferences(SPREF_KEY, Activity.MODE_PRIVATE);
        lastPlayedListPosition = sp.getInt(SPREF_INT_LIST_POSITION, -1);
        lastPlayedCurrentPosition = sp.getInt(SPREF_INT_CURRENT_POSITION, -1);
        bookmarkSortInt = sp.getInt(SPREF_INT_BOOKMARK_ORDER, -1);
        // todo - Songlist may have changed, this would make list position incorrect. Maybe should use different ID?

        //
        if (lastPlayedListPosition >= audioFileList.size()) {
            lastPlayedListPosition = 0;
            // todo - now don't load from bookmark
        }

        ServiceMusic.exiting = false;

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
            playIntent = new Intent(this, ServiceMusic.class);
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

        saveSharedPreferences();
    }

    /**
     * Unbind MusicService if app is closed, then stop and nullify MusicService. Also close bookmark Database
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");

        saveSharedPreferences();

        exitApp();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(FragmentPlayer.mNotificationId);
    }

    public void saveSharedPreferences() {

        if (!ServiceMusic.exiting && ServiceMusic.mPlayer != null) {
            SharedPreferences sp = getSharedPreferences(SPREF_KEY, Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(SPREF_INT_CURRENT_POSITION, ServiceMusic.mPlayer.getCurrentPosition());
            editor.putInt(SPREF_INT_LIST_POSITION, ServiceMusic.songPosition);
            editor.putInt(SPREF_INT_BOOKMARK_ORDER, bookmarkSortInt);
            editor.apply();
        }
    }

    /**
     * Ensure that back button has same function as home button, to prevent crash.
     */
    @Override
    public void onBackPressed() {
        Log.i(TAG, "NOW, NOW - Fragment active: " + !fragmentPlayer.isFragmentUIActive());

        if (!fragmentPlayer.isFragmentUIActive()) {
            super.onBackPressed();

        } else {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
    }

    /**
     * Search phone for music tracks and add to the newly created song array list. Return false if no tracks
     */
    public boolean getSongList() {

        int positionInSongList = 0;
        // Retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        // Get preference to check if in Podcast Mode
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean podcastMode = sharedPref.getBoolean(getResources().getString(R.string.pref_key_podcastmode), false);

        if (podcastMode) {
            Log.i(TAG, "AudioMode: Podcast Only");
        } else {
            Log.i(TAG, "AudioMode: All Audio");
        }

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
                long thisAlbumID = musicCursor.getLong(musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

                // Bitmap bit = BitmapFactory.decodeFile("");


                // Workaround for filtering invalid tracks and podcasts todo - invert to get podcasts
                if (thisDuration != null && musicCursor.getString(songPodcast) != null) {


                    if (podcastMode) {

                        if (musicCursor.getString(songPodcast).equals("1")) {

                            Log.i(TAG, "Found with podcast tag");
                            audioFileList.add(new AudioFile(thisId, thisTitle, thisArtist, thisDuration, positionInSongList, thisAlbumID));
                            positionInSongList += 1;

                        } else if (Integer.valueOf(musicCursor.getString(songDuration)) > 1200000) {

                            Log.i(TAG, "Found with long duration");

                            audioFileList.add(new AudioFile(thisId, thisTitle, thisArtist, thisDuration, positionInSongList, thisAlbumID));
                            positionInSongList += 1;

                        }
                    } else {

                        if (!musicCursor.getString(songPodcast).equals("-1")) {
                            audioFileList.add(new AudioFile(thisId, thisTitle, thisArtist, thisDuration, positionInSongList, thisAlbumID));
                            positionInSongList += 1;
                        }
                    }
                }
            }
            while (musicCursor.moveToNext());
        }

        if (musicCursor != null) {
            musicCursor.close();
        }

        if (audioFileList.size() == 0) {
            Toast.makeText(this, "No audio files found on device", Toast.LENGTH_LONG).show();
            finish();
            return false;
        } else {
            Log.i(TAG, "Song array created with " + audioFileList.size() + " files.");
            return true;
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        getNewFragment(position);

        if (position == 0) {
            getNewPlayerFragment(position);
        } else if (position == 1) {
            getNewBookmarkFragment(position);
        } else if (position == 2) {
            getNewLibraryFragment(position);
        } else if (position == 3) {
            getNewPreferenceFragment(position);
        } else if (position == 4) {
            getNewHelpFragment(position);
        } else {
            Log.e(TAG, "Nav drawer id error");
        }
    }

    public void getNewFragment(int i) {

        android.support.v4.app.Fragment newFragment;

        if (i == 0) {
            // If already exists, use. else get new
            if (fragmentPlayer == null) {
                fragmentPlayer = FragmentPlayer.newInstance(i + 1);
            }

            newFragment = fragmentPlayer;
        }
    }

    /**
     * Create a new PlayerFragment and start with the FragmentManager
     */
    public void getNewPlayerFragment(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentPlayer == null) {
            fragmentPlayer = FragmentPlayer.newInstance(position + 1);
        } else {
            mTitle = "Now Playing";
        }

        if (fragmentPlayer.isVisible()) {
            // do nothing
        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragmentPlayer)
                    .commit();
        }

    }

    /**
     * Create a new BookmarkFragment and start with the FragmentManager
     */
    public void getNewBookmarkFragment(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentBookmark == null) {
            fragmentBookmark = FragmentBookmark.newInstance(position + 1);
        } else {
            mTitle = "Bookmarks";
        }

        if (fragmentPlayer.isFragmentUIActive()) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragmentBookmark)
                    .addToBackStack("bookmarkFragment")
                    .commit();

            // If desired IS current, ignore
        } else if (fragmentBookmark.isVisible()) {
            // do nothing

        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragmentBookmark)
                    .commit();
        }


    }

    /**
     * Create a new LibraryFragment and start with the FragmentManager
     */
    private void getNewLibraryFragment(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentLibrary == null) {
            fragmentLibrary = FragmentLibrary.newInstance(position + 1);
        } else {
            mTitle = "Library"; // todo - strings refffff
        }

        if (fragmentPlayer.isFragmentUIActive()) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragmentLibrary)
                    .addToBackStack("libraryFragment")
                    .commit();

            // If desired IS current, ignore
        } else if (fragmentLibrary.isVisible()) {
            // do nothing

        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragmentLibrary)
                    .commit();
        }

    }

    /**
     * Create a new PreferenceFragment and start with the FragmentManager
     */
    private void getNewPreferenceFragment(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentPreferences == null) {
            fragmentPreferences = FragmentPreferences.newInstance(position + 1);
        } else {
            mTitle = "Settings";
        }

        if (fragmentPlayer.isFragmentUIActive()) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragmentPreferences)
                    .addToBackStack("prefFragment")
                    .commit();

            // If desired IS current, ignore
        } else if (fragmentPreferences.isVisible()) {
            // do nothing

        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragmentPreferences)
                    .commit();
        }
    }

    /**
     * Create a new HelpFragment and start with the FragmentManager
     */
    private void getNewHelpFragment(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentHelp == null) {
            fragmentHelp = FragmentHelp.newInstance(position + 1);
        } else {
            mTitle = "Help";
        }

        if (fragmentPlayer.isFragmentUIActive()) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragmentHelp)
                    .addToBackStack("helpFragment")
                    .commit();

            // If desired IS current, ignore
        } else if (fragmentHelp.isVisible()) {
            // do nothing

        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragmentHelp)
                    .commit();
        }
    }

    public void exitApp() {

        ServiceMusic.exiting = true;

        if (musicConnection != null && musicBound) {
            musicSrv.onDestroy();
            unbindService(musicConnection);
            musicBound = false;
        }

        if (playIntent != null) {
            stopService(playIntent);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(FragmentPlayer.mNotificationId);

        finish();
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
            case 5:
                mTitle = getString(R.string.title_section5);
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
        if (!mFragmentNavigationDrawer.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            //getMenuInflater().inflate(R.menu.main, menu);
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


        return super.onOptionsItemSelected(item);
    }

    public void onFragmentInteraction(String id) {
        Log.i(TAG, "onFragInteraction: " + id);
    }
}

