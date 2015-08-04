package website.julianrosser.podcastplayer.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import website.julianrosser.podcastplayer.helpers.DatabaseOpenHelper;
import website.julianrosser.podcastplayer.activities.ActivityMain;
import website.julianrosser.podcastplayer.services.ServiceMusic;
import website.julianrosser.podcastplayer.R;
import website.julianrosser.podcastplayer.fragments.FragmentNowPlaying;
import website.julianrosser.podcastplayer.objects.AudioFile;

public class DialogSaveBookmark extends DialogFragment {

    public static ActivityMain mActivityContext;

    public static android.support.v4.app.DialogFragment newInstance(int num, Context mContext) {

        mActivityContext = (ActivityMain) mContext;

        DialogSaveBookmark dialogFragment = new DialogSaveBookmark();
        Bundle bundle = new Bundle();
        bundle.putInt("num", num);
        dialogFragment.setArguments(bundle);

        return dialogFragment;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // AlertDialog.Builder builder = new AlertDialog.Builder(mActivityContext, R.style.AppCompatAlertDialogStyle);

        //final MainActivity mActivityContext = (MainActivity) mContext;

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
                        ActivityMain.mDbHelper.deleteEntryFromID(DatabaseOpenHelper.bookmarksToDelete.get(k));
                    }
                }

                String note = et.getText().toString();

                // Call method to add bookmark
                FragmentNowPlaying.addNewBookmark(note);

                // Get song
                AudioFile s = ActivityMain.audioFileList.get(ServiceMusic.songPosition);
                double songCurrentPos = Double.valueOf(String.valueOf(ServiceMusic.mPlayer.getCurrentPosition()));
                int percentFormatted = (int) ((songCurrentPos / s.getLengthMillis()) * 100);

                // Notify user that the bookmark was saved
                String toastMessage;

                if (note.length() == 0) {
                    toastMessage = "Bookmark saved at " + percentFormatted + "%";
                } else {
                    toastMessage = "Bookmark saved at " + percentFormatted + "% - '" + note + "'";
                }

                Toast.makeText(mActivityContext, toastMessage, Toast.LENGTH_LONG).show();
            }
        });

        // Chain together various setter methods to set the dialog characteristics
        builder.setNegativeButton(R.string.dialog_bookmark_negative, null);

        // If no bookmarks exist, no need to ask question, so hide checkbox layout
        if (!ActivityMain.mDbHelper.bookmarkAlreadyExists(ActivityMain.audioFileList.get(ServiceMusic.songPosition).getID())) {

            final LinearLayout ll = (LinearLayout) v.findViewById(R.id.dialog_bookmark_old_layout);
            ll.setVisibility(View.GONE);
        }

        return builder.create();
    }

}
