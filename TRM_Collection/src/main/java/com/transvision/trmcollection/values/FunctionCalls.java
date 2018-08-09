package com.transvision.trmcollection.values;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.transvision.trmcollection.database.TRMCollection_Database;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.transvision.trmcollection.values.Constants.COLLECTION_DATE_EQUALS;
import static com.transvision.trmcollection.values.Constants.COLLECTION_DATE_LESS;
import static com.transvision.trmcollection.values.Constants.COLLECTION_DATE_MORE;
import static com.transvision.trmcollection.values.Constants.FILE_ZIPPING_COMPLETED;
import static com.transvision.trmcollection.values.Constants.LOGIN_DATE_EQUALS;
import static com.transvision.trmcollection.values.Constants.LOGIN_DATE_LESS;
import static com.transvision.trmcollection.values.Constants.LOGIN_DATE_MORE;

public class FunctionCalls {

    public void logStatus(String msg) {
        Log.d("debug", msg);
    }

    public void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public String convertTo24Hour(String Time) {
        String convert = Time.substring(Time.length()-2);
//        convert = convert.substring(0, 1)+"."+convert.substring(1, 2)+".";
        convert = convert.toUpperCase();
        Time = Time.substring(0, Time.length()-2)+ " " + convert;
        String formattedDate="";
        try {
            SimpleDateFormat inFormat = new SimpleDateFormat("hh:mm aa", Locale.US);
            SimpleDateFormat outFormat = new SimpleDateFormat("HH:mm", Locale.US);
            formattedDate = outFormat.format(inFormat.parse(Time));
        } catch (Exception e) {
            e.printStackTrace();
        }
        logStatus("Converted: "+formattedDate);
        return formattedDate;
    }

    public void showprogressdialog(String title, String message, ProgressDialog progressDialog) {
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    private String Appfoldername() {
        return "TRM_Collection" + File.separator + "data";
    }

    public String transactiondateformat(String conversiondate) {
        String a1 = conversiondate.substring(0, 4);
        String a2 = conversiondate.substring(5, 7);
        String a3 = conversiondate.substring(8, 10);
        return a1 + a2 + a3;
    }

    public String filepath(String value) {
        File dir = new File(android.os.Environment.getExternalStorageDirectory(), Appfoldername()
                + File.separator + value);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String pathname = dir.toString();
        return pathname;
    }

    public File filestorepath(String value, String file) {
        File dir = new File(android.os.Environment.getExternalStorageDirectory(), Appfoldername()
                + File.separator + value);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dir1 = new File(dir, File.separator + file);
        return dir1;
    }

    public void zipdbandupload(String mobileuploadfilepath, String filename, Context context, Handler handler)
            throws IOException {
        String fileformat = ".zip";
        logStatus("Zip_Upload_1");
        logStatus("Zip_Upload_uploadpath: "+mobileuploadfilepath);
        logStatus("Zip_Upload_filename: "+filename);
        logStatus("Zip_Upload_format: "+fileformat);
        File tmp = new File(mobileuploadfilepath + filename + fileformat);
        logStatus("Zip_File_Upload: "+tmp.toString());
        logStatus("Zip_Upload_2");
        TRMCollection_Database upload = new TRMCollection_Database(context);
        logStatus("Zip_Upload_3");
        if (tmp.exists()) {
            logStatus("Zip_Upload_File Exists");
            tmp.delete();
            logStatus("Zip_Upload_File Deleted");
            upload.copyDBtoSD(mobileuploadfilepath, filename, fileformat);
            logStatus("Zip_Upload_Zipped");
            handler.sendEmptyMessage(FILE_ZIPPING_COMPLETED);
        } else {
            logStatus("Zip_Upload_File not found");
            upload.copyDBtoSD(mobileuploadfilepath, filename, fileformat);
            logStatus("Zip_Upload_Zipped");
            handler.sendEmptyMessage(FILE_ZIPPING_COMPLETED);
        }
    }

    public String set_CurrentDate() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int mnth2 = month + 1;
        String present_date1 = day + "/" + mnth2 + "/" + "" + year;
        Date date = null;
        try {
            date = new SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(present_date1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        c.setTime(date);
        return sdf.format(c.getTime());
    }

    public String receipt_date() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int mnth2 = month + 1;
        String present_date1 = year + "-" + mnth2 + "-" + "" + day;
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(present_date1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        c.setTime(date);
        return sdf.format(c.getTime());
    }

    public String receipt_date_time() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int mnth2 = month + 1;
        String present_date1 = day + "/" + mnth2 + "/" + "" + year + " " + ""+hour + ":"+ ""+minute;
        Date date = null;
        try {
            date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).parse(present_date1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
        c.setTime(date);
        return sdf.format(c.getTime());
    }

    public void check_login_collection_date(String login_date, String exist_date, Handler handler) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        Date login=null, exist=null;
        try {
            login = sdf.parse(login_date);
            exist = sdf.parse(exist_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (login.equals(exist)) {
            handler.sendEmptyMessage(LOGIN_DATE_EQUALS);
        } else if (login.after(exist)) {
            handler.sendEmptyMessage(LOGIN_DATE_MORE);
        } else handler.sendEmptyMessage(LOGIN_DATE_LESS);
    }

    private Date collectiontime(String date) {
        Date date1 = null;
        try {
            date1 = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1;
    }

    public String currentTime() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int mnth2 = month + 1;
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        String present_date1 = day + "-" + mnth2 + "-" + "" + year + " " + ""+hour + ":"+ ""+minute;
        Date date = null;
        try {
            date = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US).parse(present_date1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US);
        c.setTime(date);
        return sdf.format(c.getTime());
    }

    public boolean compare_Times(String start_time, String end_time) {
        boolean result = false;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US);
        Date fromdate = null, todate = null;
        try {
            fromdate = sdf.parse(start_time);
            todate = sdf.parse(end_time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date currentime = collectiontime(currentTime());
        if (currentime.after(fromdate)) {
            logStatus("more");
            if (currentime.before(todate)) {
                logStatus("less");
                result = true;
            } else result = false;
        }
        return result;
    }

    public boolean compare_receipt_times(String last_receipt, String pres_receipt) {
        boolean result = false;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
        Date lastreceipt = null, presentreceipt = null;
        if (!TextUtils.isEmpty(last_receipt)) {
            try {
                lastreceipt = sdf.parse(last_receipt);
                presentreceipt = sdf.parse(pres_receipt);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (presentreceipt != null) {
                if (presentreceipt.after(lastreceipt)) {
                    logStatus(COLLECTION_DATE_MORE);
                    result = true;
                } else if (presentreceipt.equals(lastreceipt)) {
                    logStatus(COLLECTION_DATE_EQUALS);
                    result = true;
                } else {
                    logStatus(COLLECTION_DATE_LESS);
                    result = false;
                }
            }
        } else {
            logStatus(COLLECTION_DATE_MORE);
            result = true;
        }
        return result;
    }

    public boolean compare_collection_dates(String collection_date) {
        String current_date = new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        Date curr_date = null, coll_date = null;
        if (!TextUtils.isEmpty(collection_date)) {
            try {
                curr_date = sdf.parse(current_date);
                coll_date = sdf.parse(collection_date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (coll_date.equals(curr_date))
                return true;
            else return false;
        } else return false;
    }

    //Dotted line
    public String line(int length) {
        StringBuilder sb5 = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb5.append("-");
        }
        return (sb5.toString());
    }

    public String space(String s, int len) {
        int temp;
        StringBuilder spaces = new StringBuilder();
        temp = len - s.length();
        for (int i = 0; i < temp; i++) {
            spaces.append(" ");
        }
        return (s + spaces);
    }

    public String aligncenter(String msg, int len) {
        int count = msg.length();
        int value = len - count;
        int append = (value / 2);
        return space(" ", append) + msg + space(" ", append);
    }

    public String alignright(String msg, int len) {
        for (int i = 0; i < len - msg.length(); i++) {
            msg = " " + msg;
        }
        msg = String.format("%" + len + "s", msg);
        return msg;
    }

    public void splitString(String msg, int lineSize, ArrayList<String> arrayList) {
        arrayList.clear();
        Pattern p = Pattern.compile("\\b.{0," + (lineSize - 1) + "}\\b\\W?");
        Matcher m = p.matcher(msg);
        while (m.find()) {
            arrayList.add(m.group().trim());
        }
    }

    public String convertto12(String time) {
        String formattedDate="";
        try {
            SimpleDateFormat inFormat = new SimpleDateFormat("hh:mm aa", Locale.US);
            SimpleDateFormat outFormat = new SimpleDateFormat("HH:mm", Locale.US);
            formattedDate = inFormat.format(outFormat.parse(time));
        } catch (Exception e) {
            e.printStackTrace();
        }
        logStatus("Converted: "+formattedDate);
        return formattedDate;
    }

    private String convertto24(String time) {
        String convert = time.substring(time.length()-2);
        convert = convert.toUpperCase();
        time = time.substring(0, time.length()-2)+ " " + convert;
        String formattedDate="";
        try {
            SimpleDateFormat inFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm aa", Locale.US);
            SimpleDateFormat outFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US);
            formattedDate = outFormat.format(inFormat.parse(time));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return formattedDate;
    }

    public boolean compare(String curr_ver, String server_ver) {
        String s1 = normalisedVersion(curr_ver);
        String s2 = normalisedVersion(server_ver);
        int cmp = s1.compareTo(s2);
        String cmpStr = cmp < 0 ? "<" : cmp > 0 ? ">" : "==";
        return cmpStr.equals("<");
    }

    private String normalisedVersion(String version) {
        String[] split = Pattern.compile(".", Pattern.LITERAL).split(version);
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(String.format("%" + 4 + 's', s));
        }
        return sb.toString();
    }

    public void updateApplication(Context context, File Apkfile) {
        Uri path;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            path = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", Apkfile);
        } else path = Uri.fromFile(Apkfile);
        Intent objIntent = new Intent(Intent.ACTION_VIEW);
        objIntent.setDataAndType(path, "application/vnd.android.package-archive");
        objIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        objIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(objIntent);
    }

    public boolean checkInternetConnection(Context context) {
        CheckInternetConnection cd = new CheckInternetConnection(context.getApplicationContext());
        return cd.isConnectingToInternet();
    }
}
