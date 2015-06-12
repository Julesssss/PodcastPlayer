package website.julianrosser.podcastplayer;

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
        return duration;
    }
}
