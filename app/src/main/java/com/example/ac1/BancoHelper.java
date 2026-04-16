package com.example.ac1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class BancoHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME    = "Ac1.db";
    private static final int    DATABASE_VERSION = 2;

    private static final String TABLE_NAME        = "tarefas";
    private static final String COLUMN_ID         = "id";
    private static final String COLUMN_DESCRICAO     = "descricao";
    private static final String COLUMN_CATEGORIA    = "categoria";
    private static final String COLUMN_VALOR        = "valor";
    private static final String COLUMN_DATA         = "data";
    private static final String COLUMN_PAGAMENTO = "forma_pagamento";
    private static final String COLUMN_PAGO  = "foi_pago";

    public BancoHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate (SQLiteDatabase db) {
    String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "( "
            + COLUMN_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_DESCRICAO   + " TEXT, "
            + COLUMN_CATEGORIA + " TEXT, "
            + COLUMN_VALOR      + " TEXT, "
            + COLUMN_DATA       + " TEXT, "
            + COLUMN_PAGAMENTO + " TEXT, "
            + COLUMN_PAGO  + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    public long inserirDespesa(String descricao, String categoria, Double valor, String data,
                               String formaPagamento, boolean foiPago) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DESCRICAO,   descricao);
        values.put(COLUMN_CATEGORIA, categoria);
        values.put(COLUMN_VALOR,      valor);
        values.put(COLUMN_DATA,    data);
        values.put(COLUMN_PAGAMENTO, formaPagamento);
        values.put(COLUMN_PAGO,  foiPago ? 1 : 0);
        return db.insert(TABLE_NAME, null, values);
    }
    public int atualizarDespesa(int id, String descricao, String categoria, Double valor, String data,
                              String formaPagamento, boolean foiPago) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DESCRICAO,   descricao);
        values.put(COLUMN_CATEGORIA, categoria);
        values.put(COLUMN_VALOR,      valor);
        values.put(COLUMN_DATA,    data);
        values.put(COLUMN_PAGAMENTO, formaPagamento);
        values.put(COLUMN_PAGO,  foiPago ? 1 : 0);
        return db.update(TABLE_NAME, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
    }
    public int excluirDespesa(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
    }
    public Cursor buscarDespesas(String categoria, boolean soPagas, String ordem) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        ArrayList<String> condicoes = new ArrayList<>();

        if (categoria != null && !categoria.equals("Todas")) {
            condicoes.add(COLUMN_CATEGORIA + " = '" + categoria + "'");
        }
        if (soPagas) {
            condicoes.add(COLUMN_PAGO + " = 1");
        }
        if (!condicoes.isEmpty()) {
            query += " WHERE " + condicoes.get(0);
            for (int i = 1; i < condicoes.size(); i++) {
                query += " AND " + condicoes.get(i);
            }
        }
        if (ordem.equals("Valor")) {
            query += " ORDER BY " + COLUMN_VALOR + " ASC";
        } else if (ordem.equals("Data")) {
            query += " ORDER BY " + COLUMN_DATA + " ASC";
        }

        return db.rawQuery(query, null);
    }
    public Cursor buscarDespesaPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};
        return db.query(TABLE_NAME, null, selection,
                selectionArgs, null, null, null);
    }
}

