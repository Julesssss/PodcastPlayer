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

import website.julianrosser.podcastplayer.activities.ActivityMain;
import website.julianrosser.podcastplayer.objects.Bookmark;
import website.julianrosser.podcastplayer.services.ServiceMusic;
import website.julianrosser.podcastplayer.R;
import website.julianrosser.podcastplayer.helpers.DatabaseOpenHelper;
import website.julianrosser.podcastplayer.dialogs.DialogSaveBookmark;
import website.julianrosser.podcastplayer.dialogs.DialogViewBookmarks;
import website.julianrosser.podcastplayer.objects.AudioFile;


/**
 * A fragment containing a the player / controlls
 */
public class FragmentNowPlaying extends android.support.v4.app.Fragment {


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

    public static final int DIALOG_VIEW_BOOKMARKS = 200;
    public static final int DIALOG_SAVE_BOOKMARK = 250;

    int mStackLevel = 0;

    public static ProgressBar progressBarLoading;

    private String SPREF_STRING_POS_FORMATTED = "trackCurrentPosFormatted";
    private String SPREF_INT_POS_SEEKBAR = "trackCurrentPosSeekbar";

    /**
     * Required empty public constructor
     */
    public FragmentNowPlaying() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FragmentNowPlaying newInstance(int sectionNumber) {
        FragmentNowPlaying fragment = new FragmentNowPlaying();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public static void addNewBookmark(String note) {
        // Values reference
        ContentValues values = new ContentValues();

        // Get song
        AudioFile s = ActivityMain.audioFileList.get(ServiceMusic.songPosition);

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
        formattedPosition = " -  (" + s.getLength() + ")";
        values.put(DatabaseOpenHelper.BOOKMARK_FORMATTED, formattedPosition);

        // Add values to new database row
        ActivityMain.mDB.insert(DatabaseOpenHelper.TABLE_NAME, null, values);

        checkForBookmarks();

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

                if (ActivityMain.musicSrv != null && ActivityMain.musicBound && ! ServiceMusic.isPreparing) {
                    // if already playing, pause
                    if (ActivityMain.musicSrv.isPng()) {
                        ActivityMain.musicSrv.pausePlayer();
                        playPause.setImageDrawable(getResources().getDrawable(R.drawable.play));
                        swipeableNotification();
                    } else {

                        ActivityMain.musicSrv.resume();
                        if (!ActivityMain.firstSongPlayed) {
                            startTimer();
                        }

                        ActivityMain.firstSongPlayed = true;

                        playPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));

                        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(FragmentNowPlaying.mNotificationId);
                    }

                } else {
                    Log.e(TAG, "SERVICE NULL / PLAYER NOT BOUND " + ActivityMain.musicSrv + ActivityMain.musicBound);
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
        if (ActivityMain.musicBound && ActivityMain.musicSrv != null) { // or still loading

            textSongTitle.setText(ServiceMusic.songTitle);
            textSongArtist.setText(ServiceMusic.songArtist);
            textSongCurrent.setText(ServiceMusic.songCurrentPosition);
            textSongLength.setText(ServiceMusic.songDuration);

            // set button to play
            if (ActivityMain.musicSrv.isPng()) {
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
                    ActivityMain.musicSrv.playPrev();
                } else {
                    ActivityMain.musicSrv.playCurrent();
                }

                if (!ActivityMain.firstSongPlayed) {
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
                    ActivityMain.musicSrv.playRandom();
                } else {
                    ActivityMain.musicSrv.playNext();
                }

                if (!ActivityMain.firstSongPlayed) {
                    startTimer();
                }
            }
        });

        // Bookmark listener
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
        });

        // Seek bar listener
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setMax(ActivityMain.SEEKBAR_RATIO);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                if (fromUser && ActivityMain.musicSrv != null) {

                    ActivityMain.musicSrv.seek(i);

                    String formatted = AudioFile.convertTime(String.valueOf(ServiceMusic.mPlayer.getCurrentPosition()));
                    FragmentNowPlaying.textSongCurrent.setText(formatted);
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
        if (ActivityMain.firstSongPlayed) {
            startTimer();
        }

        progressBarLoading = (ProgressBar) view.findViewById(R.id.progressBar);


        // If playing, show pause button.
        if (ActivityMain.musicBound && ServiceMusic.mPlayer != null && ServiceMusic.mPlayer.isPlaying()) {
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
                while (ServiceMusic.mPlayer != null && FragmentNowPlaying.seekBar != null && !ServiceMusic.exiting) {

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

                                if (FragmentNowPlaying.seekBar != null && !ServiceMusic.isPreparing && !ServiceMusic.exiting) {
                                    FragmentNowPlaying.seekBar.setProgress(ServiceMusic.getCurrentProgress());
                                    String formatted = AudioFile.convertTime(String.valueOf(ServiceMusic.mPlayer.getCurrentPosition()));
                                    FragmentNowPlaying.textSongCurrent.setText(formatted);
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

        ActivityMain.mTitle = "Now Playing";

        checkForBookmarks();

        if (ServiceMusic.isPreparing) {
            progressBarLoading.setVisibility(View.VISIBLE);
        }

        if (ServiceMusic.mPlayer != null && ActivityMain.musicBound) {

            // Load position and id of last played
            SharedPreferences sp = getActivity().getSharedPreferences(ActivityMain.SPREF_KEY, Activity.MODE_PRIVATE);
            textSongCurrent.setText(sp.getString(SPREF_STRING_POS_FORMATTED, "0:00"));
            seekBar.setProgress(sp.getInt(SPREF_INT_POS_SEEKBAR, 0));

        }
    }

    /**
     * Return an object array which holds all bookmarks for the current song. Set Alpha for skip buttons
     */
    public static void checkForBookmarks() {
        if (ActivityMain.mDbHelper.bookmarkAlreadyExists(ActivityMain.audioFileList.get(ServiceMusic.songPosition).getID())) {

            bookmarks = ActivityMain.mDbHelper.getBookmarksForCurrentTrack(ActivityMain.audioFileList.get(ServiceMusic.songPosition).getID());

            Log.i(TAG, "Bookmarks length: "+ bookmarks.size());
            // todo - add helper which checks for bookmarks, and returns Bookmarks class

            // todo - bookmarksForCurrentSong = DatabaseOpenHelper.getBookmarkArrayIfAvaliable();

            // todo use Service.bookmarkArray to populate skippable PlayerFragment array

            bookmarkSkipPrev.setAlpha(1f);
            bookmarkSkipNext.setAlpha(1f);
            bookmarkSkipPrev.setClickable(true);
            bookmarkSkipNext.setClickable(true);

        } else {
            bookmarkSkipPrev.setAlpha(0.5f);
            bookmarkSkipNext.setAlpha(0.5f);
            bookmarkSkipPrev.setClickable(false);
            bookmarkSkipNext.setClickable(false); // todo - check these
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!ServiceMusic.exiting) {
            SharedPreferences sp = getActivity().getSharedPreferences(ActivityMain.SPREF_KEY, Activity.MODE_PRIVATE);
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
        ((ActivityMain) activity).onSectionAttached(
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

        Intent resultIntent = new Intent(getActivity(), ActivityMain.class);
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

                if (! ActivityMain.mDbHelper.bookmarkAlreadyExists(ActivityMain.audioFileList.get(ServiceMusic.songPosition).getID())) {
                    Toast.makeText(getActivity(), "No bookmarks saved for 'TRACK_NAME'", Toast.LENGTH_SHORT).show();

                    break;

                } else {
                    DialogFragment dialogView = DialogViewBookmarks.newInstance(123, getActivity());
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
        }

        return super.onOptionsItemSelected(item);


    }

}