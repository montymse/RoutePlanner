package com.example.routeplanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.HashMap;


public class PrepareRunActivity extends AppCompatActivity {
    static private final int GET_LIST_ITEM_REQUEST_CODE = 1;
    static private final int START_ROUTE_RUN_REQUEST_CODE = 2;
    public static final String EXTRA_MESSAGE_ROUTE_OVERVIEW = "com.example.routeplanner.GET_LIST_ITEM";
    private boolean routeSelected = false;

    private String routeName = "";
    private String routeDistance = "";
    private String routePositions = "";
    private boolean routeIsCyclic;

    private TextView tvRouteName;
    private TextView tvRouteDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prepare_run_activity);

        tvRouteName = (TextView) findViewById(R.id.textView_route_name);
        tvRouteDistance = (TextView) findViewById(R.id.textView_route_distance);

        Button runRouteButton = findViewById(R.id.button_run_route);
        runRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (routeSelected) {
                    Intent intent = new Intent(PrepareRunActivity.this, RunRouteActivity.class);
                    intent.putExtra("name",routeName)
                            .putExtra("distance",routeName)
                            .putExtra("positions",routePositions)
                            .putExtra("cyclic",routeIsCyclic);
                    startActivityForResult(intent, START_ROUTE_RUN_REQUEST_CODE);
                } else {
                    Toast.makeText(PrepareRunActivity.this, "Please select a route first", Toast.LENGTH_SHORT).show();
                }

            }
        });

        Button chooseRouteButton = findViewById(R.id.button_choose_route);
        chooseRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PrepareRunActivity.this, RouteOverviewActivity.class);
                intent.putExtra(EXTRA_MESSAGE_ROUTE_OVERVIEW, true);
                startActivityForResult(intent, GET_LIST_ITEM_REQUEST_CODE);

            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        Log.d("GetListItemTest", "Entered onActivityResult()");
        if(resultCode == Activity.RESULT_OK &&
            requestCode == GET_LIST_ITEM_REQUEST_CODE) {
            Log.d("GetListItemTest", "Name and distance: " + data.getStringExtra("name") + " " +  data.getStringExtra("distance"));
            routeSelected = true;
            tvRouteName.setText(routeName = data.getStringExtra("name"));
            if (!data.getStringExtra("distance").equals("")) {
                tvRouteDistance.setText(routeDistance = data.getStringExtra("distance"));
            } else {
                tvRouteDistance.setText("Distance not shown");
            }
            routePositions = data.getStringExtra("positions");
            routeIsCyclic = data.getBooleanExtra("cyclic", false);

        } else if (resultCode == Activity.RESULT_OK &&
                requestCode == START_ROUTE_RUN_REQUEST_CODE) {

        }
    }
}