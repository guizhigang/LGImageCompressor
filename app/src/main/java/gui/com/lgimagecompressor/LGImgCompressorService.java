package gui.com.lgimagecompressor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LGImgCompressorService extends Service {
    private static final String TAG = "GImgCompressorService";

    private ArrayList<LGImgCompressor.CompressResult> compressResults = new ArrayList<>();
    public LGImgCompressorService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate...");
//        executorService = Executors.newCachedThreadPool();
        executorService = Executors.newFixedThreadPool(10);
        Intent intent = new Intent(Constanse.ACTION_COMPRESS_BROADCAST);
        intent.putExtra(Constanse.KEY_COMPRESS_FLAG,Constanse.FLAG_BEGAIIN);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy...");
        Intent intent = new Intent(Constanse.ACTION_COMPRESS_BROADCAST);
        intent.putExtra(Constanse.KEY_COMPRESS_FLAG,Constanse.FLAG_END);
        intent.putParcelableArrayListExtra(Constanse.KEY_COMPRESS_RESULT,compressResults);
        sendBroadcast(intent);
        compressResults.clear();
        executorService.shutdownNow();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        doCompressImages(intent,startId);
        return Service.START_NOT_STICKY;
    }

    private int taskNumber;
    private ExecutorService executorService;
    private final Object lock = new Object();

    private void doCompressImages(final Intent intent,final int taskId){
        final ArrayList<CompressServiceParam> paramArrayList = intent.getParcelableArrayListExtra(Constanse.COMPRESS_PARAM);
        synchronized (lock){
            taskNumber += paramArrayList.size();
        }
        //如果paramArrayList过大,为了避免"The application may be doing too much work on its main thread"的问题,将任务的创建和执行统一放在后台线程中执行
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < paramArrayList.size(); ++i){
                    executorService.execute(new CompressTask(paramArrayList.get(i),taskId));
                }
            }
        }).start();
    }

    private class CompressTask implements Runnable{
        private CompressServiceParam param;
        private int taskId ;

        private CompressTask(CompressServiceParam compressServiceParam,int taskId){
            this.param = compressServiceParam;
            this.taskId = taskId;
        }

        @Override
        public void run() {
            Log.d(TAG,taskId + " do compress begain..." + Thread.currentThread().getId());
            int outwidth = param.getOutWidth();
            int outHieight = param.getOutHeight();
            int maxFileSize = param.getMaxFileSize();
            String srcImageUri = param.getSrcImageUri();
            LGImgCompressor.CompressResult compressResult = new LGImgCompressor.CompressResult();
            String outPutPath = null;
            try {
                outPutPath = LGImgCompressor.getInstance(LGImgCompressorService.this).compressImage(
                        srcImageUri, outwidth, outHieight, maxFileSize);
            } catch (Exception e) {
            }
            compressResult.setSrcPath(srcImageUri);
            compressResult.setOutPath(outPutPath);
            if (outPutPath == null) {
                compressResult.setStatus(LGImgCompressor.CompressResult.RESULT_ERROR);
            }
            Log.d(TAG,taskId + " do compress end..." + Thread.currentThread().getId());
            synchronized (lock){
                compressResults.add(compressResult);
                taskNumber--;
                if(taskNumber <= 0){
                    stopSelf(taskId);
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw null;
    }
}
