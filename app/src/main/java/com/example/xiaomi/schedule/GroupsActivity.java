package com.example.xiaomi.schedule;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.SimpleExpandableListAdapter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupsActivity extends AppCompatActivity implements ExpandableListView.OnChildClickListener {

    ExpandableListView listView;
    String groupKey = "group";
    String kafedraKey = "kafedra";
    ProgressBar progressBar;
    final String HTML_ADDRESS = "HTML_ADDRESS";
    ArrayList<Map<String, String>> htmlAddresses;
    SimpleExpandableListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        listView = findViewById(R.id.listView);
        adapterInitialization();
    }

    void adapterInitialization() {
        ArrayList<Map<String, String>> kafedraData = new ArrayList<>();
        ArrayList<ArrayList<Map<String, String>>> data = new ArrayList<>();
        htmlAddresses = new ArrayList<>();
        new Thread(() -> {
            Document document = null;
            try {
                document = Jsoup.connect("https://students.bmstu.ru/schedule/list").get();
            } catch (IOException ignored) {
            }
            if (document != null) {
                Elements fackulties = document.getElementsByClass("panel-body");
                Elements accordions = new Elements();
                Elements kafedry;
                for (Element fackulty : fackulties) {
                    kafedry = fackulty.getElementsByClass("accordion");
                    accordions.addAll(kafedry);
                }
                Map<String, String> map;

                for (Element kafedra : accordions) {//заполняем список кафедр
                    Elements kafedraTitle = kafedra.getElementsByTag("h4");
                    map = new HashMap<>();
                    map.put(kafedraKey, kafedraTitle.text());
                    kafedraData.add(map);
                }


                ArrayList<Map<String, String>> groupsAtList;


                for (Element accordion : accordions) {
                    Elements groups = accordion.getElementsByClass("text-nowrap");
                    groupsAtList = new ArrayList<>();
                    for (Element group : groups) {
                        if (group.hasClass("disabled"))
                            continue;
                        map = new HashMap<>();
                        String groupName = group.text().replaceAll("\\(\\Б\\)", "").replaceAll("\\(\\М\\)", "");
                        map.put(groupKey, groupName);
                        groupsAtList.add(map);
                        map = new HashMap<>();
                        map.put(groupName, group.attr("href"));    //html адрес группы
                        htmlAddresses.add(map);
                    }
                    data.add(groupsAtList);
                }

                int size = data.size();
                for (int i = size - 1; i >= 0; i--) {
                    if (data.get(i).isEmpty()) {
                        data.remove(i);
                        kafedraData.remove(i);
                    }
                }

                String groupFrom[] = new String[]{kafedraKey};
                int groupTo[] = new int[]{android.R.id.text1};

                String childFrom[] = new String[]{groupKey};
                int childTo[] = new int[]{android.R.id.text1};

                adapter = new SimpleExpandableListAdapter(
                        this,
                        kafedraData,
                        android.R.layout.simple_expandable_list_item_1,
                        groupFrom,
                        groupTo,
                        data,
                        android.R.layout.simple_list_item_1,
                        childFrom,
                        childTo);
                GroupsActivity.this.runOnUiThread(() -> {
                    listView.setAdapter(adapter);
                    progressBar.setVisibility(View.INVISIBLE);
                    listView.setOnChildClickListener(this);
                });
            } else {
                GroupsActivity.this.runOnUiThread(() -> {
                    setContentView(R.layout.no_connection);
                    Button refreshBtn = findViewById(R.id.refreshBtn);
                    refreshBtn.setOnClickListener(view -> {
                        setContentView(R.layout.activity_main);
                        progressBar = findViewById(R.id.progressBar);
                        progressBar.setVisibility(View.VISIBLE);
                        listView = findViewById(R.id.listView);
                        adapterInitialization();
                    });
                });
            }
        }).start();
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Intent intent = new Intent();
        Map map = (Map<String, String>) parent.getExpandableListAdapter().getChild(groupPosition, childPosition);
        String groupName = (String) map.get(groupKey);
        String htmlAddress = getHtmlByGroup(groupName);
        intent.putExtra(HTML_ADDRESS, htmlAddress);
        intent.putExtra(groupKey, groupName);
        setResult(RESULT_OK, intent);
        finish();
        return false;
    }

    private String getHtmlByGroup(String groupName) {
        for (Map<String, String> map : htmlAddresses) {
            if (map.containsKey(groupName))
                return map.get(groupName);
        }
        return null;
    }
}
