package com.ywxy.laowang.common.bean;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by hjw on 2015/8/30 0030.
 */
public class LaowangItem implements Serializable {
    public String item_id;
    public String item_text;
    public String item_url;

    public void decodeJson(JSONObject json) throws Exception {
//        item_id = json.getString("id");
//        item_text = json.getString("content");
//        item_url = json.getString("pic_url");
        item_id = json.getString("objectId");
        item_text = json.getString("who");
        item_url = json.getString("url");
    }

}

