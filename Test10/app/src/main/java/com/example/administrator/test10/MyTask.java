package com.example.administrator.test10;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyTask extends AsyncTask<String,Integer,Integer>{
    public static final int TYPE_SUCCESS=0;
    public static final int TYPE_FAILED=1;
    public static final int TYPE_PAUSED=2;
    public static final int TYPE_CANCELED=3;
    private boolean isCanceled=false;
    private boolean isPaused=false;
    DownloadListener listener;
    private long downLength;
    private File file;
    private InputStream in;
    private RandomAccessFile saveFile;
    private int lastProgress;

    public MyTask(DownloadListener listener) {
        this.listener=listener;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer){
            case TYPE_SUCCESS:
                listener.onSuccess();
                break;
            case TYPE_FAILED:
                listener.onError();
                break;
            case TYPE_PAUSED:
                listener.onPaused();
                break;
            case TYPE_CANCELED:
                listener.onCanceled();
                break;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int progress=values[0];
        if (progress>lastProgress){
            listener.onProgress(progress);
            lastProgress=progress;
        }
    }

    @Override
    protected Integer doInBackground(String... strings) {

        try {
            String address=strings[0];
            URL url=new URL(address);
            String fileName=address.substring(address.lastIndexOf("/"));
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+fileName);
            Log.d("result",fileName);
            if (file.exists()){
                downLength = file.length();
            }
            long contentLength=getContentLength(address);
            if (contentLength<=0){
                return TYPE_FAILED;
            }else if (downLength>=contentLength){
                return TYPE_SUCCESS;
            }
            OkHttpClient client=new OkHttpClient();
            Request request=new Request.Builder().url(new URL(address)).build();
            Response response=client.newCall(request).execute();
            if (response!=null){
                in = response.body().byteStream();
                saveFile = new RandomAccessFile(file,"rw");
                saveFile.seek(downLength);
                byte[] b=new byte[1024];
                int total = 0,len;
                while ((len= in.read(b))!=-1){
                    if (isCanceled){
                        return TYPE_CANCELED;
                    }else if (isPaused){
                        return TYPE_PAUSED;
                    }else {
                        total+=len;
                        saveFile.write(b,0,len);
                        int progress=(int)((total+downLength)*100/contentLength);
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
                try {
                    if (in!=null){
                        in.close();
                    }
                    if (saveFile!=null){
                        saveFile.close();
                    }
                    if (isCanceled&&file!=null){
                        file.delete();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

        }
        return TYPE_FAILED;
    }
    public long getContentLength(String address){
        try {
            long contentLength=0;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(new URL(address)).build();
            Response response=client.newCall(request).execute();
            if (response!=null&&response.isSuccessful()){
                contentLength=response.body().contentLength();
                response.body().close();
                return  contentLength;
            }else {
                return -1;
            }
        }catch (IOException e){
            Log.d("result",e.getMessage());
        }
        return 0;
    }
    public void pauseDownload(){
        isPaused=true;
    }
    public void cancelDownload(){
        isCanceled=true;
    }
}
