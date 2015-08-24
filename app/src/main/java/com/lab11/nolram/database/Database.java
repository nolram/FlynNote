package com.lab11.nolram.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by nolram on 21/08/15.
 */
public class Database extends SQLiteOpenHelper {
    private static final String BANCO_DADOS = "CadernoDatabase.db";
    private static int VERSAO = 1;

    public static final String TABLE_CADERNO = "caderno";
    public static final String CADERNO_ID = "_id";
    public static final String CADERNO_TITULO = "titulo";
    public static final String CADERNO_BADGE = "badge";
    public static final String CADERNO_DESCRICAO = "descricao";
    public static final String CADERNO_DATA = "data_adicionado";
    public static final String CADERNO_ULTIMA_MODIFICACAO = "ultima_modificacao";


    public static final String TABLE_FOLHA = "folha";
    public static final String FOLHA_ID = "_id";
    public static final String FOLHA_LOCAL_IMAGEM = "local_folha";
    public static final String FOLHA_DATA = "data_adicionado";
    public static final String FOLHA_FK_CADERNO = "fk_caderno";

    public static final String TABLE_TAG = "tag";
    public static final String TAG_ID = "_id";
    public static final String TAG_TAG = "tag";
    public static final String TAG_MIN_TAG = "min_tag";
    public static final String TAG_DATA = "data_adicionado";

    public static final String TABLE_TAG_DA_FOLHA = "tag_da_folha";
    public static final String TAG_DA_FOLHA_ID_TAG = "id_tag";
    public static final String TAG_DA_FOLHA_ID_FOLHA = "id_folha";

    private static final String CREATE_CADERNO = "CREATE TABLE "+TABLE_CADERNO+"("+CADERNO_ID+" INTEGER PRIMARY KEY," +
            CADERNO_TITULO+" TEXT,"+CADERNO_BADGE+" TEXT NULL,"+CADERNO_DESCRICAO+" TEXT NULL,"+CADERNO_DATA+" DATE," +
            ""+CADERNO_ULTIMA_MODIFICACAO+" DATE);";

    private static final String CREATE_FOLHA = "CREATE TABLE "+TABLE_FOLHA+"("+FOLHA_ID+" INTEGER PRIMARY KEY, " +
            FOLHA_LOCAL_IMAGEM+" TEXT, "+FOLHA_DATA+" DATE, "+FOLHA_FK_CADERNO+" INTEGER, " +
            "FOREIGN KEY ("+FOLHA_FK_CADERNO+") REFERENCES "+TABLE_CADERNO+"("+CADERNO_ID+"));";

    private static final String CREATE_TAG = "CREATE TABLE "+TABLE_TAG+"("+TAG_ID+" INTEGER PRIMARY KEY, "+
            TAG_TAG+" TEXT, "+TAG_MIN_TAG+" TEXT, "+TAG_DATA+" DATE);";

    private static final String CREATE_TAG_DA_FOLHA = "CREATE TABLE "+TABLE_TAG_DA_FOLHA+
            "("+TAG_DA_FOLHA_ID_TAG+" INTEGER, "+TAG_DA_FOLHA_ID_FOLHA+" INTEGER, " +
            "FOREIGN KEY ("+TAG_DA_FOLHA_ID_FOLHA+") REFERENCES "+TABLE_FOLHA+"("+FOLHA_ID+")," +
            "FOREIGN KEY ("+TAG_DA_FOLHA_ID_TAG+") REFERENCES "+TABLE_TAG+"("+TAG_ID+
            "), PRIMARY KEY("+TAG_DA_FOLHA_ID_TAG+","+TAG_DA_FOLHA_ID_FOLHA+"));";


    public Database(Context context) {
        super(context, BANCO_DADOS, null, VERSAO);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CADERNO);

        db.execSQL(CREATE_FOLHA);

        db.execSQL(CREATE_TAG);

        db.execSQL(CREATE_TAG_DA_FOLHA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(Database.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CADERNO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOLHA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAG_DA_FOLHA);
        onCreate(db);
    }
}
