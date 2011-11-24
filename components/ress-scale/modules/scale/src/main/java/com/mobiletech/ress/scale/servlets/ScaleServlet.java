package com.mobiletech.ress.scale.servlets;


import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.util.List;

import com.mobiletech.ress.scale.image.test.MockupImageGenerator;
import com.mobiletech.ress.scale.servlets.util.PathCommand;
import com.mobiletech.ress.scale.servlets.util.PathUtil;
import com.mobiletech.ress.scale.servlets.util.RessCookie;
import com.mobiletech.ress.scale.servlets.util.RessCookieUtil;
import com.sun.corba.se.spi.activation._ActivatorImplBase;
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

        try {
            List<PathCommand> someCommands = PathUtil.getInstance().parsePath(aRequestUrl, "ress-scale/img/");

            //Get the cookie value and send it to the Cookie parser util that will exctract the values we need to determine
            //what imagesize to return
            String aRessCookieValue = "";
            for(Cookie aCookie : request.getCookies()) {
                if("RESS".equals(aCookie.getName())) {
                    aRessCookieValue = aCookie.getValue();
                    break;
                }
            }

            RessCookie aRessCookie = RessCookieUtil.getInstance().parseRessCookie(aRessCookieValue);

            //if defaults are present in the path and values were not set from the cookie, set them now
            setDefaultRessCookieValues(aRessCookie, someCommands);

            logger.info(aRessCookie.toString());

            for(PathCommand aCommand : someCommands) {
                logger.info(aCommand.toString());
            }

            int anImageWidth = getImageWidth(aRessCookie, someCommands);
            byte[] someDataToSend = MockupImageGenerator.generateMockup(anImageWidth);
            if(someDataToSend != null) {
                response.setContentType("image/jpg");
                response.setContentLength(someDataToSend.length);
                response.getOutputStream().write(someDataToSend);
                response.getOutputStream().flush();
                response.getOutputStream().close();
            }



        } catch(Exception e) {
            logger.info("error: ", e);
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


    private int getImageWidth(RessCookie theRessCookie, List<PathCommand> theCommands) throws Exception {

        PathCommand aMajorCommand = getMajorCommand(theCommands);
        if(aMajorCommand == null) {
            logger.debug("No major command found!");
            throw new Exception("No major command found!");
        }

        if(aMajorCommand.getCommand() == PathCommand.Command.GRID_COLUMNS) {
            int aNumberOfColumnsForImage = getWidthConfigGridNumber(theRessCookie.getActiveBreakpoint(), theCommands);
            if(theRessCookie.getGridWidth() > 0) {
                //gridwidth is set, use this primarely
                return (int)(aNumberOfColumnsForImage * theRessCookie.getGridWidth());
            } else if(theRessCookie.getViewPortWidth() > 0) {
                int aGridCount = 100;
                if(theRessCookie.getGridCount() > 0) aGridCount = theRessCookie.getGridCount();
                return (int)(((double)theRessCookie.getViewPortWidth() / (double)aGridCount) * (double)aNumberOfColumnsForImage);
            }
        }

        if(aMajorCommand.getCommand() == PathCommand.Command.PERCENTAGE) {
            int aNumberOfPercentageForImage = getWidthConfigGridNumber(theRessCookie.getActiveBreakpoint(), theCommands);
            if(theRessCookie.getViewPortWidth() > 0) {
                return (int)(((double)theRessCookie.getViewPortWidth() / 100.f) * (double)aNumberOfPercentageForImage);
            }
        }

        if(aMajorCommand.getCommand() == PathCommand.Command.PIXELS) {
            if(aMajorCommand.getValue() == null) {
                //Use breakpoints to find right
                int aPixelNumber = getWidthConfigGridNumber(theRessCookie.getActiveBreakpoint(), theCommands);
                return aPixelNumber;
            } else {
                //Use the value as the pixel size..
                return Integer.parseInt(aMajorCommand.getValue());
            }
        }

        if(aMajorCommand.getCommand() == PathCommand.Command.GROUPNAME) {
            Integer aWidth = theRessCookie.getGroups().get(aMajorCommand.getValue());
            if(aWidth != null) {
                return aWidth;
            }
        }

        logger.debug("Somehow I fell thru?? For now we will deliver the w of the vpw");
        return theRessCookie.getViewPortWidth();
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
}
