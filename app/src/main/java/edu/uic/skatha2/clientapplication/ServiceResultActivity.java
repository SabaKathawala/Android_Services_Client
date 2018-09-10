package edu.uic.skatha2.clientapplication;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.Parcelable;

import edu.uic.skatha2.services.DailyCash;

public class ServiceResultActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_service_result);
        // get data to populate
        Parcelable[] serviceData = getIntent().getParcelableArrayExtra(ClientActivity.SERVICE_DATA);
        // set adapter
        getListView().setAdapter(new DataAdapter(getApplicationContext(), serviceData));
    }
}

