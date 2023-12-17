package org.minima.system.mds.runnable;

import java.io.File;
import java.io.IOException;

import org.minima.objects.base.MiniString;
import org.minima.system.mds.MDSManager;
import org.minima.system.mds.handler.CMDcommand;
import org.minima.system.mds.handler.NOTIFYcommand;
import org.minima.system.mds.runnable.api.APIService;
import org.minima.utils.MiniFile;
import org.minima.utils.MinimaLogger;
import org.minima.utils.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJSON;
import org.mozilla.javascript.Scriptable;

public class MDSJS {
	
	/**
	 * Which MiniDAPP does this Contect apply to
	 */
	String 		mMiniDAPPID;
	String 		mMiniDAPPName;
	
	/**
	 * JS Context and Scope
	 */
	Context mContext;
	Scriptable 	mScope;
	
	/**
	 * Maxin MInima callback
	 */
	Function mMainCallback;
	
	/**
	 * Maxin MDS manager - for SQL calls
	 */
	MDSManager mMDS;
	
	/**
	 * The NET Functions
	 */
	public NETService net;
	
	/**
	 * The COMMS service 
	 */
	public COMMSService comms;
	
	/**
	 * The FILE service
	 */
	public FILEService file;
	
	/**
	 * The KeyPair service
	 */
	public KEYPAIRService keypair;
	
	/**
	 * The API Service
	 */
	public APIService api;
	
	/**
	 * Main Constructor
	 * 
	 * @param zMDS
	 * @param zMiniDAPPID
	 * @param zMiniName
	 * @param zContext
	 * @param zScope
	 */
	public MDSJS(MDSManager zMDS, String zMiniDAPPID, String zMiniName,  Context zContext, Scriptable zScope) {
		mMDS			= zMDS;
		mMiniDAPPID		= zMiniDAPPID;
		mMiniDAPPName	= zMiniName;
		mContext 		= zContext;
		mScope 			= zScope;
		net 			= new NETService(zMiniDAPPID, zMiniName, zContext, zScope);
		comms			= new COMMSService(mMDS, zMiniDAPPID, zMiniName, zContext, zScope);
		file			= new FILEService(mMDS, zMiniDAPPID, zMiniName, zContext, zScope);
		keypair			= new KEYPAIRService(mMDS, zMiniDAPPID, zMiniName, zContext, zScope);
		api				= new APIService(mMDS, zMiniDAPPID, zMiniName, zContext, zScope);
	}
	
	public String getMiniDAPPID() {
		return mMiniDAPPID;
	}
	
	public void shutdown() {
		//And now shut it down..
		mContext.exit();
	}
	
	/**
	 * Main Callback for Minima events
	 */
	public void callMainCallback(JSONObject zEvent) {

		//Forward the message as a Native JS JSONObject
		if(mMainCallback != null) {
			
			//Call the main MDS Function in JS
			mMainCallback.call(mContext, mScope, mScope, makeNativeJSONArgs(zEvent));
		}
	}
	
	/**
	 * Simple Log
	 */
	public void log(String zMessage) {
		log(zMessage, false);
	}
	
	public void log(String zMessage, boolean zNotifyAll) {
		if(!zNotifyAll) {
			MinimaLogger.log("MDS_"+mMiniDAPPName+"_"+mMiniDAPPID+" > [NOTIFY:"+zNotifyAll+"] "+zMessage, zNotifyAll);
		}else {
			MinimaLogger.log("MDS_"+mMiniDAPPName+"_"+mMiniDAPPID+" > "+zMessage, zNotifyAll);
		}
	}
	
	/**
	 * Init Call
	 */
	public void init(Function zCallback) {
		
		//Store this for later
		mMainCallback = zCallback;
		
		//Create the init message
		JSONObject init = new JSONObject();
		init.put("event", "inited");
	
		//Send to the Runnable
		callMainCallback(init);
		
		/**
		 * FOR NOW -ADD A SCEOND CALL!
		 */
		//Create the init message
		JSONObject initnew = new JSONObject();
		initnew.put("event", "MDSINIT");
	
		//Send to the Runnable
		callMainCallback(initnew);
	}
	
	/**
	 * The Main CMD function
	 */
	public void cmd(String zCommand) {
		cmd(zCommand, null);
	}
	
	public void cmd(String zCommand, Function zCallback) {
	
		//Create a Command
		CMDcommand cmd = new CMDcommand(mMiniDAPPID, zCommand);
		
		//Run it
		String result  = cmd.runCommand();
		
		//Send Info Back
		if(zCallback == null) {
			return;
		}
		
		//The arguments
		Object[] args = { NativeJSON.parse(mContext, mScope, result, new NullCallable()) };
		
		//Call the main MDS Function in JS
		zCallback.call(mContext, mScope, mScope, args);
	}
	
	/**
	 * SQL Function
	 */
	public void sql(String zCommand) {
		sql(zCommand, null);
	}
	
	public void sql(String zSQL, Function zCallback) {
		
		//Run the SQL
		JSONObject sqlresult = mMDS.runSQL(mMiniDAPPID, zSQL);
		
		if(zCallback == null) {
			return;
		}
		
		//Call the main MDS Function in JS
		zCallback.call(mContext, mScope, mScope, makeNativeJSONArgs(sqlresult));
	}
	
	/**
	 * Notify Function
	 */
	public void notify(String zText) {
			
		//Create a Command
		NOTIFYcommand notify = new NOTIFYcommand(mMiniDAPPID, mMiniDAPPName, zText, true);
		notify.runCommand();
	}
	
	public void notifycancel() {
		
		//Create a Command
		NOTIFYcommand notify = new NOTIFYcommand(mMiniDAPPID, "", "", false);
		notify.runCommand();
	}
	
	/**
	 * Load a JS file
	 */
	public void load(String zFile) {
		
		//Check no ..
		if(zFile.indexOf("..")!=-1) {
			log("Invalid file for load as has .. in location");
			return;
		}
		
		//Get base folder..
		File base = mMDS.getMiniDAPPWebFolder(mMiniDAPPID);
		
		//Load a file..
		File loadfile = new File(base,zFile);
		
		//Load the code..
		byte[] codedata;
		try {
			codedata = MiniFile.readCompleteFile(loadfile);
		} catch (IOException e) {
			//File not found..
			MinimaLogger.log(e.toString());
			return;
		}
		
		//Convert to CODE..
		String code = new String(codedata, MiniString.MINIMA_CHARSET);
		
		//Run in scope..
		mContext.evaluateString(mScope, code, "<mds_"+mMiniDAPPID+"_loaded_"+zFile+">", 1, null);
	}
	
	/**
	 * Helper to create a JS JSON
	 */
	public Object[] makeNativeJSONArgs(JSONObject zJSON) {
		
		//Create the Native JSON Object
		Object[] args =  { NativeJSON.parse(mContext, mScope, zJSON.toString(), new NullCallable()) };
		
		return args;
	}
}
