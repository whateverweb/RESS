package com.mobiletech.ress.scale.servlets;


import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import com.mobiletech.ress.scale.image.RessImage;
import com.mobiletech.ress.scale.image.ImageScaler;
import com.mobiletech.ress.scale.image.RessImageHeader;
import com.mobiletech.ress.scale.image.cache.ImageCache;
import com.mobiletech.ress.scale.image.test.MockupImageGenerator;
import com.mobiletech.ress.scale.servlets.util.PathCommand;
import com.mobiletech.ress.scale.servlets.util.PathUtil;
import com.mobiletech.ress.scale.servlets.util.RessCookie;
import com.mobiletech.ress.scale.servlets.util.RessCookieUtil;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: simjohmt
 * Date: 2011-nov-21
 * Time: 10:48:05
 * To change this template use File | Settings | File Templates.
 */
public class ScaleServlet extends HttpServlet {

    private final Logger logger = Logger.getLogger(ScaleServlet.class.getName());
    
    public ScaleServlet() {}

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String aRequestUrl = request.getRequestURI();
        logger.info("tempdir: " + System.getProperty("java.io.tmp"));
        try {
            List<PathCommand> someCommands = PathUtil.getInstance().parsePath(aRequestUrl, "ress-scale/img/");

            //We should check against a whitelist here to make sure only allowed domains are in the path so the service cannot be missused.

            //Get the cookie value and send it to the Cookie parser util that will exctract the values we need to determine
            //what imagesize to return
            String aRessCookieValue = "";
            if(request.getCookies() != null) {
                for(Cookie aCookie : request.getCookies()) {
                    if("RESS".equals(aCookie.getName())) {
                        aRessCookieValue = aCookie.getValue();
                        break;
                    }
                }
            }

            RessCookie aRessCookie = RessCookieUtil.getInstance().parseRessCookie(aRessCookieValue);

            //if defaults are present in the path and values were not set from the cookie, set them now
            setDefaultRessCookieValues(aRessCookie, someCommands);

            logger.info(aRessCookie.toString());

            for(PathCommand aCommand : someCommands) {
                logger.info(aCommand.toString());
            }

            RessImage anImage = getImage(aRessCookie, someCommands);

            //We should check and compare the etags at this point, we could get away with sending a 304
            //if the client has not changed orientation or modified the width of the viewport in any way

            if(checkEtag(anImage, request)) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }

            //The etag differed or there was no etag in the request, lets send an image to the client
            byte[] someDataToSend = ImageScaler.getInstance().getImageBytes(anImage);
            String aMimeType = ImageScaler.getInstance().getImageMimetype(anImage);
            RessImageHeader aHeader = ImageScaler.getInstance().getImageHeader(anImage);

            if(someDataToSend != null) {
                response.setContentType("image/" + aMimeType);
                if(aHeader != null) response.setHeader("ETag", aHeader.getEtag());
                response.setContentLength(someDataToSend.length);
                response.getOutputStream().write(someDataToSend);
                response.getOutputStream().flush();
                response.getOutputStream().close();
            }

        } catch(Exception e) {
            //If an exception was thrown generate an image containging the error so the enduser can easily(?) see what they
            //might have done
            logger.info("error: ", e);
            response.setContentType("image/jpg");
            byte[] someDataToSend  = {};
            try {
                someDataToSend = MockupImageGenerator.generateErrorImage(e.toString());
            } catch(Exception ex) {
                logger.info(ex);
            }
            response.setContentLength(someDataToSend.length);
            response.getOutputStream().write(someDataToSend);
            response.getOutputStream().flush();
            response.getOutputStream().close();
        }
    }


    /**
     * This function will modify the resscookie sent in if it has no values viewport width and active breakpoint from
     * the cookie and defaults are present in the path
     * @param theRessCookie
     * @param theCommands
     * @throws Exception
     */
    private void setDefaultRessCookieValues(RessCookie theRessCookie, List<PathCommand> theCommands) throws Exception {
        for(PathCommand aCommand : theCommands) {
            if(aCommand.getCommand() == PathCommand.Command.VIEWPORT) {
                if(theRessCookie.getViewPortWidth() == 0) {
                    theRessCookie.setViewPortWidth(Integer.parseInt(aCommand.getValue()));
                }
            }
            if(aCommand.getCommand() == PathCommand.Command.FALLBACK_BREAKPOINT) {
                if(theRessCookie.getActiveBreakpoint() == null) {
                    theRessCookie.setActiveBreakpoint(aCommand.getValue());
                }
            }

            if(aCommand.getCommand() == PathCommand.Command.GRID_COLUMNS) {
                if(theRessCookie.getGridCount() == 0) {
                    theRessCookie.setGridCount(Integer.parseInt(aCommand.getValue()));
                }
            }
        }
    }


    /**
     * Logic that determines the wanted size
     * @param theRessCookie
     * @param theCommands
     * @return An RessImage containing the wanted size and the url to the origin image
     * @throws Exception
     */
    private RessImage getImage(RessCookie theRessCookie, List<PathCommand> theCommands) throws Exception {

        //Get the url for the image that should be scaled
        String anImageUrl = getImageUrl(theCommands);

        //find out what the "major" command is that determines how to actually scale the image
        PathCommand aMajorCommand = getMajorCommand(theCommands);
        if(aMajorCommand == null) {
            logger.debug("No major command found!");
            throw new Exception("No major command found!");
        }

        if(aMajorCommand.getCommand() == PathCommand.Command.GRID_COLUMNS) {
            int aNumberOfColumnsForImage = getWidthConfigGridNumber(theRessCookie.getActiveBreakpoint(), theCommands);
            if(theRessCookie.getGridWidth() > 0) {
                //gridwidth is set, use this primarely
                return new RessImage(anImageUrl, (int)(aNumberOfColumnsForImage * theRessCookie.getGridWidth()));
            } else if(theRessCookie.getViewPortWidth() > 0) {
                int aGridCount = 100;
                if(theRessCookie.getGridCount() > 0) aGridCount = theRessCookie.getGridCount();
                return new RessImage(anImageUrl, (int)(((double)theRessCookie.getViewPortWidth() / (double)aGridCount) * (double)aNumberOfColumnsForImage));
            }
        }

        if(aMajorCommand.getCommand() == PathCommand.Command.PERCENTAGE) {
            int aNumberOfPercentageForImage = getWidthConfigGridNumber(theRessCookie.getActiveBreakpoint(), theCommands);
            if(theRessCookie.getViewPortWidth() > 0) {
                return new RessImage(anImageUrl, (int)(((double)theRessCookie.getViewPortWidth() / 100.f) * (double)aNumberOfPercentageForImage));
            }
        }

        if(aMajorCommand.getCommand() == PathCommand.Command.PIXELS) {
            if(aMajorCommand.getValue() == null) {
                //Use breakpoints to find right
                int aPixelNumber = getWidthConfigGridNumber(theRessCookie.getActiveBreakpoint(), theCommands);
                return new RessImage(anImageUrl, aPixelNumber);
            } else {
                //Use the value as the pixel size..
                return new RessImage(anImageUrl, Integer.parseInt(aMajorCommand.getValue()));
            }
        }

        if(aMajorCommand.getCommand() == PathCommand.Command.GROUPNAME) {
            Integer aWidth = theRessCookie.getGroups().get(aMajorCommand.getValue());
            if(aWidth != null) {
                return new RessImage(anImageUrl, aWidth);
            }
        }

        logger.debug("Somehow I fell thru?? For now we will deliver the w of the vpw");
        return new RessImage(anImageUrl, theRessCookie.getViewPortWidth());
    }


    /**
     * Does the client want the same width image as last requested on the given url? This function compares etags
     * @param theImage
     * @param theRequest
     * @return
     */
    private boolean checkEtag(RessImage theImage, HttpServletRequest theRequest) {
        //If the request contains no etag then return false
        String aRequestEtag = theRequest.getHeader("If-None-Match");
        if(aRequestEtag == null || aRequestEtag.length() == 0) return false;

        //check if the requested image even has a header cached, if not it does not exist and we can say "FALSE"!
        if(!ImageCache.getInstance().imageHeaderEntityExists(theImage.getSize() + theImage.getUrl())) return false;
        RessImageHeader aHeader = ImageScaler.getInstance().getImageHeader(theImage);

        if(aHeader == null) return false;
        //They are the same! OH joy to the world
        if(aHeader.getEtag().equals(aRequestEtag)) return true;

        return false;
    }

    private PathCommand getMajorCommand(List<PathCommand> theCommands) {
        for(PathCommand aCommand : theCommands) {
            if(aCommand.getCommand() == PathCommand.Command.GRID_COLUMNS) return aCommand;
            if(aCommand.getCommand() == PathCommand.Command.PERCENTAGE) return aCommand;
            if(aCommand.getCommand() == PathCommand.Command.PIXELS) return aCommand;
            if(aCommand.getCommand() == PathCommand.Command.GROUPNAME) return aCommand;
        }

        return null;
    }

    private int getWidthConfigGridNumber(String theBreakpointToLookFor, List<PathCommand> theCommands) throws Exception {
        for(PathCommand aCommand : theCommands) {
            if(aCommand.getCommand() == PathCommand.Command.WIDTH_CONFIG) {
                if(aCommand.getTrailingCommand().equals(theBreakpointToLookFor)) {
                    return Integer.parseInt(aCommand.getValue());
                }
            }
        }
        throw new Exception("No breakpoint found with the given name: " + theBreakpointToLookFor);
    }

    private String getImageUrl(List<PathCommand> theCommands) throws Exception {
        PathCommand aLastCommand = theCommands.get(theCommands.size() -1);
        if(aLastCommand.getCommand() == PathCommand.Command.URL) {
            return aLastCommand.getValue();            
        }
        throw new Exception("Something is strange, the last command was not an image url!");
    }
}
