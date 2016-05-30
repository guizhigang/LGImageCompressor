package gui.com.lgimagecompressor;

import android.provider.Settings;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by guizhigang on 16/5/29.
 */
public class LGImgCompressorServiceTest {
    private Object lock = new Object();
    private int taskNumber;
    private ArrayList<Integer> results = new ArrayList<>();

    private class CompressTask implements Runnable{
        private int taskId ;

        private CompressTask(int taskId){
            this.taskId = taskId;
        }

        @Override
        public void run() {
            System.out.println(taskId + " do compress begain..." + Thread.currentThread().getId());
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(taskId + " do compress end..." + Thread.currentThread().getId());
            synchronized (lock){
                System.out.println(taskId + " make taskNumber release before " + taskNumber);
                results.add(taskId);
                taskNumber--;
                System.out.println(taskId + " make taskNumber release end " + taskNumber);
                if(taskNumber <= 0){
                    System.out.println("all task done...");
                }
            }
        }
    }

    @Test
    public void startServer(){
        taskNumber = 8;
        int count = taskNumber;
        for (int i = 1; i <= count; i++){
            new Thread(new CompressTask(i)).start();
        }
        synchronized (lock){
            try {
                Thread.sleep(taskNumber * 100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {

    }
}