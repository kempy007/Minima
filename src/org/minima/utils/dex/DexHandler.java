package org.minima.utils.dex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Date;
import java.util.StringTokenizer;

import org.minima.objects.base.MiniString;
import org.minima.utils.MinimaLogger;
import org.minima.utils.json.JSONObject;

public class DexHandler implements Runnable {

	public static boolean LOGGING_ENABLED = true;
	
	Socket mSocket;
	
	public DexHandler(Socket zSocket) {
		mSocket = zSocket;
	}
	
	@Override
	public void run() {
		
		// we manage our particular client connection
		BufferedReader in 	 		 	= null; 
		PrintWriter out 	 			= null; 
		
		try {
			// Input Stream
			in = new BufferedReader(new InputStreamReader(mSocket.getInputStream(), MiniString.MINIMA_CHARSET));
			
			// Output Stream
			out = new PrintWriter(new OutputStreamWriter(mSocket.getOutputStream(), MiniString.MINIMA_CHARSET));
			
			// get first line of the request from the client
			String input = in.readLine();
			int counter = 0;
			while(input == null && counter<100){
				//Wait a sec
				Thread.sleep(1000);
				
				input = in.readLine();
				counter++;
			}
			
			//Is it still NULL
			if(input == null) {
				throw new IllegalArgumentException("Invalid NULL MDS request ");
			}
			
			// we parse the request with a string tokenizer
			StringTokenizer parse = new StringTokenizer(input);
			
			//Get the METHOD
			String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
			
			//Get the requested file
			String command = parse.nextToken();
			
			//Remove slashes..
			if(command.startsWith("/")) {
				command = command.substring(1);
			}
			if(command.endsWith("/")) {
				command = command.substring(0,command.length()-1);
			}
			
			//And finally URL decode..
			command = URLDecoder.decode(command,"UTF-8").trim();
			
			//Get the Headers..
			int contentlength = 0;
			while(input != null && !input.trim().equals("")) {
				//MinimaLogger.log("RPC : "+input);
				int ref = input.indexOf("Content-Length:"); 
				if(ref != -1) {
					//Get it..
					int start     = input.indexOf(":");
					contentlength = Integer.parseInt(input.substring(start+1).trim());
				}	
				input = in.readLine();
			}
			
			//Is it a POST request
			if(!method.equals("POST")) {
				
				//Not a valid request
				throw new IOException("Invalid request not POST");
				
			}else{
				
				//How much data
				char[] cbuf 	= new char[contentlength];
				
				//Read it ALL in
				int len,total=0;
				while( (len = in.read(cbuf,total,contentlength-total)) != -1) {
					total += len;
					if(total == contentlength) {
						break;
					}
				}
				
				//Set this..
				String dataenc 	= new String(cbuf).trim();
				String data 	= URLDecoder.decode(dataenc, "UTF-8");
		
				if(LOGGING_ENABLED) {
					MinimaLogger.log("COMMAND:"+command+" DATA:"+data);
				}
				
				//Process this request..
				JSONObject ret = DexServer.getDexManager().processRequest(command, data);
				
				//Write out echo
				writedata(out,ret.toString());
			}
		
        } catch (Exception e) {
        	MinimaLogger.log(e);
			
        }finally {
        	try {
				in.close();
				out.close();
				mSocket.close(); // we close socket connection
			} catch (Exception e) {
				MinimaLogger.log(e);
			}
		}
	}
	
	public void writedata(PrintWriter zOut, String zData) {
		
		int datalen = zData.getBytes().length;
		
		// send HTTP Headers
		zOut.println("HTTP/1.1 200 OK");
		zOut.println("Server: HTTP DEX Server 1.0");
		zOut.println("Date: " + new Date());
		zOut.println("Content-type: text/plain");
		zOut.println("Content-length: " + datalen);
		zOut.println("Access-Control-Allow-Origin: *");
		zOut.println(); // blank line between headers and content, very important !
		zOut.println(zData);
		zOut.flush(); // flush character output stream buffer
	}

}
