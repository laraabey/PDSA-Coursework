package com.example.parking_lot_entry_tracker;

import com.example.parking_lot_entry_tracker.Car;

import java.util.*;

public class ParkingQueueManager {
    private static ParkingQueueManager instance;
    private Queue<Car> parkingQueue;
    private int tokenCounter;
    private int totalParkingSlots;
    private int occupiedSlots;
    private int averageParkingDurationMinutes;

    private ParkingQueueManager(int totalSlots) {
        this.parkingQueue = new LinkedList<>();
        this.tokenCounter = 1;
        this.totalParkingSlots = totalSlots;
        this.occupiedSlots = 0;
        this.averageParkingDurationMinutes = 5;
    }

    public static synchronized ParkingQueueManager getInstance(int totalSlots) {
        if (instance == null) {
            instance = new ParkingQueueManager(totalSlots);
        }
        return instance;
    }

    public static synchronized ParkingQueueManager getInstance() {
        return instance;
    }

    // All previous methods remain the same...
    public boolean enqueueCar(String numberPlate) {
        if (numberPlate == null || numberPlate.trim().isEmpty()) {
            return false;
        }

        if (findCarByPlate(numberPlate) != null) {
            return false;
        }

        String token = String.format("#%03d", tokenCounter++);
        int position = parkingQueue.size() + 1;
        Car newCar = new Car(numberPlate.toUpperCase().trim(), token, System.currentTimeMillis(), position);

        parkingQueue.offer(newCar);
        updatePositions();
        return true;
    }

    public Car dequeueCar() {
        Car car = parkingQueue.poll();
        if (car != null) {
            occupiedSlots++;
            updatePositions();
        }
        return car;
    }

    public Car peekNextCar() {
        return parkingQueue.peek();
    }

    public boolean removeCar(String numberPlate) {
        Car carToRemove = findCarByPlate(numberPlate);
        if (carToRemove != null) {
            parkingQueue.remove(carToRemove);
            updatePositions();
            return true;
        }
        return false;
    }

    public Car findCarByPlate(String numberPlate) {
        for (Car car : parkingQueue) {
            if (car.getNumberPlate().equalsIgnoreCase(numberPlate.trim())) {
                return car;
            }
        }
        return null;
    }

    public List<Car> getAllCars() {
        return new ArrayList<>(parkingQueue);
    }

    public int getQueueSize() {
        return parkingQueue.size();
    }

    public boolean isEmpty() {
        return parkingQueue.isEmpty();
    }

    public int estimateWaitingTime(String numberPlate) {
        Car car = findCarByPlate(numberPlate);
        if (car == null) {
            return -1;
        }

        int position = car.getPosition();
        int estimatedMinutes = (position - 1) * averageParkingDurationMinutes;
        return estimatedMinutes;
    }

    public int estimateWaitingTimeForNewCar() {
        int queueSize = getQueueSize();
        return queueSize * averageParkingDurationMinutes;
    }

    public String getNextTokenNumber() {
        return String.format("#%03d", tokenCounter);
    }

    public int getNextPosition() {
        return parkingQueue.size() + 1;
    }

    private void updatePositions() {
        int position = 1;
        for (Car car : parkingQueue) {
            car.setPosition(position++);
        }
    }

    public void releaseCarFromParking() {
        if (occupiedSlots > 0) {
            occupiedSlots--;
        }
    }

    public int getAvailableSlots() {
        return totalParkingSlots - occupiedSlots;
    }

    public int getAverageParkingDuration() {
        return averageParkingDurationMinutes;
    }

    public void setAverageParkingDuration(int minutes) {
        this.averageParkingDurationMinutes = minutes;
    }

    public double getAverageWaitingTime() {
        if (parkingQueue.isEmpty()) return 0;

        long totalWaitTime = 0;
        for (Car car : parkingQueue) {
            totalWaitTime += car.getWaitingTimeMinutes();
        }
        return (double) totalWaitTime / parkingQueue.size();
    }

    public boolean addPriorityCar(String numberPlate) {
        if (numberPlate == null || numberPlate.trim().isEmpty()) {
            return false;
        }

        String token = String.format("#P%03d", tokenCounter++);
        Car priorityCar = new Car(numberPlate.toUpperCase().trim(), token, System.currentTimeMillis(), 1);
        priorityCar.setPriority(true);

        Queue<Car> tempQueue = new LinkedList<>();
        tempQueue.offer(priorityCar);
        tempQueue.addAll(parkingQueue);
        parkingQueue = tempQueue;

        updatePositions();
        return true;
    }
}
