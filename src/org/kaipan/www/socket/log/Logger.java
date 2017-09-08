package org.kaipan.www.socket.log;

public class Logger
{
	public final static int FATAL = 50000;
	public final static int ERROR = 40000;
	public final static int WARN  = 30000;
	public final static int INFO  = 20000;
	public final static int DEBUG = 10000;
	
    public static void write(String str) 
    {
        System.out.println(str);
    }
    
    public static void write(String str, int level) 
    {
        System.out.println(str);
    }
}
