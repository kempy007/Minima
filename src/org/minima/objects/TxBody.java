package org.minima.objects;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.minima.objects.base.MiniData;
import org.minima.objects.base.MiniNumber;
import org.minima.utils.Streamable;
import org.minima.utils.json.JSONArray;
import org.minima.utils.json.JSONObject;

public class TxBody implements Streamable {

	/**
	 * The Difficulty for this TXPOW to be valid.
	 */
	public MiniData 	mTxnDifficulty = new MiniData();
	
	/**
	 * The Transaction the user is trying to send
	 */
	public Transaction	mTransaction = new Transaction();
	
	/**
	 * The Witness data for the Transaction
	 */
	public Witness		mWitness = new Witness();
	
	/**
	 * The list of the current TX-POWs the user 
	 * knows about that are not yet in the this chain.
	 */
	public ArrayList<MiniData> mTxPowIDList;
	
	/**
	 * MAGIC numbers that set the chain parameters
	 */
	public Magic mMagic = new Magic();
	
	public TxBody() {
		//List of the transctions in this block
		mTxPowIDList = new ArrayList<>();
	}

	public JSONObject toJSON() {
		JSONObject txpow = new JSONObject();
		
		txpow.put("txndiff", mTxnDifficulty.to0xString());
		txpow.put("txn", mTransaction.toJSON());
		txpow.put("witness", mWitness.toJSON());
		
		//Need to make it into a JSON array
		JSONArray txns = new JSONArray();
		for(MiniData txn : mTxPowIDList) {
			txns.add(txn.to0xString());
		}
		txpow.put("txnlist", txns);
		
		txpow.put("magic", mMagic.toJSON());
		
		return txpow;
	}

	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		mTxnDifficulty.writeDataStream(zOut);
		mTransaction.writeDataStream(zOut);
		mWitness.writeDataStream(zOut);
		
		//Write out the TXPOW List
		int len = mTxPowIDList.size();
		MiniNumber ramlen = new MiniNumber(""+len);
		ramlen.writeDataStream(zOut);
		for(MiniData txpowid : mTxPowIDList) {
			txpowid.writeHashToStream(zOut);
		}
		
		mMagic.writeDataStream(zOut);
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		mTxnDifficulty  = MiniData.ReadFromStream(zIn);
		mTransaction.readDataStream(zIn);
		mWitness.readDataStream(zIn);
		
		//Read in  the TxPOW list
		mTxPowIDList = new ArrayList<>();
		MiniNumber ramlen = MiniNumber.ReadFromStream(zIn);
		int len = ramlen.getAsInt();
		for(int i=0;i<len;i++) {
			mTxPowIDList.add(MiniData.ReadHashFromStream(zIn));
		}
		
		mMagic.readDataStream(zIn);
	}
}
