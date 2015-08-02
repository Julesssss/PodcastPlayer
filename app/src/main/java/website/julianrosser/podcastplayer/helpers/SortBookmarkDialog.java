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
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;

import website.julianrosser.podcastplayer.MainActivity;
import website.julianrosser.podcastplayer.R;
import website.julianrosser.podcastplayer.fragments.BookmarkFragment;

public class SortBookmarkDialog extends DialogFragment {

    public static String DATA_SORTING_KEY = "SortBookmarkFragmentKey";

    public static MainActivity mActivityContext;

    public int tempBookmarkSort = MainActivity.bookmarkSortInt;

    public static android.support.v4.app.DialogFragment newInstance(int num, Context mContext) {

        mActivityContext = (MainActivity) mContext;

        SortBookmarkDialog dialogFragment = new SortBookmarkDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("num", num);
        dialogFragment.setArguments(bundle);

        return dialogFragment;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivityContext);

        // Get the layout inflater
        final LayoutInflater inflater = mActivityContext.getLayoutInflater();


        /*
        View view = inflater.inflate(R.layout.dialog_bookmark_sort, null);
        ListView lv = (ListView) view.findViewById(R.id.dialog_bookmark_sort_listview);


        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mActivityContext, android.R.layout.simple_list_item_1) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                text1.setTextColor(Color.WHITE);
                return view;
            }
        };

        String[] arr = {"Date Added", "Title", "Artist", "Progress"};
        ArrayAdapter<String> ad = new ArrayAdapter<String>(mActivityContext, R.layout.listitem_textview_bookmark_sort, arr);
        lv.setAdapter(ad);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Build Intent, add data, pass back to Fragment

            }
        });  */

        String[] arr = {"Date Added", "Title", "Artist", "Progress"};

        // Creating and Building the Dialog
        builder.setSingleChoiceItems(arr, MainActivity.bookmarkSortInt, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int item) {

                tempBookmarkSort = item;
            }
        });

        builder.setPositiveButton(R.string.dialog_bookmark_positive, new  DialogInterface.OnClickListener() {
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

        builder.setTitle("Sort by");

        // builder.setView(view);

        return builder.create();
    }
}
