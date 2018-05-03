package com.cros.block.model;

import java.util.Date;

import org.bson.types.ObjectId;

public class BlockInfo {

	public ObjectId _id;
	
	public String block_name;
	
	public String block_description;
	
	public String agentUserId;
	
	public String block_address;
	
	public Date create_time;

	public String txHash;
	
	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public String getBlock_name() {
		return block_name;
	}

	public void setBlock_name(String block_name) {
		this.block_name = block_name;
	}

	public String getBlock_description() {
		return block_description;
	}

	public void setBlock_description(String block_description) {
		this.block_description = block_description;
	}


	public String getAgentUserId() {
		return agentUserId;
	}

	public void setAgentUserId(String agentUserId) {
		this.agentUserId = agentUserId;
	}

	public String getBlock_address() {
		return block_address;
	}

	public void setBlock_address(String block_address) {
		this.block_address = block_address;
	}

	public Date getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}

	public String getTxHash() {
		return txHash;
	}

	public void setTxHash(String txHash) {
		this.txHash = txHash;
	}
}
