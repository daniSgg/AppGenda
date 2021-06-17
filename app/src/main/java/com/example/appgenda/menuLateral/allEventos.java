package com.example.appgenda.menuLateral;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appgenda.Agenda.modifEventos;
import com.example.appgenda.Evento;
import com.example.appgenda.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Objects;

public class allEventos extends AppCompatActivity implements AdapterView.OnItemLongClickListener {
    TextView subTitulo;

    DatabaseReference databaseReference;
    FirebaseAuth fAuth;
    String userID;
    LinkedList<Evento> eventos;
    ListView listView;
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_eventos);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        subTitulo = findViewById(R.id.subtitle);
        listView = findViewById(R.id.listV);

        eventos = new LinkedList<Evento>();
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        subTitulo.setText("Lista de todos los eventos");

        databaseReference = FirebaseDatabase.getInstance().getReference().child(userID).child("Eventos");

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String Titulo = ds.child("Titulo").getValue(String.class);
                    String Descripcion = ds.child("Descripción").getValue(String.class);
                    String fechaDesde = ds.child("fechaDesde").getValue(String.class);
                    String fechaHasta = ds.child("fechaHasta").getValue(String.class);
                    eventos.add(new Evento(Titulo, Descripcion, fechaDesde, fechaHasta));
                }

                ListIterator<Evento> iterador = eventos.listIterator();

                while (iterador.hasNext()){
                    Evento e = iterador.next();

                    String titulo = e.getTitulos();
                    String descrip = e.getDescripcion();
                    String fechIni = e.getFechaDesde();
                    String fechFin = e.getFechaHasta();

                    arrayAdapter.add("\n Titulo del Evento: " + titulo + "\n " +
                            "Descripción: " + descrip + "\n " +
                            "Fecha de Inicio: " + fechIni + "\n " +
                            "Fecha de Fin: " + fechFin + "\n");


                    listView.setAdapter(arrayAdapter);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };


        databaseReference.addListenerForSingleValueEvent(eventListener);
        listView.setOnItemLongClickListener(this);

        super.onResume();


    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(view.getContext());
        CharSequence[] items = new CharSequence[4];
        items[0] = "Ver evento";
        items[1] = "Modificar evento";
        items[2] = "Eliminar evento";
        items[3] = "Cancelar";

        //Metodos
        builder.setTitle("Seleccione una acción").setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if(i==0){
                    AlertDialog.Builder eventView = new AlertDialog.Builder(view.getContext());
                    eventView.setTitle("INFORMACIÓN DEL EVENTO");
                    eventView.setMessage(arrayAdapter.getItem(position));
                    eventView.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });

                    eventView.create().show();

                }else if(i==1){
                    Intent intent = new Intent(view.getContext(), modifEventos.class);
                    ListIterator<Evento> iterador = eventos.listIterator(position);

                    Evento e = iterador.next();
                    String titulo = e.getTitulos();
                    String descripcion = e.getDescripcion();
                    String fechDesde = e.getFechaDesde();
                    String fechHasta = e.getFechaHasta();

                    intent.putExtra("titulo", titulo);
                    intent.putExtra("descripcion", descripcion);
                    intent.putExtra("fechDesde", fechDesde);
                    intent.putExtra("fechHasta", fechHasta);

                    startActivity(intent);
                    finish();

                }else if(i==2){
                    AlertDialog.Builder eliminar = new AlertDialog.Builder(view.getContext());
                    eliminar.setTitle("¿Desea eliminar este evento?");
                    eliminar.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ListIterator<Evento> iterador = eventos.listIterator(position);
                            Evento e = iterador.next();
                            String titulo = e.getTitulos();
                            databaseReference.child(titulo).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(allEventos.this, "Evento eliminado!!", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                        }
                    });

                    eliminar.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });

                    eliminar.create().show();

                }else{
                    return;
                }
            }
        });

        android.app.AlertDialog dialog = builder.create();
        dialog.show();
        return false;
    }


}