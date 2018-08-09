package com.transvision.trmcollection.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.transvision.trmcollection.MainActivity;
import com.transvision.trmcollection.R;
import com.transvision.trmcollection.values.FunctionCalls;

import org.apache.commons.lang3.StringUtils;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

import static com.transvision.trmcollection.values.Constants.COLLECTION_SCAN;
import static com.transvision.trmcollection.values.Constants.sPref_ROLE;

public class ScanAccountID extends Fragment implements ZBarScannerView.ResultHandler {
    private ZBarScannerView mScannerView;
//    private FunctionCalls functionCalls;
    SharedPreferences sPref;
    SharedPreferences.Editor editor;

    public ScanAccountID() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mScannerView = new ZBarScannerView(getActivity());

//        functionCalls = new FunctionCalls();
        sPref = ((MainActivity) getActivity()).getsharedPref();
        editor = sPref.edit();
        editor.apply();

        return mScannerView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result result) {
        String value = result.getContents().substring(0, 10);
        if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_ROLE, ""), "AEE")
                || StringUtils.startsWithIgnoreCase(sPref.getString(sPref_ROLE, ""), "AAO")) {
            Collection_AAO_AEE collectionAaoAee = new Collection_AAO_AEE();
            Bundle bundle = new Bundle();
            bundle.putString(COLLECTION_SCAN, value);
            collectionAaoAee.setArguments(bundle);
            ((MainActivity) getActivity()).switchFragment(collectionAaoAee, getActivity().getResources().getString(R.string.collection));
        } else {
            Collection collection = new Collection();
            Bundle bundle = new Bundle();
            bundle.putString(COLLECTION_SCAN, value);
            collection.setArguments(bundle);
            ((MainActivity) getActivity()).switchFragment(collection, getActivity().getResources().getString(R.string.collection));
        }
    }
}
