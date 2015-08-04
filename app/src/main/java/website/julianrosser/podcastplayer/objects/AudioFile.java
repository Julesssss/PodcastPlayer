package website.julianrosser.podcastplayer.objects;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * A Song Object is created for each audiofile file found on device. Keeps track of title, artist, length & id information
 */
public class AudioFile implements Parcelable {

    public long id;
    private String title;
    private String artist;
    private String duration;
    private int posInSongList;
    private long albumID;

    public AudioFile(long songID, String songTitle, String songArtist, String songLength, int pos, long intAlbumnID) {
        id = songID;
        title = songTitle;
        artist = songArtist;
        duration = songLength;
        posInSongList = pos;
        albumID = intAlbumnID;


        Log.i("SONG", "AID: '" + albumID + "'");
    }

    public long getAlbumID() {
        return albumID;
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
    public static String convertTime(String milliseconds) {
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


        protected AudioFile(Parcel in) {
            id = in.readLong();
            title = in.readString();
            artist = in.readString();
            duration = in.readString();
            posInSongList = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(id);
            dest.writeString(title);
            dest.writeString(artist);
            dest.writeString(duration);
            dest.writeInt(posInSongList);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<AudioFile> CREATOR = new Parcelable.Creator<AudioFile>() {
            @Override
            public AudioFile createFromParcel(Parcel in) {
                return new AudioFile(in);
            }

            @Override
            public AudioFile[] newArray(int size) {
                return new AudioFile[size];
            }
        };
    }