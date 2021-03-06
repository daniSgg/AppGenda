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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appgenda.Evento;
import com.example.appgenda.R;
import com.google.android.gms.tasks.OnSuccessListener;
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

    // to check if we are connected to Network
    boolean isConnected = true;

    // to check if we are monitoring Network
    private boolean monitoringConnectivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_eventos);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        subTitulo = findViewById(R.id.subtitle);
        listView = findViewById(R.id.listV);

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
                    String Descripcion = ds.child("Descripci??n").getValue(String.class);
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

                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

                    try {

                        Date ini = sdf.parse(fechIni);
                        Date fin = sdf.parse(fechFin);
                        Date fechAct = sdf.parse(selectFecha);


                        if(((fechAct.after(ini)) || (fechAct.equals(ini))) && ((fechAct.before(fin)) || (fechAct.equals(fin)))){

                            arrayAdapter.add("\n Titulo del Evento: " + titulo + "\n " +
                                            "Descripci??n: " + descrip + "\n " +
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
        builder.setTitle("Seleccione una acci??n").setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if(i==0){
                    AlertDialog.Builder eventView = new AlertDialog.Builder(view.getContext());
                    eventView.setTitle("INFORMACI??N DEL EVENTO");
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
                    eliminar.setTitle("??Desea eliminar este evento?");
                    eliminar.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkConnectivity();
                            ListIterator<Evento> iterador = eventos.listIterator(position);
                            Evento e = iterador.next();
                            String titulo = e.getTitulos();
                            databaseReference.child(titulo).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(viewEventos.this, "Evento eliminado!!", Toast.LENGTH_SHORT).show();
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

    private ConnectivityManager.NetworkCallback connectivityCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            isConnected = true;
        }

        @Override
        public void onLost(Network network) {
            isConnected = false;
            Toast.makeText(viewEventos.this, "SE DEBE DISPONER DE CONEXI??N A INTERNET!!", Toast.LENGTH_SHORT).show();
        }
    };

    private void checkConnectivity() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

        if (!isConnected) {
            Toast.makeText(viewEventos.this, "SE DEBE DISPONER DE CONEXI??N A INTERNET!!", Toast.LENGTH_SHORT).show();

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