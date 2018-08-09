package com.transvision.trmcollection.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.analogics.thermalAPI.Bluetooth_Printer_3inch_prof_ThermalAPI;
import com.analogics.thermalprinter.AnalogicsThermalPrinter;
import com.lvrenyang.io.Pos;
import com.ngx.BluetoothPrinter;
import com.ngx.Enums.NGXBarcodeCommands;
import com.ngx.PrinterWidth;
import com.transvision.trmcollection.MainActivity;
import com.transvision.trmcollection.R;
import com.transvision.trmcollection.adapters.Details_adapter;
import com.transvision.trmcollection.bluetooth.BluetoothService;
import com.transvision.trmcollection.database.TRMCollection_Database;
import com.transvision.trmcollection.posting.SendingData;
import com.transvision.trmcollection.posting.SendingData.Collection_Details;
import com.transvision.trmcollection.posting.SendingData.Posting_Collection_data;
import com.transvision.trmcollection.values.ClassGPS;
import com.transvision.trmcollection.values.FunctionCalls;
import com.transvision.trmcollection.values.GetSetValues;
import com.transvision.trmcollection.values.NumberToWords;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import static com.transvision.trmcollection.values.Constants.COLLECTION_TRANSACTION_LIST_EMPTY;
import static com.transvision.trmcollection.values.Constants.PRINTER_HEADER;
import static com.transvision.trmcollection.values.Constants.sPref_COLLECTION_COLLECTED;
import static com.transvision.trmcollection.values.Constants.sPref_COLLECTION_DATE;
import static com.transvision.trmcollection.values.Constants.sPref_COLLECTION_LIMIT;
import static com.transvision.trmcollection.values.Constants.sPref_END_TIME;
import static com.transvision.trmcollection.values.Constants.sPref_MRCODE;
import static com.transvision.trmcollection.values.Constants.sPref_MRNAME;
import static com.transvision.trmcollection.values.Constants.sPref_ROLE;
import static com.transvision.trmcollection.values.Constants.sPref_START_TIME;
import static com.transvision.trmcollection.values.Constants.sPref_SUBDIVCODE;

public class Collection extends Fragment {
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

    View view;
    TextInputLayout til_acc_id;
    EditText et_acc_id, et_paid_amount;
    TextView tv_customer_name, tv_customer_rrno, tv_customer_acc_id, tv_customer_tariff, tv_customer_bill_amount;
    Button bt_add_transaction;
    RecyclerView transaction_details_view;
    ArrayList<GetSetValues> transaction_details_list;
    ArrayList<String> amt_words_list;
    LinearLayout customer_details_layout, details_table_layout;
    BottomNavigationView print_navigation_view;
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

    BluetoothPrinter mBtp;
    AnalogicsThermalPrinter conn = MainActivity.conn;
    Bluetooth_Printer_3inch_prof_ThermalAPI api;
    Pos mPos = BluetoothService.mPos;
    ExecutorService es = BluetoothService.es;
    DecimalFormat num;

    double bill_amount=0, collected_amount=0, max_collection=0;
    String paid_amount="", coll_pre_receipt_no="", coll_device_id="", coll_mode="0", coll_receipt_time="", coll_gpslong="", coll_gpslat="",
            numinwords="", numinwords1="", coll_mr_code="", amount_paid="", coll_printer="";
    boolean officecopy= false;
    double paying_amount=0;
    String pay_today_date = new SimpleDateFormat("ddMMyyyy", Locale.US).format(new Date());

    public Collection() {
        // Required empty public constructor
    }

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

                case COLLECTION_POSTING_SUCCESS:
                    progressDialog.dismiss();
                    insertCollection_output();
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
                    clear_collection_screen();
                    break;

                case COLLECTION_TRANSACTION_LIST_EMPTY:
                    et_paid_amount.setEnabled(true);
                    et_paid_amount.setText("");
                    details_table_layout.setVisibility(View.GONE);
                    print_navigation_view.setVisibility(View.GONE);
                    break;
            }
            return false;
        }
    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_collection, container, false);

        initialize();

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

        bt_add_transaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((MainActivity) getActivity()).getInternetConnection().isConnectingToInternet())
                    add_transaction();
                else showdialog(DLG_INTERNET_CONNECTION);
            }
        });

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

        return view;
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
        print_navigation_view = view.findViewById(R.id.bottom_navigation);
        print_navigation_view.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        til_acc_id = view.findViewById(R.id.til_customer_account_id);
        et_acc_id = view.findViewById(R.id.et_customer_account_id);
        et_paid_amount = view.findViewById(R.id.et_paid_amount);

        tv_customer_name = view.findViewById(R.id.customer_name);
        tv_customer_rrno = view.findViewById(R.id.customer_rrno);
        tv_customer_acc_id = view.findViewById(R.id.customer_account_id);
        tv_customer_tariff = view.findViewById(R.id.customer_tariff);
        tv_customer_bill_amount = view.findViewById(R.id.customer_bill_amount);

        bt_add_transaction = view.findViewById(R.id.btn_add_transaction);

        customer_details_layout = view.findViewById(R.id.customer_details_layout);
        details_table_layout = view.findViewById(R.id.details_list_layout);

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

        transaction_details_view = view.findViewById(R.id.details_list_view);
        transaction_details_list = new ArrayList<>();
        detailsAdapter = new Details_adapter(transaction_details_list, getSetValues, handler, editor, sPref,
                collectionDatabase, functionCalls.receipt_date());
        transaction_details_view.setHasFixedSize(true);
        transaction_details_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        transaction_details_view.setAdapter(detailsAdapter);

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
    }

    private void add_transaction() {
        if (functionCalls.compare_Times(sPref.getString(sPref_COLLECTION_DATE, "")+" "+sPref.getString(sPref_START_TIME, ""),
                sPref.getString(sPref_COLLECTION_DATE, "")+" "+sPref.getString(sPref_END_TIME, ""))) {
            if (printerconnected) {
                paid_amount = et_paid_amount.getText().toString();
                if (!TextUtils.isEmpty(paid_amount)) {
                    paying_amount = Double.parseDouble(paid_amount);
                    if (paying_amount > 0) {
                        if (paying_amount <= 10000) {
                            if (paying_amount >= bill_amount) {
                                collected_amount = Double.parseDouble(sPref.getString(sPref_COLLECTION_COLLECTED, "")) + paying_amount;
                                functionCalls.logStatus("Collection Collected: "+collected_amount);
                                if (collected_amount <= max_collection) {
                                    if (collectionDatabase.check_receipt_time(functionCalls.receipt_date_time())) {
                                        add_transaction_details();
                                    } else showdialog(DLG_SYSTEM_TIME_NOT_MATCHING);
                                } else showdialog(DLG_COLLECTION_DAY_LIMIT);
                            } else showdialog(DLG_COLLECTION_PAY_MORE_BILL);
                        } else showdialog(DLG_COLLECTION_CASH_LIMIT);
                    } else showdialog(DLG_COLLECTION_PAY_VALID);
                } else showdialog(DLG_COLLECTION_PAY);
            } else showdialog(DLG_PRINT_CONNECTION);
        } else showdialog(DLG_COLLECTION_TIME);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_print:
                    if (printerconnected) {
                        if (((MainActivity) getActivity()).getInternetConnection().isConnectingToInternet()) {
                            GPSLocation();
                            functionCalls.showprogressdialog(getActivity().getResources().getString(R.string.posting_title), getActivity().getResources().getString(R.string.posting_message), progressDialog);
                            Posting_Collection_data postingCollectionData = sendingData.new Posting_Collection_data(getSetValues, handler);
                            postingCollectionData.execute(coll_device_id, sPref.getString(sPref_MRCODE, "")+COLLECTION_KEY,
                                    getSetValues.getCustomer_rrno(), functionCalls.receipt_date(), paid_amount, coll_mode,
                                    getSetValues.getCustomer_accid(), getSetValues.getCustomer_name(), "0", "0", "0", "");
                        } else showdialog(DLG_INTERNET_CONNECTION);
                    } else showdialog(DLG_PRINT_CONNECTION);
                    break;

                case R.id.navigation_cancel:
                    break;
            }
            return false;
        }
    };

    private void add_transaction_details() {
        et_paid_amount.setEnabled(false);
        details_table_layout.setVisibility(View.VISIBLE);
        print_navigation_view.setVisibility(View.VISIBLE);
        getSet = new GetSetValues();
        getSet.setRrno(getSetValues.getCustomer_rrno());
        getSet.setCustid(getSetValues.getCustomer_accid());
        getSet.setAmount(Double.parseDouble(paid_amount));
        transaction_details_list.add(getSet);
        detailsAdapter.notifyDataSetChanged();
        amount_paid = num.format(Double.parseDouble(paid_amount));
        numinwords1 = ntw.convert(Integer.parseInt(paid_amount));
        numinwords = numinwords1.substring(0, 1).toUpperCase() + numinwords1.substring(1);
    }

    @SuppressLint("SetTextI18n")
    private void view_Customer_Details() {
        til_acc_id.setVisibility(View.GONE);
        et_acc_id.setText("");
        et_paid_amount.setText("");
        et_paid_amount.setEnabled(true);
        et_paid_amount.requestFocus();
        customer_details_layout.setVisibility(View.VISIBLE);
        tv_customer_name.setText(getSetValues.getCustomer_name());
        tv_customer_rrno.setText(getSetValues.getCustomer_rrno());
        tv_customer_acc_id.setText(getSetValues.getCustomer_accid());
        tv_customer_tariff.setText(getSetValues.getCustomer_tariff());
        tv_customer_bill_amount.setText(getActivity().getResources().getString(R.string.rupee)+" "+getSetValues.getCustomer_bill_amount()+" /-");
        bill_amount = Double.parseDouble(getSetValues.getCustomer_bill_amount());
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
        String transaction_id = "cashid" + "_" + sPref.getString(sPref_ROLE, "") + "_" + transactiondate + "_" + "" + nf.format(trans_id);
        data.close();
        return transaction_id;
    }

    public void GPSLocation() {
        if (classGPS.canGetLocation()) {
            double latitude = classGPS.getLatitude();
            double longitude = classGPS.getLongitude();
            coll_gpslong = "" + longitude;
            coll_gpslat = "" + latitude;
        }
    }

    private void insertCollection_output() {
        coll_receipt_time = functionCalls.receipt_date_time();
        ContentValues cv = new ContentValues();
        cv.put("RRNO", getSetValues.getCustomer_rrno());
        cv.put("ACCOUNT_ID", getSetValues.getCustomer_accid());
        cv.put("LF_NO", getSetValues.getCustomer_LF_no());
        cv.put("NAME", getSetValues.getCustomer_name());
        cv.put("AMOUNT", amount_paid);
        cv.put("MODE_PAYMENT", coll_mode);
        cv.put("RECEIPT_NO", getSetValues.getColl_posting_receipt_no());
        cv.put("TRANSACTION_ID", transactionid(functionCalls.receipt_date()));
        cv.put("MACHINE_ID", coll_device_id);
        cv.put("RECEIPT_DATE_TIME", coll_receipt_time);
        cv.put("RECEIPT_DATE", functionCalls.receipt_date());
        cv.put("MR_CODE", sPref.getString(sPref_MRCODE, ""));
        cv.put("GPS_LAT", "");
        cv.put("GPS_LONG", "");
        cv.put("UNIQUE_ID", getSetValues.getPosting_unique_id());
        cv.put("ROLE", sPref.getString(sPref_ROLE, ""));
        collectionDatabase.insert_collection_details(cv);
        showdialog(DLG_PRINT_START);
    }

    private void clear_collection_screen() {
        transaction_details_list.clear();
        detailsAdapter.notifyDataSetChanged();
        print_navigation_view.setVisibility(View.GONE);
        details_table_layout.setVisibility(View.GONE);
        customer_details_layout.setVisibility(View.GONE);
        til_acc_id.setVisibility(View.VISIBLE);
        et_paid_amount.setText("");
        et_paid_amount.setEnabled(true);
        amt_words_list.clear();
        et_acc_id.requestFocus();
    }

    private void showdialog(int id) {
        AlertDialog alertDialog;
        switch (id) {
            case DLG_PRINT_START:
                AlertDialog.Builder print_start = new AlertDialog.Builder(getActivity());
                print_start.setTitle("Collection Print");
                print_start.setCancelable(false);
                print_start.setMessage(getActivity().getResources().getString(R.string.ask_print));
                print_start.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        functionCalls.showprogressdialog("Printing", getActivity().getResources().getString(R.string.printing), progressDialog);
                        amt_words_list.clear();
                        switch (coll_printer) {
                            case "NGX":
                                functionCalls.splitString("Rs. "+numinwords + " only", 36, amt_words_list);
                                if (amt_words_list.size() == 1) {
                                    getSetValues.setPay_amount_in_words_1(amt_words_list.get(0));
                                    getSetValues.setPay_amount_in_words_2(" ");
                                    getSetValues.setPay_amount_in_words_3(" ");
                                } else {
                                    if (amt_words_list.size() == 2) {
                                        getSetValues.setPay_amount_in_words_1(amt_words_list.get(0));
                                        getSetValues.setPay_amount_in_words_2(amt_words_list.get(1));
                                        getSetValues.setPay_amount_in_words_3(" ");
                                    } else {
                                        if (amt_words_list.size() == 3) {
                                            getSetValues.setPay_amount_in_words_1(amt_words_list.get(0));
                                            getSetValues.setPay_amount_in_words_2(amt_words_list.get(1));
                                            getSetValues.setPay_amount_in_words_3(amt_words_list.get(2));
                                        }
                                    }
                                }
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
                                functionCalls.splitString("Amount in Words  : Rs. " + numinwords + " only", 36, amt_words_list);
                                if (amt_words_list.size() == 1) {
                                    getSetValues.setPay_amount_in_words_1("  "+amt_words_list.get(0));
                                    getSetValues.setPay_amount_in_words_2(" ");
                                    getSetValues.setPay_amount_in_words_3(" ");
                                } else {
                                    if (amt_words_list.size() == 2) {
                                        getSetValues.setPay_amount_in_words_1("  "+amt_words_list.get(0));
                                        getSetValues.setPay_amount_in_words_2("  "+amt_words_list.get(1));
                                        getSetValues.setPay_amount_in_words_3(" ");
                                    } else {
                                        if (amt_words_list.size() == 3) {
                                            getSetValues.setPay_amount_in_words_1("  "+amt_words_list.get(0));
                                            getSetValues.setPay_amount_in_words_2("  "+amt_words_list.get(1));
                                            getSetValues.setPay_amount_in_words_3("  "+amt_words_list.get(2));
                                        }
                                    }
                                }
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
                                functionCalls.splitString("Amount in Words  : Rs. " + numinwords + " only", 45, amt_words_list);
                                if (amt_words_list.size() == 1) {
                                    getSetValues.setPay_amount_in_words_1("  "+amt_words_list.get(0));
                                    getSetValues.setPay_amount_in_words_2(" ");
                                } else {
                                    if (amt_words_list.size() == 2) {
                                        getSetValues.setPay_amount_in_words_1("  "+amt_words_list.get(0));
                                        getSetValues.setPay_amount_in_words_2("  "+amt_words_list.get(1));
                                    } else {
                                        if (amt_words_list.size() == 3) {
                                            getSetValues.setPay_amount_in_words_1("  "+amt_words_list.get(0));
                                            getSetValues.setPay_amount_in_words_2("  "+amt_words_list.get(1));
                                            getSetValues.setPay_amount_in_words_3("  "+amt_words_list.get(2));
                                        }
                                    }
                                }
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
                office_print.setTitle("Collection Print");
                office_print.setCancelable(false);
                office_print.setMessage(getActivity().getResources().getString(R.string.office_payment_print));
                office_print.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        officecopy = true;
                        functionCalls.showprogressdialog("Printing", getActivity().getResources().getString(R.string.printing), progressDialog);
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
                office_print.setNeutralButton("REPRINT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        officecopy = false;
                        functionCalls.showprogressdialog("Printing", getActivity().getResources().getString(R.string.printing), progressDialog);
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
        }
    }

    private void collection_print_on_analogics() {
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
        analogicsprint(functionCalls.space("  Customer Name ",19) +functionCalls.space(":",2)+ getSetValues.getCustomer_name(), 6);
        analogicsprint(functionCalls.space("  RRNo.", 19) + functionCalls.space(":", 2) + getSetValues.getCustomer_rrno(), 6);
        analogics_double_print(functionCalls.space("  Account ID", 19) + functionCalls.space(":", 2) + getSetValues.getCustomer_accid(), 6);
        analogicsprint(functionCalls.space("  Receipt No.", 19) + functionCalls.space(":", 2) + getSetValues.getColl_posting_receipt_no(), 6);
        analogicsprint(functionCalls.space("  Receipt Type", 19) + functionCalls.space(":", 2) + "Revenue", 6);
        analogicsprint(functionCalls.space("  Payment Mode", 19) + functionCalls.space(":", 2) + "CASH", 6);
        analogicsprint(functionCalls.space("  Receipt Date", 19) + functionCalls.space(":", 2) + coll_receipt_time, 6);
        analogicsprint(functionCalls.space("  Meter Reader Name",19) + functionCalls.space(":", 2) + sPref.getString(sPref_MRNAME, ""), 6);
        analogicsprint(functionCalls.space("  Meter Reader Code",19) + functionCalls.space(":", 2) + coll_mr_code, 6);
        analogics_double_print(functionCalls.space("  Amount Paid Rs", 19) + functionCalls.space(":", 2) + amount_paid+" /-", 6);
        analogicsprint(getSetValues.getPay_amount_in_words_1(), 6);
        if (amt_words_list.size() > 2) {
            analogicsprint(getSetValues.getPay_amount_in_words_2(), 6);
            analogicsprint(getSetValues.getPay_amount_in_words_3(), 6);
        } else analogicsprint(getSetValues.getPay_amount_in_words_2(), 6);
        stringBuilder.setLength(0);
        stringBuilder.append("\n");
        analogicsprint(stringBuilder.toString(), 6);
        print_bar_code(getSetValues.getCustomer_accid());
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
        conn.printData(api.barcode_Code_128_Alpha_Numerics_VIP(msg));
    }

    private void collection_print_on_NGX() {
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
        stringBuilder.append(functionCalls.space("  SUB Division", 19)+functionCalls.space(":",2)+sPref.getString(sPref_SUBDIVCODE, "")+"\n");
        stringBuilder.append(functionCalls.space("  RAPDRP MCC TR No.",19)+functionCalls.space(":",2)+"\n");
        if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_ROLE, ""), "MR"))
            stringBuilder.append("  "+"000000"+"RAPDRPMCC"+sPref.getString(sPref_MRCODE, "")+pay_today_date+"\n");
        else if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_ROLE, ""), "AAO"))
            stringBuilder.append("  "+"00000"+"RAPDRPMCC"+sPref.getString(sPref_MRCODE, "").substring(2)+ "AAO"+pay_today_date+"\n");
        else if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_ROLE, ""), "AEE"))
            stringBuilder.append("  "+"00000"+"RAPDRPMCC"+sPref.getString(sPref_MRCODE, "").substring(2)+ "AEE"+pay_today_date+"\n");
        stringBuilder.append(functionCalls.space("  Customer Name ",19) +functionCalls.space(":",2)+ getSetValues.getCustomer_name()+"\n");
        stringBuilder.append(functionCalls.space("  RRNo.", 19) + functionCalls.space(":", 2) + getSetValues.getCustomer_rrno());
        tp.setTextSize(25);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_NORMAL, tp);

        stringBuilder.setLength(0);
        stringBuilder.append(functionCalls.space(" Account ID", 13) + functionCalls.space(":", 2) + getSetValues.getCustomer_accid());
        tp.setTextSize(35);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_NORMAL, tp);

        stringBuilder.setLength(0);
        stringBuilder.append(functionCalls.space("  Receipt No.", 19) + functionCalls.space(":", 2) + getSetValues.getColl_posting_receipt_no()+"\n");
        stringBuilder.append(functionCalls.space("  Receipt Type", 19) + functionCalls.space(":", 2) + "Revenue"+"\n");
        stringBuilder.append(functionCalls.space("  Payment Mode", 19) + functionCalls.space(":", 2) + "CASH"+"\n");
        stringBuilder.append(functionCalls.space("  Receipt Date", 19) + functionCalls.space(":", 2) + coll_receipt_time+"\n");
        stringBuilder.append(functionCalls.space("  Meter Reader Name",19) + functionCalls.space(":", 2) + sPref.getString(sPref_MRNAME, "")+"\n");
        stringBuilder.append(functionCalls.space("  Meter Reader Code",19) + functionCalls.space(":", 2) + coll_mr_code);
        tp.setTextSize(25);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_NORMAL, tp);

        stringBuilder.setLength(0);
        stringBuilder.append(functionCalls.space(" Amount Paid Rs", 16) + functionCalls.space(":", 2) + amount_paid+" /-");
        tp.setTextSize(30);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_NORMAL, tp);

        stringBuilder.setLength(0);
        stringBuilder.append(functionCalls.space("  Amount in Words  :",25)+"\n");
        stringBuilder.append("  "+getSetValues.getPay_amount_in_words_1()+"\n");
        if (amt_words_list.size() > 2) {
            stringBuilder.append("  "+getSetValues.getPay_amount_in_words_2()+"\n");
            stringBuilder.append("  "+getSetValues.getPay_amount_in_words_3());
        } else stringBuilder.append(getSetValues.getPay_amount_in_words_2());
        tp.setTextSize(25);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_NORMAL, tp);

        stringBuilder.setLength(0);
        mBtp.print();
        mBtp.printBarcode(getSetValues.getCustomer_accid(), NGXBarcodeCommands.CODE128, 45, 400);
        stringBuilder.setLength(0);
        stringBuilder.append("\n");
        stringBuilder.append(coll_device_id + coll_mr_code);
        tp.setTextSize(25);
        tp.setTypeface(tf);
        mBtp.addText(stringBuilder.toString(), ALIGN_CENTER, tp);

        stringBuilder.setLength(0);
        stringBuilder.append(functionCalls.space("  Reference Id", 17) + functionCalls.space(":", 2)+"\n");
        stringBuilder.append("  "+getSetValues.getPosting_unique_id()+"\n");
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
            printText(functionCalls.space("  Customer Name ",19) +functionCalls.space(":",2)+ getSetValues.getCustomer_name());
            printText(functionCalls.space("  RRNo.", 19) + functionCalls.space(":", 2) + getSetValues.getCustomer_rrno());
            printdoubleText(functionCalls.space("  Account ID", 19) + functionCalls.space(":", 2) + getSetValues.getCustomer_accid());
            printText(functionCalls.space("  Receipt No.", 19) + functionCalls.space(":", 2) + getSetValues.getColl_posting_receipt_no());
            printText(functionCalls.space("  Receipt Type", 19) + functionCalls.space(":", 2) + "Revenue");
            printText(functionCalls.space("  Payment Mode", 19) + functionCalls.space(":", 2) + "CASH");
            printText(functionCalls.space("  Receipt Date", 19) + functionCalls.space(":", 2) + coll_receipt_time);
            printText(functionCalls.space("  Meter Reader Name",19) + functionCalls.space(":", 2) + sPref.getString(sPref_MRNAME, ""));
            printText(functionCalls.space("  Meter Reader Code",19) + functionCalls.space(":", 2) + coll_mr_code);
            printdoubleText(functionCalls.space("  Amount Paid    Rs", 19) + functionCalls.space(":", 2) + amount_paid+" /-");
            printText(getSetValues.getPay_amount_in_words_1());
            printText(getSetValues.getPay_amount_in_words_2());
            if (amt_words_list.size() == 3) {
                printText(getSetValues.getPay_amount_in_words_3());
            }
            pos.POS_S_SetBarcode(getSetValues.getCustomer_accid(),0,72,3,60,0,0);
            pos.POS_FeedLine();
            pos.POS_S_Align(1);
            printText(functionCalls.space(" ", 10) + coll_device_id + coll_mr_code);
            pos.POS_S_Align(0);
            printText(functionCalls.space("  Reference Id", 19) + functionCalls.space(":", 2) + getSetValues.getPosting_unique_id());
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
