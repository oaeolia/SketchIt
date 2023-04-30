package com.example.itogoviyproject.server;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.itogoviyproject.Application;
import com.example.itogoviyproject.loggers.ILogger;

import org.json.JSONException;
import org.json.JSONObject;

public class Server {
    private static final String SERVER_URL = "http://192.168.1.13:5000/api/v1/";
    private static final String SERVER_RESPONSE_OK = "OK";
    private static final String SERVER_RESPONSE_BAD = "BAD";
    private static final String SERVER_RESPONSE_ERROR = "ERROR";


    private final RequestQueue requestQueue;
    private final ILogger logger;
    private final Context context;

    private int sessionId = -1;
    private String sessionToken = "";


    public Server(Context context, ILogger logger) {
        requestQueue = Volley.newRequestQueue(context);
        this.logger = logger;
        this.context = context;
    }

    public void registration(String name, String email, String password, ServerCallback<Boolean, String, Object> callback, @Nullable ServerCallback<String, Integer, Object> errorCallback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, SERVER_URL + "auth/registration", jsonBody, responseData -> {
            try {
                if (responseData.getString("status").equals(SERVER_RESPONSE_OK)) {

                    callback.onDataReady(true, null, null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_BAD)) {
                    callback.onDataReady(false, responseData.getString("message"), null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_ERROR)) {
                    if (errorCallback != null) {
                        errorCallback.onDataReady(responseData.getString("message"), -1, null);
                    }
                } else {
                    if (errorCallback != null) {
                        errorCallback.onDataReady("Server undefined error (undefined status)", null, null);
                    }
                    logger.logError("Server", "Server undefined error (undefined status, try registration)");
                }
            } catch (JSONException e) {
                logger.logError("Server", "Can`t parse JSON from server (registration): " + e.getMessage());
            }
        }, error -> {
            if (error.getMessage() != null) {
                logger.logError("Server", "Can`t registration " + error.getMessage());
            } else {
                logger.logError("Server", "Can`t registration " + error);
            }
            if (errorCallback != null) {
                errorCallback.onDataReady("Server undefined error", null, null);
            }
        });

        requestQueue.add(request);
    }

    public void login(String login, String password, ServerCallback<Boolean, String, Object> callback, @Nullable ServerCallback<String, Integer, Object> errorCallback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("login", login);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, SERVER_URL + "auth/login", jsonBody, responseData -> {
            try {
                if (responseData.getString("status").equals(SERVER_RESPONSE_OK)) {
                    sessionId = responseData.getInt("session_id");
                    sessionToken = responseData.getString("session_token");
                    logger.logInfo("Server", "Login successful, session id: " + sessionId);
                    if(responseData.has("application_id")){
                        SharedPreferences.Editor preferences = context.getSharedPreferences(Application.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE).edit();
                        preferences.putInt("application_id", responseData.getInt("application_id"));
                        preferences.putString("application_token", responseData.getString("application_token"));
                        preferences.apply();
                    }
                    callback.onDataReady(true, null, null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_BAD)) {
                    callback.onDataReady(false, responseData.getString("message"), null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_ERROR)) {
                    if (errorCallback != null) {
                        errorCallback.onDataReady(responseData.getString("message"), -1, null);
                    }
                } else {
                    if (errorCallback != null) {
                        errorCallback.onDataReady("Server undefined error (undefined status)", null, null);
                    }
                    logger.logError("Server", "Server undefined error (undefined status, try login)");
                }
            } catch (JSONException e) {
                logger.logError("Server", "Can`t parse JSON from server (login): " + e.getMessage());
            }
        }, error -> {
            if (error.getMessage() != null) {
                logger.logError("Server", "Can`t login " + error.getMessage());
            } else {
                logger.logError("Server", "Can`t login " + error);
            }
            if (errorCallback != null) {
                errorCallback.onDataReady("Server undefined error (try login)", null, null);
            }
        });

        requestQueue.add(request);
    }

    public void loginByApplicationData(String token, int id, ServerCallback<Boolean, String, Object> callback, @Nullable ServerCallback<String, Integer, Object> errorCallback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("application_token", token);
            jsonBody.put("application_session_id", id);
        } catch (JSONException e) {
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, SERVER_URL + "auth/login", jsonBody, responseData -> {
            try {
                if (responseData.getString("status").equals(SERVER_RESPONSE_OK)) {
                    sessionId = responseData.getInt("session_id");
                    sessionToken = responseData.getString("session_token");
                    logger.logInfo("Server", "Auto Login successful, session id: " + sessionId);
                    if(responseData.has("application_id")){
                        SharedPreferences.Editor preferences = context.getSharedPreferences(Application.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE).edit();
                        preferences.putInt("application_id", responseData.getInt("application_id"));
                        preferences.putString("application_token", responseData.getString("application_token"));
                        preferences.apply();
                    }
                    callback.onDataReady(true, null, null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_BAD)) {
                    callback.onDataReady(false, responseData.getString("message"), null);
                } else if (responseData.getString("status").equals(SERVER_RESPONSE_ERROR)) {
                    if (errorCallback != null) {
                        errorCallback.onDataReady(responseData.getString("message"), -1, null);
                    }
                } else {
                    if (errorCallback != null) {
                        errorCallback.onDataReady("Server undefined error (undefined status)", null, null);
                    }
                    logger.logError("Server", "Server undefined error (undefined status, try Auto login)");
                }
            } catch (JSONException e) {
                logger.logError("Server", "Can`t parse JSON from server (Auto login): " + e.getMessage());
            }
        }, error -> {
            if (error.getMessage() != null) {
                logger.logError("Server", "Can`t Auto login " + error.getMessage());
            } else {
                logger.logError("Server", "Can`t login " + error);
            }
            if (errorCallback != null) {
                errorCallback.onDataReady("Server undefined error (try Auto login)", null, null);
            }
        });

        requestQueue.add(request);
    }


    public void logout() {
        JSONObject jsonBody = new JSONObject();
        SharedPreferences preferences = context.getSharedPreferences(Application.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        try {
            jsonBody.put("application_session_id", preferences.getInt("application_id", -1));
            jsonBody.put("application_session_token", preferences.getString("application_token", ""));
        } catch (JSONException e) {
            return;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("application_id");
        editor.remove("application_token");
        editor.apply();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, SERVER_URL + "auth/logout", jsonBody, responseData -> { }, error -> { });

        requestQueue.add(request);
    }
}