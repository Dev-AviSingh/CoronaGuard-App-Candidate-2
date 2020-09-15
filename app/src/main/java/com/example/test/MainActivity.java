package com.example.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

public class MainActivity extends Activity {
    TextView currentHeartRateTextView;
    TextView currentTemperatureTextView;
    TextView maskStatusTextView;
    ImageView maskStatusImageView;
    CircularProgressBar heartRateCircularProgressBar;
    CircularProgressBar temperatureCircularProgressBar;

    private final Handler threadHandler = new Handler();

    int lastHeartRateReading = 0;

    int heartRateUpperLimit = 100;
    int heartRateLowerLimit = 60;

    float lastTemperatureReading = 0; // In Celsius
    float temperatureLowerLimit = 30; // In Celsius
    float temperatureUpperLimit = 60; // In Celsius

    int graphUpdateTimeInterval = 1000; // In milliseconds
    int dataUpdateTimeInterval = 500; // In milliseconds

    Runnable graphUpdaterThread;
    Runnable dataUpdaterThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentHeartRateTextView = findViewById(R.id.heart_rate_result_text_view);
        currentTemperatureTextView = findViewById(R.id.temperature_result_text_view);

        maskStatusImageView = findViewById(R.id.mask_status_image);
        maskStatusTextView = findViewById(R.id.mask_status_text);

        heartRateCircularProgressBar = findViewById(R.id.heart_rate_circular_progress_bar);
        heartRateCircularProgressBar.setProgressMax((float)(heartRateUpperLimit - heartRateLowerLimit));

        temperatureCircularProgressBar = findViewById(R.id.temperature_circular_progress_bar);
        temperatureCircularProgressBar.setProgressMax((temperatureUpperLimit - temperatureLowerLimit));

        onResume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Thread to update graphs
        graphUpdaterThread = new Runnable() {
            @Override
            public void run() {

                // Update the temperature speedometer
                lastTemperatureReading = DataMonitor.getTemperature();
                currentTemperatureTextView.setText(getString(R.string.temperature, lastTemperatureReading));

                // Update the heart Rate graph
                lastHeartRateReading = DataMonitor.getHeartRate();
                heartRateCircularProgressBar.setProgressWithAnimation(lastHeartRateReading - heartRateLowerLimit, (long)graphUpdateTimeInterval);
                temperatureCircularProgressBar.setProgressWithAnimation(lastTemperatureReading - temperatureLowerLimit, (long)graphUpdateTimeInterval);
                currentHeartRateTextView.setText(getString(R.string.heart_rate, lastHeartRateReading));

                int lol = (int)(Math.random() * 2) + 1;
                if(lol == 1){
                    maskStatusImageView.setImageResource(R.drawable.face_silheoutte_masked);
                    maskStatusTextView.setText(R.string.mask_worn_properly);
                }else{
                    maskStatusImageView.setImageResource(R.drawable.face_silheoutte);
                    maskStatusTextView.setText(R.string.mask_not_worn_properly);
                }

                threadHandler.postDelayed(graphUpdaterThread, graphUpdateTimeInterval);
            }
        };

        // Thread to update data
        dataUpdaterThread = new Runnable() {
            @Override
            public void run() {
                DataMonitor.setHeartRate(generateHeartRateData());
                DataMonitor.setTemperature(generateTemperatureData());
                threadHandler.postDelayed(dataUpdaterThread, dataUpdateTimeInterval);

            }
        };

        threadHandler.postDelayed(graphUpdaterThread, graphUpdateTimeInterval);
        threadHandler.postDelayed(dataUpdaterThread, dataUpdateTimeInterval);
    }

    @Override
    protected void onPause() {
        super.onPause();
        threadHandler.removeCallbacks(graphUpdaterThread);
        threadHandler.removeCallbacks(dataUpdaterThread);
    }

    // This is to be received by receive heart rate data
    public int generateHeartRateData(){
        return (int) (Math.random() * ((heartRateUpperLimit - heartRateLowerLimit) + 1) + heartRateLowerLimit);
    }

    // This is to be received by receive temperature data
    public float generateTemperatureData(){
        return (float)(Math.random() * (temperatureUpperLimit - temperatureLowerLimit + 1) + temperatureLowerLimit);
    }
}