package com.example.administrator.test10;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private DownloadService.DownloadBind downloadBind;
    private ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            downloadBind= (DownloadService.DownloadBind) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        Intent intent=new Intent(this,DownloadService.class);
        startService(intent);
        bindService(intent,serviceConnection,BIND_AUTO_CREATE);

    }
    public void startDownload(View view){
        String url="https://raw.githubusercontent.com/guolindev/eclipse/master/eclipse-inst-win64.exe";
        downloadBind.startDownload(url);
    }
    public void pauseDownload(View view){
        downloadBind.pauseDownload();
    }
    public void cencalDownload(View view){
        downloadBind.cancelDownload();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }
}
