package com.transvision.trmcollection.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.transvision.trmcollection.R;
import com.transvision.trmcollection.values.GetSetValues;

import java.util.ArrayList;

public class Payment_Report_Adapter extends RecyclerView.Adapter<Payment_Report_Adapter.Payments_ViewHolder> {
    private ArrayList<GetSetValues> arrayList = new ArrayList<>();
    Context context;

    public Payment_Report_Adapter(ArrayList<GetSetValues> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @Override
    public Payments_ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_reports_card_view, parent, false);
        Payments_ViewHolder viewHolder = new Payments_ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(Payments_ViewHolder holder, int position) {
        GetSetValues details = arrayList.get(position);
        holder.tv_slno.setText(details.getColl_re_slno());
        holder.tv_cust_id.setText(details.getColl_re_custid());
        holder.tv_receipts.setText(details.getColl_re_recpt());
        holder.tv_amount.setText(context.getResources().getString(R.string.rupee)+" "+details.getColl_re_amount());
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class Payments_ViewHolder extends ViewHolder {
        TextView tv_slno, tv_cust_id, tv_receipts, tv_amount;

        private Payments_ViewHolder(View itemView) {
            super(itemView);
            tv_slno = (TextView) itemView.findViewById(R.id.pay_re_slno);
            tv_cust_id = (TextView) itemView.findViewById(R.id.pay_re_cust_id);
            tv_receipts = (TextView) itemView.findViewById(R.id.pay_re_recpts);
            tv_amount = (TextView) itemView.findViewById(R.id.pay_re_amount);
        }
    }
}
