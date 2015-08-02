package website.julianrosser.podcastplayer.helpers;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import website.julianrosser.podcastplayer.MainActivity;
import website.julianrosser.podcastplayer.MusicService;
import website.julianrosser.podcastplayer.R;
import website.julianrosser.podcastplayer.fragments.PlayerFragment;

public class SaveBookmarkDialog extends DialogFragment {


    public static SaveBookmarkDialog newInstance(int num, Context mContext){

        SaveBookmarkDialog dialogFragment = new SaveBookmarkDialog(mContext);
        Bundle bundle = new Bundle();
        bundle.putInt("num", num);
        dialogFragment.setArguments(bundle);

        return dialogFragment;

    }

    public SaveBookmarkDialog(Context mContext) {

        final MainActivity mActivityContext = (MainActivity) mContext;

        // Instantiate an AlertDialog.Builder with its constructor
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mActivityContext, R.style.AlertDialogCustom));

        // Get the layout inflater
        final LayoutInflater inflater = mActivityContext.getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_bookmark_save, null);

        final CheckBox cb = (CheckBox) v.findViewById(R.id.dialog_bookmark_checkbox);
        final EditText et = (EditText) v.findViewById(R.id.dialog_bookmark_note);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(v);

        // todo title info
        //String stringPosition = Song.convertTime(String.valueOf(MusicService.mPlayer.getCurrentPosition()));
        //builder.setTitle("Bookmark - " + stringPosition);

        // Set positive button
        builder.setPositiveButton(R.string.dialog_bookmark_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // If Checkbox isn't checked, delete old bookmarks
                if (cb.isChecked()) {

                    for (int k = 0; k < DatabaseOpenHelper.bookmarksToDelete.size(); k++) {
                        Log.i("SaveBookmarkDialog", "Delete row with ID: " + DatabaseOpenHelper.bookmarksToDelete.get(k));
                        MainActivity.mDbHelper.deleteEntryFromID(   DatabaseOpenHelper.bookmarksToDelete.get(k)    );
                    }
                }

                String note = et.getText().toString();

                // Call method to add bookmark
                PlayerFragment.addNewBookmark(note);

                // Notify user that the bookmark was saved
                Toast.makeText(mActivityContext, "Bookmark saved " + PlayerFragment.formattedPosition, Toast.LENGTH_LONG).show();
            }
        });

        // Chain together various setter methods to set the dialog characteristics
        builder.setNegativeButton(R.string.dialog_bookmark_negative, null);

        // If no bookmarks exist, no need to ask question, so hide checkbox layout
        if (!MainActivity.mDbHelper.bookmarkAlreadyExists(MainActivity.songList.get(MusicService.songPosition).getID())) {

            final LinearLayout ll = (LinearLayout) v.findViewById(R.id.dialog_bookmark_old_layout);
            ll.setVisibility(View.GONE);
        }

        // Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();

    }

/*
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.ERROR)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.ok_button,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
                            }
                        }
                )
                .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
                    }
                })
                .create();
    } */

}
