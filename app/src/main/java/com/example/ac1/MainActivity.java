package com.example.ac1;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    EditText edtDescricao, edtData, edtValor;
    Spinner  spinnerCategoria, spinnerPagamento;
    CheckBox checkPago;
    Button btnSalvar, btnIrParaLista;

    BancoHelper bancoHelper;

    int idEmEdicao = -1;

    String[] categorias = {"Alimentação", "Transporte", "Lazer", "Educação", "Saúde", "Outros"};
    String[] pagamentos  = {"Dinheiro", "Cartão de Crédito", "Cartão de Débito", "Pix", "Transferência", "Outros"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        try {
            edtDescricao      = findViewById(R.id.edtDescricao);
            edtData           = findViewById(R.id.edtData);
            edtValor          = findViewById(R.id.edtValor);
            spinnerCategoria  = findViewById(R.id.spinnerCategoria);
            spinnerPagamento  = findViewById(R.id.spinnerPagamento);
            checkPago         = findViewById(R.id.checkPago);
            btnSalvar         = findViewById(R.id.btnSalvar);
            btnIrParaLista    = findViewById(R.id.btnIrParaLista);

            bancoHelper = new BancoHelper(this);

            spinnerCategoria.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item, categorias));

            spinnerPagamento.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item, pagamentos));

            edtData.setFocusable(false);
            edtData.setClickable(true);
            edtData.setOnClickListener(v -> {
                        Calendar cal = Calendar.getInstance();
                        new DatePickerDialog(this,
                                (view, year, month, day) ->
                                        edtData.setText(String.format("%02d/%02d/%04d", day, month + 1, year)),
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                        ).show();
        });
            Intent intent = getIntent();
            if (intent.hasExtra("id")) {
                idEmEdicao = intent.getIntExtra("id", -1);
                carregarDadosParaEdicao(idEmEdicao);
                btnSalvar.setText("Atualizar");
                setTitle("Editar Despesa");
            } else {
                setTitle("Cadastrar Despesa");
            }

            btnSalvar.setOnClickListener(v -> {;
                String descricao = edtDescricao.getText().toString().trim();
                String data      = edtData.getText().toString().trim();
                String valorStr  = edtValor.getText().toString().trim();
                String categoria = spinnerCategoria.getSelectedItem().toString();
                String formaPagamento = spinnerPagamento.getSelectedItem().toString();
                boolean foiPago     = checkPago.isChecked();

                if (descricao.isEmpty() || data.isEmpty() || valorStr.isEmpty()) {
                    Toast.makeText(this, "Preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show();
                    return;
                }

                double valor;
                try {
                    valor = Double.parseDouble(valorStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (idEmEdicao == -1) {
                    long res = bancoHelper.inserirDespesa(descricao, categoria, valor, data,
                            formaPagamento, foiPago);
                    if (res != -1) {
                        Toast.makeText(this, "Despesa salva!", Toast.LENGTH_SHORT).show();
                        limparCampos();
                    } else {
                        Toast.makeText(this, "Erro ao salvar!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    int res = bancoHelper.atualizarDespesa(idEmEdicao, descricao, categoria, valor, data,
                            formaPagamento, foiPago);
                    if (res > 0) {
                        Toast.makeText(this, "Despesa atualizada!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Erro ao atualizar!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

                btnIrParaLista.setOnClickListener(v -> startActivity(new Intent(this, ListaActivity.class)));

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void carregarDadosParaEdicao(int id) {
        Cursor cursor = bancoHelper.buscarDespesaPorId(id);
        if (cursor.moveToFirst()) {
            edtDescricao.setText(cursor.getString(1));
            edtValor.setText(String.valueOf(cursor.getDouble(3)));
            edtData.setText(cursor.getString(4));
            checkPago.setChecked(cursor.getInt(6) == 1);
            String categoria = cursor.getString(2);
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerCategoria.getAdapter();
            int posCategoria = adapter.getPosition(categoria);
            spinnerCategoria.setSelection(posCategoria);
            String formaPagamento = cursor.getString(5);
            ArrayAdapter<String> adapterPagamento = (ArrayAdapter<String>) spinnerPagamento.getAdapter();
            int posPagamento = adapterPagamento.getPosition(formaPagamento);
            spinnerPagamento.setSelection(posPagamento);
        }
    }

    private void limparCampos() {
        edtDescricao.setText("");
        edtData.setText("");
        edtValor.setText("");
        spinnerCategoria.setSelection(0);
        spinnerPagamento.setSelection(0);
        checkPago.setChecked(false);
    }
}