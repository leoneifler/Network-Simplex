package net_simplex;



public class Network {
	int nnodes, narcs;
	int[] demand;
	Arc[] arcarr; 
	Simptree tree;
	int[] nodeprice;
	
	/**
	 * the standard constructor, generating a new network
	 * @param nnodes number of nodes in the networl
	 * @param narcs number of arcs in the network
	 */
	public Network(int nnodes, int narcs){
		demand=new int[nnodes];
		tree=new Simptree(nnodes);
		arcarr=new Arc[narcs];
		nodeprice= new int[nnodes];
		this.nnodes=nnodes;
		this.narcs=narcs;
	}
/**
 * checks if all elements of the network have been initialized
 * @return true if the network is initialized
 */
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
	

	
/**
 * computes the highest cost among arc-costs excluding the arcs that have been artificially added.
 * @return the value of the highest cost
 */
	public int maxcost() {
		int max=0;
		for(int i=0; i<arcarr.length-nnodes+1; i++){
			if(null!=arcarr[i]){
				if(arcarr[i].cost>max)max=arcarr[i].cost;
			}
		}
		return max;
	}

	/**
	 * Initializes the tree-solution.
	 */
	public void initTree() {
		this.tree.succ[0]=1;
		for(int i=1;i<this.nnodes;++i){
			this.tree.depth[i]=1;
			this.tree.pred[i]=0;
			this.tree.succ[i]=i+1;
		}
	}

	/**
	 * Generates the arcs between the artificial 0-node and the rest of the nodes and computes the appropriate amount of flow in those arcs for the overall flow to be valid.
	 * Also compute the nodeprice-array.
	 */
	public void initFlowPrice() {
		int[] tempflow=demand.clone();
		for(int i=0; i<arcarr.length-nnodes+1;i++){
			tempflow[arcarr[i].startnode]-=arcarr[i].lowerb;
			tempflow[arcarr[i].endnode]+=arcarr[i].lowerb;
			
		}
		int m = 1+(this.nnodes)*(this.maxcost());
		for(int i=1; i<nnodes; i++){
			if(tempflow[i]<0){
				arcarr[arcarr.length-nnodes+i]=new Arc(0,i,0,Integer.MAX_VALUE,m,-tempflow[i], true);
				nodeprice[i]=m;
			}
			if(tempflow[i]>=0){
				arcarr[arcarr.length-nnodes+i]=new Arc(i,0,0,Integer.MAX_VALUE,m,tempflow[i], true);
				nodeprice[i]=-m;
			}
		}
		
	}
	public boolean isValidFlow() {
		int[] tempflow=demand.clone();
		for(int i=0;i<this.arcarr.length;i++){
			tempflow[arcarr[i].startnode]-=arcarr[i].flow;
			tempflow[arcarr[i].endnode]+=arcarr[i].flow;		
			}
		for(int i=0;i<this.nnodes;i++){
			if(tempflow[i]!=0)return false;
		}
		return true;
	}
	
	/**
	 * optimizes a mincost-flow network with a valid initialized flow and tree solution using the network simplex algorithm
	 * @return true if there exists a valid flow with minimal costs.
	 */
	public Boolean simplex() {
		
		// 2. Berechnung der Knotenpreise
		
		// 3. Optimalitaetstest
		
		Arc pivot=findPivot();
		if(null==pivot){
			for(int i=0;i<this.nnodes; ++i){
				if(arcarr[this.narcs-this.nnodes+i].isInTree)return false;
			}
			return true;
		}
				
		// 4. Pricing
		/*
		 *  (da isopt(a,L,U) von 3. return false) nehme dieses a als e
		 *  
		 */
		
		// 5. Augmentieren
		
		// 6. Update
		
		
		return null;
	}
	
	public Arc findPivot(){
		// muss hier equal hin? vielleicht ist es besser eine ausgelagerte Methode zu schreiben, die prueft ob a element eines Arc[] ist, anstatt dieser hier
		
		for(int i=0;i<this.arcarr.length;++i){
			Arc temp = arcarr[i];
			int redcost = temp.cost-nodeprice[temp.endnode]+nodeprice[temp.startnode];
			if(temp.flow==temp.upperb && redcost <= 0 && !temp.isInTree ){
				return temp;
			}
			if(temp.flow==temp.lowerb && redcost >= 0 && !temp.isInTree){
				return temp;
			}

		}
		return null;
	}
	
	/**
	 * prints the solution after the network-optimization
	 */
	public void printSol() {
		// TODO Auto-generated method stub
		
	}
	public void writeGraph() throws IOException{
		int begin = 0;
		int end = 0;
		int length = narcs-1;
		PrintWriter text = null;
		text = new PrintWriter(new BufferedWriter(new FileWriter("graph.gv")));
		text.println("digraph G {");
		for(int i = 0;i <= length;i++){
			begin = arcarr[i].startnode;
			end = arcarr[i].endnode;
			text.println(begin + " " + "->" + " " + end);
		}
		text.println("\n");
		text.println("}");
		text.close();
	}
}
