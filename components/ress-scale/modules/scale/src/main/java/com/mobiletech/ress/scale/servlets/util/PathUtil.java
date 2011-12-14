package com.mobiletech.ress.scale.servlets.util;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: simjohmt
 * Date: 2011-nov-24
 * Time: 08:53:55
 * Class to parse the url for the requested image
 */
public class PathUtil {
    private final Logger logger = Logger.getLogger(PathUtil.class.getName());
    private static PathUtil myInstance;

    private PathUtil() {
    }

    public static PathUtil getInstance() {
        if(myInstance == null) {
            myInstance = new PathUtil();
        }
        return myInstance;
    }

    /**
     * Logic to parse the path defined in the requirement document for what the service should be able to handles
     * @param thePath
     * @param theWebappPath
     * @return
     * @throws Exception
     */
    public List<PathCommand> parsePath(String thePath, String theWebappPath) throws Exception {
        List<PathCommand> someReturnCommands = new ArrayList<PathCommand>();

        //First, lets split the path from where the webappPath starts and stop at first element that starts with HTTP
        String aCleanedPath = cleanPath(thePath, theWebappPath);

        logger.debug("My cleaned path: " + aCleanedPath);

        //Now, lets split it on the / delimeter and do sanitycheck
        String[] aListOfCommands = splitAndSanityCheck(aCleanedPath);

        //With the strings ready, let us create a PathCommand list to return
        boolean iHavePossibleWidthCommands = false;
        boolean followingAreWidthConfig = false;
        for(String aCommand : aListOfCommands) {
            if(!followingAreWidthConfig  && !iHavePossibleWidthCommands) {
                PathCommand aPathCommand = parseSingleCommand(aCommand);
                if(aPathCommand.getCommand() == PathCommand.Command.FALLBACK_BREAKPOINT) {
                    iHavePossibleWidthCommands = true;
                } else if(aPathCommand.getCommand() == PathCommand.Command.UNKNOWN) {
                    //There cannot be an "unknown" command at this point!
                    logger.debug("Illegal command: " + aCommand);
                    throw new Exception("Illegal command: " + aCommand);
                }
                someReturnCommands.add(aPathCommand);
            } else if(!followingAreWidthConfig) {
                PathCommand aPathCommand = parseSingleCommand(aCommand);
                if(aPathCommand.getCommand() == PathCommand.Command.GRID_COLUMNS) {
                    followingAreWidthConfig = true;
                    someReturnCommands.add(aPathCommand);
                } else if(aPathCommand.getCommand() == PathCommand.Command.PERCENTAGE) {
                    followingAreWidthConfig = true;
                    someReturnCommands.add(aPathCommand);
                } else if(aPathCommand.getCommand() == PathCommand.Command.PIXELS) {
                    followingAreWidthConfig = true;
                    someReturnCommands.add(aPathCommand);
                } else if(aPathCommand.getCommand() == PathCommand.Command.UNKNOWN) {
                    followingAreWidthConfig = true;
                    PathCommand aWidthCommand = parseWidthConfigCommand(aCommand);
                    someReturnCommands.add(aWidthCommand);                    
                } else {
                    logger.debug("Bad path when parsing grid width config: " + thePath);
                    throw new Exception("Bad path when parsing grid width config: " + thePath);
                }
            } else {
                PathCommand aPathCommand = parseWidthConfigCommand(aCommand);
                someReturnCommands.add(aPathCommand);
            }
        }

        //Lets not forget the http url to the image
        String anImageUrl = parseHttpUrlFromPath(thePath); 
        PathCommand anImageCommand = new PathCommand(PathCommand.Command.URL, null, anImageUrl, null);
        someReturnCommands.add(anImageCommand);

        return someReturnCommands;
    }

    /**
     *
     * @param thePath
     * @param theWebappPath
     * @return A string containing only the commands, easily splittable on /
     * @throws Exception
     */
    private String cleanPath(String thePath, String theWebappPath) throws Exception {
        int anIndexOfHttp = thePath.indexOf("http://");
        if(anIndexOfHttp == -1) {
            logger.debug("No http-url found in the path, aborting... The path: "  + thePath);
            throw new Exception("No http-url found in the path, aborting... The path: "  + thePath);
        }

        int anIndexOfWebappPath = thePath.indexOf(theWebappPath);
        if(anIndexOfWebappPath == -1) {
            logger.debug("No webapp-path found in the path, aborting... The path: "  + thePath + " the webapppath: " + theWebappPath);
            throw new Exception("No webapp-path found in the path, aborting... The path: "  + thePath + " the webapppath: " + theWebappPath);
        }

        return thePath.substring(anIndexOfWebappPath + theWebappPath.length(), anIndexOfHttp);
    }

    /**
     *
     * @param thePath
     * @return Extracts the http url from the path
     * @throws Exception
     */
    private String parseHttpUrlFromPath(String thePath) throws Exception {
        int anIndexOfHttp = thePath.indexOf("http://");
        if(anIndexOfHttp == -1) {
            logger.debug("No http-url found in the path, aborting... The path: "  + thePath);
            throw new Exception("No http-url found in the path, aborting... The path: "  + thePath);
        }

        return thePath.substring(anIndexOfHttp);
    }


    /**
     *
     * @param theCommandString
     * @return A list that contains all the single commands, will throw exception if there are empty commands
     * @throws Exception
     */
    private String[] splitAndSanityCheck(String theCommandString) throws Exception {
        String[] aReturnArray = theCommandString.split("/");
        for(String aString : aReturnArray) {
            if(aString == null || aString.length() == 0) throw new Exception("Bad command: " + theCommandString);
        }
        return aReturnArray;
    }


    /**
     *
     * @param theCommandString
     * @return A populated pathcommand, might have UNKNOWN as command, if so, might be a width config
     * @throws Exception
     */
    private PathCommand parseSingleCommand(String theCommandString) throws Exception {
        PathCommand.Command aPossibleCommand = PathCommand.Command.UNKNOWN;
        aPossibleCommand = aPossibleCommand.eval(theCommandString);
        //If it is still unknown, dont bother to do anything more with it, it might be reevaluated later
        if(aPossibleCommand == PathCommand.Command.UNKNOWN) return new PathCommand(aPossibleCommand, null, null, null);

        //So it was an known command, lets fill out the rest
        PathCommand aReturnCommand = new PathCommand(aPossibleCommand, null, null, null);
        String[] aSplitCommand = theCommandString.split("_");
        if(aSplitCommand.length > 1) {
            String aValue = aSplitCommand[1];
            String aTrailingValue = null;
            String[] aPossibleSplit = aValue.split("-");
            if(aPossibleSplit.length > 1) {
                aValue = aPossibleSplit[0];
                aTrailingValue = aPossibleSplit[1];
            }
            aReturnCommand.setValue(aValue);
            aReturnCommand.setTrailingValue(aTrailingValue);
        }

        return aReturnCommand;
    }

    private PathCommand parseWidthConfigCommand(String theCommandString) throws Exception {
        String[] aSplitCommand = theCommandString.split("_");
        if(aSplitCommand.length != 2) {
            logger.debug("Bad width configuration: " + theCommandString);
            throw new Exception("Bad width configuration: " + theCommandString);
        }

        String aName = aSplitCommand[0];
        String aValue = aSplitCommand[1];

        PathCommand aReturnCommand = new PathCommand(PathCommand.Command.WIDTH_CONFIG, aName, aValue, null);

        return aReturnCommand;
    }

}
