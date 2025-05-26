package com.example.androidexample;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * A singleton class that provides a centralized and optimized management system
 * for networking requests using the Volley library. It ensures that all requests
 * are processed through a single instance of the RequestQueue and also provides
 * an ImageLoader to efficiently load images from URLs.
 */
public class VolleySingleton {

    // Singleton instance of VolleySingleton
    private static VolleySingleton instance;

    // RequestQueue for handling network requests
    private RequestQueue requestQueue;

    // ImageLoader for loading images from URLs
    private ImageLoader imageLoader;

    // Context for accessing application resources
    private static Context ctx;

    /**
     * Private constructor to initialize the VolleySingleton with the provided context.
     * It initializes the request queue and the image loader.
     *
     * @param context The application context.
     */
    private VolleySingleton(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();

        imageLoader = new ImageLoader(requestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap> cache = new LruCache<>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    /**
     * Returns the singleton instance of the VolleySingleton class.
     * If an instance does not exist, it will be created.
     *
     * @param context The application context.
     * @return The singleton instance of VolleySingleton.
     */
    public static synchronized VolleySingleton getInstance(Context context) {
        if (instance == null) {
            instance = new VolleySingleton(context);
        }
        return instance;
    }

    /**
     * Returns the RequestQueue for handling network requests. If the request queue is
     * not yet initialized, it creates a new one using the application's context.
     *
     * @return The RequestQueue used for handling network requests.
     */
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is used to prevent memory leaks by avoiding
            // holding references to the activity or broadcast receiver.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    /**
     * Adds a request to the RequestQueue. The request will be processed by the Volley library.
     *
     * @param req The request to add to the queue.
     * @param <T> The type of the response.
     */
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    /**
     * Returns the ImageLoader instance used for efficiently loading images from URLs.
     *
     * @return The ImageLoader instance.
     */
    public ImageLoader getImageLoader() {
        return imageLoader;
    }
}
