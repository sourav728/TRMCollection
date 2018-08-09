package com.transvision.trmcollection.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.transvision.trmcollection.R;
import com.transvision.trmcollection.values.GetSetValues;

import java.util.ArrayList;

public class BtPrinteradapter extends BaseAdapter {
    private ArrayList<GetSetValues> mylist;
    private LayoutInflater inflater;

    public BtPrinteradapter(Context context, ArrayList<GetSetValues> arraylist) {
        this.mylist = arraylist;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mylist.size();
    }

    @Override
    public Object getItem(int pos) {
        return mylist.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int pos, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.spinner_items, null);
        TextView tvitem = view.findViewById(R.id.spinner_txt);
        tvitem.setText(mylist.get(pos).getBt_printers());
        return view;
    }
}
