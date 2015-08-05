package website.julianrosser.podcastplayer.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import website.julianrosser.podcastplayer.adapters.AdapterDialogViewBookmarks;
import website.julianrosser.podcastplayer.helpers.DatabaseOpenHelper;
import website.julianrosser.podcastplayer.activities.ActivityMain;
import website.julianrosser.podcastplayer.R;

public class DialogViewBookmarks extends DialogFragment {

    public static String DATA_SORTING_KEY = "ViewBookmarksFragmentKey";

    public static ActivityMain mActivityContext;

    public static DialogFragment newInstance(int num, Context mContext) {

        mActivityContext = (ActivityMain) mContext;

        DialogViewBookmarks dialogFragment = new DialogViewBookmarks();
        Bundle bundle = new Bundle();
        bundle.putInt("num", num);
        dialogFragment.setArguments(bundle);

        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mActivityContext, R.style.AlertDialogCustom));

        // Get the layout inflater
        LayoutInflater inflater = mActivityContext.getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_bookmark_view, null);

        ListView listView = (ListView) v.findViewById(R.id.nowplaying_dialog_view_listview);
        listView.setAdapter(new AdapterDialogViewBookmarks(getActivity()));

        builder.setView(v);


        builder.setTitle("Bookmarks");

        builder.setPositiveButton("Done", null);

        return builder.create();
    }
}
