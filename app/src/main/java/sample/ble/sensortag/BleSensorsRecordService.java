package sample.ble.sensortag;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.os.Handler;
import android.widget.Toast;
import sample.ble.sensortag.ble.BleDevicesScanner;
import sample.ble.sensortag.ble.BleUtils;
import sample.ble.sensortag.config.AppConfig;
import sample.ble.sensortag.sensor.TiAccelerometerSensor;
import sample.ble.sensortag.sensor.TiGyroscopeSensor;
import sample.ble.sensortag.sensor.TiPeriodicalSensor;
import sample.ble.sensortag.sensor.TiRangeSensors;
import sample.ble.sensortag.sensor.TiSensor;
import sample.ble.sensortag.sensor.TiSensors;
import sample.ble.sensortag.sensor.TiTemperatureSensor;

public class BleSensorsRecordService extends BleService {
    private static final String TAG = BleSensorsRecordService.class.getSimpleName();

    private static final String RECORD_DEVICE_NAME = "SensorTag";
    public static final String NOTIFICATION = "sample.ble.sensortag";

    private final TiSensor<?> sensorToRead = TiSensors.getSensor(TiAccelerometerSensor.UUID_SERVICE);
    private final TiSensor<?> sensorToRead2 = TiSensors.getSensor(TiGyroscopeSensor.UUID_SERVICE);
    private BleDevicesScanner scanner;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Created BleSensorsRecordService");

        if (!AppConfig.ENABLE_RECORD_SERVICE) {
            stopSelf();
            return;
        }

        final int bleStatus = BleUtils.getBleStatus(getBaseContext());
        switch (bleStatus) {
            case BleUtils.STATUS_BLE_NOT_AVAILABLE:
                Toast.makeText(getApplicationContext(), "No BLE on this device!", Toast.LENGTH_SHORT).show();
                stopSelf();
                return;
            case BleUtils.STATUS_BLUETOOTH_NOT_AVAILABLE:
                Toast.makeText(getApplicationContext(), "No bluetooth available!", Toast.LENGTH_SHORT).show();
                stopSelf();
                return;
            default:
                break;
        }

        if (!getBleManager().initialize(getBaseContext())) {
            stopSelf();
            return;
        }

        // initialize scanner
        final BluetoothAdapter bluetoothAdapter = BleUtils.getBluetoothAdapter(getBaseContext());
        scanner = new BleDevicesScanner(bluetoothAdapter, new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                Log.d(TAG, "Device discovered: " + device.getName());
                if (RECORD_DEVICE_NAME.equals(device.getName())) {
                    scanner.stop();
                    getBleManager().connect(getBaseContext(), device.getAddress());
                }
            }
        });

        setServiceListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (scanner == null)
            return super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "Service started");
        scanner.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service stopped");
        setServiceListener(null);
        if (scanner != null)
            scanner.stop();
    }

    @Override
    public void onConnected() {
        Log.d(TAG, "Connected");
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "Disconnected");
        scanner.start();
    }

    @Override
    public void onServiceDiscovered() {
        Log.d(TAG, "Service discovered");
        ((TiPeriodicalSensor)sensorToRead).setPeriod(10);
        ((TiPeriodicalSensor)sensorToRead2).setPeriod(10);
        enableSensor(sensorToRead, true);
        enableSensor(sensorToRead2, true);
        ((TiPeriodicalSensor)sensorToRead).setPeriod(10);
        ((TiPeriodicalSensor)sensorToRead2).setPeriod(10);

    }

    @Override
    public void onDataAvailable(String serviceUuid, String characteristicUUid, String text, byte[] data) {
        //isDiscovered = false;

        final TiSensor<?> sensor = TiSensors.getSensor(serviceUuid);
        final TiRangeSensors<float[], Float> sensor2 = (TiRangeSensors<float[], Float>)sensor;
        float[] data2 = sensor2.getData();

        Log.d(TAG, "Data='" + text + "'" + data2.toString());


        Bundle bundle = new Bundle();
        bundle.putString("sensor_uuid", serviceUuid);
        bundle.putString("sensor_name", sensor.getName());
        bundle.putByteArray("data", data);
        bundle.putFloatArray("data_float", data2);
        bundle.putString("text", text);

        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra("bundle", bundle);
        sendBroadcast(intent);

        //TODO: put your record code here. Please note that it is not main thread.
    }
}
