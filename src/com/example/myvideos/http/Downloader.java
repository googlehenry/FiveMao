package com.example.myvideos.http;

import java.io.File;
import java.io.IOException;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import com.ex.fivemao.exception.CrashHandler;

/**
 * Created by 49829 on 2017/8/28.
 */

public class Downloader {
    //������
    private DownloadManager downloadManager;
    //������
    private Context mContext;
    //���ص�ID
    private long downloadId;

    public Downloader(Context context) {
        this.mContext = context;
    }
    

    //����apk
    public void downloadAPK(String url, String name) {

        //������������
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        //�ƶ�����������Ƿ���������
        request.setAllowedOverRoaming(false);

        //��֪ͨ������ʾ��Ĭ�Ͼ�����ʾ��
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setTitle("��ëӰ��APP");
        request.setDescription("��������...");
        request.setVisibleInDownloadsUi(true);

        //�������ص�·��
        request.setDestinationInExternalPublicDir(Environment.getExternalStorageDirectory().getAbsolutePath()+"/FiveMao", name);

        //��ȡDownloadManager
        downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        //����������������ض��У��������ض��к��������񷵻�һ��long�͵�id��ͨ����id����ȡ�������������񡢻�ȡ���ص��ļ��ȵ�
        downloadId = downloadManager.enqueue(request);

        //ע��㲥�����ߣ���������״̬
        mContext.registerReceiver(receiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    //�㲥�������صĸ���״̬
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkStatus();
        }
    };


    //�������״̬
    private void checkStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        //ͨ�����ص�id����
        query.setFilterById(downloadId);
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                //������ͣ
                case DownloadManager.STATUS_PAUSED:
                    break;
                //�����ӳ�
                case DownloadManager.STATUS_PENDING:
                    break;
                //��������
                case DownloadManager.STATUS_RUNNING:
                    break;
                //�������
                case DownloadManager.STATUS_SUCCESSFUL:
                    //������ɰ�װAPK
                    installAPK();
                    break;
                //����ʧ��
                case DownloadManager.STATUS_FAILED:
                    break;
            }
        }
    }

    private void setPermission(String filePath)  {
        String command = "chmod " + "777" + " " + filePath;
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec(command);
        } catch (IOException e) {
            CrashHandler.logErrorToFile(e);
        }
    }
    
    //���ص����غ�ִ�а�װ
    private void installAPK() {

        File apkFile = queryDownloadedApk();
        setPermission(apkFile.getAbsolutePath());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        //�汾��7.0�����ǲ���ֱ��ͨ��uri���ʵ�
        if (Build.VERSION.SDK_INT >= 24) {
            File file = (apkFile);
            // ����û����Activity����������Activity,��������ı�ǩ
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //����1 ������, ����2 Provider������ַ �������ļ��б���һ��   ����3  ������ļ�
            Uri apkUri = FileProvider.getUriForFile(mContext, "com.ex.fivemao.fileProvider", file);
            //�����һ���ʾ��Ŀ��Ӧ����ʱ��Ȩ��Uri��������ļ�
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile),
                    "application/vnd.android.package-archive");
        }
        mContext.startActivity(intent);

    }



    public File queryDownloadedApk() {
        File targetApkFile = null;
        if (downloadId != -1) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
            Cursor cur = downloadManager.query(query);
            if (cur != null) {
                if (cur.moveToFirst()) {
                    String uriString = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    if (!uriString.isEmpty()) {
                        targetApkFile = new File(Uri.parse(uriString).getPath());
                    }
                }
                cur.close();
            }
        }
        return targetApkFile;
    }

}