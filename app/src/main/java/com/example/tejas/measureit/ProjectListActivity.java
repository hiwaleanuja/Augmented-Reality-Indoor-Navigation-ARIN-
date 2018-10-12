package com.example.tejas.measureit;

import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ProjectListActivity extends AppCompatActivity implements NewProjectDialog.NewProjectDialogListener{


    private static final String TAG = "ProjectListActivity";

    FloatingActionButton fab;
    DatabaseHelper myDB;

    private int PROJECT_ID;
    private String PROJECT_TITLE;
    private String PROJECT_DESCRIPTION;
    private int PROJECT_MEASUREMENT;
    private int dataCount;

    List<Project> projectList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Projects");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fab = findViewById(R.id.NewProjectFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Fab Clicked");
                openAddProjectDialog();
            }
        });

        RecyclerView recyclerView = findViewById(R.id.projectRecycler);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        projectList = new ArrayList<>();

        myDB = DatabaseHelper.getInstance(this);

        //Fire SELECT query and add the received the to projectList
        //This will be done each time onCreate runs.

        Cursor res = myDB.selectData();
        dataCount = res.getCount();
        if(dataCount > 0){
            while (res.moveToNext()){
                PROJECT_ID = res.getInt(0);
                PROJECT_TITLE = res.getString(1);
                PROJECT_DESCRIPTION = res.getString(2);
                PROJECT_MEASUREMENT = res.getInt(3);


                addToProjectList(++dataCount, PROJECT_TITLE, PROJECT_DESCRIPTION, PROJECT_MEASUREMENT);
            }
            res.close();
        }
        myDB.close();

        RecyclerView.Adapter mAdapter = new ProjectAdapter(this, projectList);
        recyclerView.setAdapter(mAdapter);
    }

    public void openAddProjectDialog() {
        NewProjectDialog newProjectDialog = new NewProjectDialog();
        newProjectDialog.show(getSupportFragmentManager(), "New Project Dialog");
    }

    @Override
    public void getInfo(String title, String description) {
        //Insert the data in the projectList as well as in the database.
        PROJECT_TITLE = title;
        PROJECT_DESCRIPTION = description;

        boolean isInserted = myDB.insertData(PROJECT_TITLE, PROJECT_DESCRIPTION, 10);
        if(isInserted){
            addToProjectList(++dataCount, PROJECT_TITLE, PROJECT_DESCRIPTION, 10);
            Toast.makeText(ProjectListActivity.this,
                            "New Project Added",
                            Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(ProjectListActivity.this,
                    "Failed",
                    Toast.LENGTH_LONG).show();
        }

    }

    private void addToProjectList(int id, String title, String description, int measurement){
        Project temp = new Project(
                id,
                title,
                description,
                Integer.toString(measurement),
                R.drawable.property
        );

        projectList.add(temp);
    }

}
