package de.dennisguse.opentracks.motorcycle;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Data Model: Motorcycle Profile
 * Author: Wawang Kurniawan (PT BPR NBP 27)
 */
public class MotorcycleProfile {
    private String id;
    private String name;           // Contoh: "Honda CB500X", "Yamaha XSR 155"
    private String plateNumber;    // Nomor Polisi
    private double currentOdometerKm;
    private double oilIntervalKm;  // Misal: 2500 KM
    private double lastOilChangeOdoKm;

    public MotorcycleProfile(String id, String name, String plateNumber, double currentOdometerKm, double oilIntervalKm) {
        this.id = id;
        this.name = name;
        this.plateNumber = plateNumber;
        this.currentOdometerKm = currentOdometerKm;
        this.oilIntervalKm = oilIntervalKm;
        this.lastOilChangeOdoKm = currentOdometerKm;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("name", name);
        obj.put("plateNumber", plateNumber);
        obj.put("currentOdometerKm", currentOdometerKm);
        obj.put("oilIntervalKm", oilIntervalKm);
        obj.put("lastOilChangeOdoKm", lastOilChangeOdoKm);
        return obj;
    }

    public static MotorcycleProfile fromJson(JSONObject obj) throws JSONException {
        MotorcycleProfile p = new MotorcycleProfile(
                obj.getString("id"),
                obj.getString("name"),
                obj.optString("plateNumber", ""),
                obj.optDouble("currentOdometerKm", 0.0),
                obj.optDouble("oilIntervalKm", 2500.0)
        );
        p.lastOilChangeOdoKm = obj.optDouble("lastOilChangeOdoKm", p.currentOdometerKm);
        return p;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    public double getCurrentOdometerKm() { return currentOdometerKm; }
    public void addDistanceKm(double km) { this.currentOdometerKm += km; }
    public double getOilIntervalKm() { return oilIntervalKm; }
    public void setOilIntervalKm(double oilIntervalKm) { this.oilIntervalKm = oilIntervalKm; }
    public double getLastOilChangeOdoKm() { return lastOilChangeOdoKm; }
    public void recordOilChange() { this.lastOilChangeOdoKm = this.currentOdometerKm; }

    public boolean isOilServiceDue() {
        return (currentOdometerKm - lastOilChangeOdoKm) >= oilIntervalKm;
    }

    public double getRemainingKmToOilService() {
        double diff = (lastOilChangeOdoKm + oilIntervalKm) - currentOdometerKm;
        return Math.max(0.0, diff);
    }
}
