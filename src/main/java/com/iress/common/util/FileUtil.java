package com.iress.common.util;

import java.io.File;

public class FileUtil {
    public static final String CATALINA_HOME = "catalina.home";
    public static final String CONFIG_FILE_LOC = "/conf/Catalina/localhost/";

    //Config Files
    public static final String REDIRECT_PROPS_FILE = "redirect.properties";
    private static final String LOG_PROPS_FILE = "redirect.log4j.properties";

    private static final String TOMCAT_ROOT = getTomcatRootDirectory();

    private static String getTomcatRootDirectory( ) {
        File file = new File( String.valueOf( System.getProperty( CATALINA_HOME ) ) );
        while ( file != null ) {
            // 'bin' directory should exists within the root directory for 'most' application servers.
            File bin = new File( file, "bin" );
            if ( bin.isDirectory() ) {
                return file.getAbsolutePath();
            }
            file = file.getParentFile();
        }
        return null;
    }

    public static String getConfigFileLocation(){
        return TOMCAT_ROOT + CONFIG_FILE_LOC ;
    }

    public static String getLogFileConfig() {
        return getConfigFileLocation() + LOG_PROPS_FILE ;
    }
}
