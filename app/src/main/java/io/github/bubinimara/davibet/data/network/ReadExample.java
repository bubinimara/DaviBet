package io.github.bubinimara.davibet.data.network;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import io.github.bubinimara.davibet.data.model.Tweet;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by Davide Parise on 12/11/21.
 */
public class ReadExample implements Callback<ResponseBody> {
    private static final String TAG = "ReadJson";
    Gson gson;
    public ReadExample(){
      gson = new Gson();
    }
    public List<String> readJsonStream(InputStream in) throws IOException {
        Log.d(TAG, "readJsonStream: ");
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        List<String> messages = new ArrayList<String>();
        Log.d(TAG, "readJsonStream: Start");
        while (reader.hasNext()) {
            String message = gson.fromJson(reader, String.class);
            Log.d(TAG, "readJsonStream: "+message);
            //messages.add(message);
        }
        Log.d(TAG, "readJsonStream: End");
        reader.endArray();
        reader.close();
        return messages;
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        Log.d(TAG, "onResponse: ");
        InputStream inputStream = response.body().byteStream();
        Log.d(TAG, "onResponse: getBytestream");
        try {
            readJsonStream(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "onResponse: ",e );
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        Log.e(TAG, "onFailure: ",t );
    }


    public static Callback<ResponseBody> responseBodyCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
            Log.v(TAG, "Response");
            if (response.isSuccessful()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JsonReader reader = new JsonReader(
                                    new InputStreamReader(response.body().byteStream()));
                            Gson gson = new GsonBuilder().create();
                            Log.d(TAG, "run: Read while");
                            JsonObject j = gson.fromJson(reader, JsonObject.class);
//                            Tweet tweet = new Tweet(j.get("text").getAsString());
                            ///Tweet tweet = Tweet.fromJsonObject(j);

                            while (true) {
                                // Several types of messages can be sent.
                                // Some are not Tweet objects.
                                // https://developer.twitter.com/en/docs/tweets/data-dictionary/overview/tweet-object.html
                                j = gson.fromJson(reader, JsonObject.class);
                                Log.d(TAG, "run: "+j.toString());
                            }
                        } catch (JsonSyntaxException e) {
                            Log.v(TAG, "Stopped streaming.");
                        }
                        Log.d(TAG, "run: End");
                    }
                }).start();
            }else{
                Log.e(TAG, "onResponse: Not success");
            }

        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            Log.e("responseBodyCallback", "Response failure.");
        }
    };

}
