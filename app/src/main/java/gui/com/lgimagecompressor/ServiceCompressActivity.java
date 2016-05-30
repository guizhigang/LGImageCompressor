package gui.com.lgimagecompressor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class ServiceCompressActivity extends AppCompatActivity {
    private final String TAG = ServiceCompressActivity.class.getSimpleName();

    private long serviceStartTime;
    private CompressingReciver reciver;
    private TextView infoView;

    private class CompressingReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive:" + Thread.currentThread().getId());
            int flag = intent.getIntExtra(Constanse.KEY_COMPRESS_FLAG,-1);
            Log.d(TAG," flag:" + flag);
            if(flag == Constanse.FLAG_BEGAIIN){
                Log.d(TAG, "onCompressServiceStart");
                serviceStartTime = System.currentTimeMillis();
                updateInfo("compress begain...");
                return;
            }

            if(flag == Constanse.FLAG_END){
                ArrayList<LGImgCompressor.CompressResult> compressResults =
                        (ArrayList<LGImgCompressor.CompressResult>)intent.getSerializableExtra(Constanse.KEY_COMPRESS_RESULT);
                Log.d(TAG, compressResults.size() + "compressed done");
                Log.d(TAG, "compress " + compressResults.size() + " files used total time:" + (System.currentTimeMillis() - serviceStartTime));
                updateInfo(compressResults.size() + " files compressed done \nused total time:" + (System.currentTimeMillis() - serviceStartTime) + "ms");
            }
        }
    }

    private void updateInfo(String message){
        infoView.setText(message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent_service_compress);

        reciver = new CompressingReciver();
        IntentFilter intentFilter = new IntentFilter(Constanse.ACTION_COMPRESS_BROADCAST);
        registerReceiver(reciver, intentFilter);

        infoView = (TextView)findViewById(R.id.compress_info);
        final int maxSize = 0;

        findViewById(R.id.intent_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Uri> compressFiles = getImagesPathFormAlbum();
                Log.d(TAG, compressFiles.size() + "compresse begain");
                int size = compressFiles.size() > 10 ? 10:compressFiles.size();
                for (int i = 0; i < compressFiles.size(); ++i) {
                    Uri uri = compressFiles.get(i);
                    CompressServiceParam param = new CompressServiceParam();
                    param.setOutHeight(800);
                    param.setOutWidth(600);
                    param.setMaxFileSize(400);
                    param.setSrcImageUri(uri.toString());
                    LGImgCompressorIntentService.startActionCompress(ServiceCompressActivity.this, param);
                }
            }
        });

        findViewById(R.id.service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Uri> compressFiles = getImagesPathFormAlbum();
                int size = compressFiles.size() > 10 ? 10:compressFiles.size();
                ArrayList<CompressServiceParam> tasks = new ArrayList<CompressServiceParam>(compressFiles.size());

                for (int i = 0; i < compressFiles.size(); ++i) {
                    Uri uri = compressFiles.get(i);
                    CompressServiceParam param = new CompressServiceParam();
                    param.setOutHeight(800);
                    param.setOutWidth(600);
                    param.setMaxFileSize(400);
                    param.setSrcImageUri(uri.toString());
                    tasks.add(param);
                }
                Log.d(TAG, compressFiles.size() + "compresse begain");
                Intent intent = new Intent(ServiceCompressActivity.this, LGImgCompressorService.class);
                intent.putParcelableArrayListExtra(Constanse.COMPRESS_PARAM, tasks);
                startService(intent);
            }
        });
    }

    private ArrayList<Uri> getImagesPathFormAlbum() {
        ArrayList<Uri> paths = new ArrayList<>();
        //selection: 指定查询条件
        String selection = MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?";

        //定义selection参数匹配值
        String[] selectionArgs = {"image/jpeg", "image/png"};

        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                selection,
                selectionArgs,
                MediaStore.Images.Media.DATE_MODIFIED);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
                String url = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                long size = (int) cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
                long lastModified = (int) cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED));
                //排除size为0的无效文件
                if (size != 0) {
                    Uri uri = Uri.parse(url);
                    paths.add(uri);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return paths;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(reciver != null){
            unregisterReceiver(reciver);
        }
    }
}
