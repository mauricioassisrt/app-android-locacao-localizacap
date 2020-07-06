package br.com.myapplication;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FetchAddressService extends IntentService {
    protected ResultReceiver resultReceiver;
    public FetchAddressService() {
        super("fetchAddressService");

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent ==null)return;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

       Location location= intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);
        resultReceiver = intent.getParcelableExtra(Constants.RECEIVER);
        List<Address> addresses = null;

        try {
           addresses= geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            Log.e("teste", "Servico indisponivel");
        }catch (IllegalArgumentException e){
            Log.e("teste", "latitude e longitude invalidas", e);
        }
        if(addresses == null|| addresses.isEmpty()){
            Log.e("teste", "Nenhum endereço encontrado");
            deliverResultToReciver(Constants.FAILURE_RESULT, "nenhum endereço encontrado");
        }else{
            Address address = addresses.get(0);
            List<String> addressF = new ArrayList<String>();

            for (int i =0; i <= address.getMaxAddressLineIndex(); i++){
                addressF.add(address.getAddressLine(i));
            }
            deliverResultToReciver(Constants.SUCCESS_RESULT, TextUtils.join("|", addressF));
        }

    }
    private void deliverResultToReciver(int resultCode, String message){
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        resultReceiver.send(resultCode, bundle);
    }
}

