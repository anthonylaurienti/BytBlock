package com.example.a8bittetris;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class GameFragment extends Fragment {

    private static final String TAG = "GAME_FRAG";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // This is called after the onAttach(), doing the initial creation of the fragment.
        super.onCreate(savedInstanceState);
        Log.i(TAG, " onCreate()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //This is called after the onCreate(), this is where you will initialize all the views in this fragment.
        Log.i(TAG, " onCreateView()");
        View view = inflater.inflate(R.layout.game_fragment, container, false);
        return view;
    }
}
