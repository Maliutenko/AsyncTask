package tonymaluk.com.asynctask;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Tony on 06.04.2017.
 */

public class RequestAsync {
    private final String LOG_TAG = RequestAsync.class.getSimpleName();
    private RequestAsync.IResultListener listener;

    public RequestAsync(RequestAsync.IResultListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //test for intenet connection
    }

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String json = null;

        if (params[3] == "POST") {
            try {
                URL url = new URL(params[0] + params[1]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                String urlParameters = params[2];

                // Send post request
                urlConnection.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                json = response.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                listener.onError(e.getMessage());
            } finally {
                try {
                    if (urlConnection != null) urlConnection.disconnect();
                    if (reader != null) reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                    listener.onError(e.getMessage());
                }
            }
        }

        else if (params[3] == "GET") {
            try {
                URL url = new URL(params[0] + params[1] + params[2]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    listener.onError("Read the input stream error");
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null)
                    buffer.append(line + "\n");

                if (buffer.length() == 0) {
                    listener.onError("Server response is empty");
                    return null;
                }
                json = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                listener.onError(e.getMessage());
            } finally {
                try {
                    if (urlConnection != null) urlConnection.disconnect();
                    if (reader != null) reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                    listener.onError(e.getMessage());
                }
            }
        }
        return json;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result == null) {
            return;
        }
        listener.onResult(result);
    }

    public interface IResultListener {
        void onResult(String result);
        void onError(String error);

    }
}
