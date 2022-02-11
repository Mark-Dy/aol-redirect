package com.iress.servlet;

import com.iress.common.util.FileUtil;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;


public class RedirectServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(RedirectServlet.class);

    //Property Keys
    private static final String ENDPOINT_MAPPING_KEY = "EndpointMapping";
    private static final String ENABLE_OUTAGE_KEY = "EnableOutage";
    private static final String OUTAGE_URL_KEY = "OutageUrl";

    //Configuration Endpoint Mappings
    final static HashMap<String, String> endPointMapping = new HashMap();
    final static Properties props = new Properties();

    static boolean isOutageEnabled = false;
    static String outageUrl = "";

    @Override
    public void init(ServletConfig config) throws ServletException {
        LOGGER.info("Redirect Servlet Initialization Started.");

        super.init(config);

        try {
            LOGGER.info("Loading Configuration...");
            //Initialise the config
            reloadConfig();

        } catch (IOException e) {
            LOGGER.error("An Error has occurred during servlet initialisation", e);
            throw new ServletException(e);
        }
        LOGGER.info("Redirect Servlet Initialization Completed!");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String serverName = req.getServerName();

        if(isOutageEnabled && !"".equals(outageUrl)) {
            LOGGER.info("Redirecting to outage page: " + outageUrl);
            resp.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            resp.sendRedirect(outageUrl);
        } else {
            String redirectUrl = endPointMapping.get(serverName);
            if(redirectUrl != null) {
                LOGGER.info("Redirecting: " + serverName +" to: " + redirectUrl);
                resp.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                resp.sendRedirect(redirectUrl);
            }
            else {
                String errorMsg = "Unable to map " + serverName + " to a URL";
                LOGGER.error(errorMsg);
                throw new ServletException(errorMsg);
            }
        }

    }

    public static void reloadConfig() throws IOException {
        props.clear();
        LOGGER.info("Reading Property file...");
        props.load(new FileInputStream(FileUtil.getConfigFileLocation() + FileUtil.REDIRECT_PROPS_FILE));

        //Setting Outage Variables
        isOutageEnabled = Boolean.valueOf(props.getProperty(ENABLE_OUTAGE_KEY, "false") );
        outageUrl = props.getProperty(OUTAGE_URL_KEY,"");

        HashMap <String, String> updatedMappings = new HashMap();
        String endPoints = props.getProperty(ENDPOINT_MAPPING_KEY);

        if(endPoints != null) {
            //Build new endpoint mapping
            String[] endpointArr = endPoints.split(";");
            Arrays.stream(endpointArr).forEach(ep -> updatedMappings.put((ep.split("="))[0], (ep.split("="))[1]));
        }

        LOGGER.info("Updating Endpoint Mappings...");
        endPointMapping.clear();
        endPointMapping.putAll(updatedMappings);
    }
}
