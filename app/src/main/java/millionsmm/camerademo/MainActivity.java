package millionsmm.camerademo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_OPEN_ALBUM = 0;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_CROP_PHOTO = 2;
    private static final String TAG = "TestCamera";
    private File imageFile;
    private Uri takePhotoUri;
    private Uri cutUri;
    private ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img = findViewById(R.id.show_img);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
        }, 888);
    }

    public void testClick(View view) {
        switch (view.getId()) {
            case R.id.open_camara:
                openCamera();
                break;
            case R.id.select_from_album:
                openAlbum();
                break;
        }
    }

    private void openCamera() {
        String date = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
        String fileName = "photo_" + date;

        File path = new File(Environment.getExternalStorageDirectory() + "/myPhoto");

        if (!path.exists()) {
            path.mkdirs();
        }

        imageFile = new File(path, fileName + ".jpeg");

        takePhotoUri = getUriForFile(this, imageFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, takePhotoUri);

        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_OPEN_ALBUM);
    }

    private void cropPhoto(Uri uri, boolean fromCapture) {
//        打开系统自带的裁剪功能
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        intent.setDataAndType(uri, "image/*");
        intent.putExtra("scale", true);

        // 设置裁剪区域的宽高比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // 设置裁剪区域的宽度和高度
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);

        // 取消人脸识别
        intent.putExtra("noFaceDetection", true);
//        图片输出格式
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

        intent.putExtra("return-data", false);

        if (fromCapture) {
            cutUri = Uri.fromFile(imageFile);
        } else {
            String date = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
            String fileName = "photo_" + date;


            File mCutFile = new File(Environment.getExternalStorageDirectory() + "/myPhoto", fileName + ".jpeg");
            if (!mCutFile.getParentFile().exists()) {
                mCutFile.getParentFile().mkdirs();
            }
            cutUri = Uri.fromFile(mCutFile);
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, cutUri);

        Intent updateAlbum = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        updateAlbum.setData(uri);
        this.sendBroadcast(intent);
        startActivityForResult(intent, REQUEST_CROP_PHOTO);
    }

    //    根据不同的版本以不同的方式获取Uri
    public Uri getUriForFile(Context context, File file) {
        if (context == null || file == null) {
            throw new NullPointerException();
        }

        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(context.getApplicationContext(), "com.rain.takephotodemo.FileProvider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_OPEN_ALBUM:
                    if (data != null) {
                        Log.d(TAG, "onActivityResult:aaa " + data.getData());
                        cropPhoto(data.getData(), false);
                    }
                    break;
                case REQUEST_TAKE_PHOTO:
                    if (data != null) {
                        cropPhoto(takePhotoUri, true);
                        Log.d(TAG, "onActivityResult:aaa " + takePhotoUri);
                    }
                    break;
                case REQUEST_CROP_PHOTO:
                    if (data != null) {
                        img.setImageURI(cutUri);
                    }
                    break;
            }
        }
    }
}