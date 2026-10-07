package de.dennisguse.opentracks.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Lean Angle (Sudut Rebah) Sensor Manager for Motorcycle Touring
 * Measures roll angle using Android hardware rotation vector (IMU sensor fusion).
 *
 * Developer: Wawang Kurniawan (PT BPR NBP 27)
 */
public class LeanAngleSensor implements SensorEventListener {
    private static final String TAG = "LeanAngleSensor";

    public interface OnLeanAngleListener {
        void onLeanAngleChanged(float leanAngleDegrees, float maxLeftDegrees, float maxRightDegrees);
    }

    private final SensorManager sensorManager;
    private final Sensor rotationSensor;
    private OnLeanAngleListener listener;

    private float zeroOffsetDegrees = 0.0f;
    private float currentLeanAngle = 0.0f;
    private float maxLeanLeft = 0.0f;
    private float maxLeanRight = 0.0f;
    private boolean isRunning = false;

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    public LeanAngleSensor(Context context) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = null;
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            if (sensor == null) {
                // Fallback to orientation or accelerometer if rotation vector is unavailable
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                Log.w(TAG, "TYPE_ROTATION_VECTOR not found, fallback to ACCELEROMETER");
            }
        }
        this.rotationSensor = sensor;
    }

    public void setListener(OnLeanAngleListener listener) {
        this.listener = listener;
    }

    public synchronized void start() {
        if (!isRunning && sensorManager != null && rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI);
            isRunning = true;
            Log.d(TAG, "LeanAngleSensor started");
        }
    }

    public synchronized void stop() {
        if (isRunning && sensorManager != null) {
            sensorManager.unregisterListener(this);
            isRunning = false;
            Log.d(TAG, "LeanAngleSensor stopped");
        }
    }

    /**
     * Kalibrasi posisi nol saat motor tegak lurus di phone holder
     */
    public void calibrateZero() {
        this.zeroOffsetDegrees = this.currentLeanAngle + this.zeroOffsetDegrees;
        Log.i(TAG, "Calibrated zero offset to: " + zeroOffsetDegrees);
    }

    public void resetMaxRecords() {
        this.maxLeanLeft = 0.0f;
        this.maxLeanRight = 0.0f;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            SensorManager.getOrientation(rotationMatrix, orientationAngles);

            // orientationAngles[2] adalah Roll (kemiringan kiri-kanan) dalam radian
            float rawRollDegrees = (float) Math.toDegrees(orientationAngles[2]);
            this.currentLeanAngle = rawRollDegrees - zeroOffsetDegrees;

            // Catat rekor rebah kiri (nilai minus) dan rebah kanan (nilai plus)
            if (this.currentLeanAngle < 0) {
                float leftAbs = Math.abs(this.currentLeanAngle);
                if (leftAbs > maxLeanLeft) {
                    maxLeanLeft = leftAbs;
                }
            } else {
                if (this.currentLeanAngle > maxLeanRight) {
                    maxLeanRight = this.currentLeanAngle;
                }
            }

            if (listener != null) {
                listener.onLeanAngleChanged(this.currentLeanAngle, this.maxLeanLeft, this.maxLeanRight);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No-op
    }

    public float getCurrentLeanAngle() {
        return currentLeanAngle;
    }

    public float getMaxLeanLeft() {
        return maxLeanLeft;
    }

    public float getMaxLeanRight() {
        return maxLeanRight;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
