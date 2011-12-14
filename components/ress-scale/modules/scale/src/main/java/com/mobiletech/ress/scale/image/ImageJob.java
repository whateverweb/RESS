package com.mobiletech.ress.scale.image;

import com.mobiletech.ress.scale.image.cache.ImageCache;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: simjohmt
 * Date: 2011-nov-21
 * Time: 11:49:52
 * To change this template use File | Settings | File Templates.
 */
public class ImageJob {

    private final Logger logger = Logger.getLogger(ImageJob.class.getName());

    private RessImage myImage;
    private byte[] myImageBytes;

    private boolean iAmScaled = false;

    public ImageJob(RessImage theImage) {
        myImage = theImage;
    }

    /**
     * Checks if there is a cached version. If there is no cached version it will download the original (if not cached)
     * scale to the requested size.
     * 
     * @throws Exception
     */
    public byte[] fetchImage() throws Exception {

        logger.debug("Job running for image: " + myImage.getUrl() + " size:"  + myImage.getSize() +"px");

        //first check if the original exists in the cache, if not we need to dl that
        if(!ImageCache.getInstance().imageEntityExists(myImage.getUrl())) {
            downloadOriginal();
            scaleToPresets();
        }
        // scaleImage will place the scaled image in the private myImageBytes
        if(!ImageCache.getInstance().imageEntityExists(myImage.getSize() + myImage.getUrl())) scaleImage();
        return myImageBytes;
    }

    /**
     * Will download the "original" image resource and cache it. Will only accept responses that are of the image mimetype
     * and those with a reponse less than or equal to 2.000.000 bytes.
     *
     * @throws Exception
     */
    private void downloadOriginal() throws Exception {
        HttpClient aClient = new HttpClient(ImageScaler.getInstance().getManager());
        GetMethod aGetMethod = new GetMethod(myImage.getUrl());
        try{
            aClient.executeMethod(aGetMethod);
            if(aGetMethod.getStatusCode() == 200) {
                Header aContentType = aGetMethod.getResponseHeader("Content-Type");

                if(aContentType == null || aContentType.getValue() == null || !aContentType.getValue().contains("image")) {
                    String aWrongType = aContentType == null ? "null" : aContentType.getValue() == null ? "nothing" : aContentType.getValue();
                    throw new Exception("The content type of the remote resource was not of the type image/xxxxx, it was " + aWrongType);
                }
                byte[] aResponse = aGetMethod.getResponseBody(2000000);
                ImageCache.getInstance().cacheImage(myImage.getUrl(), aResponse);
            } else {
                throw new Exception("The repsonsecode for the image was not 200, it was " + aGetMethod.getStatusCode());
            }
        } catch(Exception e) {
            throw e;
        } finally {
            logger.debug("I was released for image: " + myImage.getUrl());
            aGetMethod.releaseConnection();
        }
    }


    /**
     * Scales the requested image to the wanted dimension, room for improvement here.
     * @throws Exception
     */
    private void scaleImage() throws Exception {
        //This function can be heavily improved!

        InputStream anInputStream = new ByteArrayInputStream( ImageCache.getInstance().getCachedImage(myImage.getUrl()));

        
        BufferedImage anOriginalImage = ImageIO.read(anInputStream);
        int anTypeToUse = anOriginalImage.getType();

        int anOriginalWidth = anOriginalImage.getWidth();
        int anOriginalHeight = anOriginalImage.getHeight();

        //Add command to override "NO scale to larger image than original"
        BufferedImage aNewImage = null;
        if(anOriginalWidth > myImage.getSize()) {
            int aNewHeight = (int) ((((float) anOriginalHeight) / ((float) anOriginalWidth)) * ((float)myImage.getSize()));
            aNewImage = new BufferedImage(myImage.getSize(), aNewHeight, anTypeToUse);
        } else {
            aNewImage = new BufferedImage(anOriginalWidth, anOriginalHeight, anTypeToUse);
        }

        logger.info("Sizes when scaling, from: " + anOriginalWidth + "*" + anOriginalHeight + " to " + aNewImage.getWidth() + "*" + aNewImage.getHeight());


        Graphics2D aGraphics = aNewImage.createGraphics();

        aGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        aGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        aGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        aGraphics.drawImage(anOriginalImage, 0,0,aNewImage.getWidth(), aNewImage.getHeight(), null);

        ByteArrayOutputStream anOutputStream = new ByteArrayOutputStream();
        ImageIO.write( aNewImage, ImageScaler.getInstance().getImageMimetype(myImage), anOutputStream );
        anOutputStream.flush();
        myImageBytes = anOutputStream.toByteArray();
        anOutputStream.close();

        //The image is ready, now lets create a meta-data container for this image and cache that too
        RessImageHeader aHeader = new RessImageHeader();
        aHeader.setContentLength(myImageBytes.length);
        aHeader.setCreated(new Date());
        aHeader.setEtag(generateEtag(myImageBytes));

        //Cache all the things!
        ImageCache.getInstance().cacheImage(myImage.getSize() + myImage.getUrl(), myImageBytes);
        ImageCache.getInstance().cacheImageHeader(myImage.getSize() + myImage.getUrl(), aHeader);
    }

    /**
     * Generates a unique etag for the given image data. !!Should we time this and see how long it takes to do a etag?
     * @param theImageData
     * @return
     */
    private String generateEtag(byte[] theImageData) {
        return DigestUtils.md5Hex(theImageData);
    }


    private void scaleToPresets() {
        //scale to "known common sizes", this can be done in a thread so we don't block just because of this
        ImageScaler.getInstance().createPresetJob(myImage);
    }

    public RessImage getImage() {
        return myImage;
    }

    public void setImage(RessImage theImage) {
        myImage = theImage;
    }

    public byte[] getImageBytes() {
        return myImageBytes;
    }

    
}
