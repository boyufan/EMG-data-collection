package example.naoki.Filtered_emg;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.os.Environment;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.RandomAccessFile;

import com.echo.holographlibrary.Line;
import com.echo.holographlibrary.LineGraph;
import com.echo.holographlibrary.LinePoint;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Queue;
import java.util.LinkedList;


public class MainActivity extends ActionBarActivity implements BluetoothAdapter.LeScanCallback, View.OnClickListener {
    public static final int MENU_LIST = 0;
    public static final int MENU_BYE = 1;

    /**
     * Device Scanning Time (ms)
     */
    private static final long SCAN_PERIOD = 5000;

    /**
     * Intent code for requesting Bluetooth enable
     */
    private static final int REQUEST_ENABLE_BT = 1;

    private static final String TAG = "BLE_Myo";

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;


    private TextView emgDataText;


    private MyoGattCallback mMyoCallback;
    private MyoCommandList2 commandList = new MyoCommandList2();

    private String deviceName;


    //数据采集按钮
    private Button btn_start;

    private Button btn_left;
    private Button btn_forward;
    private Button btn_right;

    private Button btn_stand;
    private Button btn_walk;


    private Button btn_gest10;

    //播放声音按钮
    private Button btn_play;


    //可编辑文本框
    EditText editText;
    EditText editTextName;

    //输入文本框的内容
    private String p_id = "1";


    //    //文件储存
    public String filePath = Environment.getExternalStorageDirectory() + "/Myo";
    public static String username;
    //    public static String fileName_emg = "emg.csv";
//    public static String fileName_imu = "imu.csv";
    public static String fileName_emg = "emg_";
    public static String fileName_imu = "imu_";
    public static String postfix = ".csv";
    public static String posture;

    private LineGraph graph;
    private Button graphButton1;
    private Button graphButton2;
    private Button graphButton3;
    private Button graphButton4;
    private Button graphButton5;
    private Button graphButton6;
    private Button graphButton7;
    private Button graphButton8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
        //ready
        graph = (LineGraph) findViewById(R.id.holo_graph_view);
        graphButton1 = (Button) findViewById(R.id.btn_emg1);
        graphButton2 = (Button) findViewById(R.id.btn_emg2);
        graphButton3 = (Button) findViewById(R.id.btn_emg3);
        graphButton4 = (Button) findViewById(R.id.btn_emg4);
        graphButton5 = (Button) findViewById(R.id.btn_emg5);
        graphButton6 = (Button) findViewById(R.id.btn_emg6);
        graphButton7 = (Button) findViewById(R.id.btn_emg7);
        graphButton8 = (Button) findViewById(R.id.btn_emg8);


        //set color
        graphButton1.setBackgroundColor(Color.argb(0x66, 0xff, 0, 0xff));
        graphButton2.setBackgroundColor(Color.argb(0x66, 0xff, 0x00, 0x00));
        graphButton3.setBackgroundColor(Color.argb(0x66, 0x66, 0x33, 0xff));
        graphButton4.setBackgroundColor(Color.argb(0x66, 0xff, 0x66, 0x33));
        graphButton5.setBackgroundColor(Color.argb(0x66, 0xff, 0x33, 0x66));
        graphButton6.setBackgroundColor(Color.argb(0x66, 0x00, 0x33, 0xff));
        graphButton7.setBackgroundColor(Color.argb(0x66, 0x00, 0x33, 0x33));
        graphButton8.setBackgroundColor(Color.argb(0x66, 0x66, 0xcc, 0x66));

        emgDataText = (TextView) findViewById(R.id.emgDataTextView);


        editText = (EditText) findViewById(R.id.participant_ID);//p_id EditText
        editTextName = (EditText) findViewById(R.id.user_name);

        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        Intent intent = getIntent();
        deviceName = intent.getStringExtra(ListActivity.TAG);

        if (deviceName != null) {
            // Ensures Bluetooth is available on the device and it is enabled. If not,
            // displays a dialog requesting user permission to enable Bluetooth.
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                // Scanning Time out by Handler.
                // The device scanning needs high energy.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothAdapter.stopLeScan(MainActivity.this);
                    }
                }, SCAN_PERIOD);
                mBluetoothAdapter.startLeScan(this);
            }
        }


    }

    public void initialize() {
        btn_start = (Button) findViewById(R.id.b_start);
        btn_start.setOnClickListener(this);


        btn_gest10 = (Button) findViewById(R.id.b_grasp_a);
        btn_gest10.setOnClickListener(this);

        btn_left = (Button) findViewById(R.id.b_left);
        btn_left.setOnClickListener(this);

        btn_forward = (Button) findViewById(R.id.b_forward);
        btn_forward.setOnClickListener(this);

        btn_right = (Button) findViewById(R.id.b_right);
        btn_right.setOnClickListener(this);

        btn_walk = (Button) findViewById(R.id.walking);
        btn_walk.setOnClickListener(this);

        btn_stand = (Button) findViewById(R.id.standing);
        btn_stand.setOnClickListener(this);


//        btn_play=(Button)findViewById(R.id.b_play);
//        btn_play.setOnClickListener(this);


        //create data save dir
        mkDir(filePath);
    }

    public void mkDir(String path) {
        File file = null;
        try {
            file = new File(path);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //采集按钮响应事件
    private int recordFlag;
    private static boolean mic_status = false;
    public static boolean recordering = false;
    public static boolean isrecording = false;
    int gest_cnt[] = new int[16];

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.b_start:
                username = editTextName.getText().toString();
                if (!mic_status) {
                    mic_status = true;
                    recordering = true;
                    btn_start.setText("STOP");
                    btn_start.setBackgroundColor(getResources().getColor(R.color.lightgreen));
                    p_id = editText.getText().toString();//get input p_id
                    if (mBluetoothGatt == null || !mMyoCallback.setMyoControlCommand(commandList.sendEmgOnly())) {
                        Log.d(TAG, "False EMG");
                        System.out.println("False EMG");
                    } else {
                        mMyoCallback.setMyoControlCommand(commandList.sendUnSleep());
                        mMyoCallback.setMyoControlCommand(commandList.sendVibration3());
                    }
                } else {
                    mic_status = false;
                    recordering = false;
                    btn_start.setText("START");
                    btn_start.setBackgroundColor(getResources().getColor(R.color.gray));
                    if (mBluetoothGatt == null
                            || !mMyoCallback.setMyoControlCommand(commandList.sendUnsetData())
                            || !mMyoCallback.setMyoControlCommand(commandList.sendNormalSleep())) {
                        Log.d(TAG, "False Data Stop");
                    }
                }
                break;


            case R.id.b_grasp_a:
//                posture = "_normal";
                posture = "_gap";
                btn_gest10.setText("正在采集");
                btn_gest10.setBackgroundColor(getResources().getColor(R.color.lightgreen));
                isrecording = true;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("采集结束");
                        btn_gest10.setText("采集");
                        isrecording = false;
                    }
                }, Integer.parseInt(editText.getText().toString()));
                break;

            case R.id.b_left:
//                posture = "_l";
                posture = "_x1";
                btn_left.setText("正在采集");
                btn_left.setBackgroundColor(getResources().getColor(R.color.lightgreen));
                isrecording = true;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("采集结束");
                        btn_left.setText("采集");
                        isrecording = false;
                    }
                }, Integer.parseInt(editText.getText().toString()));
                break;

            case R.id.b_forward:
//                posture = "_f";
                posture = "_origin";
                btn_forward.setText("正在采集");
                btn_forward.setBackgroundColor(getResources().getColor(R.color.lightgreen));
                isrecording = true;


                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("采集结束");
                        btn_forward.setText("采集");
                        isrecording = false;
                    }
                }, Integer.parseInt(editText.getText().toString()));
                break;

            case R.id.b_right:
//                posture = "_r";
                posture = "_x4";
                btn_right.setText("正在采集");
                btn_right.setBackgroundColor(getResources().getColor(R.color.lightgreen));
                isrecording = true;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("采集结束");
                        btn_right.setText("采集");
                        isrecording = false;
                    }
                }, Integer.parseInt(editText.getText().toString()));
                break;
                

            case R.id.standing:
                posture = "_stand";
                btn_stand.setText("正在采集");
                btn_stand.setBackgroundColor(getResources().getColor(R.color.lightgreen));
                isrecording = true;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("采集结束");
                        btn_stand.setText("采集");
                        isrecording = false;
                    }
                }, Integer.parseInt(editText.getText().toString()));
                break;

            case R.id.walking:
                posture = "_walk";
                btn_walk.setText("正在采集");
                btn_walk.setBackgroundColor(getResources().getColor(R.color.lightgreen));
                isrecording = true;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("采集结束");
                        btn_walk.setText("采集");
                        isrecording = false;
                    }
                }, Integer.parseInt(editText.getText().toString()));
                break;


            default:
                recordFlag = 0;
        }
    }

    private void startRecording() {
        new Thread(new RecoderThread()).start();
    }

    class RecoderThread implements Runnable {
        @Override
        public void run() {
            WriteDataToFile();
        }
    }

    public void WriteDataToFile() {
//        username = editTextName.getText().toString();
//        File mfile=new File(filePath,fileName_emg + username + postfix);
//        if(mfile.exists()){
//            mfile.delete();
//        }
//        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
//            while(recordering==true){
//                try{
//                    File saveFile=new File(filePath,fileName_emg + username + postfix);
//                    System.out.println(fileName_emg + username + postfix);
//                    Date date = new Date();
//                    FileOutputStream fos=new FileOutputStream(saveFile,true);
//                    String msg=emgDataText.getText().toString()+date.toString()+'\n';
//                    fos.write(msg.getBytes());
//                    fos.close();
////                    System.out.println("写入成功!");
//                }catch (Exception e){
//                    e.printStackTrace();
//
//                }
//
//            }
//        }
//        File mfile2=new File(filePath,fileName_imu + username + postfix);
//        if(mfile2.exists()){
//                mfile2.delete();
//            }
//            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
//                while(recordering==true){
//                    try{
//                        File saveFile=new File(filePath,fileName_imu + username + postfix);
//                        Date date = new Date();
//                        FileOutputStream fos=new FileOutputStream(saveFile,true);
//                        String msg=emgDataText.getText().toString()+date.toString()+'\n';
//                        fos.write(msg.getBytes());
//                        fos.close();
////                    System.out.println("写入成功!");
//                    }catch (Exception e){
//                        e.printStackTrace();
//
//                    }
//
//                }
//        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(0, MENU_LIST, 0, "Find Myo");
        menu.add(0, MENU_BYE, 0, "Good Bye");
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        this.closeBLEGatt();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch (id) {
            case MENU_LIST:
//                Log.d("Menu","Select Menu A");
                Intent intent = new Intent(this, ListActivity.class);
                startActivity(intent);
                return true;

            case MENU_BYE:
//                Log.d("Menu","Select Menu B");
                closeBLEGatt();
                Toast.makeText(getApplicationContext(), "Close", Toast.LENGTH_SHORT).show();
                //              startNopModel();
                return true;

        }
        return false;
    }

    /**
     * Define of BLE Callback
     */
    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (deviceName.equals(device.getName())) {
            Log.d(TAG, "AAAAAAAA");
            mBluetoothAdapter.stopLeScan(this);
            // Trying to connect GATT
            final HashMap<String, View> views = new HashMap<String, View>();
            //put GraphView
            views.put("graph", graph);
            //put Button1〜8

            views.put("btn1", graphButton1);
            views.put("btn2", graphButton2);
            views.put("btn3", graphButton3);
            views.put("btn4", graphButton4);
            views.put("btn5", graphButton5);
            views.put("btn6", graphButton6);
            views.put("btn7", graphButton7);
            views.put("btn8", graphButton8);

            Log.d(TAG, "Ready to callback!");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMyoCallback = new MyoGattCallback(mHandler, emgDataText, views);
                    mBluetoothGatt = device.connectGatt(getApplicationContext(), false, mMyoCallback);
                    mMyoCallback.setBluetoothGatt(mBluetoothGatt);
                }
            });
        }
    }


    public void onClickVibration(View v) {
        if (mBluetoothGatt == null || !mMyoCallback.setMyoControlCommand(commandList.sendVibration3())) {
            Log.d(TAG, "False Vibrate");
        }
    }

//    public void onClickUnlock(View v) {
//        if (mBluetoothGatt == null || !mMyoCallback.setMyoControlCommand(commandList.sendUnLock())) {
//            Log.d(TAG,"False UnLock");
//        }
//    }

    public void onClickEMG(View v) {
        if (mBluetoothGatt == null || !mMyoCallback.setMyoControlCommand(commandList.sendEmgOnly())) {
            Log.d(TAG, "False EMG");
        }
//        else {
//            saveMethod  = new GestureSaveMethod();
//            if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
//                gestureText.setText("DETECT Ready");
//            } else {
//                gestureText.setText("Teach me \'Gesture\'");
//            }
//        }
    }

    public void onClickNoEMG(View v) {
        if (mBluetoothGatt == null
                || !mMyoCallback.setMyoControlCommand(commandList.sendUnsetData())
                || !mMyoCallback.setMyoControlCommand(commandList.sendNormalSleep())) {
            Log.d(TAG, "False Data Stop");
        }
    }

//    public void onClickSave(View v) {
//        if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Ready ||
//                saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
//            saveModel   = new GestureSaveModel(saveMethod);
//            startSaveModel();
//        } else if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Not_Saved) {
//            startSaveModel();
//        }
//        saveMethod.setState(GestureSaveMethod.SaveState.Now_Saving);
//        gestureText.setText("Saving ; " + (saveMethod.getGestureCounter() + 1));
//    }
//
//    public void onClickDetect(View v) {
//        if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
//            gestureText.setText("Let's Go !!");
//            detectMethod = new GestureDetectMethod(saveMethod.getCompareDataList());
//            detectModel = new GestureDetectModel(detectMethod);
//            startDetectModel();
//        }
//    }

    public void closeBLEGatt() {
        if (mBluetoothGatt == null) {
            return;
        }
        mMyoCallback.stopCallback();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

//    public void startSaveModel() {
//        IGestureDetectModel model = saveModel;
//        model.setAction(new GestureDetectSendResultAction(this));
//        GestureDetectModelManager.setCurrentModel(model);
//    }
//
//    public void startDetectModel() {
//        IGestureDetectModel model = detectModel;
//        model.setAction(new GestureDetectSendResultAction(this));
//        GestureDetectModelManager.setCurrentModel(model);
//    }
//
//    public void startNopModel() {
//        GestureDetectModelManager.setCurrentModel(new NopModel());
//    }
//
//    public void setGestureText(final String message) {
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                gestureText.setText(message);
//            }
//        });
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(MainActivity.this);
                }
            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(this);
        }
    }
}

