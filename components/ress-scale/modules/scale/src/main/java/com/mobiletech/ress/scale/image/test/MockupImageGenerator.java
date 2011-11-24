package com.mobiletech.ress.scale.image.test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: simjohmt
 * Date: 2011-nov-24
 * Time: 15:00:57
 * To change this template use File | Settings | File Templates.
 */
public class MockupImageGenerator {

    public static byte[] generateMockup(int theWidth) {
        try{
            int aHeight = (int)((double)theWidth * (3.f/4.f));
            BufferedImage aReturnImage = new BufferedImage(theWidth, aHeight, BufferedImage.TYPE_INT_RGB);
            Graphics aGraphics = aReturnImage.createGraphics();
            aGraphics.setColor(Color.GRAY);
            aGraphics.fillRect(0, 0, theWidth, aHeight);
            aGraphics.setColor(Color.WHITE);
            aGraphics.drawLine(0, 0, theWidth -1, aHeight-1);
            aGraphics.drawLine(theWidth -1, 0, 0, aHeight -1);
            aGraphics.setColor(Color.BLACK);
            aGraphics.drawString(theWidth + " x " + aHeight, theWidth / 2, aHeight / 2);
            aGraphics.dispose();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write( aReturnImage, "jpg", baos );
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            return imageInByte;
        } catch(Exception e) {
            return null;
        }
    }


}
