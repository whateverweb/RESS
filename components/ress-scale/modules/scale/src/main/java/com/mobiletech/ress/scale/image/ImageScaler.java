package com.mobiletech.ress.scale.image;

import com.mobiletech.ress.scale.image.cache.ImageCache;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.log4j.Logger;

import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: simjohmt
 * Date: 2011-nov-21
 * Time: 11:44:37
 * To change this template use File | Settings | File Templates.
 */
public class ImageScaler {
    private static ImageScaler myInstance;
    private final Logger logger = Logger.getLogger(ImageScaler.class.getName());
    private Executor myExecutor = null;
    private MultiThreadedHttpConnectionManager myManager;


    private ImageScaler() {
        final BlockingQueue<Runnable> aWorkQueue = new ArrayBlockingQueue<Runnable>(1000);
        myExecutor = new ThreadPoolExecutor(5, 10, 30, TimeUnit.SECONDS, aWorkQueue);
        myManager = new MultiThreadedHttpConnectionManager();
    }

    public static synchronized ImageScaler getInstance() {
        if(myInstance == null) {
            myInstance = new ImageScaler();            
        }
        return myInstance;
    }

    
    public void createPresetJob(RessImage theImage) {
        myExecutor.execute(new PresetJob(theImage));        
    }

    public byte[] getImageBytes(RessImage theImage) throws Exception {
        try {
            //Do we have a cached image version??
            byte[] aReturnArray = ImageCache.getInstance().getCachedImage(theImage.getSize() + theImage.getUrl());
            return aReturnArray;
        } catch(Exception e) {
            // NOPE! If not, create a job to scale it and return a dummy image
            //Scale it and return it
            logger.info("No object found in cache, will scale original image in an image job: ", e);
            //createImageJob(theImage);
            ImageJob aJob = new ImageJob(theImage);

            return aJob.fetchImage();
        }
    }

    public String getImageMimetype(RessImage theImage) {
        return guessFileType(theImage.getUrl());    
    }

    public RessImageHeader getImageHeader(RessImage theImage) {
        try {
            RessImageHeader aReturnHeader = ImageCache.getInstance().getCachedImageHeader(theImage.getSize() + theImage.getUrl());
            return aReturnHeader;
        } catch(Exception e) {
            logger.debug("No such header in cache", e);
            return null;
        }
    }

    private String guessFileType(String theUrl) {
        //improve guessing, also allow filetype parameter incase it cant be guessed?
        if(theUrl.endsWith("jpg") || theUrl.endsWith("jpeg")) return "jpg";
        if(theUrl.endsWith("png")) return "png";
        if(theUrl.endsWith("gif")) return "gif";
        return "jpg";
    }

    public MultiThreadedHttpConnectionManager getManager() {
        return myManager;
    }
}
