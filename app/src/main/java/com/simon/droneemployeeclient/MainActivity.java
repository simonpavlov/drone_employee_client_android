package com.simon.droneemployeeclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.SupportMapFragment;
import com.simon.droneemployeeclient.droneflat.Drone;
import com.simon.droneemployeeclient.droneflat.DroneList;
import com.simon.droneemployeeclient.droneflat.DroneEmployeeBase;
import com.simon.droneemployeeclient.droneflat.SwitchButton;
import com.simon.droneemployeeclient.droneflat.Task;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Menu mSideMenu;
    private FragmentManager mFragmentManager;
    private MapTools mMapTools;
    private DroneEmployeeBase mDroneEmployeeBase;
    private DroneList mAvailableDrones;
    private ItemIdTaskMap mItemIdTaskMap;
    private Task mCurrentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Initialize main part
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Fragment manager initialize
        mFragmentManager = getSupportFragmentManager();

        //MapTools initialize
        mMapTools = new MapTools((SupportMapFragment) mFragmentManager.
                findFragmentById(R.id.location_map));

        //Employee initialize
        mDroneEmployeeBase = new DroneEmployeeBase();
        mAvailableDrones = mDroneEmployeeBase.loadAvailableDrones();
        mItemIdTaskMap = new ItemIdTaskMap();
        mCurrentTask = null;

        //Toolbar initialize
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Floating button initialize
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new SwitchButton(
                getResources().getDrawable(R.drawable.ic_done_light),
                getResources().getDrawable(R.drawable.ic_add_light),
                mMapTools));

        //???
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //Initialize NavigationView
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mSideMenu = navigationView.getMenu();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        /*
        for(Drone drone: mAvailableDrones){
            menu.add(drone.getAddress());
        }
        */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_send_tasks){
            for(Task task: mItemIdTaskMap.values()){
                mDroneEmployeeBase.sendTask(task);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Log.i("LOGINFO", "IN onNavigationItemSelected ID: " + id);

        if (id == R.id.nav_buy) {
            Log.i("LOGINFO", "Select nav_buy");
            ArrayList<String> allId = mAvailableDrones.getAllId();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Available DRONES:");
            builder.setItems(allId.toArray(new String[allId.size()]),
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Drone drone = mAvailableDrones.get(which);
                    Log.i("LOGINFO", String.valueOf(drone));

                    Task task = new Task(mDroneEmployeeBase.byTicket(drone));
                    task.addWaypoint(drone.getLastPosition());
                    if(mCurrentTask == null) {
                        mCurrentTask = task;
                        mMapTools.setCurrentTask(task);
                    }

                    MenuItem menuItem = mSideMenu.add(0, drone.hashCode(), 0, drone.getAddress());
                    mItemIdTaskMap.put(menuItem.getItemId(), task);
                    mAvailableDrones.remove(which);
                }
            });
            builder.show();
            return true;
        } else if (mItemIdTaskMap.containsKey(id)) {
            Task task = mItemIdTaskMap.get(id);
            Log.i("LOGINFO", "Select TASK: " + task);
            mCurrentTask = task;
            mMapTools.setCurrentTask(task);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}