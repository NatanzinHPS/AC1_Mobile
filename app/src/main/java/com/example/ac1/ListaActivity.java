package com.example.ac1;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ListaActivity extends AppCompatActivity{

    Spinner spinnerFiltroCategoria, spinnerOrdem;
    CheckBox checkSoPagas;
    ListView listViewDespesas;

    BancoHelper bancoHelper;
    ArrayAdapter<String> adapter;
    ArrayList<String> listaDespesa;
    ArrayList<Integer>   listaIds;

    String[] categorias = {"Todas", "Alimentação", "Transporte", "Lazer", "Educação", "Saúde", "Outros"};
    String[] ordens      = {"Sem ordenação", "Valor", "Data"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);
        setTitle("Despesas Cadastradas");

        try {
            spinnerFiltroCategoria = findViewById(R.id.spinnerFiltroCategoria);
            spinnerOrdem          = findViewById(R.id.spinnerOrdem);
            checkSoPagas          = findViewById(R.id.checkSoPagas);
            listViewDespesas      = findViewById(R.id.listViewDespesas);

            bancoHelper = new BancoHelper(this);

            spinnerFiltroCategoria.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item, categorias));

            spinnerOrdem.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item, ordens));

            carregarDespesas();

            AdapterView.OnItemSelectedListener recarregar =
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> p,
                                                   android.view.View v, int pos, long id) {
                            carregarDespesas();
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> p) {}
                    };

            spinnerFiltroCategoria.setOnItemSelectedListener(recarregar);
            spinnerOrdem.setOnItemSelectedListener(recarregar);
            checkSoPagas.setOnCheckedChangeListener((buttonView, isChecked) -> carregarDespesas());

            listViewDespesas.setOnItemClickListener((parent, view, position, id) -> {
                int idDespesa = listaIds.get(position);
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("id", idDespesa);
                startActivity(intent);
            });
            listViewDespesas.setOnItemLongClickListener((parent, view, position, id) -> {
                int idDespesa = listaIds.get(position);
                String descricao = listaDespesa.get(position).split(" \\| ")[0];
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Excluir Despesa")
                        .setMessage("Deseja excluir a despesa \"" + descricao + "\"?")
                        .setPositiveButton("Sim", (dialog, which) -> {
                            bancoHelper.excluirDespesa(idDespesa);
                            Toast.makeText(this, "Despesa excluída", Toast.LENGTH_SHORT).show();
                            carregarDespesas();
                        })
                        .setNegativeButton("Não", null)
                        .show();
                return true;
            });
        } catch (Exception e) {
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        carregarDespesas();
    }
    private void carregarDespesas() {
        String categoriaFiltro = spinnerFiltroCategoria.getSelectedItem().toString();
        String ordem = spinnerOrdem.getSelectedItem().toString();
        boolean soPagas = checkSoPagas.isChecked();

        Cursor cursor = bancoHelper.buscarDespesas(categoriaFiltro, soPagas, ordem);
        listaDespesa = new ArrayList<>();
        listaIds = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String descricao = cursor.getString(1);
                String categoria = cursor.getString(2);
                double valor = cursor.getDouble(3);
                String formaPagamento = cursor.getString(4);
                int foiPago = cursor.getInt(5);

                String status = (foiPago == 1) ? "Pago" : "Pendente";
                String item = descricao + " | " + categoria + " | R$ " + valor + " | " + formaPagamento + " | " + status;
                listaDespesa.add(item);
                listaIds.add(id);
            } while (cursor.moveToNext());
            cursor.close();

            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaDespesa);
            listViewDespesas.setAdapter(adapter);
        }
    }
}
