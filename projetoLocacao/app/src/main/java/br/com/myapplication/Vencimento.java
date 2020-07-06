package br.com.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import Classes.EquipamentoClass;
import Classes.LocacoesClass;

public class Vencimento  extends AppCompatActivity  implements OnMapReadyCallback  {

    private GoogleMap mMap;
    FusedLocationProviderClient client;
    private List<String> listaIdCliente = new ArrayList<>();
    private List<String> listaNomesContatos = new ArrayList<>();
    private List<String> listaEquipamentosNome = new ArrayList<>();
    private List<String> listaIdEquipamentos = new ArrayList<>();
     ArrayList<LocacoesClass> listaLocacoes;
    private FirebaseDatabase database;
    private DatabaseReference myRef ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vencimentos_maps);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);


        buscaContatosCelular();
        preencherEquipamentos();
        preencherLocacoes();

        for (LocacoesClass objto: listaLocacoes){
            System.out.println("Erro na linha"+objto.getLng());
        }

        System.out.println("Erro na linha"+listaLocacoes.size());
    }




    public void buscaContatosCelular(){
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");

        while (phones.moveToNext())
        {
            String  ids=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
           //pegar o id do cliente no telefone e adicionar na lista para comparação
           listaIdCliente.add(ids);

            //adicionar uma lista de contatos para exibir
           listaNomesContatos.add(name);
        }
        phones.close();
    }

    public void preencherEquipamentos(){
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        myRef.child("equipamentos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String key = myRef.child("equipamentos").push().getKey();


                for (DataSnapshot equipamentoData: dataSnapshot.getChildren()){

                    EquipamentoClass objetoEquipamento = equipamentoData.getValue(EquipamentoClass.class);
                    boolean verificaLocacao = Boolean.parseBoolean(objetoEquipamento.getStatus());
                    if( objetoEquipamento.getStatus().equals("true")){
                        listaEquipamentosNome.add(objetoEquipamento.getNome());
                        listaIdEquipamentos.add(equipamentoData.getKey());
                    }

                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void preencherLocacoes(){
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        myRef.child("locacoes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String key = myRef.child("locacoes").push().getKey();

                listaLocacoes = new ArrayList<>();  
                for (DataSnapshot locacoesData: dataSnapshot.getChildren()){
                    LocacoesClass objLocacao = locacoesData.getValue(LocacoesClass.class);
                    listaLocacoes.add(objLocacao);


                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {



        double lat, lng;
//        lat= Double.parseDouble(listaLocacoes.get(1).getLng());
//        lng =Double.parseDouble(listaLocacoes.get(1).getLat());
//        googleMap.addMarker(new MarkerOptions().position(new LatLng(-lat, -lng)).title("Jardim Botânico"));

        if (ContextCompat.checkSelfPermission(Vencimento.this, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
