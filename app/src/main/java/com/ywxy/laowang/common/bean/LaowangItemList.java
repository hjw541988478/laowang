package com.ywxy.laowang.common.bean;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hjw on 2015/8/30 0030.
 */
public class LaowangItemList implements Serializable{
    public List<LaowangItem> mLaowangList = new ArrayList<>();

    public void decodeJson(JSONArray jsonArray) throws Exception {
        int len = jsonArray.length();
        for (int i = 0; i < len; i++) {
            LaowangItem mLaowangItem = new LaowangItem();
            mLaowangItem.decodeJson(jsonArray.getJSONObject(i));
            mLaowangList.add(mLaowangItem);
        }
    }
}
