package website.julianrosser.podcastplayer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SongListAdapter extends BaseAdapter {

    public LayoutInflater inflater;

    public SongListAdapter(Context c) {
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

            convertView = inflater.inflate(R.layout.song_list_view, null);

            holder.title = (TextView) convertView.findViewById(R.id.songListTitle);

            convertView.setTag(holder);


        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(s.getTitle());

        return convertView;
    }


    // classes
    static class ViewHolder {
        TextView title;

    }


}
