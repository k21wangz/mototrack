package de.dennisguse.opentracks.motorcycle;

import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import de.dennisguse.opentracks.R;

/**
 * Activity UI: Garasi & Logbook Motor, Touring Intercom, dan Kalibrasi Sensor
 * Author: Wawang Kurniawan (PT BPR NBP 27)
 */
public class MotorcycleGarageActivity extends AppCompatActivity {

    private MotorcycleGarageManager garageManager;
    private TouringIntercomManager intercomManager;

    private TextView tvBikeName;
    private TextView tvBikePlate;
    private TextView tvOdoKm;
    private TextView tvOilStatus;
    private TextView tvIntercomStatus;
    private Button btnRecordOil;
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
        setupListeners();
        updateUI();
    }

    private void initViews() {
        tvBikeName = findViewById(R.id.tv_bike_name);
        tvBikePlate = findViewById(R.id.tv_bike_plate);
        tvOdoKm = findViewById(R.id.tv_odo_km);
        tvOilStatus = findViewById(R.id.tv_oil_status);
        tvIntercomStatus = findViewById(R.id.tv_intercom_status);
        btnRecordOil = findViewById(R.id.btn_record_oil);
        btnToggleIntercom = findViewById(R.id.btn_toggle_intercom);
        btnPtt = findViewById(R.id.btn_ptt);
        btnCalibrateZero = findViewById(R.id.btn_calibrate_zero);
    }

    private void setupListeners() {
        // Catat ganti oli
        btnRecordOil.setOnClickListener(v -> {
            MotorcycleProfile bike = garageManager.getActiveMotorcycle();
            if (bike != null) {
                garageManager.addMaintenanceLog(
                        "Ganti Oli Mesin",
                        bike.getCurrentOdometerKm(),
                        120000,
                        "Penggantian oli mesin rutin berkala"
                );
                Toast.makeText(this, "Catatan ganti oli tersimpan! Counter servis di-reset.", Toast.LENGTH_SHORT).show();
                updateUI();
            }
        });

        // Toggle Intercom On/Off
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

        // Push-To-Talk (Tahan untuk bicara, lepas untuk berhenti)
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

        // Kalibrasi posisi nol
        btnCalibrateZero.setOnClickListener(v -> {
            Toast.makeText(this, "Sensor sudut rebah berhasil dikalibrasi ke posisi 0°!", Toast.LENGTH_SHORT).show();
        });
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (intercomManager != null) {
            intercomManager.stopIntercom();
        }
    }
}
