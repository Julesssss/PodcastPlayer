package website.julianrosser.podcastplayer;


import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import website.julianrosser.podcastplayer.classes.Song;
import website.julianrosser.podcastplayer.library.DatabaseOpenHelper;


/**
 * A fragment containing a the player / controlls
 */
public class PlayerFragment extends android.support.v4.app.Fragment {


    // The fragment argument representing the section number for this fragment.
    private static final String ARG_SECTION_NUMBER = "section_number";
    // For logging purposes
    private static final String TAG = "PlayerFragment";

    // View references
    static SeekBar seekBar;
    static ImageButton playPause;
    static TextView textSongTitle;
    static TextView textSongArtist;
    static TextView textSongCurrent;
    static TextView textSongLength;

    /**
     * Required empty public constructor
     */
    public PlayerFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlayerFragment newInstance(int sectionNumber) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate view
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // Set up play / pause button
        playPause = (ImageButton) view.findViewById(R.id.buttonPlay);
        playPause.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View view) {

                if (MainActivity.musicSrv != null && MainActivity.musicBound) {

                    Log.i(TAG, "playPausebuttonClickeed");

                    // if already playing, pause
                    if (MainActivity.musicSrv.isPng()) {
                        Log.i(TAG, "Already Playing");
                        MainActivity.musicSrv.pausePlayer();
                        playPause.setImageDrawable(getResources().getDrawable(R.drawable.play));

                    } else {
                        Log.i(TAG, "Not currently playing");

                        // if first song then start, else resume
                        if (MainActivity.firstPreparedSong) {
                            MainActivity.musicSrv.playCurrent();
                        } else {
                            MainActivity.musicSrv.resume();
                        }
                        playPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));

                    }

                } else {
                    Log.e(TAG, "SERVICE NULL / PLAYER NOT BOUND");
                }
            }

        });

        // Initialize TextViews
        textSongCurrent = (TextView) view.findViewById(R.id.textSongTimeCurrent);
        textSongLength = (TextView) view.findViewById(R.id.textSongTimeLength);
        textSongTitle = (TextView) view.findViewById(R.id.songTitle);
        textSongArtist = (TextView) view.findViewById(R.id.songArtist);

        // Set track information if service is initialised
        if (MainActivity.musicSrv != null) {

            textSongTitle.setText(MusicService.songTitle);
            textSongArtist.setText(MusicService.songArtist);
            textSongCurrent.setText("0:00"); // TODO - use actual, not string
            textSongLength.setText(MusicService.songDuration);

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
                // TODO - Logic for playing previous songs
                if (MusicService.mPlayer.getCurrentPosition() < 3000) {
                    MainActivity.musicSrv.playPrev();
                } else {
                    MainActivity.musicSrv.playCurrent();
                }
            }
        });

        // Forward button listener
        ImageButton forward = (ImageButton) view.findViewById(R.id.buttonForward);
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.shuffleMode) {
                    MainActivity.musicSrv.playRandom();
                } else {
                    MainActivity.musicSrv.playNext();
                }
            }
        });

        // Shuffle Button
        ImageButton buttonShuffle = (ImageButton) view.findViewById(R.id.buttonShuffle);
        buttonShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (MainActivity.shuffleMode) {
                    Toast.makeText(getActivity(), "Shuffle Mode OFF", Toast.LENGTH_SHORT).show();
                    MainActivity.shuffleMode = false;

                } else {
                    Toast.makeText(getActivity(), "Shuffle Mode ON", Toast.LENGTH_SHORT).show();
                    MainActivity.shuffleMode = true;
                }

            }
        });

        // Bookmark listener
        ImageButton buttonBookmark = (ImageButton) view.findViewById(R.id.buttonBookmark);
        buttonBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // SQL DB
                ContentValues values = new ContentValues();

                // todo - Should there just be the DB and no array list? Yes, probably

                // Get song
                Song s = MainActivity.songList.get(MusicService.songPosition); // todo - might crash if song list changes or song changes, test

                // Get String values of names, other info
                values.put(DatabaseOpenHelper.ARTIST_NAME, s.getArtist());
                values.put(DatabaseOpenHelper.TRACK_NAME, s.getTitle());
                values.put(DatabaseOpenHelper.UNIQUE_ID, s.getIDString());


                double songCurrentPos = Double.valueOf(String.valueOf(MusicService.mPlayer.getCurrentPosition()));
                values.put(DatabaseOpenHelper.BOOKMARK_MILLIS, ((int) songCurrentPos));
                Log.i("SQL", "songCurrentPos: " + (int) songCurrentPos);

                songCurrentPos = songCurrentPos / 1000;
                String formattedPosition = (int) songCurrentPos / 60 + ":" + String.format("%02d", (int) songCurrentPos % 60);
                values.put(DatabaseOpenHelper.BOOKMARK_FORMATTED, formattedPosition);
                Log.i("SQL", "formattedPosition: " + formattedPosition);

                // Add values to new database row
                MainActivity.mDB.insert(DatabaseOpenHelper.TABLE_NAME, null, values);

                // Notify user that the bookmark was saved
                Toast.makeText(getActivity(), "Bookmark saved at " + formattedPosition, Toast.LENGTH_LONG).show();

            }
        });

        // Seek bar listener
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setMax(1000); // todo - change to reference in MainActivity
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                if (fromUser) {
                    MainActivity.musicSrv.seek(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // if song loaded, update progress. todo needed? does this do anything?
        if (MainActivity.musicSrv != null && MainActivity.musicSrv.isPng()) {
            seekBar.setProgress(MusicService.getCurrentProgress());
            playPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
        }

        startTimer();

        return view;

    }

    /**
     * TODO URGENT!!!! - WHY SEPERATE TRACKERS????
     */
    public void startTimer() {
        new Thread(new Runnable() {
            @Override

            public void run() {


                // Ensure Servce is initialized
                for (int i = 0; i < 30; i++) {
                    if (MusicService.mPlayer == null) {
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
                while (MusicService.mPlayer != null && PlayerFragment.seekBar != null) {


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
                                if (PlayerFragment.seekBar != null && MusicService.mPlayer.isPlaying()) { // todo if in view, not playing
                                    long millis = 0;
                                    if (MainActivity.firstPreparedSong) {
                                        ///Log.i(TAG, "First Load");
                                        millis = 0;//MusicService.mPlayer.getCurrentPosition();
                                    } else {
                                        /// Log.i(TAG, "Not first load");
                                        millis = MusicService.mPlayer.getCurrentPosition();
                                    }


                                    // Format time to mins, secs
                                    long second = (millis / 1000) % 60;
                                    int minutes = (int) (millis / 1000) / 60;

                                    // Set TextView with built string
                                    PlayerFragment.textSongCurrent.setText(String.valueOf(minutes) + ":" + String.format("%02d", second));


                                    PlayerFragment.seekBar.setProgress(MusicService.getCurrentProgress());

                                    Log.i(TAG, "Thread running");
                                }
                            }
                        });
                    }


                }
                Log.i(TAG, "Thread finished");
            }
        }).start();
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
}