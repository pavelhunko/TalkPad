package com.idragonit.talkpad.fragment;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.idragonit.talkpad.R;

public class AboutFragment extends Fragment implements View.OnClickListener{

    Button okButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_about, container, false);
        okButton = (Button) view.findViewById(R.id.about_ok_button);


        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        okButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        getActivity().getSupportFragmentManager().popBackStack();

    }
}
