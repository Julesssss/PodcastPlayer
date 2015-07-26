package website.julianrosser.podcastplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

import website.julianrosser.podcastplayer.fragments.PlayerFragment;
import website.julianrosser.podcastplayer.objects.Song;


public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    // Notification id used when starting foreground Activity
    private static final int NOTIFY_ID = 1;
    // Song Information Strings
    public static String songArtist = "";
    public static String songTitle = "";
    public static String songDuration = "";
    public static String songCurrentPosition = "";
    public static double songBookmarkSeekPosition = 0;
    // MediaPlayer reference
    public static MediaPlayer mPlayer;
    // The position of current song in the Song ArrayList
    public static int songPosition = 0;
    // De decide between loading from bookmark or playing from start
    public static boolean loadFromBookmark;
    // Millisecond value of current bookmark
    public static int millisecondToSeekTo;
    // ArrayList of songs
    private static ArrayList<Song> songs;
    // Binder returned to Activity
    private final IBinder musicBind = new MusicBinder();
    // For logging purposes
    public String TAG = getClass().getSimpleName();

    public static void updateTextViews() {

        // Song reference
        Song playSong = songs.get(songPosition);

        //Get song Object and update information references
        songTitle = playSong.getTitle();
        songArtist = playSong.getArtist();
        songDuration = playSong.getLength();

        songBookmarkSeekPosition = ((double) millisecondToSeekTo / (double) playSong.getLengthMillis()) * 1000;

        // Format time to minutes, secs
        long second = (millisecondToSeekTo / 1000) % 60;
        int minutes = (millisecondToSeekTo / 1000) / 60;

        songCurrentPosition = String.valueOf(minutes) + ":" + String.format("%02d", second);

        // If Fragment is in view & not null, update track information TextViews
        if (MainActivity.playerFragment != null) {

            if (PlayerFragment.textSongTitle != null) {
                PlayerFragment.textSongTitle.setText(songTitle);
            }

            if (PlayerFragment.textSongArtist != null) {
                PlayerFragment.textSongArtist.setText(songArtist);
            }

            if (PlayerFragment.textSongLength != null) {
                PlayerFragment.textSongLength.setText(songDuration);
            }

            if (PlayerFragment.textSongCurrent != null) {
                PlayerFragment.textSongCurrent.setText(songCurrentPosition);
            }
        }
    }

    public static int getCurrentProgress() {
        double pos = mPlayer.getCurrentPosition();
        double dur = mPlayer.getDuration();
        double prog = (pos / dur) * MainActivity.SEEKBAR_RATIO;

        return (int) Math.round(prog * 10) / 10;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Create new MediaPlayer
        mPlayer = new MediaPlayer();

        // Set listeners as implemented methods
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);

        // Request Audio Focus to ensure app has priority when in use
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        // Start initialization methods
        initMusicPlayer();
    }

    /**
     * For setting up MusicPlayer settings
     */
    public void initMusicPlayer() {
        mPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onDestroy() {
        // Remove Service from foreground when closed
        stopForeground(true);
    }

    /**
     * Method for prepping MediaPlayer for new file and updating track information.
     */
    public void playSong() {

        Log.i(TAG, "Play Song");

        // Reset player
        mPlayer.reset();

        // Ensure track list isn't empty
        if (songs.size() >= 0) {

            // Get reference to current song
            Song playSong = songs.get(songPosition);

            // Get song ID, then create track URI
            long currSong = playSong.getID();
            Uri trackUri = ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    currSong);

            // Try to pass new Track to MediaPlayer
            try {
                mPlayer.setDataSource(getApplicationContext(), trackUri);
            } catch (Exception e) {
                Log.e("MUSIC SERVICE", "Error setting data source", e);
            }

            // Prepare Asynchronously
            mPlayer.prepareAsync();

            // Update TextViews
            updateTextViews();
        }

    }

    /**
     * Method for passing device song list from MainActivity
     */
    public void setList(ArrayList<Song> theSongs) {
        songs = theSongs;
    }

    /**
     * Decide what action to take when track finishes playing
     */
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.i(TAG, "onCompletion()");
        mPlayer.reset();

        if (MainActivity.shuffleMode) {
            playRandom();
        } else {
            playNext();
        }
    }

    /**
     * Method for handling individual MediaPlayer errors.
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        Log.e(getClass().getSimpleName(), String.format("onError() - Error(%s%s)", what, extra));

        // Specific error handling
        // if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {

        // Reset Player
        mp.reset();

        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.i(TAG, "onPrepared");
        // If loading from a bookmark, seek to required position
        if (loadFromBookmark) {
            mPlayer.seekTo(millisecondToSeekTo);
        }

        if (MainActivity.firstPreparedSong) {
            MainActivity.firstPreparedSong = false;
        } else {
            // Start playback
            mediaPlayer.start();
            //noinspection deprecation
            PlayerFragment.playPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
            loadFromBookmark = false;
        }

        // Create Intents for notification builder
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Initialize builder
        Notification.Builder builder = new Notification.Builder(this);

        // Get Bitmap drawable for notification image
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.play);

        // Build notification with required settings
        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.notification_play_icon)
                .setLargeIcon(largeIcon)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle(songTitle)
                .setContentText(songArtist);

        // Generic notification reference, needed to differentiate between old / new code below.
        Notification notification;

        // If running Android version 16+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { // api
            notification = builder.build();

        } else {
            // Running version 15 or below
            //noinspection deprecation
            notification = builder.getNotification();
        }

        // Start ongoing notification
        startForeground(NOTIFY_ID, notification);
    }

    /**
     * For playing chosen song
     */
    public void setSong(int songIndex) {
        mPlayer.reset();
        songPosition = songIndex;
        playSong();
    }

    /**
     * For playing song through bookmark Fragment.
     */
    public void setSongAtPos(int songIndex) {
        mPlayer.reset();
        songPosition = songIndex;
        playSong();
        loadFromBookmark = true;
    }

    /**
     * For playing song through bookmark Fragment.
     */
    public void setSongButDontPlay(int songIndex) {
        mPlayer.reset();
        songPosition = songIndex;
        playSong();
        loadFromBookmark = true;
    }

    @Override
    public void onAudioFocusChange(int result) {
        Log.i(TAG, "AudioFocusChanged: " + result);
    }

    /**
     * MediaPlayer Methods
     */
    public boolean isPng() {
        return mPlayer.isPlaying();
    }

    public void pausePlayer() {
        mPlayer.pause();
    }

    public void seek(int position) {
        mPlayer.seekTo((getLength() / MainActivity.SEEKBAR_RATIO) * position);
    }

    public int getLength() {
        return mPlayer.getDuration();
    }

    public void resume() {
        mPlayer.start();
    }

    public void playPrev() {
        songPosition--;
        if (songPosition < 0) songPosition = songs.size() - 1;
        playSong();
    }

    // play current song again
    public void playCurrent() {
        if (songPosition == songs.size()) songPosition = 0;
        playSong();
    }

    //skip to next
    public void playCurrentFromPosition() {
        if (songPosition == songs.size()) songPosition = 0;
        mPlayer.reset();
        playSong();
    }

    //skip to next
    public void playNext() {
        songPosition++;
        if (songPosition == songs.size()) songPosition = 0;
        playSong();
    }

    // Find random song
    public void playRandom() {

        int oldPos = songPosition;

        songPosition = new Random().nextInt(songs.size() + 1);

        for (int j = 0; j < 5; j++) {
            if (oldPos == songPosition) {
                songPosition = new Random().nextInt(songs.size() + 1);
            } else {
                break;
            }
        }

        if (songPosition == songs.size()) {
            songPosition = 0;
        }

        playSong();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mPlayer.stop();
        mPlayer.release();
        return false;
    }

    /**
     * Required Bind Methods
     */
    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
}