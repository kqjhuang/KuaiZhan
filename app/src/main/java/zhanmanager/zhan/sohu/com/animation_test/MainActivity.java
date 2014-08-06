package zhanmanager.zhan.sohu.com.animation_test;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends Activity {
    private ImageButton btn_left;
    private ImageButton btn_refresh;
    private ImageButton btn_home;
    private ProgressBar progressbar;
    private WebView browser;
    private static final String errorHtml = "file:///android_asset/www/index.html";
    private FrameLayout splash_layout;
    private Boolean is_clear_history;
    private Boolean is_refresh = false;
    private static final int STOPSPLASH = 0;
    private static final long SPLASHTIME = 2000;
    private String home_page = "http://www.kuaizhan.com/";
    public static final int FILECHOOSER_RESULTCODE = 1;
    private static final int REQ_CAMERA = FILECHOOSER_RESULTCODE + 1;
    private static final int REQ_CHOOSE = REQ_CAMERA + 1;
    private static final int PAGE_TIME_OUT = 15000;
    private Uri mCameraUri;

    private ValueCallback<Uri> mUploadMessage;
    private String mCompressPath;
    private String mImagePath;
    private ProgressDialog mCompressWaitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        mCompressPath = FileUtil.getSdcardCacheDir() + File.separator + "app_compress.jpg";
        is_clear_history = false;
        progressbar = (ProgressBar) findViewById(R.id.processbar);
        splash_layout = (FrameLayout) findViewById(R.id.splash_layout);
        btn_left = (ImageButton) findViewById(R.id.goLeft);
        btn_left.setOnClickListener(btn_goLeft_listener);
        btn_refresh = (ImageButton) findViewById(R.id.refresh);
        btn_home = (ImageButton) findViewById(R.id.home);
        btn_refresh.setOnClickListener(btn_refresh_listener);
        btn_home.setOnClickListener(btn_home_listener);
        web_init();
        Message msg = new Message();
        msg.what = STOPSPLASH;
        splashHandler.sendMessageDelayed(msg, SPLASHTIME);
        browser.loadUrl(home_page);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null != mUploadMessage) {
            if (resultCode == Activity.RESULT_OK && requestCode == REQ_CAMERA) {
                afterOpenCamera(REQ_CAMERA);
            } else if (resultCode == Activity.RESULT_OK && requestCode == REQ_CHOOSE) {
                afterChoicePic(data, REQ_CHOOSE);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    private void web_init() {
        browser = (WebView) findViewById(R.id.webview);
        WebSettings browser_st = browser.getSettings();
        browser_st.setJavaScriptEnabled(true);
        browser_st.setSupportZoom(true);
        browser_st.setCacheMode(WebSettings.LOAD_NO_CACHE);
        browser_st.setDefaultTextEncodingName("UTF-8");
        browser_st.setDomStorageEnabled(true);
        browser.setWebViewClient(web_viewClient);
        browser.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        browser.setWebChromeClient(new MyWebChromeClient());
    }

    private WebViewClient web_viewClient = new WebViewClient() {
        boolean timeout = true;

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!getContext(url)) {
                view.loadUrl(url);
                return false;
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
            return true;
        }

        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            view.loadUrl(errorHtml);
            super.onReceivedError(view, errorCode, description, failingUrl);
            is_refresh = true;
            //这里进行无网络或错误处理，具体可以根据errorCode的值进行判断，做跟详细的处理。
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(PAGE_TIME_OUT);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (timeout) {
                        browser.loadUrl(errorHtml);
                        is_refresh = true;
                    }
                }
            }).start();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (is_clear_history) {
                is_clear_history = false;
                browser.clearHistory();//清楚历史记录
            }
            timeout = false;
            super.onPageFinished(view, url);
        }
    };
    private Handler splashHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STOPSPLASH:
                    splash_layout.setVisibility(View.GONE);//关闭启动图
                    break;
            }
            super.handleMessage(msg);
        }
    };
    //页面回退按键响应函数
    private ImageButton.OnClickListener btn_goLeft_listener = new ImageButton.OnClickListener() {
        public void onClick(View v) {
            browser.stopLoading();
            if (browser.canGoBack()) {
                browser.goBack();
            }
        }
    };
    //页面刷新按键响应函数
    private ImageButton.OnClickListener btn_refresh_listener = new ImageButton.OnClickListener() {
        public void onClick(View v) {
            if (is_refresh && browser.canGoBack()) {
                browser.clearCache(false);
                browser.goBack();
                is_refresh = false;
            } else if (is_refresh) {
                browser.reload();
                is_refresh = false;
            } else {
                browser.reload();
            }
        }
    };
    //主页按键响应函数
    private ImageButton.OnClickListener btn_home_listener = new ImageButton.OnClickListener() {
        public void onClick(View v) {
            browser.loadUrl(home_page);
            is_clear_history = true;

        }
    };

    //Url判断，是否以.apk结尾
    private boolean getContext(String matcher) {
        // TODO Auto-generated method stub
        String rxz = "^.*\\.apk$";
        Pattern rxzApk = Pattern.compile(rxz);
        Matcher m;
        m = rxzApk.matcher(matcher);
        if (m.find()) {
            Log.i("TAG", "true");
            return true;
        } else {
            Log.i("TAG", "false");
            return false;
        }

    }

    private class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                progressbar.setVisibility(view.GONE);
            } else {
                if (progressbar.getVisibility() == view.GONE)
                    progressbar.setVisibility(view.VISIBLE);
                progressbar.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }

        // For Android 3.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;
            selectImage();
        }

        // For Android < 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, "");
        }

        // For Android > 4.1.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            openFileChooser(uploadMsg, acceptType);
        }

    }

    private boolean checkSDcard() {
        boolean flag = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (!flag) {
            Toast.makeText(this, "未检测到手机存储卡", Toast.LENGTH_SHORT).show();
        }
        return flag;
    }

    private void selectImage() {
        if (!checkSDcard()) {
            return;
        }
        String[] selectPicTypeStr = {"拍照上传", "本地上传"};
        new AlertDialog.Builder(this).setItems(selectPicTypeStr,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            // 相机拍摄
                            case 0:
                                openCarcme();
                                break;
                            // 手机相册
                            case 1:
                                choicePic();
                                break;
                            default:
                                break;
                        }
                    }
                }
        ).show();
    }

    private void openCarcme() {
        mImagePath = FileUtil.getSdcardCacheDir() + File.separator + (System.currentTimeMillis() + ".jpg");
        File file = new File(mImagePath);
        if (!file.exists()) {
            File filePath = file.getParentFile();
            filePath.mkdirs();
        } else {
            if (file.exists()) {
                file.delete();
            }
        }
        mCameraUri = Uri.fromFile(file);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraUri);
        startActivityForResult(intent, REQ_CAMERA);
    }

    private void afterOpenCamera(int code) {
        new CompressTask().execute(code);

        addImageGallery(new File(mImagePath));
    }

    private void choicePic() {
        Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        innerIntent.setType("image/*");
        Intent wrapperIntent = Intent.createChooser(innerIntent, null);
        startActivityForResult(wrapperIntent, REQ_CHOOSE);
    }

    private void afterChoicePic(Intent data, int code) {
        if (TextUtils.indexOf(data.getData().toString(), "com.android.providers.media.documents") != -1) {
            mImagePath = getRealPathFromURI4_4(this, data.getData());
        } else {
            mImagePath = getRealPathFromURI(this, data.getData());
        }

        new CompressTask().execute(code);
    }

    private void addImageGallery(File file) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public String getRealPathFromURI4_4(Context context, Uri contentUri) {
        Cursor cursor = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        // Will return "image:x*"
        String wholeID = DocumentsContract.getDocumentId(contentUri);
        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];
        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";
        cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, sel, new String[]{id}, null);
        try {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        try {
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private class CompressTask extends AsyncTask<Integer, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mCompressWaitDialog == null) {
                mCompressWaitDialog = new ProgressDialog(MainActivity.this);
                mCompressWaitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mCompressWaitDialog.setCancelable(false);
                mCompressWaitDialog.setMessage("载入中...");
            }
            mCompressWaitDialog.show();
        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            FileUtils.delFile(mCompressPath);

            FileUtils.compressFile(mImagePath, mCompressPath);
            return integers[0];
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if (mCompressWaitDialog != null && mCompressWaitDialog.isShowing()) {
                mCompressWaitDialog.dismiss();
            }
            if (mUploadMessage != null) {
                Log.i("TAG", mCompressPath);
                mUploadMessage.onReceiveValue(Uri.fromFile(new File(mCompressPath)));
                mUploadMessage = null;
            }
        }
    }

}
