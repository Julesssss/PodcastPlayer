package website.julianrosser.podcastplayer.bookmarks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import website.julianrosser.podcastplayer.MainActivity;
import website.julianrosser.podcastplayer.R;
import website.julianrosser.podcastplayer.classes.Bookmark;

public class BookmarkListAdapter extends BaseAdapter {

    public LayoutInflater inflater;

    public BookmarkListAdapter(Context c) {
        inflater = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return MainActivity.bookmarkList.size();
    }

    @Override
    public Bookmark getItem(int i) {
        return MainActivity.bookmarkList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return MainActivity.bookmarkList.get(i).getID();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {


        final ViewHolder holder;
        final Bookmark b = getItem(position);

        if (null == convertView) {
            holder = new ViewHolder();

            convertView = inflater.inflate(R.layout.bookmark_list_view, null);

            holder.title = (TextView) convertView.findViewById(R.id.songListTitle);
            holder.position = (TextView) convertView.findViewById(R.id.songListPosition);

            convertView.setTag(holder);


        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(b.getTitle() + " - " + b.getArtist());
        String percent = b.getPositionPercentage();
        holder.position.setText(percent + "   -   " + b.getCurrentPosition() + " / " + b.getLength());

        return convertView;
    }


    // classes
    static class ViewHolder {
        TextView title;
        TextView position;

    }


}
