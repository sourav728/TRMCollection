package com.transvision.trmcollection.posting;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import com.transvision.trmcollection.values.FunctionCalls;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.transvision.trmcollection.values.Constants.APK_FILE_DOWNLOADED;
import static com.transvision.trmcollection.values.Constants.APK_FILE_DOWNLOAD_ERROR;
import static com.transvision.trmcollection.values.Constants.APK_FILE_NOT_FOUND;
import static com.transvision.trmcollection.values.Constants.COLLECTION_UPLOAD_ERROR;
import static com.transvision.trmcollection.values.Constants.COLLECTION_UPLOAD_FAILURE;
import static com.transvision.trmcollection.values.Constants.COLLECTION_UPLOAD_SUCCESS;
import static com.transvision.trmcollection.values.Constants.FTP_HOST;
import static com.transvision.trmcollection.values.Constants.FTP_PASS;
import static com.transvision.trmcollection.values.Constants.FTP_PORT;
import static com.transvision.trmcollection.values.Constants.FTP_USER;

public class FTPApi {
    private FunctionCalls functionCalls = new FunctionCalls();

    public class Collection_Uploadfile extends AsyncTask<String, String, String> {
        FileInputStream fis = null;
        boolean result = false;
        Context context;
        Handler handler;
        boolean collection_upload = false;
        String mobileFilePathUplod="", file_name_UP="", fileZipFormat="", serverUploadFilePath="";

        public Collection_Uploadfile(Context context, Handler handler) {
            this.context = context;
            this.handler = handler;
        }

        @Override
        protected String doInBackground(String... params) {

            mobileFilePathUplod = params[0];
            file_name_UP = params[1];
            fileZipFormat = params[2];
            serverUploadFilePath = params[3];

            functionCalls.logStatus("Collection_Upload Upload Started");
            FTPClient client = new FTPClient();
            functionCalls.logStatus("Collection_Upload 1");
            try {
                functionCalls.logStatus("Collection_Upload 2");
                client.connect(FTP_HOST, FTP_PORT);
                functionCalls.logStatus("Collection_Upload 3");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            try {
                functionCalls.logStatus("Collection_Upload 4");
                collection_upload = client.login(FTP_USER, FTP_PASS);
                functionCalls.logStatus("Collection_Upload 5");
            } catch (FTPConnectionClosedException e) {
                e.printStackTrace();
                try {
                    collection_upload = false;
                    client.disconnect();
                    handler.sendEmptyMessage(COLLECTION_UPLOAD_ERROR);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (collection_upload) {
                functionCalls.logStatus("Collection_Upload_6_Upload true");
                try {
                    functionCalls.logStatus("Collection_Upload 7");
                    client.setFileType(FTP.BINARY_FILE_TYPE);
                    functionCalls.logStatus("Collection_Upload 8");
                    client.enterLocalPassiveMode();
                    functionCalls.logStatus("Collection_Upload 9");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try {
                    functionCalls.logStatus("Collection_Upload 10");
                    client.changeWorkingDirectory(serverUploadFilePath);
                    functionCalls.logStatus("Collection_Upload 11");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try {
                    functionCalls.logStatus("Collection_Upload 12");
                    File file = new File(mobileFilePathUplod + file_name_UP + fileZipFormat);
                    functionCalls.logStatus("Collection_Upload 13");
                    String testName = file.getName();
                    functionCalls.logStatus("Collection_Upload 14");
                    fis = new FileInputStream(file);
                    functionCalls.logStatus("Collection_Upload 15");
                    ////////////////------------------ Upload billing_file to the FTP server............\\\\\\\\\\\\\\\\\\\\\\\
                    result = client.storeFile(testName, fis);
                    functionCalls.logStatus("Collection_Upload 16");
                    client.logout();
                    collection_upload = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (result)
                handler.sendEmptyMessage(COLLECTION_UPLOAD_SUCCESS);
            else handler.sendEmptyMessage(COLLECTION_UPLOAD_FAILURE);
            return null;
        }
    }

    public class Download_apk  extends AsyncTask<String, String, String> {
        boolean dwnldCmplt=false, downloadapk=false;
        Handler handler;
        FileOutputStream fos = null;
        String mobilepath = functionCalls.filepath("ApkFolder") + File.separator;
        String update_version="";

        public Download_apk(Handler handler, String update_version) {
            this.handler = handler;
            this.update_version = update_version;
        }

        @Override
        protected String doInBackground(String... params) {
            functionCalls.logStatus("Collection_Apk 1");
            FTPClient ftp_1 = new FTPClient();
            functionCalls.logStatus("Collection_Apk 2");
            try {
                functionCalls.logStatus("Collection_Apk 3");
                ftp_1.connect(FTP_HOST, FTP_PORT);
                functionCalls.logStatus("Collection_Apk 4");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                functionCalls.logStatus("Collection_Apk 5");
                ftp_1.login(FTP_USER, FTP_PASS);
                downloadapk = ftp_1.login(FTP_USER, FTP_PASS);
                functionCalls.logStatus("Collection_Apk 6");
            } catch (FTPConnectionClosedException e) {
                e.printStackTrace();
                try {
                    downloadapk = false;
                    ftp_1.disconnect();
                    handler.sendEmptyMessage(APK_FILE_DOWNLOAD_ERROR);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (downloadapk) {
                functionCalls.logStatus("Collection Apk download billing_file true");
                try {
                    functionCalls.logStatus("Collection_Apk 7");
                    ftp_1.setFileType(FTP.BINARY_FILE_TYPE);
                    ftp_1.enterLocalPassiveMode();
                    functionCalls.logStatus("Collection_Apk 8");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    functionCalls.logStatus("Collection_Apk 9");
                    ftp_1.changeWorkingDirectory("/Android/Apk/");
                    functionCalls.logStatus("Collection_Apk 10");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    functionCalls.logStatus("Collection_Apk 11");
                    FTPFile[] ftpFiles = ftp_1.listFiles("/Android/Apk/");
                    functionCalls.logStatus("Collection_Apk 12");
                    int length = ftpFiles.length;
                    functionCalls.logStatus("Collection_Apk 13");
                    functionCalls.logStatus("All_Apk_length = " + length);
                    for (int i = 0; i < length; i++) {
                        String namefile = ftpFiles[i].getName();
                        functionCalls.logStatus("Apk_namefile : " + namefile);
                        boolean isFile = ftpFiles[i].isFile();
                        if (isFile) {
                            functionCalls.logStatus("Collection_Apk_File: " + "TRM_Collection_"+update_version+".apk");
                            if (namefile.equals("TRM_Collection_"+update_version+".apk")) {
                                functionCalls.logStatus("Collection_Apk File found to download");
                                try {
                                    fos = new FileOutputStream(mobilepath + "TRM_Collection_"+update_version+".apk");
                                }
                                catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    dwnldCmplt = ftp_1.retrieveFile("/Android/Apk/" + "TRM_Collection_"+update_version+".apk", fos);
                                }
                                catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            downloadapk = false;
            try {
                ftp_1.logout();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (dwnldCmplt)
                handler.sendEmptyMessage(APK_FILE_DOWNLOADED);
            else handler.sendEmptyMessage(APK_FILE_NOT_FOUND);
        }
    }
}
