package com.optiklab.allstar;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class BookDb {
    public static class Test {
        public String id, program, subject, topic, label, pdf; public int book, questions, review, page;
        public String summary(){return program+" • "+subject+" • "+(book>0?book+". Kitap":"Kitap bilgisi yok");}
    }
    public static class Answer {public int printedNumber;public String correct;}
    private final SQLiteDatabase db;
    BookDb(Context c){
        File file=new File(c.getFilesDir(),"allstar_answers_1.sqlite");
        if(!file.exists()){
            try(InputStream in=c.getAssets().open("answers.sqlite");FileOutputStream out=new FileOutputStream(file)){
                byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);
            }catch(Exception e){throw new IllegalStateException("Cevap anahtarı açılmadı: "+e.getMessage(),e);}
        }
        db=SQLiteDatabase.openDatabase(file.getAbsolutePath(),null,SQLiteDatabase.OPEN_READONLY);
    }
    private Test from(Cursor c){Test t=new Test();t.id=c.getString(0);t.program=c.getString(1);t.subject=c.getString(2);t.book=c.getInt(3);t.topic=c.getString(4);t.label=c.getString(5);t.questions=c.getInt(6);t.review=c.getInt(7);t.page=c.getInt(8);t.pdf=c.getString(9);return t;}
    private final String COLUMNS="id,program,subject,book_no,topic_extracted,test_label_extracted,answer_entries,needs_review,pdf_page,pdf_file";
    Test test(String id){try(Cursor c=db.rawQuery("SELECT "+COLUMNS+" FROM tests WHERE id=?",new String[]{id})){return c.moveToFirst()?from(c):null;}}
    List<Test> search(String query,String prog,String sub,int book,int limit){
        List<Test> list=new ArrayList<>();StringBuilder sql=new StringBuilder("SELECT "+COLUMNS+" FROM tests WHERE 1=1");List<String> args=new ArrayList<>();
        if(prog!=null&&!prog.trim().isEmpty()){sql.append(" AND program LIKE ?");args.add("%"+prog+"%");}
        if(sub!=null&&!sub.trim().isEmpty()){sql.append(" AND subject=?");args.add(sub);}
        if(book>0){sql.append(" AND book_no=?");args.add(String.valueOf(book));}
        String q=query==null?"":query.trim();
        if(!q.isEmpty()){
            for(String token:q.split("\\s+")){
                sql.append(" AND (topic_extracted LIKE ? OR test_label_extracted LIKE ? OR header_nearest LIKE ? OR subject LIKE ?)");
                for(int i=0;i<4;i++) args.add("%"+token+"%");
            }
        }
        sql.append(" ORDER BY CASE WHEN test_label_extracted IS NULL THEN 1 ELSE 0 END,subject,book_no,pdf_page,group_on_page LIMIT ").append(Math.min(200,Math.max(1,limit)));
        try(Cursor c=db.rawQuery(sql.toString(),args.toArray(new String[0]))){while(c.moveToNext())list.add(from(c));}
        return list;
    }
    List<String> subjects(String prog){List<String> a=new ArrayList<>();a.add("Tüm dersler");
        String sql="SELECT DISTINCT subject FROM tests "+((prog==null||prog.trim().isEmpty())?"":"WHERE program LIKE ? ")+"ORDER BY subject";
        try(Cursor c=db.rawQuery(sql,prog==null||prog.trim().isEmpty()?null:new String[]{"%"+prog+"%"})){while(c.moveToNext())a.add(c.getString(0));}return a;
    }
    List<Answer> answers(String id){List<Answer> list=new ArrayList<>();try(Cursor c=db.rawQuery("SELECT printed_question_no,correct_option FROM answers WHERE test_id=? ORDER BY entry_index",new String[]{id})){while(c.moveToNext()){Answer a=new Answer();a.printedNumber=c.getInt(0);a.correct=c.getString(1);list.add(a);}}return list;}
    int count(){try(Cursor c=db.rawQuery("SELECT COUNT(*) FROM tests",null)){c.moveToFirst();return c.getInt(0);}}
}
