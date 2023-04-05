package com.mygdx.hattrick2023;

/**
 * Created by Administrator on 19-Dec-16.
 */

public class Vector2d {
    public double i = 0;
    public double j = 0;

    public Vector2d(double i, double j){
        this.i = i;
        this.j = j;
    }

    public double getMagnitude(){
        return Math.sqrt(i*i+j*j);
    }

    public double dot(Vector2d v){
        return i*v.i+j*v.j;
    }

    public Vector2d times(double scalar){
        return new Vector2d(i*scalar, j*scalar);
    }
    public Vector2d plus(Vector2d v){
        return new Vector2d(v.i+i, v.j+j);
    }
    public Vector2d proj(Vector2d v){
        return v.times(this.dot(v)/v.dot(v));
    }
    public Vector2d getUnitVector(){
        return new Vector2d(i/getMagnitude(), j/getMagnitude());
    }
    public void set(double i, double j) {
        this.i = i;
        this.j = j;
    }
}
