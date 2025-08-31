package com.example.parking_lot_entry_tracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parking_lot_entry_tracker.R;

public class QueueManagementActivity extends AppCompatActivity {
    private ParkingQueueManager queueManager;
    private QueueAdapter queueAdapter;
    private RecyclerView rvQueue;
    private TextView tvQueueSummary, tvQueueCount, tvEmptyQueue;
    private TextView tvEstimatedTime, tvEstimateDetails;
    private EditText etEstimatePlate, etCancelPlate;
    private Button btnCalculateTime, btnRemoveFromQueue, btnProcessNext, btnSearchCar, btnRefreshQueue;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_management);

        queueManager = ParkingQueueManager.getInstance();

        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        updateUI();
    }


    private void initializeViews() {
        // Header
        tvQueueSummary = findViewById(R.id.tvQueueSummary);
        tvQueueCount = findViewById(R.id.tvQueueCount);
        tvEmptyQueue = findViewById(R.id.tvEmptyQueue);

        // Queue RecyclerView
        rvQueue = findViewById(R.id.rvQueue);

        // Estimation
        etEstimatePlate = findViewById(R.id.etEstimatePlate);
        btnCalculateTime = findViewById(R.id.btnCalculateTime);
        tvEstimatedTime = findViewById(R.id.tvEstimatedTime);
        tvEstimateDetails = findViewById(R.id.tvEstimateDetails);

        // Cancellation
        etCancelPlate = findViewById(R.id.etCancelPlate);
        btnRemoveFromQueue = findViewById(R.id.btnRemoveFromQueue);

        // Actions
        btnProcessNext = findViewById(R.id.btnProcessNext);
        btnSearchCar = findViewById(R.id.btnSearchCar);
        btnRefreshQueue = findViewById(R.id.btnRefreshQueue);
    }

    private void setupRecyclerView() {
        queueAdapter = new QueueAdapter(this, queueManager.getAllCars(), queueManager);
        rvQueue.setLayoutManager(new LinearLayoutManager(this));
        rvQueue.setAdapter(queueAdapter);
    }

    private void setupClickListeners() {
        btnCalculateTime.setOnClickListener(v -> calculateWaitingTime());
        btnRemoveFromQueue.setOnClickListener(v -> removeCarFromQueue());
        btnProcessNext.setOnClickListener(v -> processNextCar());
        btnSearchCar.setOnClickListener(v -> searchCar());
        btnRefreshQueue.setOnClickListener(v -> updateUI());


            // Add your back button code to the existing method
            ImageButton btnBack = findViewById(R.id.btnBack);
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish(); // This will close current activity and return to previous one
                }
            });
    }

    private void calculateWaitingTime() {
        String plateNumber = etEstimatePlate.getText().toString().trim();

        if (plateNumber.isEmpty()) {
            Toast.makeText(this, "Please enter a car number plate", Toast.LENGTH_SHORT).show();
            return;
        }

        Car car = queueManager.findCarByPlate(plateNumber);
        if (car != null) {
            int estimatedWait = queueManager.estimateWaitingTime(plateNumber);
            tvEstimatedTime.setText("~" + estimatedWait + " minutes");
            tvEstimateDetails.setText("Based on position " + car.getPosition() + getSuffix(car.getPosition()) +
                    " • Avg parking: " + queueManager.getAverageParkingDuration() + " mins");

            Toast.makeText(this, "Estimated wait time calculated", Toast.LENGTH_SHORT).show();
        } else {
            tvEstimatedTime.setText("Car not found");
            tvEstimateDetails.setText("Please check the number plate");
            Toast.makeText(this, "Car not found in queue", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeCarFromQueue() {
        String plateNumber = etCancelPlate.getText().toString().trim();

        if (plateNumber.isEmpty()) {
            Toast.makeText(this, "Please enter a car number plate", Toast.LENGTH_SHORT).show();
            return;
        }

        Car car = queueManager.findCarByPlate(plateNumber);
        if (car != null) {
            // Show confirmation dialog
            new AlertDialog.Builder(this)
                    .setTitle("⚠️ Remove Car")
                    .setMessage("Are you sure you want to remove " + plateNumber + " from the queue?\n\n" +
                            "This will adjust all other car positions.")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        if (queueManager.removeCar(plateNumber)) {
                            Toast.makeText(this, "Car " + plateNumber + " removed from queue", Toast.LENGTH_SHORT).show();
                            etCancelPlate.setText("");
                            updateUI();
                        } else {
                            Toast.makeText(this, "Failed to remove car", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            Toast.makeText(this, "Car not found in queue", Toast.LENGTH_SHORT).show();
        }
    }

    private void processNextCar() {
        Car nextCar = queueManager.dequeueCar();
        if (nextCar != null) {
            Toast.makeText(this, "Entry processed for " + nextCar.getNumberPlate(), Toast.LENGTH_LONG).show();
            updateUI();
        } else {
            Toast.makeText(this, "No cars in queue to process", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchCar() {
        EditText input = new EditText(this);
        input.setHint("Enter car number plate");

        new AlertDialog.Builder(this)
                .setTitle("🔍 Search Car")
                .setMessage("Enter the car number plate to search:")
                .setView(input)
                .setPositiveButton("Search", (dialog, which) -> {
                    String plate = input.getText().toString().trim();
                    if (!plate.isEmpty()) {
                        Car car = queueManager.findCarByPlate(plate);
                        if (car != null) {
                            int waitTime = queueManager.estimateWaitingTime(plate);
                            String message = "Car Found!\n\n" +
                                    "Number Plate: " + car.getNumberPlate() + "\n" +
                                    "Token: " + car.getTokenNumber() + "\n" +
                                    "Position: " + car.getPosition() + getSuffix(car.getPosition()) + "\n" +
                                    "Waiting: " + car.getWaitingTimeMinutes() + " minutes\n" +
                                    "Estimated remaining: ~" + waitTime + " minutes";

                            new AlertDialog.Builder(this)
                                    .setTitle("✅ Car Found")
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                        } else {
                            Toast.makeText(this, "Car not found in queue", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateUI() {
        // Update header summary
        Car nextCar = queueManager.peekNextCar();
        String summary = queueManager.getQueueSize() + " cars";
        if (nextCar != null) {
            summary += " • Next: " + nextCar.getNumberPlate();
        }
        tvQueueSummary.setText(summary);

        // Update queue count
        tvQueueCount.setText(queueManager.getQueueSize() + " cars");

        // Update RecyclerView
        queueAdapter.updateQueue(queueManager.getAllCars());

        // Show/hide empty message
        if (queueManager.isEmpty()) {
            rvQueue.setVisibility(View.GONE);
            tvEmptyQueue.setVisibility(View.VISIBLE);
        } else {
            rvQueue.setVisibility(View.VISIBLE);
            tvEmptyQueue.setVisibility(View.GONE);
        }

        // Enable/disable buttons
        btnProcessNext.setEnabled(!queueManager.isEmpty());
    }

    private String getSuffix(int position) {
        if (position >= 11 && position <= 13) return "th";
        switch (position % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }


}
