package website.julianrosser.podcastplayer.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import website.julianrosser.podcastplayer.R;
import website.julianrosser.podcastplayer.MainActivity;
import website.julianrosser.podcastplayer.adapters.ViewBookmarksDialogAdapter;

public class BookmarkViewDialog extends DialogFragment {

    public static String DATA_SORTING_KEY = "ViewBookmarksFragmentKey";

    public static MainActivity mActivityContext;

    AlertDialog thisAlert;


    public static DialogFragment newInstance(int num, Context mContext) {

        mActivityContext = (MainActivity) mContext;

        BookmarkViewDialog dialogFragment = new BookmarkViewDialog();
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
        listView.setAdapter(new ViewBookmarksDialogAdapter(getActivity(), builder.create()));

        builder.setView(v);

        builder.setTitle("Bookmarks");

        thisAlert = builder.create();

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                closeDialog();
                Log.i(getClass().getSimpleName(), "CLOSE 2: A NEW HOPE");
            }
        });



        return thisAlert;
    }

    public  void closeDialog() {

        thisAlert.dismiss();

    }
}
