package com.example.appgenda.Agenda;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.appgenda.Evento;
import com.example.appgenda.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Objects;

public class viewEventos extends AppCompatActivity implements AdapterView.OnItemLongClickListener {
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
        setContentView(R.layout.activity_view_eventos);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        subTitulo = findViewById(R.id.subtitle);
        listView = findViewById(R.id.listV);
        listView.setOnItemLongClickListener(this);

        eventos = new LinkedList<Evento>();
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        int dia=0, mes=0, anio=0;
        dia = getIntent().getIntExtra("dia", dia);
        mes = getIntent().getIntExtra("mes", mes+1);
        anio = getIntent().getIntExtra("anio", anio);

        subTitulo.setText(anio + "-" + mes + "-" + dia);

        String selectFecha = anio + "-" + mes + "-" + dia;

        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();

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
                //AQUI YA HEMOS ACABADO CON TODOS LOS EVENTOS
                String a = "";

                ListIterator<Evento> iterador = eventos.listIterator();

                while (iterador.hasNext()){
                    Evento e = iterador.next();

                    String titulo = e.getTitulos();
                    String descrip = e.getDescripcion();
                    String fechIni = e.getFechaDesde();
                    String fechFin = e.getFechaHasta();

                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

                    try {

                        Date ini = sdf.parse(fechIni);
                        Date fin = sdf.parse(fechFin);
                        Date fechAct = sdf.parse(selectFecha);


                        if(((fechAct.after(ini)) || (fechAct.equals(ini))) && ((fechAct.before(fin)) || (fechAct.equals(fin)))){

                            arrayAdapter.add("\n Titulo del Evento: " + titulo + "\n " +
                                            "Descripción: " + descrip + "\n " +
                                            "Fecha de Inicio: " + fechIni + "\n " +
                                            "Fecha de Fin: " + fechFin + "\n");

                        }else{

                        }

                        listView.setAdapter(arrayAdapter);


                    } catch (ParseException parseException) {
                        parseException.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        databaseReference.addListenerForSingleValueEvent(eventListener);
        listView.setOnItemLongClickListener(this);

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        CharSequence[] items = new CharSequence[3];
        items[0] = "Ver evento";
        items[1] = "Eliminar evento";
        items[2] = "Cancelar";

        //Metodos

        AlertDialog dialog = builder.create();
        dialog.show();
        return false;
    }
}