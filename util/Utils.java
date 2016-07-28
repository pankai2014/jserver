package org.kaipan.www.socket.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Utils
{
    /**
     * get the absolute parent path for the jar file. 
     * 
     * @param o
     * @return
     */
    public static String getJarHome(Object o) 
    {
        String path  = o.getClass().getProtectionDomain()
                     .getCodeSource().getLocation().getFile();
        
        File jarFile = new File(path);
        
        return jarFile.getParentFile().getAbsolutePath();
    }
    
    /**
     * searching the specified configuration file and
     *      return a Properties
     *      
     * @param path
     * @return
     */
    public static Properties loadConfigFile( String path )
    {
        Properties property = null;
        
        File file = new File(path);
        if ( file.exists() && file.canRead() ) {
            try {
                property = new Properties();
                property.load(new FileReader(file.getAbsolutePath()));
            } 
            catch (FileNotFoundException e) {
                e.printStackTrace();
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
        } 

        return property;
    }
    
    public static String getFileExt(String file) 
    {
        int loc = file.indexOf('.');
        if ( loc == -1 ) return null;
        
        return file.substring(loc + 1).toLowerCase();
    }
}
