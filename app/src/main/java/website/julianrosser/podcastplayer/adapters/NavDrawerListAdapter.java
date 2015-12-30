package website.julianrosser.podcastplayer.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import website.julianrosser.podcastplayer.R;
import website.julianrosser.podcastplayer.fragments.NavDrawerFragment;

public class NavDrawerListAdapter extends BaseAdapter {

    final Typeface fontRobotoMedium;
    public LayoutInflater inflater;
    String[] sections;
    Context mContext;

    public NavDrawerListAdapter(Context c, String[] sectionsInput) {

        inflater = LayoutInflater.from(c);
        mContext = c;
        sections = sectionsInput;
        fontRobotoMedium = Typeface.createFromAsset(
                c.getAssets(),
                "Roboto-Medium.ttf");
    }

    @Override
    public int getCount() {
        return NavDrawerFragment.sections.length;
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

            convertView = inflater.inflate(R.layout.listview_navigation_bar, null);

            holder.title = (TextView) convertView.findViewById(R.id.navListTitle);
            holder.title.setTypeface(fontRobotoMedium);
            holder.title.setTextColor(Color.WHITE);

            // Get Icon ImageView reference and set NavDrawer icons
            holder.icon = (ImageView) convertView.findViewById(R.id.navListImageViewIcon);

            if (position == 0) {
                holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_headset_white_24dp));
            } else if (position == 1) {
                holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_library_books_white_24dp));
            } else if (position == 2) {
                holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_library_music_white_24dp));
            } else if (position == 3) {
                holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_settings_white_24dp));
            } else if (position == 4) {
                holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_help_white_24dp));
            } else if (position == 5) {
                holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_exit_to_app_white_24dp));
            }



            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(sections[position]);

        return convertView;
    }

    // classes
    static class ViewHolder {
        TextView title;
        ImageView icon;

    }
}
