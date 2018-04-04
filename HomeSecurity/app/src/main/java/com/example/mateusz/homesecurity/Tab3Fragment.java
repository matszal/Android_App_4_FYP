package com.example.mateusz.homesecurity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by Mateusz on 25/03/2018.
 */

public class Tab3Fragment extends Fragment {
    private static final String TAG = "Tab2Fragment";

    private Button btnTest3;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.tab3_fragment, container, false);

        btnTest3 = (Button)view.findViewById(R.id.btnTEST3);


        btnTest3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "TEST 3", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

}