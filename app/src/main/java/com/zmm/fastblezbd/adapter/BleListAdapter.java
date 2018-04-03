package com.zmm.fastblezbd.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;
import com.zmm.fastblezbd.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Author:zhangmengmeng
 * Date:2018/2/2
 * Time:上午10:34
 */

public class BleListAdapter extends RecyclerView.Adapter<BleListAdapter.ViewHolder> {


    private Context context;
    private List<BleDevice> bleDeviceList;

    public BleListAdapter(Context context) {
        this.context = context;
        bleDeviceList = new ArrayList<>();
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.bleinfo__item, parent, false));

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final BleDevice bleDevice = bleDeviceList.get(position);

        if (bleDevice != null) {
            boolean isConnected = BleManager.getInstance().isConnected(bleDevice);
            String name = bleDevice.getName();
            String mac = bleDevice.getMac();
            holder.mTvName.setText(name);
            holder.mTvMac.setText(mac);


        }

        holder.mBtnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    System.out.println("正在连接.....");
                    mListener.onConnect(bleDevice);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return bleDeviceList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

//        @BindView(R.id.tv_name)
//        TextView mTvName;
//        @BindView(R.id.tv_mac)
//        TextView mTvMac;
//        @BindView(R.id.ll_item)
//        LinearLayout mLlItem;
        TextView mTvName;
        TextView mTvMac;
        LinearLayout mLlItem;
        Button mBtnConnect;

        ViewHolder(View itemView) {
            super(itemView);

//            ButterKnife.bind(context,itemView);
            mTvName = itemView.findViewById(R.id.tv_name);
            mTvMac = itemView.findViewById(R.id.tv_mac);
            mLlItem = itemView.findViewById(R.id.ll_item);
            mBtnConnect = itemView.findViewById(R.id.btn_connect);
        }
    }


    public void addDevice(BleDevice bleDevice) {
        removeDevice(bleDevice);
        bleDeviceList.add(bleDevice);
    }

    public void removeDevice(BleDevice bleDevice) {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            BleDevice device = bleDeviceList.get(i);
            if (bleDevice.getKey().equals(device.getKey())) {
                bleDeviceList.remove(i);
            }
        }
    }

    public void clearConnectedDevice() {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            BleDevice device = bleDeviceList.get(i);
            if (BleManager.getInstance().isConnected(device)) {
                bleDeviceList.remove(i);
            }
        }
    }

    public void clearScanDevice() {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            BleDevice device = bleDeviceList.get(i);
            if (!BleManager.getInstance().isConnected(device)) {
                bleDeviceList.remove(i);
            }
        }
    }

    public void clear() {
        clearConnectedDevice();
        clearScanDevice();
    }

    public interface OnDeviceClickListener {
        void onConnect(BleDevice bleDevice);

        void onDisConnect(BleDevice bleDevice);

        void onDetail(BleDevice bleDevice);
    }

    private OnDeviceClickListener mListener;

    public void setOnDeviceClickListener(OnDeviceClickListener listener) {
        this.mListener = listener;
    }

}
