package com.mobiletech.ress.scale.servlets.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: simjohmt
 * Date: 2011-nov-24
 * Time: 10:56:30
 * POJO for the data sent via the RESS cookie
 */
public class RessCookie {

    private int myViewPortWidth;
    private String myActiveBreakpoint;
    private double myGridWidth;
    private int myGridCount;
    private Map<String, Integer> myGroups;

    public RessCookie() {
        myGroups = new HashMap<String, Integer>();
    }

    public int getViewPortWidth() {
        return myViewPortWidth;
    }

    public void setViewPortWidth(int theViewPortWidth) {
        myViewPortWidth = theViewPortWidth;
    }

    public String getActiveBreakpoint() {
        return myActiveBreakpoint;
    }

    public void setActiveBreakpoint(String theActiveBreakpoint) {
        myActiveBreakpoint = theActiveBreakpoint;
    }

    public double getGridWidth() {
        return myGridWidth;
    }

    public void setGridWidth(double theGridWidth) {
        myGridWidth = theGridWidth;
    }

    public Map<String, Integer> getGroups() {
        return myGroups;
    }

    public void setGroups(Map<String, Integer> theGroups) {
        myGroups = theGroups;
    }

    public int getGridCount() {
        return myGridCount;
    }

    public void setGridCount(int theGridCount) {
        myGridCount = theGridCount;
    }

    @Override
    public String toString() {
        return "[RESSCookie: vpw: " + myViewPortWidth + " activeBreakpoint: " + myActiveBreakpoint + " gridwidth: " + myGridWidth + " groups: " + myGroups.toString() + "]";
    }
}
