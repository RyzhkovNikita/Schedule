package com.example.xiaomi.schedule;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import static android.content.Context.MODE_PRIVATE;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_SCHEDULE = "schedule";
    private static final int DB_VERSION = 3;
    private Context context;
    private ResultCode lastOperationCode;
    final static private String httpAddress = "https://students.bmstu.ru";
    private final String HTML_ADDRESS = "HTML_ADDRESS";


    DBHelper(Context context) {
        super(context, DB_SCHEDULE, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + DB_SCHEDULE + "(_id integer primary key autoincrement," +
                " day integer," +
                " para text," +
                " para2 text," +
                " room text," +
                " room2 text," +
                " time integer," +
                " teacher text," +
                " teacher2 text," +
                " pareIndex integer);");
        try {
            Document document = null;
            String html = context.getSharedPreferences("Pref", MODE_PRIVATE).getString(HTML_ADDRESS, "");
            document = Jsoup.connect(httpAddress + html).get();
            DBInitialization(db, document);
        } catch (NoScheduleException exception) {
            lastOperationCode = ResultCode.NO_SCHEDULE;
        } catch (IOException exception) {
            lastOperationCode = ResultCode.NO_INTERNET;
        } catch (Exception exception) {
            lastOperationCode = ResultCode.EXCEPTION;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + DB_SCHEDULE);
        onCreate(db);
    }

    public void refreshTable(SQLiteDatabase db) {
        try {
            Document document = null;
            String html = context.getSharedPreferences("Pref", MODE_PRIVATE).getString(HTML_ADDRESS, "");
            document = Jsoup.connect(httpAddress + html).get();
            db.execSQL("drop table if exists " + DB_SCHEDULE);
            db.execSQL("create table if not exists " + DB_SCHEDULE + "(_id integer primary key autoincrement," +
                    " day integer," +
                    " para text," +
                    " para2 text," +
                    " room text," +
                    " room2 text," +
                    " time integer," +
                    " teacher text," +
                    " teacher2 text," +
                    " pareIndex integer);");
            DBInitialization(db, document);
        } catch (NoScheduleException exception) {
            lastOperationCode = ResultCode.NO_SCHEDULE;
        } catch (IOException exception) {
            lastOperationCode = ResultCode.NO_INTERNET;
        } catch (Exception exception) {
            lastOperationCode = ResultCode.EXCEPTION;
        }

    }

    private synchronized void DBInitialization(SQLiteDatabase db, Document document) throws Exception {
        String[] times = new String[]{
                context.getString(R.string.para1),
                context.getString(R.string.para2),
                context.getString(R.string.para3),
                context.getString(R.string.para4),
                context.getString(R.string.para5),
                context.getString(R.string.para6),
                context.getString(R.string.para7),
        };

        Elements weekName = document.getElementsByTag("h4");
        String week = weekName.get(0).text();
        String[] weeks = week.split(", ");

        Elements title = document.getElementsByTag("h1");
        String groupTitle = title.text();

        SharedPreferences sharedPreferences = context.getSharedPreferences("Pref", MODE_PRIVATE);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putString("weekCount", weeks[0]);
        ed.putString("weekName", weeks[1]);
        ed.putString("group", groupTitle);
        ed.apply();

        Elements tables = document.getElementsByClass("hidden-lg");
        if (tables.size() < 6)
            throw new NoScheduleException();
        String pare, pare2, time, room, room2, teacher, teacher2, type, type2;
        boolean isPared;

        db.beginTransaction();
        try {
            for (int dayOfWeek = 1; dayOfWeek <= 6; dayOfWeek++) {     //по дням недели

                Element table = tables.get(dayOfWeek);
                Elements elements = table.getElementsByTag("tr");

                for (org.jsoup.nodes.Element element : elements) {      //получаем 1 пару на вход

                    pare = pare2 = time = room = room2 = teacher = teacher2 = type = type2 = "";
                    isPared = false;
                    Elements tdTags = element.getElementsByTag("td");

                    for (org.jsoup.nodes.Element elem : tdTags) {       //получаем кучу тд строчек и обрабатываем

                        if (elem.hasClass("text-nowrap")) {
                            time = elem.text();
                        } else if (elem.hasClass("text-success")) {
                            pare = elem.getElementsByTag("span").text();
                            isPared = true;
                            Elements attributes = elem.getElementsByTag("i");
                            if (attributes.size() != 0) {
                                type = attributes.get(0).text();
                                room = attributes.get(1).text();
                                teacher = attributes.get(2).text();
                            }
                        } else if (elem.hasClass("text-info")) {
                            pare2 = elem.getElementsByTag("span").text();
                            isPared = true;
                            Elements attributes = elem.getElementsByTag("i");
                            if (attributes.size() != 0) {
                                type2 = attributes.get(0).text();
                                room2 = attributes.get(1).text();
                                teacher2 = attributes.get(2).text();
                            }
                        } else if (elem.hasAttr("colspan")) {
                            pare = elem.getElementsByTag("span").text();
                            Elements attributes = elem.getElementsByTag("i");
                            if (attributes.size() != 0) {
                                type = attributes.get(0).text();
                                room = attributes.get(1).text();
                                teacher = attributes.get(2).text();
                            }
                        }
                    }
                    int timeIndex = 0;
                    for (int i = 0; i < times.length; i++) {
                        if ((times[i].replaceAll("\\D", "")).equals(time.replaceAll("\\D", ""))) {
                            timeIndex = i + 1;
                            break;
                        }
                    }

                    if (pare.isEmpty() && pare2.isEmpty())
                        continue;
                    if (pare2.isEmpty())
                        pare2 = context.getString(R.string.empty);
                    if (pare.isEmpty())
                        pare = context.getString(R.string.empty);

                    ContentValues para = new ContentValues();
                    para.put("day", dayOfWeek);
                    para.put("time", timeIndex);
                    para.put("para", type + " " + pare);
                    para.put("para2", type2 + " " + pare2);
                    para.put("room", room);
                    para.put("room2", room2);
                    para.put("teacher", teacher);
                    para.put("teacher2", teacher2);
                    if (isPared)
                        para.put("pareIndex", 1);
                    else
                        para.put("pareIndex", 0);

                    db.insert(DB_SCHEDULE, null, para);
                }
            }
            db.setTransactionSuccessful();
            lastOperationCode = ResultCode.OK;
        } finally {
            db.endTransaction();
        }
    }

    public ResultCode LastOperationResult() {
        return lastOperationCode;
    }
}