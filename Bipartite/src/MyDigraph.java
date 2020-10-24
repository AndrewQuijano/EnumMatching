public class MyDigraph extends Digraph 
{
	public MyDigraph(Digraph G) 
	{
		super(G);
		// TODO Auto-generated constructor stub
	}

	public MyDigraph(In in) 
	{
		super(in);
		// TODO Auto-generated constructor stub
	}

	public MyDigraph(int v) 
	{
		super(v);
		// TODO Auto-generated constructor stub
	}

	/**
	 * delete one edge from the original graph
	 * @param v  start point
	 * @param w  end point
	 */
	public static MyDigraph deleteEdge(MyDigraph input, int s, int t)
	{
		MyDigraph R = new MyDigraph(input.V());
		//check if successful
		int succ = 0;
		for (int v = 0; v < input.V(); v++) 
		{
			for (int w : input.adj(v)) 
			{
				if(v==s && w==t)
				{
					succ=1;
				}
				else
				{
					R.addEdge(v, w);
				}
			}
		}
		if(succ==1)
		{
			//StdOut.println("Deletion successful!");
			return R;
		}
		else
		{
			StdOut.println("Deletion " + s + " " + t + "failed!");
			return R;
		}
	}

	public static boolean hasEdge(MyDigraph m_prime, int v, int w)
	{
		// TODO Auto-generated method stub
		for (int x:m_prime.adj(v))
		{
			if (x==w)
			{
				return true;
			}
		}
		return false;
	}
	
	// Write to same format as matching in network x?
	/*
	public String toString()
	{
		String x = "";
		return x;
	}
	*/
}
