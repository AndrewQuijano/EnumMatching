
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import ilog.concert.IloException;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.concert.IloObjective;
import ilog.cplex.IloCplex;
public class testBipartiteMatching {

	/**
	 * @param args
	 */
 //int A[n][m],int C[l][m],
	public double lp(){
		// n: the number of node
		// T: the number of tuple 
		
		IloCplex cplex;
		try {
			cplex = new IloCplex();
			cplex.setOut(null);
			IloNumVar lamda=cplex.numVar(0.0, 2000.0);
			
			IloNumVar[] ye= new IloNumVar[m];
			
			for(int i=0;i<m;i++){
				ye[i]= cplex.numVar(0, 8);
			}
			
			
			//constraints
			//obj constraints
						
				IloLinearNumExpr c2=cplex.linearNumExpr();
				for(int j=0;j<m;j++){
					c2.addTerm(ye[j],C[j]);

				}

				c2.addTerm(lamda, -1);
				cplex.addLe(c2, 0);
			
			cplex.addMinimize(lamda);
			
			
			//capacity 
			for(int i=0;i<n1;i++){
				
				IloLinearNumExpr c1=cplex.linearNumExpr();
				for(int j=0; j<m; j++){
					c1.addTerm(ye[j], A[i][j]);					
				}	
				
				cplex.addEq(c1, 1);
			}
	
			for(int i=n1;i<(n2+n1);i++){
				
				IloLinearNumExpr c1=cplex.linearNumExpr();
				for(int j=0; j<m; j++){
					c1.addTerm(ye[j], A[i][j]);					
				}	
				
				cplex.addLe(c1, 1);
			}


			
			if(cplex.solve()){
				System.out.println("yes");
				System.out.println("Lamda="+cplex.getValue(lamda));
//				System.out.println(0+"\t"+0+"\t"+1+"\t"+cplex.getValue(f_iuv[0][0][1]));
				for (int i=0;i<m;i++){
					System.out.println(cplex.getValue(ye[i]));
				}
				double sum = 0;
				for(int j=0; j<m; j++){
					System.out.println(cplex.getValue(ye[j])+"\t"+C[j]);
					sum += cplex.getValue(ye[j])*A[2][j];					
				}	
				System.out.println(sum);
				
//				for(int i=0; i<T.size(); i++){
//					Tuple t =T.get(i);
//					double sum1 = 0;
//					double sum2 = 0;
//					for(Tuple edge: G.edges){
//						int u= edge.s;
//						int v = edge.d;
//						if(edge.s == t.s){
//							sum1 += cplex.getValue(f_iuv[i][edge.s][edge.d]);
//							if(t.s==2 && t.d == 6){
//								System.out.println("***************"+edge.s+"\t"+edge.d+"\t"+cplex.getValue(f_iuv[i][edge.s][edge.d]));
//							}
//						}
//						
//						if(edge.d == t.d){
//							
//							sum2 += cplex.getValue(f_iuv[i][edge.s][edge.d]);
//							if(t.s==2 && t.d == 6){
//								System.out.println("***************"+edge.s+"\t"+edge.d+"\t"+cplex.getValue(f_iuv[i][edge.s][edge.d]));
//							}
//						}
//					}
//					
////					System.out.println(t.s+"\t"+t.d+"\t"+t.b+"\t"+sum1+"\t"+sum2);
////					System.out.println("*****************88");
//					}
//				
//				for(int i=0; i<T.size(); i++){				
//					Tuple t =T.get(i);
//					int s = t.s;
////
//					double sum=0;
//					for(int j=0;j<G.edges.size();j++){
//						Tuple edge = G.edges.get(j);
//						int u= edge.s;
//						int v = edge.d;
//						if(edge.s ==s )
//							sum +=cplex.getValue(f_iuv[i][edge.s][edge.d]);
//							if(t.s == 2 && t.d ==6)
//								System.out.println(u+"\t"+v+"\t"+cplex.getValue(f_iuv[i][edge.s][edge.d]));
//						
//					}
////					
//					System.out.println(t.s+"\t"+t.d+"\t"+t.b+"\t"+sum+"\t"+sum*1.0/t.b);
////					
//				}
//				System.out.println(cplex.getObjValue());
				
				double v = cplex.getObjValue();
				return v;
			}else{
				System.out.println("no");
			}
			
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}


	public int n1 = 3;
	public int n2 = 4;
	public int m = 7;
	public int l = 3;
	public  int[][] A = new int[n1+n2][m];
	public double[] C = new double[m];
	
	public void BipartiteGraph(){
		ArrayList<ManKey> edges = new ArrayList<ManKey>();
		for (int j =0;j<m;j++){
			int start = (int) (Math.random()*10000)%n1;
			int end = (int) (Math.random()*10000)%n2+n1;
			boolean flag = false;
			for(int k=0;k<edges.size();k++){
				int s= edges.get(k).source;
				int e = edges.get(k).dst;
				if(s==start&& e == end){
					flag= true;
					break;					
				}	
			}
			
			
			if(flag){
				j--;
			}else{
				edges.add(new ManKey(start,end));
				for(int i=0;i<(n1+n2);i++){
					if(i==start){
						A[i][j] = 1;
					}else if(i==end){
						A[i][j] = 1;
					}else{
						A[i][j] =0;
					}
				}
				
			}
		}

		for(int k=0;k<edges.size();k++){
			int s= edges.get(k).source;
			int e = edges.get(k).dst;
			System.out.println(s+"\t"+e);
		}
		for(int i=0;i<(n1+n2);i++){
			for(int j=0;j<m;j++){
				System.out.print(A[i][j]+"\t");
			}
			System.out.println();
		}
		

			for(int j=0;j<m;j++){
				C[j] = (int) (Math.random()*10000)%5;
			}
		
		
		
	}
	    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		testBipartiteMatching is = new testBipartiteMatching();
//		is.reading("data2/testFile.txt");

		is.BipartiteGraph();
		//lp(int[][] A, double[][] C, int n1, int n2, int m , int l )
//		is.lp(is.A, is.C, 3, 4, 7 ,2 );
		is.lp();
		
		HashMap<Integer, Integer> edges = new HashMap<Integer, Integer>();
		edges.put(3, 4);
		if(edges.get(3)==4){
			System.out.println("ye");
		}
	}

}
