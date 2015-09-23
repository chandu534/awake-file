/**
 * 
 */
package org.kawanfw.file.test.api.server.config;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Servr parameters
 * @author Nicolas de Pomereu
 */
public class ServerTestParms {

    public static File FILE_CONFIGURATOR_TXT = new File("c:\\.kawansoft\\FileConfigurator.txt");
    
    //public static String C_AWAKE_SERVER_ROOT_FILE_USERNAME = "C:\\.awake-server-root-file\\username\\";
    public static String C_AWAKE_SERVER_ROOT_FILE_USERNAME = getServerRootFromFile();

    /**
     * 
     */
    public ServerTestParms() {
    }

    /**
     * Build the server root file from info dynamically stored in FILE_CONFIGURATOR_TXT
     * @return
     */
    public static String getServerRootFromFile() {
    
        try {
            String content = FileUtils.readFileToString(FILE_CONFIGURATOR_TXT);
            
            if (content == null || content.equals("null")) {
        	return "c:\\";
            }
            
            String root = StringUtils.substringBefore(content, "!");
            String withUsername = StringUtils.substringAfter(content, "!");
            
            if (withUsername.equals("true")) {
        	return root + File.separator + "username" + File.separator;
            }
            else {
        	return root + File.separator;
            }
            
            
        } catch (IOException e) {
            
            e.printStackTrace();
        }
        
        return null;
    }

}
