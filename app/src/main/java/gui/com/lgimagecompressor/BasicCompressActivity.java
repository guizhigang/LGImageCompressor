package gui.com.lgimagecompressor;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BasicCompressActivity extends AppCompatActivity implements LGImgCompressor.CompressListener{
    private final String TAG = MainActivity.class.getSimpleName();
    private ImageView imageView;
    private TextView imageInfo;
    private final static int CAMERA_REQESTCODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_compress);

        imageInfo = (TextView) findViewById(R.id.image_info);
        imageView = (ImageView) findViewById(R.id.image_view);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });
    }

    //处理6.0动态权限问题
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    CAMERA_REQESTCODE);
        } else {
            takePictureFormCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_REQESTCODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePictureFormCamera();
            } else {
                Toast.makeText(this, "需要允许写入权限来存储图片", Toast.LENGTH_LONG).show();
            }
        }
    }

    private File imageFile;

    private void takePictureFormCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String fileName = timeStamp + "_";
        File fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        imageFile = null;
        try {
            imageFile = File.createTempFile(fileName, ".jpg", fileDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        startActivityForResult(intent, CAMERA_REQESTCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQESTCODE) {
                LGImgCompressor.getInstance(this).withListener(this).
                        starCompress(Uri.fromFile(imageFile).toString(), 600, 800, 100);
//                LGImgCompressor.getInstance(this).withListener(this).
//                        starCompressWithDefault(Uri.fromFile(imageFile).toString());
            }
        }
    }

    @Override
    public void onCompressStart() {
        Log.d(TAG, "onCompressStart");
    }

    @Override
    public void onCompressEnd(LGImgCompressor.CompressResult compressResult) {
        Log.d(TAG, "onCompressEnd outPath:" + compressResult.getOutPath());
        if (compressResult.getStatus() == LGImgCompressor.CompressResult.RESULT_ERROR)//压缩失败
            return;

        File file = new File(compressResult.getOutPath());
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));
            imageView.setImageBitmap(bitmap);
            float imageFileSize = file.length() / 1024f;
            imageInfo.setText("image info width:" + bitmap.getWidth() + " \nheight:" + bitmap.getHeight() +
                    " \nsize:" + imageFileSize + "kb" + "\nimagePath:" + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
