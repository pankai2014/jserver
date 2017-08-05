package org.kaipan.www.socket.controller;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.kaipan.www.socket.protocol.http.HttpRequest;

public class ToutiaoController implements IController
{
	public String run(HttpRequest request) 
	{
		if ( request.get.containsKey("video_id") == false ) {
			return null;
		}
		
		ScriptEngineManager manager = new ScriptEngineManager();   
		ScriptEngine engine = manager.getEngineByName("javascript");     

		String jarHome  =  new File("").getAbsolutePath();
		String filename = jarHome + "/static/toutiao.js";
		
		FileReader reader = null;
		try {
			reader = new FileReader(filename);
			engine.eval(reader);
		
			if ( engine instanceof Invocable ) {    
				Invocable invoke = (Invocable)engine;    
	
				String result = (String)invoke.invokeFunction("crc32", request.get.get("video_id"));    

				return result;
			}   

			reader.close();
		} 
		catch (ScriptException | NoSuchMethodException | IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
