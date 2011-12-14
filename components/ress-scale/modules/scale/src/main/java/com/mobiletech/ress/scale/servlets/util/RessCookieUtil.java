package com.mobiletech.ress.scale.servlets.util;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: simjohmt
 * Date: 2011-nov-24
 * Time: 10:52:53
 * Helperclass that parses the RESS cookie and creates a RessCookie object containing the data.
 */
public class RessCookieUtil {
    private final Logger logger = Logger.getLogger(RessCookieUtil.class.getName());
    private static RessCookieUtil myInstance;

    private RessCookieUtil() {
    }

    public static RessCookieUtil getInstance() {
        if(myInstance == null) {
            myInstance = new RessCookieUtil();
        }
        return myInstance;
    }

    public RessCookie parseRessCookie(String theCookieToParse) throws Exception {
        RessCookie aReturnValue = new RessCookie();

        String[] someValues = theCookieToParse.split("\\|");

        for(String aValue : someValues) {
            if(aValue.startsWith("vpw.")) {
                String[] aSplitValue = aValue.split("\\.");
                if(aSplitValue.length == 2) {
                    aReturnValue.setViewPortWidth(Integer.parseInt(aSplitValue[1]));    
                } else if(aSplitValue.length == 3) {
                    aReturnValue.setViewPortWidth((int)Double.parseDouble(aSplitValue[1] + "." + aSplitValue[2]));
                }
            } else if(aValue.startsWith("bp.")) {
                String[] aSplitValue = aValue.split("\\.");
                if(aSplitValue.length == 2) {
                    aReturnValue.setActiveBreakpoint(aSplitValue[1]);    
                }
            } else if(aValue.startsWith("gw.")) {
                String[] aSplitValue = aValue.split("\\.");
                if(aSplitValue.length == 2) {
                    aReturnValue.setGridWidth(Double.parseDouble(aSplitValue[1].replaceAll(", ", ".")));
                } else if(aSplitValue.length == 3) {
                    aReturnValue.setGridWidth(Double.parseDouble(aSplitValue[1] + "." + aSplitValue[2]));
                }
            } else if(aValue.startsWith("g.")) {
                String[] aSplitValue = aValue.split("\\.");
                if(aSplitValue.length == 3) {
                    aReturnValue.getGroups().put(aSplitValue[1], Integer.parseInt(aSplitValue[2]));
                }
            } else if(aValue.startsWith("gr.")) {
                String[] aSplitValue = aValue.split("\\.");
                if(aSplitValue.length == 2) {
                    aReturnValue.setGridCount(Integer.parseInt(aSplitValue[1]));
                }
            }

        }

        return aReturnValue;
    }

}
