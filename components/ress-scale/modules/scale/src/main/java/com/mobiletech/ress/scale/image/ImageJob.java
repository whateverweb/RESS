package com.mobiletech.ress.scale.image;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: simjohmt
 * Date: 2011-nov-21
 * Time: 11:49:52
 * To change this template use File | Settings | File Templates.
 */
public class ImageJob implements Runnable {

    private final Logger logger = Logger.getLogger(ImageJob.class.getName());

    private Image myImage;

    public ImageJob(Image theImage) {
        myImage = theImage;
    }


    public void run() {

        logger.debug("Job running for image: " + myImage.getUrl() + " size:"  + myImage.getSize() +"px");

        if(!myImage.originalExistsOnDisc()) {
            downloadOriginal();
            scaleToPresets();
        }
        if(!myImage.existsOnDisc()) scaleImage();
    }

    private void downloadOriginal() {

    }

    private void scaleImage() {

    }

    private void scaleToPresets() {
        
    }

    public Image getImage() {
        return myImage;
    }

    public void setImage(Image theImage) {
        myImage = theImage;
    }
}
