package com.transvision.trmcollection.posting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;

import com.transvision.trmcollection.values.FunctionCalls;
import com.transvision.trmcollection.values.GetSetValues;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import static android.content.Context.MODE_PRIVATE;
import static com.transvision.trmcollection.values.Constants.PREFS_NAME;
import static com.transvision.trmcollection.values.Constants.PROD_URL;
import static com.transvision.trmcollection.values.Constants.SERVICE;
import static com.transvision.trmcollection.values.Constants.TEST_TRM_URL;
import static com.transvision.trmcollection.values.Constants.TRM_COLLECTION_TESTING;
import static com.transvision.trmcollection.values.Constants.TRM_URL;

public class SendingData {
    private ReceivingData receivingData = new ReceivingData();
    private String BASE_URL;
    private FunctionCalls functionCalls = new FunctionCalls();

    public SendingData(Context context) {
        SharedPreferences sPref = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (StringUtils.startsWithIgnoreCase(sPref.getString(TRM_COLLECTION_TESTING, ""), PROD_URL)) {
            server_links(0);
        } else server_links(1);
    }

    private void server_links(int value) {
        if (value == 0) {
            BASE_URL = TRM_URL + SERVICE;
        } else BASE_URL = TEST_TRM_URL + SERVICE;
    }

    private String UrlPostConnection(String Post_Url, HashMap<String, String> datamap) throws IOException {
        StringBuilder response = new StringBuilder();
        URL url = new URL(Post_Url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(15000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(getPostDataString(datamap));
        writer.flush();
        writer.close();
        os.close();
        int responseCode=conn.getResponseCode();
        if (responseCode == HttpsURLConnection.HTTP_OK) {
            String line;
            BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line=br.readLine()) != null) {
                response.append(line);
            }
        }
        else response = new StringBuilder();
        return response.toString();
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }

    private String UrlGetConnection(String Get_Url) throws IOException {
        StringBuilder response = new StringBuilder();
        URL url = new URL(Get_Url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(15000);
        conn.setConnectTimeout(15000);
        int responseCode=conn.getResponseCode();
        if (responseCode == HttpsURLConnection.HTTP_OK) {
            String line;
            BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line=br.readLine()) != null) {
                response.append(line);
            }
        }
        else response = new StringBuilder();
        return response.toString();
    }

    @SuppressLint("StaticFieldLeak")
    public class Collection_Login extends AsyncTask<String, String, String> {
        String response="";
        Handler handler;
        GetSetValues getSetValues;

        public Collection_Login(Handler handler, GetSetValues getSetValues) {
            this.handler = handler;
            this.getSetValues = getSetValues;
        }

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> datamap = new HashMap<>();
            datamap.put("MRCode", params[0]);
            datamap.put("DeviceId", params[1]);
            datamap.put("PASSWORD", params[2]);
            functionCalls.logStatus("MRCode: "+params[0]);
            functionCalls.logStatus("DeviceID: "+params[1]);
            functionCalls.logStatus("Password: "+params[2]);
            try {
                response = UrlPostConnection(BASE_URL+"MRDetails", datamap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            receivingData.getCollection_login(result, handler, getSetValues);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class Collection_Details extends AsyncTask<String, String, String> {
        Handler handler;
        GetSetValues getSetValues;
        String response="";

        public Collection_Details(Handler handler, GetSetValues getSetValues) {
            this.handler = handler;
            this.getSetValues = getSetValues;
        }

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> datamap = new HashMap<>();
            datamap.put("MRCODE", params[0]);
            datamap.put("CONSNO", params[1]);
            datamap.put("DEVICE_ID", params[2]);
            datamap.put("Role", params[3]);
            functionCalls.logStatus("MRCODE: "+params[0]);
            functionCalls.logStatus("CONSNO: "+params[1]);
            functionCalls.logStatus("DEVICE_ID: "+params[2]);
            functionCalls.logStatus("Role: "+params[3]);
            try {
                response = UrlPostConnection(BASE_URL+"SEARCHCOLLCONSID", datamap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            receivingData.getCollection_details(result, handler, getSetValues);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class Posting_Collection_data extends AsyncTask<String, String, String> {
        String response="";
        GetSetValues getSetValues;
        Handler handler;

        public Posting_Collection_data(GetSetValues getSetValues, Handler handler) {
            this.getSetValues = getSetValues;
            this.handler = handler;
        }

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> datamap = new HashMap<>();
            datamap.put("MACHINEID", params[0]);
            datamap.put("MRCODE", params[1]);
            datamap.put("RRNO", params[2]);
            datamap.put("RECPTDATE", params[3]);
            datamap.put("AMT", params[4]);
            datamap.put("CASHCHQ", params[5]);
            datamap.put("CONSID", params[6]);
            datamap.put("CONSUMERNAME", params[7]);
            datamap.put("CHEKDDNO", params[8]);
            datamap.put("MICR", params[9]);
            datamap.put("BANKNAME", params[10]);
            datamap.put("CHEKDDDATE", params[11]);
            functionCalls.logStatus("MACHINEID :"+params[0]);
            functionCalls.logStatus("MRCODE :"+params[1]);
            functionCalls.logStatus("RRNO :"+params[2]);
            functionCalls.logStatus("RECPTDATE :"+params[3]);
            functionCalls.logStatus("AMT :"+params[4]);
            functionCalls.logStatus("CASHCHQ :"+params[5]);
            functionCalls.logStatus("CONSID :"+params[6]);
            functionCalls.logStatus("CONSUMERNAME :"+params[7]);
            functionCalls.logStatus("CHEKDDNO :"+params[8]);
            functionCalls.logStatus("MICR :"+params[9]);
            functionCalls.logStatus("BANKNAME :"+params[10]);
            functionCalls.logStatus("CHEKDDDATE :"+params[11]);
            try {
                response = UrlPostConnection(BASE_URL+"InsCollRec", datamap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            receivingData.getCollection_posting_status(result, handler, getSetValues);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class Non_Revenue_Collection_data extends AsyncTask<String, String, String> {
        String response="";
        GetSetValues getSetValues;
        Handler handler;

        public Non_Revenue_Collection_data(GetSetValues getSetValues, Handler handler) {
            this.getSetValues = getSetValues;
            this.handler = handler;
        }

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> datamap = new HashMap<>();
            datamap.put("MACHINEID", params[0]);
            datamap.put("MRCODE", params[1]);
            datamap.put("RRNO", params[2]);
            datamap.put("RECPTDATE", params[3]);
            datamap.put("AMT", params[4]);
            datamap.put("CASHCHQ", params[5]);
            datamap.put("CONSID", params[6]);
            datamap.put("CONSUMERNAME", params[7]);
            datamap.put("CHEKDDNO", params[8]);
            datamap.put("MICR", params[9]);
            datamap.put("BANKNAME", params[10]);
            datamap.put("CHEKDDDATE", params[11]);
            datamap.put("HEAD", params[12]);
            functionCalls.logStatus("MACHINEID :"+params[0]);
            functionCalls.logStatus("MRCODE :"+params[1]);
            functionCalls.logStatus("RRNO :"+params[2]);
            functionCalls.logStatus("RECPTDATE :"+params[3]);
            functionCalls.logStatus("AMT :"+params[4]);
            functionCalls.logStatus("CASHCHQ :"+params[5]);
            functionCalls.logStatus("CONSID :"+params[6]);
            functionCalls.logStatus("CONSUMERNAME :"+params[7]);
            functionCalls.logStatus("CHEKDDNO :"+params[8]);
            functionCalls.logStatus("MICR :"+params[9]);
            functionCalls.logStatus("BANKNAME :"+params[10]);
            functionCalls.logStatus("CHEKDDDATE :"+params[11]);
            functionCalls.logStatus("HEAD :"+params[12]);
            try {
                response = UrlPostConnection(BASE_URL+"NonRevenueInsCollRec", datamap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            receivingData.getCollection_non_revenue_status(result, handler, getSetValues);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class Collection_Report_Details extends AsyncTask<String, String, String> {
        String response="";
        Handler handler;
        GetSetValues getSetValues;
        ArrayList<GetSetValues> arrayList;

        public Collection_Report_Details(Handler handler, GetSetValues getSetValues, ArrayList<GetSetValues> arrayList) {
            this.handler = handler;
            this.getSetValues = getSetValues;
            this.arrayList = arrayList;
        }

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> datamap = new HashMap<>();
            datamap.put("MRCODE", params[0]);
            try {
                response = UrlPostConnection(BASE_URL+"CollDetails", datamap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            receivingData.getcollection_report_status(result, handler, getSetValues, arrayList);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class Non_Revenue_Headers extends AsyncTask<String, String, String> {
        String response="";
        Handler handler;
        SharedPreferences.Editor editor;

        public Non_Revenue_Headers(Handler handler, SharedPreferences.Editor editor) {
            this.handler = handler;
            this.editor = editor;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                response = UrlGetConnection(BASE_URL+"NonRevHead");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            receivingData.getNon_revenue_header_status(result, handler, editor);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class MR_Track extends AsyncTask<String, String, String> {
        String response="";
        Handler handler;

        public MR_Track(Handler handler) {
            this.handler = handler;
        }

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> datamap = new HashMap<>();
            datamap.put("mrcode", params[0]);
            datamap.put("deviceid", params[1]);
            datamap.put("longitude", params[2]);
            datamap.put("latitude", params[3]);
            datamap.put("b_C", params[4]);
            functionCalls.logStatus("mrcode: "+params[0]);
            functionCalls.logStatus("deviceid: "+params[1]);
            functionCalls.logStatus("longitude: "+params[2]);
            functionCalls.logStatus("latitude: "+params[3]);
            functionCalls.logStatus("b_C: "+params[4]);
            try {
                response = UrlPostConnection(BASE_URL+"Device_Location", datamap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            receivingData.mr_tracking_status(result, handler);
        }
    }
}
