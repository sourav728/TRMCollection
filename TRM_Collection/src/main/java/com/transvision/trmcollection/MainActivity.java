package com.transvision.trmcollection;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.analogics.thermalprinter.AnalogicsThermalPrinter;
import com.transvision.trmcollection.bluetooth.BluetoothService;
import com.transvision.trmcollection.database.TRMCollection_Database;
import com.transvision.trmcollection.fragments.Collection;
import com.transvision.trmcollection.fragments.Collection_AAO_AEE;
import com.transvision.trmcollection.fragments.Collection_Reports;
import com.transvision.trmcollection.fragments.ScanAccountID;
import com.transvision.trmcollection.fragments.Settings;
import com.transvision.trmcollection.fragments.Upload_Collection;
import com.transvision.trmcollection.posting.MR_Tracking_service;
import com.transvision.trmcollection.posting.SendingData;
import com.transvision.trmcollection.values.CheckInternetConnection;
import com.transvision.trmcollection.values.ClassGPS;
import com.transvision.trmcollection.values.FunctionCalls;
import com.transvision.trmcollection.values.GetSetValues;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;

import static com.transvision.trmcollection.values.Constants.ANALOGICS_PRINTER_CONNECTED;
import static com.transvision.trmcollection.values.Constants.ANALOGICS_PRINTER_DISCONNECTED;
import static com.transvision.trmcollection.values.Constants.ANALOGICS_PRINTER_PAIRED;
import static com.transvision.trmcollection.values.Constants.BLUETOOTH_RESULT;
import static com.transvision.trmcollection.values.Constants.DISCONNECTED;
import static com.transvision.trmcollection.values.Constants.GETSET;
import static com.transvision.trmcollection.values.Constants.MR_TRACKING_UPDATE;
import static com.transvision.trmcollection.values.Constants.MR_TRACKING_UPDATE_FAIL;
import static com.transvision.trmcollection.values.Constants.NON_REVENUE_HEAD;
import static com.transvision.trmcollection.values.Constants.PREFS_NAME;
import static com.transvision.trmcollection.values.Constants.RESULT;
import static com.transvision.trmcollection.values.Constants.TURNED_OFF;
import static com.transvision.trmcollection.values.Constants.sPref_Collection_login;
import static com.transvision.trmcollection.values.Constants.sPref_DEVICE_ID;
import static com.transvision.trmcollection.values.Constants.sPref_END_TIME;
import static com.transvision.trmcollection.values.Constants.sPref_MRCODE;
import static com.transvision.trmcollection.values.Constants.sPref_MRNAME;
import static com.transvision.trmcollection.values.Constants.sPref_ROLE;
import static com.transvision.trmcollection.values.Constants.sPref_START_TIME;
import static com.transvision.trmcollection.values.Constants.sPref_SUBDIVCODE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int DLG_TRANSACTION_LIST_CLEAR_MSG = 1;

    GetSetValues getSetValues;
    private Fragment fragment;
    private Toolbar toolbar;
    SharedPreferences sPref;
    SharedPreferences.Editor editor;
    TRMCollection_Database collectionDatabase;
    FunctionCalls functionCalls;
    BluetoothAdapter device_adapter;
    BluetoothDevice bluetoothDevice;
    CheckInternetConnection internetConnection;
    ClassGPS gps;
    SendingData sendingData;
    ArrayList<GetSetValues> transaction_details_arraylist;

    TextView tv_name, tv_code;
    String collection_device_id ="", mr_gpslat="", mr_gpslong="";

    public static AnalogicsThermalPrinter conn;
    public static String printer_address = "", printer="";

    public enum Steps {
        FORM0(Collection.class),
        FORM1(Collection_Reports.class),
        FORM2(Upload_Collection.class),
        FORM3(Settings.class),
        FORM4(Collection_AAO_AEE.class),
        FORM5(ScanAccountID.class);

        private Class clazz;

        Steps(Class clazz) {
            this.clazz = clazz;
        }

        public Class getFragClass() {
            return clazz;
        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MR_TRACKING_UPDATE:
                    functionCalls.logStatus("MR Tracking Successfully...");
                    break;

                case MR_TRACKING_UPDATE_FAIL:
                    functionCalls.logStatus("MR Tracking Failed...");
                    break;

                case ANALOGICS_PRINTER_CONNECTED:
                    BluetoothService.printerconnected = true;
                    break;

                case ANALOGICS_PRINTER_DISCONNECTED:
                    BluetoothService.printerconnected = false;
                    handler.sendEmptyMessage(ANALOGICS_PRINTER_PAIRED);
                    break;

                case ANALOGICS_PRINTER_PAIRED:
                    try {
                        functionCalls.logStatus(bluetoothDevice.getAddress());
                        printer_address = bluetoothDevice.getAddress();
                        conn.openBT(bluetoothDevice.getAddress());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    break;
            }
            return false;
        }
    });

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        getSetValues = (GetSetValues) intent.getSerializableExtra(GETSET);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View view = navigationView.getHeaderView(0);
        tv_name = view.findViewById(R.id.user_name);
        tv_code = view.findViewById(R.id.user_mobile_no);
        NavigationView logout_navigationView = (NavigationView) findViewById(R.id.navigation_drawer_bottom);
        logout_navigationView.setNavigationItemSelectedListener(this);

        collectionDatabase = new TRMCollection_Database(this);
        collectionDatabase.open();

        transaction_details_arraylist = new ArrayList<>();
        gps = new ClassGPS(this);

        sendingData = new SendingData(this);

        sPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        editor = sPref.edit();
        editor.apply();

        collection_device_id = sPref.getString(sPref_DEVICE_ID, "");

        GPSlocation();

        functionCalls = new FunctionCalls();
        internetConnection = new CheckInternetConnection(this);
        device_adapter = BluetoothAdapter.getDefaultAdapter();
        device_adapter.enable();

        Cursor printer_data = collectionDatabase.getPrinter_details();
        if (printer_data.getCount() > 0) {
            printer_data.moveToNext();
            printer = printer_data.getString(printer_data.getColumnIndexOrThrow("PRINTER"));
            if (!printer.equals("ALG"))
                startservice(MainActivity.this);
            else {
                conn = new AnalogicsThermalPrinter();
                startBroadcast();
            }
        }

        tv_name.setText(sPref.getString(sPref_MRNAME, ""));
        tv_code.setText(sPref.getString(sPref_MRCODE, ""));

        registerReceiver(mReceiver, new IntentFilter(BLUETOOTH_RESULT));

        if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_ROLE, ""), "AEE")
                || StringUtils.startsWithIgnoreCase(sPref.getString(sPref_ROLE, ""), "AAO")) {
            switchContent(Steps.FORM4, getResources().getString(R.string.collection));
        } else switchContent(Steps.FORM0, getResources().getString(R.string.collection));

        if (getSetValues.getCollection_login().equals("Yes")) {
            SendingData.MR_Track mrTrack = sendingData.new MR_Track(handler);
            mrTrack.execute(sPref.getString(sPref_MRCODE, ""), sPref.getString(sPref_DEVICE_ID, ""), mr_gpslong, mr_gpslat, "C");
        }
    }

    public void switchContent(Steps currentForm, String title) {
        try {
            fragment = (Fragment) currentForm.getFragClass().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        toolbar.setTitle(title);
        ft.replace(R.id.container_main, fragment, currentForm.name());
        ft.commit();
    }

    public void switchFragment(Fragment fragment, String title) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        toolbar.setTitle(title);
        ft.replace(R.id.container_main, fragment);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (transaction_details_arraylist.size() != 0) {
            showdialog(DLG_TRANSACTION_LIST_CLEAR_MSG);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle print_navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_collection:
                if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_ROLE, ""), "AEE")
                        || StringUtils.startsWithIgnoreCase(sPref.getString(sPref_ROLE, ""), "AAO")) {
                    switchContent(Steps.FORM4, getResources().getString(R.string.collection));
                } else switchContent(Steps.FORM0, getResources().getString(R.string.collection));
                break;

            case R.id.nav_collection_reports:
                switchContent(Steps.FORM1, getResources().getString(R.string.collection_reports));
                break;

            case R.id.nav_upload:
                switchContent(Steps.FORM2, getResources().getString(R.string.upload));
                break;

            case R.id.nav_settings:
                switchContent(Steps.FORM3, getResources().getString(R.string.settings));
                break;

            case R.id.nav_logout:
                if (transaction_details_arraylist.size() != 0) {
                    showdialog(DLG_TRANSACTION_LIST_CLEAR_MSG);
                } else {
                    editor.putString(sPref_Collection_login, "No");
                    editor.putString(sPref_MRCODE, "");
                    editor.putString(sPref_MRNAME, "");
                    editor.putString(sPref_SUBDIVCODE, "");
                    editor.putString(sPref_START_TIME, "");
                    editor.putString(sPref_END_TIME, "");
                    editor.putString(sPref_ROLE, "");
                    editor.putString(NON_REVENUE_HEAD, "");
                    editor.commit();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public ClassGPS getClassGPS() {
        return this.gps;
    }

    public GetSetValues getSetValues() {
        return this.getSetValues;
    }

    public TRMCollection_Database getCollectionDatabase() {
        return this.collectionDatabase;
    }

    public SharedPreferences getsharedPref() {
        return this.sPref;
    }

    public CheckInternetConnection getInternetConnection() {
        return this.internetConnection;
    }

    public ArrayList<GetSetValues> get_details_list() {
        return this.transaction_details_arraylist;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            FunctionCalls fcall = new FunctionCalls();
            String status = intent.getStringExtra("message");
            fcall.logStatus("Handler Printer Broadcast receiving Connected from service");
            switch (status) {
                case RESULT:
                    String printer = intent.getStringExtra("printer");
                    fcall.showToast(MainActivity.this, printer+" Bluetooth Printer Connected");
                    break;

                case DISCONNECTED:
                    fcall.showToast(MainActivity.this, "Bluetooth Printer Disconnected");
                    break;

                case TURNED_OFF:
                    fcall.showToast(MainActivity.this, "Please Turn On the printer and proceed...");
                    break;
            }
        }
    };

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void startservice(final Context context) {
        if (!isMyServiceRunning(BluetoothService.class)) {
            functionCalls.logStatus("Service not running");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    functionCalls.logStatus("Service Started");
                    Intent bluetoothservice = new Intent(context, BluetoothService.class);
                    startService(bluetoothservice);
                }
            }, 500);
        } else functionCalls.logStatus("Service running");
        Intent tracking_Service = new Intent(context, MR_Tracking_service.class);
        startService(tracking_Service);
    }

    public void stopservice(Context context) {
        functionCalls.logStatus("Service Stopped");
        Intent bluetoothservice = new Intent(context, BluetoothService.class);
        stopService(bluetoothservice);
    }

    private void GPSlocation() {
        if (gps.canGetLocation()) {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            mr_gpslat = ""+latitude;
            mr_gpslong = ""+longitude;
        }
    }

    public String getApplication_version() {
        String version = "";
        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    public String getCollection_device_id() {
        return this.collection_device_id;
    }

    public void hidekeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    private void showdialog(int id) {
        switch (id) {
            case DLG_TRANSACTION_LIST_CLEAR_MSG:
                AlertDialog.Builder details_clear = new AlertDialog.Builder(this);
                details_clear.setCancelable(false);
                details_clear.setMessage(getResources().getString(R.string.details_clear_msg));
                details_clear.setPositiveButton(getResources().getString(R.string.select_ok), null);
                AlertDialog alertDialog = details_clear.create();
                alertDialog.show();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSetValues.setCollection_login("No");
        handler.removeCallbacksAndMessages(null);
        unregisterReceiver(mReceiver);
        if (!TextUtils.isEmpty(printer)) {
            if (!printer.equals("ALG"))
                stopservice(MainActivity.this);
            else {
                unregisterReceiver(Receiver);
                if (BluetoothService.printerconnected)
                    try {
                        conn.closeBT();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
            }
        }
    }

    public void startBroadcast() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                device_adapter.startDiscovery();
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
                filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                registerReceiver(Receiver, filter);
            }
        }, 1500);
    }

    public BroadcastReceiver Receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                handler.sendEmptyMessage(ANALOGICS_PRINTER_CONNECTED);
                functionCalls.showToast(MainActivity.this, "Analogics Printer Connected");
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                handler.sendEmptyMessage(ANALOGICS_PRINTER_DISCONNECTED);
                functionCalls.showToast(MainActivity.this, "Analogics Printer Disconnected");
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                if (StringUtils.startsWithIgnoreCase(device.getName(), "AT3TV3")) {
                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        bluetoothDevice = device;
                        handler.sendEmptyMessage(ANALOGICS_PRINTER_PAIRED);
                    }
                } else if (StringUtils.startsWithIgnoreCase(device.getName(), "AT2TV3")) {
                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        bluetoothDevice = device;
                        handler.sendEmptyMessage(ANALOGICS_PRINTER_PAIRED);
                    }
                }
            }
        }
    };
}
