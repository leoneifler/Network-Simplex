package net_simplex;



public class Network {
	int nnodes, narcs;
	int[] demand;
	Arc[] arcarr; 
	Simptree tree;
	int[] nodeprice;
	
	public Network(int nnodes, int narcs){
		demand=new int[nnodes];
		tree=new Simptree(nnodes);
		arcarr=new Arc[narcs];
		nodeprice= new int[nnodes];
		this.nnodes=nnodes;
		this.narcs=narcs;
	}

	public boolean isInitialized(){
		if(nnodes==0 || narcs==0 || demand==null || tree==null || nodeprice==null){
			return false;
		}
		if(!tree.isInitliazied())return false;
		for(int i=0; i<arcarr.length;++i){
			if(!arcarr[i].isInitialized())return false;
		}
		return true;
	}
	public void readData(String filename){
		
	}

	

	public int maxcost() {
		int max=0;
		for(int i=0; i<arcarr.length; i++){
			if(null!=arcarr[i]){
				if(arcarr[i].cost>max)max=arcarr[i].cost;
			}
		}
		return max;
	}

	public void compPrize() {
		for(int i=1;i<this.nnodes; ++i){
			//TODO Knotenpreisarray ausrechnen
		}
	}
	
	public void initTree() {
		this.tree.succ[0]=1;
		for(int i=1;i<this.nnodes;++i){
			this.tree.depth[i]=1;
			this.tree.pred[i]=0;
			this.tree.succ[i]=i+1;
		}
	}
	

}
