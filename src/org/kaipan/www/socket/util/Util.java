package org.kaipan.www.socket.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.kaipan.www.socket.log.Logger;

public class Util
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
            	Logger.write(e.getMessage(), Logger.ERROR);
            } 
            catch (IOException e) {
            	Logger.write(e.getMessage(), Logger.ERROR);
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
    
	public static String md5(String data) throws NoSuchAlgorithmException 
    {
    	MessageDigest md5 = MessageDigest.getInstance("MD5");   
    	md5.update(data.getBytes()); 
    	
        byte[] bytes = md5.digest();
        
        StringBuilder stringBuilder = new StringBuilder();  
        for ( int i = 0; i < bytes.length; i++ ) {
	        int val = bytes[i] & 0xff;  
	        if ( val < 16 ) {
	        	stringBuilder.append(0);  
	        } 
	        else {  
	        	stringBuilder.append(Integer.toHexString(val));  
	        }
        }
        
        return stringBuilder.toString();
    }
	
	public static byte[] sha1(String data) throws NoSuchAlgorithmException 
	{
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		sha1.update(data.getBytes());

		return sha1.digest();
	}
}
