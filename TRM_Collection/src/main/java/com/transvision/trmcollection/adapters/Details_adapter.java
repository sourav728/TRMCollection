package com.transvision.trmcollection.adapters;

import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.transvision.trmcollection.R;
import com.transvision.trmcollection.database.TRMCollection_Database;
import com.transvision.trmcollection.values.GetSetValues;

import java.util.ArrayList;

import static com.transvision.trmcollection.values.Constants.COLLECTION_TOTAL;
import static com.transvision.trmcollection.values.Constants.COLLECTION_TRANSACTION_LIST_EMPTY;
import static com.transvision.trmcollection.values.Constants.sPref_COLLECTION_COLLECTED;

public class Details_adapter extends RecyclerView.Adapter<Details_adapter.DetailsHolder> {
    private ArrayList<GetSetValues> arrayList;
    private GetSetValues getSetValues;
    private Handler handler;
    private SharedPreferences sPref;
    private SharedPreferences.Editor editor;
    private TRMCollection_Database collectionDatabase;
    private String receipt_date;

    public Details_adapter(ArrayList<GetSetValues> arrayList, GetSetValues getSetValues, Handler handler,
                           SharedPreferences.Editor editor, SharedPreferences sPref, TRMCollection_Database collectionDatabase,
                           String receipt_date) {
        this.arrayList = arrayList;
        this.getSetValues = getSetValues;
        this.handler = handler;
        this.editor = editor;
        this.sPref = sPref;
        this.collectionDatabase = collectionDatabase;
        this.receipt_date = receipt_date;
    }

    @Override
    public DetailsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.collection_details_card_list, parent, false);
        return new DetailsHolder(view);
    }

    @Override
    public void onBindViewHolder(DetailsHolder holder, int position) {
        GetSetValues getSetValues = arrayList.get(position);
        holder.tvrrno.setText(getSetValues.getRrno());
        holder.tvcustid.setText(getSetValues.getCustid());
        holder.tvamount.setText(String.valueOf(getSetValues.getAmount()));
        calculate_total();
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class DetailsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvrrno, tvcustid, tvamount;
        ImageView image;
        DetailsHolder(View itemView) {
            super(itemView);
            tvrrno = itemView.findViewById(R.id.detail_rrno);
            tvcustid = itemView.findViewById(R.id.details_acc_id);
            tvamount = itemView.findViewById(R.id.detail_amount);
            image = itemView.findViewById(R.id.detail_cancel);
            image.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            GetSetValues getSetValues = arrayList.get(position);
            switch (v.getId()) {
                case R.id.detail_cancel:
                    double reduced = Double.parseDouble(sPref.getString(sPref_COLLECTION_COLLECTED, "")) - getSetValues.getAmount();
                    editor.putString(sPref_COLLECTION_COLLECTED, String.valueOf(reduced));
                    editor.commit();
                    collectionDatabase.delete_collection_record(getSetValues.getCustid(), receipt_date);
                    arrayList.remove(position);
                    notifyItemRemoved(position);
                    calculate_total();
                    if (arrayList.isEmpty())
                        handler.sendEmptyMessage(COLLECTION_TRANSACTION_LIST_EMPTY);
                    break;
            }
        }
    }

    private void calculate_total() {
        int total = 0;
        for (int i = 0; i < arrayList.size(); i++) {
            total = (int) (total + Math.round(arrayList.get(i).getAmount()));
        }
        getSetValues.setCollection_total(String.valueOf(total));
        handler.sendEmptyMessage(COLLECTION_TOTAL);
    }
}
