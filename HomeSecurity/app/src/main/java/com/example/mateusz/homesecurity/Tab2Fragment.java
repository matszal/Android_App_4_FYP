package com.example.mateusz.homesecurity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.zip.Inflater;

/**
 * Created by Mateusz on 25/03/2018.
 */

public class Tab2Fragment extends Fragment {
    private static final String TAG = "Tab1Fragment";

    private Button btnTest2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.tab2_fragment, container, false);

        btnTest2 = (Button)view.findViewById(R.id.btnTEST2);


        btnTest2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "TEST 2", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

}
