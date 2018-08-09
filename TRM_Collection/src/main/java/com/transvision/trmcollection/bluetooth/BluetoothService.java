package com.transvision.trmcollection.bluetooth;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;

import com.analogics.thermalprinter.AnalogicsThermalPrinter;
import com.lvrenyang.io.BTPrinting;
import com.lvrenyang.io.Canvas;
import com.lvrenyang.io.IOCallBack;
import com.lvrenyang.io.Pos;
import com.ngx.BluetoothPrinter;
import com.transvision.trmcollection.database.TRMCollection_Database;
import com.transvision.trmcollection.values.FunctionCalls;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.transvision.trmcollection.values.Constants.BLUETOOTH_RESULT;
import static com.transvision.trmcollection.values.Constants.DISCONNECTED;
import static com.transvision.trmcollection.values.Constants.RESULT;
import static com.transvision.trmcollection.values.Constants.TURNED_OFF;

public class BluetoothService extends Service implements IOCallBack {
    private static final int PRINTER_CONNECTED = 10;
    private static final int PRINTER_DISCONNECTED = 11;

    BluetoothAdapter mBluetoothAdapter;
    static BluetoothSocket mmSocket;
    BluetoothDevice bluetoothDevice;
    FunctionCalls functionCalls;
    String rep_deviceconnected="";
    public static boolean printerconnected = false;
    boolean ngxprinter = false, ngxservice = false, analogics = false, goojprt = false, broadcast = false,
            devicepaired = false, service_destroyed=false;
    String printer="";
    TRMCollection_Database collectionDatabase;

    public static BluetoothPrinter mBtp = BluetoothPrinter.INSTANCE;
    public static AnalogicsThermalPrinter conn = new AnalogicsThermalPrinter();

    private String mConnectedDeviceName = "";
    public static String printer_address = "";
    public static SharedPreferences mSp;
    private int count=0;

    BTPrinting mBt = new BTPrinting();
    public static Canvas mCanvas = new Canvas();
    public static Pos mPos = new Pos();
    public static ExecutorService es = Executors.newScheduledThreadPool(30);
    BluetoothService mActivity;

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothPrinter.MESSAGE_STATE_CHANGE:
                    if (!printer.equals("ALG")) {
                        switch (msg.arg1) {
                            case BluetoothPrinter.STATE_CONNECTED:
                                printer_address = bluetoothDevice.getAddress();
                                functionCalls.logStatus("Connected to: " + mConnectedDeviceName);
                                sendBroadcastMessage("NGX", RESULT);
                                ngxprinter = true;
                                printerconnected = true;
                                break;
                            case BluetoothPrinter.STATE_CONNECTING:
                                functionCalls.logStatus("Connecting");
                                break;
                            case BluetoothPrinter.STATE_LISTEN:
                                functionCalls.logStatus("State Listening");
                                break;
                            case BluetoothPrinter.STATE_NONE:
                                functionCalls.logStatus("Not Connected");
                                if (!broadcast) {
                                    if (!service_destroyed)
                                        startBroadcast();
                                }
                                functionCalls.logStatus("Handler Printer Turned Off: " + rep_deviceconnected);
                                if (!printer.equals("ALG")) {
                                    sendBroadcastMessage("", TURNED_OFF);
                                }
                                ngxprinter = false;
                                break;
                        }
                    }
                    break;

                case BluetoothPrinter.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    if (!printer.equals("ALG"))
                        mConnectedDeviceName = "Device: " + msg.getData().getString(BluetoothPrinter.DEVICE_NAME);
                    break;

                case BluetoothPrinter.MESSAGE_STATUS:
                    if (!printer.equals("ALG"))
                        functionCalls.logStatus("Status: " + msg.getData().getString(BluetoothPrinter.STATUS_TEXT));
                    break;

                case PRINTER_CONNECTED:
                    if (!goojprt)
                        analogics = true;
                    printerconnected = true;
                    printer_address = bluetoothDevice.getAddress();
                    functionCalls.logStatus("Handler Printer Connected: " + rep_deviceconnected);
                    functionCalls.logStatus("Handler Printer Broadcast sending Connected from service");
                    if (printer.equals("GPT")) {
                        sendBroadcastMessage("Phi", RESULT);
                    } else if (printer.equals("ALG")) {
                        sendBroadcastMessage("Analogics", RESULT);
                    }
                    break;

                case PRINTER_DISCONNECTED:
                    analogics = false;
                    goojprt = false;
                    devicepaired = false;
                    if (printerconnected) {
                        printerconnected = false;
                        functionCalls.logStatus("Handler Printer Disconnected: " + rep_deviceconnected);
                        sendBroadcastMessage("", DISCONNECTED);
                    } else {
                        functionCalls.logStatus("Handler Printer Turned Off: " + rep_deviceconnected);
                        sendBroadcastMessage("", TURNED_OFF);
                    }
                    if (!broadcast) {
                        if (!service_destroyed)
                            startBroadcast();
                    }
                    break;

                default:
                    break;
            }
            return true;
        }
    });

    public BluetoothService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        functionCalls = new FunctionCalls();

        collectionDatabase = new TRMCollection_Database(this);
        collectionDatabase.open();
        Cursor print = collectionDatabase.getPrinter_details();
        if (print.getCount() > 0) {
            print.moveToNext();
            printer = print.getString(print.getColumnIndex("PRINTER"));
        }
        if (printer.equals("GPT")) {
            mActivity = this;
            mPos.Set(mBt);
            mCanvas.Set(mBt);
            mBt.SetCallBack(this);
        }
    }

    private void sendBroadcastMessage(String printer_msg, String message) {
        Intent intent = new Intent();
        intent.setAction(BLUETOOTH_RESULT);
        intent.putExtra("message", message);
        intent.putExtra("printer", printer_msg);
        sendBroadcast(intent);
    }

    private void startBroadcast() {
        functionCalls.logStatus("Broadcast Receiver Starting");
        mBluetoothAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (printer.equals("ALG"))
            startBroadcast();
        else getPairedDevices();

        return Service.START_STICKY;
    }

    private void getPairedDevices() {
        Set<BluetoothDevice> pairedDevice = mBluetoothAdapter.getBondedDevices();
        if (pairedDevice.size() > 0) {
            try {
                for (BluetoothDevice device : pairedDevice) {
                    printerConnection(device);
                }
            } catch (Exception e) {
                if (!devicepaired) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            functionCalls.logStatus("Device Paired but Broadcast starting after 1 seconds...");
                            startBroadcast();
                        }
                    }, 1000);
                }
            }
        } else {
            if (!devicepaired) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        functionCalls.logStatus("Device Paired but Broadcast starting after 1 seconds...");
                        startBroadcast();
                    }
                }, 1000);
            }
        }
    }

    private void printerConnection(final BluetoothDevice device) {
        switch (printer) {
            case "ALG":
                if (StringUtils.startsWithIgnoreCase(device.getName(), "AT3TV3")) {
                    bluetoothDevice = device;
                    rep_deviceconnected = device.getName();
                    devicepaired = true;
                    functionCalls.logStatus("Analogics Bluetooth device connection name: "+device.getName());
                    functionCalls.logStatus("Analogics Bluetooth device connection address: "+device.getAddress());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                functionCalls.logStatus("Connecting....");
                                conn.openBT(device.getAddress());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }, 500);
                }
                break;

            case "NGX":
                devicepaired = true;
                setNgxprinter(device);
                break;

            case "GPT":
                if (StringUtils.startsWithIgnoreCase(device.getName(), "BP301-2")) {
                    bluetoothDevice = device;
                    rep_deviceconnected = device.getName();
                    devicepaired = true;
                    functionCalls.logStatus("Bluetooth device: "+device.getName());
                    es.submit(new TaskOpen(mBt, device.getAddress(), mActivity));
                }
                break;
        }
    }

    public static BluetoothSocket getSocket() {
        return mmSocket;
    }

    public static BluetoothPrinter getngxprinter() {
        return mBtp;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        service_destroyed = true;
        if (printerconnected) {
            if (analogics) {
                try {
                    conn.closeBT();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if (ngxprinter) {
                mBtp.onActivityPause();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBtp.onActivityDestroy();
                    }
                }, 500);
            }
            if (goojprt) {
                es.submit(new TaskClose(mBt));
            }
        }
        if (broadcast) {
            unregisterReceiver(mReceiver);
        }
        mHandler.removeCallbacksAndMessages(null);
        functionCalls.logStatus("Service destroyed...");
    }

    @Override
    public void OnOpen() {
        goojprt = true;
        mHandler.sendEmptyMessage(PRINTER_CONNECTED);
    }

    @Override
    public void OnOpenFailed() {
        goojprt = false;
        mHandler.sendEmptyMessage(PRINTER_DISCONNECTED);
    }

    @Override
    public void OnClose() {
        goojprt = false;
        mHandler.sendEmptyMessage(PRINTER_DISCONNECTED);
    }

    private void setNgxprinter(final BluetoothDevice device) {
        if (!ngxservice) {
            bluetoothDevice = device;
            ngxservice = true;
            mSp = PreferenceManager.getDefaultSharedPreferences(this);
            try {
                mBtp.initService(this, mHandler);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (StringUtils.startsWithIgnoreCase(device.getName(), "BTP")) {
                    mBtp.setPreferredPrinter(device.getAddress());
                    rep_deviceconnected = device.getName();
                    functionCalls.logStatus("Bluetooth device: "+device.getName());
                } else functionCalls.logStatus("NGX Bluetooth device not paired or switched On");
            }
        }, 1000);
    }

    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            broadcast = true;
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    functionCalls.logStatus("ACTION_FOUND_PAIRED: "+device.getName());
                    printerConnection(device);
                } else functionCalls.logStatus("ACTION_FOUND_UNPAIRED: "+device.getName());
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                functionCalls.logStatus("ACTION_CONNECTED: "+device.getName());
                mHandler.sendEmptyMessage(PRINTER_CONNECTED);
                printerconnected = true;
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                functionCalls.logStatus("ACTION_DISCOVERY_FINISHED");
                if (!printerconnected)
                    mBluetoothAdapter.startDiscovery();
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                functionCalls.logStatus("ACTION_DISCOVERY_STARTED");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                functionCalls.logStatus("ACTION_DISCONNECTED: "+device.getName());
                mHandler.sendEmptyMessage(PRINTER_DISCONNECTED);
                printerconnected = false;
                mBluetoothAdapter.startDiscovery();
            }
            else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    functionCalls.logStatus("Paired Device: "+device.getName());
                }
            }
        }
    };

    private class TaskOpen implements Runnable {
        BTPrinting bt = null;
        String address = null;
        Context context = null;

        private TaskOpen(BTPrinting bt, String address, Context context) {
            this.bt = bt;
            this.address = address;
            this.context = context;
        }

        @Override
        public void run() {
            bt.Open(address, context);
        }
    }

    private class TaskClose implements Runnable {
        BTPrinting bt = null;

        private TaskClose(BTPrinting bt) {
            this.bt = bt;
        }

        @Override
        public void run() {
            bt.Close();
        }
    }
}
