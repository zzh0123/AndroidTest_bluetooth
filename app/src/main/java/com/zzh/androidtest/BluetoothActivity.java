package com.zzh.androidtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;



public class BluetoothActivity extends AppCompatActivity implements
        OnClickListener, OnItemClickListener, OnCheckedChangeListener,
        BlueConnectTask.BlueConnectListener, InputDialogFragment.InputCallbacks, BlueAcceptTask.BlueAcceptListener {
    private static final String TAG = "BluetoothActivity";
    private CheckBox ck_bluetooth;
    private TextView tv_discovery;
    private ListView lv_bluetooth;
    private BluetoothAdapter mBluetooth;
    private ArrayList<BlueDevice> mDeviceList = new ArrayList<BlueDevice>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        bluetoothPermissions();
        ck_bluetooth = (CheckBox) findViewById(R.id.ck_bluetooth);
        tv_discovery = (TextView) findViewById(R.id.tv_discovery);
        lv_bluetooth = (ListView) findViewById(R.id.lv_bluetooth);
        if (BluetoothUtil.getBlueToothStatus(this) == true) {
            ck_bluetooth.setChecked(true);
        }
        ck_bluetooth.setOnCheckedChangeListener(this);
        tv_discovery.setOnClickListener(this);
        mBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (mBluetooth == null) {
            Toast.makeText(this, "???????????????????????????", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // ?????????????????????????????????????????????
    private void bluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    /**
     * ??????onRequestPermissionsResult??????
     * ?????????????????????????????????,???????????????
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (BluetoothUtil.getBlueToothStatus(this) == true) {
                ck_bluetooth.setChecked(true);
            }
            ck_bluetooth.setOnCheckedChangeListener(this);
            tv_discovery.setOnClickListener(this);
            mBluetooth = BluetoothAdapter.getDefaultAdapter();
            if (mBluetooth == null) {
                Toast.makeText(this, "???????????????????????????", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "?????????????????????", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.ck_bluetooth) {
            if (isChecked == true) {
                beginDiscovery();
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                startActivityForResult(intent, 1);
                // ?????????????????????????????????????????????????????????
                mHandler.postDelayed(mAccept, 1000);
            } else {
                cancelDiscovery();
                BluetoothUtil.setBlueToothStatus(this, false);
                mDeviceList.clear();
                BlueListAdapter adapter = new BlueListAdapter(this, mDeviceList);
                lv_bluetooth.setAdapter(adapter);
            }
        }
    }

    private Runnable mAccept = new Runnable() {
        @Override
        public void run() {
            if (mBluetooth.getState() == BluetoothAdapter.STATE_ON) {
                BlueAcceptTask acceptTask = new BlueAcceptTask(true);
                acceptTask.setBlueAcceptListener(BluetoothActivity.this);
                acceptTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                mHandler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_discovery) {
            beginDiscovery();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "??????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "???????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Runnable mRefresh = new Runnable() {
        @Override
        public void run() {
            beginDiscovery();
            mHandler.postDelayed(this, 2000);
        }
    };

    private void beginDiscovery() {
        if (mBluetooth.isDiscovering() != true) {
            mDeviceList.clear();
            BlueListAdapter adapter = new BlueListAdapter(BluetoothActivity.this, mDeviceList);
            lv_bluetooth.setAdapter(adapter);
            tv_discovery.setText("????????????????????????");
            mBluetooth.startDiscovery();
        }
    }

    private void cancelDiscovery() {
        mHandler.removeCallbacks(mRefresh);
        tv_discovery.setText("????????????????????????");
        if (mBluetooth.isDiscovering() == true) {
            mBluetooth.cancelDiscovery();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mHandler.postDelayed(mRefresh, 50);
        blueReceiver = new BluetoothReceiver();
        //????????????????????????????????????IntentFilter?????????addAction???????????????
        IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        foundFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        foundFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(blueReceiver, foundFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelDiscovery();
        unregisterReceiver(blueReceiver);
    }

    private BluetoothReceiver blueReceiver;

    private class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive action=" + action);
            // ????????????????????????????????????
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BlueDevice item = new BlueDevice(device.getName(), device.getAddress(), device.getBondState() - 10);
                mDeviceList.add(item);
                BlueListAdapter adapter = new BlueListAdapter(BluetoothActivity.this, mDeviceList);
                lv_bluetooth.setAdapter(adapter);
                lv_bluetooth.setOnItemClickListener(BluetoothActivity.this);
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                mHandler.removeCallbacks(mRefresh);
                tv_discovery.setText("????????????????????????");
            } else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    tv_discovery.setText("????????????" + device.getName());
                } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    tv_discovery.setText("????????????" + device.getName());
                    mHandler.postDelayed(mRefresh, 50);
                } else if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    tv_discovery.setText("????????????" + device.getName());
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        cancelDiscovery();
        BlueDevice item = mDeviceList.get(position);
        BluetoothDevice device = mBluetooth.getRemoteDevice(item.address);
        try {
            if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                Log.d(TAG, "????????????");
                Boolean result = (Boolean) createBondMethod.invoke(device);
            } else if (device.getBondState() == BluetoothDevice.BOND_BONDED &&
                    item.state != BlueListAdapter.CONNECTED) {
                tv_discovery.setText("????????????");
                BlueConnectTask connectTask = new BlueConnectTask(item.address);
                connectTask.setBlueConnectListener(this);
                connectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, device);
            } else if (device.getBondState() == BluetoothDevice.BOND_BONDED &&
                    item.state == BlueListAdapter.CONNECTED) {
                tv_discovery.setText("??????????????????");
                InputDialogFragment dialog = InputDialogFragment.newInstance(
                        "", 0, "???????????????????????????");
                String fragTag = getResources().getString(R.string.app_name);
                dialog.show(getFragmentManager(), fragTag);
            }
        } catch (Exception e) {
            e.printStackTrace();
            tv_discovery.setText("???????????????" + e.getMessage());
        }
    }

    //?????????????????????
    @Override
    public void onInput(String title, String message, int type) {
        Log.d(TAG, "onInput message=" + message);
        Log.d(TAG, "mBlueSocket is " + (mBlueSocket == null ? "null" : "not null"));
        BluetoothUtil.writeOutputStream(mBlueSocket, message);
    }

    private BluetoothSocket mBlueSocket;

    //?????????????????????
    @Override
    public void onBlueConnect(String address, BluetoothSocket socket) {
        mBlueSocket = socket;
        tv_discovery.setText("????????????");
        refreshAddress(address);
    }

    //????????????????????????
    private void refreshAddress(String address) {
        for (int i = 0; i < mDeviceList.size(); i++) {
            BlueDevice item = mDeviceList.get(i);
            if (item.address.equals(address) == true) {
                item.state = BlueListAdapter.CONNECTED;
                mDeviceList.set(i, item);
            }
        }
        BlueListAdapter adapter = new BlueListAdapter(this, mDeviceList);
        lv_bluetooth.setAdapter(adapter);
    }

    //????????????????????????
    @Override
    public void onBlueAccept(BluetoothSocket socket) {
        Log.d(TAG, "onBlueAccept socket is " + (socket == null ? "null" : "not null"));
        if (socket != null) {
            mBlueSocket = socket;
            BluetoothDevice device = mBlueSocket.getRemoteDevice();
            refreshAddress(device.getAddress());
            BlueReceiveTask receive = new BlueReceiveTask(mBlueSocket, mHandler);
            receive.start();
        }
    }

    //???????????????????????????
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                byte[] readBuf = (byte[]) msg.obj;
                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.d(TAG, "handleMessage readMessage=" + readMessage);
                AlertDialog.Builder builder = new AlertDialog.Builder(BluetoothActivity.this);
                builder.setTitle("??????????????????").setMessage(readMessage).setPositiveButton("??????", null);
                builder.create().show();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBlueSocket != null) {
            try {
                mBlueSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}