package com.homjay.longsandemo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Homjay
 * @date 2022/11/21 15:28
 * @describe
 */
public class MeansureActivity extends AppCompatActivity {

    private TextView tvSure,tvDate,tvOpen,tvControl,tvClear;
    private EditText etInput;
    private BleDevice bleDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meansure);

        bleDevice = getIntent().getParcelableExtra("ble");


        tvSure = findViewById(R.id.tv_sure);
        tvDate = findViewById(R.id.tv_date);
        tvOpen = findViewById(R.id.tv_open);
        etInput = findViewById(R.id.et_input);
        tvControl = findViewById(R.id.tv_control);
        tvClear = findViewById(R.id.tv_clear);


        tvClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvControl.setText("");
            }
        });

        tvOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BleManager.getInstance().notify(bleDevice, CmdConstants.UUID_Server, CmdConstants.UUID_Notify, new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        Toast.makeText(MeansureActivity.this,"通知打开成功",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        Toast.makeText(MeansureActivity.this,"通知打开失败",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCharacteristicChanged(final byte[] data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addText(tvControl,HexUtil.formatHexString(data,true));
                            }
                        });

                    }
                });
            }
        });


        tvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etInput.getEditableText().toString())){
                    Toast.makeText(MeansureActivity.this,"指令不能为空",Toast.LENGTH_SHORT).show();
                }else {
                    StringBuilder builder = new StringBuilder();
                    builder.append(etInput.getEditableText().toString());
                    builder.append(CmdUtils.getCheck(etInput.getEditableText().toString()));
                    Toast.makeText(MeansureActivity.this,"指令:" + builder.toString(),Toast.LENGTH_SHORT).show();
                    byte[] data = HexUtils.hexStringToBytes(builder.toString());
                    BleManager.getInstance().write(bleDevice, CmdConstants.UUID_Server, CmdConstants.UUID_Write, data, new BleWriteCallback() {
                        @Override
                        public void onWriteSuccess(int current, int total, byte[] justWrite) {
                            Toast.makeText(MeansureActivity.this,"指令输入成功",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onWriteFailure(BleException exception) {
                            Toast.makeText(MeansureActivity.this,"数据写入失败：" + exception.getDescription(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = timeStampDate();
                StringBuilder builder = new StringBuilder();
                builder.append(CmdConstants.Cmd_Start);
                builder.append(date);
                builder.append(CmdUtils.getCheck(date));
                byte[] hex = HexUtils.hexStringToBytes(builder.toString());
                Toast.makeText(MeansureActivity.this,date,Toast.LENGTH_SHORT).show();
                BleManager.getInstance().write(bleDevice, CmdConstants.UUID_Server, CmdConstants.UUID_Write, hex, new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        Toast.makeText(MeansureActivity.this,"时间同步成功",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Toast.makeText(MeansureActivity.this,"数据写入失败：" + exception.getDescription(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }


    private  String timeStampDate() {
        Date nowTime = new Date(System.currentTimeMillis());
        SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyyMMddHHmmdd");
        return sdFormatter.format(nowTime);
    }

    private void addText(TextView textView, String content) {
        textView.append(content);
        textView.append("\n");
        int offset = textView.getLineCount() * textView.getLineHeight();
        if (offset > textView.getHeight()) {
            textView.scrollTo(0, offset - textView.getHeight());
        }
    }
}
