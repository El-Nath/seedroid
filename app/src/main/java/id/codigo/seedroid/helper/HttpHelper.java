package id.codigo.seedroid.helper;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.codigo.seedroid.ApplicationMain;
import id.codigo.seedroid.R;

/**
 * Created by Lukma on 3/29/2016.
 */
public class HttpHelper {
    private static final String TAG = HttpHelper.class.getSimpleName();

    private static HttpHelper instance;

    private RequestQueue requestQueue;
    private RetryPolicy retryPolicy = new RetryPolicy() {
        @Override
        public int getCurrentTimeout() {
            return 50000;
        }

        @Override
        public int getCurrentRetryCount() {
            return 50000;
        }

        @Override
        public void retry(VolleyError error) throws VolleyError {
        }
    };

    private HttpHelper() {
        requestQueue = getRequestQueue();
    }

    public static synchronized HttpHelper getInstance() {
        if (instance == null) {
            instance = new HttpHelper();
        }
        return instance;
    }

    /**
     * Make a GET request and return a string
     *
     * @param url      URL of the request to make
     * @param listener Listener of the response from request
     */
    public void get(String url, final HttpListener<String> listener) {
        Log.d(TAG, "request:" + url);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        listener.onReceive(true, ApplicationMain.getInstance().getString(R.string.status_success), response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NoConnectionError) {
                            listener.onReceive(false, ApplicationMain.getInstance().getString(R.string.status_no_connection), null);
                        } else {
                            listener.onReceive(false, ApplicationMain.getInstance().getString(R.string.status_failed), null);
                        }
                        Log.e(TAG, error.getMessage() + "");
                    }
                });
        request.setRetryPolicy(retryPolicy);
        addToRequestQueue(request);
    }

    /**
     * Make a POST request and return a string
     *
     * @param url        URL of the request to make
     * @param parameters Parameters of the request to make
     * @param listener   Listener of the response from request
     */
    public void post(final String url, final Map<String, String> parameters, final HttpListener<String> listener) {
        String requestHttpPost = url;
        for (String key : parameters.keySet()) {
            requestHttpPost += "[" + key + ":" + parameters.get(key) + "],";
        }
        Log.d(TAG, "request:" + requestHttpPost);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        listener.onReceive(true, ApplicationMain.getInstance().getString(R.string.status_success), response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NoConnectionError) {
                            listener.onReceive(false, ApplicationMain.getInstance().getString(R.string.status_no_connection), null);
                        } else {
                            listener.onReceive(false, ApplicationMain.getInstance().getString(R.string.status_failed), null);
                        }
                        Log.e(TAG, error.getMessage() + "");
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String requestHttpPost = "";
                for (String key : super.getHeaders().keySet()) {
                    requestHttpPost += "[" + key + ":" + super.getHeaders().get(key) + "],";
                }
                Log.d(TAG, "header:" + requestHttpPost);

                return new HashMap<>();
            }

            @Override
            protected Map<String, String> getParams() {
                return parameters;
            }
        };
        request.setRetryPolicy(retryPolicy);
        request.setTag(url);
        addToRequestQueue(request);
    }


    /**
     * Make a POST multipart request
     *
     * @param url        URL of the request to make
     * @param parameters Parameters of the request to make
     * @param delegate   Listener of the response from request
     */
    public void postMultipart(String url, Map<String, String> parameters, UploadStatusDelegate delegate) {
        try {
            MultipartUploadRequest request = new MultipartUploadRequest(ApplicationMain.getInstance(), UUID.randomUUID().toString(), url);
            request.addHeader("Authorization", "Basic YWRtaW46MTIzNA==");
            request.setDelegate(delegate);

            for (String key : parameters.keySet()) {
                if (key.startsWith("file-")) {
                    request.addFileToUpload(parameters.get(key), key.replace("file-", ""));
                } else {
                    request.addParameter(key, parameters.get(key));
                }
            }

            String requestHttpPost = url;
            for (String key : parameters.keySet()) {
                requestHttpPost += "[" + key.replace("file-", "") + ":" + parameters.get(key) + "],";
            }
            Log.d(TAG, "request:" + requestHttpPost);

            request.startUpload();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private <T> void addToRequestQueue(Request<T> request) {
        getRequestQueue().add(request);
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ApplicationMain.getInstance());
        }
        return requestQueue;
    }

    public interface HttpListener<T> {
        void onReceive(boolean status, String message, T response);
    }
}
