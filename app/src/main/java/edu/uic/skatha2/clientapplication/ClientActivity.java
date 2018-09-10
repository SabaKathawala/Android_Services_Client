package edu.uic.skatha2.clientapplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import edu.uic.skatha2.services.DailyCash;
import edu.uic.skatha2.services.BalanceService;

public class ClientActivity extends Activity {

    private Button createButton;
    private Button queryButton;

    private Spinner year;
    private EditText month;
    private EditText day;
    private EditText range;

    private BalanceService mBalanceService;
    private boolean mIsBound = false;
    private boolean isDatabaseCreated = false;
    public static String SERVICE_DATA = "SERVICE_DATA";
    protected static final String TAG = "Client";
    private static String validation;
    public static Map<Integer, Integer> monthToDay = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        // map for month and day validation
        monthToDay.put(1, 31);
        monthToDay.put(2, 28);
        monthToDay.put(3, 31);
        monthToDay.put(4, 30);
        monthToDay.put(5, 31);
        monthToDay.put(6, 30);
        monthToDay.put(7, 31);
        monthToDay.put(8, 31);
        monthToDay.put(9, 30);
        monthToDay.put(10, 31);
        monthToDay.put(11, 30);
        monthToDay.put(12, 31);

        //gives you a dropdown to select year: 2017/2018
        year = (Spinner) findViewById(R.id.year_input);

        // Create an ArrayAdapter using the string array and a default pinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.year, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        year.setAdapter(adapter);


        createButton = (Button) findViewById(R.id.create_button);
        queryButton = (Button) findViewById(R.id.query_button);

        month = (EditText) findViewById(R.id.month_input);
        day = (EditText) findViewById(R.id.day_input);
        range = (EditText) findViewById(R.id.range_input);

        createButton.setOnClickListener(createButtonListener);
        queryButton.setOnClickListener(queryButtonListener);

    }

    View.OnClickListener  createButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(BalanceService.class.getName());

            // Must make intent explicit or lower target API level to 19.
            ResolveInfo info = getPackageManager().resolveService(intent, 0);
            intent.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

//            mIsBound = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
//            if (mIsBound) {
//                Log.i(TAG, "bindService() succeeded!");
//            } else {
//                Log.i(TAG, "bindService() failed!");
//            }

            Thread service = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(!mIsBound);
                    try {
                        boolean isCreated = mBalanceService.createDatabase();
                        if(isCreated) {
                            isDatabaseCreated = true;
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
            service.start();
        }
    };

    View.OnClickListener  queryButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isDatabaseCreated) {
                final int yearValue = Integer.parseInt(year.getSelectedItem().toString());
                final int monthValue = Integer.parseInt(month.getText().toString());
                final int dayValue = Integer.parseInt(day.getText().toString());
                final int rangeValue = Integer.parseInt(range.getText().toString());

                if(!validateRange(yearValue, monthValue, dayValue, rangeValue)) {
                   Toast.makeText(getApplicationContext(), validation, Toast.LENGTH_LONG).show();
                   return;
                }
                Thread service = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DailyCash[] output = mBalanceService.dailyCash(dayValue, monthValue, yearValue, rangeValue);
                            Intent intent = new Intent(ClientActivity.this, ServiceResultActivity.class);
                            if(output == null) {
                                output = new DailyCash[0];
                            }
                            intent.putExtra(SERVICE_DATA, output);
                            startActivity(intent);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
                service.start();
            } else {
                Toast.makeText(getApplicationContext(), "Please click on Create Database first", Toast.LENGTH_LONG).show();
            }
        }
    };

    private boolean validateRange(int yearValue, int monthValue, int dayValue, int rangeValue) {
        if(!monthToDay.containsKey(monthValue)) {
            validation = "Month could only have value from 1 to 12";
            return false;
        }
        int range = monthToDay.get(monthValue);
        if(!(dayValue >= 1 && dayValue <= range)) {
            validation = "Day exceeds the valid value for the month";
            return false;
        }
        if(yearValue == 2018) {
            if((monthValue == 3 && dayValue > 2) || monthValue > 3 ){
                validation = "The last acceptable date is March 2, 2018";
                return false;
            }
        }
        validation = "Range should lie between 1 to 30";
        return rangeValue >= 1 && rangeValue <= 30;

    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder iService) {
            mBalanceService = BalanceService.Stub.asInterface(iService);
            mIsBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            mBalanceService = null;
            mIsBound = false;

        }
    };

    // Bind to KeyGenerator Service
    @Override
    protected void onResume() {
        super.onResume();

        if (!mIsBound) {

            boolean b;
            Intent i = new Intent(BalanceService.class.getName());

            // Must make intent explicit
            ResolveInfo info = getPackageManager().resolveService(i, 0);
            i.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

            b = bindService(i, this.mConnection, Context.BIND_AUTO_CREATE);
            if (b) {
                Log.i(TAG, "bindService() succeeded!");
            } else {
                Log.i(TAG, "bindService() failed!");
            }

        }
    }

    @Override
    protected void onPause() {
        if (mIsBound) {
            unbindService(this.mConnection);
            mIsBound = false;
        }
        super.onPause();
    }

}
