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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appgenda.Agenda.Calendar;
import com.example.appgenda.R;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    EditText etEmail, etPassword;
    Button btnLogin;
    TextView createBtn, forgotTextLink;
    ProgressBar progressBar;
    FirebaseAuth fAuth;

    // to check if we are connected to Network
    boolean isConnected = true;

    // to check if we are monitoring Network
    private boolean monitoringConnectivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Llamamos a los componentes de la pantalla de login
        etEmail = findViewById(R.id.Email);
        etPassword = findViewById(R.id.Password);
        btnLogin = findViewById(R.id.btnLogin);
        createBtn = findViewById(R.id.createText);
        forgotTextLink = findViewById(R.id.forgotPassword);

        //Llamamos al componente progressBar
        progressBar = findViewById(R.id.progressBar2);

        //Inicializamos la clase auth de firebase
        fAuth = FirebaseAuth.getInstance();

        //Si cuando iniciemos la aplicación ya esta iniciada la sesión, que nos habra directamente la clase calendario
        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), Calendar.class));
            finish();
        }

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

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

            //Autenticamos el usuario
            fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    //Muestra un mensaje cuando se haya completado la función de logeo
                    Toast.makeText(Login.this, "Logeado correctamente!!", Toast.LENGTH_SHORT).show();
                    finish();

                    //Cuando se haya completado, se muestra una nueva pantalla
                    startActivity(new Intent(getApplicationContext(), Calendar.class));

                }else{
                    //Muestra mensaje de error
                    Toast.makeText(Login.this, "Error!! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            });

            checkConnectivity();
        });

        //Hacemos botón los distintos text view
        forgotTextLink.setOnClickListener(v -> {
            //Creamos un campo donde podamos escribir nuestro email
            EditText resetMail = new EditText(v.getContext());

            //Creamos un cuadro de dialogo para preguntar si quiere restablecer la contraseña
            AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
            passwordResetDialog.setTitle("¿Has olvidado la contraseña?");
            passwordResetDialog.setMessage("Introduzca su email para recibir el link para restablecer la contraseña.");
            passwordResetDialog.setView(resetMail);

            //Si presionamos el botón si...
            passwordResetDialog.setPositiveButton("Si", (dialog, which) -> {
                checkConnectivity();

                //Recibe el email y envia el link para resetear la contraseña
                String email = resetMail.getText().toString();
                fAuth.sendPasswordResetEmail(email)
                        .addOnSuccessListener(unused -> Toast.makeText(Login.this, "El link para restablecer la contraseña ha sido enviado a su Email.", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(Login.this, "Error! El link de reseteo no ha sido enviado!! " + e.getMessage(), Toast.LENGTH_SHORT).show());
            });

            //Si presionamos el botón no...
            passwordResetDialog.setNegativeButton("No", (dialog, which) -> {
                //Cierra

            });

            //Para crear e iniciar el cuadro de dialogo
            passwordResetDialog.create().show();

        });

        createBtn.setOnClickListener(v -> {
            //Cerramos la activity de actual y pasamos a la que queremos abrir (para que no se apilen)
            finish();

            //Accedemos a la pantalla de registro al pulsar en este botón
            startActivity(new Intent(getApplicationContext(), Registrar.class));
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
            Toast.makeText(Login.this, "SE DEBE DISPONER DE CONEXIÓN A INTERNET!!", Toast.LENGTH_SHORT).show();
        }
    };

    private void checkConnectivity() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

        if (!isConnected) {
            Toast.makeText(Login.this, "SE DEBE DISPONER DE CONEXIÓN A INTERNET!!", Toast.LENGTH_SHORT).show();

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