package com.nl.tudelft.rdfgearsUI.client;

import java.util.HashMap;

public class HMKey {
	public static void main(String[] args){
		new Op();
	}
	public void op(){
		
	}
}

class Op {
	
	private HashMap <HKey, HKey> t = new HashMap <HKey, HKey>();

	public Op(){
		HKey t1 = new HKey(1);
		t.put(t1, new HKey(2));
		
		t.keySet().iterator().next().setVal(3);
		System.out.println(t1.getVal());
	}
}

class HKey {
	int val = 0;
	public HKey(int _v){
		val = _v;
	}
	public int getVal(){
		return val;
	}
	public void setVal(int x){
		val = x;
	}
}
