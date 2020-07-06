package br.com.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import Classes.EquipamentoClass;

public class Equipamento extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference myRef ;
    private EquipamentoClass objetoEquipamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipamento);


    }

    public void store(View view){

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        TextView codigoEquiapento = (TextView) findViewById(R.id.editCodigoEquipamento);
        TextView nomeEquipamento = (TextView) findViewById(R.id.editNomeEquipamento);
        if(codigoEquiapento ==null  && nomeEquipamento==null ){
            Toast.makeText(this, "Existem campos sem preenchimento", Toast.LENGTH_SHORT).show();
        }else{
            save(nomeEquipamento.getText().toString(), codigoEquiapento.getText().toString());
            codigoEquiapento.setText("");
            nomeEquipamento.setText("");

            Toast.makeText(this, "Salvo com Sucesso", Toast.LENGTH_SHORT).show();

        }

    }
    public void save(String nome, String codigo){
        try {
            String key = myRef.child("equipamentos").push().getKey();
            objetoEquipamento = new EquipamentoClass();
            objetoEquipamento.setCodigo(codigo);
            objetoEquipamento.setNome(nome);
            objetoEquipamento.setStatus("false");
           myRef.child("equipamentos").child(key).setValue(objetoEquipamento);
        }catch (Exception e){
            Toast.makeText(this, "Erro", Toast.LENGTH_SHORT).show();
        }
    }

}
