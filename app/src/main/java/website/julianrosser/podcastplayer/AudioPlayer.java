package website.julianrosser.podcastplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class AudioPlayer {

    private MediaPlayer mPlayer;

    public AudioPlayer(Context c) {
        Log.i("context", "Context: " + c.toString());
        mPlayer = MediaPlayer.create(c, R.raw.i);
        Log.i("context", "mPlayer: " + mPlayer.toString());
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                stop();
            }
        });
    }

    public void play() {
        // Start MediaPlayer
        mPlayer.start();
    }

    public void pause() {
        mPlayer.pause();
    }

    public void resume() {
        mPlayer.start();
    }

    public void stop() {
        mPlayer.stop();
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

    public void seekTo(int i) {
        mPlayer.seekTo((this.getLength() / 100) * i);
    }

    public int getCurrentProgress() {
        return mPlayer.getCurrentPosition();
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
        if (null == mPlayer) {
            return false;
        } else {
            return mPlayer.isPlaying();
        }

    }

}
