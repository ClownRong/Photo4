package com.clown.ocr.two;

import android.support.annotation.NonNull;

/**
 * Created by Joker on 2017/07/30.
 */

public class Size implements Comparable<Size> {

    public int width;
    public int height;

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "Size{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }

    @Override
    public int compareTo(@NonNull Size o) {
        int i = this.getWidth() - o.getWidth();
        if (i == 0) {
            return this.getHeight() - o.getHeight();
        }
        return i;
    }

}
