package com.example.renda;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

public abstract class AsyncTaskWithDialog<Result> extends AsyncTask<Void, Void, Result>{
    
    private Activity activity;
    private ProgressDialog progressDialog;
    
    public AsyncTaskWithDialog(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        // ロード中のダイアログを表示
        progressDialog = new ProgressDialog(this.activity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("ロード中");
        progressDialog.show();
    }
    
    abstract void onPostExecuteWithDismissDialog(Result result);
    
    protected void onPostExecute(Result result) {
        // 終了処理
        onPostExecuteWithDismissDialog(result);
        // ロード中のダイアログを終了
        progressDialog.dismiss();
    }
}
