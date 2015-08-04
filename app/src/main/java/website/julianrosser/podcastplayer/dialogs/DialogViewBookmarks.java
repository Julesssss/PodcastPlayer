package website.julianrosser.podcastplayer.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.util.ArrayList;

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

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivityContext, R.style.AppCompatAlertDialogStyle);

        // At least one BM exists, so...
        // todo use Service.bookmarkArray to populate list

        builder.setTitle("Bookmarks");

        builder.setPositiveButton("Done", null);

        ArrayList bookmarks = DatabaseOpenHelper.bookmarksToDelete;

        String[] bookmarksStringArray = new String[bookmarks.size()];

        for (int i = 0; i < bookmarks.size(); i++) {
            bookmarksStringArray[i] = "Bookmark " + i; // todo - get bm info (percent & note)
        }

        builder.setItems(bookmarksStringArray, new DialogInterface.OnClickListener() { // todo replace with bm's
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                Log.i("DVB", "Load bookmark: " + position);
            }
        });


        return builder.create();
    }
}
