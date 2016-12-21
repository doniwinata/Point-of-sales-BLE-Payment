package com.example.doni.pos;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;

public class MainActivity extends AppCompatActivity {
    private Button start;
    private Button stop;
    public TextView connectedDevice;
    public TextView Logs;
    public String tampLogs = "Logs: \n";
    private BroadcastReceiver receiver;
    private static final int ENABLE_BLUETOOTH_REQUEST = 17;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editPref;
    private boolean finalKey = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editPref = preferences.edit();
        //set POS ID
//        editPref.putString("POS_ID_VALUE", "POS_ID");
//        editPref.putString("123456789101112", "SK");
//        editPref.commit();
        //set layout
        setContentView(R.layout.activity_main);

        connectedDevice = (TextView) findViewById(R.id.connected_device);
        Logs = (TextView) findViewById(R.id.log_view);
        Logs.setMovementMethod(new ScrollingMovementMethod());
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra(GattService.DEVICE_NAME);
                String log = intent.getStringExtra(GattService.LOGS_PROTOCOL);
                String close = intent.getStringExtra(GattService.CLOSE);

                final String other_log = intent.getStringExtra(GattService.OTHER_LOGS);
                if(close!=null){
                    stopService(new Intent(MainActivity.this, GattService.class));
                    updateUi();
                    Intent intents = getIntent();
                    finish();
                    startActivity(intents);
                }
               // Log.d("valS" , s);
                if(s!=null){
                connectedDevice.setText("Customer Name/ID:"+ s);
                }
                if(other_log!=null && !finalKey){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tampLogs+=other_log;
                            Logs.setText(tampLogs);
                            finalKey=true;
                        }
                    });
                }

                if(log!=null){
                        byte[] data = Base64.decode(log, Base64.DEFAULT);
                        ByteArrayInputStream bais = new ByteArrayInputStream(data);
                        DataInputStream in = new DataInputStream(bais);
                        try {
                            while (in.available() > 0) {
                                String element = in.readUTF();
                                Log.d("hasil",element);
                                if(element.equals("2")){
                                    tampLogs+="\nround 1(X3,X4,ZKP3,ZKP4)";
                                }else if (element.equals("1")){
                                    tampLogs+="\nround 2 received(X1,X2,ZKP1,ZKP2)";
                                }
                                if(element.length()>30){
                                    element = "("+element.length()+")"+element.substring(0,20)+"...";
                                }
                                tampLogs+="\n"+element;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Logs.setText(tampLogs);
                            }


                        });
                }       // do something here.
            }
        };
        start = (Button) findViewById(R.id.start_pos);
        stop = (Button) findViewById(R.id.stop_pos);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, GattService.class));
                updateUi();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                BluetoothManager bluetoothManager = (BluetoothManager) MainActivity.this.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
                BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                if (bluetoothAdapter == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Bluetooth is Not Enabled");
                    builder.setMessage("Please Enable Bluetooth").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
                    builder.show();

                } else if (!bluetoothAdapter.isEnabled()) {

                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    MainActivity.this.startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST);

                } else if (!bluetoothAdapter.isMultipleAdvertisementSupported()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Warning");
                    builder.setMessage("Return true if the multi advertisement is supported by the chipset")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            });
                    builder.show();

                } else {
                    start();
                }
                updateUi();
            }

        });

    }

    private void start() {
        startService(new Intent(this, GattService.class));
      //  finish();
    }

    private void updateUi(){
        if(isServiceRunning(GattService.class)){
            start.setEnabled(false);
            stop.setEnabled(true);
        }else{
            start.setEnabled(true);
            stop.setEnabled(false);
        }
    }
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ENABLE_BLUETOOTH_REQUEST) {
            if (resultCode == RESULT_OK) {
                start();
            } else {
                finish();
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(GattService.COPA_RESULT)
        );
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(GattService.LOGS_PROTOCOL)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

}
