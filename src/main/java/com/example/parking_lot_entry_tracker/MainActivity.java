package com.example.parking_lot_entry_tracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

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

import com.example.parking_lot_entry_tracker.Car;
import com.example.parking_lot_entry_tracker.ParkingQueueManager;
import com.example.parking_lot_entry_tracker.QueueManagementActivity;
import com.example.parking_lot_entry_tracker.R;

public class MainActivity extends AppCompatActivity {
    private ParkingQueueManager queueManager;
    private TextView tvWaitingCount, tvAvailableSlots, tvAvgWait;
    private TextView tvNextCarPlate, tvNextCarInfo;
    private TextView tvTokenPreview, tvPositionPreview;
    private EditText etCarPlate;
    private Button btnAddCar, btnAllowEntry, btnShowEstimates;
    private Button btnViewQueue, btnPriority, btnRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_control);

        // Initialize Queue Manager with 25 total slots
        queueManager = ParkingQueueManager.getInstance(25);

        initializeViews();
        setupClickListeners();
        updateUI();
    }

    private void initializeViews() {
        // Stats
        tvWaitingCount = findViewById(R.id.tvWaitingCount);
        tvAvailableSlots = findViewById(R.id.tvAvailableSlots);
        tvAvgWait = findViewById(R.id.tvAvgWait);

        // Next Car
        tvNextCarPlate = findViewById(R.id.tvNextCarPlate);
        tvNextCarInfo = findViewById(R.id.tvNextCarInfo);

        // Add Car
        etCarPlate = findViewById(R.id.etCarPlate);
        btnAddCar = findViewById(R.id.btnAddCar);
        tvTokenPreview = findViewById(R.id.tvTokenPreview);
        tvPositionPreview = findViewById(R.id.tvPositionPreview);

        // Actions
        btnAllowEntry = findViewById(R.id.btnAllowEntry);
        btnShowEstimates = findViewById(R.id.btnShowEstimates);

        // Navigation
        btnViewQueue = findViewById(R.id.btnViewQueue);
        btnPriority = findViewById(R.id.btnPriority);
        btnRefresh = findViewById(R.id.btnRefresh);
    }

    private void setupClickListeners() {
        btnAddCar.setOnClickListener(v -> addCarToQueue());
        btnAllowEntry.setOnClickListener(v -> allowEntry());
        btnShowEstimates.setOnClickListener(v -> showEstimates());
        btnViewQueue.setOnClickListener(v -> openQueueManagement());
        btnPriority.setOnClickListener(v -> handlePriorityEntry());
        btnRefresh.setOnClickListener(v -> updateUI());

        // Update token preview when typing
        etCarPlate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTokenPreview();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void addCarToQueue() {
        String plateNumber = etCarPlate.getText().toString().trim();

        if (plateNumber.isEmpty()) {
            Toast.makeText(this, "Please enter a car number plate", Toast.LENGTH_SHORT).show();
            return;
        }

        if (queueManager.enqueueCar(plateNumber)) {
            Toast.makeText(this, "Car " + plateNumber + " added to queue!", Toast.LENGTH_SHORT).show();
            etCarPlate.setText("");
            updateUI();
        } else {
            Toast.makeText(this, "Car already in queue or invalid plate", Toast.LENGTH_SHORT).show();
        }
    }

    private void allowEntry() {
        Car nextCar = queueManager.dequeueCar();
        if (nextCar != null) {
            Toast.makeText(this, "Entry allowed for " + nextCar.getNumberPlate(), Toast.LENGTH_LONG).show();
            updateUI();
        } else {
            Toast.makeText(this, "No cars in queue", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEstimates() {
        // Show toast with current estimates or open dialog
        if (queueManager.isEmpty()) {
            Toast.makeText(this, "No cars in queue to estimate", Toast.LENGTH_SHORT).show();
        } else {
            StringBuilder estimates = new StringBuilder("Wait Time Estimates:\n");
            for (Car car : queueManager.getAllCars()) {
                int waitTime = queueManager.estimateWaitingTime(car.getNumberPlate());
                estimates.append(car.getNumberPlate()).append(": ~").append(waitTime).append(" mins\n");
            }

            new AlertDialog.Builder(this)
                    .setTitle("⏱️ Waiting Time Estimates")
                    .setMessage(estimates.toString())
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void openQueueManagement() {
        Intent intent = new Intent(this, QueueManagementActivity.class);
        startActivity(intent);
    }

    private void handlePriorityEntry() {
        // Simple priority entry dialog
        EditText input = new EditText(this);
        input.setHint("Enter emergency vehicle plate");

        new AlertDialog.Builder(this)
                .setTitle("🚨 Priority Entry")
                .setMessage("Enter emergency vehicle number plate:")
                .setView(input)
                .setPositiveButton("Add Priority", (dialog, which) -> {
                    String plate = input.getText().toString().trim();
                    if (!plate.isEmpty()) {
                        if (queueManager.addPriorityCar(plate)) {
                            Toast.makeText(this, "Priority entry added for " + plate, Toast.LENGTH_SHORT).show();
                            updateUI();
                        } else {
                            Toast.makeText(this, "Failed to add priority entry", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateTokenPreview() {
        String nextToken = queueManager.getNextTokenNumber();
        int nextPosition = queueManager.getNextPosition();
        int estimatedWait = queueManager.estimateWaitingTimeForNewCar();

        tvTokenPreview.setText("Next Token: " + nextToken);
        tvPositionPreview.setText("Position: " + nextPosition + getSuffix(nextPosition) +
                " • Est. Wait: ~" + estimatedWait + " minutes");
    }

    private void updateUI() {
        // Update stats
        tvWaitingCount.setText(String.valueOf(queueManager.getQueueSize()));
        tvAvailableSlots.setText(String.valueOf(queueManager.getAvailableSlots()));
        tvAvgWait.setText(String.valueOf((int) queueManager.getAverageWaitingTime()));

        // Update next car display
        Car nextCar = queueManager.peekNextCar();
        if (nextCar != null) {
            tvNextCarPlate.setText("🚗 " + nextCar.getNumberPlate());
            long waitingMins = nextCar.getWaitingTimeMinutes();
            int estimatedLeft = queueManager.estimateWaitingTime(nextCar.getNumberPlate());
            tvNextCarInfo.setText(nextCar.getTokenNumber() + " • Waiting " + waitingMins +
                    " mins • Est: " + estimatedLeft + " mins left");
        } else {
            tvNextCarPlate.setText("No cars waiting");
            tvNextCarInfo.setText("Queue is empty");
        }

        // Update token preview
        updateTokenPreview();

        // Enable/disable buttons based on queue state
        btnAllowEntry.setEnabled(!queueManager.isEmpty());
        btnShowEstimates.setEnabled(!queueManager.isEmpty());
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

    @Override
    protected void onResume() {
        super.onResume();
        updateUI(); // Refresh UI when returning from other activities
    }
}
