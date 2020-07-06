package br.com.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import Classes.Cliente;
import Classes.LocacoesClass;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private static final int MY_LOCALIZACAO = 0;
    FusedLocationProviderClient client;
    AdressResultRecivier resultReciver;
    List<LocacoesClass> listaLocacoes = new ArrayList<>();
    List<LocacoesClass> listaLocacoesAtuais = new ArrayList<>();
    ArrayAdapter<String> adapterSpiner;
   private  FirebaseDatabase database;
    private DatabaseReference myRef ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


      if(isOnline()){
          SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map3);
          mapFragment.getMapAsync(this);
// Here, thisActivity is the current activity
          if (ContextCompat.checkSelfPermission(this,
                  Manifest.permission.READ_CONTACTS)
                  != PackageManager.PERMISSION_GRANTED) {

              // Permission is not granted
              // Should we show an explanation?
              if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                      Manifest.permission.READ_CONTACTS) ) {
                  // Show an explanation to the user *asynchronously* -- don't block
                  // this thread waiting for the user's response! After the user
                  // sees the explanation, try again to request the permission.
              } else {
                  // No explanation needed; request the permission
                  ActivityCompat.requestPermissions(this,  new String[]{Manifest.permission.READ_CONTACTS},  MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                  // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                  // app-defined int constant. The callback method gets the
                  // result of the request.
              }
          } else {
              // Permission has already been granted
          }

// Here, thisActivity is the current activity
          if (ContextCompat.checkSelfPermission(this,
                  Manifest.permission.ACCESS_FINE_LOCATION)
                  != PackageManager.PERMISSION_GRANTED) {

              // Permission is not granted
              // Should we show an explanation?
              if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                      Manifest.permission.ACCESS_FINE_LOCATION) ) {
                  // Show an explanation to the user *asynchronously* -- don't block
                  // this thread waiting for the user's response! After the user
                  // sees the explanation, try again to request the permission.
              } else {
                  // No explanation needed; request the permission
                  ActivityCompat.requestPermissions(this,  new String[]{Manifest.permission.ACCESS_FINE_LOCATION},  MY_LOCALIZACAO);

                  // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                  // app-defined int constant. The callback method gets the
                  // result of the request.
              }
          } else {
              // Permission has already been granted
          }
      }else{
          Intent intent = new Intent(this, SemInternet.class);
          startActivity(intent);
          finish();
      }

    }
    public void proximaTela(View view){

        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
    public  void  telaVecimentos(View view){
        Intent intent = new Intent(this, Main2Activity.class);
        startActivity(intent);
    }
    public void equipamento(View view){

        Intent intents= new Intent(this, Equipamento.class);
        startActivity(intents);
    }

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
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(objetos.getLat()), Double.parseDouble(objetos.getLng())), 13));
                                listaLocacoesAtuais.add(objetos);


                            }
                        }
                    }
                }

                if(listaLocacoesAtuais.size()==0){
                    Toast.makeText(MainActivity.this, "Não possuem Vencimentos para data de "+formattedDate, Toast.LENGTH_SHORT).show();

                }


                phones.close();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

        }

    }
    @Override
    protected void onResume() {
        final Geocoder geocoder;
        geocoder = new Geocoder(this, Locale.getDefault());
        super.onResume();
        //essa parada aquui é pra verificar se o google play service esta atualizado
        int errorCode= GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        switch (errorCode){
            case ConnectionResult.SERVICE_MISSING:
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
            case ConnectionResult.SERVICE_DISABLED:
             Log.d("TEste","ShowDialog");
             GoogleApiAvailability.getInstance().getErrorDialog(this, errorCode,
                     0, new DialogInterface.OnCancelListener() {
                         @Override
                         public void onCancel(DialogInterface dialog) {
                             finish();
                         }
             }).show();
            break;
            case ConnectionResult.SUCCESS:
                Log.d("teste", "Google Play Atualizado service");
                break;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
                try {
            if (client !=null){
                //ate aqui daqui pra baixo a gente fala de mapa
                client.getLastLocation()
                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                List<Address> addresses = null;

                                if(location != null){
                                    //TextView endereco = (TextView) findViewById(R.id.endereco);

                                    try {
                                        addresses = geocoder.getFromLocation(location.getLatitude(),  location.getLongitude(), 1);
                                        String address = addresses.get(0).getAddressLine(0);
                                        //    endereco.setText(address.toString());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    String address = addresses.get(0).getAddressLine(0);
                                    Log.i("MAIN ACTIVTY LOCATION ",  location.getLatitude() + " - " + location.getLongitude());
                                }else{
                                    Log.i("ERRO 88",  "null");
                                }

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
            }else{
               // Toast.makeText(this, "Atenção verifique sua conexão com a internet!!!", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){

        }

        //ate aqui
//        LocationRequest locationRequest = LocationRequest.create();
//
//        locationRequest.setInterval(15 * 1000);
//        locationRequest.setFastestInterval(5 * 1000);
//        locationRequest.setPriority(locationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
//                .addLocationRequest(locationRequest);
//
//        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
//        settingsClient.checkLocationSettings(builder.build())
//                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
//                    @Override
//                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
//                        Log.i("teste" ,
//                                locationSettingsResponse.getLocationSettingsStates().isNetworkLocationPresent()+"");
//                }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        if (e instanceof ResolvableApiException){
//                            try {
//                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
//                                resolvableApiException.startResolutionForResult(MainActivity.this, 10);
//
//                            } catch (IntentSender.SendIntentException ex) {
//                                ex.printStackTrace();
//                            }
//                        }
//                    }
//                });
        LocationCallback locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if(locationResult==null){
                    Log.i("teste2", " local is null");
                 return;
                }
                for (Location location: locationResult.getLocations()){
                    Log.i("teste2", location.getLatitude()+"");
                    if (!Geocoder.isPresent()){
                        return;
                    }
                    startIntentService(location);
                }
            }



            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
              Log.i("teste", locationAvailability.isLocationAvailable()+"");
            }
        };
          //      client.requestLocationUpdates(locationRequest, locationCallback, null);
    }
    private void startIntentService(Location location) {
       Intent intent= new Intent(this, FetchAddressService.class);
       intent.putExtra(Constants.RECEIVER, resultReciver);
       intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
       startService(intent);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private  class AdressResultRecivier extends ResultReceiver{

        public AdressResultRecivier(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if(resultData == null)return;
            final String addressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            if (addressOutput != null){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, addressOutput, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    public boolean isOnline() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return manager.getActiveNetworkInfo() != null &&
                manager.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
