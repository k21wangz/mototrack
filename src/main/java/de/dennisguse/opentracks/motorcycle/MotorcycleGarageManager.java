package de.dennisguse.opentracks.motorcycle;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manager untuk Mengelola Garasi Motor dan Catatan Pemeliharaan
 * Author: Wawang Kurniawan (PT BPR NBP 27)
 */
public class MotorcycleGarageManager {
    private static final String TAG = "MotorcycleGarageManager";
    private static final String PREF_NAME = "mototrack_garage_prefs";
    private static final String KEY_MOTORCYCLES = "motorcycles_json";
    private static final String KEY_LOGS = "maintenance_logs_json";
    private static final String KEY_ACTIVE_BIKE_ID = "active_bike_id";

    private final SharedPreferences prefs;
    private final List<MotorcycleProfile> motorcycles = new ArrayList<>();
    private final List<MaintenanceLog> logs = new ArrayList<>();
    private String activeBikeId = "";

    public MotorcycleGarageManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadData();
    }

    private void loadData() {
        motorcycles.clear();
        logs.clear();

        // 1. Load Motorcycles
        String bikesJson = prefs.getString(KEY_MOTORCYCLES, null);
        if (bikesJson != null) {
            try {
                JSONArray arr = new JSONArray(bikesJson);
                for (int i = 0; i < arr.length(); i++) {
                    motorcycles.add(MotorcycleProfile.fromJson(arr.getJSONObject(i)));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing motorcycles JSON", e);
            }
        }

        // Default: Buat motor contoh jika masih kosong
        if (motorcycles.isEmpty()) {
            MotorcycleProfile defaultBike = new MotorcycleProfile(
                    "bike-1",
                    "Touring Bike (Default)",
                    "D 2727 NBP",
                    12500.0,
                    2500.0
            );
            motorcycles.add(defaultBike);
            activeBikeId = defaultBike.getId();
            saveMotorcycles();
        } else {
            activeBikeId = prefs.getString(KEY_ACTIVE_BIKE_ID, motorcycles.get(0).getId());
        }

        // 2. Load Logs
        String logsJson = prefs.getString(KEY_LOGS, null);
        if (logsJson != null) {
            try {
                JSONArray arr = new JSONArray(logsJson);
                for (int i = 0; i < arr.length(); i++) {
                    logs.add(MaintenanceLog.fromJson(arr.getJSONObject(i)));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing maintenance logs JSON", e);
            }
        }
    }

    public synchronized void saveMotorcycles() {
        try {
            JSONArray arr = new JSONArray();
            for (MotorcycleProfile p : motorcycles) {
                arr.put(p.toJson());
            }
            prefs.edit()
                    .putString(KEY_MOTORCYCLES, arr.toString())
                    .putString(KEY_ACTIVE_BIKE_ID, activeBikeId)
                    .apply();
        } catch (JSONException e) {
            Log.e(TAG, "Error saving motorcycles", e);
        }
    }

    public synchronized void saveLogs() {
        try {
            JSONArray arr = new JSONArray();
            for (MaintenanceLog log : logs) {
                arr.put(log.toJson());
            }
            prefs.edit().putString(KEY_LOGS, arr.toString()).apply();
        } catch (JSONException e) {
            Log.e(TAG, "Error saving logs", e);
        }
    }

    public List<MotorcycleProfile> getMotorcycles() {
        return new ArrayList<>(motorcycles);
    }

    public MotorcycleProfile getActiveMotorcycle() {
        for (MotorcycleProfile p : motorcycles) {
            if (p.getId().equals(activeBikeId)) {
                return p;
            }
        }
        return motorcycles.isEmpty() ? null : motorcycles.get(0);
    }

    public void setActiveBikeId(String bikeId) {
        this.activeBikeId = bikeId;
        prefs.edit().putString(KEY_ACTIVE_BIKE_ID, bikeId).apply();
    }

    public void addMotorcycle(String name, String plateNumber, double initialOdoKm, double oilIntervalKm) {
        MotorcycleProfile newBike = new MotorcycleProfile(
                UUID.randomUUID().toString(),
                name,
                plateNumber,
                initialOdoKm,
                oilIntervalKm
        );
        motorcycles.add(newBike);
        if (motorcycles.size() == 1) {
            activeBikeId = newBike.getId();
        }
        saveMotorcycles();
    }

    /**
     * Tambahkan jarak tempuh hasil touring ke odometer motor yang sedang aktif
     */
    public void addTouringDistance(double distanceKm) {
        MotorcycleProfile active = getActiveMotorcycle();
        if (active != null && distanceKm > 0) {
            active.addDistanceKm(distanceKm);
            saveMotorcycles();
            Log.i(TAG, "Added " + distanceKm + " KM to bike " + active.getName() + ", new Odo: " + active.getCurrentOdometerKm());
        }
    }

    public void addMaintenanceLog(String category, double odometerKm, long costRupiah, String notes) {
        MotorcycleProfile active = getActiveMotorcycle();
        String bikeId = active != null ? active.getId() : "default";

        MaintenanceLog log = new MaintenanceLog(
                UUID.randomUUID().toString(),
                bikeId,
                System.currentTimeMillis(),
                odometerKm,
                category,
                costRupiah,
                notes
        );
        logs.add(0, log); // Add to beginning (newest first)
        saveLogs();

        // Jika servis adalah ganti oli, reset counter oli pada profil motor
        if (category.toLowerCase().contains("oli") && active != null) {
            active.recordOilChange();
            saveMotorcycles();
        }
    }

    public List<MaintenanceLog> getLogsForActiveBike() {
        List<MaintenanceLog> result = new ArrayList<>();
        MotorcycleProfile active = getActiveMotorcycle();
        if (active == null) return result;

        for (MaintenanceLog log : logs) {
            if (log.getMotorcycleId().equals(active.getId())) {
                result.add(log);
            }
        }
        return result;
    }
}
