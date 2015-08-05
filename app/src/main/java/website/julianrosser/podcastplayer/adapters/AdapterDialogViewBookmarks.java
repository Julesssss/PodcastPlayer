package website.julianrosser.podcastplayer.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import website.julianrosser.podcastplayer.R;
import website.julianrosser.podcastplayer.fragments.FragmentNavigationDrawer;
import website.julianrosser.podcastplayer.fragments.FragmentNowPlaying;
import website.julianrosser.podcastplayer.objects.Bookmark;

public class AdapterDialogViewBookmarks extends BaseAdapter {

    final Typeface fontRobotoMedium;
    public LayoutInflater inflater;
    Context mContext;

    public AdapterDialogViewBookmarks(Context c) {

        inflater = LayoutInflater.from(c);
        mContext = c;
        fontRobotoMedium = Typeface.createFromAsset(
                c.getAssets(),
                "Roboto-Medium.ttf");
    }

    @Override
    public int getCount() {
        return FragmentNowPlaying.bookmarks.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        final ViewHolder holder;

        if (null == convertView) {
            holder = new ViewHolder();

            convertView = inflater.inflate(R.layout.listview_bookmark_view_dialog, null);

            holder.title = (TextView) convertView.findViewById(R.id.navListTitle);
            holder.title.setTypeface(fontRobotoMedium);

            Bookmark b = FragmentNowPlaying.bookmarks.get(position);

            String s = "BM: " + b.getPercent() + "%" + "  /  " + b.getNote();

            holder.title.setText(s);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        return convertView;
    }

    // classes
    static class ViewHolder {
        TextView title;

    }
}
