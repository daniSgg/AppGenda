package com.example.appgenda.Agenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.appgenda.R;
import com.example.appgenda.menuLateral.editProfile;
import com.example.appgenda.menuLateral.userProfile;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class addEventos extends AppCompatActivity {
    EditText nEvento, fechDesde, fechHasta, descripcion;
    Button guardar, cancelar;

    DatabaseReference databaseReference;
    ValueEventListener eventListener;
    FirebaseAuth fAuth;
    String userID;

    // to check if we are connected to Network
    boolean isConnected = true;

    // to check if we are monitoring Network
    private boolean monitoringConnectivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_eventos);

        nEvento = findViewById(R.id.edtNombreEvento);
        fechDesde = findViewById(R.id.edtFechaDesde);
        fechHasta = findViewById(R.id.edtFechaHasta);
        descripcion = findViewById(R.id.edtDescripcion);

        guardar = findViewById(R.id.btnGuardar);
        cancelar = findViewById(R.id.btnCancelar);

        int dia=0, mes=0, anio=0;
        dia = getIntent().getIntExtra("dia", dia);
        mes = getIntent().getIntExtra("mes", mes+1);
        anio = getIntent().getIntExtra("anio", anio);

        fechDesde.setText(anio + "-" + mes + "-" + dia);
        fechHasta.setText(anio + "-" + mes + "-" + dia);

        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titulo = nEvento.getText().toString();
                String fechDsd = fechDesde.getText().toString();
                String fechHst = fechHasta.getText().toString();
                String descrip = descripcion.getText().toString();

                if(TextUtils.isEmpty(titulo)){
                    nEvento.setError("El título del evento es requerido!!");
                    return;
                }

                if(TextUtils.isEmpty(fechDsd)){
                    fechDesde.setError("La fecha de inicio del evento es requerido!!");
                    return;
                }

                if(TextUtils.isEmpty(fechHst)){
                    fechHasta.setError("La fecha de fin del evento es requerido!!");
                    return;
                }

                databaseReference = FirebaseDatabase.getInstance().getReference().child(userID);

                Map<String, Object> evento = new HashMap<>();
                evento.put("Titulo", titulo);
                evento.put("Descripción", descrip);
                evento.put("fechaDesde", fechDsd);
                evento.put("fechaHasta", fechHst);

                databaseReference.child("Eventos")/*.child(fecha)*/.child(titulo).setValue(evento).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(addEventos.this, "Evento añadido!!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), Calendar.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(addEventos.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                checkConnectivity();
            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(fAuth.getCurrentUser() == null){
            this.finish();
        }
    }

    private ConnectivityManager.NetworkCallback connectivityCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            isConnected = true;
        }

        @Override
        public void onLost(Network network) {
            isConnected = false;
            Toast.makeText(addEventos.this, "SE DEBE DISPONER DE CONEXIÓN A INTERNET", Toast.LENGTH_SHORT).show();
        }
    };

    private void checkConnectivity() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

        if (!isConnected) {
            Toast.makeText(addEventos.this, "SE DEBE DISPONER DE CONEXIÓN A INTERNET", Toast.LENGTH_SHORT).show();

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