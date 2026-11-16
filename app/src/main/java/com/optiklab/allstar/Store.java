package com.optiklab.allstar;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public final class Store extends SQLiteOpenHelper {
    Store(Context c){super(c,"allstar_progress.db",null,1);}
    @Override public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE attempts(id INTEGER PRIMARY KEY AUTOINCREMENT,test_id TEXT NOT NULL,ts INTEGER NOT NULL,choices TEXT NOT NULL,correct INTEGER NOT NULL,wrong INTEGER NOT NULL,blank INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE qrmap(qr TEXT PRIMARY KEY,test_id TEXT NOT NULL)");
        db.execSQL("CREATE TABLE solutions(test_id TEXT NOT NULL,q_no INTEGER NOT NULL,image_path TEXT,solution TEXT,PRIMARY KEY(test_id,q_no))");
    }
    @Override public void onUpgrade(SQLiteDatabase db,int old,int now){}
    void mapQr(String qr,String testId){android.content.ContentValues v=new android.content.ContentValues();v.put("qr",qr);v.put("test_id",testId);getWritableDatabase().insertWithOnConflict("qrmap",null,v,SQLiteDatabase.CONFLICT_REPLACE);}
    String qrTest(String qr){try(Cursor c=getReadableDatabase().rawQuery("SELECT test_id FROM qrmap WHERE qr=?",new String[]{qr})){return c.moveToFirst()?c.getString(0):null;}}
    static class Attempt {long id,ts;String testId;String[] choices;int correct,wrong,blank;}
    static String[] parse(String choices){try{JSONArray a=new JSONArray(choices);String[] result=new String[a.length()];for(int i=0;i<a.length();i++){result[i]=a.isNull(i)?null:a.getString(i);}return result;}catch(Exception e){return new String[0];}}
    long save(String testId,String[] choices,List<BookDb.Answer> keys){
        int good=0,bad=0,empty=0;JSONArray arr=new JSONArray();for(int i=0;i<keys.size();i++){
            String a=i<choices.length?choices[i]:null;arr.put(a==null?JSONObjectNull.NULL:a);
            if(a==null)empty++;else if(a.equals(keys.get(i).correct))good++;else bad++;
        }
        android.content.ContentValues v=new android.content.ContentValues();v.put("test_id",testId);v.put("ts",System.currentTimeMillis());v.put("choices",arr.toString());v.put("correct",good);v.put("wrong",bad);v.put("blank",empty);return getWritableDatabase().insert("attempts",null,v);
    }
    private Attempt from(Cursor c){Attempt a=new Attempt();a.id=c.getLong(0);a.testId=c.getString(1);a.ts=c.getLong(2);a.choices=parse(c.getString(3));a.correct=c.getInt(4);a.wrong=c.getInt(5);a.blank=c.getInt(6);return a;}
    Attempt attempt(long id){try(Cursor c=getReadableDatabase().rawQuery("SELECT id,test_id,ts,choices,correct,wrong,blank FROM attempts WHERE id=?",new String[]{String.valueOf(id)})){return c.moveToFirst()?from(c):null;}}
    List<Attempt> recent(int limit){List<Attempt> list=new ArrayList<>();try(Cursor c=getReadableDatabase().rawQuery("SELECT id,test_id,ts,choices,correct,wrong,blank FROM attempts ORDER BY ts DESC LIMIT "+Math.min(Math.max(limit,1),100),null)){while(c.moveToNext())list.add(from(c));}return list;}
    int[] summary(){try(Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*),COALESCE(SUM(correct),0),COALESCE(SUM(wrong),0),COALESCE(SUM(blank),0) FROM attempts",null)){c.moveToFirst();return new int[]{c.getInt(0),c.getInt(1),c.getInt(2),c.getInt(3)};}}
    void savePhoto(String testId,int q,String path){android.content.ContentValues v=new android.content.ContentValues();v.put("test_id",testId);v.put("q_no",q);v.put("image_path",path);getWritableDatabase().insertWithOnConflict("solutions",null,v,SQLiteDatabase.CONFLICT_IGNORE);getWritableDatabase().update("solutions",v,"test_id=? AND q_no=?",new String[]{testId,String.valueOf(q)});
        android.content.ContentValues reset=new android.content.ContentValues();reset.putNull("solution");getWritableDatabase().update("solutions",reset,"test_id=? AND q_no=?",new String[]{testId,String.valueOf(q)});
    }
    void saveSolution(String testId,int q,String text){android.content.ContentValues v=new android.content.ContentValues();v.put("test_id",testId);v.put("q_no",q);v.put("solution",text);getWritableDatabase().insertWithOnConflict("solutions",null,v,SQLiteDatabase.CONFLICT_IGNORE);getWritableDatabase().update("solutions",v,"test_id=? AND q_no=?",new String[]{testId,String.valueOf(q)});}
    String[] getSolution(String testId,int q){try(Cursor c=getReadableDatabase().rawQuery("SELECT image_path,solution FROM solutions WHERE test_id=? AND q_no=?",new String[]{testId,String.valueOf(q)})){return c.moveToFirst()?new String[]{c.getString(0),c.getString(1)}:new String[]{null,null};}}
    static class JSONObjectNull {static final Object NULL=org.json.JSONObject.NULL;}
}
