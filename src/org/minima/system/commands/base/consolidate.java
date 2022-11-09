package org.minima.system.commands.base;

import java.util.ArrayList;
import java.util.Arrays;

import org.minima.database.MinimaDB;
import org.minima.database.txpowtree.TxPoWTreeNode;
import org.minima.database.wallet.ScriptRow;
import org.minima.objects.Coin;
import org.minima.objects.base.MiniData;
import org.minima.objects.base.MiniNumber;
import org.minima.system.brains.TxPoWSearcher;
import org.minima.system.commands.Command;
import org.minima.system.commands.CommandException;
import org.minima.system.commands.send.send;
import org.minima.system.params.GlobalParams;
import org.minima.utils.MinimaLogger;
import org.minima.utils.json.JSONArray;
import org.minima.utils.json.JSONObject;

public class consolidate extends Command {

	public consolidate() {
		super("consolidate","[tokenid:] (coinage:) (maxcoins:) (maxsigs:) (burn:) (debug:) (dryrun:) - Consolidate coins by sending them back to yourself");
	}
	
	@Override
	public ArrayList<String> getValidParams(){
		return new ArrayList<>(Arrays.asList(new String[]{"tokenid","coinage","maxcoins",
				"maxsigs","burn","debug","dryrun"}));
	}
	
	@Override
	public JSONObject runCommand() throws Exception {
		JSONObject ret = getJSONReply();
		
		//The tokenid
		String tokenid = getParam("tokenid");
		
		//Is there a burn
		MiniNumber burn = getNumberParam("burn", MiniNumber.ZERO);
		
		//Is this a dry run
		boolean debug 	= getBooleanParam("debug", false);
		boolean dryrun 	= getBooleanParam("dryrun", false);
		if(dryrun) {
			debug = true;
		}
		
		//Get the tip of the tree
		TxPoWTreeNode tip 	= MinimaDB.getDB().getTxPoWTree().getTip();
		
		//Get the parent deep enough for valid confirmed coins
		int confdepth = GlobalParams.MINIMA_CONFIRM_DEPTH.getAsInt();
		for(int i=0;i<confdepth;i++) {
			tip = tip.getParent();
			if(tip == null) {
				//Insufficient blocks
				ret.put("status", false);
				ret.put("message", "Insufficient blocks..");
				return ret;
			}
		}
		
		//How old do the coins need to be..
		MiniNumber coinage = getNumberParam("coinage", MiniNumber.ZERO);
				
		//Lets build a transaction..
		ArrayList<Coin> foundcoins	= TxPoWSearcher.getRelevantUnspentCoins(tip,tokenid,true);
		ArrayList<Coin> relcoins 	= new ArrayList<>();
		
		//Now make sure they are old enough
		MiniNumber mincoinblock = tip.getBlockNumber().sub(coinage);
		for(Coin relc : foundcoins) {
			if(relc.getBlockCreated().isLessEqual(mincoinblock)) {
				relcoins.add(relc);
			}
		}
		
		//Sort coins via same address - since they require the same signature
		relcoins = send.orderCoins(relcoins);
		
		//How many coins are there
		int totcoins = relcoins.size();
		if(totcoins<3) {
			throw new CommandException("Not enough coins ("+totcoins+") to consolidate");
		}
		
		//Maximum number of coins and signatures
		int MAX_SIGS 	= getNumberParam("maxsigs", new MiniNumber(5)).getAsInt();
		int MAX_COINS 	= getNumberParam("maxcoins", new MiniNumber(20)).getAsInt();
		
		String 		currentaddress 	= "";
		MiniNumber 	totalamount 	= MiniNumber.ZERO;
		int 		totalsigs 		= 0;
		int 		totalcoins 		= 0;
		for(Coin cc : relcoins) {
			
			//This coins address
			String coinaddress = cc.getAddress().to0xString();
			
			//The Amount
			MiniNumber coinamount = cc.getAmount();
			if(!cc.getTokenID().to0xString().equals("0x00")) {
				coinamount = cc.getToken().getScaledTokenAmount(cc.getAmount());
			}
			
			//Is it a new address
			if(!currentaddress.equals(coinaddress)) {
				
				//Are we at the limit
				if(totalsigs+1>MAX_SIGS) {
					if(debug) {
						MinimaLogger.log("Consolidate - max sigs reached "+totalsigs);
					}
					break;
				}
				
				//New address = new signature
				currentaddress = coinaddress;
				totalsigs++;
			}
			
			//Add to the total..
			totalamount = totalamount.add(coinamount);
			
			//One more coin
			totalcoins++;
			
			if(debug) {
				MinimaLogger.log("Consolidate - add coin "+coinamount+" totalcoins:"+totalcoins+"  totalsigs:"+totalsigs+" coinid:"+cc.getCoinID().to0xString());
			}
			
			//Do checks..
			if(totalcoins>=MAX_COINS) {
				if(debug) {
					MinimaLogger.log("Consolidate - max coins reached "+totalcoins);
				}
				break;
			}
		}
		
		//Get one of your addresses
		ScriptRow newwalletaddress 	= MinimaDB.getDB().getWallet().getDefaultAddress();
		MiniData myaddress 			= new MiniData(newwalletaddress.getAddress());
		
		//Construct the command
		String command = "send coinage:"+coinage.toString()+" split:2 dryrun:"+dryrun+" debug:"+debug+" burn:"+burn.toString()
				+" amount:"+totalamount.toString()+" address:"+myaddress.to0xString()+" tokenid:"+tokenid;
		
		if(debug) {
			MinimaLogger.log("Consolidate command : "+command);
		}
		
		JSONArray result 		= Command.runMultiCommand(command);
		JSONObject sendresult 	= (JSONObject) result.get(0); 
		if((boolean) sendresult.get("status")) {
			ret.put("response", sendresult.get("response"));
		}else {
			ret.put("status", false);
			if(sendresult.get("message") != null) {
				ret.put("message", sendresult.get("message"));
			}else if(sendresult.get("error") != null) {
				ret.put("message", sendresult.get("error"));
			}else {
				ret.put("message", sendresult);
			}
		}
		
		return ret;
	}

	@Override
	public Command getFunction() {
		return new consolidate();
	}
}
