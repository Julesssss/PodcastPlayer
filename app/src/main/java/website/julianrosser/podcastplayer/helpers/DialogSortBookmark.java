package website.julianrosser.podcastplayer.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import website.julianrosser.podcastplayer.MainActivity;
import website.julianrosser.podcastplayer.R;

public class DialogSortBookmark extends DialogFragment {

    public static String DATA_SORTING_KEY = "SortBookmarkFragmentKey";

    public static MainActivity mActivityContext;

    public int tempBookmarkSort = MainActivity.bookmarkSortInt;

    public static android.support.v4.app.DialogFragment newInstance(int num, Context mContext) {

        mActivityContext = (MainActivity) mContext;

        DialogSortBookmark dialogFragment = new DialogSortBookmark();
        Bundle bundle = new Bundle();
        bundle.putInt("num", num);
        dialogFragment.setArguments(bundle);

        return dialogFragment;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivityContext, R.style.AppCompatAlertDialogStyle);

        // Creating and Building the Dialog
        builder.setSingleChoiceItems(getResources().getStringArray(R.array.arraySortOptions), MainActivity.bookmarkSortInt, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int item) {

                tempBookmarkSort = item;
            }
        });


        builder.setPositiveButton(R.string.dialog_bookmark_sort_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent data = new Intent();
                data.putExtra(DATA_SORTING_KEY, tempBookmarkSort);
                Log.i("SortBD", "Data: " + tempBookmarkSort);
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);

                MainActivity.bookmarkSortInt = tempBookmarkSort;
            }
        });

        builder.setNegativeButton(R.string.dialog_bookmark_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                tempBookmarkSort = MainActivity.bookmarkSortInt;
            }
        });

        // builder.setTitle("Sort by");

        // builder.setView(view);

        return builder.create();
    }
}
