package com.cros.block.model;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

public class Blockchain {

	//Blockchain
	
	private ObjectId _id;
	
	private Long ver;
	
	private String prev_block;
	
	private Long height;
	
	private int size;
	
	private Date time;
	
	private String merkleroot;
	
	private List tx;
	
	private String block_hash;
	
	private String bitcoin_tx;

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public Long getVer() {
		return ver;
	}

	public void setVer(Long ver) {
		this.ver = ver;
	}

	public String getPrev_block() {
		return prev_block;
	}

	public void setPrev_block(String prev_block) {
		this.prev_block = prev_block;
	}

	public Long getHeight() {
		return height;
	}

	public void setHeight(Long height) {
		this.height = height;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getMerkleroot() {
		return merkleroot;
	}

	public void setMerkleroot(String merkleroot) {
		this.merkleroot = merkleroot;
	}

	public List getTx() {
		return tx;
	}

	public void setTx(List tx) {
		this.tx = tx;
	}

	public String getBlock_hash() {
		return block_hash;
	}

	public void setBlock_hash(String block_hash) {
		this.block_hash = block_hash;
	}

	public String getBitcoin_tx() {
		return bitcoin_tx;
	}

	public void setBitcoin_tx(String bitcoin_tx) {
		this.bitcoin_tx = bitcoin_tx;
	}
	
	
}
