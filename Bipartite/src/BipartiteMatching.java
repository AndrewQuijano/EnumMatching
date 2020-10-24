import java.util.InputMismatchException;

/*************************************************************************
 *  Compilation:  javac BipartiteMatching.java
 *  Execution:    java BipartiteMatching N E
 *  Dependencies: FordFulkerson.java FlowNetwork.java FlowEdge.java 
 *
 *  Find a maximum matching in a bipartite graph. Solve by reducing
 *  to maximum flow.
 *
 *  The order of growth of the running time in the worst case is E V
 *  because each augmentation increases the cardinality of the matching
 *  by one.
 *
 *  The Hopcroft-Karp algorithm improves this to E V^1/2 by finding
 *  a maximal set of shortest augmenting paths in each phase.
 *
 *********************************************************************/

public class BipartiteMatching 
{
	public static int debug = 1;

	public static void main(String[] args) 
	{

		// read in bipartite network with 2N vertices and E edges
		// we assume the vertices on one side of the bipartition
		// are named 0 to N-1 and on the other side are N to 2N-1.

		// G for original graph
		// F for flow network based on which calculated M
		// M for matching which consists of edges in F of which weight>0
		// D for generated digraph
		In in;
		if(args.length == 0)
		{
			//in = new In(new Scanner(System.in));
			in = new In("./test.txt");
		}
		else
		{
			in = new In(args[0]);
		}
		
		//In in = new In(new Scanner(args[0]));
		Bag<MyDigraph> matches = new Bag<MyDigraph>();

		int N = 0;
		try
		{
			N = in.readInt();
		}
		catch(InputMismatchException e)
		{
			System.out.println("ARR");
			System.out.print(e.getMessage());
		}
		int s = 2 * N, t = 2 * N + 1;
		if (N < 0)
		{
			throw new IllegalArgumentException("Number of nodes must be nonnegative");
		}
		else
		{
			System.out.println("Nodes: " + N);
			System.out.println("Source: " + s);
			System.out.println("Target: " + t);
		}
		
		MyDigraph G = new MyDigraph(2*N);
		MyFlowNetwork F = new MyFlowNetwork(2*N + 2);
		int E = in.readInt();
		if (E < 0)
		{
			throw new IllegalArgumentException("Number of edges must be nonnegative");
		}
		else
		{
			System.out.println("Edges: " + E);
		}
		
		for (int i = 0; i < E; i++) 
		{
			int v = in.readInt();
			int w = in.readInt();
			/*
			if (v < 0 || v > N)
			{
				throw new IndexOutOfBoundsException("vertex " + v + " is not between 0 and " + (E - 1));
			}
			if (w < 0 || w > N)
			{
				throw new IndexOutOfBoundsException("vertex " + w + " is not between 0 and " + (E - 1));
			}
			*/
			G.addEdge(v, w);
			F.addEdge(new FlowEdge(v, w, Double.POSITIVE_INFINITY));
			if(debug==1)
			{
				StdOut.println(v + "-" + w);
			}
		}

		for (int i = 0; i < N; i++) 
		{
			F.addEdge(new FlowEdge(s, i, 1.0));
			F.addEdge(new FlowEdge(i + N, t, 1.0));
		}
		
		// compute maximum flow and minimum cut
		FordFulkerson maxflow = new FordFulkerson(F, s, t);
		
		if(debug==1)
		{
			StdOut.println();
			StdOut.println("Find Maximum matching: \nSize of maximum matching = " + (int) maxflow.value());
			StdOut.println();
		}

		// construct digraph
		MyDigraph M = new MyDigraph(2*N);
		matches.add(M);
		MyDigraph D = new MyDigraph(2*N);
		for (int v = 0; v < N; v++)
		{
			for (FlowEdge e : F.adj(v)) 
			{
				if (e.from() == v && e.flow() > 0)
				{
					if(debug==1)
					{
						StdOut.println("positive: " + e.from() + "-" + e.to());
					}
					M.addEdge(e.from(), e.to());
					D.addEdge(e.from(), e.to());
				}
				
				if (e.from() == v && e.flow() == 0)
				{	
					if(debug==1)
					{
						StdOut.println("negative: " + e.to() +"-" +e.from());
					}
					// FLIP ARROW (src, target) -> (target, src)
					D.addEdge(e.to(), e.from());
				}
			}
		}
		if(debug==1)
		{
			StdOut.println();
			StdOut.println("\nDirected Graph\n"+D.toString());
		}
		// KosarajuSharirSCC algorithm to check the strong connected component
		KosarajuSharirSCC scc = new KosarajuSharirSCC(D);

		// number of connected components
		int ccc = scc.count();
		StdOut.println("KosarajuSharirSCC algorithm find " + ccc + " components");

		// compute list of vertices in each strong component
		@SuppressWarnings("unchecked")
		Queue<Integer>[] components = new Queue[ccc];
		for (int i = 0; i < ccc; i++) 
		{
			components[i] = new Queue<Integer>();
		}
		for (int v = 0; v < D.V(); v++) 
		{
			components[scc.id(v)].enqueue(v);
		}

		if(debug==1)
		{
			// print results
			for (int i = 0; i < ccc; i++) 
			{
				for (int v : components[i]) 
				{
					StdOut.print(v + " ");
				}
				StdOut.println();
			}
		}

		// Trim unnecessary arcs from D(G,M) by a strongly connected component decomposition algorithm.
		/*
		for (int v = 0; v < D.V(); v++) 
		{
			for (int w : D.adj(v)) 
			{
				if(scc.id(v)!=scc.id(w))
				{
					D = MyDigraph.deleteEdge(D, v, w); 
					if(debug==1)
					{
						StdOut.println("Delete "+v+" "+w);
					}
				}
			}
		}
		*/
		
		StdOut.println("\nDirected Graph: D(G, M)\n"+D.toString());
		
		//dfs find a cycle?
		if(debug==1)
		{
			int start=0;
			MyDepthFirstDirectedPaths dfs = new MyDepthFirstDirectedPaths(D, start);
			for (int v = 0; v < D.V(); v++) 
			{
				if (dfs.hasPathTo(v)) 
				{
					StdOut.printf("%d to %d:  ", start, v);
					int flip = 0;
					for (int x : dfs.pathTo(v)) 
					{
						if (x == start && flip==0)
						{
							StdOut.print(x); 
							flip=1;
						}
						else
						{
							StdOut.print("-" + x);
						}
					}
					StdOut.println();
				}
				else 
				{
					StdOut.printf("%d to %d:  not connected\n", start, v);
				}
			}
		}

		Enum_maximum_matching_iter(matches, G, M, D);

		int count=1;
		System.out.println("\n...Print all matches...\n");
		for (MyDigraph i: matches)
		{
			StdOut.println(""+count+" matches\n"+i.toString());
			count++;
		}
	}

	public static void Enum_maximum_matching_iter(Bag<MyDigraph> matches, MyDigraph g,
			MyDigraph m, MyDigraph d)
	{
		if(g.E()==0)
		{
			StdOut.println("FINISHED!");
			return;
		}
		
		// Note: 
		// s = 2N 
		// t = 2N + 1
		// top nodes go from [0, d.V() - 1]
		// bottom nodes go from [d.V() to 2 * d.V() - 1]
		
		// check if there exists a cycle
		for (int s = 0; s < d.V(); s++)
		{
			// input digraph D(G, M) and source node s
			MyDepthFirstDirectedPaths dfs = new MyDepthFirstDirectedPaths(d, s);
			// is there a path from s to s? -> Cycle?
			if (dfs.hasPathTo(s))
			{
				MyDigraph m_prime = new MyDigraph(m);
				// Step 3: e_start e_end is the first edge on the cycle
				// you already have a cycle so just pick an e in the cycle!
				int e_start=s;
				int e_end=0;
				
				// to detect if we need to add or delete this edge
				int flip=0; 
				
				// to detect if it is the first time to visit the start
				int init=0;
				
				// define the precursor
				int temp = s;
				
				// Step 5: Exchange edges along the cycle and utput obtained maxmimum M'
				for (int x : dfs.pathTo(s)) 
				{
					if (x==s && init==0)
					{
						init = 1;
						continue;
					}
					if (flip==0)
					{
						//StdOut.println("Delete:\t"+temp+"\t"+x);
						if (init==1)
						{
							e_end=x; 
							init=2;
						}
						m_prime = MyDigraph.deleteEdge(m_prime, temp, x);
						flip = 1;
					}
					else
					{
						//StdOut.println("Add:\t"+x+"\t"+temp);
						m_prime.addEdge(x, temp);
						flip = 0;
					}
					temp = x;
				}
				matches.add(m_prime);
				
				//construct g_plus
				MyDigraph g_plus = constructG_plus(g, e_start, e_end);
				
				//construct g_minus
				MyDigraph g_minus = constructG_minus(g, e_start, e_end);
				
				MyDigraph d_plus = constructDfromGM2(g_plus, MyDigraph.deleteEdge(m, e_start, e_end));
				MyDigraph d_minus = constructDfromGM2(g_minus, m_prime);
				
				//step 6 and step 7
				
				if(debug==2)
				{
					StdOut.println("m"+m.toString());
					StdOut.println("edge="+" "+e_start+" "+e_end);
					StdOut.println("\ng_plus\n"+g_plus.toString());
					StdOut.println("\nm/e\n"+MyDigraph.deleteEdge(m, e_start, e_end));
					StdOut.println("\nd_plus\n"+d_plus.toString());
					//StdOut.println("\ng_minus\n"+g_minus.toString());
					//StdOut.println("\nd_minus\n"+d_minus.toString());
					//StdOut.println("\nM/e\n"+MyDigraph.deleteEdge(m, e_start, e_end).toString());
				}
				
				Enum_maximum_matching_iter(matches, g_plus, m, d_plus);
				Enum_maximum_matching_iter(matches, g_minus, m_prime, d_minus);
				return;
			}
		}
		
		// Step 8
		// Find a feasible path with length 2 and generate a new maximum matching M'
		int [] pair;
		pair = new int[m.V()];
		// NOTE: ITS ONLY ONE SET OF VERTICES
		for(int v = 0; v < m.V(); v++)
		{
			pair[v]=Integer.MAX_VALUE;
		}
		for(int v=0; v<m.V(); v++)
		{
			for(int w: m.adj(v))
			{
				pair[v]=w;
				pair[w]=v;
			}
		}
		
		// Iterate through all vertices in G
		for(int v=0; v<g.V(); v++)
		{
			if (pair[v]==Integer.MAX_VALUE)
			{
				//if v is in the left side
				for (int w: g.adj(v))
				{
					if(pair[w] != Integer.MAX_VALUE)
					{
						System.out.println("Option 1");
						MyDigraph m_prime = new MyDigraph(m);
						m_prime = MyDigraph.deleteEdge(m_prime, pair[w], w);
						m_prime.addEdge(v, w);
						matches.add(m_prime);
						
						MyDigraph g_plus = constructG_plus(g, v, w);
						MyDigraph g_minus = constructG_minus(g, v, w);
						
						Enum_maximum_matching_iter(matches, g_plus, m_prime, constructDfromGM2(g_plus,m_prime));
						Enum_maximum_matching_iter(matches, g_minus, m, constructDfromGM2(g_minus,m));
						return;
					}
				}
				//if v is in the right side
				for (int w: d.adj(v))
				{
					if(pair[w] != Integer.MAX_VALUE)
					{
						System.out.println("Option 2");
						MyDigraph m_prime = new MyDigraph(m);
						m_prime = MyDigraph.deleteEdge(m_prime, w, pair[w]);
						StdOut.println("delete: " + w + "-" + pair[w]);
						StdOut.println("add:    " + w + "-" + v);
						m_prime.addEdge(w, v);
						matches.add(m_prime);
						
						MyDigraph g_plus = constructG_plus(g, w, v);
						MyDigraph d_plus = constructDfromGM2(g_plus, m_prime);
						MyDigraph g_minus = constructG_minus(g, w, v);
						MyDigraph d_minus = constructDfromGM2(g_minus, m);
						
						Enum_maximum_matching_iter(matches, g_plus, m_prime, d_plus);
						Enum_maximum_matching_iter(matches, g_minus, m, d_minus);
						return;
					}
				}	
			}
		}
	}

	public static MyDigraph constructG_minus(MyDigraph g, int e_start, int e_end) 
	{
		MyDigraph g_minus = new MyDigraph(g);
		g_minus = MyDigraph.deleteEdge(g_minus, e_start, e_end);
		return g_minus;
	}

	public static MyDigraph constructG_plus(MyDigraph g, int e_start, int e_end) 
	{
		MyDigraph g_plus = new MyDigraph(g);
		for (int x : g.adj(e_start))
		{
			g_plus = MyDigraph.deleteEdge(g_plus, e_start , x);
		}
		MyDigraph gtemp = new MyDigraph(g.reverse());

		for (int x : gtemp.adj(e_end))
		{
			if(x!=e_start)
			{
				g_plus = MyDigraph.deleteEdge(g_plus, x , e_end);
			}
		}
		return g_plus;
	}

	//xcch
	private static MyDigraph constructDfromGM2(MyDigraph g_plus,
			MyDigraph m_prime) 
	{
		MyDigraph d= new MyDigraph(g_plus);
		// Iterate through all vertices
		for (int v = 0; v < g_plus.V(); v++) 
		{
			// for all neighbors in v
			for (int w : g_plus.adj(v)) 
			{
				// If the edge in matching, FLIP IT
				if (MyDigraph.hasEdge(m_prime, v, w) == false)
				{
					d.addEdge(w, v);
					d = MyDigraph.deleteEdge(d, v, w);
				}
			}
		}
		return d;
	}
}
