package com.homjay.longsandemo.utlis;

/**
 * @author Homjay
 * @date 2022/5/31 10:46
 * @describe
 */

import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 系统处理异常类，处理整个APP的异常
 */
public class CrashExceptionHandler implements Thread.UncaughtExceptionHandler{

    private Context mContext;

    // 本类实例
    private static CrashExceptionHandler myCrashHandler;

    // 系统默认的UncaughtExceptionHandler
    private Thread.UncaughtExceptionHandler mDefaultException;

    // 保证只有一个实例
    public CrashExceptionHandler() {
    }

    // 单例模式
    public synchronized static CrashExceptionHandler newInstance(){
        if (myCrashHandler == null){
            myCrashHandler = new CrashExceptionHandler();
        }
        return myCrashHandler;
    }


    /**
     * 初始化
     * @param context
     */
    public void init(Context context){
        this.mContext = context;
        // 系统默认处理类
        this.mDefaultException = Thread.getDefaultUncaughtExceptionHandler();
        // 将该类设置为系统默认处理类
        Thread.setDefaultUncaughtExceptionHandler(this);
    }


    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        if(!handleExample(e) && mDefaultException != null) { //判断异常是否已经被处理
            mDefaultException.uncaughtException(t, e);
        }else {
            // 睡眠3s主要是为了下面的Toast能够显示出来，否则，Toast是没有机会显示的
            try {
                Thread.sleep(3000);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            //退出程序
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    /**
     * 提示用户出现异常，将异常信息保存/上传
     * @param ex
     * @return
     */
    private boolean handleExample(Throwable ex){
        if (ex == null){
            return false;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                // 不能使用这个ToastUtils.show()，不能即时的提示，会因为异常出现问题
//            ToastUtils.show("很抱歉，程序出现异常，即将退出！");
//            MyToast.toast(mContext, "很抱歉,程序出现异常,即将退出.", Toast.LENGTH_LONG);
                Toast.makeText(mContext, "很抱歉，程序出现异常，即将退出", Toast.LENGTH_SHORT).show();

                Looper.loop();
            }
        }).start();

        saveCrashInfoToFile(ex);

        return true;
    }

    /**
     * 保存异常信息到本地
     * @param ex
     */
    private void saveCrashInfoToFile(Throwable ex) {
        //获取错误原因
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable exCause = ex.getCause();
        while (exCause != null) {
            exCause.printStackTrace(printWriter);
            exCause = exCause.getCause();
        }
        printWriter.close();

        // 错误日志文件名称
        String fileName = "crash-" + timeStampDate()+ ".log";

        // 判断sd卡可正常使用
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //文件存储位置
            String path = Environment.getExternalStorageDirectory().getPath() + "/crash_logInfo/";
            File fl = new File(path);
            //创建文件夹
            if (!fl.exists()) {
                fl.mkdirs();
            }
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(path + fileName);
                fileOutputStream.write(writer.toString().getBytes());
                fileOutputStream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }catch (Exception e2){
                e2.printStackTrace();
                Log.d("MyCrashHandler", "saveCrashInfoToFile: "+e2.getMessage());
            }finally {
                //干掉当前的程序
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }

    }

    /**
     * 时间戳转换成日期格式字符串
     * 格式 - 2021-08-05 13:59:05
     */
    public String timeStampDate() {
        Date nowTime = new Date(System.currentTimeMillis());
        SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:dd");
        return sdFormatter.format(nowTime);
    }
}


