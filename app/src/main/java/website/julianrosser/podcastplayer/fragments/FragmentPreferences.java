package website.julianrosser.podcastplayer.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.SwitchPreference;
import android.support.v4.preference.PreferenceFragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import website.julianrosser.podcastplayer.R;
import website.julianrosser.podcastplayer.activities.MainActivity;

public class FragmentPreferences extends PreferenceFragment {


    public ListView lv;
    SharedPreferences sharedPreferences;

    private static final String ARG_SECTION_NUMBER = "section_number";

    public static FragmentPreferences newInstance(int sectionNumber) {
        FragmentPreferences fragment = new FragmentPreferences();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get shared preferences
        sharedPreferences = getActivity().getSharedPreferences("website.julianrosser.podcastplayer"
                + "_preferences", Context.MODE_PRIVATE);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        setPreferenceTextColour();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        if (v != null) {

            lv = (ListView) v.findViewById(android.R.id.list);
            lv.setPadding(0, 0, 0, 0);

            lv.setBackgroundColor(getResources().getColor(R.color.mat_grey_mid));




        }

        return v;
    }


    public void setPreferenceTextColour(){

        // todo - tidy

        // set default subtitles & listeners
        CheckBoxPreference checkboxPrefShuffle = (CheckBoxPreference) getPreferenceManager().findPreference(getString(R.string.pref_key_shuffle));

        Spannable spanSummary = new SpannableString( "Play files in a random order" );
        spanSummary.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.white_light)), 0, spanSummary.length(), 0);
        checkboxPrefShuffle.setSummary(spanSummary);

        Spannable spanTitle = new SpannableString( "Shuffle tracks" );
        spanTitle.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.white_light)), 0, spanTitle.length(), 0);
        checkboxPrefShuffle.setTitle(spanTitle);


        // set default subtitles & listeners
        SwitchPreference switchPreference = (SwitchPreference) getPreferenceManager().findPreference(getString(R.string.pref_key_podcastmode));

        spanSummary = new SpannableString( "Ignore music files when building library" );
        spanSummary.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.white_light)), 0, spanSummary.length(), 0);
        switchPreference.setSummary(spanSummary);

        spanTitle = new SpannableString( "Podcast only mode" );
        spanTitle.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.white_light)), 0, spanTitle.length(), 0);
        switchPreference.setTitle(spanTitle);

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


    /** Callback to activity, helps to set title string.
     *
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String id);
    }

}
