package com.cros.block.util;

import java.util.ArrayList;
import java.util.List;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;

/**
 * @ClassName: MerkleTree
 * @Package org.weibei.blockchain.util
 * @Description:TODO ADD FUNCTION
 * @date: 2016年12月9日 下午2:24:25
 * @author hokuny@foxmail.com
 * @version 
 */
public class MerkleTree {

	// List of transaction hashes
	List<String> txList;

	// Merkle Root
	String root;

	/**
	 * constructor
	 * 
	 * @param txList transaction List
	 */
	public MerkleTree(List<String> txList) {
		this.txList = txList;
		root = "";
	}

	/**
	 * execute merkle_tree and set root.
	 */
	public String merkle_tree() {

		List<String> tempTxList = new ArrayList<String>();

		for (int i = 0; i < this.txList.size(); i++) {
			tempTxList.add(this.txList.get(i));
		}

		List<String> newTxList = getNewTxList(tempTxList);
		while (newTxList.size() != 1) {
			newTxList = getNewTxList(newTxList);
		}

		return newTxList.get(0);
	}

	/**
	 * return Node Hash List.
	 * 
	 * @param tempTxList
	 * @return
	 */
	private List<String> getNewTxList(List<String> tempTxList) {

		List<String> newTxList = new ArrayList<String>();
		int index = 0;
		String left;
		String right;
		while (index < tempTxList.size()) {
			// left node
			left = tempTxList.get(index);
			index++;

			// right node
			if (index == tempTxList.size()) {
				// if there is an odd number of nodes, hash the last node with itself
				right = left; 
			} else {
				right = tempTxList.get(index);
			}
			String sha2HexValue = Utils.HEX.encode(Sha256Hash.hashTwice((left + right).getBytes()));
			newTxList.add(sha2HexValue);
			index++;
		}

		return newTxList;
	}

	/**
	 * Get Root
	 * 
	 * @return
	 */
	public String getRoot() {
		return this.root;
	}

}