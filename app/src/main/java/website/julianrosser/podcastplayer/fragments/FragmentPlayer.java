package website.julianrosser.podcastplayer.fragments;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import website.julianrosser.podcastplayer.R;
import website.julianrosser.podcastplayer.activities.MainActivity;
import website.julianrosser.podcastplayer.dialogs.DialogSaveBookmark;
import website.julianrosser.podcastplayer.dialogs.DialogViewBookmarks;
import website.julianrosser.podcastplayer.helpers.DatabaseOpenHelper;
import website.julianrosser.podcastplayer.objects.AudioFile;
import website.julianrosser.podcastplayer.objects.Bookmark;
import website.julianrosser.podcastplayer.services.ServiceMusic;


/**
 * A fragment containing a the player / controlls
 */
public class FragmentPlayer extends android.support.v4.app.Fragment {


    public static final int DIALOG_VIEW_BOOKMARKS = 200;
    public static final int DIALOG_SAVE_BOOKMARK = 250;
    // The fragment argument representing the section number for this fragment.
    private static final String ARG_SECTION_NUMBER = "section_number";
    // For logging purposes
    private static final String TAG = "PlayerFragment";
    public static String formattedPosition;
    // Sets an ID for the notification
    public static int mNotificationId = 111;
    // View references
    public static SeekBar seekBar;
    public static ImageButton playPause;
    public static ImageButton bookmarkSkipPrev;
    public static ImageButton bookmarkSkipNext;
    public static TextView textSongTitle;
    public static TextView textSongArtist;
    public static TextView textSongCurrent;
    public static TextView textSongLength;
    public static ArrayList<Bookmark> bookmarks;
    public static ProgressBar progressBarLoading;
    static DialogViewBookmarks dialogView;
    int mStackLevel = 0;
    private String SPREF_STRING_POS_FORMATTED = "trackCurrentPosFormatted";
    private String SPREF_INT_POS_SEEKBAR = "trackCurrentPosSeekbar";

    /**
     * Required empty public constructor
     */
    public FragmentPlayer() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FragmentPlayer newInstance(int sectionNumber) {
        FragmentPlayer fragment = new FragmentPlayer();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public static void addNewBookmark(String note) {
        // Values reference
        ContentValues values = new ContentValues();

        // Get song
        AudioFile s = MainActivity.audioFileList.get(ServiceMusic.songPosition);

        // Get String values of names, other info
        values.put(DatabaseOpenHelper.ARTIST_NAME, s.getArtist());
        values.put(DatabaseOpenHelper.ARTIST_NAME, s.getArtist());
        values.put(DatabaseOpenHelper.TRACK_NAME, s.getTitle());
        values.put(DatabaseOpenHelper.UNIQUE_ID, s.getIDString());
        values.put(DatabaseOpenHelper.BOOKMARK_NOTE, note);

        double songCurrentPos = Double.valueOf(String.valueOf(ServiceMusic.mPlayer.getCurrentPosition()));
        values.put(DatabaseOpenHelper.BOOKMARK_MILLIS, ((int) songCurrentPos));

        // Format bookmark percent
        int percentFormatted = (int) ((songCurrentPos / s.getLengthMillis()) * 100);
        values.put(DatabaseOpenHelper.BOOKMARK_PERCENT, percentFormatted);

        // Format poition string
        formattedPosition = " (" + s.getLength() + ")";
        values.put(DatabaseOpenHelper.BOOKMARK_FORMATTED, formattedPosition);

        // Add values to new database row
        MainActivity.mDB.insert(DatabaseOpenHelper.TABLE_NAME, null, values);

        checkForBookmarks();

    }

    /**
     * Return an object array which holds all bookmarks for the current song. Set Alpha for skip buttons
     */
    public static void checkForBookmarks() {
        if (MainActivity.mDbHelper.bookmarkAlreadyExists(MainActivity.audioFileList.get(ServiceMusic.songPosition).getID())) {

            bookmarks = MainActivity.mDbHelper.getBookmarksForCurrentTrack(MainActivity.audioFileList.get(ServiceMusic.songPosition).getID());

            Log.i(TAG, "Bookmarks length: " + bookmarks.size());

            // todo use Service.bookmarkArray to populate skippable PlayerFragment array
            /*
            bookmarkSkipPrev.setAlpha(1f);
            bookmarkSkipNext.setAlpha(1f);
            bookmarkSkipPrev.setClickable(true);
            bookmarkSkipNext.setClickable(true);

        } else {
            bookmarkSkipPrev.setAlpha(0.5f);
            bookmarkSkipNext.setAlpha(0.5f);
            bookmarkSkipPrev.setClickable(false);
            bookmarkSkipNext.setClickable(false); // todo - check these */
        }
    }

    public  void closeOpenDialog() {
        if (dialogView != null) {
            dialogView.closeDialog();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        bookmarks = new ArrayList<>();
        Log.i(TAG, "Bookmark Ref: " + bookmarks);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate view
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        final Typeface fontRobotoRegular = Typeface.createFromAsset(
                getActivity().getAssets(),
                "Roboto-Regular.ttf");

        // Set up play / pause button
        playPause = (ImageButton) view.findViewById(R.id.buttonPlay);
        playPause.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View view) {

                if (MainActivity.musicSrv != null && MainActivity.musicBound && !ServiceMusic.isPreparing) {
                    // if already playing, pause
                    if (MainActivity.musicSrv.isPng()) {
                        MainActivity.musicSrv.pausePlayer();
                        playPause.setImageDrawable(getResources().getDrawable(R.drawable.play));
                        swipeableNotification();
                    } else {

                        MainActivity.musicSrv.resume();
                        if (!MainActivity.firstSongPlayed) {
                            startTimer();
                        }

                        MainActivity.firstSongPlayed = true;

                        playPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));

                        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(FragmentPlayer.mNotificationId);
                    }

                } else {
                    Log.e(TAG, "SERVICE NULL / PLAYER NOT BOUND " + MainActivity.musicSrv + MainActivity.musicBound);
                }
            }

        });

        // Initialize TextViews
        textSongCurrent = (TextView) view.findViewById(R.id.textSongTimeCurrent);
        textSongLength = (TextView) view.findViewById(R.id.textSongTimeLength);
        textSongTitle = (TextView) view.findViewById(R.id.songTitle);
        textSongArtist = (TextView) view.findViewById(R.id.songArtist);

        // Set font
        textSongCurrent.setTypeface(fontRobotoRegular);
        textSongLength.setTypeface(fontRobotoRegular);
        textSongTitle.setTypeface(fontRobotoRegular);
        textSongArtist.setTypeface(fontRobotoRegular);

        // Set track information if service is initialised
        if (MainActivity.musicBound && MainActivity.musicSrv != null) { // or still loading

            textSongTitle.setText(ServiceMusic.songTitle);
            textSongArtist.setText(ServiceMusic.songArtist);
            textSongCurrent.setText(ServiceMusic.songCurrentPosition);
            textSongLength.setText(ServiceMusic.songDuration);

            // set button to play
            if (MainActivity.musicSrv.isPng()) {
                //noinspection deprecation
                playPause.setImageDrawable(getResources().getDrawable(R.drawable.play));
            }
        }

        // Rewind button listener
        ImageButton rewind = (ImageButton) view.findViewById(R.id.buttonRewind);
        rewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ServiceMusic.loadFromBookmark = false;

                if (ServiceMusic.mPlayer.getCurrentPosition() < 3000) {
                    MainActivity.musicSrv.playPrev();
                } else {
                    MainActivity.musicSrv.playCurrent();
                }

                if (!MainActivity.firstSongPlayed) {
                    startTimer();
                }

            }
        });

        // Forward button listener
        ImageButton forward = (ImageButton) view.findViewById(R.id.buttonForward);
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ServiceMusic.loadFromBookmark = false;
                if (ServiceMusic.shuffleMode(getActivity())) {
                    MainActivity.musicSrv.playRandom();
                } else {
                    MainActivity.musicSrv.playNext();
                }

                if (!MainActivity.firstSongPlayed) {
                    startTimer();
                }
            }
        });

        /* Bookmark listener
        ImageButton buttonBookmark = (ImageButton) view.findViewById(R.id.mainBookmarkButtonSave);
        buttonBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showDialog(DIALOG_SAVE_BOOKMARK);

            }
        });

        bookmarkSkipPrev = (ImageButton) view.findViewById(R.id.buttonBookmarkSkipPrev);
        bookmarkSkipPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // todo
            }
        });

        bookmarkSkipNext = (ImageButton) view.findViewById(R.id.buttonBookmarkSkipNext);
        bookmarkSkipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // todo
            }
        }); */

        // Seek bar listener
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setMax(MainActivity.SEEKBAR_RATIO);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                if (fromUser && MainActivity.musicSrv != null) {

                    MainActivity.musicSrv.seek(i);

                    String formatted = AudioFile.convertTime(String.valueOf(ServiceMusic.mPlayer.getCurrentPosition()));
                    FragmentPlayer.textSongCurrent.setText(formatted);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        ImageView im = (ImageView) view.findViewById(R.id.imageView);

//        Song s = MainActivity.songList.get(MusicService.songPosition);
        //long albumID = s.getAlbumID();

        //Bitmap bm = MainActivity.albumArt(getActivity(), albumID);

        //Log.i(TAG, "Bitmap: " + bm.getWidth() + " / " + bm.getHeight() + " / " + bm.toString() + " / ");

        //im.setImageBitmap(bm);


        // Start timer if not the first time fragment is opened
        if (MainActivity.firstSongPlayed) {
            startTimer();
        }

        progressBarLoading = (ProgressBar) view.findViewById(R.id.progressBar);


        // If playing, show pause button.
        if (MainActivity.musicBound && ServiceMusic.mPlayer != null && ServiceMusic.mPlayer.isPlaying()) {
            //noinspection deprecation
            playPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
        }

        return view;

    }

    /**
     * Thread which updates seekbar and position textviews. First ensure Sevice has started
     */
    public void startTimer() {
        new Thread(new Runnable() {
            @Override

            public void run() {
                // Ensure Service is initialized
                for (int i = 0; i < 30; i++) {
                    if (ServiceMusic.mPlayer == null) {
                        Log.i(getClass().getSimpleName(), "Player Progress Tracker - Music Service not initialized: " + i);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Log.i(TAG, "Thread started");

                // update textview while service is alive
                while (ServiceMusic.mPlayer != null && FragmentPlayer.seekBar != null && !ServiceMusic.exiting) {

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Get MainActivity for UI Thread
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Local reference for millis

                                if (FragmentPlayer.seekBar != null && !ServiceMusic.isPreparing && !ServiceMusic.exiting) {
                                    FragmentPlayer.seekBar.setProgress(ServiceMusic.getCurrentProgress());
                                    String formatted = AudioFile.convertTime(String.valueOf(ServiceMusic.mPlayer.getCurrentPosition()));
                                    FragmentPlayer.textSongCurrent.setText(formatted);
                                }
                            }
                        });
                    }
                }
                Log.i(TAG, "Thread finished");
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();

        MainActivity.mTitle = "Now Playing";

        checkForBookmarks();

        if (ServiceMusic.isPreparing) {
            progressBarLoading.setVisibility(View.VISIBLE);
        }

        if (ServiceMusic.mPlayer != null && MainActivity.musicBound) {

            // Load position and id of last played
            SharedPreferences sp = getActivity().getSharedPreferences(MainActivity.SPREF_KEY, Activity.MODE_PRIVATE);
            textSongCurrent.setText(sp.getString(SPREF_STRING_POS_FORMATTED, "0:00"));
            seekBar.setProgress(sp.getInt(SPREF_INT_POS_SEEKBAR, 0));

        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!ServiceMusic.exiting) {
            SharedPreferences sp = getActivity().getSharedPreferences(MainActivity.SPREF_KEY, Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(SPREF_STRING_POS_FORMATTED, textSongCurrent.getText().toString());
            editor.putInt(SPREF_INT_POS_SEEKBAR, seekBar.getProgress());
            editor.apply();
        }
    }

    /**
     * Required lifecycle methods
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //AudioPlayerService.release();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // AudioPlayerService.stop();
    }

    public boolean isFragmentUIActive() {
        return isAdded() && !isDetached() && !isRemoving() && isResumed();
    }

    /**
     * Workaround function for having swipeable notification when paused. Can't work out how to update
     * foreground noti and allow to be dismissed. Come back to this later.
     */
    public void swipeableNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getActivity())
                        .setSmallIcon(R.drawable.ic_pause_dark)
                        .setTicker(ServiceMusic.songTitle)
                        .setContentTitle(ServiceMusic.songTitle)
                        .setContentText(ServiceMusic.songArtist)
                        .setAutoCancel(true);

        Intent resultIntent = new Intent(getActivity(), MainActivity.class);
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        getActivity(),
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        // Pass the Notification to the NotificationManager:
        NotificationManager mNotificationManager = (NotificationManager) getActivity()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { // api
            // 16+
            mNotificationManager.notify(mNotificationId,
                    mBuilder.build());
        } else {
            mNotificationManager.notify(mNotificationId,
                    mBuilder.getNotification());
        }

    }

    public void showDialog(int type) {

        mStackLevel++;

        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
        Fragment prev = getActivity().getFragmentManager().findFragmentByTag("dialogView"); // todo something here to destinguish
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        switch (type) {

            case DIALOG_VIEW_BOOKMARKS:

                if (!MainActivity.mDbHelper.bookmarkAlreadyExists(MainActivity.audioFileList.get(ServiceMusic.songPosition).getID())) {
                    Toast.makeText(getActivity(), "No bookmarks saved for 'TRACK_NAME'", Toast.LENGTH_SHORT).show();

                    break;

                } else {
                    dialogView = (DialogViewBookmarks) DialogViewBookmarks.newInstance(123, getActivity());
                    dialogView.setTargetFragment(this, DIALOG_VIEW_BOOKMARKS);
                    dialogView.show(getFragmentManager().beginTransaction(), "dialogView");

                    break;
                }


            case DIALOG_SAVE_BOOKMARK:

                DialogFragment dialogFragmentSave = DialogSaveBookmark.newInstance(123, getActivity());
                dialogFragmentSave.setTargetFragment(this, DIALOG_SAVE_BOOKMARK);
                dialogFragmentSave.show(getFragmentManager().beginTransaction(), "dialogSave");

                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_fragment_nowplaying, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_view_bookmarks) {

            showDialog(DIALOG_VIEW_BOOKMARKS);

        } else if (id == R.id.action_add_bookmark) {

            showDialog(DIALOG_SAVE_BOOKMARK);
        }

        return super.onOptionsItemSelected(item);


    }

}