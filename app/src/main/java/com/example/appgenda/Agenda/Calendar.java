package com.example.appgenda.Agenda;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.appgenda.R;
import com.example.appgenda.menuLateral.ajustes;
import com.example.appgenda.menuLateral.allEventos;
import com.example.appgenda.menuLateral.userProfile;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class Calendar extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CalendarView.OnDateChangeListener {
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;
    CalendarView calendarView;

    FirebaseAuth fAuth;

    // to check if we are connected to Network
    boolean isConnected = true;

    // to check if we are monitoring Network
    private boolean monitoringConnectivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        fAuth = FirebaseAuth.getInstance();

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(this);

        calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(fAuth.getCurrentUser() == null){
            this.finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(toggle.onOptionsItemSelected(item)){
            return true;
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.perfil:
                Toast.makeText(this, "Entrando en Mi Perfil", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), userProfile.class));
                checkConnectivity();
                break;
            case R.id.eventos:
                Toast.makeText(this, "Entrando en Mis Eventos", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), allEventos.class));
                checkConnectivity();
                break;
            case R.id.settings:
                Toast.makeText(this, "Entrando en Mis Ajustes", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), ajustes.class));
                break;
            case R.id.cerrar:
                AlertDialog.Builder salir = new AlertDialog.Builder(this);
                salir.setTitle("¿Desea salir de la aplicación?");

                salir.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

                salir.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Cierra

                    }
                });

                salir.create().show();
                break;

        }

        super.onResume();

        if(fAuth.getCurrentUser() == null){
            this.finish();
        }

        return true;
    }

    @Override
    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(view.getContext());
        CharSequence []items = new CharSequence[3];
        items[0] = "Agregar Evento";
        items[1] = "Ver eventos";
        items[2] = "Cancelar";

        final int dia = year, mes = month, anio = dayOfMonth;

        builder.setTitle("Seleccione una tarea").setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int i) {
                if(i==0){
                    Intent intent = new Intent(view.getContext(), addEventos.class);
                    intent.putExtra("dia", dia);
                    intent.putExtra("mes", mes+1);
                    intent.putExtra("anio", anio);
                    startActivity(intent);
                }else if(i==1){
                    Intent intent = new Intent(view.getContext(), viewEventos.class);
                    intent.putExtra("dia", dia);
                    intent.putExtra("mes", mes+1);
                    intent.putExtra("anio", anio);
                    startActivity(intent);
                    checkConnectivity();

                }else{
                    return;
                }
            }
        });

        android.app.AlertDialog dialog = builder.create();
        dialog.show();

    }

    private ConnectivityManager.NetworkCallback connectivityCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            isConnected = true;
        }

        @Override
        public void onLost(Network network) {
            isConnected = false;
            Toast.makeText(Calendar.this, "SE DEBE DISPONER DE CONEXIÓN A INTERNET", Toast.LENGTH_SHORT).show();
        }
    };

    private void checkConnectivity() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

        if (!isConnected) {
            Toast.makeText(Calendar.this, "SE DEBE DISPONER DE CONEXIÓN A INTERNET!!", Toast.LENGTH_SHORT).show();

            connectivityManager.registerNetworkCallback(new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build(), connectivityCallback);
            monitoringConnectivity = true;
        }
    }

    @Override
    protected void onPause() {
        if (monitoringConnectivity) {
            final ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.unregisterNetworkCallback(connectivityCallback);
            monitoringConnectivity = false;
        }
        super.onPause();
    }
}