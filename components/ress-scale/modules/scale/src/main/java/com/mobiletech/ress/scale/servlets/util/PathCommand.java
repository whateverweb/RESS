package com.mobiletech.ress.scale.servlets.util;

/**
 * Created by IntelliJ IDEA.
 * User: simjohmt
 * Date: 2011-nov-24
 * Time: 08:55:35
 * To change this template use File | Settings | File Templates.
 */
public class PathCommand {

    public enum Command {
        VIEWPORT,
        GROUPNAME,
        FALLBACK_BREAKPOINT,
        GRID_COLUMNS,
        WIDTH_CONFIG,
        PIXELS,
        URL,
        PERCENTAGE,
        UNKNOWN;

        Command eval(String theEvaluated) {
            if(theEvaluated != null) {
                if(theEvaluated.startsWith("vpw")) return VIEWPORT;
                if(theEvaluated.startsWith("gn")) return GROUPNAME;
                if(theEvaluated.startsWith("bp")) return FALLBACK_BREAKPOINT;
                if(theEvaluated.startsWith("gr")) return GRID_COLUMNS;
                if(theEvaluated.startsWith("px")) return PIXELS;
                if(theEvaluated.startsWith("pc")) return PERCENTAGE;
                if(theEvaluated.startsWith("http://")) return URL;
                //NOTE! WIDTH_CONFIG is not forgotten, it cannot be determined based on a single path command, all
                //commands after a GRID_COLUMNS or a FALLBACK_BREAKPOINT (w/o a gr after) are considered to be a
                //width config
            }
            return UNKNOWN;    
        }
    };

    private Command myCommand;
    private String myTrailingCommand;
    private String myValue;
    private String myTrailingValue;



    public PathCommand(Command theCommand, String theTrailingCommand, String theValue, String theTrailingValue) {
        myCommand = theCommand;
        myValue = theValue;
        myTrailingValue = theTrailingValue;
        myTrailingCommand = theTrailingCommand;
    }

    public Command getCommand() {
        return myCommand;
    }

    public void setCommand(Command theCommand) {
        myCommand = theCommand;
    }

    public String getValue() {
        return myValue;
    }

    public void setValue(String theValue) {
        myValue = theValue;
    }

    public String getTrailingValue() {
        return myTrailingValue;
    }

    public void setTrailingValue(String theTrailingValue) {
        myTrailingValue = theTrailingValue;
    }

    public String getTrailingCommand() {
        return myTrailingCommand;
    }

    public void setTrailingCommand(String theTrailingCommand) {
        myTrailingCommand = theTrailingCommand;
    }

    @Override
    public String toString(){
        return "[command: " + myCommand.toString() + " trailingCommand: " + myTrailingCommand + " value: " + myValue + " trailingValue: " + myTrailingValue +  "]";        
    }
}
