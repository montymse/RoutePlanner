package com.example.routeplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class PersonalStatisticsActivity extends AppCompatActivity {


    DatabaseReference rootRef;
    String ID;

    TextView display_distance;
    TextView display_time;
    TextView display_avgSpeed;

    public int totalTime;
    public double avgSpeed;
    public double totalDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_statistics);

        //Get reference
        rootRef= FirebaseDatabase.getInstance().getReference().child("PersonalStats");
        ID="-MAD-Y5CEBd4OA0yp7g4";

        //Setting textview
        display_distance = findViewById(R.id.displaydistance);
        display_time = findViewById(R.id.displaytime);
        display_avgSpeed =findViewById(R.id.displayspeed);

        //Getting lastest stats
        rootRef.child(ID).addListenerForSingleValueEvent(new ValueEventListener() {
                 @Override
                 public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                     //Getting info to stats
                     totalTime=dataSnapshot.getValue(PersonalStats.class).getTime();
                     avgSpeed =Calspeed(dataSnapshot.getValue(PersonalStats.class).getNum(),dataSnapshot.getValue(PersonalStats.class).getTotalspeed());
                     totalDistance=dataSnapshot.getValue(PersonalStats.class).getTotaldistance();

                     totalTime = Math.round(totalTime*100) / 100;
                     avgSpeed = Math.round(avgSpeed*100.0) / 100.0;
                     totalDistance = Math.round(totalDistance*100.0) / 100.0;

                     //Calculating statistics
                     //Viewing staticstics
                     display_time.setText("Total Time:\n"+totalTime + " minutes");
                     display_distance.setText("Total distance:\n"+totalDistance + " km");
                     display_avgSpeed.setText("Average speed:\n"+ avgSpeed + " km/h");
                 }

                 @Override
                 public void onCancelled(@NonNull DatabaseError databaseError) {

                 }
             });
    }


    private double Calspeed(int num, double total )  {
            return (total / (double) num);
    }
}



