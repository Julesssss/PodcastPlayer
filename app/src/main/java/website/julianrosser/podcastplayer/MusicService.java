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


public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    private static final int NOTIFY_ID = 1;
    static String songArtist = "";
    static String songTitle = "";
    // Binder returned to Activity
    private final IBinder musicBind = new MusicBinder();
    //media mPlayer
    private MediaPlayer mPlayer;
    //song list
    private ArrayList<Song> songs;
    //current position
    private int songPosition;
    private String TAG = getClass().getSimpleName();

    public MusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //initialize position
        songPosition = 0;
        //create mPlayer
        mPlayer = new MediaPlayer();

        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        initMusicPlayer();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    public void initMusicPlayer() {
        //set mPlayer properties
        mPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public void playSong() {
        //play a song
        mPlayer.reset();
        //get song
        Song playSong = songs.get(songPosition);
        songTitle = playSong.getTitle();
        songArtist = playSong.getArtist();
        //get id
        long currSong = playSong.getID();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);

        try {
            mPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        mPlayer.prepareAsync();

        // If in view & not null, set player title to song
        if (MainActivity.playerFragment != null && MainActivity.playerFragment.isVisible()) {
            Log.i(TAG, "Visable");

            if (PlayerFragment.textSongTitle != null) {
                Log.i(TAG, "TextView 1 Not null");
                PlayerFragment.textSongTitle.setText(songTitle);
            }
            if (PlayerFragment.textSongArtist != null) {
                Log.i(TAG, "TextView 2 Not null");
                PlayerFragment.textSongArtist.setText(songArtist);
            }
        }

    }

    public void setList(ArrayList<Song> theSongs) {
        songs = theSongs;
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

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mPlayer.reset();
        playNext();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        // todo extraaaaa
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        // todo if wanted start else just load
        if (!MainActivity.loadOnly) {
            mediaPlayer.start();
            MainActivity.loadOnly = false;
        }


        Log.i(getClass().getSimpleName(), "onPrepared");

        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);
        // Gte Bitmap drawable
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.play);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.notification_play_icon)
                .setLargeIcon(largeIcon)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle(songTitle)
                .setContentText(songArtist);

        Notification notification;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { // api
            // 16+
            notification = builder.build();

        } else {
            //noinspection deprecation
            notification = builder.getNotification();
        }

        startForeground(NOTIFY_ID, notification);


    }

    public void setSong(int songIndex) {
        songPosition = songIndex;
    }

    @Override
    public void onAudioFocusChange(int result) {
        Log.i(TAG, "AudioFocusChanged: " + result);
    }

    public int getPosn() {
        return mPlayer.getCurrentPosition();
    }

    public int getDur() {
        return mPlayer.getDuration();
    }

    public boolean isPng() {
        return mPlayer.isPlaying();
    }

    public void pausePlayer() {
        mPlayer.pause();
    }

    public void seek(int posn) {
        mPlayer.seekTo(posn);
    }

    public void go() {
        mPlayer.start();
    }

    public void playPrev() {
        songPosition--;
        if (songPosition < 0) songPosition = songs.size() - 1;
        playSong();
    }

    //skip to next
    public void playNext() {
        songPosition++;
        if (songPosition == songs.size()) songPosition = 0;
        playSong();
    }

    //skip to next
    public void playRandom() {
        songPosition = new Random().nextInt(songs.size() + 1);
        if (songPosition == songs.size()) songPosition = 0;
        playSong();
    }

    public void loadLastSong() {
        //play a song
        mPlayer.reset();
        //get song
        Song playSong = songs.get(songPosition);
        songTitle = playSong.getTitle();
        songArtist = playSong.getArtist();
        //get id
        long currSong = playSong.getID();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);

        try {
            mPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        mPlayer.prepareAsync();

        // If in view & not null, set player title to song
        if (MainActivity.playerFragment != null && MainActivity.playerFragment.isVisible()) {
            Log.i(TAG, "Visable");

            if (PlayerFragment.textSongTitle != null) {
                Log.i(TAG, "TextView 1 Not null");
                PlayerFragment.textSongTitle.setText(songTitle);
            }
            if (PlayerFragment.textSongArtist != null) {
                Log.i(TAG, "TextView 2 Not null");
                PlayerFragment.textSongArtist.setText(songArtist);
            }
        }


    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
}
