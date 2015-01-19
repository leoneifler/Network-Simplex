package net_simplex;

public class Simptree {
	int[] depth;
	int[] succ;
	int[] pred;
	int[] arcind;
	
	public Simptree(int n){
		depth = new int[n];
		succ = new int[n];
		pred = new int[n];
		arcind = new int[n-1];
	}

	public boolean isInitliazied() {
		if(null==depth || null==succ || null==pred || null==arcind)return false;
		return true;
	}
	
	
}
