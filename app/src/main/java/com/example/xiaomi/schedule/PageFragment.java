package com.example.xiaomi.schedule;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;


public class PageFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    private static final String DB_SCHEDULE = "schedule";
    private static final int ALL_PARA_NUMBER = 7;
    private int mPage;

    public static PageFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        PageFragment fragment = new PageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getInt(ARG_PAGE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return getSchedule(mPage, container, inflater);
    }

    public LinearLayout getSchedule(int day, ViewGroup container, LayoutInflater inflater) {
        DBHelper dbHelper = new DBHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor;
        String[] selectionArgs = {Integer.toString(day)};
        cursor = db.query(DB_SCHEDULE, null, "day = ?", selectionArgs, null, null, null);

        int time, index;
        String para, para2, room, room2, teacher, teacher2;
        boolean paraNotFound;
        LinearLayout linearLayout1 = (LinearLayout) inflater.inflate(R.layout.activity_sched, container, false);
        LinearLayout linearLayout = linearLayout1.findViewById(R.id.linLayout);
        int[] tvId = {
                R.id.tv1para,
                R.id.tv2para,
                R.id.tv3para,
                R.id.tv4para,
                R.id.tv5para,
                R.id.tv6para,
                R.id.tv7para
        };

        for (int i = 1; i <= ALL_PARA_NUMBER; i++) {
            paraNotFound = true;
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    time = cursor.getInt(cursor.getColumnIndex("time"));
                    if (time == i) {
                        para = cursor.getString(cursor.getColumnIndex("para"));
                        para2 = cursor.getString(cursor.getColumnIndex("para2"));
                        room = cursor.getString(cursor.getColumnIndex("room"));
                        room2 = cursor.getString(cursor.getColumnIndex("room2"));
                        teacher = cursor.getString(cursor.getColumnIndex("teacher"));
                        teacher2 = cursor.getString(cursor.getColumnIndex("teacher2"));
                        index = cursor.getInt(cursor.getColumnIndex("pareIndex"));
                        paraNotFound = false;
                        String[] params1 = new String[]{
                                para,
                                room,
                                teacher
                        };
                        String[] params2 = new String[]{
                                para2,
                                room2,
                                teacher2
                        };
                        if (index == 0) {
                            addPara(linearLayout, params1, inflater);
                        } else {
                            addPara(linearLayout, params1, params2, inflater);
                        }
                    }
                } while (cursor.moveToNext());

            }

            if (paraNotFound) {
                addEmptyPara(linearLayout, inflater);
            }
        }
        cursor.close();
        db.close();
        return linearLayout1;
    }

    private void addEmptyPara(LinearLayout linearLayout, LayoutInflater inflater) {
        ConstraintLayout pareLayout = (ConstraintLayout) inflater.inflate(R.layout.pare_item, linearLayout, false);
        linearLayout.addView(pareLayout);
    }

    private void addPara(LinearLayout linearLayout, String[] params, LayoutInflater inflater) {
        ConstraintLayout pareLayout = (ConstraintLayout) inflater.inflate(R.layout.pare_item, linearLayout, false);

        TextView tvPare = pareLayout.findViewById(R.id.tv_para);
        TextView tvRoom = pareLayout.findViewById(R.id.tv_room);
        TextView tvTeacher = pareLayout.findViewById(R.id.tv_teacher);

        tvPare.setText(params[0]);
        tvRoom.setText(params[1]);
        tvTeacher.setText(params[2]);


        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if (tabletSize) {
            tvPare.setTextSize(20);
            tvRoom.setTextSize(16);
            tvTeacher.setTextSize(16);
        } else {
            tvPare.setTextSize(12);
            tvRoom.setTextSize(11);
            tvTeacher.setTextSize(11);
        }

        linearLayout.addView(pareLayout);
    }

    private void addPara(LinearLayout linearLayout, String[] params1, String[] params2, LayoutInflater inflater) {
        LinearLayout helpLayout = new LinearLayout(getContext());
        helpLayout.setOrientation(LinearLayout.HORIZONTAL);                     //создаем лэйаут с двумя pare_item
        LinearLayout.LayoutParams linLayoutParam = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        linLayoutParam.weight = 1;

        ConstraintLayout pareLayout1 = (ConstraintLayout) inflater.inflate(R.layout.pare_item, linearLayout, false);
        ConstraintLayout pareLayout2 = (ConstraintLayout) inflater.inflate(R.layout.pare_item, linearLayout, false);

        TextView tvPare1 = pareLayout1.findViewById(R.id.tv_para);
        TextView tvRoom1 = pareLayout1.findViewById(R.id.tv_room);
        TextView tvTeacher1 = pareLayout1.findViewById(R.id.tv_teacher);

        TextView tvPare2 = pareLayout2.findViewById(R.id.tv_para);
        TextView tvRoom2 = pareLayout2.findViewById(R.id.tv_room);
        TextView tvTeacher2 = pareLayout2.findViewById(R.id.tv_teacher);


        tvPare1.setText(params1[0]);
        tvRoom1.setText(params1[1]);
        tvTeacher1.setText(params1[2]);


        tvPare2.setText(params2[0]);
        tvRoom2.setText(params2[1]);
        tvTeacher2.setText(params2[2]);

        tvPare1.setTextColor(getResources().getColor(R.color.paraGreen));
        tvRoom1.setTextColor(getResources().getColor(R.color.paraGreen));
        tvTeacher1.setTextColor(getResources().getColor(R.color.paraGreen));

        tvPare2.setTextColor(getResources().getColor(R.color.paraBlue));
        tvRoom2.setTextColor(getResources().getColor(R.color.paraBlue));
        tvTeacher2.setTextColor(getResources().getColor(R.color.paraBlue));

        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if (tabletSize) {
            tvPare1.setTextSize(20);
            tvRoom1.setTextSize(16);
            tvTeacher1.setTextSize(16);
            tvPare2.setTextSize(20);
            tvRoom2.setTextSize(16);
            tvTeacher2.setTextSize(16);
        } else {
            tvPare1.setTextSize(12);
            tvRoom1.setTextSize(11);
            tvTeacher1.setTextSize(11);
            tvPare2.setTextSize(12);
            tvRoom2.setTextSize(11);
            tvTeacher2.setTextSize(11);
        }

        helpLayout.addView(pareLayout1);
        helpLayout.addView(pareLayout2);

        linearLayout.addView(helpLayout, linLayoutParam);

    }
}



