package com.transvision.trmcollection.fragments;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.analogics.thermalAPI.Bluetooth_Printer_3inch_prof_ThermalAPI;
import com.analogics.thermalprinter.AnalogicsThermalPrinter;
import com.lvrenyang.io.Pos;
import com.ngx.BluetoothPrinter;
import com.ngx.PrinterWidth;
import com.transvision.trmcollection.MainActivity;
import com.transvision.trmcollection.R;
import com.transvision.trmcollection.adapters.BtPrinteradapter;
import com.transvision.trmcollection.bluetooth.BluetoothService;
import com.transvision.trmcollection.database.TRMCollection_Database;
import com.transvision.trmcollection.values.FunctionCalls;
import com.transvision.trmcollection.values.GetSetValues;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import static android.text.Layout.Alignment.ALIGN_CENTER;
import static android.text.Layout.Alignment.ALIGN_NORMAL;
import static com.transvision.trmcollection.bluetooth.BluetoothService.printerconnected;
import static com.transvision.trmcollection.values.Constants.PRINTER_HEADER;
import static com.transvision.trmcollection.values.Constants.sPref_MRCODE;
import static com.transvision.trmcollection.values.Constants.sPref_MRNAME;

public class Settings extends Fragment {
    private static final int CHANGE_BT_PRINTER = 1;
    private static final int DLG_PRINT_CONNECTION = 2;

    View view;
    TextView tv_settings_mrname, tv_settings_mrcode, tv_settings_device_id, tv_settings_app_ver, tv_settings_printer;
    Button bt_modify_printer, bt_test_printer;
    String coll_printer="";

    TRMCollection_Database collectionDatabase;
    FunctionCalls functionCalls;
    SharedPreferences sPref;
    SharedPreferences.Editor editor;

    BluetoothPrinter mBtp;
    AnalogicsThermalPrinter conn = MainActivity.conn;
    Bluetooth_Printer_3inch_prof_ThermalAPI api;
    Pos mPos = BluetoothService.mPos;
    ExecutorService es = BluetoothService.es;

    int selected = 0;

    public Settings() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        collectionDatabase = ((MainActivity) getActivity()).getCollectionDatabase();
        functionCalls = new FunctionCalls();
        sPref = ((MainActivity) getActivity()).getsharedPref();
        editor = sPref.edit();
        editor.apply();

        tv_settings_mrname = view.findViewById(R.id.settings_mr_name);
        tv_settings_mrname.setText(sPref.getString(sPref_MRNAME, ""));
        tv_settings_mrcode = view.findViewById(R.id.settings_mr_code);
        tv_settings_mrcode.setText(sPref.getString(sPref_MRCODE, ""));
        tv_settings_device_id = view.findViewById(R.id.settings_device_ID);
        tv_settings_device_id.setText(((MainActivity) getActivity()).getCollection_device_id());
        tv_settings_app_ver = view.findViewById(R.id.settings_app_ver);
        tv_settings_app_ver.setText(((MainActivity) getActivity()).getApplication_version());
        tv_settings_printer = view.findViewById(R.id.settings_bt_printer);

        Cursor data = collectionDatabase.getPrinter_details();
        if (data.getCount() > 0) {
            data.moveToNext();
            coll_printer = data.getString(data.getColumnIndex("PRINTER"));
            if (coll_printer.equals("NGX")) {
                mBtp = BluetoothService.getngxprinter();
            } else if (coll_printer.equals("ALG")) {
                api = new Bluetooth_Printer_3inch_prof_ThermalAPI();
            }
            tv_settings_printer.setText(coll_printer);
        }

        bt_modify_printer = view.findViewById(R.id.btn_modify_printer);
        bt_test_printer = view.findViewById(R.id.btn_test_printer);

        bt_modify_printer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showdialog(CHANGE_BT_PRINTER);
            }
        });

        bt_test_printer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (printerconnected) {
                    switch (coll_printer) {
                        case "NGX":
                            test_print_on_NGX();
                            break;

                        case "ALG":
                            test_print_on_analogics();
                            break;

                        case "GPT":
                            es.submit(new TestPrint(mPos));
                            break;
                    }
                }
            }
        });

        return view;
    }

    private void showdialog(int id) {
        switch (id) {
            case CHANGE_BT_PRINTER:
                AlertDialog.Builder chg_printer = new AlertDialog.Builder(getActivity());
                chg_printer.setTitle(getActivity().getResources().getString(R.string.change_printer));
                final LinearLayout chg_bt = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.bt_printer_layout, null);
                chg_printer.setView(chg_bt);
                chg_printer.setCancelable(false);
                final Spinner bt_spin = chg_bt.findViewById(R.id.printers_spin);
                final ArrayList<GetSetValues> printers_list = new ArrayList<>();
                GetSetValues getSetValues;
                BtPrinteradapter printer_Adapter = new BtPrinteradapter(getActivity(), printers_list);
                for (int i = 0; i < getResources().getStringArray(R.array.bt_print_list).length; i++) {
                    getSetValues = new GetSetValues();
                    getSetValues.setBt_printers(getResources().getStringArray(R.array.bt_print_list)[i]);
                    printers_list.add(getSetValues);
                    printer_Adapter.notifyDataSetChanged();
                }
                bt_spin.setAdapter(printer_Adapter);
                bt_spin.setSelection(selected);
                bt_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selected = position;
                        bt_spin.setSelection(selected);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                chg_printer.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                final AlertDialog printer_dialog = chg_printer.create();
                printer_dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button positive = printer_dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        positive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (selected != 0) {
                                    final GetSetValues getSetValues = printers_list.get(selected);
                                    collectionDatabase.printer_details(getSetValues.getBt_printers());
                                    tv_settings_printer.setText(getSetValues.getBt_printers());
                                    printer_dialog.dismiss();
                                    if (((MainActivity) getActivity()).isMyServiceRunning(BluetoothService.class))
                                        ((MainActivity) getActivity()).stopservice(getActivity());
                                    if (StringUtils.startsWithIgnoreCase(MainActivity.printer, "ALG")) {
                                        getActivity().unregisterReceiver(((MainActivity) getActivity()).Receiver);
                                        if (BluetoothService.printerconnected)
                                            try {
                                                conn.closeBT();
                                            } catch (IOException ex) {
                                                ex.printStackTrace();
                                            }
                                    }
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!StringUtils.startsWithIgnoreCase(getSetValues.getBt_printers(), "ALG"))
                                                ((MainActivity) getActivity()).startservice(getActivity());
                                            else ((MainActivity) getActivity()).startBroadcast();
                                        }
                                    }, 500);
                                } else functionCalls.showToast(getActivity(), getActivity().getResources().getString(R.string.printer_selection_error));
                            }
                        });
                    }
                });
                printer_dialog.show();
                printer_dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
                break;

            case DLG_PRINT_CONNECTION:
                AlertDialog.Builder print_connection = new AlertDialog.Builder(getActivity());
                print_connection.setTitle(getActivity().getResources().getString(R.string.dlg_printer_connection));
                print_connection.setCancelable(false);
                print_connection.setMessage(getActivity().getResources().getString(R.string.printer_connection_error));
                print_connection.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                AlertDialog alertDialog = print_connection.create();
                alertDialog.show();
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
                break;
        }
    }

    private void test_print_on_analogics() {
        int maxline = 38;
        StringBuilder stringBuilder = new StringBuilder();
        analogicsprint(functionCalls.line(maxline), 6);
        analogics_double_print(functionCalls.aligncenter(PRINTER_HEADER, maxline), 6);
        analogicsprint(functionCalls.line(maxline), 6);
        stringBuilder.setLength(0);
        stringBuilder.append("\n");
        stringBuilder.append("\n");
        stringBuilder.append("\n");
        stringBuilder.append("\n");
        analogicsprint(stringBuilder.toString(), 6);
    }

    public void analogicsprint(String Printdata, int feed_line) {
        conn.printData(api.font_Courier_38_VIP(Printdata));
        text_line_spacing(feed_line);
    }

    public void analogics_double_print(String Printdata, int feed_line) {
        conn.printData(api.font_Double_Height_On_VIP());
        analogicsprint(Printdata, feed_line);
        conn.printData(api.font_Double_Height_Off_VIP());
    }

    public void text_line_spacing(int space) {
        conn.printData(api.variable_Size_Line_Feed_VIP(space));
    }

    private void test_print_on_NGX() {
        int maxline = 38;
        mBtp.setPrinterWidth(PrinterWidth.PRINT_WIDTH_72MM);
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/DroidSansMono.ttf");
        TextPaint tp = new TextPaint();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(functionCalls.line(maxline));
        tp.setTextSize(25);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_NORMAL, tp);

        stringBuilder.setLength(0);
        stringBuilder.append(PRINTER_HEADER);
        tp.setTextSize(35);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_CENTER, tp);

        stringBuilder.setLength(0);
        stringBuilder.append(functionCalls.line(maxline));
        tp.setTextSize(25);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_NORMAL, tp);
        mBtp.print();
        mBtp.printUnicodeText("\n\n\n\n");
    }

    private class TestPrint implements Runnable {
        Pos pos;

        private TestPrint(Pos pos) {
            this.pos = pos;
        }

        public void run() {
            final boolean bPrintResult = PrintTicket();
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    functionCalls.showToast(getActivity(), bPrintResult ? getActivity().getResources().getString(R.string.print_success) : getActivity().getResources().getString(R.string.print_failed));
                }
            });
        }

        private boolean PrintTicket() {
            pos.POS_S_Align(1);
            printText(functionCalls.line(47));
            printdoubleText(PRINTER_HEADER);
            printText(functionCalls.line(47));
            pos.POS_FeedLine();
            pos.POS_FeedLine();
            pos.POS_FeedLine();
            return pos.GetIO().IsOpened();
        }

        private void printText(String msg) {
            pos.POS_S_TextOut(msg + "\r\n", 0, 0, 0, 0, 4);
        }

        private void printdoubleText(String msg) {
            pos.POS_S_TextOut(msg + "\r\n", 0, 0, 1, 0, 4);
        }
    }

}
