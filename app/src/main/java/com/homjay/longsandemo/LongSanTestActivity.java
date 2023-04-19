package com.homjay.longsandemo;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.homjay.longsandemo.cmd.CmdConstants;
import com.homjay.longsandemo.cmd.CmdUtils;
import com.homjay.longsandemo.cmd.HexUtils;
import com.homjay.longsandemo.utlis.CSCRCTmpUtils;
import com.homjay.longsandemo.utlis.CSCRCUtils;
import com.homjay.longsandemo.utlis.MyCountDownTimer;
import com.homjay.longsandemo.utlis.MyLog;
import com.homjay.longsandemo.utlis.TimerTasker;
import com.homjay.longsandemo.view.HeartView;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Homjay
 * @date 2022/11/22 14:03
 * @describe
 */
public class LongSanTestActivity  extends AppCompatActivity {

    private TextView tv_getversion,tv_date,tv_reset,tv_blood_info,tv_sn,tv_blood_set,tv_onpause_blood,tv_stop_blood
            ,tv_start,tv_blood_meansure,tv_onpause_meansure,tv_stop_meansure,txt,tv_users_info,tv_heart_meansure
            ,tv_onpause_heart,tv_stop_heart,tv_send_byte;

    private TextView tv_cout_down,tv_hrv_compressive_data_txt,tv_hrv_mental_data_txt,tv_hrv_pressure_data_txt,tv_hrv_fatigue_data_txt,
            tv_heart_rate_data_txt,tv_pulse_rate_data_txt,tv_blood_oxy_saturation_data_txt,tv_blood_diastolic,tv_blood_systolic;

    private RelativeLayout layout_info;
    private HeartView heart_view,blood_oxy_view;

    private ScrollView sc_txt;
    private BleDevice bleDevice;
    private BleDevice weightBle;

    private String code;
    private String version;
    private boolean isBitMode = true;
    private boolean isOTAStatus = false;
    private int num_exception_data; //异常数据次数，超过五次结束测量

    private int type_wake;//谁唤醒的
    private static final int WAKE_MEASURE = 1;//测量发起的唤醒
    private static final int WAKE_VERSION = 2;//获取版本号发起的唤醒
    private static final long TIME_HEART = 1000; //测量心跳
    private static final long TIME_OAT = 1000; //固件升级

    private TimerTasker otaTimer;//固件升级
    private TimerTasker taskerHeart; //心跳
    private MyCountDownTimer countDownTimer;//倒计时
    private int isUpdata = 0;

    private long sendSzie = 0;
    private long NotifySzie = 0;
    private long OderSzie = 0;
    private String dataStrS;
    private int packageSize = 1024;

    //OTA
    private byte blockNumber = 0x01;
    private int FileLength = 0;
    private int sucOtaCount = 0;//升级完成计数器
    private byte [] buffer ;
    private byte responseACK = 0x01;

    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    private String data = "aa 55 02 84 13 00 99 " +
            "aa 55 0c a8 67 67 67 67 67 66 66 65 64 63 62 61 72 " +
            "aa 55 0d a8 60 5f 5e 5e 5d 5c 5c 5b 5a 59 58 58 57 5a " +
            "aa 55 0c a8 57 56 56 55 55 54 54 54 54 53 53 53 aa " +
            "aa 55 0d a8 52 52 51 51 50 4f 4e 4e 4d 4d 4e 50 53 c1 " +
            "aa 55 0c a8 57 5d 65 6d 75 7c 81 86 89 8b c5 62 d6 " +
            "aa 55 0d a8 87 84 82 80 7d 7b 79 77 75 74 72 71 6f e5 " +
            "aa 55 0c a8 6e 6c 6b 69 67 65 64 62 61 60 60 5f 74 " +
            "aa 55 4d a8 5f 5f 5f 5f 5f 5f 5f 5f 5f 5f 5e 5e 5e 85 " +
            "aa 55 0c a8 5d 5c 5b 5a 59 58 58 57 56 55 55 54 d6 aa 55 01 " +
            "92 00 93 aa 55 01 ab 00 ac aa 55 02 a9 00 61 0c aa 55 02 a7 " +
            "00 49 f2 aa 55 04 b0 00 96 00 5A a4 " +
            "aa 55 0d a8 54 54 53 53 53 52 52 52 52 52 51 51 51 e3";


    Handler mHandler = new Handler();



    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (countDownTimer != null){
            countDownTimer.cancel();
            countDownTimer = null;
        }

        if (mHandler != null) {
            mHandler = null;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_longsan_test);

        bleDevice = getIntent().getParcelableExtra("ble");
        weightBle = getIntent().getParcelableExtra("weight");

        tv_getversion = findViewById(R.id.tv_getversion);
        tv_date = findViewById(R.id.tv_date);
        tv_reset = findViewById(R.id.tv_reset);
        tv_blood_info = findViewById(R.id.tv_blood_info);
        tv_sn = findViewById(R.id.tv_sn);
        tv_blood_set = findViewById(R.id.tv_blood_set);
        tv_onpause_blood = findViewById(R.id.tv_onpause_blood);
        tv_stop_blood = findViewById(R.id.tv_stop_blood);
        tv_start = findViewById(R.id.tv_start);
        tv_blood_meansure = findViewById(R.id.tv_blood_meansure);
        tv_onpause_meansure = findViewById(R.id.tv_onpause_meansure);
        tv_stop_meansure = findViewById(R.id.tv_stop_meansure);
        tv_users_info = findViewById(R.id.tv_users_info);
        tv_heart_meansure = findViewById(R.id.tv_heart_meansure);
        tv_onpause_heart = findViewById(R.id.tv_onpause_heart);
        tv_stop_heart = findViewById(R.id.tv_stop_heart);
        tv_send_byte = findViewById(R.id.tv_send_byte);
        layout_info = findViewById(R.id.layout_info);


        tv_pulse_rate_data_txt = findViewById(R.id.tv_pulse_rate_data_txt);
        tv_blood_oxy_saturation_data_txt = findViewById(R.id.tv_blood_oxy_saturation_data_txt);
        tv_blood_diastolic = findViewById(R.id.tv_blood_diastolic);
        tv_blood_systolic = findViewById(R.id.tv_blood_systolic);
        tv_heart_rate_data_txt = findViewById(R.id.tv_heart_rate_data_txt);
        tv_hrv_compressive_data_txt = findViewById(R.id.tv_hrv_compressive_data_txt);
        tv_hrv_mental_data_txt = findViewById(R.id.tv_hrv_mental_data_txt);
        tv_hrv_pressure_data_txt = findViewById(R.id.tv_hrv_pressure_data_txt);
        tv_hrv_fatigue_data_txt = findViewById(R.id.tv_hrv_fatigue_data_txt);
        tv_cout_down = findViewById(R.id.tv_cout_down);

        heart_view = findViewById(R.id.heart_view);
        blood_oxy_view = findViewById(R.id.blood_oxy_view);


        txt = findViewById(R.id.txt);
        sc_txt = findViewById(R.id.sc_txt);

        tv_cout_down.setText("请开始测量");

//        String s = "0807";
//        String w = s.substring(0,2);
//        String k = s.substring(2,4);
//        byte b = Byte.parseByte(w);
//        byte be = Byte.parseByte(k);

//        byte[] j = HexUtils.hexStringToBytes(s);
//
//        int d = ((j[0] & 0x0F)<< 8 ) +  j[1];

//        tv_heart_rate_data_txt.setText(d + "");

        data = "57281c070000";

        int fatigue = Integer.valueOf(data.substring(0,2), 16);
        int pressure = Integer.valueOf(data.substring(2,4), 16);
        int spirit = Integer.valueOf(data.substring(4,6), 16);
        int compressive = Integer.valueOf(data.substring(6,8), 16);

        if (fatigue == 0 || pressure == 0 || spirit == 0 || compressive == 0) return;

        countDownTimer = new MyCountDownTimer(160,1000);

        BleManager.getInstance().notify(bleDevice, CmdConstants.UUID_Server, CmdConstants.UUID_Notify, new BleNotifyCallback() {
            @Override
            public void onNotifySuccess() {
                Toast.makeText(LongSanTestActivity.this,"通知打开成功",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNotifyFailure(BleException exception) {
                Toast.makeText(LongSanTestActivity.this,"通知打开失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCharacteristicChanged(final byte[] data) {
                NotifySzie += data.length;
                final String dataStr =  HexUtil.formatHexString(data);
                dataStrS = HexUtil.formatHexString(data,true);
                //处理发送成功后返回数据
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addText(txt, "接收：" + ": " + dataStrS );
                        MyLog.e(LongSanTestActivity.class.getSimpleName(), "onCharacteristicChanged: " +"接收：" + ": " + dataStrS );
                        tv_send_byte.setText("Send：" + sendSzie + "字节，" + "Receive：" + NotifySzie + "字节" + "Order：" + OderSzie + "字节");
                    }
                });

                //进入升级模式
                if (CmdConstants.Rev_Update_Start.equals(dataStrS)){
                    if (isUpdata == 0){
                        isUpdata = 1;
                        Log.e("固件升级", "进入固件升级模式");
//                        new Xmodem2("file:///android_asset/VITA_vh66_ti4900_v20.0.11.img",bleDevice,LongSanTestActivity.this).send();
//                            Xmodem2.responseACK = data[0];
                        //非升级完成
                        if (!isOTAStatus){
                            responseACK = data[0];
                            startOTA();
                        }else {
                            //累计收到30STS才算升级成功
                            if (sucOtaCount >= 30){
                                Log.e("固件升级", "重启");
                                //重启
                                writeHandSet(HexUtils.hexStringToBytes("00"));
                                isOTAStatus = false;
                                isUpdata = 1;
                                sucOtaCount = 1;
                            }else {
                                Log.e("升级计数器",sucOtaCount+"c");
                                sucOtaCount ++;
                                isUpdata = 0;
                            }
                        }

//                        updataOTA();
                         return;
                    }

                }

                //发送成功
                if (CmdConstants.Rev_Update_Success.equals(dataStrS)){
                    if (isUpdata == 1){
//                        new Xmodem2("file:///android_asset/VITA_vh66_ti4900_v20.0.11.img",bleDevice,LongSanTestActivity.this).send();
//                        Xmodem2.responseACK = data[0];
                        responseACK = 06;
//
                        return;
                    }

                }


                if (dataStr.contains(CmdConstants.Rev_Update_Error_ReSend)){
                    if (isUpdata == 1){
                        Log.e("固件升级","请求重发同一个数据包");
//                        writeHandSet(HexUtils.hexStringToBytes("00"));
//
                        isUpdata = 0;
//                        Xmodem2.responseACK = data[0];
                        responseACK = data[0];
                        otaTimer.cancelTimer();
//
                        return;
                    }
                }


                if (dataStr.contains(CmdConstants.Rev_Update_Error_Wrong_Sequence)){
                    if (isUpdata == 1){
                        Log.e("固件升级","包序号错了");
//                        writeHandSet(HexUtils.hexStringToBytes("00"));
                        isUpdata = 0;
//                        Xmodem2.responseACK = data[0];
                        responseACK = data[0];
                        otaTimer.cancelTimer();
                        return;
                    }
                }

                if (dataStr.contains(CmdConstants.Rev_Update_Error_Stop_Can)){
                    if (isUpdata == 1){
                        Log.e("固件升级","包序号错了,终止发送");
//                        writeHandSet(HexUtils.hexStringToBytes("00"));
                        isUpdata = 0;
//                        Xmodem2.responseACK = data[0];
                        responseACK = data[0];
                        otaTimer.cancelTimer();
                        return;
                    }
                }

                //处理发送成功后返回数据
                List<String> cmdList = CmdUtils.getDataCmdList(dataStr);
//                List<String> cmdList = CmdUtils.getDataHandSetCmdList(dataStr);
                if (cmdList == null || cmdList.size() == 0) return;
                for (String cmd : cmdList) {
                    String code = CmdUtils.getCode(cmd);
                    String backState = CmdUtils.getBackState(cmd);

                    //用户数据发送成功
                    if (CmdConstants.Code_user.equals(code)) {
                        if (!TextUtils.isEmpty(backState)) {
//                            Log.i(MyBleService.TAG, "用户数据发送失败");
                            continue;
                        }
//                        List<String> markList = Hawk.get(AppConstants.Hawk_Mark_List);
//                        int i = 0;
//                        for (String cmd_mark : markList) {
//                            sendMessageCMD(cmd_mark, i);
//                            i = i + TIME_MARK_PACKAGE;
//                        }
                    }

                    //发送标定数据成功
                    if (CmdConstants.Code_send_mask.equals(code)) {
                        if (!TextUtils.isEmpty(backState)) {
//                            Log.i(MyBleService.TAG, "标定数据发送失败");
                            continue;
                        }
                        String cmd_measure = CmdUtils.getStartMeasureOrMarkCmd(true, 0, 0);
//                        sendMessageCMD(cmd_measure, TIME_START_MEASURE);
                        write(cmd_measure);
                    }

                    //发送开始测量成功
                    if (CmdConstants.Code_start_measure_mask.equals(code)) {
                        if (!TextUtils.isEmpty(backState)) {
//                            Log.i(MyBleService.TAG, "开始测量发送失败");
                            continue;
                        }
//                        startMeasure();
                        if (taskerHeart != null) {
                            taskerHeart.cancelTimer();
                            taskerHeart.startTimer();
                        }
                    }

                    //返回版本信息
                    if (CmdConstants.Code_version.equals(code)) {
                        String version = CmdUtils.getVersionInfo(cmd);
//                        Hawk.put(AppConstants.Hawk_Ota_Version, version);
                        addText(txt, "接收：" + "version:" + version );
                    }

                }

                //不是测量返回，防止结束测量后，还有数据返回
//                if (!isMeasuring) return;
                //测量中，但是在保存数据
//                if (isSaving) return;

                //处理数据显示
                try {
//                    handleRev(cmdList);

                    getData(dataStrS);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


        tv_getversion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write(CmdConstants.Cmd_Version);
            }
        });

        tv_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = timeStampDate();
                String datas = date.substring(0,4);
                String datass = date.substring(4);
                String data = Integer.toHexString(Integer.parseInt(datas));
                if (data.length() < 4){
                    data = "0"+data;
                }
                String time = "";
                for (int i = 0; i <datass.length()/2 ; i++) {
                    String hh = Integer.toHexString(Integer.parseInt(datass.substring(i*2,i*2+2)));
                    if (hh.length() < 2){
                        hh = "0"+hh;
                    }
                    time += hh;
                }
                StringBuilder builder = new StringBuilder();
                builder.append(CmdConstants.Cmd_Start);
                builder.append("0704");
                builder.append(data);
                builder.append(time);
                builder.append(CmdUtils.getCheck("0704"+data+time));
                write(builder.toString());
            }
        });

        tv_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeHandSet(HexUtils.hexStringToBytes("00"));
//                write("aa55000808");
//                Xmodem2.responseACK = 0x00;
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            while (Xmodem2.responseACK != 0x43){
//                                write("1b");
//                                write("1b");
//                                write("1b");
//                            }
//                            Thread.sleep(50);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
//                write("aa5501100A1B");
            }
        });

        tv_sn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String date = "383B2678235C";
//                StringBuilder builder = new StringBuilder();
//                builder.append(CmdConstants.Cmd_Start);
//                builder.append("0C0C");
//                builder.append(date);
//                builder.append(CmdUtils.getCheck("0C0C"+date));
//                write(builder.toString());

//                BleManager.getInstance().notify(weightBle, CmdConstants.UUID_Serverw, CmdConstants.UUID_Notifyw, new BleNotifyCallback() {
//                    @Override
//                    public void onNotifySuccess() {
//                        Toast.makeText(LongSanTestActivity.this,"weight通知打开成功",Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onNotifyFailure(BleException exception) {
//                        Toast.makeText(LongSanTestActivity.this,"weight通知打开失败"+exception.getDescription(),Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onCharacteristicChanged(byte[] data) {
//                        final String dataStr =  HexUtil.formatHexString(data);
//                        final String dataStrS =  HexUtil.formatHexString(data,true);
//                        //处理发送成功后返回数据
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                addText(txt, "接收：" + ": " + dataStrS );
//                                Log.e(LongSanTestActivity.class.getSimpleName(), "onCharacteristicChanged: " +"接收：" + ": " + dataStrS );
//                            }
//                        });
//                    }
//                });

                //查看电量
                for (BleDevice bleDevice :BleManager.getInstance().getAllConnectedDevice()){
                    if (bleDevice.getName().contains("XIAODUO")){
                        BleManager.getInstance().notify(bleDevice, CmdConstants.UUID_Electricity_Server, CmdConstants.UUID_Electricity_Notify, new BleNotifyCallback() {
                            @Override
                            public void onNotifySuccess() {
                                Toast.makeText(LongSanTestActivity.this,"weight通知打开成功",Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onNotifyFailure(BleException exception) {
                                Toast.makeText(LongSanTestActivity.this,"weight通知打开失败"+exception.getDescription(),Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCharacteristicChanged(byte[] data) {
                                final String dataStr =  HexUtil.formatHexString(data);
                                MyLog.e(LongSanTestActivity.class.getSimpleName(), "weight onCharacteristicChanged: " + "接收：" + HexUtils.byteToHexString(data) );
                                if ( HexUtils.byteToHexString(data).length() == 2){
                                    tv_sn.setText(HexUtils.hexToDec(HexUtils.byteToHexString(data)) + "%");
                                }
                            }
                        });
                    }
                }

            }
        });

        tv_blood_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                write("aa55000B0B");
                write("aa5501104253");
                if (taskerHeart == null){
                    taskerHeart = new TimerTasker(TIME_HEART, new TimerTasker.OnTimerRun() {
                        @Override
                        public void onRun() {
                            write(CmdConstants.Cmd_Heart);
                        }
                    });
                }else {
                    taskerHeart.startTimer();
                }

                if (countDownTimer != null){
                    countDownTimer.start(new MyCountDownTimer.OnTimerCallBack() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onTick(int times) {
                            tv_cout_down.setText("倒计时：" + times + "s");
                        }

                        @Override
                        public void onFinish() {
                            write("aa550211002134");
                            tv_cout_down.setText("测量完成" );
                            tv_send_byte.setText("Send：" + sendSzie + "字节，" + "Receive：" + NotifySzie + "字节" + "Order：" + OderSzie + "字节");
                            MyLog.e("测量完成","共发送了：" + sendSzie + "字节，" + "共接收了" + NotifySzie + "字节"+ "Order：" + OderSzie + "字节");
                            if (taskerHeart != null){
                                taskerHeart.cancelTimer();
                                taskerHeart = null;
                            }
                        }
                    });
                }


                NotifySzie = 0;
                sendSzie = 0;
//                BleManager.getInstance().connect(weightBle, new BleGattCallback() {
//                    @Override
//                    public void onStartConnect() {
//
//                    }
//
//                    @Override
//                    public void onConnectFail(BleDevice bleDevice, BleException exception) {
//
//                    }
//
//                    @Override
//                    public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
//                        Toast.makeText(LongSanTestActivity.this,"weightBle连接成功",Toast.LENGTH_SHORT).show();
//
//                    }
//
//                    @Override
//                    public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
//
//                    }
//                });

            }
        });

        tv_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                write(CmdConstants.Cmd_Wake);
                if (isBitMode){
                    isBitMode = false;
                    sc_txt.setVisibility(View.GONE);
                    layout_info.setVisibility(View.VISIBLE);
                    tv_start.setText("字节模式");
                }else {
                    isBitMode = true;
                    sc_txt.setVisibility(View.VISIBLE);
                    layout_info.setVisibility(View.GONE);
                    tv_start.setText("解析模式");
                }
            }
        });


        tv_blood_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write("aa5501102031");
//                write("aa5501102031");
                if (taskerHeart == null){
                    taskerHeart = new TimerTasker(TIME_HEART, new TimerTasker.OnTimerRun() {
                        @Override
                        public void onRun() {
                            write(CmdConstants.Cmd_Heart);
                        }
                    });
                }else {
                    taskerHeart.startTimer();
                }
            }
        });

        tv_onpause_blood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write("aa550211012034");
                if (taskerHeart != null){
                    taskerHeart.cancelTimer();
                    taskerHeart = null;
                }
            }
        });

        tv_stop_blood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write("aa550211002033");
                if (taskerHeart != null){
                    taskerHeart.cancelTimer();
                    taskerHeart = null;
                }
            }
        });

        tv_blood_meansure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write("aa5501102132");
                if (taskerHeart == null){
                    taskerHeart = new TimerTasker(TIME_HEART, new TimerTasker.OnTimerRun() {
                        @Override
                        public void onRun() {
                            write(CmdConstants.Cmd_Heart);
                        }
                    });
                }else {
                    taskerHeart.startTimer();
                }
            }
        });

        tv_onpause_meansure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write("aa550211012135");
                if (taskerHeart != null){
                    taskerHeart.cancelTimer();
                    taskerHeart = null;
                }
            }
        });
        tv_stop_meansure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write("aa550211002134");
                if (taskerHeart != null){
                    taskerHeart.cancelTimer();
                    taskerHeart = null;
                }
            }
        });

        tv_users_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //用户信息
//                String users = CmdUtils.getUserCmd();
//                write(users);
                //固件升级
//                int h = 02;
//                int b = 01;
//                int c = ~b;
//                String d = "1b";
//                StringBuilder builder = new StringBuilder();
//                builder.append(h);
//                builder.append(b);
//                builder.append(c);
//                builder.append(d);
//                for (int i = 0 ; i < 1023; i++){
//                    builder.append("1A");
//                }
//                Log.e("TAGsdsaadsadad", "onClick: " + getCrc16(new Byte[]{(byte)0x88,0x1a,(byte)0x88,0x1a,(byte)0x88,0x1a,(byte)0x88,0x1a,(byte)0x88,0x1a,(byte)0x88,0x1a,(byte)0x88,0x1a,(byte)0x88,0x1a}) );
//                writeHandSet(HexUtils.hexStringToBytes("60"));
//                write("aa55000808");
//                    write("1b");
//                    write("1b");
//                    write("1b");
//                    write("1b");
//                updataOTA();
                writeHandSet(HexUtils.hexStringToBytes("60"));
                isUpdata = 0;
            }
        });

        tv_heart_meansure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write("aa5501100A1B");
                if (taskerHeart == null){
                    taskerHeart = new TimerTasker(TIME_HEART, new TimerTasker.OnTimerRun() {
                        @Override
                        public void onRun() {
                            write(CmdConstants.Cmd_Heart);
                        }
                    });
                }else {
                    taskerHeart.startTimer();
                }
            }
        });

        tv_onpause_heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write("aa550211010A1e");
                if (taskerHeart != null){
                    taskerHeart.cancelTimer();
                    taskerHeart = null;
                }
            }
        });

        tv_stop_heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write("aa550211000A1d");
                if (taskerHeart != null){
                    taskerHeart.cancelTimer();
                    taskerHeart = null;
                }
            }
        });
    }

    private void write(String data){
        byte[] hex = HexUtils.hexStringToBytes(data);
//        Toast.makeText(MeansureActivity.this,date,Toast.LENGTH_SHORT).show();
        BleManager.getInstance().write(bleDevice, CmdConstants.UUID_Server, CmdConstants.UUID_Write, hex, new BleWriteCallback() {
            @Override
            public void onWriteSuccess(int current, int total, byte[] justWrite) {
//                Toast.makeText(MeansureActivity.this,"时间同步成功",Toast.LENGTH_SHORT).show();
                addText(txt,"发送：" + HexUtils.byteToHexString(justWrite));
                tv_send_byte.setText("Send：" + sendSzie + "字节，" + "Receive：" + NotifySzie + "字节" + "Order：" + OderSzie + "字节");
                sendSzie += justWrite.length;

                MyLog.e(LongSanTestActivity.class.getSimpleName(), "onWriteSuccess: " + "发送：" + HexUtils.byteToHexString(justWrite) );
//                String data = HexUtil.formatHexString(justWrite);
//
//                if (CmdConstants.Cmd_Wake.equals(data)) {
//                    if (type_wake == WAKE_MEASURE) {
////                        String users = CmdUtils.getUserCmd();
////                        write(users);
//                    } else if (type_wake == WAKE_VERSION) {
//                        write(CmdConstants.Cmd_Version);
//                    }
//                }
            }

            @Override
            public void onWriteFailure(BleException exception) {
                addText(txt,"发送：" + exception.getDescription());
//                Toast.makeText(MeansureActivity.this,"数据写入失败：" + exception.getDescription(),Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void writes(byte[] data){
//        Toast.makeText(MeansureActivity.this,date,Toast.LENGTH_SHORT).show();
        BleManager.getInstance().write(bleDevice, CmdConstants.UUID_Server, CmdConstants.UUID_Write, data, new BleWriteCallback() {
            @Override
            public void onWriteSuccess(int current, int total, byte[] justWrite) {
//                Toast.makeText(MeansureActivity.this,"时间同步成功",Toast.LENGTH_SHORT).show();
                addText(txt,"发送：" + HexUtils.byteToHexString(justWrite));
                tv_send_byte.setText("Send：" + sendSzie + "字节，" + "Receive：" + NotifySzie + "字节" + "Order：" + OderSzie + "字节");
                sendSzie += justWrite.length;

                MyLog.e(LongSanTestActivity.class.getSimpleName(), "onWriteSuccess: " + "发送：" + HexUtils.byteToHexString(justWrite) );
//                String data = HexUtil.formatHexString(justWrite);
//
//                if (CmdConstants.Cmd_Wake.equals(data)) {
//                    if (type_wake == WAKE_MEASURE) {
////                        String users = CmdUtils.getUserCmd();
////                        write(users);
//                    } else if (type_wake == WAKE_VERSION) {
//                        write(CmdConstants.Cmd_Version);
//                    }
//                }
            }

            @Override
            public void onWriteFailure(BleException exception) {
                addText(txt,"发送：" + exception.getDescription());
//                Toast.makeText(MeansureActivity.this,"数据写入失败：" + exception.getDescription(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void writeHandSet(byte[] data){
            BleManager.getInstance().write(bleDevice, CmdConstants.UUID_Server, CmdConstants.UUID_Chanage_CPU, data, new BleWriteCallback() {
                @Override
                public void onWriteSuccess(int current, int total, byte[] justWrite) {
//                Toast.makeText(MeansureActivity.this,"时间同步成功",Toast.LENGTH_SHORT).show();
                    addText(txt,"发送：" + HexUtils.byteToHexString(justWrite));

                    MyLog.e(LongSanTestActivity.class.getSimpleName(), "onWriteSuccess: " + "发送：" + HexUtils.byteToHexString(justWrite) );
//                String data = HexUtil.formatHexString(justWrite);
//
//                if (CmdConstants.Cmd_Wake.equals(data)) {
//                    if (type_wake == WAKE_MEASURE) {
////                        String users = CmdUtils.getUserCmd();
////                        write(users);
//                    } else if (type_wake == WAKE_VERSION) {
//                        write(CmdConstants.Cmd_Version);
//                    }
//                }
                }

                @Override
                public void onWriteFailure(BleException exception) {
                    addText(txt,"发送：" + exception.getDescription());
//                Toast.makeText(MeansureActivity.this,"数据写入失败：" + exception.getDescription(),Toast.LENGTH_SHORT).show();
                }
            });
    }


    private void writeHandSets(byte[] data) throws InterruptedException {
//        while (data.length > 0){
//            byte[] send  = new byte[343];
//            byte[] now = new byte[data.length - 343];
//            System.arraycopy(data,0,send,0,343);
//            System.arraycopy(data,343,now,0,data.length-343);
//            data = new byte[data.length - 343];
//            data = now;
            BleManager.getInstance().write(bleDevice, CmdConstants.UUID_Server, CmdConstants.UUID_Chanage_CPU, data, new BleWriteCallback() {
                @Override
                public void onWriteSuccess(int current, int total, byte[] justWrite) {
//                Toast.makeText(MeansureActivity.this,"时间同步成功",Toast.LENGTH_SHORT).show();
                    addText(txt,"发送：" + HexUtils.byteToHexString(justWrite));

                    MyLog.e(LongSanTestActivity.class.getSimpleName(), "onWriteSuccess: " + "发送：" + HexUtils.byteToHexString(justWrite) );
//                String data = HexUtil.formatHexString(justWrite);
//
//                if (CmdConstants.Cmd_Wake.equals(data)) {
//                    if (type_wake == WAKE_MEASURE) {
////                        String users = CmdUtils.getUserCmd();
////                        write(users);
//                    } else if (type_wake == WAKE_VERSION) {
//                        write(CmdConstants.Cmd_Version);
//                    }
//                }
                }

                @Override
                public void onWriteFailure(BleException exception) {
                    addText(txt,"发送：" + exception.getDescription());
//                Toast.makeText(MeansureActivity.this,"数据写入失败：" + exception.getDescription(),Toast.LENGTH_SHORT).show();
                }
            });
//        }

    }

    private void addText(TextView textView, String content) {
        textView.append(content);
        textView.append("\n");
        int offset = textView.getLineCount() * textView.getLineHeight();
        if (offset > textView.getHeight()) {
            textView.scrollTo(0, offset - textView.getHeight());
        }
        scroll2Bottom(sc_txt,txt);
    }

    private  String timeStampDate() {
        Date nowTime = new Date(System.currentTimeMillis());
        SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyyMMddHHmmdd");
        return sdFormatter.format(nowTime);
    }

    public static void scroll2Bottom(final ScrollView scroll, final View inner) {
        Handler handler = new Handler();
        handler.post(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (scroll == null || inner == null) {
                    return;
                }
                int offset = inner.getMeasuredHeight() - scroll.getMeasuredHeight();
                if (offset < 0) {
                    offset = 0;
                }
                scroll.scrollTo(0, offset);
            }
        });
    }
    //检验数据是否合格，不合格立即终止并提示
    private boolean isExceptionData5() {
        if (num_exception_data > 5) {
//            Toast.makeText(LongSanTestActivity.this,"测量方式不正确，请重新测量",Toast.LENGTH_LONG).show();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            return true;
        }
        return false;
    }

    private void getData(String data){
        List<String> cmdList = CmdUtils.getDataCmdList(data.replace(" ",""));
        for (String datatt : cmdList){
            OderSzie += datatt.length();
            MyLog.e("指令组合", "合成指令 ==============" +datatt);
        }
        Map<String, List<Integer>> map = CmdUtils.getData(cmdList);
        if (map == null || map.size() == 0) return;
        for (Map.Entry<String, List<Integer>> entry : map.entrySet()) {
            String codeHex = entry.getKey();
            List<Integer> ints = entry.getValue();
            if (CmdConstants.Type_bp.equals(codeHex)) {
                int sbp = ints.get(0);
                int dbp = ints.get(1);
                Log.d(LongSanTestActivity.class.getSimpleName(), "sbp:" + sbp + "_dbp:" + dbp);

                //血压
//                if (!CmdUtils.checkData(CmdConstants.Type_sbp, sbp)) {
//                    num_exception_data++;
//                    isExceptionData5();
//                    return;
//                }
//                if (!CmdUtils.checkData(CmdConstants.Type_dbp, dbp)) {
//                    num_exception_data++;
//                    isExceptionData5();
//                    return;
//                }

                tv_blood_diastolic.setText(String.valueOf(sbp));
                tv_blood_systolic.setText(String.valueOf(dbp));
            } else if (CmdConstants.Type_heartRate.equals(codeHex)) {
                //心率
                int heart = ints.get(0);
                Log.d(LongSanTestActivity.class.getSimpleName(), "heart:" + heart);

                if (!CmdUtils.checkData(CmdConstants.Type_heartRate, heart)) {
                    num_exception_data++;
                    isExceptionData5();
                    return;
                }


                tv_pulse_rate_data_txt.setText(String.valueOf(heart));
            } else if (CmdConstants.Type_spo2.equals(codeHex)) {
                //血氧
                int spo2 = ints.get(0);
                Log.d(LongSanTestActivity.class.getSimpleName(), "spo2:" + spo2);

                if (!CmdUtils.checkData(CmdConstants.Type_spo2, spo2)) {
                    num_exception_data++;
                    isExceptionData5();
                    return;
                }

//                mSpo2List.add(spo2);
                tv_blood_oxy_saturation_data_txt.setText(String.valueOf(spo2));
            } else if (CmdConstants.Type_spo2_x.equals(codeHex)) {
                //血氧波形
                //显示波形
                for (int i = 0; i < ints.size(); i++) {
                    int tu = ints.get(i);
                    blood_oxy_view.offer(tu);
//                    Logger.d(TAG_spo2_x, tu + "");
                }
            }else if (CmdConstants.Code_heart.equals(codeHex)){
                String tus = "";
                for (int i = 0; i < ints.size(); i++) {
                    int tu = ints.get(i);
                    tus += "   " + tu;
                    heart_view.offer(tu);
//                    Logger.d(TAG_spo2_x, tu + "");
                }
                tv_heart_rate_data_txt.setText(tus);
            }else if (CmdConstants.Code_hrv.equals(codeHex)){
                int fatigue = ints.get(0);
                int pressure = ints.get(1);
                int spirit = ints.get(2);
                int compressive = ints.get(3);

                tv_hrv_fatigue_data_txt.setText(fatigue+"");
                tv_hrv_pressure_data_txt.setText(pressure+"");
                tv_hrv_mental_data_txt.setText(spirit+"");
                tv_hrv_compressive_data_txt.setText(compressive+"");
            }
        }
    }


    private void startOTA(){
        try {
            String res = "";
            //得到资源中的Raw数据流
            InputStream in = getAssets().open("VITA_vh66_ti4900_v20.0.11.img");

            //得到数据的大小
            FileLength = in.available();

            buffer = new byte[FileLength];

            //读取数据
            in.read(buffer);
//            //依test.txt的编码类型选择合适的编码，如果不调整会乱码
            res = HexUtils.byteToHexString(buffer);
            Log.e("文件数据："," old szie ===== " + res.length() + "\n" + res);
            blockNumber = 0x01;
            //关闭
            in.close();
            responseACK = 0x06;
            otaTimer = new TimerTasker(TIME_OAT, new TimerTasker.OnTimerRun() {
                @Override
                public void onRun() {
                    if (responseACK == 06 ){
                        if (FileLength == 0 ){
                            isUpdata = 0;
                            writes(new byte[]{0x04});
                            otaTimer.cancelTimer();
                            isOTAStatus = true;
//                            return;//传输完毕，防止还没发送完，重复执行
                        }else {
                            if (FileLength < packageSize){
                                updataOTALess();
                            }else {
                                updataOTA();
                            }
                        }

                    }else {
                        if (responseACK != 01){
                            Toast.makeText(LongSanTestActivity.this,"升级失败",Toast.LENGTH_SHORT).show();
                            otaTimer.cancelTimer();
                            writeHandSet(HexUtils.hexStringToBytes("00"));
                        }
                    }
                }
            });
            otaTimer.startTimer();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void updataOTALess(){
        responseACK = 0x01;
        byte[] elss = new byte[packageSize];
        System.arraycopy( buffer, 0, elss, 0, buffer.length);
        //不足补1A
        for (int i = buffer.length; i < packageSize; i++) {
            elss[i] = (byte) 0X1A;
        }
        FileLength = 0;//进入此方法，最后一次传输，直接负值0结束
        buffer = new byte[packageSize+5];
        buffer[0] = 0x02;
        buffer[1] = blockNumber;
        buffer[2] = (byte) ~blockNumber;
        System.arraycopy( elss, 0, buffer, 3, packageSize);
        buffer[packageSize +3] = CSCRCUtils.calcCrc(elss)[0];
        buffer[packageSize +4] = CSCRCUtils.calcCrc(elss)[1];
        writes(buffer);
        Log.e("文件数据："," new szie ===== " + buffer.length + "\n" + HexUtils.byteToHexString(buffer));
//        otaTimer.cancelTimer();
    }

    private void updataOTA(){
        responseACK = 0x01;
        byte[] ota = new byte[packageSize + 5];
        byte[] read = new byte[packageSize];
        byte[] buffers = new byte[FileLength - packageSize];
        System.arraycopy( buffer, 0, read, 0, packageSize);
        System.arraycopy( buffer, packageSize, buffers, 0, FileLength - packageSize);
        buffer = new byte[FileLength - packageSize];
        buffer = buffers;
        FileLength = FileLength - packageSize;
        ota[0] = 0x02;
        ota[1] = blockNumber;
        ota[2] = (byte) ~blockNumber;
        System.arraycopy(read, 0, ota, 3, read.length);
        ota[packageSize + 3] = CSCRCUtils.calcCrc(read)[0];
        ota[packageSize + 4] = CSCRCUtils.calcCrc(read)[1];
        Log.e("文件数据：", "ota size ====== " + ota.length + "\n" + HexUtils.byteToHexString(ota));
        writes(ota);
        blockNumber = (byte) ((++blockNumber) % 256);
        Log.e("11111", "包序号: " + blockNumber);
    }


    //old
    private void updataOTAs(){
        executor.submit(new Runnable() {
            @Override
            public void run() {
                String res = "";
                try{
                    //得到资源中的Raw数据流
                    InputStream in = getAssets().open("VITA_vh66_ti4900_v20.0.11.img");

                    //得到数据的大小
                    int length = in.available();

                    byte [] buffer = new byte[length];

                    //读取数据
                    in.read(buffer);
//            //依test.txt的编码类型选择合适的编码，如果不调整会乱码
                    res = HexUtils.byteToHexString(buffer);
                    Log.e("文件数据："," old szie ===== " + res.length() + "\n" + res);
                    byte blockNumber = 0x01;

                    while (res.length() > packageSize){
                        byte[] ota = new byte[packageSize+5];
                        byte[] read = new byte[packageSize];
                        byte[] buffers = new byte[length - packageSize];
                        System.arraycopy( buffer, 0, read, 0, packageSize);
                        System.arraycopy( buffer, packageSize, buffers, 0, length - packageSize);
                        buffer = new byte[length - packageSize];
                        buffer = buffers;
                        length = length - packageSize;
                        res = HexUtils.byteToHexString(buffer);
                        ota[0] = 0x02;
                        ota[1] = blockNumber;
                        ota[2] = (byte) ~blockNumber;
                        System.arraycopy(read, 0, ota, 3, read.length);
                        ota[packageSize + 3] = CSCRCUtils.calcCrc(read)[0];
                        ota[packageSize + 4] = CSCRCUtils.calcCrc(read)[1];
                        Log.e("文件数据：", "ota size ====== " + ota.length + "\n" + HexUtils.byteToHexString(ota));
                        writes(ota);

                        Thread.sleep(8000);

//                        if (
//                                blockNumber == 40 || blockNumber == 60 || blockNumber == 80 || blockNumber == 100 || blockNumber == 120||
//                                blockNumber == -40 || blockNumber == -60 || blockNumber == -80 || blockNumber == -100 || blockNumber == -120
//                        ){
//                            Log.e("TAG", "run: 执行GC" );
//                            System.gc();
//                        }
//
//                        if (Xmodem2.responseACK == 15){
//                            writes(ota);
//                            Thread.sleep(15000);
//                            if (Xmodem2.responseACK == 15){
//                                writeHandSet(HexUtils.hexStringToBytes("00"));
//                                Toast.makeText(LongSanTestActivity.this,"升级失败",Toast.LENGTH_SHORT).show();
//                                return;
//                            }
//                        }

                        blockNumber = (byte) ((++blockNumber) % 256);
                        Log.e("11111", "包序号: " + blockNumber);
                    }

                    byte[] elss = new byte[packageSize];
                    System.arraycopy( buffer, 0, elss, 0, buffer.length);
                    //不足补1A
                    for (int i = buffer.length; i < packageSize; i++) {
                        elss[i] = (byte) 0X1A;
                    }
                    buffer = new byte[packageSize+5];
                    buffer[0] = 0x02;
                    buffer[1] = blockNumber;
                    buffer[2] = (byte) ~blockNumber;
                    System.arraycopy( elss, 0, buffer, 3, packageSize);
                    buffer[packageSize +3] = CSCRCTmpUtils.getCRC16ReturnBytes(elss,elss.length)[0];
                    buffer[packageSize +4] = CSCRCTmpUtils.getCRC16ReturnBytes(elss,elss.length)[1];
                    writes(buffer);
                    //关闭
                    in.close();
                    res = HexUtils.byteToHexString(buffer);
                    Log.e("文件数据："," new szie ===== " + buffer.length + "\n" + HexUtils.byteToHexString(buffer));

//            writeHandSet(res);

                }

                catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
}
