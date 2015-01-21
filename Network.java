package net_simplex;

import java.io.*;
import java.util.ArrayList;


public class Network {
	int nnodes, narcs, fakearcind;
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
		this.fakearcind=this.narcs-this.nnodes+1;
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
		for(int i=0; i<this.fakearcind; i++){
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
			this.tree.arcind[i-1]=this.fakearcind+i-1;
		}
		this.tree.succ[this.nnodes-1]=0;
	}

	/**
	 * Generates the arcs between the artificial 0-node and the rest of the nodes and computes the appropriate amount of flow in those arcs for the overall flow to be valid.
	 * Also compute the nodeprice-array.
	 */
	public void initFlowPrice() {
		int[] tempflow=demand.clone();
		for(int i=0; i<this.fakearcind;i++){
			tempflow[arcarr[i].startnode]-=arcarr[i].lowerb;
			tempflow[arcarr[i].endnode]+=arcarr[i].lowerb;
			
		}
		int m = 1+(this.nnodes)*(this.maxcost());
		for(int i=1; i<nnodes; i++){
			if(tempflow[i]<0){
				arcarr[this.fakearcind+i-1]=new Arc(0,i,0,Integer.MAX_VALUE,m,-tempflow[i], true);
				nodeprice[i]=m;
			}
			if(tempflow[i]>=0){
				arcarr[this.fakearcind+i-1]=new Arc(i,0,0,Integer.MAX_VALUE,m,tempflow[i], true);
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
		
		
		
		// 3. Optimalitaetstest + pivot-Element finden. 
		
		int pivot=findPivot();
		
		
		while(-1!=pivot){
			//find u,v from the pivot
			Arc pivotarc = arcarr[pivot];
			
			int u,v=0;
			//udirection tells us if we go along the upath from the joint 
			boolean udirection = true;
			int strt= pivotarc.startnode;
			int end=pivotarc.endnode;
			
			if(tree.depth[strt]>tree.depth[end]){
				u=strt;
				v=end;
				if(pivotarc.flow==pivotarc.upperb)udirection=false;
			}else{
				v=strt;
				u=end;
				if(pivotarc.flow==pivotarc.lowerb)udirection=false;
			}
			
			ArrayList<Integer> upath = new ArrayList<Integer>();
			ArrayList<Integer> vpath = new ArrayList<Integer>();
			upath.add(u);
			vpath.add(v);
			
			
			while(tree.depth[upath.get(upath.size()-1)]!=tree.depth[v]){
				upath.add(tree.pred[upath.get(upath.size()-1)]);
			}
			while(upath.get(upath.size()-1).intValue()!=vpath.get(0).intValue()){
				upath.add(tree.pred[upath.get(upath.size()-1)]);
				vpath.add(0, tree.pred[vpath.get(0)]);
			}
			//TODO check changed: corrected order of varcs and simplified for-loop
			ArrayList<Arc> uarcs = new ArrayList<Arc>();
			for(int i=0;i<upath.size()-1;++i){
				uarcs.add(findTreeArc(upath.get(i),upath.get(i+1)));
				assert(null!=uarcs.get(uarcs.size()-1));
			}
			ArrayList<Arc> varcs = new ArrayList<Arc>();
			for(int i=0;i<vpath.size()-1;i++){
				varcs.add(findTreeArc(vpath.get(i),vpath.get(i+1)));
				assert(null!=varcs.get(varcs.size()-1));
			}
			
			//TODO check changed: fixed the way f is computed
			varcs.add(pivotarc);
			int pivotindex = varcs.size()-1;
			varcs.addAll(uarcs);
			vpath.addAll(upath);
			
			int eps = Integer.MAX_VALUE;
			Arc f = null;
			int e1=-1,e2=-1,f1=-1,f2=-1;
			//determine all forward/backward-arcs and calculate the amount eps which the flow is shifted
			boolean[] isforwardarc = new boolean[varcs.size()];
			if(udirection){
				for(int i=0;i<varcs.size();i++){
					//determine whether it is a forward-arc
					Arc temp = varcs.get(i);
					if(temp.endnode==vpath.get(i).intValue()){
						isforwardarc[i]=true;
						eps=Math.min(eps, temp.upperb-temp.flow);
					}
					else{
						isforwardarc[i]=false;
						eps=Math.min(eps, temp.flow-temp.lowerb);
					}
					
					
				}
				
			}else{
				for(int i=0;i<varcs.size();i++){
					//determine whether it is a forward-arc
					Arc temp = varcs.get(i);
					if(temp.endnode==vpath.get(i+1).intValue()){
						isforwardarc[i]=true;
						eps=Math.min(eps, temp.upperb-temp.flow);
					}
					else{
						isforwardarc[i]=false;
						eps=Math.min(eps, temp.flow-temp.lowerb);
					}
				}
			}
			//now go through the arc-array again and determine the leaving arc f and f1,f2,e1,e2
			if(udirection){
				for(int i=0;i<varcs.size();i++){
					Arc temp = varcs.get(i);
					if((isforwardarc[i] && temp.flow+eps==temp.upperb)||(!isforwardarc[i]&&temp.flow-eps==temp.lowerb)){
						f=temp;
						if(i>pivotindex){
							e2=u;
							e1=v;
							f2=vpath.get(i);
							f1=vpath.get(i+1);
						}else{
							e2=v;
							e1=u;
							f2=vpath.get(i+1);
							f1=vpath.get(i);
						}
						break;
					}
				}
			}else{
				for(int i=varcs.size()-1;i>=0;i--){
					Arc temp = varcs.get(i);
					if((isforwardarc[i] && temp.flow+eps==temp.upperb)||(!isforwardarc[i]&&temp.flow-eps==temp.lowerb)){
						f=temp;
						if(i<pivotindex){
							e2=v;
							e1=u;
							f2=vpath.get(i+1);
							f1=vpath.get(i);
						}else{
							e2=u;
							e1=v;
							f2=vpath.get(i);
							f1=vpath.get(i+1);
						}
						break;
					}
				}
			}
		
			assert(e1!=-1 && e2!=-1 && f1!=-1 && f2!=-1);
			
			/**
			ArrayList<Arc> forwardarcs = new ArrayList<Arc>();
			ArrayList<Arc> backwardarcs = new ArrayList<Arc>();
			boolean[] isforwardarcu = new boolean[uarcs.size()];
			boolean[] isforwardarcv = new boolean[varcs.size()];


			for(int i=0; i<uarcs.size();++i){
				Arc temp = uarcs.get(i);
				if(udirection){
					if(temp.endnode == upath.get(i)){
						forwardarcs.add(temp);
						isforwardarcu[i]=true;
					}
					else{
						backwardarcs.add(temp);
						isforwardarcu[i]=false;

					}
				}else{
					if(temp.endnode == upath.get(i+1)){
						forwardarcs.add(temp);
						isforwardarcu[i]=true;

					}
					else{
						backwardarcs.add(temp);
						isforwardarcu[i]=false;

					}
				}
			}
			for(int i=0; i<varcs.size();++i){
				Arc temp = varcs.get(i);
				if(udirection){
					if(temp.endnode == vpath.get(i+1)){
						forwardarcs.add(temp);
						isforwardarcv[i]=true;
					}
					else{
						backwardarcs.add(temp);
						isforwardarcv[i]=false;
					}
				}else{
					if(temp.endnode == vpath.get(i)){
						forwardarcs.add(temp);
						isforwardarcv[i]=true;
					}
					else{
						backwardarcs.add(temp);
						isforwardarcv[i]=false;
					}
				}
			}
			
			//add pivotarc as a forward or backward arc
			
			if(!udirection){
				if(pivotarc.endnode==u)forwardarcs.add(pivotarc);
				else backwardarcs.add(pivotarc);
			}else{
				if(pivotarc.endnode==v)forwardarcs.add(pivotarc);
				else backwardarcs.add(pivotarc);
			}
			
			//compute amount of augmentation
			for(int i=0;i <forwardarcs.size();++i){
				Arc temp = forwardarcs.get(i);
				eps=Math.min(eps, temp.upperb-temp.flow);
			}
			for(int i=0;i <backwardarcs.size();++i){
				Arc temp = backwardarcs.get(i);
				eps=Math.min(eps, temp.flow-temp.lowerb);
			}
			
			//determine the tree-leaving arc f
			//TODO this is wrong. check it. f does not have to be in uarcs if udirection is true.
			//TODO include the setting of e1,e2,f1,f2 in this step. 
			if(udirection){
				for(int i=0; i<uarcs.size();++i){
					if(isforwardarcu[i] && uarcs.get(i).flow+eps==uarcs.get(i).upperb){
						f=uarcs.get(i);
						break;
					}
					if(!isforwardarcu[i] && uarcs.get(i).flow-eps==uarcs.get(i).lowerb){
						f=uarcs.get(i);
						break;
					}
				}
			}else{
				for(int i=varcs.size()-1; i>=0;--i){
					if(isforwardarcv[i] && varcs.get(i).flow+eps==varcs.get(i).upperb){
						f=varcs.get(i);
						break;
					}
					if(!isforwardarcv[i] && varcs.get(i).flow-eps==varcs.get(i).lowerb){
						f=varcs.get(i);
						break;
					}
				}
			}
			if(f==null)f=pivotarc;
		
			
			//update nodeprice
			int f1,f2;
			
			//TODO f2 und e2 sind so zu w�hlen, dass beide in S sind
			
			if(tree.depth[f.endnode]>tree.depth[f.startnode]){
				f1=f.startnode;
				f2=f.endnode;
			}
			else{
				f1=f.endnode;
				f2=f.startnode;
			}
			int e1,e2;
			int pivotdir=1;
			if(udirection){
				e2=u;
				e1=v;
				if(pivotarc.endnode==u)pivotdir=-1;
			}else{
				e2=v;
				e1=u;
				if(pivotarc.endnode==v)pivotdir=-1;
			}
			*/
			//z=f1 versuch um gleiche ebene zu managen
			/**int z=f1;
			int pivotdir=1;
			
			while(tree.depth[tree.succ[z]]>tree.depth[z]){
				if(pivotarc.endnode==tree.succ[z]){
					e1=pivotarc.startnode;
					e2=pivotarc.endnode;
					break;
				}
				if(pivotarc.startnode==tree.succ[z]){
					pivotdir=-1;
					e2=pivotarc.startnode;
					e1=pivotarc.endnode;
					break;
				}
				z++;
			}
			assert(e1!=-1 && e2!=-1);
			**/
			int pivotdir; 
			if(pivotarc.endnode==e2)pivotdir=1;
			else pivotdir=-1;
			
			int redcost= pivotarc.cost+this.nodeprice[pivotarc.startnode]-this.nodeprice[pivotarc.endnode];
			int y=f2;
			nodeprice[y]+=pivotdir*redcost;
			while(tree.depth[tree.succ[y]]>tree.depth[f2]){
				y=tree.succ[y];
				nodeprice[y]+=pivotdir*redcost;
			}
			//augmentiere den fluss
			/*
			for(int i=0;i<forwardarcs.size();++i){
				forwardarcs.get(i).flow+=eps;
			}
			for(int i=0;i<backwardarcs.size();++i){
				backwardarcs.get(i).flow-=eps;
			}*/
			for(int i=0;i<varcs.size();++i){
				Arc temp=varcs.get(i);
				if(isforwardarc[i])temp.flow+=eps;
				else temp.flow-=eps;
			}
			//TODO this is probably garbage
			if((e1==f1 && e2==f2)||(e1==f2 && e2==f1)){
				pivot=findPivot();
				continue;
			}
			
			//update succ[]depth[] and pred[]
			
			int[] tempdepth = tree.depth.clone();
			int[] temppred = tree.pred.clone();
			tempdepth[e2]=tempdepth[e1]+1;
			temppred[e2]=e1;
			int a=f1;
			int b=tree.succ[e1];
			int i=e2;
			while(tree.succ[a]!=f2){
				a=tree.succ[a];
			}
			int k=i;
			while(tree.depth[tree.succ[k]]>tree.depth[i]){
				k=tree.succ[k];
				tempdepth[k]=tempdepth[tree.pred[k]]+1;
			}
			int r = tree.succ[k];
			while(i!=f2){
				int j=i;
				i=tree.pred[i];
				temppred[i]=j;
				tempdepth[i]=tempdepth[j]+1;
				
				tree.succ[k]=i;
				k=i;
				while(tree.succ[k]!=j){
					k=tree.succ[k];
					tempdepth[k]=tempdepth[tree.pred[k]]+1;
				}
				if(tree.depth[r]>tree.depth[i]){
					tree.succ[k]=r;
					while(tree.depth[tree.succ[k]]>tree.depth[i]){
						k=tree.succ[k];
						tempdepth[k]=tempdepth[tree.pred[k]]+1;
					}
					r=tree.succ[k];
				}
				
			}
			
			
			
			if(e1!=a){
				tree.succ[a]=r;
				tree.succ[e1]=e2;
				tree.succ[k]=b;
			}else{
				tree.succ[e1]=e2;
				tree.succ[k]=r;
			}
			assert(this.tree.isValidTree());
			tree.depth=tempdepth;
			tree.pred=temppred;
			
			
			
			pivotarc.isInTree=true;
			f.isInTree=false;
			for(int j=0;j<tree.arcind.length;++j){
				Arc temp=arcarr[tree.arcind[j]];
				if(f.startnode==temp.startnode && f.endnode==temp.endnode){
					tree.arcind[j]=pivot;
					break;
				}
			}
			
			
			
			//update the pivot element
			
			pivot=this.findPivot();
		
		}
		
		for(int i=0;i<this.nnodes-1; ++i){
			if(arcarr[this.fakearcind+i].isInTree && arcarr[this.fakearcind+i].flow!=0)return false;
		}
		return true;
		
	}
	private Arc findTreeArc(int i, int j) {
		for(int c=0;c<tree.arcind.length;++c){
			Arc temparc = arcarr[tree.arcind[c]];
			if((temparc.startnode==i && temparc.endnode==j) || (temparc.endnode==i && temparc.startnode==j)){
				return temparc;
			}
		}
		return null;
		
	}
	/**
	private void computeNodeprices() {
		nodeprice[0]=0;
		int temp =0;
		while(tree.succ[temp]<nnodes){
			nodeprice[tree.succ[temp]]=nodeprice[tree.pred[temp]];
			temp=tree.succ[temp];
		}
		
		
		
		
		int temp = this.nnodes-1;
		while(temp >0){
			int i=0;
			while(arcarr[tree.arcind[i]].startnode!=temp && arcarr[tree.arcind[i]].endnode!=temp){
				i++;
			}
			Arc temparc = arcarr[tree.arcind[i]];
			if(temparc.startnode==temp){
				nodeprice[temparc.endnode]=nodeprice[temp]+temparc.cost;
			}else{
				nodeprice[temparc.startnode]=nodeprice[temp]-temparc.cost;
			}
		}
	}*/
	public int findPivot(){
		// muss hier equal hin? vielleicht ist es besser eine ausgelagerte Methode zu schreiben, die prueft ob a element eines Arc[] ist, anstatt dieser hier
		
		for(int i=0;i<this.arcarr.length;++i){
			Arc temp = arcarr[i];
			if(temp.isInTree)continue;
			int redcost = temp.cost-nodeprice[temp.endnode]+nodeprice[temp.startnode];
			if(temp.flow==temp.upperb && redcost > 0 ){
				return i;
			}
			if(temp.flow==temp.lowerb && redcost < 0 ){
				return i;
			}

		}
		return -1;
	}
	
	public void printCosts(){
		int costs=0;
		for (Arc temp: arcarr){
			costs+=(temp.cost*temp.flow);
		}
		System.out.println("The total costs of the problem are: " + costs);
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
	public void writeTree() throws IOException{
		int begin = 0;
		int end = 0;
		
		PrintWriter text = null;
		text = new PrintWriter(new BufferedWriter(new FileWriter("graph.gv")));
		text.println("digraph G {");
		for(int i = 0;i < this.tree.arcind.length;i++){
			begin = arcarr[tree.arcind[i]].startnode;
			end = arcarr[tree.arcind[i]].endnode;
			text.println(begin + " " + "->" + " " + end);
		}
		text.println("\n");
		text.println("}");
		text.close();
	}
}
