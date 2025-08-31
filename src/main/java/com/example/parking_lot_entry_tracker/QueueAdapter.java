package com.example.parking_lot_entry_tracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.QueueViewHolder> {
    private List<Car> carList;
    private Context context;
    private ParkingQueueManager queueManager;

    public QueueAdapter(Context context, List<Car> carList, ParkingQueueManager queueManager) {
        this.context = context;
        this.carList = carList;
        this.queueManager = queueManager;
    }

    @NonNull
    @Override
    public QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.queue_item, parent, false);
        return new QueueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {
        Car car = carList.get(position);

        // Set car plate with emoji
        String plateText = "🚗 " + car.getNumberPlate();
        if (car.isPriority()) {
            plateText = "🚨 " + car.getNumberPlate() + " (PRIORITY)";
        }
        holder.tvCarPlate.setText(plateText);

        // Set car details
        String details = car.getPosition() + getSuffix(car.getPosition()) + " • " +
                car.getTokenNumber() + " • " + car.getFormattedArrivalTime();
        holder.tvCarDetails.setText(details);

        // Set waiting time
        holder.tvWaitingTime.setText(car.getWaitingTimeMinutes() + "m");

        // Set estimated remaining time
        int estimatedWait = queueManager.estimateWaitingTime(car.getNumberPlate());
        if (estimatedWait >= 0) {
            holder.tvEstimatedRemaining.setText("~" + estimatedWait + "m left");
        } else {
            holder.tvEstimatedRemaining.setText("~0m left");
        }

        // Highlight next car
        if (position == 0) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.next_car_background));
            holder.viewBorder.setBackgroundColor(ContextCompat.getColor(context, R.color.success_color));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.normal_background));
            holder.viewBorder.setBackgroundColor(ContextCompat.getColor(context, R.color.primary_color));
        }
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public void updateQueue(List<Car> newCarList) {
        this.carList = newCarList;
        notifyDataSetChanged();
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

    static class QueueViewHolder extends RecyclerView.ViewHolder {
        TextView tvCarPlate, tvCarDetails, tvWaitingTime, tvEstimatedRemaining;
        View viewBorder;

        public QueueViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCarPlate = itemView.findViewById(R.id.tvCarPlate);
            tvCarDetails = itemView.findViewById(R.id.tvCarDetails);
            tvWaitingTime = itemView.findViewById(R.id.tvWaitingTime);
            tvEstimatedRemaining = itemView.findViewById(R.id.tvEstimatedRemaining);
            viewBorder = itemView.findViewById(R.id.viewBorder);
        }
    }
}
