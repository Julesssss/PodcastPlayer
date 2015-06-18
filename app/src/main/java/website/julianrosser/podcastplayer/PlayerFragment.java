package website.julianrosser.podcastplayer;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;


/**
 * A fragment containing a the playerview
 */
public class PlayerFragment extends android.support.v4.app.Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "PlayerFragment";


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
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // Set up play / pause button
        playPause = (ImageButton) view.findViewById(R.id.buttonPlay);
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if already playing, pause
                if (MainActivity.musicSrv.isPng()) {

                    MainActivity.musicSrv.pausePlayer();
                    playPause.setImageDrawable(getResources().getDrawable(R.drawable.play));

                } else {

                    // if initialized
                    if (MainActivity.musicSrv != null && MainActivity.musicBound) {

                        playPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
                        MainActivity.musicSrv.go();
                    } else {
                        Log.i(TAG, "SERVICE NULL / PLAYER NOT BOUND");
                    }
                }
            }
        });

        // Time TextViews
        textSongCurrent = (TextView) view.findViewById(R.id.textSongTimeCurrent);
        textSongLength = (TextView) view.findViewById(R.id.textSongTimeLength);

        // Set track information if service is initialised
        if (MainActivity.musicSrv != null) {
            // Set track info
            textSongTitle = (TextView) view.findViewById(R.id.songTitle);
            textSongArtist = (TextView) view.findViewById(R.id.songArtist);
            textSongTitle.setText(MusicService.songTitle);
            textSongArtist.setText(MusicService.songArtist);

            textSongCurrent.setText("0:00");
            textSongLength.setText(MusicService.songDuration);

            // set button to pause
            if (MainActivity.musicSrv.isPng()) {
                playPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));
            }
        }

        // Rewind button listener
        ImageButton rewind = (ImageButton) view.findViewById(R.id.buttonRewind);
        rewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.musicSrv.playPrev();
            }
        });

        // Forward button listener
        ImageButton forward = (ImageButton) view.findViewById(R.id.buttonForward);
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.musicSrv.playNext();
            }
        });


        // Seek bar listener
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setMax(1000);
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

        // if song loaded, todo test
        if (MainActivity.musicSrv != null && MainActivity.musicSrv.isPng()) {
            seekBar.setProgress(MusicService.getCurrentProgress());
        }

        startTimer();

        return view;

    }

    public void startTimer() {
        new Thread(new Runnable() {
            @Override

            public void run() {


                // Ensure Servce is initialized
                for (int i = 0; i < 30; i++) {
                    if (MusicService.mPlayer == null) {
                        Log.i(getClass().getSimpleName(), "Progress Tracker - Music Service not initialized: " + i);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // update textview while service is alive
                while (MusicService.mPlayer != null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Get MainActivity for UI Thread
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Local reference for millis
                                long millis = MusicService.mPlayer.getCurrentPosition();

                                // Format time to mins, secs
                                long second = (millis / 1000) % 60;
                                int minutes = (int) (millis / 1000) / 60;

                                // Set TextView with built string
                                PlayerFragment.textSongCurrent.setText(String.valueOf(minutes) + ":" + String.format("%02d", second));
                            }
                        });
                    }


                }
            }
        }).start();
    }


    /**
     * Update textviews with track details
     * <p/>
     * static void updateTrackInfo() {
     * if (MainActivity.musicSrv != null) {
     * textSongTitle = (TextView) view.findViewById(R.id.songTitle);
     * textSongArtist = (TextView) view.findViewById(R.id.songArtist);
     * textSongTitle.setText(MusicService.songTitle);
     * textSongArtist.setText(MusicService.songArtist);
     * } else {
     * Log.i("PlayerFragment", "Can't set text, service is null");
     * }
     * }
     */



        /*  Create listener
        playPause.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View view) {

            }
        });

        //noinspection deprecation
        //playPause.setImageDrawable(getResources().getDrawable(R.drawable.pause));

        ImageButton rewind = (ImageButton) view.findViewById(R.id.buttonRewind);
        rewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            AudioPlayerService.rewind();
            }
        });

        ImageButton forward = (ImageButton) view.findViewById(R.id.buttonForward);
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioPlayerService.forward();
            }
        });

        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setMax(1000);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                if (fromUser) {
                    AudioPlayerService.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        Button buttonTrack = (Button) view.findViewById(R.id.buttonTrack);
        buttonTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Random r = new Random();
                int i = r.nextInt(3);
                Intent mAudioPlayerService = new Intent(getActivity(), AudioPlayerService.class);
                mAudioPlayerService.setAction(AudioPlayerService.ACTION_SET_TRACK);
                getActivity().startService(mAudioPlayerService);
            }
        });

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


