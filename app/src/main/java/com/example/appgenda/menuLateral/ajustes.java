package com.example.appgenda.menuLateral;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appgenda.Authentication.Login;
import com.example.appgenda.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ajustes extends AppCompatActivity {
    Button deleteAccount, restearPasswd, btnGit, btnYT, btnTWI, btnCorreo;

    FirebaseAuth fAuth;
    FirebaseUser fUser;
    DatabaseReference eliminarDatos;

    String userId;

    // to check if we are connected to Network
    boolean isConnected = true;

    // to check if we are monitoring Network
    private boolean monitoringConnectivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        deleteAccount = findViewById(R.id.btnEliminar);
        restearPasswd = findViewById(R.id.resetPasswdLocal);
        btnGit = findViewById(R.id.btnGit);
        btnYT = findViewById(R.id.btnYT);
        btnTWI = findViewById(R.id.btnTWI);
        btnCorreo = findViewById(R.id.btnCorreo);

        fAuth = FirebaseAuth.getInstance();
        fUser = fAuth.getCurrentUser();

        userId = fAuth.getCurrentUser().getUid();

        eliminarDatos = FirebaseDatabase.getInstance().getReference().child(userId);

        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(ajustes.this);
                dialog.setTitle("¿Estas seguro?");
                dialog.setMessage("Eliminar esta cuenta dará como resultado la eliminación completa de " +
                        "su cuenta del sistema y no podrá acceder a la aplicación.");
                dialog.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        checkConnectivity();

                        fUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    eliminarDatos.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(ajustes.this, "Cuenta Eliminada", Toast.LENGTH_SHORT).show();
                                                finish();
                                                startActivity(new Intent(getApplicationContext(), Login.class));
                                                onStop();
                                            }else{
                                                Toast.makeText(ajustes.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                }else{
                                    Toast.makeText(ajustes.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

                dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });

               dialog.create().show();
            }
        });

        //Botón para cambiar la contraseña
        restearPasswd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creamos un campo donde podamos escribir nuestro email
                EditText resetPassword = new EditText(v.getContext());

                //Creamos un cuadro de dialogo para preguntar si quiere restablecer la contraseña
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("¿Quieres cambiar la contraseña?");
                passwordResetDialog.setMessage("Introduzca una nueva contraseña mayor a 6 caracteres de longitud. " +
                        "              ¡AVISO! Si cambia la contraseña se cerrará la sesión automáticamente.");
                passwordResetDialog.setView(resetPassword);

                //Si presionamos el botón si...
                passwordResetDialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkConnectivity();

                        //Recibe el email y envia el link para resetear la contraseña
                        String newPassword = resetPassword.getText().toString();
                        fUser.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(ajustes.this, "La contraseña se cambió correctamente.", Toast.LENGTH_SHORT).show();

                                //Cerramos la sesión del usuario activo
                                FirebaseAuth.getInstance().signOut();
                                Toast.makeText(ajustes.this, "Cerrando Sesión...", Toast.LENGTH_SHORT).show();
                                finish();

                                //Volvemos a una ventana vacia de login
                                startActivity(new Intent(getApplicationContext(), Login.class));
                                onStop();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ajustes.this, "El cambio de contraseña falló.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                //Si presionamos el botón no...
                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Cierra
                        return;
                    }
                });

                //Para crear e iniciar el cuadro de dialogo
                passwordResetDialog.create().show();

            }
        });

        btnGit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkConnectivity();

                String url = "https://github.com/daniSgg/AppGenda";
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        btnYT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkConnectivity();

                String url = "https://www.youtube.com/channel/UCn_86qODO0Xe97p5HOt9RPg";
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        btnTWI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkConnectivity();

                String url = "https://twitter.com/AppGenda_Info";
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        btnCorreo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
                alert.setTitle("CORREO DE CONTACTO");
                alert.setMessage("appGenda.contactUser@gmail.com");
                alert.setPositiveButton("Enviar correo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkConnectivity();

                        Intent email = new Intent(Intent.ACTION_SEND);
                        email.putExtra(Intent.EXTRA_SUBJECT, "Cabecera de ejemplo");
                        email.putExtra(Intent.EXTRA_TEXT, "El correo de contacto es: " +
                                "appGenda.contactUser@gmail.com");
                        email.setType("message/rfc822");
                        startActivity(Intent.createChooser(email, "Elige tu cliente de correo:"));
                    }
                });

                alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });

                alert.create().show();
            }
        });

    }

    //Cierro sesión
    public void logout(View view) {
        AlertDialog.Builder logout = new AlertDialog.Builder(ajustes.this);
        logout.setTitle("¿Desea cerrar sesión?");
        logout.setPositiveButton("Cerrar Sesión", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkConnectivity();

                //Cerramos la sesión del usuario activo
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(ajustes.this, "Cerrando Sesión...", Toast.LENGTH_SHORT).show();
                finish();

                //Volvemos a una ventana vacia de login
                startActivity(new Intent(getApplicationContext(), Login.class));
                onStop();
            }
        });

        logout.setNegativeButton("Cencelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Cierra
                return;
            }
        });

        logout.create().show();

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
            Toast.makeText(ajustes.this, "SE DEBE DISPONER DE CONEXIÓN A INTERNET", Toast.LENGTH_SHORT).show();
        }
    };

    private void checkConnectivity() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

        if (!isConnected) {
            Toast.makeText(ajustes.this, "SE DEBE DISPONER DE CONEXIÓN A INTERNET!!", Toast.LENGTH_SHORT).show();

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