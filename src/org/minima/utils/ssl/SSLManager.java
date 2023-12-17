package org.minima.utils.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;

import org.minima.database.MinimaDB;
import org.minima.objects.base.MiniData;
import org.minima.system.params.GeneralParams;
import org.minima.utils.MinimaLogger;

public class SSLManager {

	public static File getSSLFolder() {
		return new File(GeneralParams.DATA_FOLDER,"ssl"); 
	}
	
	public static File getKeystoreFile() {
		//Where are we storing the key file..
		File sslkeyfolder = getSSLFolder(); 
		sslkeyfolder.mkdirs();
		
		//The actual Key Store..
		File sslkeyfile = new File(sslkeyfolder,"sslkeystore"); 
		
		return sslkeyfile;
	}
	
	public static void makeKeyFile() {
		
		try {
			
			//Check file and password exist
			File sslfile 		 = getKeystoreFile();
			String keystorecheck = MinimaDB.getDB().getUserDB().getString("sslkeystorepass", null);
			
			if(!sslfile.exists() || (keystorecheck==null)) {
				
				//Generate the key store..
				generateKeyStore();
						
			}else {
				
				//Try to load keystore..
				MinimaLogger.log("Loading SSL Keystore.. ");
				
				KeyStore ks = getSSLKeyStore();
				if(ks == null) {
					//Problem..
					MinimaLogger.log("Issue with keystore.. regenerate. Could be issues with UserDB..");
					
					//Some issue.. recreate..
					generateKeyStore();
				}
			}
			
		}catch(Exception exc) {
			MinimaLogger.log(exc);
		}
	}
	
	private static void generateKeyStore() throws Exception {
		
		MinimaLogger.log("Generating SSL Keystore.. "+KeyStore.getDefaultType());
		
		//Set a Random Key - and save DB
		String keystorepass = MiniData.getRandomData(32).to0xString(); 
		MinimaDB.getDB().getUserDB().setString("sslkeystorepass", keystorepass);
		MinimaDB.getDB().saveUserDB();
		
		// Create Key
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(4096);
        KeyPair keyPair 			= keyPairGenerator.generateKeyPair();
        final X509Certificate cert 	= SelfSignedCertGenerator.generate(keyPair, "SHA256withRSA", "localhost", 730);
        KeyStore createkeystore 	= SelfSignedCertGenerator.createKeystore(cert, keyPair.getPrivate());

        // Save the File
        OutputStream fos = new FileOutputStream(getKeystoreFile());
        createkeystore.store(fos, keystorepass.toCharArray());
        fos.flush();
        fos.close();
	}
	
	public static KeyStore getSSLKeyStore() {
		try {
			//Get the keystore pass
			String keystorepass = MinimaDB.getDB().getUserDB().getString("sslkeystorepass", null);
			
			// Load the keystore
	        KeyStore loadedKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        InputStream fis = new FileInputStream(getKeystoreFile());
	        loadedKeyStore.load(fis, keystorepass.toCharArray());
	        fis.close();
			
			return loadedKeyStore;
			
		}catch(Exception exc) {
			MinimaLogger.log(exc.toString());
		}
		
		return null;
	}
	
	public static KeyManagerFactory getSSLKeyFactory(KeyStore zKeyStore) {
		try {
			//Get the keystore pass
			String keystorepass = MinimaDB.getDB().getUserDB().getString("sslkeystorepass", null);
			
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(zKeyStore, keystorepass.toCharArray());

			return keyManagerFactory;
			
		}catch(Exception exc) {
			MinimaLogger.log(exc);
		}
		
		return null;
	}
}
