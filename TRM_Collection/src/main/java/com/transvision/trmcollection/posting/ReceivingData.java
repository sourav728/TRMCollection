package com.transvision.trmcollection.posting;

import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.transvision.trmcollection.values.FunctionCalls;
import com.transvision.trmcollection.values.GetSetValues;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.transvision.trmcollection.values.Constants.COLLECTION_DETAILS_FAILURE;
import static com.transvision.trmcollection.values.Constants.COLLECTION_DETAILS_SUCCESS;
import static com.transvision.trmcollection.values.Constants.COLLECTION_LOGIN_FAILURE;
import static com.transvision.trmcollection.values.Constants.COLLECTION_LOGIN_NOT_APPROVED;
import static com.transvision.trmcollection.values.Constants.COLLECTION_LOGIN_SUCCESS;
import static com.transvision.trmcollection.values.Constants.COLLECTION_POSTING_FAILURE;
import static com.transvision.trmcollection.values.Constants.COLLECTION_POSTING_PAID;
import static com.transvision.trmcollection.values.Constants.COLLECTION_POSTING_SUCCESS;
import static com.transvision.trmcollection.values.Constants.COLLECTION_REPORTS_FAILURE;
import static com.transvision.trmcollection.values.Constants.COLLECTION_REPORTS_SUCCESS;
import static com.transvision.trmcollection.values.Constants.MR_TRACKING_UPDATE;
import static com.transvision.trmcollection.values.Constants.MR_TRACKING_UPDATE_FAIL;
import static com.transvision.trmcollection.values.Constants.NON_REVENUE_HEAD;
import static com.transvision.trmcollection.values.Constants.NON_REVENUE_HEAD_KEY;
import static com.transvision.trmcollection.values.Constants.NON_REVENUE_HEAD_LIST_FAILURE;
import static com.transvision.trmcollection.values.Constants.NON_REVENUE_HEAD_LIST_SUCCESS;

public class ReceivingData {
    private FunctionCalls functionCalls = new FunctionCalls();

    private String parseServerXML(String result) {
        String value="";
        XmlPullParserFactory pullParserFactory;
        InputStream res;
        try {
            res = new ByteArrayInputStream(result.getBytes());
            pullParserFactory = XmlPullParserFactory.newInstance();
            pullParserFactory.setNamespaceAware(true);
            XmlPullParser parser = pullParserFactory.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(res, null);
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String name = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        switch (name) {
                            case "string":
                                value = parser.nextText();
                                break;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public void getCollection_login(String result, Handler handler, GetSetValues getSetValues) {
        result = parseServerXML(result);
        functionCalls.logStatus("Collection_Login: "+result);
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String message = jsonObject.getString("message");
                if (StringUtils.startsWithIgnoreCase(message, "Success!")) {
                    getSetValues.setColl_flag(jsonObject.getString("COLLFLAG"));//"Y"
                    if (StringUtils.startsWithIgnoreCase(jsonObject.getString("COLLFLAG"), "Y")) {
                        getSetValues.setColl_mrcode(jsonObject.getString("MRCODE"));
                        getSetValues.setColl_mrname(jsonObject.getString("MRNAME"));
                        getSetValues.setColl_subdiv_code(jsonObject.getString("SUBDIVCODE"));
                        getSetValues.setColl_start_time(functionCalls.convertTo24Hour(jsonObject.getString("Start_Time")));
                        getSetValues.setColl_end_time(functionCalls.convertTo24Hour(jsonObject.getString("End_Time")));
                        getSetValues.setColl_limit(jsonObject.getString("COLL_LIMIT"));
                        getSetValues.setColl_date(jsonObject.getString("COLLDATE").substring(0, 10));
                        if (!TextUtils.isEmpty(jsonObject.getString("RECPTNO")))
                            getSetValues.setColl_recpt_no(jsonObject.getString("RECPTNO"));
                        else getSetValues.setColl_recpt_no("0");
                        getSetValues.setColl_app_version(jsonObject.getString("CL_ANDR_VER"));
                        getSetValues.setColl_subdiv_name(jsonObject.getString("SUBDIVNAME"));
                        if (!TextUtils.isEmpty(jsonObject.getString("REVNONREV"))) {
                            getSetValues.setCollection_type(jsonObject.getString("REVNONREV"));
                        } else getSetValues.setCollection_type("0");
                        handler.sendEmptyMessage(COLLECTION_LOGIN_SUCCESS);
                    } else handler.sendEmptyMessage(COLLECTION_LOGIN_NOT_APPROVED);
                } else handler.sendEmptyMessage(COLLECTION_LOGIN_FAILURE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            handler.sendEmptyMessage(COLLECTION_LOGIN_FAILURE);
        }
    }

    public void getCollection_details(String result, Handler handler, GetSetValues getSetValues) {
        result = parseServerXML(result);
        functionCalls.logStatus("Collection Details: "+result);
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(result);
            if (StringUtils.startsWithIgnoreCase(jsonObject.getString("message"), "Success")) {
                getSetValues.setCustomer_name(jsonObject.getString("NAME"));
                getSetValues.setCustomer_rrno(jsonObject.getString("RRNO"));
                getSetValues.setCustomer_accid(jsonObject.getString("CONSNO"));
                getSetValues.setCustomer_tariff(jsonObject.getString("TARIFFNAME"));
                getSetValues.setCustomer_LF_no(jsonObject.getString("LF_NO"));
                getSetValues.setCustomer_bill_amount(jsonObject.getString("PAYABLE_AMOUNT"));
                if (!TextUtils.isEmpty(jsonObject.getString("CHQ_DISHONOUR_FLAG"))) {
                    getSetValues.setColl_chq_dishonour(jsonObject.getString("CHQ_DISHONOUR_FLAG"));
                } else getSetValues.setColl_chq_dishonour("N");
                handler.sendEmptyMessage(COLLECTION_DETAILS_SUCCESS);
            } else handler.sendEmptyMessage(COLLECTION_DETAILS_FAILURE);
        } catch (JSONException e) {
            e.printStackTrace();
            handler.sendEmptyMessage(COLLECTION_DETAILS_FAILURE);
        }
    }

    public void getCollection_posting_status(String result, Handler handler, GetSetValues getSetValues) {
        result = parseServerXML(result);
        functionCalls.logStatus("Collection Posting: "+result);
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(result);
            if (StringUtils.startsWithIgnoreCase(jsonObject.getString("message"), "Success")) {
                getSetValues.setPosting_unique_id(jsonObject.getString("Submessage"));
                getSetValues.setColl_posting_receipt_no(jsonObject.getString("Submessage1"));
                handler.sendEmptyMessage(COLLECTION_POSTING_SUCCESS);
            } else if (StringUtils.startsWithIgnoreCase(jsonObject.getString("message"), "Paid")) {
                handler.sendEmptyMessage(COLLECTION_POSTING_PAID);
            } else handler.sendEmptyMessage(COLLECTION_POSTING_FAILURE);
        } catch (JSONException e) {
            e.printStackTrace();
            handler.sendEmptyMessage(COLLECTION_POSTING_FAILURE);
        }
    }

    public void getCollection_non_revenue_status(String result, Handler handler, GetSetValues getSetValues) {
        result = parseServerXML(result);
        functionCalls.logStatus("Collection Posting: "+result);
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(result);
            if (StringUtils.startsWithIgnoreCase(jsonObject.getString("message"), "Success")) {
                getSetValues.setPosting_unique_id(jsonObject.getString("Submessage"));
                getSetValues.setColl_posting_receipt_no(jsonObject.getString("Submessage1"));
                handler.sendEmptyMessage(COLLECTION_POSTING_SUCCESS);
            } else if (StringUtils.startsWithIgnoreCase(jsonObject.getString("message"), "Paid")) {
                handler.sendEmptyMessage(COLLECTION_POSTING_PAID);
            } else handler.sendEmptyMessage(COLLECTION_POSTING_FAILURE);
        } catch (JSONException e) {
            e.printStackTrace();
            handler.sendEmptyMessage(COLLECTION_POSTING_FAILURE);
        }
    }

    public void getcollection_report_status(String result, Handler handler, GetSetValues getSetValues, ArrayList<GetSetValues> arrayList) {
        result = parseServerXML(result);
        functionCalls.logStatus("Collection Report: "+result);
        JSONArray jsonArray;
        int slno = 0;
        try {
            jsonArray = new JSONArray(result);
            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    getSetValues = new GetSetValues();
                    slno = slno + 1;
                    getSetValues.setColl_reports_slno(""+slno);
                    getSetValues.setColl_reports_acc_id(jsonObject.getString("CONSID"));
                    getSetValues.setColl_reports_recpt_no(jsonObject.getString("RECPTNO"));
                    getSetValues.setColl_reports_amount(jsonObject.getString("AMT"));
                    arrayList.add(getSetValues);
                }
                handler.sendEmptyMessage(COLLECTION_REPORTS_SUCCESS);
            } else handler.sendEmptyMessage(COLLECTION_REPORTS_FAILURE);
        } catch (JSONException e) {
            e.printStackTrace();
            handler.sendEmptyMessage(COLLECTION_REPORTS_FAILURE);
        }
    }

    public void getNon_revenue_header_status(String result, Handler handler, SharedPreferences.Editor editor) {
        result = parseServerXML(result);
        functionCalls.logStatus("Non Revenue Headers: "+result);
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("SELECT");
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(result);
            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    arrayList.add(jsonObject.getString("AC_HEAD_NAME"));
                }
                Gson gson = new Gson();
                List<String> textList = new ArrayList<>();
                textList.addAll(arrayList);
                String jsonText = gson.toJson(textList);
                editor.putString(NON_REVENUE_HEAD, "Yes");
                editor.putString(NON_REVENUE_HEAD_KEY, jsonText);
                editor.commit();
                handler.sendEmptyMessage(NON_REVENUE_HEAD_LIST_SUCCESS);
            } else handler.sendEmptyMessage(NON_REVENUE_HEAD_LIST_FAILURE);
        } catch (JSONException e) {
            e.printStackTrace();
            handler.sendEmptyMessage(NON_REVENUE_HEAD_LIST_FAILURE);
        }
    }

    public void mr_tracking_status(String result, Handler handler) {
        result = parseServerXML(result);
        functionCalls.logStatus("MR Tracking Status: "+result);
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(result);
            if (StringUtils.startsWithIgnoreCase(jsonObject.getString("message"), "Success")) {
                handler.sendEmptyMessage(MR_TRACKING_UPDATE);
            } else handler.sendEmptyMessage(MR_TRACKING_UPDATE_FAIL);
        } catch (JSONException e) {
            e.printStackTrace();
            handler.sendEmptyMessage(MR_TRACKING_UPDATE_FAIL);
        }
    }
}
