package com.example.ProSudoku;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.ProSudoku.R;

/**
 * Created by Vanya on 26.04.2015
 */
public class AboutInfoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //View rootView = inflater.inflate(R.layout.about_info_fragment, container, false);
        return inflater.inflate(R.layout.about_info_fragment, container, false);
    }
}
