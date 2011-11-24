package com.mobiletech.ress.scale.image;

/**
 * Created by IntelliJ IDEA.
 * User: simjohmt
 * Date: 2011-nov-21
 * Time: 11:50:02
 * To change this template use File | Settings | File Templates.
 */
public class Image {

    private int mySize;
    private String myUrl;

    public Image(String theUrl, int theSize) {
        mySize = theSize;
        myUrl = theUrl;
    }

    public int getSize() {
        return mySize;
    }

    public void setSize(int theSize) {
        mySize = theSize;
    }

    public String getUrl() {
        return myUrl;
    }

    public void setUrl(String theUrl) {
        myUrl = theUrl;
    }

    public boolean existsOnDisc() {
        return false;
    }

    public boolean originalExistsOnDisc() {
        return false;
    }

}
