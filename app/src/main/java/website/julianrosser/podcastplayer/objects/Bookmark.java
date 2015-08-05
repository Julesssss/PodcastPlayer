package website.julianrosser.podcastplayer.objects;

public class Bookmark {

    int id;
    int percent;
    int position;
    String note;

    public Bookmark(String idInt, int pct, String pos, String noteText) {

        id = Integer.valueOf(idInt);
        percent = pct;
        position = Integer.valueOf(pos);
        note = noteText;
    }

    public int getId() {
        return id;
    }

    public int getPercent() {
        return percent;
    }

    public int getPosition() {
        return position;
    }

    public String getNote() {
        return note;
    }
}
