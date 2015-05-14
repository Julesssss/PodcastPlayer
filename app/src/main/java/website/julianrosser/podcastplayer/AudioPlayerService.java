package website.julianrosser.podcastplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import java.util.Random;

public class AudioPlayerService extends Service {

    public static MediaPlayer mPlayer;
    static final String TAG = "AudioPlayerService";
    public Context mContext;

    // For checking prepared
    static boolean isPrepared;

    public static final String ACTION_INIT = "INIT";
    public static final String ACTION_SET_TRACK = "SET_TRACK";
    public static final String ACTION_PLAY = "PLAY";
    public static final String ACTION_PAUSE = "PAUSE";
    public static final String ACTION_FOREGROUND = "FOREGROUND";
    public static final String ACTION_FORWARD = "FORWARD";
    public static final String ACTION_REWIND = "REWIND";
    public static final String ACTION_STOP = "STOP";


    public AudioPlayerService() {
    }

    /**
     * Don't think its needed, but keep it just in case
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        // TODO: Return the communication channel to the service.
        throw null; //new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        mPlayer = new MediaPlayer();


    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        String action = intent.getAction();
        switch (action) {
            case ACTION_INIT:
                Log.i(TAG, "Action Init");
                initPlayer();
                return 6;
            case ACTION_PLAY:
                Log.i(TAG, "Action Play");
                play();
                return 7;
            case ACTION_FOREGROUND:
                Log.i(TAG, "Action Play");
                moveToForeground();
                return 37;
            case ACTION_PAUSE:
                Log.i(TAG, "Action Pause");
                return 8;
            case ACTION_SET_TRACK:
                Log.i(TAG, "Action Pause");
                setTrack(PlayerFragment.tracks[new Random().nextInt(PlayerFragment.tracks.length)]);
                return 18;
            default:
                Log.i(TAG, "Action UNKNOWN");
                return 9;
        }
    }

    private void moveToForeground() {
        Notification notification = new Notification(R.drawable.abc_ic_menu_share_mtrl_alpha, getText(R.string.ticker_text),
                System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, AudioPlayerService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, getText(R.string.notification_title),
                getText(R.string.notification_message), pendingIntent);
        startForeground(1, notification);
    }

    private void initPlayer() {

        mPlayer = MediaPlayer.create(this, PlayerFragment.tracks[new Random().nextInt(PlayerFragment.tracks.length)]);

        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                isPrepared = true;
            }
        });

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                setTrack(PlayerFragment.tracks[new Random().nextInt(PlayerFragment.tracks.length)]);
            }
        });
    }

    public void play() {
        // Start MediaPlayer
        mPlayer.start();
        getCheckPreparedThread().start();
    }

    public  void setTrack(Uri path) {

        mPlayer.stop();
        mPlayer.reset();
        try {
            mPlayer.setDataSource(this, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mPlayer.prepare();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        if (!mPlayer.isLooping()) {
            mPlayer.start();
        }

        getCheckPreparedThread().start();


    }

    public void pause() {
        mPlayer.pause();
    }

    public static void resume() {
        mPlayer.start();
        getCheckPreparedThread().start();
    }

    public static void stop() {
        mPlayer.stop();
    }

    public static int getPlayTime() {
        return mPlayer.getCurrentPosition();
    }

    public static void rewind() {
        mPlayer.seekTo(getPlayTime() - 2500);
        getCheckPreparedThread().start();
    }

    public static void forward() {
        mPlayer.seekTo(getPlayTime() + 2500);

        getCheckPreparedThread().start();
    }

    public static void seekTo(int i) {
        mPlayer.seekTo((getLength() / 1000) * i);
    }

    public static int getCurrentProgress() {
        Log.i(TAG, "Cur Pos: " + mPlayer.getCurrentPosition() + "  /  Dur  " + mPlayer.getDuration());
        double pos = mPlayer.getCurrentPosition();
        double dur = mPlayer.getDuration();
        double prog = (pos / dur) * 1000;

        return (int)Math.round(prog * 10) / 10;
    }



    public static int getLength() {
        return mPlayer.getDuration();
    }

    public static void release() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public Boolean getIsPlaying() {
        return null != mPlayer && mPlayer.isPlaying();

    }

    public boolean getIsPrepared(){
        return isPrepared;
    }

    /**
     *
     */
    public static Thread getCheckPreparedThread() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 20; i > 0; i--) {
                    if (!isPrepared) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        startProgressTracker();
                        break;
                    }
                }
            }
        });
    }

    public static void startProgressTracker() {
        Log.i("TAG", "startProgressTracker()");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mPlayer != null && mPlayer.isPlaying()) {
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    PlayerFragment.seekBar.setProgress(getCurrentProgress());
                }
                Log.i("TAG", "NOT PLAYING, SO STOP");

            }
        }).start();
    }
}
