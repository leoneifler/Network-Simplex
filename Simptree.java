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

	public boolean isValidTree() {
		int test1=0;
		boolean[] test = new boolean[succ.length];
		test[test1]=true;
		for(int i:succ){
			test1=succ[test1];
			test[test1]=true;
		}
		for(int i=0;i<test.length;++i){
			if(!test[i])System.out.println(i);
		}
			
		if(test1!=0){
			//System.out.println("succ-array stimmt nicht");
			//this.printSucc();
			return false;
		}
		
		for(int i:depth){
			int test2=i;
			for(int j=0;j<depth[i];j++){
				test2=pred[test2];
			}
			if(test2!=0){
				//System.out.println("tiefe/parent von Knoten " + i + " stimmt nicht");
				return false;
			}
		}
		
		return true;
	}

	private void printSucc() {
		int temp=0;
		for(int i=0;i<this.succ.length;i++){
			//System.out.println(temp);
			temp=succ[temp];
		}
	}
	
	
}
