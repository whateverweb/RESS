package com.mobiletech.ress.scale.image;

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

    private Executor myExecutor = null;


    private ImageScaler() {
        final BlockingQueue<Runnable> aWorkQueue = new ArrayBlockingQueue<Runnable>(1000);
        myExecutor = new ThreadPoolExecutor(5, 10, 30, TimeUnit.SECONDS, aWorkQueue); 
    }

    public static synchronized ImageScaler getInstance() {
        if(myInstance == null) {
            myInstance = new ImageScaler();            
        }
        return myInstance;
    }

    
    public void createImageJob(Image theImage) {
        myExecutor.execute(new ImageJob(theImage));        
    }

}
