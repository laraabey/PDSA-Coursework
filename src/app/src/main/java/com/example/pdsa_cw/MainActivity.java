package com.example.myapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pdsa_cw.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView queueCountText, nextInQueueText;
    private RecyclerView recyclerQueue;
    private QueueAdapter adapter;
    private ArrayList<String> queueList;

    // Firebase reference
    private DatabaseReference queueRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.MainActivity);

        // Initialize views
        queueCountText = findViewById(R.id.queueCountText);
        nextInQueueText = findViewById(R.id.nextInQueueText); // Make sure you added this TextView in XML for "Next in queue"
        recyclerQueue = findViewById(R.id.recyclerQueue);

        // Setup RecyclerView
        recyclerQueue.setLayoutManager(new LinearLayoutManager(this));
        queueList = new ArrayList<>();
        adapter = new QueueAdapter(queueList);
        recyclerQueue.setAdapter(adapter);

        // Firebase database reference
        queueRef = FirebaseDatabase.getInstance().getReference("queue");

        // Listen for changes
        queueRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                queueList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    String car = itemSnapshot.getValue(String.class);
                    queueList.add(car);
                }

                // Update RecyclerView
                adapter.notifyDataSetChanged();

                // Update count
                int count = queueList.size();
                queueCountText.setText("cars = " + count);

                // Update next in queue
                if (count > 0) {
                    nextInQueueText.setText("Next: " + queueList.get(0));
                } else {
                    nextInQueueText.setText("No cars in queue");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });
    }
}
