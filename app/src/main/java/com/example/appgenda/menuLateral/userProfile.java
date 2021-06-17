package com.example.appgenda.menuLateral;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appgenda.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class userProfile extends AppCompatActivity {
    TextView fullName, email, phone, verifyMsg;
    Button btnVerificar, editImage, btnAjustes, btnEventos;
    ImageView profileImage;

    FirebaseAuth fAuth;
    FirebaseUser user;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    ValueEventListener eventListener;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //Llamamos a los componentes de la pantalla de perfil
        phone = findViewById(R.id.profilePhone);
        fullName = findViewById(R.id.profileName);
        email = findViewById(R.id.profileEmail);
        verifyMsg = findViewById(R.id.textVerify);
        btnVerificar = findViewById(R.id.btnVerify);
        profileImage = findViewById(R.id.profileImg);
        editImage = findViewById(R.id.changeProfile);
        btnAjustes = findViewById(R.id.accesoAjustes);
        btnEventos = findViewById(R.id.accesoEventos);

        //Inicializamos la clase auth de firebase
        fAuth = FirebaseAuth.getInstance();

        //Inicializamos la clase storage de firebase
        storageReference = FirebaseStorage.getInstance().getReference();

        StorageReference profileRef = storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImage);
            }
        });

        //Obtenemos el id del usuario autenticado
        userId = fAuth.getCurrentUser().getUid();

        //Devuelve si el usuario inició sesión o no
        user = fAuth.getCurrentUser();

        //Condición que es ejecutada sobre la pantalla del perfil de usuario si no ha sido verificado el correo
        if(!user.isEmailVerified()){
            verifyMsg.setVisibility(View.VISIBLE);
            btnVerificar.setVisibility(View.VISIBLE);

            btnVerificar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Para enviar el link de verificación
                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(v.getContext(), "El correo de verificación ha sido enviado!!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("tag", "Error! El correo no pudo ser enviado!! " + e.getMessage());
                        }
                    });
                }
            });
        }else{
            verifyMsg.setVisibility(View.GONE);
            btnVerificar.setVisibility(View.GONE);
        }

        //Llamamos a la referencia de donde queremos sacar los datos
        databaseReference = FirebaseDatabase.getInstance().getReference().child(userId).child("InfoUser");
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullName.setText(snapshot.child("fName").getValue().toString());
                email.setText(snapshot.child("email").getValue().toString());
                phone.setText(snapshot.child("phone").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        databaseReference.addValueEventListener(eventListener);

        //Editar el perfil de usuario
        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Abrimos la galería
                Intent i = new Intent(v.getContext(), editProfile.class);
                i.putExtra("fullName", fullName.getText().toString());
                i.putExtra("email", email.getText().toString());
                i.putExtra("phone", phone.getText().toString());
                startActivity(i);
                finish();
            }
        });

        btnAjustes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Toast.makeText(userProfile.this, "Entrando en ajustes...", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), ajustes.class));
            }
        });

        btnEventos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Toast.makeText(userProfile.this, "Entrando en Mis Eventos...", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), allEventos.class));
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
}