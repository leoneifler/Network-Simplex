package net_simplex;

public class Simptree {
	int[] depth;
	int[] succ;
	int[] pred;
	
	public Simptree(int n){
		depth = new int[n];
		succ = new int[n];
		pred = new int[n];
	}

	public boolean isInitliazied() {
		if(null==depth || null==succ || null==pred)return false;
		return true;
	}
	
	
}
