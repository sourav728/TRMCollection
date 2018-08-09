package com.transvision.trmcollection.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.transvision.trmcollection.R;
import com.transvision.trmcollection.values.FunctionCalls;

public class Upload_Collection extends Fragment {
    View view;
    Button bt_collection_upload;
    FunctionCalls functionCalls;
    ProgressDialog progressDialog;

    public Upload_Collection() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_upload_collection, container, false);

        bt_collection_upload = (Button) view.findViewById(R.id.btn_collection_upload);

        functionCalls = new FunctionCalls();
        progressDialog = new ProgressDialog(getActivity());

        bt_collection_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }

}
