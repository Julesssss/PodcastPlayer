package website.julianrosser.podcastplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.util.Random;

public class AudioPlayer {

    public Context mContext;
    private static MediaPlayer mPlayer;
    static boolean isPrepared;

    public AudioPlayer(Context c, Uri track) {
        mContext = c;
        Log.i("context", "Context: " + c.toString());
        mPlayer = MediaPlayer.create(c, track);
        Log.i("context", "mPlayer: " + mPlayer.toString());

        isPrepared = false;
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

    public void setTrack(Uri path) {

        mPlayer.stop();
        mPlayer.reset();
        try {
            mPlayer.setDataSource(mContext, path);
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

    public void resume() {
        mPlayer.start();
        getCheckPreparedThread().start();
    }

    public void stop() {
        mPlayer.stop();
    }

    public int getPlayTime() {
        return mPlayer.getCurrentPosition();
    }

    public void rewind() {
        mPlayer.seekTo(getPlayTime() - 2500);
        getCheckPreparedThread().start();
    }

    public void forward() {
        mPlayer.seekTo(getPlayTime() + 2500);

        getCheckPreparedThread().start();
    }

    public void seekTo(int i) {
        mPlayer.seekTo((this.getLength() / 1000) * i);
    }

    public int getCurrentProgress() {
        Log.i(getClass().getSimpleName(), "Cur Pos: " + mPlayer.getCurrentPosition() + "  /  Dur  " + mPlayer.getDuration());
        double pos = mPlayer.getCurrentPosition();
        double dur = mPlayer.getDuration();
        double prog = (pos / dur) * 1000;

        return (int)Math.round(prog * 10) / 10;
    }



    public int getLength() {
        return mPlayer.getDuration();
    }

    public void release() {
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

                    PlayerFragment.seekBar.setProgress(PlayerFragment.mPlayer.getCurrentProgress());
                }
                Log.i("TAG", "NOT PLAYING, SO STOP");

            }
        }).start();
    }

}
