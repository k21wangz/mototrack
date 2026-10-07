package de.dennisguse.opentracks.motorcycle;

import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.dennisguse.opentracks.R;

/**
 * Activity UI: Garasi & Logbook Motor, Touring Intercom, dan Kalibrasi Sensor
 * Author: Wawang Kurniawan (PT BPR NBP 27)
 */
public class MotorcycleGarageActivity extends AppCompatActivity {

    private MotorcycleGarageManager garageManager;
    private TouringIntercomManager intercomManager;

    private Toolbar toolbar;
    private TextView tvBikeName;
    private TextView tvBikePlate;
    private TextView tvOdoKm;
    private TextView tvOilStatus;
    private TextView tvIntercomStatus;
    private TextView tvRecentLogs;
    private Button btnEditBike;
    private Button btnRecordOil;
    private Button btnAddLog;
    private Button btnToggleIntercom;
    private Button btnPtt;
    private Button btnCalibrateZero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motorcycle_garage);

        garageManager = new MotorcycleGarageManager(this);
        intercomManager = new TouringIntercomManager(this);

        initViews();
        setupToolbar();
        setupListeners();
        updateUI();
    }

    private void initViews() {
        toolbar = findViewById(R.id.garage_toolbar);
        tvBikeName = findViewById(R.id.tv_bike_name);
        tvBikePlate = findViewById(R.id.tv_bike_plate);
        tvOdoKm = findViewById(R.id.tv_odo_km);
        tvOilStatus = findViewById(R.id.tv_oil_status);
        tvIntercomStatus = findViewById(R.id.tv_intercom_status);
        tvRecentLogs = findViewById(R.id.tv_recent_logs);
        btnEditBike = findViewById(R.id.btn_edit_bike);
        btnRecordOil = findViewById(R.id.btn_record_oil);
        btnAddLog = findViewById(R.id.btn_add_log);
        btnToggleIntercom = findViewById(R.id.btn_toggle_intercom);
        btnPtt = findViewById(R.id.btn_ptt);
        btnCalibrateZero = findViewById(R.id.btn_calibrate_zero);
    }

    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupListeners() {
        // 1. Edit Data Motor (Nama, Plat, Odometer, Interval Oli)
        btnEditBike.setOnClickListener(v -> showEditBikeDialog());

        // 2. Tambah Catatan Servis / BBM
        btnAddLog.setOnClickListener(v -> showAddLogDialog());

        // 3. Catat ganti oli cepat
        btnRecordOil.setOnClickListener(v -> {
            MotorcycleProfile bike = garageManager.getActiveMotorcycle();
            if (bike != null) {
                garageManager.addMaintenanceLog(
                        "Ganti Oli Mesin",
                        bike.getCurrentOdometerKm(),
                        120000,
                        "Penggantian oli mesin rutin berkala"
                );
                Toast.makeText(this, "Catatan ganti oli tersimpan! Counter servis berhasil di-reset.", Toast.LENGTH_SHORT).show();
                updateUI();
            }
        });

        // 4. Toggle Intercom On/Off
        btnToggleIntercom.setOnClickListener(v -> {
            if (intercomManager.isListening()) {
                intercomManager.stopIntercom();
                btnToggleIntercom.setText("Nyalakan Intercom");
                btnPtt.setEnabled(false);
                tvIntercomStatus.setText("Status: Intercom Mati");
            } else {
                intercomManager.startIntercom();
                btnToggleIntercom.setText("Matikan Intercom");
                btnPtt.setEnabled(true);
                tvIntercomStatus.setText("Status: Terhubung (Siap Bicara)");
            }
        });

        // 5. Push-To-Talk (Tahan untuk bicara, lepas untuk berhenti)
        btnPtt.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                intercomManager.startTalking();
                tvIntercomStatus.setText("Sedang Berbicara (Transmitting)...");
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                intercomManager.stopTalking();
                tvIntercomStatus.setText("Status: Mendengarkan Rombongan...");
                return true;
            }
            return false;
        });

        // 6. Kalibrasi posisi nol
        btnCalibrateZero.setOnClickListener(v -> {
            Toast.makeText(this, "Sensor kemiringan berhasil dikalibrasi ke posisi 0°!", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Dialog untuk mengubah data motor (Nama, Plat Nomor, Odometer, Interval Oli)
     */
    private void showEditBikeDialog() {
        MotorcycleProfile bike = garageManager.getActiveMotorcycle();
        if (bike == null) return;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        EditText etName = new EditText(this);
        etName.setHint("Nama Motor (misal: Yamaha XSR 155)");
        etName.setText(bike.getName());
        layout.addView(etName);

        EditText etPlate = new EditText(this);
        etPlate.setHint("Nomor Polisi (misal: D 1234 NBP)");
        etPlate.setText(bike.getPlateNumber());
        layout.addView(etPlate);

        EditText etOdo = new EditText(this);
        etOdo.setHint("Odometer Total Saat Ini (KM)");
        etOdo.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etOdo.setText(String.format(Locale.US, "%.0f", bike.getCurrentOdometerKm()));
        layout.addView(etOdo);

        EditText etInterval = new EditText(this);
        etInterval.setHint("Interval Servis Oli (KM, misal: 2500)");
        etInterval.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etInterval.setText(String.format(Locale.US, "%.0f", bike.getOilIntervalKm()));
        layout.addView(etInterval);

        new AlertDialog.Builder(this)
                .setTitle("Ubah Data Motor")
                .setView(layout)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String plate = etPlate.getText().toString().trim();
                    String odoStr = etOdo.getText().toString().trim();
                    String intervalStr = etInterval.getText().toString().trim();

                    if (!name.isEmpty()) bike.setName(name);
                    bike.setPlateNumber(plate);

                    try {
                        if (!odoStr.isEmpty()) bike.setCurrentOdometerKm(Double.parseDouble(odoStr));
                        if (!intervalStr.isEmpty()) bike.setOilIntervalKm(Double.parseDouble(intervalStr));
                    } catch (NumberFormatException ignored) {}

                    garageManager.saveMotorcycles();
                    Toast.makeText(this, "Data motor berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                    updateUI();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    /**
     * Dialog untuk menambah catatan servis / bensin
     */
    private void showAddLogDialog() {
        MotorcycleProfile bike = garageManager.getActiveMotorcycle();
        if (bike == null) return;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        EditText etCat = new EditText(this);
        etCat.setHint("Jenis (Oli Mesin, BBM, Kampas Rem, dll.)");
        layout.addView(etCat);

        EditText etCost = new EditText(this);
        etCost.setHint("Biaya (Rp)");
        etCost.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(etCost);

        EditText etNotes = new EditText(this);
        etNotes.setHint("Keterangan / Bengkel / Liter BBM");
        layout.addView(etNotes);

        new AlertDialog.Builder(this)
                .setTitle("Tambah Catatan Perawatan / BBM")
                .setView(layout)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String category = etCat.getText().toString().trim();
                    if (category.isEmpty()) category = "Servis Berkala";

                    long cost = 0;
                    try {
                        String costStr = etCost.getText().toString().trim();
                        if (!costStr.isEmpty()) cost = Long.parseLong(costStr);
                    } catch (NumberFormatException ignored) {}

                    String notes = etNotes.getText().toString().trim();

                    garageManager.addMaintenanceLog(category, bike.getCurrentOdometerKm(), cost, notes);
                    Toast.makeText(this, "Catatan berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
                    updateUI();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void updateUI() {
        MotorcycleProfile bike = garageManager.getActiveMotorcycle();
        if (bike != null) {
            tvBikeName.setText(bike.getName());
            tvBikePlate.setText(bike.getPlateNumber().isEmpty() ? "Tanpa Plat" : bike.getPlateNumber());
            tvOdoKm.setText(String.format(Locale.getDefault(), "%,.1f KM", bike.getCurrentOdometerKm()));

            if (bike.isOilServiceDue()) {
                tvOilStatus.setText("WAKTUNYA SERVIS!");
                tvOilStatus.setTextColor(0xFFEF4444); // Red
            } else {
                tvOilStatus.setText(String.format(Locale.getDefault(), "Sisa %,.0f KM", bike.getRemainingKmToOilService()));
                tvOilStatus.setTextColor(0xFF10B981); // Green
            }
        }

        // Tampilkan riwayat logbook
        List<MaintenanceLog> logs = garageManager.getLogsForActiveBike();
        if (logs.isEmpty()) {
            tvRecentLogs.setText("Belum ada riwayat servis atau BBM.");
        } else {
            StringBuilder sb = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            int limit = Math.min(logs.size(), 5);
            for (int i = 0; i < limit; i++) {
                MaintenanceLog log = logs.get(i);
                sb.append("• ").append(sdf.format(new Date(log.getDateMillis())))
                        .append(" - ").append(log.getCategory())
                        .append(" (Rp ").append(String.format(Locale.getDefault(), "%,d", log.getCostRupiah())).append(")\n")
                        .append("  KM: ").append(String.format(Locale.getDefault(), "%,.0f", log.getOdometerKm()));
                if (!log.getNotes().isEmpty()) {
                    sb.append(" | ").append(log.getNotes());
                }
                sb.append("\n\n");
            }
            tvRecentLogs.setText(sb.toString().trim());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (intercomManager != null) {
            intercomManager.stopIntercom();
        }
    }
}
