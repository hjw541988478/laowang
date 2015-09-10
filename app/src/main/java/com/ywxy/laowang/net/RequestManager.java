package com.ywxy.laowang.net;

import android.content.Context;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.ywxy.laowang.common.bean.LaowangItemList;
import com.ywxy.laowang.common.util.NetUtil;

import org.json.JSONArray;

/**
 * Created by hjw on 2015/8/30 0030.
 */
public class RequestManager {

    public static final String GIF_URL = "http://pic.962.net/up/2012-8/2012082010565730752.gif";
    public static final String LAOWANG_URL = "http://www.xiaohua123.net/admin.php/Api/?ctime=%s&page=%s";
    //    private static BitmapCache bitmapCache;
    private static RequestManager instance;
    private static RequestQueue requestQueue;

    private RequestManager(Context context) {
        requestQueue = Volley.newRequestQueue(context);
//        bitmapCache = new BitmapCache();
    }

    public static RequestManager getInstance(Context context) {
        if (instance == null) {
            instance = new RequestManager(context);
        }
        return instance;
    }


    public static LaowangItemList loadLaowangItems(Context context, int page, final OnLaowangItemsRequestListener listener) {
        final LaowangItemList list = new LaowangItemList();
        if (!NetUtil.isNetworkAvailable(context)) {
            if (listener != null)
                listener.onError("请检查网络连接~");
        } else {
            String requstUrl = String.format(LAOWANG_URL, System.currentTimeMillis() / 1000, page);
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(requstUrl, new Response.Listener<JSONArray>() {

                @Override
                public void onResponse(JSONArray array) {
                    if (array != null) {
                        LaowangItemList list = new LaowangItemList();
                        try {
                            list.decodeJson(array);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (listener != null)
                            listener.onSuccess(list);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    if (listener != null)
                        listener.onError(volleyError.getMessage());
                }
            });
            requestQueue.add(jsonArrayRequest);
        }
        return list;
    }

    public static void loadNetworkImage(ImageView mImageView, String imgUrl) {
//        ImageLoader imageLoader = new ImageLoader(requestQueue, bitmapCache);
//        ImageLoader.ImageListener listener = ImageLoader.getImageListener(mImageView, R.drawable.ic_default_img, R.drawable.ic_default_img);
//        imageLoader.get(imgUrl, listener);
    }

    public interface OnLaowangItemsRequestListener {
        void onSuccess(LaowangItemList list);

        void onError(String error);
    }


}

