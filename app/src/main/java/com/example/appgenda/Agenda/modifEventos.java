package com.example.appgenda.Agenda;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appgenda.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class modifEventos extends AppCompatActivity {
    EditText nEvento, fechDesde, fechHasta, descripcion;
    Button guardar, cancelar;

    DatabaseReference databaseReference;
    FirebaseAuth fAuth;
    String userID;

    // to check if we are connected to Network
    boolean isConnected = true;

    // to check if we are monitoring Network
    private boolean monitoringConnectivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modif_eventos);

        String titulo = getIntent().getStringExtra("titulo");
        String descrp = getIntent().getStringExtra("descripcion");
        String fdsd = getIntent().getStringExtra("fechDesde");
        String fhst = getIntent().getStringExtra("fechHasta");

        nEvento = findViewById(R.id.edtNombreEvento);
        fechDesde = findViewById(R.id.edtFechaDesde);
        fechHasta = findViewById(R.id.edtFechaHasta);
        descripcion = findViewById(R.id.edtDescripcion);

        guardar = findViewById(R.id.btnGuardar);
        cancelar = findViewById(R.id.btnCancelar);

        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference().child(userID).child("Eventos");

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nEvento.getText().toString().isEmpty() || fechDesde.getText().toString().isEmpty() || fechHasta.getText().toString().isEmpty()){
                    Toast.makeText(modifEventos.this, "Hay campos vacios!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> update = new HashMap<>();
                String tlt = String.valueOf(nEvento.getText()),
                        des = String.valueOf(descripcion.getText()),
                        desc = String.valueOf(fechDesde.getText()),
                        hast = String.valueOf(fechHasta.getText());

                update.put("Titulo", tlt);
                update.put("Descripción", des);
                update.put("fechaDesde", desc);
                update.put("fechaHasta", hast);

                databaseReference.child(tlt).updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(modifEventos.this, "Evento Actualizado!", Toast.LENGTH_SHORT).show();

                        databaseReference.child(titulo).removeValue();
                        databaseReference.child(tlt).setValue(update);

                        finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(modifEventos.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

        nEvento.setText(titulo);
        descripcion.setText(descrp);
        fechDesde.setText(fdsd);
        fechHasta.setText(fhst);

        super.onResume();
    }

    private ConnectivityManager.NetworkCallback connectivityCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            isConnected = true;
        }

        @Override
        public void onLost(Network network) {
            isConnected = false;
            Toast.makeText(modifEventos.this, "SE DEBE DISPONER DE CONEXIÓN A INTERNET!!", Toast.LENGTH_SHORT).show();
        }
    };

    private void checkConnectivity() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

        if (!isConnected) {
            Toast.makeText(modifEventos.this, "SE DEBE DISPONER DE CONEXIÓN A INTERNET!!", Toast.LENGTH_SHORT).show();

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