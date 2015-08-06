package website.julianrosser.podcastplayer.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import website.julianrosser.podcastplayer.R;
import website.julianrosser.podcastplayer.activities.MainActivity;
import website.julianrosser.podcastplayer.fragments.FragmentPlayer;
import website.julianrosser.podcastplayer.objects.AudioFile;
import website.julianrosser.podcastplayer.objects.Bookmark;

public class AdapterDialogViewBookmarks extends BaseAdapter {

    final Typeface fontRobotoRegular;
    public LayoutInflater inflater;
    Context mContext;
    AlertDialog parentDialog;

    public AdapterDialogViewBookmarks(Context c, AlertDialog alertDialog) {

        parentDialog =alertDialog;
        inflater = LayoutInflater.from(c);
        mContext = c;
        fontRobotoRegular = Typeface.createFromAsset(
                c.getAssets(),
                "Roboto-Regular.ttf");
    }

    @Override
    public int getCount() {
        return FragmentPlayer.bookmarks.size();
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
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        final ViewHolder holder;

        Bookmark b = FragmentPlayer.bookmarks.get(position);

        if (null == convertView) {
            holder = new ViewHolder();

            convertView = inflater.inflate(R.layout.listview_bookmark_view_dialog, null);

            holder.layout_dialog_view_list = (RelativeLayout) convertView.findViewById(R.id.layout_dialog_view_list);

            holder.title = (TextView) convertView.findViewById(R.id.dialogViewTextFormatted);

            holder.percent = (TextView) convertView.findViewById(R.id.dialog_view_percent);

            holder.imageDelete = (ImageView) convertView.findViewById(R.id.dialog_view_image);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String formattedInfo = "" + AudioFile.convertTime(b.getFormattedPosition());

        if (b.getNote().length() > 0) {
            formattedInfo += " - '" + b.getNote() + "'";
        }

        holder.title.setText(formattedInfo);
        holder.title.setTypeface(fontRobotoRegular);

        holder.percent.setTypeface(fontRobotoRegular);
        holder.percent.setText(b.getPercent() + "%");

        holder.imageDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "Delete()", Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }

    // classes
    static class ViewHolder {
        RelativeLayout layout_dialog_view_list;
        TextView title;
        TextView percent;
        ImageView imageDelete;
    }
}
