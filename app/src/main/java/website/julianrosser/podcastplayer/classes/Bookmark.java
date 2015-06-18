package website.julianrosser.podcastplayer.classes;

import android.util.Log;

import website.julianrosser.podcastplayer.MainActivity;

/**
 * Created by user on 14-May-15.
 */
public class Bookmark {

    public long id;
    private String title;
    private String artist;
    private String duration;
    private String currentPosition;
    private long durationInMillis;
    private int posInSongList;

    public Bookmark(Song song, String songPosition) {
        id = song.getID();
        title = song.getTitle();
        artist = song.getArtist();
        duration = song.getLength();
        durationInMillis = song.getLengthMillis();
        currentPosition = songPosition;
        posInSongList = song.getPos();
    }

    public long getID() {
        return id;
    }

    public int getPos() {
        return posInSongList;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getLength() { return (duration); }

    public String getCurrentPosition() {return convertTime(currentPosition);}

    public int getCurrentPositionInMillis() {return Integer.valueOf(currentPosition);}

    public String getPositionPercentage() {

        double pos = Long.valueOf(currentPosition);

        String percent = "" + (int) Math.round(pos / durationInMillis * 100);

        return percent + "%";
    }










    // Method for converting track duration to minutes & seconds
    private String convertTime(String miliseconds) {
        // Time conversion
        double songLength = Double.valueOf(miliseconds) / 1000;

        return (int) songLength / 60 + ":" + String.format(  "%02d", (int) songLength % 60);
    }
}
