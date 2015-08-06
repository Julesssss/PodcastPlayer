package website.julianrosser.podcastplayer.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import website.julianrosser.podcastplayer.R;
import website.julianrosser.podcastplayer.activities.MainActivity;

public class FragmentHelp extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";


    public FragmentHelp() {
        // Required empty public constructor
    }

    public static FragmentHelp newInstance(int sectionNumber) {
        FragmentHelp fragment = new FragmentHelp();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_help, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    public boolean isFragmentUIActive() {
        return isAdded() && !isDetached() && !isRemoving();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String id);
    }
}
