package com.transvision.trmcollection.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.analogics.thermalAPI.Bluetooth_Printer_3inch_prof_ThermalAPI;
import com.analogics.thermalprinter.AnalogicsThermalPrinter;
import com.google.gson.Gson;
import com.lvrenyang.io.Pos;
import com.ngx.BluetoothPrinter;
import com.ngx.Enums.NGXBarcodeCommands;
import com.ngx.PrinterWidth;
import com.transvision.trmcollection.MainActivity;
import com.transvision.trmcollection.R;
import com.transvision.trmcollection.adapters.BtPrinteradapter;
import com.transvision.trmcollection.adapters.Details_adapter;
import com.transvision.trmcollection.bluetooth.BluetoothService;
import com.transvision.trmcollection.database.TRMCollection_Database;
import com.transvision.trmcollection.posting.SendingData;
import com.transvision.trmcollection.posting.SendingData.Posting_Collection_data;
import com.transvision.trmcollection.posting.SendingData.Non_Revenue_Collection_data;
import com.transvision.trmcollection.posting.SendingData.Collection_Details;
import com.transvision.trmcollection.posting.SendingData.Non_Revenue_Headers;
import com.transvision.trmcollection.values.ClassGPS;
import com.transvision.trmcollection.values.FunctionCalls;
import com.transvision.trmcollection.values.GetSetValues;
import com.transvision.trmcollection.values.NumberToWords;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

import static android.text.Layout.Alignment.ALIGN_CENTER;
import static android.text.Layout.Alignment.ALIGN_NORMAL;
import static com.transvision.trmcollection.bluetooth.BluetoothService.printerconnected;
import static com.transvision.trmcollection.values.Constants.COLLECTION_DETAILS_FAILURE;
import static com.transvision.trmcollection.values.Constants.COLLECTION_DETAILS_SUCCESS;
import static com.transvision.trmcollection.values.Constants.COLLECTION_KEY;
import static com.transvision.trmcollection.values.Constants.COLLECTION_POSTING_FAILURE;
import static com.transvision.trmcollection.values.Constants.COLLECTION_POSTING_PAID;
import static com.transvision.trmcollection.values.Constants.COLLECTION_POSTING_SUCCESS;
import static com.transvision.trmcollection.values.Constants.COLLECTION_PRINTING_COMPLETED;
import static com.transvision.trmcollection.values.Constants.COLLECTION_SCAN;
import static com.transvision.trmcollection.values.Constants.COLLECTION_TOTAL;
import static com.transvision.trmcollection.values.Constants.COLLECTION_TRANSACTION_LIST_EMPTY;
import static com.transvision.trmcollection.values.Constants.NON_REVENUE_HEAD;
import static com.transvision.trmcollection.values.Constants.NON_REVENUE_HEAD_KEY;
import static com.transvision.trmcollection.values.Constants.NON_REVENUE_HEAD_LIST_FAILURE;
import static com.transvision.trmcollection.values.Constants.NON_REVENUE_HEAD_LIST_SUCCESS;
import static com.transvision.trmcollection.values.Constants.PRINTER_HEADER;
import static com.transvision.trmcollection.values.Constants.sPref_COLLECTION_COLLECTED;
import static com.transvision.trmcollection.values.Constants.sPref_COLLECTION_DATE;
import static com.transvision.trmcollection.values.Constants.sPref_COLLECTION_LIMIT;
import static com.transvision.trmcollection.values.Constants.sPref_COLLECTION_TYPE;
import static com.transvision.trmcollection.values.Constants.sPref_END_TIME;
import static com.transvision.trmcollection.values.Constants.sPref_MRCODE;
import static com.transvision.trmcollection.values.Constants.sPref_MRNAME;
import static com.transvision.trmcollection.values.Constants.sPref_ROLE;
import static com.transvision.trmcollection.values.Constants.sPref_START_TIME;
import static com.transvision.trmcollection.values.Constants.sPref_SUBDIVCODE;

public class Collection_AAO_AEE extends Fragment implements View.OnClickListener {
    private static final int DLG_PRINT_START = 1;
    private static final int DLG_PRINT_OFFICE = 2;
    private static final int DLG_PRINT_CONNECTION = 3;
    private static final int DLG_INTERNET_CONNECTION = 4;
    private static final int DLG_ACCOUNT_ID_FAILURE = 5;
    private static final int DLG_COLLECTION_PAID = 6;
    private static final int DLG_COLLECTION_FAILED= 7;
    private static final int DLG_COLLECTION_PAY = 8;
    private static final int DLG_COLLECTION_PAY_VALID = 9;
    private static final int DLG_COLLECTION_CASH_LIMIT = 10;
    private static final int DLG_COLLECTION_DAY_LIMIT = 11;
    private static final int DLG_COLLECTION_PAY_MORE_BILL = 12;
    private static final int DLG_COLLECTION_TIME = 13;
    private static final int DLG_ACCOUNT_ID_VALID = 14;
    private static final int DLG_ACCOUNT_ID_ALREADY_SEARCH = 15;
    private static final int DLG_SYSTEM_TIME_NOT_MATCHING = 16;
    private static final int DLG_PRINT_NEXT = 17;
    private static final int DLG_PAYMENT_CHQ_MODE = 18;
    private static final int DLG_PAYMENT_DD_MODE = 19;
    private static final int DLG_TRANSACTION_COUNT = 20;
    private static final int DLG_CHEQUE_DISHONOUR = 21;
    private static final int DLG_MIS_TYPE_ERROR = 22;
    private static final int DLG_CHEQUE_ERROR = 23;
    private static final int DLG_CHEQUE_MICR_ERROR = 24;
    private static final int DLG_DD_ERROR = 25;

    View view;
    TextInputLayout til_acc_id;
    EditText et_acc_id, et_paid_amount, et_chq_dd_date;
    TextView tv_customer_name, tv_customer_rrno, tv_customer_acc_id, tv_customer_tariff, tv_customer_bill_amount, tv_total_collection;
    Button bt_add_transaction, bt_print, bt_cancel;
    RadioGroup coll_mode_group, coll_type_group;
    RadioButton coll_cash_mode, coll_chq_mode, coll_dd_mode, coll_rev_mode, coll_non_rev_mode;
    RecyclerView transaction_details_view;
    ArrayList<GetSetValues> transaction_details_list;
    ArrayList<String> amt_words_list, mis_type_list;
    LinearLayout customer_details_layout, details_table_layout, collection_type_layout, mis_type_layout, transaction_mode_layout;
    Spinner sp_mis_type;
    RelativeLayout print_layout;
    Details_adapter detailsAdapter;
    SharedPreferences sPref;
    SharedPreferences.Editor editor;
    SendingData sendingData;
    GetSetValues getSetValues, getSet;
    FunctionCalls functionCalls;
    ProgressDialog progressDialog;
    TRMCollection_Database collectionDatabase;
    NumberToWords ntw;
    ClassGPS classGPS;
    DatePickerDialog datePickerDialog;
    ArrayAdapter<String> header_adapter;
    Gson gson;
    String[] text;

    BluetoothPrinter mBtp;
    AnalogicsThermalPrinter conn = MainActivity.conn;
    Bluetooth_Printer_3inch_prof_ThermalAPI api;
    Pos mPos = BluetoothService.mPos;
    ExecutorService es = BluetoothService.es;
    DecimalFormat num;

    double bill_amount=0, collected_amount=0, max_collection=0;
    String paid_amount="", coll_pre_receipt_no="", coll_device_id="", coll_mode="0", coll_receipt_time="", coll_gpslong="", coll_gpslat="",
            numinwords="", numinwords1="", coll_mr_code="", amount_paid="", coll_printer="", coll_cheque_dd_number="0", coll_cheque_micr="0",
            coll_chq_dd_date="", coll_chq_dd_bank="0", coll_non_rev_mis_type="", jsonText="";
    boolean officecopy=false, chq_dishonour_flag=false, revenue_mode=false;
    double paying_amount=0;
    String pay_today_date = new SimpleDateFormat("ddMMyyyy", Locale.US).format(new Date());
    int position=0, bank_selected=0, date=0, month=0, year=0, mis_type_position=1;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case COLLECTION_DETAILS_SUCCESS:
                    progressDialog.dismiss();
                    view_Customer_Details();
                    break;

                case COLLECTION_DETAILS_FAILURE:
                    progressDialog.dismiss();
                    showdialog(DLG_ACCOUNT_ID_FAILURE);
                    break;

                case COLLECTION_TRANSACTION_LIST_EMPTY:
                    clear_transactions();
                    break;

                case COLLECTION_POSTING_SUCCESS:
                    progressDialog.dismiss();
                    coll_receipt_time = functionCalls.receipt_date_time();
                    collectionDatabase.update_collection_details(transaction_details_list.get(position).getCustid(),
                            getSetValues.getColl_posting_receipt_no(), getSetValues.getPosting_unique_id(),
                            coll_receipt_time, functionCalls.receipt_date());
                    if (position > 0) {
                        showdialog(DLG_PRINT_NEXT);
                    } else showdialog(DLG_PRINT_START);
                    break;

                case COLLECTION_POSTING_PAID:
                    progressDialog.dismiss();
                    showdialog(DLG_COLLECTION_PAID);
                    break;

                case COLLECTION_POSTING_FAILURE:
                    progressDialog.dismiss();
                    showdialog(DLG_COLLECTION_FAILED);
                    break;

                case COLLECTION_PRINTING_COMPLETED:
                    if (transaction_details_list.size() > 0) {
                        functionCalls.logStatus("transaction_details_list: "+transaction_details_list.size());
                        position++;
                        functionCalls.logStatus("Position: "+position);
                        if (position == transaction_details_list.size()) {
                            clear_collection_screen();
                        } else posting_collection_data();
                    } else clear_collection_screen();
                    break;

                case COLLECTION_TOTAL:
                    tv_total_collection.setText(String.format("%s %s /-", getActivity().getResources().getString(R.string.rupee), getSetValues.getCollection_total()));
                    break;

                case NON_REVENUE_HEAD_LIST_SUCCESS:
                    functionCalls.logStatus("Headers Success...!!!");
                    jsonText = sPref.getString(NON_REVENUE_HEAD_KEY, "");
                    text = gson.fromJson(jsonText, String[].class);
                    mis_type_list.clear();
                    for (String mis_type : text) {
                        mis_type_list.add(mis_type);
                        header_adapter.notifyDataSetChanged();
                    }
                    break;

                case NON_REVENUE_HEAD_LIST_FAILURE:
                    functionCalls.logStatus("Headers Failure...!!!");
                    break;
            }
            return false;
        }
    });

    public Collection_AAO_AEE() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_collection_aao_aee, container, false);

        initialize();

        coll_mode_setup();

        Bundle bundle = getArguments();
        if (bundle != null) {
            searchAccountID(bundle.getString(COLLECTION_SCAN));
        }

        et_acc_id.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (!TextUtils.isEmpty(et_acc_id.getText().toString())) {
                        searchAccountID(String.format("%10s", et_acc_id.getText().toString()).replace(' ', '0'));
                    } else showdialog(DLG_ACCOUNT_ID_VALID);
                }
                return false;
            }
        });

        functionCalls.logStatus("Collection AAO AEE");

        et_paid_amount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (((MainActivity) getActivity()).getInternetConnection().isConnectingToInternet())
                        add_transaction();
                    else showdialog(DLG_INTERNET_CONNECTION);
                }
                return false;
            }
        });

        sp_mis_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mis_type_position = position;
                coll_non_rev_mis_type = mis_type_list.get(position);
                sp_mis_type.setSelection(mis_type_position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.aao_btn_add_transaction:
                if (((MainActivity) getActivity()).getInternetConnection().isConnectingToInternet())
                    add_transaction();
                else showdialog(DLG_INTERNET_CONNECTION);
                break;

            case R.id.coll_print_btn:
                if (printerconnected) {
                    switch (coll_mode) {
                        case "0":
                            if (((MainActivity) getActivity()).getInternetConnection().isConnectingToInternet()) {
                                posting_collection_data();
                            } else showdialog(DLG_INTERNET_CONNECTION);
                            break;

                        case "1":
                            showdialog(DLG_PAYMENT_CHQ_MODE);
                            break;

                        case "2":
                            showdialog(DLG_PAYMENT_DD_MODE);
                            break;
                    }
                } else showdialog(DLG_PRINT_CONNECTION);
                break;

            case R.id.coll_cancel_btn:
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuInflater mi = getActivity().getMenuInflater();
        mi.inflate(R.menu.collection_scan_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan_barcode:
                ((MainActivity) getActivity()).switchContent(MainActivity.Steps.FORM5, "Scan BarCode");
                break;
        }
        return false;
    }

    private void searchAccountID(String accountID) {
        if (collectionDatabase.check_collection_by_account_id_on_date(accountID, functionCalls.receipt_date())) {
            if (((MainActivity) getActivity()).getInternetConnection().isConnectingToInternet()) {
                functionCalls.showprogressdialog("Record", getActivity().getResources().getString(R.string.collection_acc_details), progressDialog);
                Collection_Details collectionDetails = sendingData.new Collection_Details(handler, getSetValues);
                collectionDetails.execute(sPref.getString(sPref_MRCODE, ""), accountID,
                        ((MainActivity) getActivity()).getCollection_device_id(), sPref.getString(sPref_ROLE, ""));
            } else showdialog(DLG_INTERNET_CONNECTION);
        } else showdialog(DLG_ACCOUNT_ID_ALREADY_SEARCH);
    }

    private void initialize() {
        til_acc_id = view.findViewById(R.id.aao_til_customer_account_id);
        et_acc_id = view.findViewById(R.id.aao_et_customer_account_id);
        et_paid_amount = view.findViewById(R.id.aao_et_paid_amount);

        tv_customer_name = view.findViewById(R.id.aao_customer_name);
        tv_customer_rrno = view.findViewById(R.id.aao_customer_rrno);
        tv_customer_acc_id = view.findViewById(R.id.aao_customer_account_id);
        tv_customer_tariff = view.findViewById(R.id.aao_customer_tariff);
        tv_customer_bill_amount = view.findViewById(R.id.aao_customer_bill_amount);
        tv_total_collection = view.findViewById(R.id.total_coll);

        bt_add_transaction = view.findViewById(R.id.aao_btn_add_transaction);
        bt_add_transaction.setOnClickListener(this);
        bt_print = view.findViewById(R.id.coll_print_btn);
        bt_print.setOnClickListener(this);
        bt_cancel = view.findViewById(R.id.coll_cancel_btn);
        bt_cancel.setOnClickListener(this);

        customer_details_layout = view.findViewById(R.id.aao_customer_details_layout);
        details_table_layout = view.findViewById(R.id.aao_details_list_layout);
        print_layout = view.findViewById(R.id.print_bottom_bar_layout);
        collection_type_layout = view.findViewById(R.id.aao_coll_type_layout);
        mis_type_layout = view.findViewById(R.id.aao_non_revenue_header_layout);
        transaction_mode_layout = view.findViewById(R.id.aao_transaction_mode_layout);

        coll_mode_group = view.findViewById(R.id.coll_mode_group);
        coll_cash_mode = view.findViewById(R.id.coll_cash_mode);
        coll_chq_mode = view.findViewById(R.id.coll_chq_mode);
        coll_dd_mode = view.findViewById(R.id.coll_dd_mode);
        coll_type_group = view.findViewById(R.id.aao_coll_type_group);
        coll_rev_mode = view.findViewById(R.id.aao_coll_rev_mode);
        coll_non_rev_mode = view.findViewById(R.id.aao_coll_non_rev_mode);

        classGPS = ((MainActivity) getActivity()).getClassGPS();
        sendingData = new SendingData(getActivity());
        sPref = ((MainActivity) getActivity()).getsharedPref();
        editor = sPref.edit();
        editor.apply();
        getSetValues = ((MainActivity) getActivity()).getSetValues();
        functionCalls = new FunctionCalls();
        progressDialog = new ProgressDialog(getActivity());
        collectionDatabase = ((MainActivity) getActivity()).getCollectionDatabase();
        ntw = new NumberToWords();
        num = new DecimalFormat("##.00");
        amt_words_list = new ArrayList<>();
        gson = new Gson();

        sp_mis_type = view.findViewById(R.id.aao_sp_mis_type_list);
        sp_mis_type.setSelection(mis_type_position);
        mis_type_list = new ArrayList<>();
        header_adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, mis_type_list);
        sp_mis_type.setAdapter(header_adapter);
        if (StringUtils.startsWithIgnoreCase(sPref.getString(NON_REVENUE_HEAD, ""), "Yes")) {
            jsonText = sPref.getString(NON_REVENUE_HEAD_KEY, "");
            text = gson.fromJson(jsonText, String[].class);
            mis_type_list.clear();
            for (String mis_type : text) {
                mis_type_list.add(mis_type);
                header_adapter.notifyDataSetChanged();
            }
        }

        transaction_details_view = view.findViewById(R.id.aao_details_list_view);
        transaction_details_list = ((MainActivity) getActivity()).get_details_list();
        detailsAdapter = new Details_adapter(transaction_details_list, getSetValues, handler, editor, sPref,
                collectionDatabase, functionCalls.receipt_date());
        transaction_details_view.setHasFixedSize(true);
        transaction_details_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        transaction_details_view.setAdapter(detailsAdapter);

        if (transaction_details_list.size() > 0) {
            details_table_layout.setVisibility(View.VISIBLE);
            print_layout.setVisibility(View.VISIBLE);
            if (chq_dishonour_flag)
                coll_chq_mode.setVisibility(View.GONE);
            else coll_chq_mode.setVisibility(View.VISIBLE);
            if (revenue_mode)
                coll_non_rev_mode.setVisibility(View.GONE);
            else coll_non_rev_mode.setVisibility(View.VISIBLE);
        }

        max_collection = Double.parseDouble(sPref.getString(sPref_COLLECTION_LIMIT, ""));
        coll_device_id = ((MainActivity) getActivity()).getCollection_device_id();
        coll_mr_code = sPref.getString(sPref_MRCODE, "");
        if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_ROLE, ""), "AAO")) {
            coll_mr_code = "10"+coll_mr_code.substring(2);
        } else if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_ROLE, ""), "AEE")) {
            coll_mr_code = "11"+coll_mr_code.substring(2);
        }
        coll_pre_receipt_no = functionCalls.receipt_date().substring(0, 4)+coll_mr_code;

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

        if (StringUtils.startsWithIgnoreCase(getSetValues.getCollection_login(), "Yes")) {
            if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_COLLECTION_TYPE, ""), "1")) {
                Non_Revenue_Headers nonRevenueHeaders = sendingData.new Non_Revenue_Headers(handler, editor);
                nonRevenueHeaders.execute();
            }
        }
    }

    private void add_transaction() {
        if (functionCalls.compare_Times(sPref.getString(sPref_COLLECTION_DATE, "")+" "+sPref.getString(sPref_START_TIME, ""),
                sPref.getString(sPref_COLLECTION_DATE, "")+" "+sPref.getString(sPref_END_TIME, ""))) {
            if (transaction_details_list.size() <= 25) {
                if (printerconnected) {
                    paid_amount = et_paid_amount.getText().toString();
                    if (!TextUtils.isEmpty(paid_amount)) {
                        paying_amount = Double.parseDouble(paid_amount);
                        if (paying_amount > 0) {
                            if (validate_paying()) {
                                if (validate_bill_pay()) {
                                    collected_amount = Double.parseDouble(sPref.getString(sPref_COLLECTION_COLLECTED, "")) + paying_amount;
                                    functionCalls.logStatus("Collection Collected: "+collected_amount);
                                    if (collected_amount <= max_collection) {
                                        if (getMis_type()) {
                                            if (collectionDatabase.check_receipt_time(functionCalls.receipt_date_time())) {
                                                add_transaction_details();
                                            } else showdialog(DLG_SYSTEM_TIME_NOT_MATCHING);
                                        } else showdialog(DLG_MIS_TYPE_ERROR);
                                    } else showdialog(DLG_COLLECTION_DAY_LIMIT);
                                } else showdialog(DLG_COLLECTION_PAY_MORE_BILL);
                            } else showdialog(DLG_COLLECTION_CASH_LIMIT);
                        } else showdialog(DLG_COLLECTION_PAY_VALID);
                    } else showdialog(DLG_COLLECTION_PAY);
                } else showdialog(DLG_PRINT_CONNECTION);
            } else showdialog(DLG_TRANSACTION_COUNT);
        } else showdialog(DLG_COLLECTION_TIME);
    }

    private boolean validate_paying() {
        if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_COLLECTION_TYPE, ""), "1")) {
            return coll_non_rev_mode.isChecked() || !coll_cash_mode.isChecked() || paying_amount <= 10000;
        } else return !coll_cash_mode.isChecked() || paying_amount <= 10000;
    }

    private boolean validate_bill_pay() {
        if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_COLLECTION_TYPE, ""), "1")) {
            return coll_non_rev_mode.isChecked() || paying_amount >= bill_amount;
        } else return paying_amount >= bill_amount;
    }

    private void posting_collection_data() {
        GetSetValues getset = transaction_details_list.get(position);
        functionCalls.showprogressdialog(getActivity().getResources().getString(R.string.posting_title), getActivity().getResources().getString(R.string.posting_message), progressDialog);
        if (coll_non_rev_mode.isChecked()) {
            Non_Revenue_Collection_data nonRevenueCollectionData = sendingData.new Non_Revenue_Collection_data(getSetValues, handler);
            nonRevenueCollectionData.execute(coll_device_id, sPref.getString(sPref_MRCODE, "")+COLLECTION_KEY,
                    getset.getRrno(), functionCalls.receipt_date(), String.valueOf(getset.getAmount()), coll_mode,
                    getset.getCustid(), getset.getColl_grp_customer_name(), coll_cheque_dd_number, coll_cheque_micr,
                    coll_chq_dd_bank, coll_chq_dd_date, coll_non_rev_mis_type);
        } else {
            Posting_Collection_data postingCollectionData = sendingData.new Posting_Collection_data(getSetValues, handler);
            postingCollectionData.execute(coll_device_id, sPref.getString(sPref_MRCODE, "")+COLLECTION_KEY,
                    getset.getRrno(), functionCalls.receipt_date(), String.valueOf(getset.getAmount()), coll_mode,
                    getset.getCustid(), getset.getColl_grp_customer_name(), coll_cheque_dd_number, coll_cheque_micr,
                    coll_chq_dd_bank, coll_chq_dd_date);
        }
    }

    private void view_Customer_Details() {
        et_acc_id.setText("");
        et_paid_amount.setText("");
        et_paid_amount.setEnabled(true);
        et_paid_amount.requestFocus();
        transaction_mode_layout.setVisibility(View.VISIBLE);
        customer_details_layout.setVisibility(View.VISIBLE);
        tv_customer_name.setText(getSetValues.getCustomer_name());
        tv_customer_rrno.setText(getSetValues.getCustomer_rrno());
        tv_customer_acc_id.setText(getSetValues.getCustomer_accid());
        tv_customer_tariff.setText(getSetValues.getCustomer_tariff());
        tv_customer_bill_amount.setText(String.format("%s %s /-", getActivity().getResources().getString(R.string.rupee), getSetValues.getCustomer_bill_amount()));
        bill_amount = Double.parseDouble(getSetValues.getCustomer_bill_amount());
        if (StringUtils.startsWithIgnoreCase(getSetValues.getColl_chq_dishonour(), "Y") ||
                StringUtils.startsWithIgnoreCase(getSetValues.getColl_chq_dishonour(), "y")) {
            showdialog(DLG_CHEQUE_DISHONOUR);
            chq_dishonour_flag = true;
            coll_chq_mode.setVisibility(View.GONE);
        } else {
            if (!chq_dishonour_flag)
                coll_chq_mode.setVisibility(View.VISIBLE);
        }
        if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_COLLECTION_TYPE, ""), "1")) {
            collection_type_layout.setVisibility(View.VISIBLE);
        } else collection_type_layout.setVisibility(View.GONE);
    }

    private String transactionid(String date) {
        int trans_id;
        Cursor data = collectionDatabase.countfortransaction(date, sPref.getString(sPref_ROLE, ""));
        data.moveToNext();
        int count = data.getInt(data.getColumnIndex("COUNT"));
        if (count == 0) {
            trans_id = 0;
        } else {
            String trans = data.getString(data.getColumnIndex("TRANSACTION_ID"));
            String transid = trans.substring(trans.length() - 5);
            trans_id = Integer.parseInt(transid);
        }
        NumberFormat nf = new DecimalFormat("00000");
        trans_id = trans_id + 1;
        String transactiondate = functionCalls.transactiondateformat(date);
        String transaction_id = "";
        switch (coll_mode) {
            case "0":
                transaction_id = "cashid" + "_" + sPref.getString(sPref_ROLE, "") + "_" + transactiondate + "_" + "" + nf.format(trans_id);
                break;

            case "1":
                transaction_id = "chqid" + "_" + sPref.getString(sPref_ROLE, "") + "_" + transactiondate + "_" + "" + nf.format(trans_id);
                break;

            case "2":
                transaction_id = "ddid" + "_" + sPref.getString(sPref_ROLE, "") + "_" + transactiondate + "_" + "" + nf.format(trans_id);
                break;
        }
        data.close();
        return transaction_id;
    }

    private void coll_mode_setup() {
        coll_mode_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (coll_cash_mode.isChecked()) {
                    coll_mode = "0";
                } else if (coll_chq_mode.isChecked()) {
                    coll_mode = "1";
                    coll_cash_mode.setVisibility(View.GONE);
                } else {
                    coll_mode = "2";
                    coll_cash_mode.setVisibility(View.GONE);
                }
            }
        });

        coll_type_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (coll_rev_mode.isChecked()) {
                    mis_type_layout.setVisibility(View.GONE);
                    et_paid_amount.setText("");
                } else {
                    sp_mis_type.setSelection(0);
                    mis_type_layout.setVisibility(View.VISIBLE);
                    et_paid_amount.setText("");
                }
            }
        });
    }

    private boolean getMis_type() {
        return !coll_non_rev_mode.isChecked() || mis_type_position != 0;
    }

    private void add_transaction_details() {
        ((MainActivity) getActivity()).hidekeyboard(et_paid_amount);
        customer_details_layout.setVisibility(View.GONE);
        details_table_layout.setVisibility(View.VISIBLE);
        if (coll_non_rev_mode.isChecked()) {
            til_acc_id.setVisibility(View.GONE);
            coll_chq_mode.setVisibility(View.GONE);
        }
        if (coll_rev_mode.isChecked()) {
            revenue_mode = true;
            coll_non_rev_mode.setVisibility(View.GONE);
        }
        et_acc_id.requestFocus();
        print_layout.setVisibility(View.VISIBLE);
        getSet = new GetSetValues();
        getSet.setRrno(getSetValues.getCustomer_rrno());
        getSet.setCustid(getSetValues.getCustomer_accid());
        getSet.setAmount(Double.parseDouble(paid_amount));
        getSet.setColl_grp_customer_name(getSetValues.getCustomer_name());
        amount_paid = num.format(Double.parseDouble(paid_amount));
        numinwords1 = ntw.convert(Integer.parseInt(paid_amount));
        numinwords = numinwords1.substring(0, 1).toUpperCase() + numinwords1.substring(1);
        amt_words_list.clear();
        switch (coll_printer) {
            case "NGX":
                functionCalls.splitString("Rs. "+numinwords + " only", 36, amt_words_list);
                if (amt_words_list.size() == 1) {
                    getSet.setPay_amount_in_words_1(amt_words_list.get(0));
                    getSet.setPay_amount_in_words_2(" ");
                    getSet.setPay_amount_in_words_3(" ");
                } else {
                    if (amt_words_list.size() == 2) {
                        getSet.setPay_amount_in_words_1(amt_words_list.get(0));
                        getSet.setPay_amount_in_words_2(amt_words_list.get(1));
                        getSet.setPay_amount_in_words_3(" ");
                    } else {
                        if (amt_words_list.size() == 3) {
                            getSet.setPay_amount_in_words_1(amt_words_list.get(0));
                            getSet.setPay_amount_in_words_2(amt_words_list.get(1));
                            getSet.setPay_amount_in_words_3(amt_words_list.get(2));
                        }
                    }
                }
                break;

            case "ALG":
                functionCalls.splitString("Amount in Words  : Rs. " + numinwords + " only", 36, amt_words_list);
                if (amt_words_list.size() == 1) {
                    getSet.setPay_amount_in_words_1("  "+amt_words_list.get(0));
                    getSet.setPay_amount_in_words_2(" ");
                    getSet.setPay_amount_in_words_3(" ");
                } else {
                    if (amt_words_list.size() == 2) {
                        getSet.setPay_amount_in_words_1("  "+amt_words_list.get(0));
                        getSet.setPay_amount_in_words_2("  "+amt_words_list.get(1));
                        getSet.setPay_amount_in_words_3(" ");
                    } else {
                        if (amt_words_list.size() == 3) {
                            getSet.setPay_amount_in_words_1("  "+amt_words_list.get(0));
                            getSet.setPay_amount_in_words_2("  "+amt_words_list.get(1));
                            getSet.setPay_amount_in_words_3("  "+amt_words_list.get(2));
                        }
                    }
                }
                break;

            case "GPT":
                functionCalls.splitString("Amount in Words  : Rs. " + numinwords + " only", 45, amt_words_list);
                if (amt_words_list.size() == 1) {
                    getSet.setPay_amount_in_words_1("  "+amt_words_list.get(0));
                    getSet.setPay_amount_in_words_2(" ");
                } else {
                    if (amt_words_list.size() == 2) {
                        getSet.setPay_amount_in_words_1("  "+amt_words_list.get(0));
                        getSet.setPay_amount_in_words_2("  "+amt_words_list.get(1));
                    } else {
                        if (amt_words_list.size() == 3) {
                            getSet.setPay_amount_in_words_1("  "+amt_words_list.get(0));
                            getSet.setPay_amount_in_words_2("  "+amt_words_list.get(1));
                            getSet.setPay_amount_in_words_3("  "+amt_words_list.get(2));
                        }
                    }
                }
                break;
        }
        transaction_details_list.add(getSet);
        detailsAdapter.notifyDataSetChanged();
        editor.putString(sPref_COLLECTION_COLLECTED, paid_amount);
        editor.commit();
        insertCollection_output();
        clear_customer_details();
    }

    private void insertCollection_output() {
        ContentValues cv = new ContentValues();
        cv.put("RRNO", getSetValues.getCustomer_rrno());
        cv.put("ACCOUNT_ID", getSetValues.getCustomer_accid());
        cv.put("LF_NO", getSetValues.getCustomer_LF_no());
        cv.put("NAME", getSetValues.getCustomer_name());
        cv.put("AMOUNT", amount_paid);
        cv.put("MODE_PAYMENT", coll_mode);
        cv.put("RECEIPT_NO", "");
        cv.put("TRANSACTION_ID", transactionid(functionCalls.receipt_date()));
        cv.put("MACHINE_ID", coll_device_id);
        cv.put("RECEIPT_DATE_TIME", "");
        cv.put("RECEIPT_DATE", functionCalls.receipt_date());
        cv.put("MR_CODE", sPref.getString(sPref_MRCODE, ""));
        cv.put("GPS_LAT", "");
        cv.put("GPS_LONG", "");
        cv.put("UNIQUE_ID", "");
        cv.put("ROLE", sPref.getString(sPref_ROLE, ""));
        collectionDatabase.insert_collection_details(cv);
    }

    private void clear_customer_details() {
        tv_customer_name.setText("");
        tv_customer_rrno.setText("");
        tv_customer_acc_id.setText("");
        tv_customer_tariff.setText("");
        tv_customer_bill_amount.setText("");
        et_paid_amount.setText("");
    }

    private void clear_transactions() {
        til_acc_id.setVisibility(View.VISIBLE);
        et_paid_amount.setEnabled(true);
        et_paid_amount.setText("");
        revenue_mode = false;
        chq_dishonour_flag = false;
        coll_cash_mode.setChecked(true);
        coll_rev_mode.setChecked(true);
        coll_cash_mode.setVisibility(View.VISIBLE);
        coll_chq_mode.setVisibility(View.VISIBLE);
        coll_non_rev_mode.setVisibility(View.VISIBLE);
        details_table_layout.setVisibility(View.GONE);
        transaction_mode_layout.setVisibility(View.GONE);
        print_layout.setVisibility(View.GONE);
        coll_cheque_dd_number="0"; coll_cheque_micr="0"; coll_chq_dd_bank="0"; coll_chq_dd_date="";
        sp_mis_type.setSelection(0);
    }

    private void clear_collection_screen() {
        transaction_details_list.clear();
        detailsAdapter.notifyDataSetChanged();
        print_layout.setVisibility(View.GONE);
        transaction_mode_layout.setVisibility(View.GONE);
        details_table_layout.setVisibility(View.GONE);
        customer_details_layout.setVisibility(View.GONE);
        til_acc_id.setVisibility(View.VISIBLE);
        et_paid_amount.setText("");
        et_paid_amount.setEnabled(true);
        amt_words_list.clear();
        et_acc_id.requestFocus();
        position = 0;
        coll_rev_mode.setChecked(true);
        coll_cash_mode.setChecked(true);
        revenue_mode = false;
        chq_dishonour_flag = false;
        coll_cash_mode.setVisibility(View.VISIBLE);
        coll_chq_mode.setVisibility(View.VISIBLE);
        coll_non_rev_mode.setVisibility(View.VISIBLE);
        coll_cheque_dd_number="0"; coll_cheque_micr="0"; coll_chq_dd_bank="0"; coll_chq_dd_date="";
        sp_mis_type.setSelection(0);
    }

    private void showdialog(int id) {
        AlertDialog alertDialog;
        switch (id) {
            case DLG_PRINT_START:
                AlertDialog.Builder print_start = new AlertDialog.Builder(getActivity());
                print_start.setTitle(getActivity().getResources().getString(R.string.collection_print_title));
                print_start.setCancelable(false);
                print_start.setMessage(getActivity().getResources().getString(R.string.ask_print));
                print_start.setPositiveButton(getActivity().getResources().getString(R.string.yes_label), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        functionCalls.showprogressdialog(getActivity().getResources().getString(R.string.printing_label), getActivity().getResources().getString(R.string.printing), progressDialog);
                        switch (coll_printer) {
                            case "NGX":
                                collection_print_on_NGX();
                                new Handler().postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        showdialog(DLG_PRINT_OFFICE);
                                    }
                                }, 5000);
                                break;

                            case "ALG":
                                collection_print_on_analogics();
                                new Handler().postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        showdialog(DLG_PRINT_OFFICE);
                                    }
                                }, 5000);
                                break;

                            case "GPT":
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        es.submit(new TaskPrint(mPos));
                                    }
                                }, 1000);
                                break;
                        }
                    }
                });
                alertDialog = print_start.create();
                alertDialog.show();
                break;

            case DLG_PRINT_OFFICE:
                AlertDialog.Builder office_print = new AlertDialog.Builder(getActivity());
                office_print.setTitle(getActivity().getResources().getString(R.string.collection_print_title));
                office_print.setCancelable(false);
                office_print.setMessage(getActivity().getResources().getString(R.string.office_payment_print));
                office_print.setPositiveButton(getActivity().getResources().getString(R.string.yes_label), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        officecopy = true;
                        functionCalls.showprogressdialog(getActivity().getResources().getString(R.string.printing_label), getActivity().getResources().getString(R.string.printing), progressDialog);
                        switch (coll_printer) {
                            case "NGX":
                                collection_print_on_NGX();
                                new Handler().postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        handler.sendEmptyMessage(COLLECTION_PRINTING_COMPLETED);
                                    }
                                }, 3000);
                                break;

                            case "ALG":
                                collection_print_on_analogics();
                                new Handler().postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        handler.sendEmptyMessage(COLLECTION_PRINTING_COMPLETED);
                                    }
                                }, 3000);
                                break;

                            case "GPT":
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        es.submit(new TaskPrint(mPos));
                                    }
                                }, 1000);
                                break;
                        }
                    }
                });
                office_print.setNeutralButton(getActivity().getResources().getString(R.string.reprint_label), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        officecopy = false;
                        functionCalls.showprogressdialog(getActivity().getResources().getString(R.string.printing_label), getActivity().getResources().getString(R.string.printing), progressDialog);
                        switch (coll_printer) {
                            case "NGX":
                                collection_print_on_NGX();
                                new Handler().postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        showdialog(DLG_PRINT_OFFICE);
                                    }
                                }, 5000);
                                break;

                            case "ALG":
                                collection_print_on_analogics();
                                new Handler().postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        showdialog(DLG_PRINT_OFFICE);
                                    }
                                }, 5000);
                                break;

                            case "GPT":
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        es.submit(new TaskPrint(mPos));
                                    }
                                }, 1000);
                                break;
                        }
                    }
                });
                alertDialog = office_print.create();
                alertDialog.show();
                break;

            case DLG_PRINT_CONNECTION:
                AlertDialog.Builder print_connection = new AlertDialog.Builder(getActivity());
                print_connection.setTitle(getActivity().getResources().getString(R.string.dlg_printer_connection));
                print_connection.setCancelable(false);
                print_connection.setMessage(getActivity().getResources().getString(R.string.printer_connection_error));
                print_connection.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                alertDialog = print_connection.create();
                alertDialog.show();
                break;

            case DLG_INTERNET_CONNECTION:
                AlertDialog.Builder internet_connection = new AlertDialog.Builder(getActivity());
                internet_connection.setCancelable(false);
                internet_connection.setMessage(getActivity().getResources().getString(R.string.internet_connection));
                internet_connection.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                alertDialog = internet_connection.create();
                alertDialog.show();
                break;

            case DLG_ACCOUNT_ID_FAILURE:
                AlertDialog.Builder acc_id_fail = new AlertDialog.Builder(getActivity());
                acc_id_fail.setCancelable(false);
                acc_id_fail.setMessage(getActivity().getResources().getString(R.string.collection_acc_id_error));
                acc_id_fail.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                alertDialog = acc_id_fail.create();
                alertDialog.show();
                break;

            case DLG_COLLECTION_PAID:
                AlertDialog.Builder collection_paid = new AlertDialog.Builder(getActivity());
                collection_paid.setCancelable(false);
                collection_paid.setMessage(getActivity().getResources().getString(R.string.collection_paid));
                collection_paid.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clear_collection_screen();
                    }
                });
                alertDialog = collection_paid.create();
                alertDialog.show();
                break;

            case DLG_COLLECTION_FAILED:
                AlertDialog.Builder collection_failed = new AlertDialog.Builder(getActivity());
                collection_failed.setCancelable(false);
                collection_failed.setMessage(getActivity().getResources().getString(R.string.collection_posting_fail));
                collection_failed.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                alertDialog = collection_failed.create();
                alertDialog.show();
                break;

            case DLG_COLLECTION_PAY:
                AlertDialog.Builder collection_pay = new AlertDialog.Builder(getActivity());
                collection_pay.setCancelable(false);
                collection_pay.setMessage(getActivity().getResources().getString(R.string.collection_amount));
                collection_pay.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                alertDialog = collection_pay.create();
                alertDialog.show();
                break;

            case DLG_COLLECTION_PAY_VALID:
                AlertDialog.Builder collection_pay_valid = new AlertDialog.Builder(getActivity());
                collection_pay_valid.setCancelable(false);
                collection_pay_valid.setMessage(getActivity().getResources().getString(R.string.collection_amount_valid));
                collection_pay_valid.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                alertDialog = collection_pay_valid.create();
                alertDialog.show();
                break;

            case DLG_COLLECTION_CASH_LIMIT:
                AlertDialog.Builder collection_cash_limit = new AlertDialog.Builder(getActivity());
                collection_cash_limit.setCancelable(false);
                collection_cash_limit.setMessage(getActivity().getResources().getString(R.string.collection_cash_limit));
                collection_cash_limit.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                alertDialog = collection_cash_limit.create();
                alertDialog.show();
                break;

            case DLG_COLLECTION_DAY_LIMIT:
                AlertDialog.Builder collection_day_limit = new AlertDialog.Builder(getActivity());
                collection_day_limit.setTitle(getActivity().getResources().getString(R.string.collection_over_title));
                collection_day_limit.setCancelable(false);
                collection_day_limit.setMessage(getActivity().getResources().getString(R.string.collection_over_message));
                collection_day_limit.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        collected_amount = collected_amount - paying_amount;
                        editor.putString(sPref_COLLECTION_COLLECTED, ""+collected_amount);
                        editor.commit();
                    }
                });
                alertDialog = collection_day_limit.create();
                alertDialog.show();
                break;

            case DLG_COLLECTION_PAY_MORE_BILL:
                AlertDialog.Builder collection_pay_more = new AlertDialog.Builder(getActivity());
                collection_pay_more.setCancelable(false);
                collection_pay_more.setMessage(getActivity().getResources().getString(R.string.collection_bill_amount));
                collection_pay_more.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                alertDialog = collection_pay_more.create();
                alertDialog.show();
                break;

            case DLG_COLLECTION_TIME:
                AlertDialog.Builder collection_time = new AlertDialog.Builder(getActivity());
                collection_time.setTitle(getActivity().getResources().getString(R.string.collection_time_title));
                collection_time.setCancelable(false);
                collection_time.setMessage(getActivity().getResources().getString(R.string.collection_time_message));
                collection_time.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        bt_add_transaction.setEnabled(false);
                    }
                });
                alertDialog = collection_time.create();
                alertDialog.show();
                break;

            case DLG_ACCOUNT_ID_VALID:
                AlertDialog.Builder acc_id_valid = new AlertDialog.Builder(getActivity());
                acc_id_valid.setCancelable(false);
                acc_id_valid.setMessage(getActivity().getResources().getString(R.string.collection_acc_id));
                acc_id_valid.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                alertDialog = acc_id_valid.create();
                alertDialog.show();
                break;

            case DLG_ACCOUNT_ID_ALREADY_SEARCH:
                AlertDialog.Builder acc_id_search = new AlertDialog.Builder(getActivity());
                acc_id_search.setCancelable(false);
                acc_id_search.setMessage(getActivity().getResources().getString(R.string.collection_acc_id_search_error));
                acc_id_search.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                alertDialog = acc_id_search.create();
                alertDialog.show();
                break;

            case DLG_SYSTEM_TIME_NOT_MATCHING:
                AlertDialog.Builder system_date = new AlertDialog.Builder(getActivity());
                system_date.setCancelable(false);
                system_date.setMessage(getActivity().getResources().getString(R.string.system_date_not_matching));
                system_date.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                alertDialog = system_date.create();
                alertDialog.show();
                break;

            case DLG_PRINT_NEXT:
                AlertDialog.Builder print_next = new AlertDialog.Builder(getActivity());
                print_next.setCancelable(false);
                print_next.setTitle(getActivity().getResources().getString(R.string.collection_print_title));
                print_next.setMessage(getActivity().getResources().getString(R.string.collection_next_print));
                print_next.setPositiveButton(getActivity().getResources().getString(R.string.next), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        functionCalls.showprogressdialog("Printing", getActivity().getResources().getString(R.string.printing), progressDialog);
                        switch (coll_printer) {
                            case "NGX":
//                                collection_print_on_NGX();
                                new Handler().postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        showdialog(DLG_PRINT_OFFICE);
                                    }
                                }, 5000);
                                break;

                            case "ALG":
//                                collection_print_on_analogics();
                                new Handler().postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                        showdialog(DLG_PRINT_OFFICE);
                                    }
                                }, 5000);
                                break;

                            case "GPT":
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        es.submit(new TaskPrint(mPos));
                                    }
                                }, 1000);
                                break;
                        }
                    }
                });
                alertDialog = print_next.create();
                alertDialog.show();
                break;

            case DLG_PAYMENT_CHQ_MODE:
                AlertDialog.Builder chq_mode = new AlertDialog.Builder(getActivity());
                chq_mode.setCancelable(false);
                chq_mode.setTitle(getActivity().getResources().getString(R.string.collection_chq_title));
                @SuppressLint("InflateParams")
                LinearLayout chq_view = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.collection_dialog, null);
                chq_mode.setView(chq_view);
                LinearLayout chq_details = chq_view.findViewById(R.id.cheque_layout);
                chq_details.setVisibility(View.VISIBLE);
                final EditText et_chq_no = chq_view.findViewById(R.id.et_cheque_no);
                final EditText et_chq_micr = chq_view.findViewById(R.id.et_cheque_micr);
                et_chq_dd_date = chq_view.findViewById(R.id.et_chq_dd_date);
                et_chq_dd_date.setText("");
                ImageView date_select = chq_view.findViewById(R.id.date_selection);
                final Spinner sp_banks = chq_view.findViewById(R.id.sp_bank_list);
                final ArrayList<GetSetValues> bank_list = new ArrayList<>();
                BtPrinteradapter bank_Adapter = new BtPrinteradapter(getActivity(), bank_list);
                for (int i = 0; i < getActivity().getResources().getStringArray(R.array.bank_list).length; i++) {
                    GetSetValues getSetValues = new GetSetValues();
                    getSetValues.setBt_printers(getResources().getStringArray(R.array.bank_list)[i]);
                    bank_list.add(getSetValues);
                    bank_Adapter.notifyDataSetChanged();
                }
                sp_banks.setAdapter(bank_Adapter);
                sp_banks.setSelection(bank_selected);
                sp_banks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        bank_selected = position;
                        sp_banks.setSelection(bank_selected);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                date_select.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        calenderDialog();
                    }
                });
                chq_mode.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                chq_mode.setNeutralButton(getActivity().getResources().getString(R.string.select_cancel), null);
                final AlertDialog cheque_dialog = chq_mode.create();
                cheque_dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button positive = cheque_dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        Button negative = cheque_dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                        positive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                coll_cheque_dd_number = et_chq_no.getText().toString();
                                if (!TextUtils.isEmpty(coll_cheque_dd_number)) {
                                    if (coll_cheque_dd_number.length() == 6) {
                                        coll_cheque_micr = et_chq_micr.getText().toString();
                                        if (!TextUtils.isEmpty(coll_cheque_micr)) {
                                            if (coll_cheque_micr.length() == 9) {
                                                coll_chq_dd_date = et_chq_dd_date.getText().toString();
                                                if (!TextUtils.isEmpty(coll_chq_dd_date)) {
                                                    if (bank_selected != 0) {
                                                        GetSetValues getSetValues = bank_list.get(bank_selected);
                                                        coll_chq_dd_bank = getSetValues.getBt_printers();
                                                        if (((MainActivity) getActivity()).getInternetConnection().isConnectingToInternet()) {
                                                            cheque_dialog.dismiss();
                                                            bank_selected=0;
                                                            posting_collection_data();
                                                        } else showdialog(DLG_INTERNET_CONNECTION);
                                                    } else functionCalls.showToast(getActivity(), getActivity().getResources().getString(R.string.collection_cheque_bank));
                                                } else et_chq_dd_date.setError(getActivity().getResources().getString(R.string.collection_cheque_date));
                                            } else et_chq_micr.setError(getActivity().getResources().getString(R.string.cheque_micr_error));
                                        } else et_chq_micr.setError(getActivity().getResources().getString(R.string.collection_cheque_micr));
                                    } else et_chq_no.setError(getActivity().getResources().getString(R.string.cheque_error));
                                } else et_chq_no.setError(getActivity().getResources().getString(R.string.collection_cheque_number));
                            }
                        });
                        negative.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                bank_selected = 0;
                                cheque_dialog.dismiss();
                            }
                        });
                    }
                });
                cheque_dialog.show();
                break;

            case DLG_PAYMENT_DD_MODE:
                AlertDialog.Builder dd_mode = new AlertDialog.Builder(getActivity());
                dd_mode.setCancelable(false);
                dd_mode.setTitle(getActivity().getResources().getString(R.string.collection_dd_title));
                @SuppressLint("InflateParams")
                LinearLayout dd_view = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.collection_dialog, null);
                dd_mode.setView(dd_view);
                LinearLayout dd_details = dd_view.findViewById(R.id.demand_draft_layout);
                dd_details.setVisibility(View.VISIBLE);
                final EditText et_dd_no = dd_view.findViewById(R.id.et_dd_no);
                final EditText et_dd_micr = dd_view.findViewById(R.id.et_dd_micr);
                et_chq_dd_date = dd_view.findViewById(R.id.et_chq_dd_date);
                et_chq_dd_date.setText("");
                ImageView dd_date_select = dd_view.findViewById(R.id.date_selection);
                final Spinner dd_sp_banks = dd_view.findViewById(R.id.sp_bank_list);
                final ArrayList<GetSetValues> dd_bank_list = new ArrayList<>();
                BtPrinteradapter dd_bank_Adapter = new BtPrinteradapter(getActivity(), dd_bank_list);
                for (int i = 0; i < getActivity().getResources().getStringArray(R.array.bank_list).length; i++) {
                    GetSetValues getSetValues = new GetSetValues();
                    getSetValues.setBt_printers(getResources().getStringArray(R.array.bank_list)[i]);
                    dd_bank_list.add(getSetValues);
                    dd_bank_Adapter.notifyDataSetChanged();
                }
                dd_sp_banks.setAdapter(dd_bank_Adapter);
                dd_sp_banks.setSelection(bank_selected);
                dd_sp_banks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        bank_selected = position;
                        dd_sp_banks.setSelection(bank_selected);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                dd_date_select.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        calenderDialog();
                    }
                });
                dd_mode.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                dd_mode.setNeutralButton(getActivity().getResources().getString(R.string.select_cancel), null);
                final AlertDialog dd_dialog = dd_mode.create();
                dd_dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button positive = dd_dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        Button negative = dd_dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                        positive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                coll_cheque_dd_number = et_dd_no.getText().toString();
                                if (!TextUtils.isEmpty(coll_cheque_dd_number)) {
                                    if (coll_cheque_dd_number.length() == 6) {
                                        coll_cheque_micr = et_dd_micr.getText().toString();
                                        if (!TextUtils.isEmpty(coll_cheque_micr)) {
                                            if (coll_cheque_micr.length() == 9) {
                                                coll_chq_dd_date = et_chq_dd_date.getText().toString();
                                                if (!TextUtils.isEmpty(coll_chq_dd_date)) {
                                                    if (bank_selected != 0) {
                                                        GetSetValues getSetValues = dd_bank_list.get(bank_selected);
                                                        coll_chq_dd_bank = getSetValues.getBt_printers();
                                                        if (((MainActivity) getActivity()).getInternetConnection().isConnectingToInternet()) {
                                                            dd_dialog.dismiss();
                                                            bank_selected=0;
                                                            posting_collection_data();
                                                        } else showdialog(DLG_INTERNET_CONNECTION);
                                                    } else functionCalls.showToast(getActivity(), getActivity().getResources().getString(R.string.collection_dd_bank));
                                                } else et_chq_dd_date.setError(getActivity().getResources().getString(R.string.collection_dd_date));
                                            } else et_dd_micr.setError(getActivity().getResources().getString(R.string.dd_micr_error));
                                        } else et_dd_micr.setError(getActivity().getResources().getString(R.string.collection_dd_micr));
                                    } else et_dd_no.setError(getActivity().getResources().getString(R.string.dd_error));
                                } else et_dd_no.setError(getActivity().getResources().getString(R.string.collection_dd_number));
                            }
                        });
                        negative.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                bank_selected = 0;
                                dd_dialog.dismiss();
                            }
                        });
                    }
                });
                dd_dialog.show();
                break;

            case DLG_TRANSACTION_COUNT:
                AlertDialog.Builder transaction_count = new AlertDialog.Builder(getActivity());
                transaction_count.setCancelable(false);
                transaction_count.setTitle(getActivity().getResources().getString(R.string.transaction_dialog_title));
                transaction_count.setMessage(getActivity().getResources().getString(R.string.transaction_dialog_msg));
                transaction_count.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                alertDialog = transaction_count.create();
                alertDialog.show();
                break;

            case DLG_CHEQUE_DISHONOUR:
                AlertDialog.Builder chq_dishonour = new AlertDialog.Builder(getActivity());
                chq_dishonour.setCancelable(false);
                chq_dishonour.setTitle(getActivity().getResources().getString(R.string.chq_dishonour_dialog_title));
                chq_dishonour.setMessage(getActivity().getResources().getString(R.string.chq_dishonour_dialog_msg));
                chq_dishonour.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                alertDialog = chq_dishonour.create();
                alertDialog.show();
                break;

            case DLG_MIS_TYPE_ERROR:
                AlertDialog.Builder mis_type_error = new AlertDialog.Builder(getActivity());
                mis_type_error.setCancelable(false);
                mis_type_error.setMessage(getActivity().getResources().getString(R.string.mis_type_error));
                mis_type_error.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                alertDialog = mis_type_error.create();
                alertDialog.show();
                break;

            case DLG_CHEQUE_ERROR:
                AlertDialog.Builder cheque_error = new AlertDialog.Builder(getActivity());
                cheque_error.setCancelable(false);
                cheque_error.setMessage(getActivity().getResources().getString(R.string.cheque_error));
                cheque_error.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialog = cheque_error.create();
                alertDialog.show();
                break;

            case DLG_CHEQUE_MICR_ERROR:
                AlertDialog.Builder micr_error = new AlertDialog.Builder(getActivity());
                micr_error.setCancelable(false);
                micr_error.setMessage(getActivity().getResources().getString(R.string.cheque_micr_error));
                micr_error.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                alertDialog = micr_error.create();
                alertDialog.show();
                break;

            case DLG_DD_ERROR:
                AlertDialog.Builder dd_error = new AlertDialog.Builder(getActivity());
                dd_error.setCancelable(false);
                dd_error.setMessage(getActivity().getResources().getString(R.string.dd_error));
                dd_error.setPositiveButton(getActivity().getResources().getString(R.string.select_ok), null);
                alertDialog = dd_error.create();
                alertDialog.show();
                break;
        }
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            Date Starttime = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            try {
                Starttime = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse((""+ year + "-" + ""+ (month + 1) + "-" + ""+dayOfMonth));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            et_chq_dd_date.setText(sdf.format(Starttime));
        }
    };

    private void calenderDialog() {
        Calendar cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        date = cal.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(getActivity(), dateSetListener, year, month, date);
        datePickerDialog.getDatePicker().setMaxDate(cal.getTimeInMillis());
        Calendar min_cal = Calendar.getInstance();
        min_cal.set(Calendar.DAY_OF_MONTH, date - 20);
        datePickerDialog.getDatePicker().setMinDate(min_cal.getTimeInMillis());
        Dialog dialog = datePickerDialog;
        dialog.show();
    }

    private void collection_print_on_analogics() {
        GetSetValues details = transaction_details_list.get(position);
        int maxline = 38;
        StringBuilder stringBuilder = new StringBuilder();
        analogicsprint(functionCalls.line(maxline), 6);
        analogics_double_print(functionCalls.aligncenter(PRINTER_HEADER, maxline), 6);
        analogicsprint(functionCalls.line(maxline), 6);
        if (officecopy) {
            officecopy = false;
            analogics_double_print(functionCalls.aligncenter("HESCOM COPY ", maxline), 6);
        } else analogics_double_print(functionCalls.aligncenter("CUSTOMER COPY ", maxline), 6);
        analogics_double_print(functionCalls.aligncenter("CASH RECEIPT (RAPDRP-MCC) ", maxline), 6);
        analogicsprint(functionCalls.line(maxline), 6);
        analogicsprint(functionCalls.space("  SUB Division", 19)+functionCalls.space(":",2)+sPref.getString(sPref_SUBDIVCODE, ""), 6);
        analogicsprint(functionCalls.space("  RAPDRP MCC TR No.",19)+functionCalls.space(":",2), 6);
        if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_ROLE, ""), "MR"))
            analogicsprint("  "+"000000"+"RAPDRPMCC"+sPref.getString(sPref_MRCODE, "")+pay_today_date, 6);
        else if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_ROLE, ""), "AAO"))
            analogicsprint("  "+"00000"+"RAPDRPMCC"+sPref.getString(sPref_MRCODE, "").substring(2)+ "AAO"+pay_today_date, 6);
        else if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_ROLE, ""), "AEE"))
            analogicsprint("  "+"00000"+"RAPDRPMCC"+sPref.getString(sPref_MRCODE, "").substring(2)+ "AEE"+pay_today_date, 6);
        analogicsprint(functionCalls.space("  Customer Name ",19) +functionCalls.space(":",2)+ details.getColl_grp_customer_name(), 6);
        analogicsprint(functionCalls.space("  RRNo.", 19) + functionCalls.space(":", 2) + details.getRrno(), 6);
        analogics_double_print(functionCalls.space("  Account ID", 19) + functionCalls.space(":", 2) + details.getCustid(), 6);
        analogicsprint(functionCalls.space("  Receipt No.", 19) + functionCalls.space(":", 2) + getSetValues.getColl_posting_receipt_no(), 6);
        if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_COLLECTION_TYPE, ""), "1")) {
            if (coll_non_rev_mode.isChecked()) {
                analogicsprint(functionCalls.space("  Receipt Type", 19) + functionCalls.space(":", 2) + "Non Revenue", 6);
                analogicsprint(functionCalls.space("  Mis Type", 19) + functionCalls.space(":", 2) + coll_non_rev_mis_type, 6);
            } else analogicsprint(functionCalls.space("  Receipt Type", 19) + functionCalls.space(":", 2) + "Revenue", 6);
        } else analogicsprint(functionCalls.space("  Receipt Type", 19) + functionCalls.space(":", 2) + "Revenue", 6);
        switch (coll_mode) {
            case "0":
                analogicsprint(functionCalls.space("  Payment Mode", 19) + functionCalls.space(":", 2) + "CASH", 6);
                break;

            case "1":
                analogicsprint(functionCalls.space("  Payment Mode", 19) + functionCalls.space(":", 2) + "CHEQUE", 6);
                break;

            case "2":
                analogicsprint(functionCalls.space("  Payment Mode", 19) + functionCalls.space(":", 2) + "DEMAND DRAFT", 6);
                break;
        }
        analogicsprint(functionCalls.space("  Receipt Date", 19) + functionCalls.space(":", 2) + coll_receipt_time, 6);
        switch (coll_mode) {
            case "1":
                analogicsprint(functionCalls.space("  Cheque Number",19) + functionCalls.space(":", 2) + coll_cheque_dd_number, 6);
                analogicsprint(functionCalls.space("  Cheque MICR",19) + functionCalls.space(":", 2) + coll_cheque_micr, 6);
                analogicsprint(functionCalls.space("  Cheque Bank",19) + functionCalls.space(":", 2) + coll_chq_dd_bank, 6);
                analogicsprint(functionCalls.space("  Cheque Date",19) + functionCalls.space(":", 2) + coll_chq_dd_date, 6);
                break;

            case "2":
                analogicsprint(functionCalls.space("  DD Number",19) + functionCalls.space(":", 2) + coll_cheque_dd_number, 6);
                analogicsprint(functionCalls.space("  DD Bank",19) + functionCalls.space(":", 2) + coll_chq_dd_bank, 6);
                analogicsprint(functionCalls.space("  DD Date",19) + functionCalls.space(":", 2) + coll_chq_dd_date, 6);
                break;
        }
        analogicsprint(functionCalls.space("  Meter Reader Name",19) + functionCalls.space(":", 2) + sPref.getString(sPref_MRNAME, ""), 6);
        analogicsprint(functionCalls.space("  Meter Reader Code",19) + functionCalls.space(":", 2) + coll_mr_code, 6);
        analogics_double_print(functionCalls.space("  Amount Paid Rs", 19) + functionCalls.space(":", 2) + ""+details.getAmount()+" /-", 6);
        analogicsprint(details.getPay_amount_in_words_1(), 6);
        if (amt_words_list.size() > 2) {
            analogicsprint(details.getPay_amount_in_words_2(), 6);
            analogicsprint(details.getPay_amount_in_words_3(), 6);
        } else analogicsprint(details.getPay_amount_in_words_2(), 6);
        stringBuilder.setLength(0);
        stringBuilder.append("\n");
        analogicsprint(stringBuilder.toString(), 6);
        print_bar_code(details.getCustid());
        stringBuilder.setLength(0);
        stringBuilder.append("\n");
        analogicsprint(stringBuilder.toString(), 6);
        analogicsprint(functionCalls.aligncenter(coll_device_id + coll_mr_code, maxline), 6);
        analogicsprint(functionCalls.space("  Reference Id", 19) + functionCalls.space(":", 2) + getSetValues.getPosting_unique_id(), 6);
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

    private void print_bar_code(String msg) {
        String feeddata;
        feeddata = api.barcode_Code_128_Alpha_Numerics_VIP(msg);
        conn.printData(feeddata);
    }

    private void collection_print_on_NGX() {
        GetSetValues details = transaction_details_list.get(position);
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

        stringBuilder.setLength(0);
        if (officecopy) {
            officecopy = false;
            stringBuilder.append("HESCOM COPY"+"\n");
        } else stringBuilder.append("CUSTOMER COPY"+"\n");
        stringBuilder.append("CASH RECEIPT (RAPDRP-MCC)");
        tp.setTextSize(35);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_CENTER, tp);

        stringBuilder.setLength(0);
        stringBuilder.append(functionCalls.line(maxline));
        tp.setTextSize(25);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_NORMAL, tp);

        stringBuilder.setLength(0);
        stringBuilder.append(functionCalls.space("  SUB Division", 19)).append(functionCalls.space(":", 2)).append(sPref.getString(sPref_SUBDIVCODE, "")).append("\n");
        stringBuilder.append(functionCalls.space("  RAPDRP MCC TR No.", 19)).append(functionCalls.space(":", 2)).append("\n");
        if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_ROLE, ""), "MR"))
            stringBuilder.append("  " + "000000" + "RAPDRPMCC").append(sPref.getString(sPref_MRCODE, "")).append(pay_today_date).append("\n");
        else if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_ROLE, ""), "AAO"))
            stringBuilder.append("  " + "00000" + "RAPDRPMCC").append(sPref.getString(sPref_MRCODE, "").substring(2)).append("AAO").append(pay_today_date).append("\n");
        else if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_ROLE, ""), "AEE"))
            stringBuilder.append("  " + "00000" + "RAPDRPMCC").append(sPref.getString(sPref_MRCODE, "").substring(2)).append("AEE").append(pay_today_date).append("\n");
        stringBuilder.append(functionCalls.space("  Customer Name ", 19)).append(functionCalls.space(":", 2)).append(details.getColl_grp_customer_name()).append("\n");
        stringBuilder.append(functionCalls.space("  RRNo.", 19)).append(functionCalls.space(":", 2)).append(details.getRrno());
        tp.setTextSize(25);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_NORMAL, tp);

        stringBuilder.setLength(0);
        stringBuilder.append(functionCalls.space(" Account ID", 13)).append(functionCalls.space(":", 2)).append(details.getCustid());
        tp.setTextSize(35);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_NORMAL, tp);

        stringBuilder.setLength(0);
        stringBuilder.append(functionCalls.space("  Receipt No.", 19)).append(functionCalls.space(":", 2)).append(getSetValues.getColl_posting_receipt_no()).append("\n");
        if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_COLLECTION_TYPE, ""), "1")) {
            if (coll_non_rev_mode.isChecked()) {
                stringBuilder.append(functionCalls.space("  Receipt Type", 19)).append(functionCalls.space(":", 2)).append("Non Revenue").append("\n");
                stringBuilder.append(functionCalls.space("  Mis Type", 19)).append(functionCalls.space(":", 2)).append(coll_non_rev_mis_type).append("\n");
            } else stringBuilder.append(functionCalls.space("  Receipt Type", 19)).append(functionCalls.space(":", 2)).append("Revenue").append("\n");
        } else stringBuilder.append(functionCalls.space("  Receipt Type", 19)).append(functionCalls.space(":", 2)).append("Revenue").append("\n");
        switch (coll_mode) {
            case "0":
                stringBuilder.append(functionCalls.space("  Payment Mode", 19)).append(functionCalls.space(":", 2)).append("CASH").append("\n");
                break;

            case "1":
                stringBuilder.append(functionCalls.space("  Payment Mode", 19)).append(functionCalls.space(":", 2)).append("CHEQUE").append("\n");
                break;

            case "2":
                stringBuilder.append(functionCalls.space("  Payment Mode", 19)).append(functionCalls.space(":", 2)).append("DEMAND DRAFT").append("\n");
                break;
        }
        stringBuilder.append(functionCalls.space("  Receipt Date", 19)).append(functionCalls.space(":", 2)).append(coll_receipt_time).append("\n");
        switch (coll_mode) {
            case "1":
                stringBuilder.append(functionCalls.space("  Cheque Number", 19)).append(functionCalls.space(":", 2)).append(coll_cheque_dd_number).append("\n");
                stringBuilder.append(functionCalls.space("  Cheque MICR", 19)).append(functionCalls.space(":", 2)).append(coll_cheque_micr).append("\n");
                stringBuilder.append(functionCalls.space("  Cheque Bank", 19)).append(functionCalls.space(":", 2)).append(coll_chq_dd_bank).append("\n");
                stringBuilder.append(functionCalls.space("  Cheque Date", 19)).append(functionCalls.space(":", 2)).append(coll_chq_dd_date).append("\n");
                break;

            case "2":
                stringBuilder.append(functionCalls.space("  DD Number", 19)).append(functionCalls.space(":", 2)).append(coll_cheque_dd_number).append("\n");
                stringBuilder.append(functionCalls.space("  DD Bank", 19)).append(functionCalls.space(":", 2)).append(coll_chq_dd_bank).append("\n");
                stringBuilder.append(functionCalls.space("  DD Date", 19)).append(functionCalls.space(":", 2)).append(coll_chq_dd_date).append("\n");
                break;
        }
        stringBuilder.append(functionCalls.space("  Meter Reader Name", 19)).append(functionCalls.space(":", 2)).append(sPref.getString(sPref_MRNAME, "")).append("\n");
        stringBuilder.append(functionCalls.space("  Meter Reader Code", 19)).append(functionCalls.space(":", 2)).append(coll_mr_code);
        tp.setTextSize(25);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_NORMAL, tp);

        stringBuilder.setLength(0);
        stringBuilder.append(functionCalls.space(" Amount Paid Rs", 16)).append(functionCalls.space(":", 2)).append(details.getAmount()).append(" /-");
        tp.setTextSize(30);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_NORMAL, tp);

        stringBuilder.setLength(0);
        stringBuilder.append(functionCalls.space("  Amount in Words  :", 25)).append("\n");
        stringBuilder.append("  ").append(details.getPay_amount_in_words_1()).append("\n");
        if (amt_words_list.size() > 2) {
            stringBuilder.append("  ").append(details.getPay_amount_in_words_2()).append("\n");
            stringBuilder.append("  ").append(details.getPay_amount_in_words_3());
        } else stringBuilder.append(details.getPay_amount_in_words_2());
        tp.setTextSize(25);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_NORMAL, tp);

        stringBuilder.setLength(0);
        mBtp.print();
        mBtp.printBarcode(details.getCustid(), NGXBarcodeCommands.CODE128, 45, 400);
        stringBuilder.setLength(0);
        stringBuilder.append("\n");
        stringBuilder.append(coll_device_id).append(coll_mr_code);
        tp.setTextSize(25);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_CENTER, tp);

        stringBuilder.setLength(0);
        stringBuilder.append(functionCalls.space("  Reference Id", 17)).append(functionCalls.space(":", 2)).append("\n");
        stringBuilder.append("  ").append(getSetValues.getPosting_unique_id()).append("\n");
        stringBuilder.append(functionCalls.line(maxline));
        tp.setTextSize(25);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_NORMAL, tp);
        mBtp.print();
        mBtp.printUnicodeText("\n\n\n\n");
    }

    private class TaskPrint implements Runnable {
        Pos pos;

        private TaskPrint(Pos pos) {
            this.pos = pos;
        }

        public void run() {
            final boolean bPrintResult = PrintTicket();
            final boolean bIsOpened = pos.GetIO().IsOpened();
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    functionCalls.showToast(getActivity(), bPrintResult ? getActivity().getResources().getString(R.string.print_success) : getActivity().getResources().getString(R.string.print_failed));
                    if (bIsOpened) {
                        progressDialog.dismiss();
                        if (officecopy) {
                            officecopy = false;
                            handler.sendEmptyMessage(COLLECTION_PRINTING_COMPLETED);
                        } else showdialog(DLG_PRINT_OFFICE);
                    }
                }
            });
        }

        private boolean PrintTicket() {
            GetSetValues details = transaction_details_list.get(position);
            pos.POS_S_Align(1);
            printText(functionCalls.line(47));
            printdoubleText(PRINTER_HEADER);
            printText(functionCalls.line(47));
            if (officecopy)
                printdoubleText("HESCOM COPY");
            else printdoubleText("CUSTOMER COPY");
            printdoubleText("CASH RECEIPT (RAPDRP-MCC)");
            printText(functionCalls.line(47));
            pos.POS_S_Align(0);
            printText(functionCalls.space("  SUB Division", 19)+functionCalls.space(":",2)+sPref.getString(sPref_SUBDIVCODE, ""));
            printText(functionCalls.space("  RAPDRP MCC TR No.",19)+functionCalls.space(":",2));
            if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_ROLE, ""), "MR"))
                printText("  "+"000000"+"RAPDRPMCC"+sPref.getString(sPref_MRCODE, "")+pay_today_date);
            else if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_ROLE, ""), "AAO"))
                printText("  "+"00000"+"RAPDRPMCC"+sPref.getString(sPref_MRCODE, "").substring(2)+ "AAO"+pay_today_date);
            else if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_ROLE, ""), "AEE"))
                printText("  "+"00000"+"RAPDRPMCC"+sPref.getString(sPref_MRCODE, "").substring(2)+ "AEE"+pay_today_date);
            printText(functionCalls.space("  Customer Name ",19) +functionCalls.space(":",2)+ details.getColl_grp_customer_name());
            printText(functionCalls.space("  RRNo.", 19) + functionCalls.space(":", 2) + details.getRrno());
            printdoubleText(functionCalls.space("  Account ID", 19) + functionCalls.space(":", 2) + details.getCustid());
            printText(functionCalls.space("  Receipt No.", 19) + functionCalls.space(":", 2) + getSetValues.getColl_posting_receipt_no());
            if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_COLLECTION_TYPE, ""), "1")) {
                if (coll_non_rev_mode.isChecked()) {
                    printText(functionCalls.space("  Receipt Type", 19) + functionCalls.space(":", 2) + "Non Revenue");
                    printText(functionCalls.space("  Mis Type", 19) + functionCalls.space(":", 2) + coll_non_rev_mis_type);
                } else printText(functionCalls.space("  Receipt Type", 19) + functionCalls.space(":", 2) + "Revenue");
            } else printText(functionCalls.space("  Receipt Type", 19) + functionCalls.space(":", 2) + "Revenue");
            switch (coll_mode) {
                case "0":
                    printText(functionCalls.space("  Payment Mode", 19) + functionCalls.space(":", 2) + "CASH");
                    break;

                case "1":
                    printText(functionCalls.space("  Payment Mode", 19) + functionCalls.space(":", 2) + "CHEQUE");
                    break;

                case "2":
                    printText(functionCalls.space("  Payment Mode", 19) + functionCalls.space(":", 2) + "DEMAND DRAFT");
                    break;
            }
            printText(functionCalls.space("  Receipt Date", 19) + functionCalls.space(":", 2) + coll_receipt_time);
            switch (coll_mode) {
                case "1":
                    printText(functionCalls.space("  Cheque Number",19) + functionCalls.space(":", 2) + coll_cheque_dd_number);
                    printText(functionCalls.space("  Cheque MICR",19) + functionCalls.space(":", 2) + coll_cheque_micr);
                    printText(functionCalls.space("  Cheque Bank",19) + functionCalls.space(":", 2) + coll_chq_dd_bank);
                    printText(functionCalls.space("  Cheque Date",19) + functionCalls.space(":", 2) + coll_chq_dd_date);
                    break;

                case "2":
                    printText(functionCalls.space("  DD Number",19) + functionCalls.space(":", 2) + coll_cheque_dd_number);
                    printText(functionCalls.space("  DD Bank",19) + functionCalls.space(":", 2) + coll_chq_dd_bank);
                    printText(functionCalls.space("  DD Date",19) + functionCalls.space(":", 2) + coll_chq_dd_date);
                    break;
            }
            printText(functionCalls.space("  Meter Reader Name",19) + functionCalls.space(":", 2) + sPref.getString(sPref_MRNAME, ""));
            printText(functionCalls.space("  Meter Reader Code",19) + functionCalls.space(":", 2) + coll_mr_code);
            printdoubleText(functionCalls.space("  Amount Paid    Rs", 19) + functionCalls.space(":", 2) + ""+details.getAmount()+" /-");
            printText(details.getPay_amount_in_words_1());
            printText(details.getPay_amount_in_words_2());
            if (amt_words_list.size() == 3) {
                printText(details.getPay_amount_in_words_3());
            }
            pos.POS_S_SetBarcode(details.getCustid(),1,72,3,60,0,0);
            pos.POS_FeedLine();
            pos.POS_S_Align(1);
            printText(functionCalls.space(" ", 10) + coll_device_id + coll_mr_code);
            pos.POS_S_Align(0);
            printText(functionCalls.space("  Reference Id", 16) + functionCalls.space(":", 2) + getSetValues.getPosting_unique_id());
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}
