package com.example.appgenda.Authentication;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appgenda.Agenda.Calendar;
import com.example.appgenda.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Registrar extends AppCompatActivity {
    EditText etFullName, etEmail, etPassword, etPhone;
    Button btnRegistro;
    TextView tvLoginBtn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    String userID;

    // to check if we are connected to Network
    boolean isConnected = true;

    // to check if we are monitoring Network
    private boolean monitoringConnectivity = false;

    public static final String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        //Llamamos a los componentes de la pantalla de registro
        etFullName = findViewById(R.id.fullName);
        etEmail = findViewById(R.id.Email);
        etPassword = findViewById(R.id.Password);
        etPhone = findViewById(R.id.phone);
        btnRegistro = findViewById(R.id.btnRegistro);
        tvLoginBtn = findViewById(R.id.createText);

        //Llamamos al componente progressBar
        progressBar = findViewById(R.id.progressBar);

        //Inicializamos la clase auth de firebase
        fAuth = FirebaseAuth.getInstance();

        //Acción que ocurre cuando hacemos "click" sobre el boton registrar
        btnRegistro.setOnClickListener(v -> {
            String nomCompleto = etFullName.getText().toString();
            String telefono = etPhone.getText().toString();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            //Mensaje de error que es provocado si el campo nombre completo se encuentra vacio
            if(TextUtils.isEmpty(nomCompleto)){
                etFullName.setError("El campo nombre completo es requerido!!");
                return;
            }

            //Mensaje de error que es provocado si el campo teléfono se encuentra vacio
            if(TextUtils.isEmpty(telefono)){
                etPhone.setError("El campo teléfono es requerido!!");
                return;
            }

            //Mensaje de error que es provocado si el campo email se encuentra vacio
            if(TextUtils.isEmpty(email)){
                etEmail.setError("El campo email es requerido!!");
                return;
            }

            //Mensaje de error que es provocado si el campo contraseña se encuentra vacio
            if(TextUtils.isEmpty(password)){
                etPassword.setError("El campo contraseña es requerido!!");
                return;
            }

            //Establezco una longitud para la contraseña
            if(password.length() < 6){
                etPassword.setError("La contraseña debe tener mas de 6 carácteres!!");
                return;
            }

            //Hacemos el progressbar visible una vez cumplimos los requisitos anteriores
            progressBar.setVisibility(View.VISIBLE);

            //Para registrar el usuario en Firebase
            fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    //Para enviar el link de verificación
                    FirebaseUser FirebaseUser = fAuth.getCurrentUser();
                    FirebaseUser.sendEmailVerification()
                            .addOnSuccessListener(aVoid -> Toast.makeText(Registrar.this, "El correo de verificación ha sido enviado!!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Log.d(TAG, "Error! El correo no pudo ser enviado!! " + e.getMessage()));

                    //Muestra un mensaje cuando se haya completado la función de registro
                    Toast.makeText(Registrar.this, "Usuario creado!!", Toast.LENGTH_SHORT).show();

                    //Devuelve el id del usuario registrado
                    userID = fAuth.getCurrentUser().getUid();

                    //Creamos una referencia al usuario a través de la id de usuario
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(userID);

                    //Creamos un array en el que almacenaremos la información que queremos mostrar del usuario registrado
                    Map<String, Object> user = new HashMap<>();
                    user.put("fName", nomCompleto);
                    user.put("email", email);
                    user.put("phone", telefono);

                    databaseReference.child("InfoUser").setValue(user).addOnSuccessListener(unused -> {
                        Log.d(TAG, "Éxito: Perfil de usuario creado para " + userID);
                        finish();
                    }).addOnFailureListener(e -> Log.d(TAG, "FALLO: " + e.toString()));

                    //Cuando se haya completado, se muestra una nueva pantalla
                    startActivity(new Intent(getApplicationContext(), Calendar.class));

                }else{
                    //Muestra mensaje de error
                    Toast.makeText(Registrar.this, "Error!! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            });

            checkConnectivity();
        });

        //Hacemos botón el text view
        tvLoginBtn.setOnClickListener(v -> {
            //Cerramos la pestaña de actual y pasamos a la que queremos abrir (para que no se apilen)
            finish();

            //Accedemos a la pantalla de login al pulsar en este botón
            startActivity(new Intent(getApplicationContext(), Login.class));

        });
    }

    private ConnectivityManager.NetworkCallback connectivityCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            isConnected = true;
        }

        @Override
        public void onLost(Network network) {
            isConnected = false;
            Toast.makeText(Registrar.this, "SE DEBE DISPONER DE CONEXIÓN A INTERNET!!", Toast.LENGTH_SHORT).show();
        }
    };

    private void checkConnectivity() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

        if (!isConnected) {
            Toast.makeText(Registrar.this, "SE DEBE DISPONER DE CONEXIÓN A INTERNET!!", Toast.LENGTH_SHORT).show();

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