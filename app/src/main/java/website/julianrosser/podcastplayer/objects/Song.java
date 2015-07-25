package website.julianrosser.podcastplayer.objects;

/**
 * A Song Object is created for each audiofile file found on device. Keeps track of title, artist, length & id information
 */
public class Song {

    public long id;
    private String title;
    private String artist;
    private String duration;
    private int posInSongList;

    public Song(long songID, String songTitle, String songArtist, String songLength, int pos) {
        id = songID;
        title = songTitle;
        artist = songArtist;
        duration = songLength;
        posInSongList = pos;
    }

    public long getID() {
        return id;
    }

    public String getIDString() {
        return String.valueOf(id);
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

    public String getLength() {
        return convertTime(duration);
    }

    public long getLengthMillis() {
        return Long.valueOf(duration);
    }

    /**
     * Method for converting track duration to hour : minutes : seconds
     */
    private String convertTime(String milliseconds) {
        // Time conversion
        double songLength = Double.valueOf(milliseconds) / 1000;
        String formattedString;

        int h = (int) songLength / 60;

        if (h > 60) {
            formattedString = h / 60 + ":" + String.format("%02d", h % 60) + ":" + String.format("%02d", (int) songLength % 60);
        } else {
            formattedString = (int) songLength / 60 + ":" + String.format("%02d", (int) songLength % 60);
        }

        return formattedString;

    }


}
