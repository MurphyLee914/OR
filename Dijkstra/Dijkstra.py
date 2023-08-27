import pandas as pd
import numpy as np
import networkx as nx
import matplotlib.pyplot as plt
import copy
import re
import math

Nodes = ['s', 'a', 'b', 'c', 't']

Arcs = {('s', 'a'): 5,
        ('s', 'b'): 8,
        ('a', 'c'): 2,
        ('b', 'a'): 10,
        ('c', 'b'): 3,
        ('b', 't'): 4,
        ('c', 't'): 3
        }

Graph = nx.DiGraph()
cnt = 0
pos_location = {}
for name in Nodes:
    cnt += 1
    X_coor = np.random.randint(1, 10)
    Y_coor = np.random.randint(1, 10)
    Graph.add_node(name,
                   ID=cnt,
                   node_type='normal',
                   demand=0,
                   x_coor=X_coor,
                   y_coor=Y_coor,
                   min_dis=0,
                   previous_node=None
                   )
    pos_location[name] = (X_coor, Y_coor)

for key in Arcs.keys():
    Graph.add_edge(key[0], key[1],
                   length=Arcs[key],
                   travelTime=0,
                   )


def Dijkstra(Graph, org, des):
    big_M = 1000000
    Queue = []
    for node in Graph.nodes:
        Queue.append(node)
        if node == org:
            Graph.nodes[node]['min_dis'] = 0
        else:
            Graph.nodes[node]['min_dis'] = big_M

    while(len(Queue) > 0):
        min_dis = big_M
        current_node = None
        for node in Queue:
            if Graph.nodes[node]['min_dis'] < min_dis:
                current_node = node
                min_dis = Graph.nodes[node]['min_dis']
        if current_node != None:
            Queue.remove(current_node)
        for child in Graph.successors(current_node):
            arc_key = (current_node, child)
            dis_temp = Graph.nodes[current_node]['min_dis'] + Graph.edges[arc_key]['length']
            if dis_temp < Graph.nodes[child]['min_dis']:
                Graph.nodes[child]['min_dis'] = dis_temp
                Graph.nodes[child]['previous_node'] = current_node

    opt_dis = Graph.nodes[des]['min_dis']
    current_node = des
    opt_path = [current_node]
    while(current_node != org):
        current_node = Graph.nodes[current_node]['previous_node']
        opt_path.insert(0, current_node)

    return Graph, opt_dis, opt_path


Graph, opt_dis, opt_path = Dijkstra(Graph, 's', 't')
print(opt_dis)
print(opt_path)
