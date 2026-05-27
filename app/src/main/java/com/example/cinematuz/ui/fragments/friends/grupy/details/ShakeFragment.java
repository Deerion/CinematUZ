package com.example.cinematuz.ui.fragments.friends.grupy.details;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.cinematuz.R;
import com.example.cinematuz.data.models.MediaItem;

import java.util.ArrayList;
import java.util.Random;

/**
 * Fragment obsługujący losowanie filmu poprzez potrząsanie telefonem.
 * Wykorzystuje akcelerometr do wykrywania ruchu i wibracje jako informację zwrotną.
 */
public class ShakeFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Vibrator vibrator;

    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7F;
    private static final int SHAKE_SLOP_TIME_MS = 500;
    private long mShakeTimestamp;
    private int shakeCount = 0;

    private ArrayList<MediaItem> eligibleMovies;
    private String groupId;
    private TextView tvShakeSubtitle;

    /**
     * Inicjalizuje dane wejściowe (lista filmów do losowania).
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eligibleMovies = (ArrayList<MediaItem>) getArguments().getSerializable("ELIGIBLE_MOVIES");
            groupId = getArguments().getString("GROUP_ID");
        }
    }

    /**
     * Inicjalizuje widok oraz usługi systemowe (SensorManager, Vibrator).
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shake, container, false);
        tvShakeSubtitle = view.findViewById(R.id.tvShakeSubtitle);

        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);

        return view;
    }

    /**
     * Obsługuje zmiany wartości z akcelerometru. Wykrywa potrząśnięcie i zlicza je.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0] / SensorManager.GRAVITY_EARTH;
            float y = event.values[1] / SensorManager.GRAVITY_EARTH;
            float z = event.values[2] / SensorManager.GRAVITY_EARTH;
            float gForce = (float) Math.sqrt(x * x + y * y + z * z);

            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                final long now = System.currentTimeMillis();
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) return;
                mShakeTimestamp = now;

                shakeCount++;
                vibratePhone();

                if (tvShakeSubtitle != null) {
                    if (shakeCount == 1) tvShakeSubtitle.setText("Jeszcze 2 razy...");
                    else if (shakeCount == 2) tvShakeSubtitle.setText("Jeszcze 1 raz!");
                    else if (shakeCount >= 3) finishShakeAndNavigate();
                }
            }
        }
    }

    /**
     * Wywołuje krótką wibrację telefonu.
     */
    private void vibratePhone() {
        if (vibrator == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, 255));
            } else {
                vibrator.vibrate(200);
            }
        } catch (Exception ignored) {}
    }

    /**
     * Kończy proces losowania po osiągnięciu wymaganej liczby potrząśnięć.
     * Losuje film z listy i aktualizuje dane w Firestore (jeśli losowanie odbywa się w grupie).
     */
    private void finishShakeAndNavigate() {
        if (shakeCount > 3) return;
        sensorManager.unregisterListener(this);

        if (eligibleMovies != null && !eligibleMovies.isEmpty()) {
            MediaItem winner = eligibleMovies.get(new Random().nextInt(eligibleMovies.size()));
            
            if (groupId != null) {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("groups").document(groupId)
                        .update("winnerId", String.valueOf(winner.getId()), "winnerReason", "Decyzja losu")
                        .addOnCompleteListener(task -> {
                            navigateToWinnerLocal(winner);
                        });
            } else {
                navigateToWinnerLocal(winner);
            }
        }
    }

    /**
     * Przenosi użytkownika do fragmentu wyświetlającego zwycięzcę.
     * 
     * @param winner Wylosowany film.
     */
    private void navigateToWinnerLocal(MediaItem winner) {
        GroupDetailsFragment.lastSeenWinnerId = String.valueOf(winner.getId());

        Bundle b = new Bundle();
        b.putSerializable("WINNER_MOVIE", winner);
        b.putString("WINNER_REASON", "Decyzja losu");
        b.putSerializable("ELIGIBLE_MOVIES", eligibleMovies);
        if (groupId != null) {
            b.putString("GROUP_ID", groupId);
            b.putBoolean("IS_ADMIN", true);
        }

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.winnerFragment, b);
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    /**
     * Rejestruje nasłuchiwanie sensora przy wznowieniu fragmentu.
     */
    @Override
    public void onResume() {
        super.onResume();
        shakeCount = 0;
        if (tvShakeSubtitle != null) tvShakeSubtitle.setText("Potrząśnij telefonem 3 razy");
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    /**
     * Wyrejestrowuje nasłuchiwanie sensora przy wstrzymaniu fragmentu.
     */
    @Override
    public void onPause() {
        if (sensorManager != null) sensorManager.unregisterListener(this);
        super.onPause();
    }
}