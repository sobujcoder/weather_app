package com.example.assignment3;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText cityInput;
    private Button fetchButton;
    private TextView weatherDetails;
    private ProgressBar progressBar;

    private static final String API_KEY = "717c53fcbcbb2efd3219e795fee95784"; // No space
    private static final String TAG = "WeatherApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityInput = findViewById(R.id.cityInput);
        fetchButton = findViewById(R.id.fetchButton);
        weatherDetails = findViewById(R.id.weatherDetails);
        progressBar = findViewById(R.id.progressBar);

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityName = cityInput.getText().toString().trim();
                if (!cityName.isEmpty()) {
                    new FetchWeatherTask().execute(cityName);
                } else {
                    weatherDetails.setText("Please enter a city name.");
                }
            }
        });
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            weatherDetails.setText("");
        }

        @Override
        protected String doInBackground(String... params) {
            String cityName = params[0];
            String urlString = "https://api.openweathermap.org/data/2.5/forecast?q="
                    + cityName
                    + "&units=metric&appid="
                    + API_KEY;

            Log.d(TAG, "Request URL: " + urlString);

            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000); // 10 seconds
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    return response.toString();
                } else {
                    return "Error: Server responded with code " + responseCode;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE);

            if (result.startsWith("Error")) {
                weatherDetails.setText(result);
            } else {
                parseWeatherData(result);
            }
        }
    }

    private void parseWeatherData(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray listArray = jsonObject.getJSONArray("list");

            StringBuilder weatherInfo = new StringBuilder();
            for (int i = 0; i < listArray.length(); i += 8) { // every 24 hours (3h * 8 = 24h)
                JSONObject dayData = listArray.getJSONObject(i);
                String date = dayData.getString("dt_txt");

                JSONObject main = dayData.getJSONObject("main");
                double temp = main.getDouble("temp");

                JSONArray weatherArray = dayData.getJSONArray("weather");
                String weatherDescription = weatherArray.getJSONObject(0).getString("description");

                weatherInfo.append("Date: ").append(date).append("\n")
                        .append("Temperature: ").append(temp).append("°C\n")
                        .append("Weather: ").append(weatherDescription).append("\n\n");
            }

            weatherDetails.setText(weatherInfo.toString());
        } catch (Exception e) {
            e.printStackTrace();
            weatherDetails.setText("Error parsing weather data.");
        }
    }
}
