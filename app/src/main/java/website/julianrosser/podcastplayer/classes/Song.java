package website.julianrosser.podcastplayer.classes;

import android.provider.MediaStore;
import android.util.Log;

import website.julianrosser.podcastplayer.MusicService;

/**
 * Created by user on 14-May-15.
 */
public class Song {

    public long id;
    private String title;
    private String artist;
    private String duration;
    private int posInSongList;
    private String filePath;

    public Song(long songID, String songTitle, String songArtist, String songLength, int pos, String path) {
        id = songID;
        title = songTitle;
        artist = songArtist;
        duration = songLength;
        posInSongList = pos;
        filePath = path;
    }

    public long getID() {
        return id;
    }

    public String getIDString () {
        return String.valueOf(id);
    }

    public int getPos() {return posInSongList;}

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getLength() {
        return convertTime(duration);
    }

    public long getLengthMillis() {
        return Long.valueOf(duration);
    }

    // Method for converting track duration to minutes & seconds
    private String convertTime(String miliseconds) {
        // Time conversion
        double songLength = Double.valueOf(miliseconds) / 1000;

        return (int) songLength / 60 + ":" + String.format(  "%02d", (int) songLength % 60);
        //return (int) songLength / 60 + ":" + (int) Math.round(songLength % 60);

    }


}
