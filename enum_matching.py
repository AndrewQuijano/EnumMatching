from networkx.algorithms.cycles import find_cycle
from networkx.algorithms.matching import is_perfect_matching
from networkx.exception import NetworkXNoCycle
from networkx import Graph, DiGraph
from more_itertools import peekable
from networkx import get_node_attributes
from misc import maximum_matching_all


def enum_perfect_matching(g):
    match = maximum_matching_all(g)
    matches = [match]
    # m, d = build_d(g, match)
    if is_perfect_matching(g, match):
        enum_perfect_matching_iter(matches, g, match)
    else:
        print("No perfect matching found!")


def enum_perfect_matching_iter(matches, g, m):
    # Step 1
    if not peekable(g.edges()):
        return

    # Step 2 Find a cycle in G
    _, d = build_d(g, m)
    cycle = find_cycle_in_dgm(d)
    if cycle:
        # Step 3 - Choose edge e from the cycle obtained
        # Step 4 - Find a cycle containing e via DFS
        # It is already done as we picked e from the cycle.

        # Step 5 - Exchange edges to generate new M'
        m_prime = m.copy()
        e_start = cycle[0]
        s = cycle[0]
        e_end = 0

        # to detect if we need to add or delete this edge
        flip = 0
        # to detect if it is the first time to visit the start
        init = 0
        # define the precursor
        temp = s

        # Step 5: Exchange edges along the cycle and output
        # obtained maximum M'
        for x in cycle:
            if x == s and init == 0:
                init = 1
                continue

            if flip == 0:
                if init == 1:
                    e_end = x
                    init = 2
                m_prime.remove_edge(temp, x)
                flip = 1
            else:
                m_prime.add_edge(x, temp)
                flip = 0
            temp = x

        # Pre-requisite for Step 6 and 7
        g_plus = construct_g_plus(g, e_start, e_end)
        g_minus = construct_g_minus(g, e_start, e_end)

        # Step 6 and 7
        enum_perfect_matching_iter(matches, g_plus, m)
        enum_perfect_matching_iter(matches, g_minus, m_prime)
    else:
        return


def enum_maximum_matching(g):
    match = maximum_matching_all(g)
    m, d = build_d(g, match)
    matches = [m]
    if g.is_directed():
        enum_maximum_matching_iter(matches, g, m, d)
    else:
        enum_maximum_matching_iter(matches, build_g(g), m, d)
    # Convert di-graphs to maximum matchings
    final_matches = []
    for match in matches:
        ma = maximum_matching_all(match)
        final_matches.append(ma)
    return final_matches


def enum_maximum_matching_iter(matches, g, m, d):
    # If there are no edges in G
    if not peekable(g.edges()) or not peekable(d.edges()):
        print("D(G, M) or G has no edges!")
        return
    else:
        # Step 2 Find a cycle in D(G, M)
        cycle = find_cycle_in_dgm(d)

        if cycle:
            # Step 3 - Choose edge e from the cycle obtained
            # Step 4 - Find a cycle containing e via DFS
            # It is already done as we picked e from the cycle.

            # Step 5 - Exchange edges to generate new M'
            m_prime = m.copy()
            e_start = cycle[0]
            s = cycle[0]
            e_end = 0

            # to detect if we need to add or delete this edge
            flip = 0
            # to detect if it is the first time to visit the start
            init = 0
            # define the precursor
            temp = s

            # Step 5: Exchange edges along the cycle and output
            # obtained maximum M'
            for x in cycle:
                if x == s and init == 0:
                    init = 1
                    continue

                if flip == 0:
                    if init == 1:
                        e_end = x
                        init = 2
                    m_prime.remove_edge(temp, x)
                    flip = 1
                else:
                    m_prime.add_edge(x, temp)
                    flip = 0
                temp = x

            # Pre-requisite for Step 6 and 7
            g_plus = construct_g_plus(g, e_start, e_end)
            g_minus = construct_g_minus(g, e_start, e_end)

            m.remove_edge(e_start, e_end)
            d_plus = construct_d_from_gm2(g_plus, m)
            m.add_edge(e_start, e_end)
            d_minus = construct_d_from_gm2(g_minus, m_prime)

            # Step 6 and 7
            enum_maximum_matching_iter(matches, g_plus, m, d_plus)
            enum_maximum_matching_iter(matches, g_minus, m_prime, d_minus)
        else:
            # Step 8
            nodes = list(g.nodes())
            pair = [float("inf")] * (max(nodes) + 1)
            for v in nodes:
                for w in m.successors(v):
                    pair[v] = w
                    pair[w] = v

            for v in nodes:
                if pair[v] == float("inf"):
                    # if v is in the left side
                    for w in g.successors(v):
                        if pair[w] != float("inf"):
                            m_prime = m.copy()
                            m_prime.add_edge(v, w)
                            m_prime.remove_edge(pair[w], w)
                            matches.append(m_prime)

                            g_plus = construct_g_plus(g, v, w)
                            g_minus = construct_g_minus(g, v, w)
                            d_plus = construct_d_from_gm2(g_plus, m_prime)
                            d_minus = construct_d_from_gm2(g_minus, m)

                            enum_maximum_matching_iter(matches, g_plus, m_prime, d_plus)
                            enum_maximum_matching_iter(matches, g_minus, m, d_minus)
                            return
                    # if v is in the right side
                    for w in d.successors(v):
                        if pair[w] != float("inf"):
                            m_prime = m.copy()
                            m_prime.add_edge(w, v)
                            m_prime.remove_edge(w, pair[w])
                            matches.append(m_prime)

                            g_plus = construct_g_plus(g, w, v)
                            d_plus = construct_d_from_gm2(g_plus, m_prime)

                            g_minus = construct_g_minus(g, w, v)
                            d_minus = construct_d_from_gm2(g_minus, m)

                            enum_maximum_matching_iter(matches, g_plus, m_prime, d_plus)
                            enum_maximum_matching_iter(matches, g_minus, m, d_minus)
                            return


# -----------------------------Helper functions--------------------------
# input: undirected bipartite graph
# output: directed bipartite graph with only arrows 0 to 1
def build_g(graph):
    g = DiGraph()
    for n, d in graph.nodes(data=True):
        if d['biparite'] == 0:
            g.add_node(n, biparite=0)
        else:
            g.add_node(n, biparite=1)
    top = get_node_attributes(graph, 'biparite')
    # Get edges
    for e in graph.edges():
        if top[e[0]] == 0:
            g.add_edge(e[0], e[1])
    return g


def build_d(g, match):
    d = DiGraph()
    m = DiGraph()
    for node, data in g.nodes(data=True):
        d.add_node(node, biparite=data['biparite'])
        m.add_node(node, biparite=data['biparite'])

    m_edges = []
    for s, t in match.items():
        m_edges.append((s, t))
    data = get_node_attributes(g, 'biparite')
    for ee in g.edges():
        if (ee[1], ee[0]) in m_edges or (ee[0], ee[1]) in m_edges:
            if data[ee[0]] == 0:
                d.add_edge(ee[0], ee[1])
                m.add_edge(ee[0], ee[1])
            else:
                d.add_edge(ee[1], ee[0])
        else:
            if data[ee[0]] == 0:
                d.add_edge(ee[1], ee[0])
            else:
                d.add_edge(ee[0], ee[1])
    return m, d


def find_cycle_in_dgm(d):
    path = list()
    for node in d.nodes():
        try:
            cycle = find_cycle(d, source=node, orientation=None)
            for e in cycle:
                if e[0] not in path:
                    path.append(e[0])
                if e[1] not in path:
                    path.append(e[1])
            path.append(node)
            return path
        except NetworkXNoCycle:
            continue
    return None


def construct_g_minus(g, e_start, e_end):
    g_minus = g.copy()
    g_minus.remove_edge(e_start, e_end)
    return g_minus


def construct_g_plus(g, e_start, e_end):
    g_plus = g.copy()
    # g_plus.remove_node(e_start)
    # g_plus.remove_node(e_end)
    for x in g.successors(e_start):
        g_plus.remove_edge(e_start, x)

    for x in g.reverse(copy=True).successors(e_end):
        if x != e_start:
            g_plus.remove_edge(x, e_end)
    return g_plus


def construct_d_from_gm2(g_plus, m_prime):
    d = g_plus.copy()
    for v in g_plus.nodes():
        for w in g_plus.successors(v):
            if not m_prime.has_edge(v, w):
                d.add_edge(w, v)
                d.remove_edge(v, w)
    return d


def create_example():
    francis = Graph()
    francis.add_nodes_from([1, 2, 3, 4, 5, 6], biparite=0)
    francis.add_nodes_from([7, 8, 9, 10], biparite=1)
    francis.add_edges_from([(1, 7), (1, 10)], capacity=1)
    francis.add_edges_from([(2, 10)], capacity=1)
    francis.add_edges_from([(3, 8), (3, 9)], capacity=1)
    francis.add_edges_from([(4, 7)], capacity=1)
    francis.add_edges_from([(5, 7)], capacity=1)
    francis.add_edges_from([(6, 10)], capacity=1)
    return francis
