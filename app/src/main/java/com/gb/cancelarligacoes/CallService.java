package com.gb.cancelarligacoes;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.N)
public class CallService extends CallScreeningService {
    private Call.Details mDetails;
    private static final String FILE_PREFERENCE = "FilePreference";
    private SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("Iniciou a classe CAll SERVICE");
    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        Log.i("BindService",""+service);
        return super.bindService(service, conn, flags);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("onStartCommand","" + intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onScreenCall(@NonNull Call.Details callDetails) {
        String formattedPhoneNumber = PhoneNumberUtils.formatNumber(callDetails.getHandle().toString(),Locale.getDefault().getCountry());
        Log.i("phoneNumber: ",""+formattedPhoneNumber);
        mDetails = callDetails;
        preferences = getSharedPreferences(FILE_PREFERENCE, Context.MODE_PRIVATE);
        if (callDetails.getCallDirection() == Call.Details.DIRECTION_INCOMING && preferences.getBoolean("status",true) == true && getContactName(formattedPhoneNumber) == null) {
            blockCall();
        }
    }

    public void blockCall() {
        CallScreeningService.CallResponse
                response = new CallScreeningService.CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(true)
                .setSkipNotification(true)
                .build();
        respondToCall(mDetails, response);
    }

    private String getContactName(String phoneNumber){
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI , Uri.encode(phoneNumber));
        String[] projection = new String[] {ContactsContract.Contacts.DISPLAY_NAME};
        ContentResolver contentResolver = getContentResolver();
        Cursor query = contentResolver.query(uri,projection,null,null,null);
        String contactName = null;
        if (query.moveToFirst()) {
            contactName = query.getString(query.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }
        query.close();
        return contactName;
    }
}
