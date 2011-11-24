package com.mobiletech.ress.scale.servlets;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;


import com.mobiletech.license.License;
import com.mobiletech.wurfl.Wurfl;


public class DeviceRepoInit extends HttpServlet {

    private final Logger logger = Logger.getLogger(DeviceRepoInit.class.getName());
    private static boolean initialized = false;

    @Override
    public void init(ServletConfig config) throws ServletException {
        if (!initialized) {
            initialized = true;

            if (isMobileEnabled() == false) {
                return;
            }
            super.init(config);
            //Check license after we've tried to initilize the devicelib
            License l;
            try {
                l = License.getInstance();
                if (!l.isValid()) {
                    System.err.println("The Mobiletech License is not valid. Please contact support@mobiletech.no");
                    logger.severe("The Mobiletech License is not valid. Please contact support@mobiletech.no");
                    throw new RuntimeException("The Mobiletech License is not valid. Please contact support@mobiletech.no");
                } else if (l.getMode().equalsIgnoreCase("DEMO")) {
                    System.out.println("This Mobiletech product is running in DEMO mode.");
                    logger.warning("This Mobiletech product is running in DEMO mode.");
                } else if (l.isExpired()) {
                    System.err.println("This Mobiletech license is expired. Contact support@mobiletech.no");
                    logger.severe("This Mobiletech license is expired. Contact support@mobiletech.no");
                }
            } catch (Exception ex) {
                System.err.println("The Mobiletech License could not be initialized. Please contact support@mobiletech.no");
                logger.severe("The Mobiletech License could not be initialized. Please contact support@mobiletech.no");
                throw new ServletException("The Mobiletech License could not be initialized. Please contact support@mobiletech.no", ex);
            }

            // make sure the device repo is loaded (singleton pattern)
            logger.info("Starting Mobiletech Devicelib, please be patient. This may take several minutes if there are new devicelib files that needs to be downloaded and updated.");
            Wurfl wurfl = null;

            String devicelibLocation = null;
            List<String> devicelibFiles = null;

            devicelibFiles = new LinkedList<String>();
            devicelibFiles.addAll(Arrays.asList("wurfl.xml,wurfl_patch_mt.xml,wurfl_patch_mt_robots.xml,wurfl_patch_mt_web_browsers.xml,wurfl_patch_mt_2.xml".split(",")));


            String contextName = config.getServletContext().getInitParameter("webAppRootKey");
            if (contextName == null) {
                contextName = config.getServletContext().getServletContextName();
            }
            wurfl = Wurfl.getInstance(System.getProperty("java.io.tmpdir")
                    + File.separator + System.getProperty("user.name") + "-"
                    + contextName, devicelibFiles);

            try {
                logger.info("Start to wait for background building of Mobiletech Devicelib Index");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                logger.info("Was interrupted...");
            }
            while (wurfl.isBuilding()) {
                logger.info("Still waiting for Mobiletech Devicelib Index to be built (this delay will only occur on webapp startup)");
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    logger.info("Was interrupted...");
                }
            }
            logger.info("Mobiletech Devicelib Index built and ready!");

        }


    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (isMobileEnabled() == false) {
            return;
        }

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        // the reload operation
        if(request.getParameter("function")!=null) {
            if("reload".equalsIgnoreCase(request.getParameter("function"))) {
                Wurfl wurfl = Wurfl.getInstance();
                if(wurfl!=null) {
                    wurfl.updateRepository();
                    out.println(wurfl.getStats());
                }
            }
        } else {
            out.println("OK, your request didn't really do anything");
        }
    }

    /**
     * We are going to perform the same operations for POST requests as for GET
     * methods, so this method just sends the request to the doGet method.
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (isMobileEnabled() == false) {
            return;
        }
        doGet(request, response);
    }

    private boolean isMobileEnabled() {
        String mobileEnabled = System.getProperty("mobile.enabled");
        if (mobileEnabled != null && mobileEnabled.equalsIgnoreCase("false")) {
            return false;
        }
        return true;
    }
}

