package com.cros.block.model;

import org.bson.types.ObjectId;

public class Wallet {

	//Wallet
	private ObjectId _id;
	
	private String userPublicKey;
	
	private String userPrivateKey;
	
	private String userWalletAddress;
	
	private String agentUserId;

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public String getUserPublicKey() {
		return userPublicKey;
	}

	public void setUserPublicKey(String userPublicKey) {
		this.userPublicKey = userPublicKey;
	}

	public String getUserPrivateKey() {
		return userPrivateKey;
	}

	public void setUserPrivateKey(String userPrivateKey) {
		this.userPrivateKey = userPrivateKey;
	}

	public String getUserWalletAddress() {
		return userWalletAddress;
	}

	public void setUserWalletAddress(String userWalletAddress) {
		this.userWalletAddress = userWalletAddress;
	}

	public String getAgentUserId() {
		return agentUserId;
	}

	public void setAgentUserId(String agentUserId) {
		this.agentUserId = agentUserId;
	}


}
