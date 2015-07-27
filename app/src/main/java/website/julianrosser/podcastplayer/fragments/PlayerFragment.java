package website.julianrosser.podcastplayer.fragments;


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

import website.julianrosser.podcastplayer.MainActivity;
import website.julianrosser.podcastplayer.MusicService;
import website.julianrosser.podcastplayer.R;
import website.julianrosser.podcastplayer.helpers.DatabaseOpenHelper;
import website.julianrosser.podcastplayer.objects.Song;


/**
 * A fragment containing a the player / controlls
 */
public class PlayerFragment extends android.support.v4.app.Fragment {


    // The fragment argument representing the section number for this fragment.
    private static final String ARG_SECTION_NUMBER = "section_number";
    // For logging purposes
    private static final String TAG = "PlayerFragment";

    // View references
    public static SeekBar seekBar;
    public static ImageButton playPause;
    public static TextView textSongTitle;
    public static TextView textSongArtist;
    public static TextView textSongCurrent;
    public static TextView textSongLength;

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
                    // if already playing, pause
                    if (MainActivity.musicSrv.isPng()) {
                        MainActivity.musicSrv.pausePlayer();
                        playPause.setImageDrawable(getResources().getDrawable(R.drawable.play));
                    } else {

                        MainActivity.musicSrv.resume();
                        startTimer();
                        MainActivity.firstSongPlayed = true;

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
            Log.i("SEEK", "SEEK2 : " + MusicService.songCurrentPosition);
            textSongCurrent.setText(MusicService.songCurrentPosition);
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
                MusicService.loadFromBookmark = false;

                if (MusicService.mPlayer.getCurrentPosition() < 3000) {
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
                MusicService.loadFromBookmark = false;
                if (MainActivity.shuffleMode) {
                    MainActivity.musicSrv.playRandom();
                } else {
                    MainActivity.musicSrv.playNext();
                }

                if (!MainActivity.firstSongPlayed) {
                    startTimer();
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

                addNewBookmark();

            }
        });

        // Seek bar listener
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setMax(MainActivity.SEEKBAR_RATIO);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                if (fromUser) {
                    MainActivity.musicSrv.seek(i);

                    String formatted = Song.convertTime(String.valueOf(MusicService.mPlayer.getCurrentPosition()));
                    PlayerFragment.textSongCurrent.setText(formatted);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        // Start timer if not the first time fragment is opened
        if (MainActivity.firstSongPlayed) {
            startTimer();

            // If playing, show pause button.
            if (MusicService.mPlayer != null && MusicService.mPlayer.isPlaying()) {
                //noinspection deprecation
                playPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
            }
        }

        return view;

    }

    public void addNewBookmark() {
        // Values reference
        ContentValues values = new ContentValues();

        // Get song
        Song s = MainActivity.songList.get(MusicService.songPosition);

        // Get String values of names, other info
        values.put(DatabaseOpenHelper.ARTIST_NAME, s.getArtist());
        values.put(DatabaseOpenHelper.ARTIST_NAME, s.getArtist());
        values.put(DatabaseOpenHelper.TRACK_NAME, s.getTitle());
        values.put(DatabaseOpenHelper.UNIQUE_ID, s.getIDString());

        double songCurrentPos = Double.valueOf(String.valueOf(MusicService.mPlayer.getCurrentPosition()));
        values.put(DatabaseOpenHelper.BOOKMARK_MILLIS, ((int) songCurrentPos));

        // Change from millis to seconds
        songCurrentPos = songCurrentPos / 1000;

        // Format position for DB Fragment display
        String formattedPosition = " -  (" + (int) songCurrentPos / 60 + ":" + String.format("%02d", (int) songCurrentPos % 60)
                + " / " + s.getLength() + ")";
        values.put(DatabaseOpenHelper.BOOKMARK_FORMATTED, formattedPosition);

        // Add values to new database row
        MainActivity.mDB.insert(DatabaseOpenHelper.TABLE_NAME, null, values);

        // Notify user that the bookmark was saved
        Toast.makeText(getActivity(), "Bookmark saved " + formattedPosition, Toast.LENGTH_LONG).show();
    }

    /**
     * Thread which updates seekbar and position textviews. First ensure Sevice has started
     */
    public void startTimer() {
        new Thread(new Runnable() {
            @Override

            public void run() {

                // Ensure ServIce is initialized
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
                                if (PlayerFragment.seekBar != null) {

                                    PlayerFragment.seekBar.setProgress(MusicService.getCurrentProgress());

                                    String formatted = Song.convertTime(String.valueOf(MusicService.mPlayer.getCurrentPosition()));
                                    PlayerFragment.textSongCurrent.setText(formatted);

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
        Log.i("SEEK", "SEEK3BBB : " + MainActivity.seekbarPosition);
        seekBar.setProgress(MainActivity.seekbarPosition);
        Log.i("SEEK", "SEEK3 : " + MusicService.songCurrentPosition);
        textSongCurrent.setText(MainActivity.textCurrentPos);
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity.seekbarPosition = seekBar.getProgress();
        MainActivity.textCurrentPos = textSongCurrent.getText().toString();
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