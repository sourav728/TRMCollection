package com.transvision.trmcollection.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.transvision.trmcollection.values.FunctionCalls;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.io.IOException;

public class TRMCollection_Database {
    private MyHelper mh ;
    private SQLiteDatabase sdb ;
    private FunctionCalls functionCalls = new FunctionCalls();
    private String dbpath ="", dbfolder="databases", db_name="collection.db";
    private File databasefile = null;

    public TRMCollection_Database(Context context) {
        try {
            databasefile = functionCalls.filestorepath(dbfolder, db_name);
            if (databasefile.exists())
                functionCalls.logStatus("TRM Collection Database Exists");
            else functionCalls.logStatus("TRM Collection Database Not Exists");
            dbpath = functionCalls.filepath(dbfolder) + File.separator + db_name;
            mh = new MyHelper(context, dbpath, null, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void open() {
        sdb = mh.getWritableDatabase();
    }

    public void close() {
        sdb.close();
    }

    public class MyHelper extends SQLiteOpenHelper {
        public MyHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("Create table COLLECTION_OUTPUT(_id integer primary key, " +
                    "RRNO TEXT, ACCOUNT_ID TEXT, LF_NO TEXT, NAME TEXT, AMOUNT TEXT, MODE_PAYMENT TEXT, RECEIPT_NO TEXT, TRANSACTION_ID TEXT, " +
                    "MACHINE_ID TEXT, RECEIPT_DATE_TIME TEXT, RECEIPT_DATE TEXT, MR_CODE TEXT, GPS_LAT TEXT, GPS_LONG TEXT, UNIQUE_ID TEXT, " +
                    "ROLE TEXT);");
            db.execSQL("Create table RECEIPT_NO(_id integer primary key, RECPT_NO TEXT);");
            db.execSQL("Create table COLLECTION_DATE(_id integer primary key, COLL_DATE TEXT);");
            db.execSQL("Create table PRINTER_CONN(_id integer primary key, PRINTER TEXT);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    public void insert_collection_details(ContentValues cv) {
        sdb.insert("COLLECTION_OUTPUT", null, cv);
    }

    public void update_collection_details(String account_id, String receipt_no, String unique_id, String receipt_time, String receipt_date) {
        Cursor data = null;
        data = sdb.rawQuery("UPDATE COLLECTION_OUTPUT SET RECEIPT_NO='"+receipt_no+"' and RECEIPT_DATE_TIME='"+receipt_time+"' " +
                "and UNIQUE_ID='"+unique_id+"' WHERE RECEIPT_DATE='"+receipt_date+"' and ACCOUNT_ID='"+account_id+"'", null);
        data.moveToNext();
        data.close();
    }

    public void delete_collection_record(String account_id, String receipt_date) {
        Cursor data = null;
        data = sdb.rawQuery("DELETE FROM COLLECTION_OUTPUT WHERE ACCOUNT_ID='"+account_id+"' and RECEIPT_DATE='"+receipt_date+"'", null);
        data.moveToNext();
        data.close();
    }

    public Cursor collection_output() {
        Cursor data = null;
        data = sdb.rawQuery("SELECT * FROM COLLECTION_OUTPUT", null);
        return data;
    }

    public Cursor collection_output_by_receipt_date(String receipt_date) {
        Cursor data = null;
        data = sdb.rawQuery("SELECT * FROM COLLECTION_OUTPUT WHERE RECEIPT_DATE ="+"'"+receipt_date+"'", null);
        return data;
    }

    public Cursor collection_output_by_receipt_date() {
        Cursor data = null;
        data = sdb.rawQuery("SELECT * FROM COLLECTION_OUTPUT", null);
        return data;
    }

    public boolean check_collection_by_account_id_on_date(String account_id, String receipt_date) {
        Cursor data = null;
        data = sdb.rawQuery("SELECT * FROM COLLECTION_OUTPUT WHERE ACCOUNT_ID = '"+account_id+"' and RECEIPT_DATE = '"+receipt_date+"'", null);
        if (data.getCount() > 0) {
            data.close();
            return false;
        } else {
            data.close();
            return true;
        }
    }

    public boolean check_receipt_time(String pres_receipt_time) {
        Cursor data = null;
        data = sdb.rawQuery("select * from COLLECTION_OUTPUT", null);
        if (data.getCount() > 0) {
            data.moveToNext();
            String last_receipt = data.getString(data.getColumnIndex("RECEIPT_DATE_TIME"));
            if (functionCalls.compare_receipt_times(last_receipt, pres_receipt_time)) {
                data.close();
                return true;
            } else {
                data.close();
                return false;
            }
        } else {
            data.close();
            return true;
        }
    }

    public Cursor collection_output_by_Role(String role) {
        Cursor data = null;
        data = sdb.rawQuery("SELECT * FROM COLLECTION_OUTPUT WHERE ROLE ="+"'"+role+"'", null);
        return data;
    }

    public Cursor addingrecptno(String role) {
        Cursor data = null;
        data = sdb.rawQuery("select MAX(RECEIPT_NO)RECEIPT_NO from COLLECTION_OUTPUT WHERE ROLE ="+"'"+role+"'", null);
        return data;
    }

    public Cursor countfortransaction(String date, String role) {
        Cursor data = null;
        data = sdb.rawQuery("select count(RECEIPT_DATE)COUNT, TRANSACTION_ID from COLLECTION_OUTPUT where RECEIPT_DATE = " + "'" + date + "' and ROLE ="+"'"+role+"'", null);
        return data;
    }

    public void insert_collection_receipt_no(String receipt_no) {
        ContentValues cv = new ContentValues();
        cv.put("RECPT_NO", receipt_no);
        Cursor data = getCollection_receipt();
        if (data.getCount() > 0)
            sdb.update("RECEIPT_NO", cv, null, null);
        else sdb.insert("RECEIPT_NO", null, cv);
    }

    public void insert_collection_date(String date) {
        ContentValues cv = new ContentValues();
        cv.put("COLL_DATE", date);
        Cursor data = getCollection_date();
        if (data.getCount() > 0)
            sdb.update("COLLECTION_DATE", cv, null, null);
        else sdb.insert("COLLECTION_DATE", null, cv);
    }

    public Cursor getCollection_receipt() {
        Cursor data = null;
        data = sdb.rawQuery("SELECT * FROM RECEIPT_NO", null);
        return data;
    }

    public Cursor getCollection_date() {
        Cursor data = null;
        data = sdb.rawQuery("SELECT * FROM COLLECTION_DATE", null);
        return data;
    }

    public void printer_details(String printer) {
        ContentValues cv = new ContentValues();
        cv.put("PRINTER", printer);
        Cursor data = getPrinter_details();
        if (data.getCount() > 0)
            sdb.update("PRINTER_CONN", cv, null, null);
        else sdb.insert("PRINTER_CONN", null, cv);
    }

    public Cursor getPrinter_details() {
        Cursor data = null;
        data = sdb.rawQuery("SELECT * FROM PRINTER_CONN", null);
        return data;
    }

    //===========================//Converting Database to file format//=====================================//
    public void copyDBtoSD(String sdFilePath, String sdFilename, String format) throws IOException {
        mh.getReadableDatabase();
        try {
            //zipPath is absolute path of zipped file
            ZipFile zipFile = new ZipFile(sdFilePath + sdFilename + format);
            //filePath is absolute path of file that want to be zip
            File fileToAdd = new File(db_name);
            //create zip parameters such a password, encryption method, etc
            ZipParameters parameters = new ZipParameters();
            ZipParameters parameters1 = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
            parameters.setPassword("12345");
            parameters.setFileNameInZip(sdFilename + ".db");
            parameters.setSourceExternalStream(true);
            zipFile.addFile(fileToAdd, parameters);
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

    public Cursor summart4(String receipt_date) {
        Cursor c18 = null;
        c18 = sdb.rawQuery("select null MODE_PAYMENT, 'Start Recpt No : ' || min(RECEIPT_NO) mode1,'End   Recpt No : ' || max(RECEIPT_NO) Total_Receipts,null Net_Amount from COLLECTION_OUTPUT WHERE RECEIPT_DATE ="+"'"+receipt_date+"'", null);
        return c18;
    }

    public Cursor summart4() {
        Cursor c18 = null;
        c18 = sdb.rawQuery("select null MODE_PAYMENT, 'Start Recpt No : ' || min(RECEIPT_NO) mode1,'End   Recpt No : ' || max(RECEIPT_NO) Total_Receipts,null Net_Amount from COLLECTION_OUTPUT", null);
        return c18;
    }

    public Cursor summart1() {
        Cursor c15 = null;
        c15 = sdb.rawQuery("SELECT null MODE_PAYMENT, 'Printed on Date Time : ' || datetime(CURRENT_TIMESTAMP,'localtime')mode1 ,null Total_Receipts,null Net_Amount", null);
        return c15;
    }
}
