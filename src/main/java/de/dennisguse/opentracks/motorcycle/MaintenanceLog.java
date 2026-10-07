package de.dennisguse.opentracks.motorcycle;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Data Model: Catatan Servis & Perawatan Motor (Oli, Busi, Rantai, Bensin)
 * Author: Wawang Kurniawan (PT BPR NBP 27)
 */
public class MaintenanceLog {
    private String id;
    private String motorcycleId;
    private long dateMillis;
    private double odometerKm;
    private String category;    // "Ganti Oli Mesin", "Kampas Rem", "Busi", "BBM / Bensin", dll.
    private long costRupiah;
    private String notes;

    public MaintenanceLog(String id, String motorcycleId, long dateMillis, double odometerKm, String category, long costRupiah, String notes) {
        this.id = id;
        this.motorcycleId = motorcycleId;
        this.dateMillis = dateMillis;
        this.odometerKm = odometerKm;
        this.category = category;
        this.costRupiah = costRupiah;
        this.notes = notes;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("motorcycleId", motorcycleId);
        obj.put("dateMillis", dateMillis);
        obj.put("odometerKm", odometerKm);
        obj.put("category", category);
        obj.put("costRupiah", costRupiah);
        obj.put("notes", notes);
        return obj;
    }

    public static MaintenanceLog fromJson(JSONObject obj) throws JSONException {
        return new MaintenanceLog(
                obj.getString("id"),
                obj.optString("motorcycleId", ""),
                obj.optLong("dateMillis", System.currentTimeMillis()),
                obj.optDouble("odometerKm", 0.0),
                obj.optString("category", "Servis Umum"),
                obj.optLong("costRupiah", 0),
                obj.optString("notes", "")
        );
    }

    public String getId() { return id; }
    public String getMotorcycleId() { return motorcycleId; }
    public long getDateMillis() { return dateMillis; }
    public double getOdometerKm() { return odometerKm; }
    public String getCategory() { return category; }
    public long getCostRupiah() { return costRupiah; }
    public String getNotes() { return notes; }
}
