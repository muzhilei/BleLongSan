package com.homjay.longsandemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleRssiCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.homjay.longsandemo.adapter.DeviceAdapter;
import com.homjay.longsandemo.comm.ObserverManager;
import com.homjay.longsandemo.utlis.ClsUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;

    private LinearLayout layout_setting;
    private TextView txt_setting;
    private Button btn_scan,btn_auto_handset;
    private EditText et_name, et_mac, et_uuid,et_check;
    private Switch sw_auto;
    private ImageView img_loading;

    private Animation operatingAnim;
    private DeviceAdapter mDeviceAdapter;
    private ProgressDialog progressDialog;
    private BleDevice weightBle;
    private boolean isAuto = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);

    }


    @Override
    protected void onResume() {
        super.onResume();
        showConnectedDevice();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                if (btn_scan.getText().equals(getString(R.string.start_scan))) {
//                    checkPermissions();

                    setScanRule();
                    startScan();
                } else if (btn_scan.getText().equals(getString(R.string.stop_scan))) {
                    BleManager.getInstance().cancelScan();
                }
                break;

            case R.id.txt_setting:
                if (layout_setting.getVisibility() == View.VISIBLE) {
                    layout_setting.setVisibility(View.GONE);
                    txt_setting.setText(getString(R.string.expand_search_settings));
                } else {
                    layout_setting.setVisibility(View.VISIBLE);
                    txt_setting.setText(getString(R.string.retrieve_search_settings));
                }
                break;
            case R.id.btn_auto_handset:
                isAuto = true;
                startScan();
                break;
        }
    }


    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btn_scan = (Button) findViewById(R.id.btn_scan);
        btn_auto_handset = (Button) findViewById(R.id.btn_auto_handset);
        btn_scan.setText(getString(R.string.start_scan));
        btn_scan.setOnClickListener(this);
        btn_auto_handset.setOnClickListener(this);

        et_name = (EditText) findViewById(R.id.et_name);
        et_mac = (EditText) findViewById(R.id.et_mac);
        et_uuid = (EditText) findViewById(R.id.et_uuid);
        et_check = (EditText) findViewById(R.id.et_check);
        sw_auto = (Switch) findViewById(R.id.sw_auto);

        layout_setting = (LinearLayout) findViewById(R.id.layout_setting);
        txt_setting = (TextView) findViewById(R.id.txt_setting);
        txt_setting.setOnClickListener(this);
        layout_setting.setVisibility(View.GONE);
        txt_setting.setText(getString(R.string.expand_search_settings));

        img_loading = (ImageView) findViewById(R.id.img_loading);
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        operatingAnim.setInterpolator(new LinearInterpolator());
        progressDialog = new ProgressDialog(this);

        mDeviceAdapter = new DeviceAdapter(this);
        mDeviceAdapter.setOnDeviceClickListener(new DeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onConnect(BleDevice bleDevice) {
                if (!BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().cancelScan();
                    connect(bleDevice);
                }
            }

            @Override
            public void onDisConnect(final BleDevice bleDevice) {
                if (BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().disconnect(bleDevice);
                }
            }

            @Override
            public void onDetail(BleDevice bleDevice) {
//                if (BleManager.getInstance().isConnected(bleDevice)) {
//                    Intent intent = new Intent(MainActivity.this, OperationActivity.class);
//                    intent.putExtra(OperationActivity.KEY_DATA, bleDevice);
//                    startActivity(intent);
//                }
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,LongSanTestActivity.class);
                intent.putExtra("ble",bleDevice);
                intent.putExtra("weight",weightBle);
                startActivity(intent);

            }
        });
        ListView listView_device = (ListView) findViewById(R.id.list_device);
        listView_device.setAdapter(mDeviceAdapter);
    }

    private void showConnectedDevice() {
        List<BleDevice> deviceList = BleManager.getInstance().getAllConnectedDevice();
        mDeviceAdapter.clearConnectedDevice();
        for (BleDevice bleDevice : deviceList) {
            mDeviceAdapter.addDevice(bleDevice);
        }
        mDeviceAdapter.notifyDataSetChanged();
    }


    private void setScanRule() {
        String[] uuids;
        String str_uuid = et_uuid.getText().toString();
        if (TextUtils.isEmpty(str_uuid)) {
            uuids = null;
        } else {
            uuids = str_uuid.split(",");
        }
        UUID[] serviceUuids = null;
        if (uuids != null && uuids.length > 0) {
            serviceUuids = new UUID[uuids.length];
            for (int i = 0; i < uuids.length; i++) {
                String name = uuids[i];
                String[] components = name.split("-");
                if (components.length != 5) {
                    serviceUuids[i] = null;
                } else {
                    serviceUuids[i] = UUID.fromString(uuids[i]);
                }
            }
        }

        String[] names;
        String str_name = et_name.getText().toString();
        if (TextUtils.isEmpty(str_name)) {
            names = null;
        } else {
            names = str_name.split(",");
        }

        String mac = et_mac.getText().toString();

        boolean isAutoConnect = sw_auto.isChecked();

        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setServiceUuids(serviceUuids)      // 只扫描指定的服务的设备，可选
                .setDeviceName(true, names)   // 只扫描指定广播名的设备，可选
                .setDeviceMac(mac)                  // 只扫描指定mac的设备，可选
                .setAutoConnect(isAutoConnect)      // 连接时的autoConnect参数，可选，默认false
                .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
        BleManager.getInstance().setSplitWriteNum(20);//发送指令字节数控制
    }

    private void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mDeviceAdapter.clearScanDevice();
                mDeviceAdapter.notifyDataSetChanged();
                img_loading.startAnimation(operatingAnim);
                img_loading.setVisibility(View.VISIBLE);
                btn_scan.setText(getString(R.string.stop_scan));
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                if (bleDevice.getMac().equals("C8:B2:1E:CB:4F:B9")){
                    weightBle = bleDevice;
                }

                String bleName = bleDevice.getName() == null?"":bleDevice.getName();

                if (TextUtils.isEmpty(et_check.getText().toString())){
                    mDeviceAdapter.addDevice(bleDevice);
                    mDeviceAdapter.notifyDataSetChanged();
                }else {
                    if (bleName.contains(et_check.getText().toString())){
                        mDeviceAdapter.addDevice(bleDevice);
                        mDeviceAdapter.notifyDataSetChanged();
                    }
                }

                if (bleName.contains("XIAODUO") && isAuto){
                    setPair(bleDevice);
                }

            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
                btn_scan.setText(getString(R.string.start_scan));
            }
        });
    }

    private void setPair(BleDevice bleDevice){
        try {
            if (bleDevice.getDevice().getBondState() == BluetoothDevice.BOND_NONE){
                //通过工具类ClsUtils,调用createBond方法
//                if (isConnocet){
//                    return;
//                }
                boolean isSuccess = ClsUtils.createBond(bleDevice.getDevice().getClass(), bleDevice.getDevice());
                if (isSuccess){
                    Log.e("setPair","配对成功" + bleDevice.getDevice().getName());
//                    if (!isConnocet){
//                        connect(bleDevice.getDevice());
//                    }
                    connect(bleDevice.getDevice());
//                            BleManager.getInstance().getBluetoothAdapter().getProfileProxy(getActivity(),mListener,BluetoothProfile.HID_DEVICE);
//                            Method method = mBluetoothProfile.getClass().getMethod("connect",new Class[]{BluetoothDevice.class});
//                            method.invoke(mBluetoothProfile,bleDevice.getDevice());

//                            BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
//                                @Override
//                                public void onStartConnect() {
//
//                                }
//
//                                @Override
//                                public void onConnectFail(BleDevice bleDevice, BleException exception) {
//                                    TVLog.e("连接失败" + bleDevice.getName() + exception.getDescription());
//                                    isConnocet = false;
//                                }
//
//                                @Override
//                                public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
//                                    isConnocet = false;
//                                    TVLog.e("连接成功" + bleDevice.getName());
//                                    BleManager.getInstance().notify(bleDevice, CmdConstants.UUID_Electricity_Server, CmdConstants.UUID_Electricity_Notify, new BleNotifyCallback() {
//                                        @Override
//                                        public void onNotifySuccess() {
////                                Toast.makeText(LongSanTestActivity.this,"weight通知打开成功",Toast.LENGTH_SHORT).show();
//                                        }
//
//                                        @Override
//                                        public void onNotifyFailure(BleException exception) {
////                                Toast.makeText(LongSanTestActivity.this,"weight通知打开失败"+exception.getDescription(),Toast.LENGTH_SHORT).show();
//                                        }
//
//                                        @Override
//                                        public void onCharacteristicChanged(byte[] data) {
//                                            final String dataStr =  HexUtil.formatHexString(data);
//                                            TVLog.e( "weight onCharacteristicChanged: " + "接收：" + HexUtils.byteToHexString(data) );
//                                            if ( HexUtils.byteToHexString(data).length() == 2){
//                                                tv_value_ele.setText(HexUtils.hexToDec(HexUtils.byteToHexString(data)) + "%");
//                                            }
//                                        }
//                                    });
//                                }
//
//                                @Override
//                                public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
//                                    isConnocet = false;
//                                }
//                            });
                }else {
                    Log.e("setPair","配对失败" + bleDevice.getDevice().getName());
                }
            }
//                    else {
//                        ClsUtils.removeBond(bleDevice.getDevice().getClass(),bleDevice.getDevice());
//                    }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void connect(final BluetoothDevice bluetoothDevice) {
        if(bluetoothDevice.getBluetoothClass().getDeviceClass() == 1344){
//            final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
                @Override
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    Log.e("btclass", profile + "");

                    if (profile == getInputDeviceHiddenConstant()) {
                        Class instance = null;
                        try {
                            //instance = Class.forName("android.bluetooth.IBluetoothInputDevice");
//                            instance = Class.forName("android.bluetooth.IBluetoothInputDevice");
//                            Method connect = instance.getDeclaredMethod("connect", BluetoothDevice.class);
//                            Object value = connect.invoke(proxy, bluetoothDevice);
                            Method method = proxy.getClass().getMethod("connect",new Class[]{BluetoothDevice.class});
                            Object value = method.invoke(proxy,bluetoothDevice);
//                            if (value.toString().equals("true")){
//                                isConnocet = true;
//                            }else {
//                                isConnocet = false;
//                            }

                            Log.e("btclass", value.toString());
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }



                    }
                }

                @Override
                public void onServiceDisconnected(int profile) {

                }
            };

            BleManager.getInstance().getBluetoothAdapter().getProfileProxy(this, mProfileListener,getInputDeviceHiddenConstant());


        }

    }

    public static int getInputDeviceHiddenConstant() {
        Class<BluetoothProfile> clazz = BluetoothProfile.class;
        for (Field f : clazz.getFields()) {
            int mod = f.getModifiers();
            if (Modifier.isStatic(mod) && Modifier.isPublic(mod) && Modifier.isFinal(mod)) {
                try {
                    if (f.getName().equals("INPUT_DEVICE")) {
                        return f.getInt(null);
                    }
                } catch (Exception e) {
                    Log.e("", e.toString(), e);
                }
            }
        }
        return -1;
    }





    private void connect(final BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                progressDialog.show();
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
                btn_scan.setText(getString(R.string.start_scan));
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, getString(R.string.connect_fail), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
                //接受通知指令字节数控制
                BleManager.getInstance().setMtu(bleDevice, 512, new BleMtuChangedCallback() {
                    @Override
                    public void onSetMTUFailure(BleException exception) {
                        Log.e(TAG, "onSetMTUFailure: "+exception.getDescription() );
                    }

                    @Override
                    public void onMtuChanged(int mtu) {
                        Log.e(TAG, "onMtuChanged: " + mtu );
                    }
                });
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();

                mDeviceAdapter.removeDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();

                if (isActiveDisConnected) {
                    Toast.makeText(MainActivity.this, getString(R.string.active_disconnected), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.disconnected), Toast.LENGTH_LONG).show();
                    ObserverManager.getInstance().notifyObserver(bleDevice);
                }

            }
        });
    }

    private void readRssi(BleDevice bleDevice) {
        BleManager.getInstance().readRssi(bleDevice, new BleRssiCallback() {
            @Override
            public void onRssiFailure(BleException exception) {
                Log.i(TAG, "onRssiFailure" + exception.toString());
            }

            @Override
            public void onRssiSuccess(int rssi) {
                Log.i(TAG, "onRssiSuccess: " + rssi);
            }
        });
    }

    private void setMtu(BleDevice bleDevice, int mtu) {
        BleManager.getInstance().setMtu(bleDevice, mtu, new BleMtuChangedCallback() {
            @Override
            public void onSetMTUFailure(BleException exception) {
                Log.i(TAG, "onsetMTUFailure" + exception.toString());
            }

            @Override
            public void onMtuChanged(int mtu) {
                Log.i(TAG, "onMtuChanged: " + mtu);
            }
        });
    }
}
