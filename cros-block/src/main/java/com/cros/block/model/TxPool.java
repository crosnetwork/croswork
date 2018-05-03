package com.cros.block.model;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

public class TxPool {

	//TxPool
	private ObjectId _id;
	
	private int ver;
	
	private int vin_size;
	
	private int vout_size;
	
	private Date tx_time;
	
	private List in;
	
	private List out;
	
	private String status;
	
	private String tx_id;
	
	private String scriptSig;
}
