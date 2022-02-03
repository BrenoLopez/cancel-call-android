package com.gb.cancelarligacoes;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.lang.String;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import static android.app.role.RoleManager.ROLE_CALL_SCREENING;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ID = 1;
    private static final String FILE_PREFERENCE = "FilePreference";
    private SharedPreferences.Editor editor;
    private  SharedPreferences preferences;
    private Switch buttonStatus;
    private TextView textStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hasPermissions(this,getApplicationContext(), new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_CONTACTS});
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ) {
                requestRole();
            }
            else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){

//                Intent intent = new Intent(this,CallService.class);
//                System.out.println("intent de merda"+intent);
//                startService(intent);
                Intent mCallServiceIntent = new Intent("android.telecom.CallScreeningService");
                mCallServiceIntent.setPackage("com.gb.cancelarligacoes");
                ServiceConnection mServiceConnection = new ServiceConnection(){

                    @Override
                    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                        Log.i("onserviceconnected",""+iBinder);
                        // iBinder is an instance of CallScreeningService.CallScreenBinder
                        // CallScreenBinder is an inner class present inside CallScreenService
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName componentName) {

                    }

                    @Override
                    public void onBindingDied(ComponentName name) {

                    }
                };
                bindService(mCallServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            }
        preferences = getSharedPreferences(FILE_PREFERENCE, Context.MODE_PRIVATE);
        editor =  preferences.edit();
        if(!preferences.contains("status")) {
            editor.putBoolean("status",true);
            editor.commit();
        }
        buttonStatus = findViewById(R.id.buttonStatus);
        textStatus = findViewById(R.id.textStatus);
        buttonStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleStatus();
            }
        });
        buttonStatus.setChecked(preferences.getBoolean("status",false));
        textStatus.setText(preferences.getBoolean("status",false)?"Ativado":"Desativado");
    }


    private void requestRole() {
        @SuppressLint("WrongConstant")
        RoleManager roleManager = (RoleManager) getSystemService(ROLE_SERVICE);
        Intent intent = roleManager.createRequestRoleIntent(ROLE_CALL_SCREENING);
        startActivityForResult(intent, REQUEST_ID);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_ID) {
//            if (resultCode == Activity.RESULT_OK) {
//                System.out.println("executa aqui!");
//            } else {
//                System.out.println("executa aqui!2");
//            }
//        }
//    }
    private void startCallReceiver(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ){
            BroadcastReceiver callReceiver = new CallReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.PHONE_STATE");
            registerReceiver(callReceiver, filter);
        }
    }
    private void startCallService(){

    }
    @Override
    protected void onResume() {
        startCallReceiver();
        super.onResume();
    }

    public void handleStatus(){
        editor.putBoolean("status",!preferences.getBoolean("status",false));
        editor.commit();
        buttonStatus.setChecked(preferences.getBoolean("status",false));
        textStatus.setText(preferences.getBoolean("status",false)?"Ativado":"Desativado");
    }

    private void hasPermissions(Activity activity, Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissions, 1);
                }
            }
        }
    }

}