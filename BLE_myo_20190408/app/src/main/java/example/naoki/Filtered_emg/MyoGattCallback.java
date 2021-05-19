package example.naoki.Filtered_emg;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.echo.holographlibrary.Line;
import com.echo.holographlibrary.LineGraph;
import com.echo.holographlibrary.LinePoint;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Semaphore;

/**
 * Created by naoki on 15/04/15.
 */

public class MyoGattCallback extends BluetoothGattCallback {
    /** Service ID */
    private static final String MYO_CONTROL_ID  = "d5060001-a904-deb9-4748-2c7f4a124842";
    private static final String MYO_EMG_DATA_ID = "d5060005-a904-deb9-4748-2c7f4a124842";
    private static final String MYO_IMU_DATA_ID = "d5060002-a904-deb9-4748-2c7f4a124842";
    /** Characteristics ID */
    private static final String MYO_INFO_ID = "d5060101-a904-deb9-4748-2c7f4a124842";
    private static final String FIRMWARE_ID = "d5060201-a904-deb9-4748-2c7f4a124842";
    private static final String COMMAND_ID  = "d5060401-a904-deb9-4748-2c7f4a124842";
    private static final String EMG_0_ID    = "d5060105-a904-deb9-4748-2c7f4a124842";
    private static final String EMG_1_ID    = "d5060205-a904-deb9-4748-2c7f4a124842";
    private static final String EMG_2_ID    = "d5060305-a904-deb9-4748-2c7f4a124842";
    private static final String EMG_3_ID    = "d5060405-a904-deb9-4748-2c7f4a124842";
    private static final String IMU_ID      = "d5060402-a904-deb9-4748-2c7f4a124842";

    private static final double MYOHW_ORIENTATION_SCALE = 16384.0f; ///< See myohw_imu_data_t::orientation
    private static final double MYOHW_ACCELEROMETER_SCALE = 2048.0f; ///< See myohw_imu_data_t::accelerometer
    private static final double MYOHW_ACCEMYOHW_GYROSCOPE_SCALELEROMETER_SCALE = 16.0f; ///< See myohw_imu_data_t::gyroscope




    /** android Characteristic ID (from Android Samples/BluetoothLeGatt/SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG) */
    private static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    private Queue<BluetoothGattDescriptor> descriptorWriteQueue = new LinkedList<BluetoothGattDescriptor>();
    private Queue<BluetoothGattCharacteristic> readCharacteristicQueue = new LinkedList<BluetoothGattCharacteristic>();

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mCharacteristic_command;
    private BluetoothGattCharacteristic mCharacteristic_emg0;
    /** Reference to Characteristic EMG 1 */
    private BluetoothGattCharacteristic mCharacteristic_emg1;
    /** Reference to Characteristic EMG 2 */
    private BluetoothGattCharacteristic mCharacteristic_emg2;
    /** Reference to Characteristic EMG 3 */
    private BluetoothGattCharacteristic mCharacteristic_emg3;

    private BluetoothGattCharacteristic mCharacteristic_IMU;


    private MyoCommandList commandList = new MyoCommandList();

    private String TAG = "MyoGatt";

    private TextView dataView;
    private String callback_msg;
    private Handler mHandler;
    public int[] emgDatas = new int[16];//回调的数据
    public double[] imuDatas = new double[10];

    private LineGraph lineGraph;
    private Button btn_emg1;
    private Button btn_emg2;
    private Button btn_emg3;
    private Button btn_emg4;
    private Button btn_emg5;
    private Button btn_emg6;
    private Button btn_emg7;
    private Button btn_emg8;

    private int nowGraphIndex = 0;
    private Button nowButton;

    int[][] dataList1_a = new int[8][50];
    int[][] dataList1_b = new int[8][50];

    public MyoGattCallback(Handler handler, TextView view,final HashMap<String,View> views) {
        mHandler = handler;
        dataView = view;
        lineGraph = (LineGraph) views.get("graph");
        btn_emg1 = (Button) views.get("btn1");
        nowButton = btn_emg1;
        btn_emg1.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                nowGraphIndex = 0;
                nowButton = btn_emg1;
            }});
        btn_emg2 = (Button) views.get("btn2");
        btn_emg2.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                nowGraphIndex = 1;
                nowButton = btn_emg2;
            }});
        btn_emg3 = (Button) views.get("btn3");
        btn_emg3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowGraphIndex = 2;
                nowButton = btn_emg3;
            }
        });
        btn_emg4 = (Button) views.get("btn4");
        btn_emg4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowGraphIndex = 3;
                nowButton = btn_emg4;
            }
        });
        btn_emg5 = (Button) views.get("btn5");
        btn_emg5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowGraphIndex = 4;
                nowButton = btn_emg5;
            }
        });
        btn_emg6 = (Button) views.get("btn6");
        btn_emg6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowGraphIndex = 5;
                nowButton = btn_emg6;
            }
        });
        btn_emg7 = (Button) views.get("btn7");
        btn_emg7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowGraphIndex = 6;
                nowButton = btn_emg7;
            }
        });
        btn_emg8 = (Button) views.get("btn8");
        btn_emg8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowGraphIndex = 7;
                nowButton = btn_emg8;
            }
        });
    }
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        Log.d(TAG, "onConnectionStateChange: " + status + " -> " + newState);
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            // GATT Connected
            // Searching GATT Service
            gatt.discoverServices();

        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            // GATT Disconnected
            stopCallback();
            Log.d(TAG,"Bluetooth Disconnected");
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        Log.d(TAG, "onServicesDiscovered received: " + status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            // Find GATT Service
            BluetoothGattService service_imu = gatt.getService(UUID.fromString(MYO_IMU_DATA_ID));
            BluetoothGattService service_emg = gatt.getService(UUID.fromString(MYO_EMG_DATA_ID));
            if (service_emg == null || service_imu == null) {
                Log.d(TAG,"No Myo EMG-Data Service or IMU-Data Service");
            } else {
                Log.d(TAG, "Find Myo EMG-Data Service and IMU-Data Service!!");
                // Getting CommandCharacteristic
                mCharacteristic_emg0 = service_emg.getCharacteristic(UUID.fromString(EMG_0_ID));
                mCharacteristic_emg1 = service_emg.getCharacteristic(UUID.fromString(EMG_1_ID));
                mCharacteristic_emg2 = service_emg.getCharacteristic(UUID.fromString(EMG_2_ID));
                mCharacteristic_emg3 = service_emg.getCharacteristic(UUID.fromString(EMG_3_ID));
                mCharacteristic_IMU = service_imu.getCharacteristic(UUID.fromString(IMU_ID));
                if (mCharacteristic_emg0 == null || mCharacteristic_emg1 == null ||
                        mCharacteristic_emg2 == null || mCharacteristic_emg3 == null || mCharacteristic_IMU == null) {
                    callback_msg = "Not Found EMG-Data Characteristic or IMU";
                } else {
                    // Setting the notification
                    boolean registered_0 = gatt.setCharacteristicNotification(mCharacteristic_emg0, true);
                    boolean registered_1 = gatt.setCharacteristicNotification(mCharacteristic_emg1, true);
                    boolean registered_2 = gatt.setCharacteristicNotification(mCharacteristic_emg2, true);
                    boolean registered_3 = gatt.setCharacteristicNotification(mCharacteristic_emg3, true);

                    boolean registered_4 = gatt.setCharacteristicNotification(mCharacteristic_IMU, true);
                    if (!registered_0 || !registered_1 || !registered_2 || !registered_3 || !registered_4) {
                        Log.d(TAG,"EMG-Data Notification FALSE !! ");
                    } else {
                        Log.d(TAG,"EMG-Data Notification TRUE !!");
                        // Turn ON the Characteristic Notification
                        BluetoothGattDescriptor descriptor_0 = mCharacteristic_emg0.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
                        BluetoothGattDescriptor descriptor_1 = mCharacteristic_emg1.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
                        BluetoothGattDescriptor descriptor_2 = mCharacteristic_emg2.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
                        BluetoothGattDescriptor descriptor_3 = mCharacteristic_emg3.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));

                        BluetoothGattDescriptor descriptor_4 = mCharacteristic_IMU.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));

                        if (descriptor_0 != null && descriptor_1 != null && descriptor_2 != null && descriptor_3 != null && descriptor_4 != null){
                            descriptor_0.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            descriptor_1.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            descriptor_2.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            descriptor_3.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                            descriptor_4.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                            writeGattDescriptor(descriptor_0);
                            writeGattDescriptor(descriptor_1);
                            writeGattDescriptor(descriptor_2);
                            writeGattDescriptor(descriptor_3);

                            writeGattDescriptor(descriptor_4);

                            Log.d(TAG,"Set descriptor");

                        } else {
                            Log.d(TAG,"No descriptor");
                        }
                    }
                }
            }

            BluetoothGattService service = gatt.getService(UUID.fromString(MYO_CONTROL_ID));
            if (service == null) {
                Log.d(TAG,"No Myo Control Service !!");
            } else {
                Log.d(TAG, "Find Myo Control Service !!");
                // Get the MyoInfoCharacteristic
                BluetoothGattCharacteristic characteristic =
                        service.getCharacteristic(UUID.fromString(MYO_INFO_ID));
                if (characteristic == null) {
                } else {
                    Log.d(TAG, "Find read Characteristic !!");
                    //put the characteristic into the read queue
                    readCharacteristicQueue.add(characteristic);
                    //if there is only 1 item in the queue, then read it.  If more than 1, we handle asynchronously in the callback above
                    //GIVE PRECEDENCE to descriptor writes.  They must all finish first.
                    if((readCharacteristicQueue.size() == 1) && (descriptorWriteQueue.size() == 0)) {
                        mBluetoothGatt.readCharacteristic(characteristic);
                    }
/*                        if (gatt.readCharacteristic(characteristic)) {
                            Log.d(TAG, "Characteristic read success !!");
                        }
*/
                }

                // Get CommandCharacteristic
                mCharacteristic_command = service.getCharacteristic(UUID.fromString(COMMAND_ID));
                if (mCharacteristic_command == null) {
                } else {
                    Log.d(TAG, "Find command Characteristic !!");
                    setMyoControlCommand(commandList.sendUnSleep());
                }
            }

            boolean prioritySet = mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
        }
    }

    public void writeGattDescriptor(BluetoothGattDescriptor d){
        //put the descriptor into the write queue
        descriptorWriteQueue.add(d);
        //if there is only 1 item in the queue, then write it.  If more than 1, we handle asynchronously in the callback above
        if(descriptorWriteQueue.size() == 1){
            mBluetoothGatt.writeDescriptor(d);
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "Callback: Wrote GATT Descriptor successfully.");
        }
        else{
            Log.d(TAG, "Callback: Error writing GATT Descriptor: "+ status);
        }
        descriptorWriteQueue.remove();  //pop the item that we just finishing writing
        //if there is more to write, do it!
        if(descriptorWriteQueue.size() > 0)
            mBluetoothGatt.writeDescriptor(descriptorWriteQueue.element());
        else if(readCharacteristicQueue.size() > 0)
            mBluetoothGatt.readCharacteristic(readCharacteristicQueue.element());
    }

    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        readCharacteristicQueue.remove();
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (UUID.fromString(FIRMWARE_ID).equals(characteristic.getUuid())) {
                // Myo Firmware Infomation
                final byte[] data = characteristic.getValue();
                if (data != null && data.length > 0) {
                    ByteReader byteReader = new ByteReader();
                    byteReader.setByteData(data);

                    Log.d(TAG, String.format("This Version is %d.%d.%d - %d",
                            byteReader.getShort(), byteReader.getShort(),
                            byteReader.getShort(), byteReader.getShort()));

                }
                if (data == null) {
                    Log.d(TAG,"Characteristic String is " + characteristic.toString());
                }
            } else if (UUID.fromString(MYO_INFO_ID).equals(characteristic.getUuid())) {
                // Myo Device Information
                final byte[] data = characteristic.getValue();
                if (data != null && data.length > 0) {
                    ByteReader byteReader = new ByteReader();
                    byteReader.setByteData(data);

                    callback_msg = String.format("Serial Number     : %02x:%02x:%02x:%02x:%02x:%02x",
                            byteReader.getByte(), byteReader.getByte(), byteReader.getByte(),
                            byteReader.getByte(), byteReader.getByte(), byteReader.getByte()) +
                            '\n' + String.format("Unlock            : %d", byteReader.getShort()) +
                            '\n' + String.format("Classifier builtin:%d active:%d (have:%d)",
                            byteReader.getByte(), byteReader.getByte(), byteReader.getByte()) +
                            '\n' + String.format("Stream Type       : %d", byteReader.getByte());

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            dataView.setText(callback_msg);
                        }
                    });

                }
            }
        }
        else{
            Log.d(TAG, "onCharacteristicRead error: " + status);
        }

        if(readCharacteristicQueue.size() > 0)
            mBluetoothGatt.readCharacteristic(readCharacteristicQueue.element());
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "onCharacteristicWrite success");
        } else {
            Log.d(TAG, "onCharacteristicWrite error: " + status);
        }
    }

    long last_send_never_sleep_time_ms = System.currentTimeMillis();
    final static long NEVER_SLEEP_SEND_TIME = 500;  // Milli Second
    int cnt=0;
    public static Semaphore semaphore = new Semaphore(1);
    public static String filePath = Environment.getExternalStorageDirectory() + "/Myo";
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//        cnt++;
//        System.out.println("this is the count:"+cnt);
        Date date = new Date();
        System.out.println(characteristic.getUuid().toString());
        System.out.println(cnt+": "+date.toString());
        long systemTime_ms = System.currentTimeMillis();
//        if(systemTime_ms > last_send_never_sleep_time_ms + NEVER_SLEEP_SEND_TIME) {
////                //set Myo [Never Sleep Mode]
//                setMyoControlCommand(commandList.sendUnSleep());
//                last_send_never_sleep_time_ms = systemTime_ms;
//        }

        if ( EMG_0_ID.equals(characteristic.getUuid().toString())
                || EMG_1_ID.equals(characteristic.getUuid().toString())
                || EMG_2_ID.equals(characteristic.getUuid().toString())
                || EMG_3_ID.equals(characteristic.getUuid().toString())) {

            byte[] emg_data = characteristic.getValue();


            ByteReader emg_br = new ByteReader();
            emg_br.setByteData(emg_data);

            final String callback_msg = String.format("%d,%d,%d,%d,%d,%d,%d,%d\n" +
                            "%d,%d,%d,%d,%d,%d,%d,%d",
                    emg_br.getByte(),emg_br.getByte(),emg_br.getByte(),emg_br.getByte(),
                    emg_br.getByte(),emg_br.getByte(),emg_br.getByte(),emg_br.getByte(),
                    emg_br.getByte(),emg_br.getByte(),emg_br.getByte(),emg_br.getByte(),
                    emg_br.getByte(),emg_br.getByte(),emg_br.getByte(),emg_br.getByte());

            //储存回调的数据,
            emg_br = new ByteReader();
            emg_br.setByteData(emg_data);
            for(int emgInputIndex = 0;emgInputIndex<16;emgInputIndex++) {
                emgDatas[emgInputIndex] = emg_br.getByte();
            }
            try{
                File saveFile=new File(filePath, MainActivity.fileName_emg+MainActivity.username+MainActivity.posture+MainActivity.postfix);
//                Date date = new Date();
                FileOutputStream fos=new FileOutputStream(saveFile,true);
                String msg;
                int index;
                if(MainActivity.isrecording){
                    index = 1;
                }else{
                    index = 0;
                }
                msg = String.format("%s,%d",callback_msg, index)+'\n';
                fos.write(msg.getBytes());
                fos.close();
                System.out.println("写入成功!");
            }catch (Exception e){
                e.printStackTrace();
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    dataView.setText(callback_msg);
                    lineGraph.removeAllLines();

                    for(int inputIndex = 0;inputIndex<8;inputIndex++) {
                        dataList1_a[inputIndex][0] = emgDatas[0+inputIndex];
                        dataList1_b[inputIndex][0] = emgDatas[7+inputIndex];
                    }
                    // 折れ線グラフ
                    int number = 50;
                    int addNumber = 100;
                    Line line = new Line();
                    while (0 < number) {
                        number--;
                        addNumber--;

                        //１点目add
                        if(number != 0){
                            for(int setDatalistIndex = 0;setDatalistIndex < 8;setDatalistIndex++){
                                dataList1_a[setDatalistIndex][number] = dataList1_a[setDatalistIndex][number - 1];
                            }
                        }
                        LinePoint linePoint = new LinePoint();
                        linePoint.setY(dataList1_a[nowGraphIndex][number]); //ランダムで生成した値をSet
                        linePoint.setX(addNumber); //x軸を１ずつずらしてSet
                        //linePoint.setColor(Color.parseColor("#9acd32")); // 丸の色をSet

                        line.addPoint(linePoint);
                        //2点目add
                        /////number--;
                        addNumber--;
                        if(number != 0){
                            for(int setDatalistIndex = 0;setDatalistIndex < 8;setDatalistIndex++) {
                                dataList1_b[setDatalistIndex][number] = dataList1_b[setDatalistIndex][number - 1];
                            }
                        }
                        linePoint = new LinePoint();
                        linePoint.setY(dataList1_b[nowGraphIndex][number]); //ランダムで生成した値をSet
                        linePoint.setX(addNumber); //x軸を１ずつずらしてSet
                        //linePoint.setColor(Color.parseColor("#9acd32")); // 丸の色をSet

                        line.addPoint(linePoint);
                    }
                    if(nowButton != null) {
                        //line.setColor(Color.parseColor("#9acd32")); // 線の色をSet
                        line.setColor(((ColorDrawable)nowButton.getBackground()).getColor()); // 線の色をSet
                    }
                    line.setShowingPoints(false);
                    lineGraph.addLine(line);
                    lineGraph.setRangeY(-128, 128); // 表示するY軸の最低値・最高値 今回は0から1まで
                    //graph.setRangeX(0, 100); // 表示するX軸の最低値・最高値　今回は0からデータベースの取得した
                }
            });

            if (systemTime_ms > last_send_never_sleep_time_ms + NEVER_SLEEP_SEND_TIME) {
                // set Myo [Never Sleep Mode]
                setMyoControlCommand(commandList.sendUnSleep());
                last_send_never_sleep_time_ms = systemTime_ms;
            }
        }

        //imu的数据读取代码
        if ( IMU_ID.equals(characteristic.getUuid().toString())){
            byte[] imu_data = characteristic.getValue();
            ByteReader imu_br = new ByteReader();
            imu_br.setByteData(imu_data);
//            System.out.println("this is IMU data: "+emg_br.getByte()+" "+emg_br.getByte()+" "+emg_br.getByte()+ " " + emg_br.getByte());
//            System.out.println("this is IMU data: "+emg_br.getByte()+" "+emg_br.getByte()+" "+emg_br.getByte());
//            System.out.println("this is IMU data: "+emg_br.getByte()+" "+emg_br.getByte()+" "+emg_br.getByte());
            final String callback_imu = String.format("%.4f, %.4f, %.4f, %.4f, %.4f, %.4f, %.4f, %.4f, %.4f, %.4f",
                    imu_br.getShort()/MYOHW_ORIENTATION_SCALE,imu_br.getShort()/MYOHW_ORIENTATION_SCALE,imu_br.getShort()/MYOHW_ORIENTATION_SCALE,
                    imu_br.getShort()/MYOHW_ORIENTATION_SCALE,
                    imu_br.getShort()/MYOHW_ACCELEROMETER_SCALE,imu_br.getShort()/MYOHW_ACCELEROMETER_SCALE,imu_br.getShort()/MYOHW_ACCELEROMETER_SCALE,
                    imu_br.getShort()/MYOHW_ACCEMYOHW_GYROSCOPE_SCALELEROMETER_SCALE,imu_br.getShort()/MYOHW_ACCEMYOHW_GYROSCOPE_SCALELEROMETER_SCALE,
                    imu_br.getShort()/MYOHW_ACCEMYOHW_GYROSCOPE_SCALELEROMETER_SCALE);
            System.out.println(callback_imu);

            //储存回调的数据,
//            imu_br = new ByteReader();
//            imu_br.setByteData(imu_data);
//            for(int imuInputIndex = 0; imuInputIndex<4; imuInputIndex++) {
//                imuDatas[imuInputIndex] = imu_br.getShort()/MYOHW_ORIENTATION_SCALE;
//            }
//            for(int imuInputIndex = 4; imuInputIndex<7; imuInputIndex++) {
//                imuDatas[imuInputIndex] = imu_br.getShort()/MYOHW_ACCELEROMETER_SCALE;
//            }
//            for(int imuInputIndex = 7; imuInputIndex<10; imuInputIndex++) {
//                imuDatas[imuInputIndex] = imu_br.getShort()/MYOHW_ACCEMYOHW_GYROSCOPE_SCALELEROMETER_SCALE;
//            }
            try{
                File saveFile=new File(filePath, MainActivity.fileName_imu+MainActivity.username+MainActivity.posture+MainActivity.postfix);
//                Date date = new Date();
                FileOutputStream fos=new FileOutputStream(saveFile,true);
                String msg;

                int index;
                if(MainActivity.isrecording){
                    index = 1;
                }else{
                    index = 0;
                }
                msg = String.format("%s,%d",callback_imu, index)+'\n';
                fos.write(msg.getBytes());
                fos.close();
                System.out.println("写入成功!");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }



    public void setBluetoothGatt(BluetoothGatt gatt) {
        mBluetoothGatt = gatt;
    }

    public boolean setMyoControlCommand(byte[] command) {
        if ( mCharacteristic_command != null) {
            mCharacteristic_command.setValue(command);
            int i_prop = mCharacteristic_command.getProperties();
            if (i_prop == BluetoothGattCharacteristic.PROPERTY_WRITE) {
                if (mBluetoothGatt.writeCharacteristic(mCharacteristic_command)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void stopCallback() {
        // Before the closing GATT, set Myo [Normal Sleep Mode].
        setMyoControlCommand(commandList.sendNormalSleep());
        descriptorWriteQueue = new LinkedList<BluetoothGattDescriptor>();
        readCharacteristicQueue = new LinkedList<BluetoothGattCharacteristic>();
        if (mCharacteristic_command != null) {
            mCharacteristic_command = null;
        }
        if (mCharacteristic_emg0 != null) {
            mCharacteristic_emg0 = null;
        }
        if (mBluetoothGatt != null) {
            mBluetoothGatt = null;
        }
    }
}




