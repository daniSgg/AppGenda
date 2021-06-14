package com.example.appgenda;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.example.appgenda.Authentication.Login;

public class MainActivity extends AppCompatActivity {

    // to check if we are connected to Network
    boolean isConnected = true;

    // to check if we are monitoring Network
    private boolean monitoringConnectivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        super.onResume();
        checkConnectivity();

    }

    private ConnectivityManager.NetworkCallback connectivityCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            isConnected = true;
            finish();
            startActivity(new Intent(getApplicationContext(), Login.class));

        }

        @Override
        public void onLost(Network network) {
            isConnected = false;
            Toast.makeText(MainActivity.this, "SE DEBE DISPONER DE CONEXIÓN A INTERNET", Toast.LENGTH_SHORT).show();
        }
    };

    // Method to check network connectivity in Main Activity
    private void checkConnectivity() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

        if(isConnected){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                }
            }, 2000);


        }

        if (!isConnected) {
            Toast.makeText(MainActivity.this, "SE DEBE DISPONER DE CONEXIÓN A INTERNET", Toast.LENGTH_SHORT).show();

// if Network is not connected we will register a network callback to  monitor network
            connectivityManager.registerNetworkCallback(
                    new NetworkRequest.Builder()
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .build(), connectivityCallback);
            monitoringConnectivity = true;
        }

    }

    @Override
    protected void onPause() {
        // if network is being moniterd then we will unregister the network callback
        if (monitoringConnectivity) {
            final ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.unregisterNetworkCallback(connectivityCallback);
            monitoringConnectivity = false;
        }
        super.onPause();
    }

}