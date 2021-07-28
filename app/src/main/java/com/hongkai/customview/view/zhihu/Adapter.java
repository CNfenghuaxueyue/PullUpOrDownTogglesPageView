package com.hongkai.customview.view.zhihu;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public abstract class Adapter<T> {
    private List<T> list;

    public Adapter(List<T> list) {
        this.list = list;
    }

    public abstract View getView(Context context, int position, ViewGroup parent);

    public int getItemType(T t, int position) {
        return 0;
    }

    public int getItmeCount() {
        return list.size();
    }

    public T getItem(int position) {
        return list.get(position);
    }

    public List<T> getData() {
        return list;
    }


}
