package net_simplex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class Launch {

	public static void main(String[] args) throws IOException {

		if(args.length<1){
			System.out.println("Missing source name");
			System.exit(1);
		}
		//TODO time measuring
		Network mincost = readData(args[0]);
		assert(mincost.isInitialized());
		
		Boolean optimizable = mincost.simplex();
		if(!optimizable){
			System.out.println("There is no optimal extremum for the given mincost-flow instance.");
		}else{
			mincost.printSol();
		}
		
	}
	/**
	 * reads in the data and generates a network, initialized with a valid flow.
	 * @param filename the name of the data-file
	 * @return the new network
	 * @throws IOException 
	 */
public static Network readData(String filename) throws IOException{
		
		Network mincost=null;
		BufferedReader input= new BufferedReader(new FileReader(filename));
		//TODO error handling
		int nodecount=1;
		int arccount=0;
		int nnodes=0;
		int narcs=0;
		int line=1;
		while (input.ready()) {
			String buf = input.readLine();
			char descr = buf.charAt(0);
			String[] split = buf.split("\\s+");
			
			switch(descr){
			case 'c': 
				line++;
				break;
			case 'p': 
				if(split.length!=4 || split[1].compareTo("min")!=0){
					System.out.println("Error with input at line" + String.valueOf(line) + " .Check the input format. Specified line is:");
					input.close();
					System.exit(-1);
				}
				nnodes=Integer.parseInt(split[2]);
				narcs=Integer.parseInt(split[3]);
				mincost=new Network(nnodes+1,narcs+nnodes);
				line++;
				break;
			case 'n':
				if(null==mincost||nodecount!=Integer.parseInt(split[1]) || split.length!=3 || nodecount>nnodes){
					System.out.println("Error with input at line " + String.valueOf(line) + " .Check the input format. Specified line is:");
					System.out.println(buf);
					input.close();
					System.exit(-1);
				}
				mincost.demand[nodecount]=Integer.parseInt(split[2]);
				nodecount++;
				line++;
				break;
			case 'a':
				if(null==mincost || split.length!=6 || arccount >=narcs){
					System.out.println("Error with input at line " + String.valueOf(line) + " .Check the input format. Specified line is:");
					System.out.println(buf);
					input.close();
					System.exit(-1);
				}
				int startn=Integer.parseInt(split[1]);
				int endn=Integer.parseInt(split[2]);
				if(startn >nnodes || 0>startn || endn>nnodes || 0>endn){
					System.out.println("Error with input at line " + String.valueOf(line) + ". Start - or endnode of the arc are not in the graph. Specified line is:");
					System.out.println(buf);
					input.close();
					System.exit(-1);
				}
				int lowerb=Integer.parseInt(split[3]);
				int upperb=Integer.parseInt(split[4]);
				int cost=Integer.parseInt(split[5]);
				if(lowerb>upperb || lowerb <0 ){
					System.out.println("Error with input at line " + String.valueOf(line) + ". Flowbounds are incorrect. Specified line is:");
					System.out.println(buf);
					input.close();
					System.exit(-1);
				}
				mincost.arcarr[arccount]=new Arc(startn,endn,lowerb,upperb,cost,lowerb);
				arccount++;
				line++;
				break;
			}
			
		}
		if(nnodes!=nodecount-1 ||narcs!=arccount){
			System.out.println("Error with input. Less nodes or arcs than specified in the problem line.");
			input.close();
			System.exit(-1);
		}
	
		input.close();
		mincost.initFlowPrice();
		mincost.initTree();
		assert(mincost.isInitialized());
		assert(mincost.isValidFlow());
		return mincost;
	}
}
