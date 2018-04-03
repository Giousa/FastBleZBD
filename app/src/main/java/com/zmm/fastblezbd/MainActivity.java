package com.zmm.fastblezbd;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleIndicateCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.zmm.fastblezbd.adapter.BleListAdapter;
import com.zmm.fastblezbd.utils.ToastUtils;
import com.zmm.fastblezbd.utils.TypeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements BleListAdapter.OnDeviceClickListener {

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    //点击按钮
    @BindView(R.id.scan_ble)
    Button mScanBle;
    @BindView(R.id.btn_start)
    Button mBtnStart;
    @BindView(R.id.btn_pause)
    Button mBtnPause;
    @BindView(R.id.btn_stop)
    Button mBtnStop;
    @BindView(R.id.btn_clear)
    Button mBtnClear;
    @BindView(R.id.btn_device_set)
    Button mBtnDeviceSet;


    //日志
    @BindView(R.id.tv_content)
    TextView mTvContent;

    //返回参数
    @BindView(R.id.tv_model)
    TextView mTvModel;
    @BindView(R.id.tv_speed_level)
    TextView mTvSpeedLevel;
    @BindView(R.id.tv_speed_value)
    TextView mTvSpeedValue;
    @BindView(R.id.tv_speed_offset)
    TextView mTvSpeedOffset;
    @BindView(R.id.tv_spasm_num)
    TextView mTvSpasmNum;
    @BindView(R.id.tv_spasm_level)
    TextView mTvSpasmLevel;
    @BindView(R.id.tv_resistance)
    TextView mTvResistance;
    @BindView(R.id.tv_intelligence)
    TextView mTvIntelligence;
    @BindView(R.id.tv_direction)
    TextView mTvDirection;


    //设置参数
    @BindView(R.id.rb_model_bei)
    RadioButton mRbModelBei;
    @BindView(R.id.rb_model_zhu)
    RadioButton mRbModelZhu;
    @BindView(R.id.rb_intel_open)
    RadioButton mRbIntelOpen;
    @BindView(R.id.rb_intel_close)
    RadioButton mRbIntelClose;
    @BindView(R.id.rb_direc_zheng)
    RadioButton mRbDirecZheng;
    @BindView(R.id.rb_direc_fan)
    RadioButton mRbDirecFan;
    @BindView(R.id.et_time)
    EditText mEtTime;
    @BindView(R.id.et_speed)
    EditText mEtSpeed;
    @BindView(R.id.et_spasm)
    EditText mEtSpasm;
    @BindView(R.id.et_resistance)
    EditText mEtResistance;



    private BleListAdapter mBleListAdapter;
    private ProgressDialog progressDialog;


    private static final int REQUEST_CODE_OPEN_GPS = 1;
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2;

    private BleDevice mBleDevice;


    private int mRes;
    private int mSpasmLevel;
    private int mSpasmNum;
    private int mOffset;
    private int mSpeedValue;
    private int mSpeedLevel;
    private byte mModel;
    private byte mIntelligence;
    private byte mDirection;

    private boolean isOk = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        initRecyclerView();
        initBLE();


    }


    private void initRecyclerView() {

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBleListAdapter = new BleListAdapter(this);

        mRecyclerView.setAdapter(mBleListAdapter);
        mBleListAdapter.setOnDeviceClickListener(this);

        progressDialog = new ProgressDialog(this);
    }

    private void initBLE() {
        BleManager.getInstance().
                init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setMaxConnectCount(7)
                .setOperateTimeout(30000);

        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setScanTimeOut(0)              // 扫描超时时间，可选，默认10秒；小于等于0表示不限制扫描时间
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);


    }



    @OnClick({R.id.scan_ble, R.id.btn_start, R.id.btn_device_set, R.id.btn_pause, R.id.btn_stop, R.id.btn_clear})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.scan_ble:

                mRecyclerView.setVisibility(View.VISIBLE);
                if (mScanBle.getText().equals(getString(R.string.start_scan))) {
                    checkPermissions();
//                    startScan();
                } else if (mScanBle.getText().equals(getString(R.string.stop_scan))) {
                    BleManager.getInstance().cancelScan();
                }
                break;

            case R.id.btn_start:

                if(isOk){
                    writeBleData(CommonConfig.startByte);
                }else {
                    ToastUtils.SimpleToast("请连接设备");
                }
                break;

            case R.id.btn_device_set:
                if(isOk){
                    setParam();
//                    writeBleData(CommonConfig.setByte);
                }else {
                    ToastUtils.SimpleToast("请连接设备");
                }
                break;

            case R.id.btn_pause:
                if(isOk){
                    writeBleData(CommonConfig.pauseByte);
                }else {
                    ToastUtils.SimpleToast("请连接设备");
                }
                break;

            case R.id.btn_stop:
                if(isOk){
                    writeBleData(CommonConfig.stopByte);
                }else {
                    ToastUtils.SimpleToast("请连接设备");
                }
                break;

            case R.id.btn_clear:
                mTvContent.setText("");
                break;
        }
    }


    /**
     * 打开订阅
     */
    private void indicateBle() {
        BleManager.getInstance().indicate(
                mBleDevice,
                "0000ffe1-0000-1000-8000-00805f9b34fb",
                "0000ffe2-0000-1000-8000-00805f9b34fb",
                new BleIndicateCallback() {


                    @Override
                    public void onIndicateSuccess() {
                        // 打开通知操作成功
//                        toast("通知成功");
                        ToastUtils.SimpleToast("读写成功");
                        mTvContent.append("读写成功\n");
                        isOk = true;
                        //
                        mRecyclerView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onIndicateFailure(BleException exception) {
                        // 打开通知操作失败
//                        toast("通知失败");
                        ToastUtils.SimpleToast("读写失败");
                        mTvContent.append("读写失败\n");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        // 打开通知后，设备发过来的数据将在这里出现
                        int length = data.length;
                        String s = Arrays.toString(data);
                        mTvContent.append("数据个数："+length+"\n");
                        mTvContent.append("data：" + s + "\n");

                        if (length == 9) {
                            mModel = data[4];
                            mSpeedLevel = data[5] & 0xFF;
                            mSpeedValue = data[6] & 0xFF;
                            mOffset = data[7] & 0xFF;
                            mSpasmNum = data[8] & 0xFF;

                        } else if (length == 4) {
                            mSpasmLevel = data[0] & 0xFF;
                            mRes = data[1] & 0xFF;
                            mIntelligence = data[2];
                            mDirection = data[3];
                        }else if(length == 13){
                            mModel = data[4];
                            mSpeedLevel = data[5] & 0xFF;
                            mSpeedValue = data[6] & 0xFF;
                            mOffset = data[7] & 0xFF;
                            mSpasmNum = data[8] & 0xFF;
                            mSpasmLevel = data[9] & 0xFF;
                            mRes = data[10] & 0xFF;
                            mIntelligence = data[11];
                            mDirection = data[12];
                        }


                        dealData();
                    }
                });
    }

    /**
     * 处理数据
     */
    private void dealData() {
        if (mModel == 0x01) {
            mTvModel.setText("模式:被动");
        } else {
            mTvModel.setText("模式:主动");
        }

        mTvSpeedLevel.setText("速度档位：" + mSpeedLevel);
        mTvSpeedValue.setText("转速：" + mSpeedValue);
        mTvSpeedOffset.setText("偏移：" + mOffset);
        mTvSpasmLevel.setText("痉挛等级：" + mSpasmLevel);
        mTvSpasmNum.setText("痉挛次数：" + mSpasmNum);
        mTvResistance.setText("阻力："+mRes);

        if (mIntelligence == 0x40) {
            mTvIntelligence.setText("智能：关闭");
        } else {
            mTvIntelligence.setText("智能：开启");
        }

        if (mDirection == 0x50) {
            mTvDirection.setText("方向：反转");
        } else {
            mTvDirection.setText("方向：正向");
        }

    }


    private void writeBleData(byte[] data) {
        BleManager.getInstance().write(
                mBleDevice,
                "0000ffe1-0000-1000-8000-00805f9b34fb",
                "0000ffe3-0000-1000-8000-00805f9b34fb",
                data,
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        // 发送数据到设备成功
                        mTvContent.append("writeBleData:发送数据到设备成功\n");
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        // 发送数据到设备失败
                        mTvContent.append("writeBleData:发送数据到设备失败\n");

                    }
                });
    }


    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
            return;
        }

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }

    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.notifyTitle)
                            .setMessage(R.string.gpsNotifyMsg)
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                            .setPositiveButton(R.string.setting,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                } else {
                    setScanRule();
                    startScan();
                }
                break;
        }
    }

    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (checkGPSIsOpen()) {
                setScanRule();
                startScan();
            }
        }
    }


    //扫描规则
    private void setScanRule() {
//        name: SOPLAR_HRP_0075  mac: 8C:DE:52:0E:40:D3


    }

    //开始扫描
    private void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mBleListAdapter.clearScanDevice();
                mBleListAdapter.notifyDataSetChanged();
                mScanBle.setText(getString(R.string.stop_scan));
            }

            @Override
            public void onScanning(final BleDevice bleDevice) {

                if (!TextUtils.isEmpty(bleDevice.getDevice().getName())) {
                    System.out.println("onScanning bleDevice::" + bleDevice.getDevice().getName());

                    UIUtils.runInMainThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvContent.append(bleDevice.getDevice().getName() + "\n");
                            mBleListAdapter.addDevice(bleDevice);
                            mBleListAdapter.notifyDataSetChanged();
                        }
                    });

                }

            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                mScanBle.setText(getString(R.string.start_scan));
            }
        });
    }


    @Override
    public void onConnect(BleDevice bleDevice) {
        if (!BleManager.getInstance().isConnected(bleDevice)) {
            BleManager.getInstance().cancelScan();
            connect(bleDevice);
        }
    }

    private void connect(BleDevice bleDevice) {

        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                progressDialog.show();
                mTvContent.append("正在连接...\n");
            }

            @Override
            public void onConnectFail(BleException exception) {
                progressDialog.dismiss();
//                toast("连接失败");
                mTvContent.append("连接失败\n");
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {

                mBleDevice = bleDevice;
                progressDialog.dismiss();
                mBleListAdapter.addDevice(bleDevice);
                mBleListAdapter.notifyDataSetChanged();

//                toast("连接成功");
                ToastUtils.SimpleToast("连接成功");
                mTvContent.append("连接成功\n");
                indicateBle();


//                subscriberWrite(bleDevice);
//                subscriberNotify(bleDevice);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();

                mBleListAdapter.removeDevice(bleDevice);
                mBleListAdapter.notifyDataSetChanged();

                if (isActiveDisConnected) {
                    ToastUtils.SimpleToast("断开了");
                    mTvContent.append("断开了\n");
                } else {
                    ToastUtils.SimpleToast("连接断开");
                    mTvContent.append("连接断开\n");

                }


            }
        });
    }


    @Override
    public void onDisConnect(BleDevice bleDevice) {

    }

    @Override
    public void onDetail(BleDevice bleDevice) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
    }


    private byte model;
    private byte time;
    private byte speed;
    private byte spasm;
    private byte resistance;
    private byte intelligence;
    private byte direction;

    /**
     * 设置参数
     */
    private void setParam() {

        StringBuffer sb = new StringBuffer();

        sb.append("设置参数：");

        if(mRbModelBei.isChecked()){
            model = 0x01;
            sb.append("模式：被动，");
        }else {
            model = 0x02;
            sb.append("模式：主动，");
        }



        Editable mEtTimeText = mEtTime.getText();
        if(mEtTimeText == null){
            time = 0x05;
            sb.append("时间：5分，");
        }else {
            int timeInt = Integer.valueOf(mEtTimeText.toString().trim());
            time = TypeUtil.int2Byte(timeInt);
            System.out.println("时间参数:"+timeInt);
            sb.append("时间："+timeInt+",");
        }

        Editable mEtSpeedText = mEtSpeed.getText();
        if(mEtSpeedText == null){
            speed = 0x01;
            sb.append("速度：1档，");
        }else {
            int speedInt = Integer.valueOf(mEtSpeedText.toString().trim());
            speed = TypeUtil.int2Byte(speedInt);
            System.out.println("速度参数:"+speedInt);
            sb.append("速度："+speedInt+"档，");
        }

        Editable mEtSpasmText = mEtSpasm.getText();
        if(mEtSpasmText == null){
            spasm = 0x01;
            sb.append("痉挛等级：1档，");
        }else {
            int spasmInt = Integer.valueOf(mEtSpasmText.toString().trim());
            spasm = TypeUtil.int2Byte(spasmInt);
            System.out.println("痉挛参数:"+spasmInt);
            sb.append("痉挛等级："+spasmInt+"档，");
        }


        Editable mEtResistanceText = mEtResistance.getText();
        if(mEtResistanceText == null){
            resistance = 0x01;
            sb.append("阻力：1档，");
        }else {
            int resistanceInt = Integer.valueOf(mEtResistanceText.toString().trim());
            resistance = TypeUtil.int2Byte(resistanceInt);
            System.out.println("阻力参数:"+resistanceInt);
            sb.append("阻力："+resistanceInt+"档，");
        }

        if(mRbIntelOpen.isChecked()){
            intelligence = 0x41;
            sb.append("智能模式：开启，");
        }else {
            intelligence = 0x40;
            sb.append("智能模式：关闭，");
        }

        if(mRbDirecZheng.isChecked()){
            direction = 0x51;
            sb.append("方向：正转。");
        }else {
            direction = 0x50;
            sb.append("方向：反转。");
        }

        byte[] setByte = {(byte) 0xA3,(byte)0x20,(byte)0x21,(byte)0x81,model,time,speed,spasm,resistance,intelligence,direction};

//        System.out.println("设置参数:"+ Arrays.toString(setByte));
        mTvContent.append(sb.toString()+"\n");
        mTvContent.append("设置参数：" + Arrays.toString(setByte) + "\n");
        writeBleData(setByte);
    }



}
