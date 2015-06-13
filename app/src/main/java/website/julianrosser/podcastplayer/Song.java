package website.julianrosser.podcastplayer;

import android.provider.MediaStore;
import android.util.Log;

/**
 * Created by user on 14-May-15.
 */
public class Song {

    public long id;
    private String title;
    private String artist;
    private String duration;

    public Song(long songID, String songTitle, String songArtist, String songLength) {
        id = songID;
        title = songTitle;
        artist = songArtist;
        duration = songLength;
    }

    public long getID() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getLength() {
        Log.i("SONGGGG", "Duratttt:  " + duration);
        return convertTime(duration);
    }

    // Method for converting track duration to minutes & seconds
    private String convertTime(String miliseconds) {
        Log.i("SONG", "" + miliseconds);
        // Time conversion
        double songLength = Double.valueOf(miliseconds) / 1000;
        int mins = (int) songLength / 60;
        double secs = Math.round(songLength % 60);

        Log.i(getClass().getSimpleName(), "length: " + songLength + "  min: " + mins + "  secs: " + secs);

        return mins + ":" + (int)secs;
    }
}
