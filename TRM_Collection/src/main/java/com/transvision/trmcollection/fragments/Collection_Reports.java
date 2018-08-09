package com.transvision.trmcollection.fragments;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.analogics.thermalAPI.Bluetooth_Printer_3inch_prof_ThermalAPI;
import com.analogics.thermalprinter.AnalogicsThermalPrinter;
import com.lvrenyang.io.Canvas;
import com.ngx.BluetoothPrinter;
import com.ngx.PrinterWidth;
import com.transvision.trmcollection.MainActivity;
import com.transvision.trmcollection.R;
import com.transvision.trmcollection.adapters.Payment_Report_Adapter;
import com.transvision.trmcollection.bluetooth.BluetoothService;
import com.transvision.trmcollection.database.TRMCollection_Database;
import com.transvision.trmcollection.posting.SendingData;
import com.transvision.trmcollection.posting.SendingData.Collection_Report_Details;
import com.transvision.trmcollection.values.FunctionCalls;
import com.transvision.trmcollection.values.GetSetValues;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import static android.text.Layout.Alignment.ALIGN_CENTER;
import static android.text.Layout.Alignment.ALIGN_NORMAL;
import static com.transvision.trmcollection.bluetooth.BluetoothService.printerconnected;
import static com.transvision.trmcollection.values.Constants.COLLECTION_REPORTS_FAILURE;
import static com.transvision.trmcollection.values.Constants.COLLECTION_REPORTS_SUCCESS;
import static com.transvision.trmcollection.values.Constants.sPref_MRCODE;
import static com.transvision.trmcollection.values.Constants.sPref_ROLE;
import static com.transvision.trmcollection.values.Constants.sPref_SUBDIVNAME;

public class Collection_Reports extends Fragment {
    View view;

    private static final int PRINT_DLG = 1;
    private static final int EXIT_DLG = 2;
    private static final int DLG_PRINT_CONNECTION = 3;
    private static final int DLG_INTERNET_CONNECTION = 4;
    private static final int DLG_NO_COLLECTION_DATA = 5;
    private static final int DLG_NO_COLLECTION_REPORTS = 6;

    TRMCollection_Database collectionDatabase;
    BottomNavigationView print_navigation_view;
    RecyclerView coll_views;
    ArrayList<GetSetValues> coll_list, collection_reports_list;
    GetSetValues getSetValues;
    Payment_Report_Adapter reportAdapter;
    TextView tv_total, tv_recpt_series;
    String coll_printdate="", coll_subdiv_code="", coll_start_recpt="", coll_deviceID="", coll_stop_recpt="", printdt1="", printdt2="",
            receipt_series="", coll_printer="";
    Double total_amount=0.0;
    FunctionCalls functionCalls;
    SendingData sendingData;
    SharedPreferences sPref;

    ProgressDialog printing;
    BluetoothPrinter mBtp;
    AnalogicsThermalPrinter conn = BluetoothService.conn;
    Canvas mCanvas = BluetoothService.mCanvas;
    ExecutorService es = BluetoothService.es;
    Bluetooth_Printer_3inch_prof_ThermalAPI api;
    float yaxis = 0;
    int phiPrinter_height = 0;
    DecimalFormat num;

    private Handler handler = null;
    {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case COLLECTION_REPORTS_SUCCESS:
                        printing.dismiss();
                        coll_start_recpt = collection_reports_list.get(0).getColl_reports_recpt_no();
                        coll_stop_recpt = collection_reports_list.get(collection_reports_list.size()-1).getColl_reports_recpt_no();

                        coll_list.clear();
                        for (int i = 0; i < collection_reports_list.size(); i++) {
                            GetSetValues getSet = collection_reports_list.get(i);
                            getSetValues = new GetSetValues();
                            getSetValues.setColl_re_slno(getSet.getColl_reports_slno());
                            getSetValues.setColl_re_custid(getSet.getColl_reports_acc_id());
                            if (TextUtils.isEmpty(receipt_series))
                                receipt_series = getSet.getColl_reports_recpt_no();
                            String recpt = getSet.getColl_reports_recpt_no();
                            recpt = recpt.substring(recpt.length() - 5, recpt.length());
                            getSetValues.setColl_re_recpt(".."+recpt);
                            Double amount = Double.parseDouble(getSet.getColl_reports_amount());
                            getSetValues.setColl_re_amount(num.format(amount));
                            total_amount = total_amount + amount;
                            coll_list.add(getSetValues);
                            reportAdapter.notifyDataSetChanged();
                        }

                        tv_total.setText(getResources().getString(R.string.rupee)+" "+num.format(total_amount)+" /-");
                        tv_recpt_series.setText(receipt_series);
                        break;

                    case COLLECTION_REPORTS_FAILURE:
                        printing.dismiss();
                        showdialog(DLG_NO_COLLECTION_DATA);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    public Collection_Reports() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_collection_reports, container, false);

        collectionDatabase = new TRMCollection_Database(getActivity());
        collectionDatabase.open();

        functionCalls = new FunctionCalls();
        sendingData = new SendingData(getActivity());
        num = new DecimalFormat("##.00");

        coll_deviceID = ((MainActivity) getActivity()).getCollection_device_id();
        sPref = ((MainActivity) getActivity()).getsharedPref();

        print_navigation_view = (BottomNavigationView) view.findViewById(R.id.collection_report_navigation);
        print_navigation_view.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        tv_total = (TextView) view.findViewById(R.id.payment_report_total);
        tv_recpt_series = (TextView) view.findViewById(R.id.payment_report_series);

        coll_views = (RecyclerView) view.findViewById(R.id.payment_report_view);
        coll_list = new ArrayList<>();
        collection_reports_list = new ArrayList<>();
        reportAdapter = new Payment_Report_Adapter(coll_list, getActivity());
        coll_views.setHasFixedSize(true);
        coll_views.setLayoutManager(new LinearLayoutManager(getActivity()));
        coll_views.setAdapter(reportAdapter);

        Cursor data = collectionDatabase.getPrinter_details();
        if (data.getCount() > 0) {
            data.moveToNext();
            coll_printer = data.getString(data.getColumnIndex("PRINTER"));
            if (coll_printer.equals("NGX")) {
                mBtp = BluetoothService.getngxprinter();
            } else if (coll_printer.equals("ALG")) {
                api = new Bluetooth_Printer_3inch_prof_ThermalAPI();
            }
        }

        switch (sPref.getString(sPref_ROLE, "")) {
            case "AAO":
                coll_subdiv_code = sPref.getString(sPref_MRCODE, "").substring(2);
                break;

            case "AEE":
                coll_subdiv_code = sPref.getString(sPref_MRCODE, "").substring(2);
                break;

            case "MR":
                coll_subdiv_code = sPref.getString(sPref_MRCODE, "").substring(0, 6);
                break;
        }

        if (((MainActivity) getActivity()).getInternetConnection().isConnectingToInternet()) {
            printing = ProgressDialog.show(getActivity(), getActivity().getResources().getString(R.string.collection_reports_title),
                    getActivity().getResources().getString(R.string.collection_reports_message));
            Collection_Report_Details collectionReportDetails = sendingData.new Collection_Report_Details(handler, getSetValues,
                    collection_reports_list);
            collectionReportDetails.execute(sPref.getString(sPref_MRCODE, ""));
        } else showdialog(DLG_INTERNET_CONNECTION);

        return view;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_print:
                    if (coll_list.size() > 0) {
                        if (printerconnected) {
                            showdialog(PRINT_DLG);
                        } else showdialog(DLG_PRINT_CONNECTION);
                    } else showdialog(DLG_NO_COLLECTION_REPORTS);
                    break;

                case R.id.navigation_cancel:
                    getActivity().finish();
                    break;
            }
            return false;
        }
    };

    private void printedon() {
        if (coll_printdate.length() > 11) {
            printdt1 = coll_printdate.substring(0, 10);
        }

        if (coll_printdate.length() > 23) {
            printdt2 = coll_printdate.substring(23);
        }
    }

    private void showdialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case PRINT_DLG:
                final AlertDialog printdlg = new AlertDialog.Builder(getActivity()).create();
                printdlg.setTitle(getActivity().getResources().getString(R.string.collection_report_status_title));
                printdlg.setCancelable(false);
                LinearLayout ab1linear = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.checkprinted, null);
                printdlg.setView(ab1linear);
                final TextView print_msg = (TextView) ab1linear.findViewById(R.id.textView1);
                Button yes_btn = (Button) ab1linear.findViewById(R.id.button1);
                Button no_btn = (Button) ab1linear.findViewById(R.id.button2);
                no_btn.setText(R.string.no_label);
                print_msg.setText(getResources().getString(R.string.ask_print));
                Cursor coll_date = collectionDatabase.summart1();
                coll_date.moveToNext();
                coll_printdate = coll_date.getString(coll_date.getColumnIndex("mode1"));
                printedon();
                yes_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (BluetoothService.printerconnected) {
                            printdlg.dismiss();
                            showprinting_progress();
                            switch (coll_printer) {
                                case "NGX":
                                    printngx();
                                    new Handler().postDelayed(new Runnable() {

                                        @Override
                                        public void run() {
                                            printing.dismiss();
                                            showdialog(EXIT_DLG);
                                        }
                                    }, 5000);
                                    break;

                                case "GPT":
                                    phiPrinter_height = 600 + (17 * 36) + (coll_list.size() * 31);
                                    es.submit(new TaskPrint(mCanvas, phiPrinter_height));
                                    break;

                                case "ALG":
                                    printanalogics();
                                    new Handler().postDelayed(new Runnable() {

                                        @Override
                                        public void run() {
                                            printing.dismiss();
                                            showdialog(EXIT_DLG);
                                        }
                                    }, 5000);
                                    break;
                            }
                        } else {
                            printdlg.dismiss();
                            showdialog(DLG_PRINT_CONNECTION);
                        }
                    }
                });
                no_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        printdlg.dismiss();
                    }
                });
                printdlg.show();
                break;

            case EXIT_DLG:
                final AlertDialog exitdlg = new AlertDialog.Builder(getActivity()).create();
                exitdlg.setTitle(getActivity().getResources().getString(R.string.collection_report_status_title));
                exitdlg.setCancelable(false);
                LinearLayout rl = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.checkprinted, null);
                exitdlg.setView(rl);
                final TextView exit_txt = (TextView) rl.findViewById(R.id.textView1);
                exit_txt.setText(getResources().getString(R.string.collection_report_print));
                Button exityes_btn = (Button) rl.findViewById(R.id.button1);
                Button exitreprint_btn = (Button) rl.findViewById(R.id.button2);
                exityes_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        exitdlg.dismiss();
                    }
                });
                exitreprint_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        exitdlg.dismiss();
                        showprinting_progress();
                        switch (coll_printer) {
                            case "NGX":
                                printngx();
                                new Handler().postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        printing.dismiss();
                                        showdialog(EXIT_DLG);
                                    }
                                }, 5000);
                                break;

                            case "ALG":
                                printanalogics();
                                new Handler().postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        printing.dismiss();
                                        showdialog(EXIT_DLG);
                                    }
                                }, 5000);
                                break;

                            case "GPT":
                                es.submit(new TaskPrint(mCanvas, phiPrinter_height));
                                break;
                        }
                    }
                });
                exitdlg.show();
                break;

            case DLG_PRINT_CONNECTION:
                AlertDialog.Builder print_connection = new AlertDialog.Builder(getActivity());
                print_connection.setTitle(getActivity().getResources().getString(R.string.dlg_printer_connection));
                print_connection.setCancelable(false);
                print_connection.setMessage(getActivity().getResources().getString(R.string.printer_connection_error));
                print_connection.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                dialog = print_connection.create();
                dialog.show();
                break;

            case DLG_INTERNET_CONNECTION:
                AlertDialog.Builder internet_connection = new AlertDialog.Builder(getActivity());
                internet_connection.setCancelable(false);
                internet_connection.setMessage(getActivity().getResources().getString(R.string.internet_connection));
                internet_connection.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                dialog = internet_connection.create();
                dialog.show();
                break;

            case DLG_NO_COLLECTION_DATA:
                AlertDialog.Builder no_data = new AlertDialog.Builder(getActivity());
                no_data.setCancelable(false);
                no_data.setMessage(getActivity().getResources().getString(R.string.no_collection_data));
                no_data.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                dialog = no_data.create();
                dialog.show();
                break;

            case DLG_NO_COLLECTION_REPORTS:
                AlertDialog.Builder no_reports = new AlertDialog.Builder(getActivity());
                no_reports.setCancelable(false);
                no_reports.setMessage(getActivity().getResources().getString(R.string.no_collection_records));
                no_reports.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                dialog = no_reports.create();
                dialog.show();
                break;
        }
    }

    private void showprinting_progress() {
        printing = ProgressDialog.show(getActivity(), "Printing", getActivity().getResources().getString(R.string.printing));
    }

    private void printngx() {
        int maxlength = 44;
        mBtp.setPrinterWidth(PrinterWidth.PRINT_WIDTH_72MM);
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/DroidSansMono.ttf");
        TextPaint tp = new TextPaint();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getActivity().getResources().getString(R.string.collection_report_header));
        tp.setTextSize(35);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_CENTER, tp);
        stringBuilder.setLength(0);
        stringBuilder.append(functionCalls.space(getActivity().getResources().getString(R.string.mid), 4) + functionCalls.space(":", 2) + coll_deviceID+"\n");
        stringBuilder.append(sPref.getString(sPref_SUBDIVNAME, ""));
        tp.setTextSize(25);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_CENTER, tp);
        stringBuilder.setLength(0);
        stringBuilder.append(functionCalls.line(maxlength)+"\n");
        stringBuilder.append(printdt1 + " : " + printdt2+"\n");
        stringBuilder.append(functionCalls.line(maxlength)+"\n");
        stringBuilder.append(functionCalls.space(getActivity().getResources().getString(R.string.start_receipt), 14) + " : " + coll_start_recpt+"\n");
        stringBuilder.append(functionCalls.space(getActivity().getResources().getString(R.string.end_receipt), 14) + " : " + coll_stop_recpt+"\n");
        stringBuilder.append(functionCalls.space(getActivity().getResources().getString(R.string.total_receipt), 14) + " : " + coll_list.size()+"\n");
        stringBuilder.append(functionCalls.line(maxlength)+"\n");
        stringBuilder.append(functionCalls.space(getActivity().getResources().getString(R.string.receipt_series), 14) + " : " + receipt_series+"\n");
        stringBuilder.append(functionCalls.line(maxlength)+"\n");
        stringBuilder.append(getActivity().getResources().getString(R.string.total)+" : "+functionCalls.alignright(getResources().getString(R.string.rupee)+" "+
                total_amount+"\n", 34));
        stringBuilder.append(functionCalls.line(maxlength)+"\n");
        stringBuilder.append(functionCalls.alignright(getActivity().getResources().getString(R.string.slno), 5)
                +"  "+functionCalls.space(functionCalls.aligncenter(getActivity().getResources().getString(R.string.acct_id), 10), 10)
                +"  "+functionCalls.aligncenter(getActivity().getResources().getString(R.string.receipt), 7)
                +"  "+functionCalls.aligncenter(getActivity().getResources().getString(R.string.amount), 16)+"\n");
        stringBuilder.append(functionCalls.line(maxlength));
        tp.setTextSize(22);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_NORMAL, tp);
        stringBuilder.setLength(0);
        for (int i = 0; i < coll_list.size(); i++) {
            GetSetValues getSetValues = coll_list.get(i);
            if (i == coll_list.size()-1) {
                stringBuilder.append(functionCalls.alignright(getSetValues.getColl_re_slno(), 5)+"  "+
                        functionCalls.space(functionCalls.space(getSetValues.getColl_re_custid(), 10), 10)+"  "+
                        functionCalls.alignright(getSetValues.getColl_re_recpt(), 7)+"  "+
                        functionCalls.alignright(getSetValues.getColl_re_amount(), 16));
            } else stringBuilder.append(functionCalls.alignright(getSetValues.getColl_re_slno(), 5)+"  "+
                    functionCalls.space(functionCalls.space(getSetValues.getColl_re_custid(), 10), 10)+"  "+
                    functionCalls.alignright(getSetValues.getColl_re_recpt(), 7)+"  "+
                    functionCalls.alignright(getSetValues.getColl_re_amount(), 16)+"\n");
        }
        tp.setTextSize(22);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_NORMAL, tp);
        stringBuilder.setLength(0);
        stringBuilder.append(functionCalls.line(maxlength)+"\n");
        stringBuilder.append(getActivity().getResources().getString(R.string.total)+" : "+functionCalls.alignright(getResources().getString(R.string.rupee)+" "+
                total_amount+"\n", 34));
        stringBuilder.append(functionCalls.line(maxlength));
        tp.setTextSize(22);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_NORMAL, tp);
        mBtp.print();
        mBtp.printUnicodeText("\n\n\n");
    }

    private void printanalogics() {
        int maxlength = 30;
        int feed_line = 6;
        analogics_double_print(functionCalls.aligncenter(getActivity().getResources().getString(R.string.collection_report_header), 30), feed_line);
        analogicsprint(functionCalls.aligncenter(functionCalls.space(getActivity().getResources().getString(R.string.mid), 4) + functionCalls.space(":", 2) + coll_deviceID, 44), feed_line);
        analogicsprint(functionCalls.aligncenter(sPref.getString(sPref_SUBDIVNAME, "")+" ", 44), feed_line);
        analogics_line_print(functionCalls.line(maxlength), feed_line);
        analogicsprint(printdt1 + " : " + printdt2, feed_line);
        analogics_line_print(functionCalls.line(maxlength), feed_line);
        analogicsprint(functionCalls.space(getActivity().getResources().getString(R.string.start_receipt), 14) + " : " + coll_start_recpt, feed_line);
        analogicsprint(functionCalls.space(getActivity().getResources().getString(R.string.end_receipt), 14) + " : " + coll_stop_recpt, feed_line);
        analogicsprint(functionCalls.space(getActivity().getResources().getString(R.string.total_receipt), 14) + " : " + coll_list.size(), feed_line);
        analogics_line_print(functionCalls.line(maxlength), feed_line);
        analogicsprint(functionCalls.space(getActivity().getResources().getString(R.string.receipt_series), 14) + " : " + receipt_series, feed_line);
        analogics_line_print(functionCalls.line(maxlength), feed_line);
        analogicsprint(getActivity().getResources().getString(R.string.total)+" : "+functionCalls.alignright(""+total_amount, 36), feed_line);
        analogics_line_print(functionCalls.line(maxlength), feed_line);
        analogicsprint(functionCalls.alignright(getActivity().getResources().getString(R.string.slno), 5)
                +" "+functionCalls.space(functionCalls.aligncenter(getActivity().getResources().getString(R.string.acct_id), 12), 12)
                +" "+functionCalls.aligncenter(getActivity().getResources().getString(R.string.receipt), 7)
                +" "+functionCalls.aligncenter(getActivity().getResources().getString(R.string.amount), 18), feed_line);
        analogics_line_print(functionCalls.line(maxlength), feed_line);
        for (int i = 0; i < coll_list.size(); i++) {
            GetSetValues getSetValues = coll_list.get(i);
            analogicsprint(functionCalls.alignright(getSetValues.getColl_re_slno(), 5)+"  "+
                    functionCalls.space(functionCalls.aligncenter(getSetValues.getColl_re_custid(), 12), 12)+"  "+
                    functionCalls.alignright(getSetValues.getColl_re_recpt(), 7)+"  "+
                    functionCalls.alignright(getSetValues.getColl_re_amount(), 16), 3);
        }
        analogics_line_print(functionCalls.line(maxlength), feed_line);
        analogicsprint(getActivity().getResources().getString(R.string.total)+" : "+functionCalls.alignright(""+total_amount, 36), feed_line);
        analogics_line_print(functionCalls.line(maxlength), feed_line);
        analogicsprint(" ", 6);
        analogicsprint(" ", 6);
        analogicsprint(" ", 6);
    }

    private void analogicsprint(String Printdata, int feed_line) {
        conn.printData(api.font_Courier_44_VIP(Printdata));
        text_line_spacing(feed_line);
    }

    private void analogics_double_print(String Printdata, int feed_line) {
        conn.printData(api.font_Double_Height_On_VIP());
        conn.printData(api.font_Courier_30_VIP(Printdata));
        text_line_spacing(feed_line);
        conn.printData(api.font_Double_Height_Off_VIP());
    }

    private void analogics_line_print(String Printdata, int feed_line) {
        conn.printData(api.font_Courier_30_VIP(Printdata));
        text_line_spacing(feed_line);
    }

    private void text_line_spacing(int space) {
        conn.printData(api.variable_Size_Line_Feed_VIP(space));
    }

    private class TaskPrint implements Runnable {
        Canvas canvas = null;
        int print_height = 0;

        private TaskPrint(Canvas pos, int height) {
            this.canvas = pos;
            this.print_height = height;
        }

        @Override
        public void run() {
            final boolean bPrintResult = PrintTicket(canvas, 576, print_height);
            final boolean bIsOpened = canvas.GetIO().IsOpened();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity().getApplicationContext(), bPrintResult ? getResources().getString(R.string.print_success) : getResources().getString(R.string.print_failed), Toast.LENGTH_SHORT).show();
                    if (bIsOpened) {
                        yaxis = 0;
                        printing.dismiss();
                        showdialog(EXIT_DLG);
                    }
                }
            });
        }

        private boolean PrintTicket(Canvas canvas, int nPrintWidth, int nPrintHeight) {
            boolean bPrintResult = false;
            Typeface tfNumber = Typeface.createFromAsset(getActivity().getAssets(), "fonts/DroidSansMono.ttf");
            canvas.CanvasBegin(nPrintWidth, nPrintHeight);
            canvas.SetPrintDirection(0);

            int maxlength = 40;
            int small_font_height = 20;
            int normal_font_height = 24;
            int double_font_height = 32;

            printboldtext(canvas, functionCalls.aligncenter(getActivity().getResources().getString(R.string.collection_report_header), 30), tfNumber, double_font_height, 6);
            printtext(canvas, functionCalls.aligncenter(functionCalls.space(getActivity().getResources().getString(R.string.mid), 4) + functionCalls.space(":", 2) + coll_deviceID, 40), tfNumber, normal_font_height, 6);
            printtext(canvas, functionCalls.aligncenter(sPref.getString(sPref_SUBDIVNAME, "")+" ", 40), tfNumber, normal_font_height, 6);
            printtext(canvas, functionCalls.line(maxlength), tfNumber, normal_font_height, 6);
            printtext(canvas, printdt1 + " : " + printdt2, tfNumber, normal_font_height, 6);
            printtext(canvas, functionCalls.line(maxlength), tfNumber, normal_font_height, 6);
            printtext(canvas, functionCalls.space(getActivity().getResources().getString(R.string.start_receipt), 14) + " : " + coll_start_recpt, tfNumber, normal_font_height, 6);
            printtext(canvas, functionCalls.space(getActivity().getResources().getString(R.string.end_receipt), 14) + " : " + coll_stop_recpt, tfNumber, normal_font_height, 6);
            printtext(canvas, functionCalls.space(getActivity().getResources().getString(R.string.total_receipt), 14) + " : " + coll_list.size(), tfNumber, normal_font_height, 6);
            printtext(canvas, functionCalls.line(maxlength), tfNumber, normal_font_height, 6);
            printtext(canvas, functionCalls.space(getActivity().getResources().getString(R.string.receipt_series), 14) + " : " + receipt_series, tfNumber, normal_font_height, 6);
            printtext(canvas, functionCalls.line(maxlength), tfNumber, normal_font_height, 6);
            printboldtext(canvas, getActivity().getResources().getString(R.string.total) + " : " + functionCalls.alignright(getResources().getString(R.string.rupee)+" "+num.format(total_amount)+" /-", 32), tfNumber, normal_font_height, 6);
            printtext(canvas, functionCalls.line(maxlength), tfNumber, normal_font_height, 6);
            printtext(canvas, functionCalls.alignright(getActivity().getResources().getString(R.string.slno), 5)+"  "+functionCalls.space(functionCalls.aligncenter(getActivity().getResources().getString(R.string.acct_id), 12), 12)+"  "+
                    functionCalls.aligncenter(getActivity().getResources().getString(R.string.receipt), 7) +"  "+functionCalls.aligncenter(getActivity().getResources().getString(R.string.amount), 16), tfNumber, small_font_height, 6);
            printtext(canvas, functionCalls.line(maxlength), tfNumber, normal_font_height, 6);
            for (int i = 0; i < coll_list.size(); i++) {
                GetSetValues getSetValues = coll_list.get(i);
                printtext(canvas, functionCalls.alignright(getSetValues.getColl_re_slno(), 5)+"  "+
                        functionCalls.space(functionCalls.aligncenter(getSetValues.getColl_re_custid(), 12), 12)+"  "+
                        functionCalls.alignright(getSetValues.getColl_re_recpt(), 7)+"  "+
                        functionCalls.alignright(getSetValues.getColl_re_amount(), 16), tfNumber, small_font_height, 3);
            }
            printtext(canvas, functionCalls.line(maxlength), tfNumber, normal_font_height, 6);
            printboldtext(canvas, getActivity().getResources().getString(R.string.total) + " : " + functionCalls.alignright(getResources().getString(R.string.rupee)+" "+num.format(total_amount)+" /-", 32), tfNumber, normal_font_height, 6);
            printtext(canvas, functionCalls.line(maxlength), tfNumber, normal_font_height, 6);
            printtext(canvas, "", tfNumber, normal_font_height, 6);
            printtext(canvas, "", tfNumber, normal_font_height, 6);

            canvas.CanvasPrint(1, 0);

            bPrintResult = canvas.GetIO().IsOpened();
            return bPrintResult;
        }

        private void printtext(Canvas canvas, String text, Typeface tfNumber, float textsize, float axis) {
            yaxis++;
            canvas.DrawText(text + "\r\n", 0, yaxis, 0, tfNumber, textsize, Canvas.DIRECTION_LEFT_TO_RIGHT);
            if (textsize == 20) {
                yaxis = yaxis + textsize + 8;
            } else yaxis = yaxis + textsize + 6;
            yaxis = yaxis + axis;
        }

        private void printboldtext(Canvas canvas, String text, Typeface tfNumber, float textsize, float axis) {
            yaxis++;
            canvas.DrawText(text + "\r\n", 0, yaxis, 0, tfNumber, textsize, Canvas.FONTSTYLE_BOLD);
            if (textsize == 20) {
                yaxis = yaxis + textsize + 8;
            } else yaxis = yaxis + textsize + 6;
            yaxis = yaxis + axis;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}
