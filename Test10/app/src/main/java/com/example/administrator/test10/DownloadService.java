package com.example.administrator.test10;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.io.File;

public class DownloadService extends Service {
    private MyTask downloadTask;
    private String downloadUrl;

    private DownloadListener downloadListener=new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            NotificationManager notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(1,getNotification("Downloading...",progress));
        }

        @Override
        public void onSuccess() {
            downloadTask=null;
            stopForeground(true);
            NotificationManager notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(1,getNotification("Downloading Success",-1));
        }

        @Override
        public void onError() {
            downloadTask=null;
            stopForeground(true);
            NotificationManager notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(1,getNotification("Downloading Failed",-1));
        }

        @Override
        public void onPaused() {
            downloadTask=null;
        }

        @Override
        public void onCanceled() {
            downloadTask=null;
            stopForeground(true);
        }
    };

    class DownloadBind extends Binder{
        public void startDownload(String url){
            if (downloadTask==null){
                downloadUrl=url;
                downloadTask=new MyTask(downloadListener);
                downloadTask.execute(downloadUrl);
                startForeground(1,getNotification("Downloading...",0));

            }
        }
        public void pauseDownload(){
            if (downloadTask!=null){
                downloadTask.pauseDownload();
            }
        }
        public void cancelDownload(){
            if (downloadTask!=null){
                downloadTask.cancelDownload();
            }else {
                if (downloadUrl!=null){
                    String fileName=downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String director= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file=new File(director+fileName);
                    if (file.exists()){
                        file.delete();
                    }
                    NotificationManager notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.cancel(1);
                    stopForeground(true);
                }
            }
        }
    }
    DownloadBind downloadBind=new DownloadBind();
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return downloadBind;
    }
    private Notification getNotification(String title, int progress){
        Intent intent=new Intent(this,MainActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        builder.setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        if (progress>0){
            builder.setContentText(progress+"%");
            builder.setProgress(100,progress,false);
        }
        return builder.build();
    }
}
