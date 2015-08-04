package website.julianrosser.podcastplayer.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

import website.julianrosser.podcastplayer.activities.ActivityMain;
import website.julianrosser.podcastplayer.services.ServiceMusic;
import website.julianrosser.podcastplayer.R;
import website.julianrosser.podcastplayer.adapters.AdapterLibrarySongList;


/**
 * A fragment representing a list of Songs
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class FragmentLibrary extends android.support.v4.app.Fragment implements AbsListView.OnItemClickListener {

    private static final String ARG_SECTION_NUMBER = "library";
    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    public AdapterLibrarySongList adapterLibrarySongList;
    private OnFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FragmentLibrary() {
    }

    public static FragmentLibrary newInstance(int sectionNumber) {
        FragmentLibrary fragment = new FragmentLibrary();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create custom adapter
        adapterLibrarySongList = new AdapterLibrarySongList(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audiofile, container, false);

        AbsListView mListView = (AbsListView) view.findViewById(android.R.id.list);

        // Set the custom adapter
        mListView.setAdapter(adapterLibrarySongList);

        // This is to ensure 5.0+ works
        mListView.setBackgroundColor(getResources().getColor(R.color.mat_grey_mid));

        TextView emptyText = (TextView) view.findViewById(android.R.id.empty);
        mListView.setEmptyView(emptyText);

        // Show scrollbar
        mListView.setScrollbarFadingEnabled(false);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((ActivityMain) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));

        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        ActivityMain.firstSongPlayed = true;
        ServiceMusic.loadFromBookmark = false;

        ActivityMain.musicSrv.setSong(position);

        Log.i(getClass().getSimpleName(), "onItemClick: " + position);
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(String.valueOf(ActivityMain.audioFileList.get(position).getTitle()));
        }

        FragmentNavigationDrawer.mDrawerListView.setItemChecked(0, true);

        // Launch player fragment
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, FragmentNowPlaying.newInstance(position + 1))
                .commit();


        // Update ActionBar title
        getActionBar().setTitle(getString(R.string.title_section1));

        // update textviews
        ServiceMusic.updateTextViews();

    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String id);
    }

}
