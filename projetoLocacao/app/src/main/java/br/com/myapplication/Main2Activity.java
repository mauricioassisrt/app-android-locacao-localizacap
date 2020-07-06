package br.com.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import Classes.Cliente;
import Classes.EquipamentoClass;
import Classes.LocacoesClass;

public class Main2Activity extends AppCompatActivity implements OnMapReadyCallback {



    private FirebaseDatabase database;
    private DatabaseReference myRef ;
    List<LocacoesClass> listaLocacoes = new ArrayList<>();

    List<LocacoesClass> listaLocacoesAtuais = new ArrayList<>();
    Spinner s, equiapmentos;
    List<String> listaIdCliente = new ArrayList<>();
    List<EquipamentoClass> listaEquipamento = new ArrayList<>();
   private List<String> listaNomeEquipamento = new ArrayList<>();
    private  String address, idcliente, idEquipamento;
    ArrayList<String> listaIdEquipamento = new ArrayList<>();
    ArrayAdapter<String> arrayAdpter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);
        preencherEquipamentos();

        //spiner de equipamentos
        equiapmentos = (Spinner) findViewById(R.id.spinner3);
        //pega o id spiner e busca na lista de id do equipamento
        equiapmentos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(listaIdEquipamento.size()!=0){
                    Long item = parent.getItemIdAtPosition(position);
                    idEquipamento = listaIdEquipamento.get(position);
                }else{

                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
                    if( objetoEquipamento.getStatus().equals("true")){
                        arrayAdpter.add(objetoEquipamento.getNome());
                        equiapmentos.setAdapter(arrayAdpter);
                        listaIdEquipamento.add(equipamentoData.getKey());
                    }

                }
                if(listaIdEquipamento.size()==0){

                    arrayAdpter.add("Nâo há equipamentos!");
                    equiapmentos.setAdapter(arrayAdpter);
                    listaIdEquipamento = new ArrayList<>();
                    Toast.makeText(Main2Activity.this, "Não há equipamentos para finalziar", Toast.LENGTH_LONG).show();
                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    @Override
    public void onMapReady(final GoogleMap googleMap) {

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        myRef.child("locacoes").addValueEventListener(new ValueEventListener() {



            List<Cliente> listaCliente = new ArrayList<>();
            Cliente objetoCliente= new Cliente();
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot locacoesData: dataSnapshot.getChildren()){

                    LocacoesClass objLocacao = locacoesData.getValue(LocacoesClass.class);
                    listaLocacoes.add(objLocacao);
                    System.out.println("NA DATA "+objLocacao.getValor().toString());

                }
                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");
                Cliente objeto = new Cliente();
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                String formattedDate = df.format(c.getTime());
                System.out.println("Format dateTime => " + formattedDate);

                while (phones.moveToNext())
                {

                    String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String  ids=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));

                    for (LocacoesClass objetos: listaLocacoes){
                        System.out.println("DATA"+objetos.getVencimento());
                        if(ids.equals(objetos.getClienteid())){
                            if(formattedDate.equals(objetos.getVencimento())){
                                googleMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(objetos.getLat()), Double.parseDouble(objetos.getLng()))).title("Cliente:"+name+" Valor da Locação:R$ "+objetos.getValor()+",00"));
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(objetos.getLat()), Double.parseDouble(objetos.getLng())), 16));
                                listaLocacoesAtuais.add(objetos);


                            }
                            }
                        }
                    }

                if(listaLocacoesAtuais.size()==0){
                    Toast.makeText(Main2Activity.this, "Não possuem Vencimentos para data de "+formattedDate, Toast.LENGTH_SHORT).show();

                }


                phones.close();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        if (ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    public void finalizaLocacao(View view){
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();


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
                        objetoEquipamento.setStatus("false");
                        myRef.child("equipamentos").child(idEquipamento).setValue(objetoEquipamento);
                        Toast.makeText(Main2Activity.this, "Locação Finalizada com sucesso!", Toast.LENGTH_SHORT).show();
                        System.out.println("FOR");
                        preencherEquipamentos();
                        System.exit(0);
                        break;

                    }

                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
