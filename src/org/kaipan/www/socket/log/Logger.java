package org.kaipan.www.socket.log;

public class Logger
{
	public final static int FATAL = 50000;
	public final static int ERROR = 40000;
	public final static int WARN  = 30000;
	public final static int INFO  = 20000;
	public final static int DEBUG = 10000;
	
	public final static int ALL   = Integer.MIN_VALUE;
	
	public static void info(String message) 
	{
		System.out.println(message);
	}
	
	public static void warn(String message) 
	{
		System.out.println(message);
	}
	
	public static void debug(String message) 
	{
		
	}
	
	public static void error(String message) 
	{
		System.out.println(message);
	}
    
    public static void error(StackTraceElement[] stack) 
    {
    	for ( StackTraceElement line : stack ) {
    		System.out.println(line);
    	}
    }
}
