package com.transvision.trmcollection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.transvision.trmcollection.adapters.RoleAdapter;
import com.transvision.trmcollection.database.TRMCollection_Database;
import com.transvision.trmcollection.posting.FTPApi;
import com.transvision.trmcollection.posting.FTPApi.Download_apk;
import com.transvision.trmcollection.posting.SendingData;
import com.transvision.trmcollection.posting.SendingData.Collection_Login;
import com.transvision.trmcollection.values.CheckInternetConnection;
import com.transvision.trmcollection.values.ClassGPS;
import com.transvision.trmcollection.values.FunctionCalls;
import com.transvision.trmcollection.values.GetSetValues;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.transvision.trmcollection.values.Constants.APK_FILE_DOWNLOADED;
import static com.transvision.trmcollection.values.Constants.APK_FILE_DOWNLOAD_ERROR;
import static com.transvision.trmcollection.values.Constants.APK_FILE_NOT_FOUND;
import static com.transvision.trmcollection.values.Constants.COLLECTION_LOGIN_FAILURE;
import static com.transvision.trmcollection.values.Constants.COLLECTION_LOGIN_NOT_APPROVED;
import static com.transvision.trmcollection.values.Constants.COLLECTION_LOGIN_SUCCESS;
import static com.transvision.trmcollection.values.Constants.GETSET;
import static com.transvision.trmcollection.values.Constants.LOGIN_DATE_EQUALS;
import static com.transvision.trmcollection.values.Constants.LOGIN_DATE_LESS;
import static com.transvision.trmcollection.values.Constants.LOGIN_DATE_MORE;
import static com.transvision.trmcollection.values.Constants.MR_TRACKING_UPDATE;
import static com.transvision.trmcollection.values.Constants.MR_TRACKING_UPDATE_FAIL;
import static com.transvision.trmcollection.values.Constants.PREFS_NAME;
import static com.transvision.trmcollection.values.Constants.PROD_URL;
import static com.transvision.trmcollection.values.Constants.TEST_URL;
import static com.transvision.trmcollection.values.Constants.TRM_COLLECTION_TESTING;
import static com.transvision.trmcollection.values.Constants.sPref_APPLICATION_VERSION;
import static com.transvision.trmcollection.values.Constants.sPref_COLLECTION_COLLECTED;
import static com.transvision.trmcollection.values.Constants.sPref_COLLECTION_DATE;
import static com.transvision.trmcollection.values.Constants.sPref_COLLECTION_LIMIT;
import static com.transvision.trmcollection.values.Constants.sPref_COLLECTION_TYPE;
import static com.transvision.trmcollection.values.Constants.sPref_Collection_login;
import static com.transvision.trmcollection.values.Constants.sPref_DEVICE_ID;
import static com.transvision.trmcollection.values.Constants.sPref_END_TIME;
import static com.transvision.trmcollection.values.Constants.sPref_MRCODE;
import static com.transvision.trmcollection.values.Constants.sPref_MRNAME;
import static com.transvision.trmcollection.values.Constants.sPref_RECEIPT_NO;
import static com.transvision.trmcollection.values.Constants.sPref_ROLE;
import static com.transvision.trmcollection.values.Constants.sPref_START_TIME;
import static com.transvision.trmcollection.values.Constants.sPref_SUBDIVCODE;
import static com.transvision.trmcollection.values.Constants.sPref_SUBDIVNAME;

public class LoginActivity extends AppCompatActivity {

    private static final int RequestPermissionCode = 1;

    private static final int DLG_LOGIN = 2;
    private static final int DLG_LOGIN_FAILURE = 3;
    private static final int DLG_LOGIN_APPROVAL = 4;
    private static final int DLG_APK_UPDATE_SUCCESS = 5;
    private static final int DLG_APK_NOT_FOUND = 6;
    private static final int DLG_INTERNET_CONNECTION = 7;

    ClassGPS classGPS;
    Spinner role_spinner;
    ArrayList<GetSetValues> roles_list;
    RoleAdapter roleAdapter;
    GetSetValues getSetValues, getSet;
    SharedPreferences sPref;
    SharedPreferences.Editor editor;
    SendingData sendingData;
    FTPApi ftpApi;
    FunctionCalls functionCalls;
    ProgressDialog progressDialog;
    TRMCollection_Database collectionDatabase;
    CheckInternetConnection checkInternetConnection;

    Button login_btn;
    CheckBox testing_app;
    String collection_device_id = "", login_role = "", curr_version = "", mr_gpslat = "", mr_gpslong = "";

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case COLLECTION_LOGIN_SUCCESS:
                    progressDialog.dismiss();
                    getSetValues.setCollection_login("Yes");
                    editor.putString(sPref_MRCODE, getSetValues.getColl_mrcode());
                    editor.putString(sPref_MRNAME, getSetValues.getColl_mrname());
                    editor.putString(sPref_SUBDIVCODE, getSetValues.getColl_subdiv_code());
                    editor.putString(sPref_SUBDIVNAME, getSetValues.getColl_subdiv_name());
                    editor.putString(sPref_START_TIME, getSetValues.getColl_start_time());
                    editor.putString(sPref_END_TIME, getSetValues.getColl_end_time());
                    if (!functionCalls.compare_collection_dates(sPref.getString(sPref_COLLECTION_DATE, ""))) {
                        editor.putString(sPref_COLLECTION_COLLECTED, "0");
                    }
                    editor.putString(sPref_COLLECTION_LIMIT, getSetValues.getColl_limit());
                    editor.putString(sPref_COLLECTION_DATE, getSetValues.getColl_date());
                    editor.putString(sPref_RECEIPT_NO, getSetValues.getColl_recpt_no());
                    editor.putString(sPref_Collection_login, "Yes");
                    editor.putString(sPref_ROLE, login_role);
                    editor.putString(sPref_APPLICATION_VERSION, getSetValues.getColl_app_version());
                    editor.putString(sPref_COLLECTION_TYPE, getSetValues.getCollection_type());
                    editor.putString(sPref_DEVICE_ID, collection_device_id);
                    editor.commit();
                    collectionDatabase.insert_collection_date(functionCalls.set_CurrentDate());
                    if (functionCalls.compare(curr_version, getSetValues.getColl_app_version())) {
                        showdialog(DLG_APK_UPDATE_SUCCESS);
                    } else movetoNext();
                    break;

                case COLLECTION_LOGIN_FAILURE:
                    progressDialog.dismiss();
                    showdialog(DLG_LOGIN_FAILURE);
                    break;

                case COLLECTION_LOGIN_NOT_APPROVED:
                    progressDialog.dismiss();
                    showdialog(DLG_LOGIN_APPROVAL);
                    break;

                case LOGIN_DATE_EQUALS:
                    if (!TextUtils.isEmpty(sPref.getString(sPref_COLLECTION_DATE, ""))) {
                        if (functionCalls.compare_Times(sPref.getString(sPref_COLLECTION_DATE, "") + " " + sPref.getString(sPref_START_TIME, ""),
                                sPref.getString(sPref_COLLECTION_DATE, "") + " " + sPref.getString(sPref_END_TIME, "")))
                            movetoNext();
                        else
                            functionCalls.showToast(LoginActivity.this, "Collection Time is over...");
                    }
                    break;

                case LOGIN_DATE_MORE:
                    clear_sPref();
                    break;

                case LOGIN_DATE_LESS:
                    clear_sPref();
                    functionCalls.showToast(LoginActivity.this, "Please check the system date...");
                    break;

                case APK_FILE_DOWNLOADED:
                    progressDialog.dismiss();
                    functionCalls.updateApplication(LoginActivity.this, new File(functionCalls.filepath("ApkFolder") +
                            File.separator + "TRM_Collection_" + getSetValues.getColl_app_version() + ".apk"));
                    break;

                case APK_FILE_NOT_FOUND:
                    progressDialog.dismiss();
                    showdialog(DLG_APK_NOT_FOUND);
                    break;

                case APK_FILE_DOWNLOAD_ERROR:
                    Download_apk downloadApk = ftpApi.new Download_apk(handler, getSetValues.getColl_app_version());
                    downloadApk.execute();
                    break;

                case MR_TRACKING_UPDATE:
                    functionCalls.logStatus("MR Tracking Successfully...");
                    break;

                case MR_TRACKING_UPDATE_FAIL:
                    functionCalls.logStatus("MR Tracking Failed...");
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initialize();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkPermissionsMandAbove();
            }
        }, 500);

        for (int i = 0; i < getResources().getStringArray(R.array.login_role).length; i++) {
            getSet = new GetSetValues();
            getSet.setLogin_role(getResources().getStringArray(R.array.login_role)[i]);
            roles_list.add(getSet);
            roleAdapter.notifyDataSetChanged();
        }

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!StringUtils.startsWithIgnoreCase(login_role, "--SELECT--")) {
                    showdialog(DLG_LOGIN);
                } else
                    functionCalls.showToast(LoginActivity.this, getResources().getString(R.string.collection_app_login_role_error));
            }
        });

        for (int i = 0; i < roles_list.size(); i++) {
            if (sPref.getString(sPref_ROLE, "").equals(roles_list.get(i).getLogin_role())) {
                role_spinner.setSelection(i);
                break;
            }
        }

        role_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                GetSetValues roledetails = roles_list.get(position);
                login_role = roledetails.getLogin_role();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        testing_app.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (testing_app.isChecked())
                    editor.putString(TRM_COLLECTION_TESTING, TEST_URL);
                else editor.putString(TRM_COLLECTION_TESTING, PROD_URL);
                editor.commit();
                sendingData = new SendingData(LoginActivity.this);
            }
        });
    }

    private void initialize() {
        role_spinner = (Spinner) findViewById(R.id.login_users_spin);
        roles_list = new ArrayList<>();
        roleAdapter = new RoleAdapter(roles_list, this);
        role_spinner.setAdapter(roleAdapter);

        sPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        editor = sPref.edit();
        editor.apply();

        getSetValues = new GetSetValues();
        functionCalls = new FunctionCalls();
        classGPS = new ClassGPS(LoginActivity.this);
        ftpApi = new FTPApi();
        progressDialog = new ProgressDialog(LoginActivity.this);
        checkInternetConnection = new CheckInternetConnection(this);

        login_btn = (Button) findViewById(R.id.login_btn);
        testing_app = (CheckBox) findViewById(R.id.cb_testing_app);

        if (StringUtils.startsWithIgnoreCase(sPref.getString(TRM_COLLECTION_TESTING, ""), TEST_URL))
            testing_app.setChecked(true);
        else testing_app.setChecked(false);
        sendingData = new SendingData(this);

        GPSlocation();
    }

    private void movetoNext() {
        if (functionCalls.compare(curr_version, sPref.getString(sPref_APPLICATION_VERSION, ""))) {
            showdialog(DLG_APK_UPDATE_SUCCESS);
        } else {
            send_MR_Location();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra(GETSET, getSetValues);
            startActivity(intent);
            finish();
        }
    }

    private void checkforlogin(Handler handler) {
        if (StringUtils.startsWithIgnoreCase(sPref.getString(sPref_Collection_login, ""), "Yes")) {
            String login = functionCalls.set_CurrentDate();
            Cursor exist = collectionDatabase.getCollection_date();
            if (exist.getCount() > 0) {
                exist.moveToNext();
                functionCalls.check_login_collection_date(login, exist.getString(exist.getColumnIndex("COLL_DATE")), handler);
            }
        }
    }

    @SuppressLint("HardwareIds")
    private void getDevice_id() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (tm != null)
            collection_device_id = tm.getDeviceId();
        //collection_device_id = "354016070557564";
        //Username-10540038
        // collection_device_id = "359932076932608";
//        collection_device_id = "357869083548989";
//        collection_device_id = "866133032881726";
//        collection_device_id = "352514083194823";
//        collection_device_id = "352514083077473";

        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            curr_version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        collectionDatabase = new TRMCollection_Database(this);
        collectionDatabase.open();
        checkforlogin(handler);
    }

    private void clear_sPref() {
        editor.putString(sPref_START_TIME, "");
        editor.putString(sPref_END_TIME, "");
        editor.putString(sPref_Collection_login, "No");
        editor.commit();
    }

    private void send_MR_Location() {
        SendingData.MR_Track mrTrack = sendingData.new MR_Track(handler);
        mrTrack.execute(sPref.getString(sPref_MRCODE, ""), sPref.getString(sPref_DEVICE_ID, ""), mr_gpslong, mr_gpslat, "C");
    }

    private void GPSlocation() {
        if (classGPS.canGetLocation()) {
            double latitude = classGPS.getLatitude();
            double longitude = classGPS.getLongitude();
            mr_gpslat = "" + latitude;
            mr_gpslong = "" + longitude;
        }
    }

    private void showdialog(int id) {
        AlertDialog dialog;
        switch (id) {
            case DLG_LOGIN:
                AlertDialog.Builder login_dlg = new AlertDialog.Builder(this);
                login_dlg.setTitle(getResources().getString(R.string.dialog_login));
                login_dlg.setCancelable(false);
                @SuppressLint("InflateParams")
                LinearLayout dlg_linear = (LinearLayout) getLayoutInflater().inflate(R.layout.dlg_login_layout, null);
                login_dlg.setView(dlg_linear);
                final EditText et_loginid = dlg_linear.findViewById(R.id.et_login_id);
                final EditText et_password = dlg_linear.findViewById(R.id.et_login_password);
                login_dlg.setPositiveButton(getResources().getString(R.string.dialog_login), null);
                login_dlg.setNegativeButton(getResources().getString(android.R.string.cancel), null);
                final AlertDialog login_dialog = login_dlg.create();
                login_dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button positive = login_dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        Button negative = login_dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        positive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (checkInternetConnection.isConnectingToInternet()) {
                                    String login_id = et_loginid.getText().toString();
                                    if (!TextUtils.isEmpty(login_id)) {
                                        String password = et_password.getText().toString();
                                        if (!TextUtils.isEmpty(password)) {
                                            login_dialog.dismiss();
                                            functionCalls.showprogressdialog(getResources().getString(R.string.login_dialog_title),
                                                    getResources().getString(R.string.login_dialog_msg), progressDialog);
                                            Collection_Login collectionLogin = sendingData.new Collection_Login(handler, getSetValues);
                                            collectionLogin.execute(login_id, collection_device_id, password);
                                        } else
                                            et_password.setError(getResources().getString(R.string.dialog_login_password_error));
                                    } else
                                        et_loginid.setError(getResources().getString(R.string.dialog_login_id_error));
                                } else {
                                    login_dialog.dismiss();
                                    showdialog(DLG_INTERNET_CONNECTION);
                                }
                            }
                        });
                        negative.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                login_dialog.dismiss();
                            }
                        });
                    }
                });
                login_dialog.show();
                login_dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
                login_dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
                break;

            case DLG_LOGIN_FAILURE:
                AlertDialog.Builder login_fail = new AlertDialog.Builder(LoginActivity.this);
                login_fail.setTitle(getResources().getString(R.string.dialog_login));
                login_fail.setCancelable(false);
                login_fail.setMessage(getResources().getString(R.string.collection_login_failure));
                login_fail.setPositiveButton(getResources().getString(R.string.select_ok), null);
                dialog = login_fail.create();
                dialog.show();
                break;

            case DLG_LOGIN_APPROVAL:
                AlertDialog.Builder login_approval = new AlertDialog.Builder(LoginActivity.this);
                login_approval.setTitle(getResources().getString(R.string.dialog_login));
                login_approval.setCancelable(false);
                login_approval.setMessage(getResources().getString(R.string.collection_login_approval));
                login_approval.setPositiveButton(getResources().getString(R.string.select_ok), null);
                dialog = login_approval.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GRAY);
                break;

            case DLG_APK_UPDATE_SUCCESS:
                AlertDialog.Builder apk_update = new AlertDialog.Builder(LoginActivity.this);
                apk_update.setTitle(getResources().getString(R.string.collection_apk_update_title));
                apk_update.setCancelable(false);
                apk_update.setMessage(getResources().getString(R.string.current_version) + " " + curr_version +
                        "\n" + "\n" +
                        getResources().getString(R.string.newer_version) + " " + sPref.getString(sPref_APPLICATION_VERSION, "") + "\n");
                apk_update.setPositiveButton(getResources().getString(R.string.collection_apk_update_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        functionCalls.showprogressdialog(getResources().getString(R.string.collection_apk_update_title),
                                getResources().getString(R.string.collection_apk_download_msg), progressDialog);
                        Download_apk downloadApk = ftpApi.new Download_apk(handler, getSetValues.getColl_app_version());
                        downloadApk.execute();
                    }
                });
                apk_update.setNeutralButton(getResources().getString(R.string.select_cancel), null);
                dialog = apk_update.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.RED);
                break;

            case DLG_APK_NOT_FOUND:
                AlertDialog.Builder apk_not_found = new AlertDialog.Builder(LoginActivity.this);
                apk_not_found.setTitle(getResources().getString(R.string.collection_apk_update_title));
                apk_not_found.setCancelable(false);
                apk_not_found.setMessage(getResources().getString(R.string.collection_apk_not_found_to_download));
                apk_not_found.setPositiveButton(getResources().getString(R.string.select_ok), null);
                dialog = apk_not_found.create();
                dialog.show();
                break;

            case DLG_INTERNET_CONNECTION:
                AlertDialog.Builder internet_connection = new AlertDialog.Builder(LoginActivity.this);
                internet_connection.setCancelable(false);
                internet_connection.setMessage(getResources().getString(R.string.internet_connection));
                internet_connection.setPositiveButton(getResources().getString(R.string.select_ok), null);
                dialog = internet_connection.create();
                dialog.show();
                break;
        }
    }

    @TargetApi(23)
    public void checkPermissionsMandAbove() {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= 23) {
            if (checkPermission()) {
                getDevice_id();
            } else {
                requestPermission();
            }
        } else {
            getDevice_id();
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(LoginActivity.this, new String[]
                {
                        READ_PHONE_STATE,
                        WRITE_EXTERNAL_STORAGE,
                        ACCESS_FINE_LOCATION,
                        CAMERA
                }, RequestPermissionCode);
    }

    private boolean checkPermission() {
        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int FourthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ThirdPermissionResult == PackageManager.PERMISSION_GRANTED &&
                FourthPermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean ReadPhoneStatePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadStoragePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadLocationPermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadCameraPermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    if (ReadPhoneStatePermission && ReadStoragePermission && ReadLocationPermission && ReadCameraPermission) {
                        getDevice_id();
                    } else {
                        functionCalls.showToast(LoginActivity.this, getResources().getString(R.string.collection_app_permissions));
                        finish();
                    }
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
