package net_simplex;

import java.util.ArrayList; 

public class TempNetwork {
	int nnodes, narcs;
	ArrayList<Integer> demand;
	ArrayList<Arc> arcarr; 
	
	
	public TempNetwork(int nnodes, int narcs) {
		this.nnodes=nnodes;
		this.narcs=narcs;
		this.demand = new ArrayList<Integer>();
		for(int i=0;i<nnodes;++i){
			demand.add(0);
		}
		this.arcarr = new ArrayList<Arc>();
	}
	
	public void handleParallels() {
		ArrayList< ArrayList<Integer>> nodeLists = new ArrayList< ArrayList< Integer >>();
		for(int i=0;i<this.nnodes;++i){
			nodeLists.add(new ArrayList<Integer>());
		}
		ArrayList<Integer> problemArcs=new ArrayList<Integer>();
		for(int i=0;i<this.narcs;++i){
			Arc temp=this.arcarr.get(i);
			if(nodeLists.get(temp.startnode).contains(temp.endnode) || nodeLists.get(temp.endnode).contains(temp.startnode)){
				problemArcs.add(i);
			}
			nodeLists.get(temp.startnode).add(temp.endnode);
		}
		int lengthen = problemArcs.size();
		if(lengthen!=0){
			System.out.println("This problem has parallel or antiparallel arcs");
		}
		this.nnodes+=lengthen;
		this.narcs+=lengthen;
		for(int i=0;i<lengthen;++i){
			this.demand.add(0);
			Arc temp = this.arcarr.get(problemArcs.get(i));
			this.arcarr.add(new Arc(demand.size()-1,temp.endnode,temp.lowerb,temp.upperb,0,temp.flow,false));
			temp.endnode=demand.size()-1;
		}
	
	}

	public Network createNetwork() {
		
		Network mincost = new Network(this.nnodes,this.narcs+this.nnodes-1);
		for(int i=0;i<this.nnodes;++i){
			if(this.demand.get(i)!=null){
				mincost.demand[i]=this.demand.get(i);
			}
		}
		for(int i=0;i<this.narcs;++i){
			mincost.arcarr[i]=this.arcarr.get(i);
		}
		return mincost;
	}
}

