package com.uranites.shhhquiet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;

public class FAQ extends AppCompatActivity {

    ExpandableListView expandableListView;
    ArrayList<String> listGroup = new ArrayList<>();
    HashMap<String, ArrayList<String>> listChild = new HashMap<>();
    MainAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_f_a_q);

        expandableListView = findViewById(R.id.exp_list_view);

        for(int g=0; g<=10; g++)
        {
            listGroup.add("Group"+g);
            ArrayList<String> arrayList = new ArrayList<>();
            for(int c=0; c<=5; c++)
            {
                arrayList.add("Item"+c);
            }
            listChild.put(listGroup.get(g), arrayList);
        }

        adapter = new MainAdapter(listGroup, listChild);
        expandableListView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
        finish();
    }
}