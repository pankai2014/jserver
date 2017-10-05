package org.kaipan.jserver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileInput 
{
	public static void main(String[] args) 
	{
		//System.out.println();
		File file = new File("/home/will/Develop/workspace/index.html");
		
		try {
			FileInputStream in = new FileInputStream(file);
			
			int b;
			while((b=in.read())!=-1){//read()方法
				//无符号整数参数所表示的值以十六进制
				//Integer.toHexString(b)
				  System.out.print(b + " ");
				}
			
			System.out.println(" ");
			
			in.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			FileInputStream in = new FileInputStream(file);
			Long length = file.length();
			byte[] bytes = new byte[length.intValue()];
			
			BufferedInputStream bufin = new BufferedInputStream(in);
			
			while((bufin.read(bytes))!=-1){//read()方法
				//无符号整数参数所表示的值以十六进制
				//Integer.toHexString(b)
				  //System.out.println(bytes);
					System.out.println(new String(bytes, 0, length.intValue()));
				}
			
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
