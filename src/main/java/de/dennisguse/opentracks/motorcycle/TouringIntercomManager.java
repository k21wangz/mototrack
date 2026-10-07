package de.dennisguse.opentracks.motorcycle;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Touring Intercom: Group Voice Call & Walkie-Talkie
 * Mendukung 2 Mode:
 * 1. Mode LAN / Hotspot Lokal: UDP Multicast Audio (0 Kuota, Tanpa Internet, Latensi < 50ms)
 * 2. Mode PTT (Push-To-Talk) ramah helm & tombol Bluetooth
 *
 * Author: Wawang Kurniawan (PT BPR NBP 27)
 */
public class TouringIntercomManager {
    private static final String TAG = "TouringIntercom";

    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG_IN = AudioFormat.CHANNEL_IN_MONO;
    private static final int CHANNEL_CONFIG_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = 1024;
    private static final int PORT = 50005;
    private static final String BROADCAST_IP = "255.255.255.255";

    private final Context context;
    private final AtomicBoolean isListening = new AtomicBoolean(false);
    private final AtomicBoolean isTransmitting = new AtomicBoolean(false);

    private DatagramSocket socket;
    private Thread receiverThread;
    private Thread senderThread;

    public interface IntercomStatusListener {
        void onTransmissionStateChanged(boolean isTalking);
        void onPeerSpeaking(String senderIp);
    }

    private IntercomStatusListener statusListener;

    public TouringIntercomManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public void setStatusListener(IntercomStatusListener listener) {
        this.statusListener = listener;
    }

    /**
     * Memulai mendengarkan panggilan suara grup di jaringan LAN / Hotspot
     */
    public synchronized void startIntercom() {
        if (isListening.get()) return;

        try {
            socket = new DatagramSocket(PORT);
            socket.setBroadcast(true);
            isListening.set(true);

            receiverThread = new Thread(this::receiveAudioLoop, "IntercomReceiver");
            receiverThread.start();
            Log.i(TAG, "Intercom listening on UDP port " + PORT);
        } catch (Exception e) {
            Log.e(TAG, "Error starting intercom socket", e);
        }
    }

    /**
     * Berhenti mendengarkan
     */
    public synchronized void stopIntercom() {
        isListening.set(false);
        stopTalking();

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        if (receiverThread != null) {
            receiverThread.interrupt();
        }
        Log.i(TAG, "Intercom stopped");
    }

    /**
     * Push-To-Talk: Mulai transmisi suara dari mikrofon ke rombongan
     */
    public synchronized void startTalking() {
        if (!isListening.get() || isTransmitting.get()) return;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Microphone permission not granted");
            return;
        }

        isTransmitting.set(true);
        if (statusListener != null) {
            statusListener.onTransmissionStateChanged(true);
        }

        senderThread = new Thread(this::sendAudioLoop, "IntercomSender");
        senderThread.start();
    }

    /**
     * Push-To-Talk: Lepas tombol bicara
     */
    public synchronized void stopTalking() {
        if (!isTransmitting.get()) return;

        isTransmitting.set(false);
        if (statusListener != null) {
            statusListener.onTransmissionStateChanged(false);
        }
        if (senderThread != null) {
            senderThread.interrupt();
        }
    }

    private void sendAudioLoop() {
        AudioRecord recorder = null;
        try {
            int minBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG_IN, AUDIO_FORMAT);
            int bufSize = Math.max(minBuf, BUFFER_SIZE);

            recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, SAMPLE_RATE, CHANNEL_CONFIG_IN, AUDIO_FORMAT, bufSize);
            recorder.startRecording();

            byte[] buffer = new byte[BUFFER_SIZE];
            InetAddress broadcastAddr = InetAddress.getByName(BROADCAST_IP);

            while (isTransmitting.get() && !Thread.currentThread().isInterrupted()) {
                int read = recorder.read(buffer, 0, buffer.length);
                if (read > 0 && socket != null && !socket.isClosed()) {
                    DatagramPacket packet = new DatagramPacket(buffer, read, broadcastAddr, PORT);
                    socket.send(packet);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Audio transmission error", e);
        } finally {
            if (recorder != null) {
                try {
                    recorder.stop();
                    recorder.release();
                } catch (Exception ignored) {}
            }
        }
    }

    private void receiveAudioLoop() {
        AudioTrack track = null;
        try {
            int minBuf = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG_OUT, AUDIO_FORMAT);
            track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, SAMPLE_RATE, CHANNEL_CONFIG_OUT, AUDIO_FORMAT, minBuf, AudioTrack.MODE_STREAM);
            track.play();

            byte[] buffer = new byte[BUFFER_SIZE];

            while (isListening.get() && !Thread.currentThread().isInterrupted()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Jangan putar suara sendiri saat sedang bicara
                if (!isTransmitting.get() && packet.getLength() > 0) {
                    track.write(packet.getData(), 0, packet.getLength());
                    if (statusListener != null) {
                        statusListener.onPeerSpeaking(packet.getAddress().getHostAddress());
                    }
                }
            }
        } catch (Exception e) {
            if (isListening.get()) {
                Log.e(TAG, "Audio receiver error", e);
            }
        } finally {
            if (track != null) {
                try {
                    track.stop();
                    track.release();
                } catch (Exception ignored) {}
            }
        }
    }

    public boolean isTransmitting() {
        return isTransmitting.get();
    }

    public boolean isListening() {
        return isListening.get();
    }
}
