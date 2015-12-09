package website.julianrosser.podcastplayer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

import website.julianrosser.podcastplayer.activities.MainActivity;
import website.julianrosser.podcastplayer.R;
import website.julianrosser.podcastplayer.fragments.FragmentPlayer;
import website.julianrosser.podcastplayer.objects.AudioFile;


public class ServiceMusic extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    public static final String ACTION_PREVIOUS = "action_previous";
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
    public static boolean exiting;
    // Ensure MediaPlayer isn;t preparing
    public static boolean isPreparing = false;

    // Millisecond value of current bookmark
    public static int millisecondToSeekTo;
    // ArrayList of songs
    private static ArrayList<AudioFile> audioFiles;
    // Binder returned to Activity
    private final IBinder musicBind = new MusicBinder();
    public String NOTI_PLAY = "notificationPlay";
    public String NOTI_RESUME = "notificationResume";
    public String NOTI_PAUSE = "notificationPause";
    // For logging purposes
    public String TAG = getClass().getSimpleName();

    public static void updateTextViews() {

        // Song reference
        AudioFile playAudioFile = audioFiles.get(songPosition);

        //Get song Object and update information references
        songTitle = playAudioFile.getTitle();
        songArtist = playAudioFile.getArtist();
        songDuration = playAudioFile.getLength();

        songBookmarkSeekPosition = ((double) millisecondToSeekTo / (double) playAudioFile.getLengthMillis()) * 1000;
        // TODO - What if this is called while prepping?? -  if (!MusicService.isPreparing);

        songCurrentPosition = AudioFile.convertTime(String.valueOf(ServiceMusic.mPlayer.getCurrentPosition()));

        // If Fragment is in view & not null, update track information TextViews
        if (MainActivity.fragmentPlayer != null) {

            if (FragmentPlayer.textSongTitle != null) {
                FragmentPlayer.textSongTitle.setText(songTitle);
            }

            if (FragmentPlayer.textSongArtist != null) {
                FragmentPlayer.textSongArtist.setText(songArtist);
            }

            if (FragmentPlayer.textSongLength != null) {
                FragmentPlayer.textSongLength.setText(songDuration);
            }

            if (FragmentPlayer.textSongCurrent != null) {
                FragmentPlayer.textSongCurrent.setText(songCurrentPosition);
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
        Log.i(TAG, "onCreate");

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
        Log.i(TAG, "onDestroy");
        mPlayer.release();
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
        if (audioFiles.size() >= 0) {

            // Get reference to current song
            AudioFile playAudioFile = audioFiles.get(songPosition);

            FragmentPlayer.progressBarLoading.setVisibility(View.VISIBLE);

            // Get song ID, then create track URI
            long currSong = playAudioFile.getID();
            Uri trackUri = ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    currSong);

            // Try to pass new Track to MediaPlayer
            try {
                mPlayer.setDataSource(getApplicationContext(), trackUri);
            } catch (Exception e) {
                Log.e("MUSIC SERVICE", "Error setting data source", e);
            }


            isPreparing = true;

            // Prepare Asynchronously
            mPlayer.prepareAsync();

            // Update TextViews
            updateTextViews();
        }

    }

    /**
     * Method for passing device song list from MainActivity
     */
    public void setList(ArrayList<AudioFile> theAudioFiles) {
        audioFiles = theAudioFiles;
    }

    /**
     * Decide what action to take when track finishes playing
     */
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.i(TAG, "onCompletion()");
        mPlayer.reset();

        if (shuffleMode(this)) {
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "onStartCommand");
        handleIntent(intent);

        return START_STICKY;
    }

    private void handleIntent(Intent intent) {

        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        Log.i(TAG, "INTENT: " + action);

        if (action.equalsIgnoreCase(ACTION_PREVIOUS)) {
            Log.i(TAG, "ACTION PREVIOUS!!!!!!!!!!!!");
            // mController.getTransportControls().skipToPrevious();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.i(TAG, "onPrepared");

        FragmentPlayer.progressBarLoading.setVisibility(View.INVISIBLE); // todo - need to check fragment is alive????????/ --V

        isPreparing = false;

        // If loading from a bookmark, seek to required position
        if (loadFromBookmark) {
            mPlayer.seekTo(millisecondToSeekTo);
            millisecondToSeekTo = 0;
        }

        if (MainActivity.firstPreparedSong) {
            MainActivity.firstPreparedSong = false;
            mediaPlayer.start();
            mediaPlayer.pause();

        } else {
            // Start playback
            mediaPlayer.start();
            launchNotification(NOTI_PLAY);
            //noinspection deprecation
            FragmentPlayer.playPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));

            removePauseNotification();

        }

        FragmentPlayer.checkForBookmarks();

        loadFromBookmark = false;
    }


    public void launchNotification(String noti) {

        if (noti.equals(NOTI_PLAY) || noti.equals(NOTI_RESUME)) {

            // Create Intents for notification builder
            Intent notIntent = new Intent(this, MainActivity.class);
            notIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                    notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Initialize builder
            Notification.Builder builder = new Notification.Builder(this);

            // Get Bitmap drawable for notification image
            Bitmap largeIconPlay = BitmapFactory.decodeResource(getResources(), R.drawable.play);
            Bitmap largeIconPause = BitmapFactory.decodeResource(getResources(), R.drawable.pause);

            // Build notification with required settings
            builder.setContentIntent(pendInt)
                    .setSmallIcon(R.drawable.ic_play_dark)
                    .setTicker(songTitle)
                    .setContentTitle(songTitle)
                    .setContentText(songArtist)
                    .setOngoing(true);

            /*
                stopForeground(false);
                builder.setSmallIcon(R.drawable.ic_pause_dark);
                builder.setLargeIcon(largeIconPause);
                builder.setOngoing(false);
                */

            // Generic notification reference, needed to differentiate between old / new code below.
            Notification notification;





            // If running 5+, set as media controller
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                /* // todo ignore this ftm
                Intent intent = new Intent(MusicService.ACTION_PREVIOUS);
                PendingIntent pendingIntent = PendingIntent.getService(this, */
                      //  0 /* no requestCode */, intent, 0 /* no flags */);

                //builder.setStyle(new Notification.MediaStyle());
                //builder.addAction(new Notification.Action(android.R.drawable.ic_media_previous, "Previous", pendingIntent));


                builder.setLargeIcon(largeIconPlay);
                notification = builder.build();

                // If running Android version 16+
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                notification = builder.build();

            } else {
                // Running version 15 or below
                //noinspection deprecation
                notification = builder.getNotification();
            }

            // Start ongoing notification
            startForeground(NOTIFY_ID, notification);

        } else if (noti.equals(NOTI_PAUSE)) {
            stopForeground(true);
            //stopForeground(false);
        }
    }

    public void removePauseNotification() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(FragmentPlayer.mNotificationId);
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
    public void setSongAtPosButDontPlay(int songIndex) {
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
        launchNotification(NOTI_PAUSE);
    }

    public void seek(int position) {
        mPlayer.seekTo((getLength() / MainActivity.SEEKBAR_RATIO) * position);
    }

    public int getLength() {
        return mPlayer.getDuration();
    }

    public void resume() {
        mPlayer.start();
        launchNotification(NOTI_RESUME);
    }

    public void playPrev() {
        songPosition--;
        if (songPosition < 0) songPosition = audioFiles.size() - 1;
        playSong();
    }

    // play current song again
    public void playCurrent() {
        if (songPosition == audioFiles.size()) songPosition = 0;
        playSong();
    }

    //skip to next
    public void playNext() {
        songPosition++;
        if (songPosition == audioFiles.size()) songPosition = 0;
        playSong();
    }

    // Find random song
    public void playRandom() {

        int oldPos = songPosition;

        songPosition = new Random().nextInt(audioFiles.size() + 1);

        for (int j = 0; j < 5; j++) {
            if (oldPos == songPosition) {
                songPosition = new Random().nextInt(audioFiles.size() + 1);
            } else {
                break;
            }
        }

        if (songPosition == audioFiles.size()) {
            songPosition = 0;
        }

        playSong();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind");
        //mPlayer.stop();
        //mPlayer.reset();
        return false;
    }

    public static boolean shuffleMode(Context c) {
        // Get preference to check if in shuffle mode
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);

        return sharedPref.getBoolean("checkbox_pref_shuffle", false);
    }

    /**
     * Required Bind Methods
     */
    public class MusicBinder extends Binder {
        public ServiceMusic getService() {
            return ServiceMusic.this;
        }
    }
}