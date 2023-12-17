package org.minima.system.commands.base;

import java.util.ArrayList;
import java.util.Arrays;

import org.minima.objects.base.MiniData;
import org.minima.objects.base.MiniNumber;
import org.minima.system.commands.Command;
import org.minima.system.commands.CommandException;
import org.minima.utils.Crypto;
import org.minima.utils.json.JSONObject;

public class random extends Command {

	public random() {
		super("random","(size:) - Generate a random hash value, defaults to 32 bytes");
	}
	
	@Override
	public String getFullHelp() {
		return "\nrandom\n"
				+ "\n"
				+ "Generate a random hash value, defaults to 32 bytes.\n"
				+ "\n"
				+ "size: (optional)\n"
				+ "    Integer number of bytes for the hash value.\n"
				+ "\n"
				+ "Examples:\n"
				+ "\n"
				+ "random\n"
				+ "\n"
				+ "random size:64\n";	
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"size","type"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		//How big a random number
		MiniNumber size = getNumberParam("size", new MiniNumber(32));
		
		//Generate..
		MiniData rand = MiniData.getRandomData(size.getAsInt());
		
		//Now hash it
		String hashtype = getParam("type", "sha3");
		
		byte[] hash = null;
		if(hashtype.equals("sha2")) {
			hash = Crypto.getInstance().hashSHA2(rand.getBytes());
		
		}else if(hashtype.equals("sha3")) {
			hash = Crypto.getInstance().hashData(rand.getBytes());
		
		}else {
			throw new CommandException("Invalid hash type : "+hashtype);
		}
		
		//Hash it
		MiniData randhash = new MiniData(hash);
		
		JSONObject resp = new JSONObject();
		resp.put("size", size.toString());
		resp.put("random", rand.to0xString());
		resp.put("hashed", randhash.to0xString());
		resp.put("type", hashtype);
		
		ret.put("response", resp);
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new random();
	}

}
