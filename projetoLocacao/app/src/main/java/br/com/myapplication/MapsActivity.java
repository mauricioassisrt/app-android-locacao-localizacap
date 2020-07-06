package br.com.myapplication;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import Classes.EquipamentoClass;
import Classes.LocacoesClass;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, AdapterView.OnItemClickListener {
    FusedLocationProviderClient client;
    private GoogleMap mMap;
    Spinner s, equiapmentos;
    int contador=0;
    ArrayAdapter<String> adapterSpiner;
    ArrayList<String> listaIdCliente = new ArrayList<>();
    ArrayList<String> listaIdEquipamento = new ArrayList<>();

    ArrayAdapter<String> arrayAdpter;
    private FirebaseDatabase database;
    private DatabaseReference myRef ;
    private  String address, idcliente, idEquipamento;
    private EquipamentoClass objetoEquipamento;
    private LocacoesClass objetoLocacao;

    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //spiner de contatos do celular
        s = (Spinner) findViewById(R.id.spinner);
        //spiner de equipamentos
        equiapmentos = (Spinner) findViewById(R.id.spinner2);

        client = LocationServices.getFusedLocationProviderClient(this);

        buscaContatosCelular();
        preencherEquipamentos();

        //pega o id spiner e busca na lista de id do cliente do telefone
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Long item = parent.getItemIdAtPosition(position);
                idcliente = listaIdCliente.get(position);
              //  Toast.makeText(parent.getContext(), "Selected "+item+"ID DO TELEFONE "+ idcliente, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //pega o id spiner e busca na lista de id do equipamento
        equiapmentos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Long item = parent.getItemIdAtPosition(position);
                idEquipamento = listaIdEquipamento.get(position);
               // Toast.makeText(parent.getContext(), "Selected "+item+" ID DO EQUIPAMENTO "+ idEquipamento, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    public void buscaContatosCelular(){
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");
        adapterSpiner = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item);

        while (phones.moveToNext())
        {
            String  ids=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            listaIdCliente.add(ids);

            adapterSpiner.add(name);
            s.setAdapter(adapterSpiner);

        }
        phones.close();
    }
    public void preencherEquipamentos(){
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        arrayAdpter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item);
        myRef.child("equipamentos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String key = myRef.child("equipamentos").push().getKey();


                for (DataSnapshot equipamentoData: dataSnapshot.getChildren()){

                    EquipamentoClass objetoEquipamento = equipamentoData.getValue(EquipamentoClass.class);
                    boolean verificaLocacao = Boolean.parseBoolean(objetoEquipamento.getStatus());
                    if( objetoEquipamento.getStatus().equals("false")){

                        arrayAdpter.add(objetoEquipamento.getNome());
                        equiapmentos.setAdapter(arrayAdpter);
                        listaIdEquipamento.add(equipamentoData.getKey());
                    }else{
                        contador+=1;
                    }

                }
                if(listaIdEquipamento.size()==0){
                    arrayAdpter.add("SEM EQUIPAMENTO");
                    equiapmentos.setAdapter(arrayAdpter);
                    listaIdEquipamento.add("SEM EQUIPAMENTO");
                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    public void save(View view){
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        TextView enderecos = (TextView) findViewById(R.id.editEndereco);
        TextView dataLocacao = (TextView) findViewById(R.id.editDataLocacao);
        TextView dataVencimento = (TextView) findViewById(R.id.editDatavencimento);
        TextView valor = (TextView) findViewById(R.id.editValorLocacao);
        //obtem os equipamentos todos e compara o id selecionado do spiner com o do banco e seta no objeto equipamento alterando o status para true (Locado)
        myRef.child("equipamentos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //pega todos os equipamentos
                String key = myRef.child("equipamentos").push().getKey();
                for (DataSnapshot equipamentoData: dataSnapshot.getChildren()){
                //atribui a resposta no objeto
                    EquipamentoClass objetoEquipamento = equipamentoData.getValue(EquipamentoClass.class);
                    //se o id selecionado for igual o do banco altere o status
                    if( idEquipamento.equals(equipamentoData.getKey())){
                      objetoEquipamento.getNome();
                      objetoEquipamento.getCodigo();
                      objetoEquipamento.setStatus("true");
                        myRef.child("equipamentos").child(idEquipamento).setValue(objetoEquipamento);
                        preencherEquipamentos();

                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        if(listaIdEquipamento.get(0).equals("SEM EQUIPAMENTO")){
            Toast.makeText(this, "POSSUI EQUIPAMENTOS LOCADAOS VÁ ATÉ A TELA DE VENCIMENTOS PARA LIBERAR ", Toast.LENGTH_SHORT).show();
            System.out.println("ERRO1");
        }else{
            try {
                String key = myRef.child("locacoes").push().getKey();

                objetoLocacao = new LocacoesClass();
                objetoLocacao.setClienteid(idcliente);
                objetoLocacao.setLat(Double.toString(latitude));
                objetoLocacao.setLng(Double.toString(longitude));
                objetoLocacao.setEndereco(enderecos.getText().toString());
                objetoLocacao.setLocacao(dataLocacao.getText().toString());
                objetoLocacao.setVencimento(dataVencimento.getText().toString());
                objetoLocacao.setValor(valor.getText().toString());

                objetoLocacao.setEquipamentoid(idEquipamento.toString());

                myRef.child("locacoes").child(key).setValue(objetoLocacao);
                Toast.makeText(this, "Salvo com Sucesso", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);

            }catch (Exception e){
                Toast.makeText(this, "Erro", Toast.LENGTH_SHORT).show();
            }
        }


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        final Geocoder geocoder;
        geocoder = new Geocoder(this, Locale.getDefault());


        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {

                    @Override
                    public void onSuccess(Location location) {
                        List<Address> addresses = null;

                        if (location != null) {
                            try {
                                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                             address = addresses.get(0).getAddressLine(0);
                            LatLng coordenadas = new LatLng(location.getLatitude(), location.getLongitude());

                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            //Toast.makeText(MapsActivity.this, "+"+latitude, Toast.LENGTH_SHORT).show();
                            mMap.addMarker(new MarkerOptions().position(coordenadas).title(address));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(coordenadas));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordenadas,16));
                            TextView enderecos = (TextView) findViewById(R.id.editEndereco);
                            enderecos.setText(address.toString());
                            Calendar c = Calendar.getInstance();

                            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                            String formattedDate = df.format(c.getTime());
                            System.out.println("Format dateTime => " + formattedDate);
                            TextView dataAtual = (TextView) findViewById(R.id.editDataLocacao);

                            TextView dataVenc = (TextView) findViewById(R.id.editDatavencimento);
                            dataVenc.setText(formattedDate);
                            dataAtual.setText(formattedDate);

                        } else {
                            Log.i("test ", "null");
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }


                });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                List<Address> endereco = null;
                try {
                    TextView enderecos = (TextView) findViewById(R.id.editEndereco);


                    endereco = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

                    MarkerOptions markerOptions = new MarkerOptions();

                    // Setting the position for the marker
                    markerOptions.position(latLng);

                    Calendar c = Calendar.getInstance();

                    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                    String formattedDate = df.format(c.getTime());
                    System.out.println("Format dateTime => " + formattedDate);
                    TextView dataAtual = (TextView) findViewById(R.id.editDataLocacao);
                    dataAtual.setText(formattedDate);
                    // Setting the title for the marker.
                    // This will be displayed on taping the marker
                    latitude = latLng.latitude;
                    longitude = latLng.longitude;
                  //  Toast.makeText(MapsActivity.this, "+"+latitude, Toast.LENGTH_SHORT).show();
                    markerOptions.title(endereco.get(0).getAddressLine(0));
                    String adresss = endereco.get(0).getAddressLine(0);
                    // Clears the previously touched position
                    enderecos.setText(adresss.toString());
                    mMap.clear();

                    // Animating to the touched position
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                    // Placing a marker on the touched position
                    mMap.addMarker(markerOptions);
                  //  Toast.makeText(MapsActivity.this, "MAPA"+markerOptions.toString(), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }
}