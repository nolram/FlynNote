package com.lab11.nolram.database.controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import com.lab11.nolram.database.Database;
import com.lab11.nolram.database.model.Folha;
import com.lab11.nolram.database.model.Tag;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nolram on 24/08/15.
 */
public class FolhaDataSource {
    private SQLiteDatabase database;
    private Database dbHelper;
    private String[] allColumnsFolha = { Database.FOLHA_ID,
            Database.FOLHA_LOCAL_IMAGEM, Database.FOLHA_FK_CADERNO, Database.FOLHA_DATA,
            Database.FOLHA_TITULO};
    private String[] allColumnsTag = { Database.TAG_ID, Database.TAG_TAG, Database.TAG_MIN_TAG };
    private String[] allTagsFolhas = {Database.TAG_DA_FOLHA_ID, Database.TAG_DA_FOLHA_ID_FOLHA, Database.TAG_DA_FOLHA_ID_TAG};

    public FolhaDataSource(Context context) {
        dbHelper = new Database(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Folha criarFolhaERetornar(String local_imagem, long fk_caderno, String titulo){
        DateTime now = new DateTime();
        ContentValues values = new ContentValues();
        values.put(Database.FOLHA_LOCAL_IMAGEM, local_imagem);
        values.put(Database.FOLHA_FK_CADERNO, fk_caderno);
        values.put(Database.FOLHA_DATA, now.toString());
        values.put(Database.FOLHA_TITULO, titulo);
        long dbInsert = database.insert(Database.TABLE_FOLHA, null, values);
        Cursor cursor = database.query(Database.TABLE_FOLHA, allColumnsFolha, Database.FOLHA_ID + " = " + dbInsert,
                null, null, null, null);
        cursor.moveToFirst();
        Folha folha = cursorToFolha(cursor);
        cursor.close();
        return folha;
    }

    public void criarFolha(String local_imagem, long fk_caderno, String titulo, String[] tags){
        DateTime now = new DateTime();
        ContentValues values = new ContentValues();
        values.put(Database.FOLHA_LOCAL_IMAGEM, local_imagem);
        values.put(Database.FOLHA_FK_CADERNO, fk_caderno);
        values.put(Database.FOLHA_DATA, now.toString());
        values.put(Database.FOLHA_TITULO, titulo);
        long dbInsertFolha = database.insert(Database.TABLE_FOLHA, null, values);
        insertTags(tags, dbInsertFolha);
    }

    private void insertTags(String[] tags, long dbInsertFolha) {
        ContentValues values, values1, values2;
        Tag tag;
        long dbInsertTag;
        if(tags != null){
            //Log.d("tamanho", String.valueOf(tags.length));
            for (int i=0; i < tags.length; i++) {
                tag = getTag(tags[i].toLowerCase());
                if(tag == null) {

                    values = new ContentValues();
                    values.put(Database.TAG_TAG, tags[i]);
                    values.put(Database.TAG_MIN_TAG, tags[i].toLowerCase());
                    dbInsertTag = database.insert(Database.TABLE_TAG, null, values);

                    values1 = new ContentValues();
                    values1.put(Database.TAG_DA_FOLHA_ID_FOLHA, dbInsertFolha);
                    values1.put(Database.TAG_DA_FOLHA_ID_TAG, dbInsertTag);
                    database.insert(Database.TABLE_TAG_DA_FOLHA, null, values1);

                }else {

                    values2 = new ContentValues();
                    values2.put(Database.TAG_DA_FOLHA_ID_FOLHA, dbInsertFolha);
                    values2.put(Database.TAG_DA_FOLHA_ID_TAG, tag.getId());
                    database.insert(Database.TABLE_TAG_DA_FOLHA, null, values2);

                }

            }
        }
    }

    public Tag getTag(String min_tag){
        Tag tag = null;
        Cursor cursor = database.query(Database.TABLE_TAG,
                allColumnsTag, Database.TAG_MIN_TAG + " = '" + min_tag+"'", null, null, null, null);
        if(cursor != null && cursor.moveToFirst()){
            tag = cursorToTag(cursor);
        }
        cursor.close();
        return tag;
    }

    public List<Folha> getAllFolhas(long fk_caderno) {
        List<Folha> folhas = new ArrayList<Folha>();
        //log();
        Cursor cursor = database.query(Database.TABLE_FOLHA, allColumnsFolha,
                Database.FOLHA_FK_CADERNO + " = " + fk_caderno, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Folha folha = cursorToFolha(cursor);
            folhas.add(folha);
            cursor.moveToNext();
        }
        cursor.close();
        return folhas;
    }

    public List<Tag> getAllTags(long fk_folha){
        List<Tag> tags = new ArrayList<>();
        final String QUERY = "SELECT t."+Database.TAG_ID+", t."+Database.TAG_TAG+
                ", t."+Database.TAG_MIN_TAG+", tt."+Database.TAG_DA_FOLHA_ID+", tt."+Database.TAG_DA_FOLHA_ID_TAG+
                " FROM "+Database.TABLE_TAG+" t INNER JOIN "+
                Database.TABLE_TAG_DA_FOLHA+" tt ON tt."+Database.TAG_DA_FOLHA_ID_FOLHA+" = ?"+
                " WHERE t."+Database.TAG_ID+"=tt."+Database.TAG_DA_FOLHA_ID_TAG;
        //Log.d("query", QUERY);
        Cursor cursor = database.rawQuery(QUERY, new String[]{String.valueOf(fk_folha)});
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Tag tag = cursorToTag(cursor);
            tags.add(tag);
            cursor.moveToNext();
        }
        cursor.close();
        return tags;
    }

    public void deleteFolha(Folha folha) {
        long id = folha.getId();
        System.out.println("Comment deleted with id: " + id);
        database.delete(Database.TABLE_FOLHA, Database.FOLHA_ID+ " = " + id, null);
    }

    public void log(){
        Cursor cursor = database.query(Database.TABLE_TAG_DA_FOLHA, allTagsFolhas, null, null, null, null, null);
        while (cursor.moveToNext()){
            Log.d(Database.TABLE_TAG_DA_FOLHA, Database.TABLE_TAG_DA_FOLHA);
            Log.d(Database.TAG_DA_FOLHA_ID, String.valueOf(cursor.getLong(0)));
            Log.d(Database.TAG_DA_FOLHA_ID_FOLHA, String.valueOf(cursor.getLong(1)));
            Log.d(Database.TAG_DA_FOLHA_ID_TAG, String.valueOf(cursor.getLong(2)));
        }
        cursor.close();
    }

    private Folha cursorToFolha(Cursor cursor) {
        Folha folha = new Folha();
        folha.setId(cursor.getLong(0));
        folha.setLocal_folha(cursor.getString(1));
        folha.setFk_caderno(cursor.getLong(2));
        folha.setData_adicionado(cursor.getString(3));
        folha.setTitulo(cursor.getString(4));
        folha.setTags(getAllTags(folha.getId()));
        return folha;
    }

    private Tag cursorToTag(Cursor cursor){
        Tag tag = new Tag();
        tag.setId(cursor.getLong(0));
        tag.setTag(cursor.getString(1));
        tag.setTagMin(cursor.getString(2));
        return tag;
    }
}