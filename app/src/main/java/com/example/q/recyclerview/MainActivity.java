package com.example.q.recyclerview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int READ_PERMISION_REQ_CODE = 123;
    private static final int WRITE_PERMISSION_REQ_CODE = 456;
    private static final int REQUEST_ENABLE_BT = 789;

    private int[] images = {
            R.drawable.image1, R.drawable.image3,
            R.drawable.image6, R.drawable.image8,
            R.drawable.image9, R.drawable.image10,
            R.drawable.image12
    };

    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    ImageView selectImage;
    ImageView outputImage;
    Button captureBtn;
    Button shareBtn;
    Button sendBtn;

    private BluetoothManager mBluetoothManager = null;

    private static final UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터
    private Set<BluetoothDevice> deviceSet; // 블루투스 디바이스 데이터 셋
    private BluetoothDevice bluetoothDevice; // 블루투스 디바이스
    private int pairedDeviceNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GetStorageReadPermission();
        GetStorageWritePermission();

        layoutManager = new GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RecyclerAdapter(images);
        recyclerView.setAdapter(adapter);

        selectImage = (ImageView) findViewById(R.id.imageView);
        outputImage = (ImageView) findViewById(R.id.outputimage);

        adapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                selectImage.setImageResource(images[position]);
                selectImage.setVisibility(View.VISIBLE);
            }
        });

        captureBtn = (Button) findViewById(R.id.capture);
        captureBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeScreenshot();
            }
        });

        shareBtn = (Button) findViewById(R.id.share);
        shareBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareScreenshot();
            }
        });

        sendBtn = (Button) findViewById(R.id.send);
        sendBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(bluetoothDevice != null) {
                    // connect device 쪽으로 옮기기
                    sendMessage("JSON STRING!");
                }
            }
        });



        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            finish();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_ENABLE_BT);
            } else if (mBluetoothManager == null) {
                setupChat();
            }
        }
    }

    private void shareScreenshot() {
        selectBluetoothDevice();
    }

    private void setupChat() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        mBluetoothManager = new BluetoothManager(this, mHandler);
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothManager.STATE_CONNECTED:
                            break;
                        case BluetoothManager.STATE_CONNECTING:
                            break;
                        case BluetoothManager.STATE_LISTEN:
                        case BluetoothManager.STATE_NONE:
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);

                    Log.i("***BT", writeMessage);
                    // textViewReceive.setText("ME: " + writeMessage);

                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the bufferl
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    Log.i("***BT", readMessage);
                    // textViewReceive.setText("RECEIVE:  " + readMessage);

                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    String mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != this) {
                        Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    break;
            }
        }
    };

    private void sendMessage(String message) {

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mBluetoothManager.write(send);
        }
    }


    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            selectImage.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(selectImage.getDrawingCache());
            selectImage.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            openScreenshot(imageFile);

        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }

    private void openScreenshot(File imageFile) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath(), options);

        outputImage.setImageBitmap(bitmap);
    }

    public void GetStorageReadPermission() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.i("***PERMISSION", "Got StoragePermission");
        } else {
            Log.i("***PERMISSION", "Try to get StoragePermission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
        }
    }

    public void GetStorageWritePermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.i("***PERMISSION", "Got StoragePermission");
        } else {
            Log.i("***PERMISSION", "Try to get StoragePermission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQ_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {

            case WRITE_PERMISSION_REQ_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.i("***PERMISSION", "Got StoragePermission");
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.i("***PERMISSION", "Failed to get StoragePermission");
                }
                return;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (requestCode == RESULT_OK) { // '사용'을 눌렀을 때
                    selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
                } else { // '취소'를 눌렀을 때

                    // 여기에 처리 할 코드를 작성하세요.
                }
                break;
        }
    }

    public void selectBluetoothDevice() {

        // 이미 페어링 되어있는 블루투스 기기를 찾습니다.
        deviceSet = bluetoothAdapter.getBondedDevices();

        // 페어링 된 디바이스의 크기를 저장
        pairedDeviceNum = deviceSet.size();

        // 페어링 되어있는 장치가 없는 경우
        if (pairedDeviceNum == 0) {
            // 페어링을 하기위한 함수 호출
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("페어링 되어있는 블루투스 디바이스가 없습니다");
            builder.setPositiveButton("알겠소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 그냥 종료
                }
            });

            // 뒤로가기 버튼 누를 때 창이 안닫히도록 설정
            // builder.setCancelable(false);

            // 다이얼로그 생성
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            // 디바이스를 선택하기 위한 다이얼로그 생성
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Device List");

            // 페어링 된 각각의 디바이스의 이름과 주소를 저장
            List<String> list = new ArrayList<>();

            // 모든 디바이스의 이름을 리스트에 추가
            for (BluetoothDevice tmpDevice : deviceSet) {
                list.add(tmpDevice.getName() +": " + tmpDevice.getAddress());
            }

            list.add("취소");

            final int listSize = list.size();

            // List를 CharSequence 배열로 변경
            final CharSequence[] charSequences = list.toArray(new CharSequence[listSize]);

            // 해당 아이템을 눌렀을 때 호출 되는 이벤트 리스너
            builder.setItems(charSequences, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 해당 디바이스와 연결하는 함수 호출
                    if (which != listSize) {
                        connectDevice(charSequences[which].toString());
                    }
                }
            });

            // 뒤로가기 버튼 누를 때 창이 안닫히도록 설정
            builder.setCancelable(false);

            // 다이얼로그 생성
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    public void connectDevice(String deviceProperty) {

        // 페어링 된 디바이스들을 모두 탐색
        for (BluetoothDevice tempDevice : deviceSet) {
            // 사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료
            if (deviceProperty.equals(tempDevice.getName() +": " + tempDevice.getAddress())) {
                bluetoothDevice = tempDevice;
                break;
            }
        }
        mBluetoothManager.connect(bluetoothDevice, true);
        SendJSON();
    }

    public void SendJSON() {
        // 페어링을 하기위한 함수 호출
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("명함 전송");
        builder.setMessage("Name: " + bluetoothDevice.getName() + "\nAddress: " + bluetoothDevice.getAddress());
        builder.setPositiveButton("전송", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendMessage("JSON STRING!");
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 그냥 종료
            }
        });

        // 뒤로가기 버튼 누를 때 창이 안닫히도록 설정
        // builder.setCancelable(false);

        // 다이얼로그 생성
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}