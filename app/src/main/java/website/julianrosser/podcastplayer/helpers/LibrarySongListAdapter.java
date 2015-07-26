package website.julianrosser.podcastplayer.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import website.julianrosser.podcastplayer.MainActivity;
import website.julianrosser.podcastplayer.R;
import website.julianrosser.podcastplayer.objects.Song;

public class LibrarySongListAdapter extends BaseAdapter {

    public LayoutInflater inflater;

    public LibrarySongListAdapter(Context c) {
        inflater = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return MainActivity.songList.size();
    }

    @Override
    public Song getItem(int i) {
        return MainActivity.songList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return MainActivity.songList.get(i).getID();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {


        final ViewHolder holder;
        final Song s = getItem(position);

        if (null == convertView) {
            holder = new ViewHolder();

            convertView = inflater.inflate(R.layout.library_song_list_view, null);

            holder.title = (TextView) convertView.findViewById(R.id.songListTitle);
            holder.artist = (TextView) convertView.findViewById(R.id.songListArtist);

            convertView.setTag(holder);


        } else {
            holder = (ViewHolder) convertView.getTag();
        }



        holder.title.setText(s.getTitle());
        holder.artist.setText(s.getArtist() + " (" + s.getLength() + ")");

        return convertView;
    }


    // classes
    static class ViewHolder {
        TextView title;
        TextView artist;

    }


}
