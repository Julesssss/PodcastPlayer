package website.julianrosser.podcastplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class AudioPlayer {

    private MediaPlayer mPlayer;
    private Boolean isPlaying;

    public AudioPlayer(Context c) {
        Log.i("context", "Context: " + c.toString());
        mPlayer = MediaPlayer.create(c, R.raw.i);
        Log.i("context", "mPlayer: " + mPlayer.toString());
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                stop();
            }
        });
        isPlaying = false;
    }

    public void play(Context c) {
        // Start MediaPlayer
        mPlayer.start();
        isPlaying = true;
    }

    public void pause() {
        mPlayer.pause();
        isPlaying = false;
    }

    public void resume() {
        mPlayer.start();
        isPlaying = true;
    }

    public void stop() {
        mPlayer.stop();
        isPlaying = false;
    }

    public int getPlayTime() {
        return mPlayer.getCurrentPosition();
    }

    public void rewind() {
        mPlayer.seekTo(getPlayTime() - 2500);
    }

    public void forward() {
        mPlayer.seekTo(getPlayTime() + 2500);
    }

    public void release() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public Boolean getIsPlaying() {
        if (null == mPlayer) {
            return false;
        } else {
            return isPlaying;
        }

    }

}
