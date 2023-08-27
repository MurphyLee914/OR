from gurobipy import *
import numpy as np
import copy
import matplotlib.pyplot as plt

RLP = Model("relaxed MIP")
x = {}
for i in range(2):
    x[i] = RLP.addVar(lb=0,
                      ub=GRB.INFINITY,
                      vtype=GRB.BINARY,
                      name="x_" + str(i))
RLP.setObjective(100 * x[0] + 150 * x[1], GRB.MAXIMIZE)
RLP.addConstr(2 * x[0] + x[1] <= 10, name="c1")
RLP.addConstr(3 * x[0] + 6 * x[1] <= 40, name="c2")

RLP.optimize()


class Node:
    def __init__(self):
        self.local_LB = 0
        self.local_UB = np.inf
        self.x_sol = {}
        self.x_int_sol = {}
        self.branch_var_list = []
        self.model = None
        self.cnt = None
        self.is_integer = False

    def deepcopy_node(node):
        new_node = Node()
        new_node.local_LB = 0
        new_node.local_UB = np.inf
        new_node.x_sol = copy.deepcopy(node.x_sol)
        new_node.x_int_sol = copy.deepcopy(node.x_int_sol)
        new_node.branch_var_list = []
        new_node.model = node.model.copy()
        new_node.cnt = node.cnt
        new_node.is_integer = node.is_integer

        return new_node


def Branch_and_bound(RLP):
    RLP.optimize()
    global_UB = RLP.ObjVal
    global_LB = 0
    eps = 1e-3
    incumbent_node = None
    Gap = np.inf

    # BnB starts
    Queue = []
    node = Node()
    node.local_LB = 0
    node.local_UB = global_UB
    node.model = RLP.copy()
    node.model.setParam("OutputFlag", 0)
    node.cnt = 0
    Queue.append(node)

    cnt = 0
    Global_UB_change = []
    Global_LB_change = []
    while len(Queue) > 0 and global_UB - global_LB > eps:
        current_node = Queue.pop()
        cnt += 1

        current_node.model.optimize()
        Solution_status = current_node.model.Status

        is_integer = True
        Is_Pruned = False

        if Solution_status == 2:
            for var in current_node.model.getVars():
                current_node.x_sol[var.varName] = var.x
                print(var.varName, var.x)

                current_node.x_int_sol[var.varName] = int(var.x)
                if abs(var.x - int(var.x)) >= eps:
                    is_integer = False
                    current_node.branch_var_list.append(var.varName)

            if is_integer:
                current_node.is_integer = True
                current_node.local_UB = current_node.model.ObjVal
                current_node.local_LB = current_node.model.ObjVal
                if current_node.local_LB > global_LB:
                    global_LB = current_node.local_LB
                    incumbent_node = Node.deepcopy_node(current_node)
            if not is_integer:
                current_node.is_integer = False
                current_node.local_UB = current_node.model.ObjVal
                current_node.local_LB = 0
                for var_name in current_node.x_int_sol.keys():
                    var = current_node.model.getVarByName(var_name)
                    current_node.local_LB += current_node.x_int_sol[var_name] * var.obj
                if current_node.local_LB > global_LB or current_node.local_LB == global_LB and current_node.is_integer == True:
                    global_LB = current_node.local_LB
                    incumbent_node = Node.deepcopy_node(current_node)
                    incumbent_node.local_LB = current_node.local_LB
                    incumbent_node.local_UB = current_node.local_UB

            # Pruning
            if is_integer:
                Is_Pruned = True

            if not is_integer and current_node.local_UB < global_LB:
                Is_Pruned = True

            Gap = round(100 * (global_UB - global_LB) / global_LB, 2)
            print('\n ---------- \n', cnt, '\t Gap = ', Gap, ' %')
        elif Solution_status != 2:
            is_integer = False
            Is_Pruned = True
            continue

        if not Is_Pruned:
            branch_var_name = current_node.branch_var_list[0]
            left_var_bound = int(current_node.x_sol[branch_var_name])
            right_var_bound = int(current_node.x_sol[branch_var_name]) + 1

            left_node = Node.deepcopy_node(current_node)
            right_node = Node.deepcopy_node(current_node)

            temp_var = left_node.model.getVarByName(branch_var_name)
            left_node.model.addConstr(temp_var <= left_var_bound, name='branch_left' + str(cnt))
            left_node.model.setParam('OutputFlag', 0)
            left_node.model.update()
            cnt += 1
            left_node.cnt = cnt

            temp_var = right_node.model.getVarByName(branch_var_name)
            right_node.model.addConstr(temp_var >= right_var_bound, name='branch_right' + str(cnt))
            right_node.model.setParam('OutputFlag', 0)
            right_node.model.update()
            cnt += 1
            right_node.cnt = cnt

            Queue.append(left_node)
            Queue.append(right_node)

            temp_global_UB = 0
            for node in Queue:
                node.model.optimize()
                if node.model.Status == 2:
                    if node.model.ObjVal > temp_global_UB:
                        temp_global_UB = node.model.ObjVal

            global_UB = temp_global_UB
            Global_UB_change.append(global_UB)
            Global_LB_change.append(global_LB)

    global_UB = global_LB
    Gap = round(100 * (global_UB - global_LB) / global_LB, 2)
    Global_UB_change.append(global_UB)
    Global_LB_change.append(global_LB)

    print('\n\n\n\n')
    print('----------')
    print('BnB Opt Slo Found')
    print('----------')
    print('\nFinal Gap = ', Gap, ' %')
    print('Opt Slo:', incumbent_node.x_int_sol)
    print('Opt Obj:', global_LB)

    return incumbent_node, Gap, Global_UB_change, Global_LB_change


incumbent_node, Gap, Global_UB_change, Global_LB_change = Branch_and_bound(RLP)


fig = plt.figure(1)
plt.figure(figsize=(15, 10))
font_dict = {'family': 'Times New Roman',
             'style': 'oblique',
             'weight': 'normal',
             'color': 'green',
             'size': 20
             }

plt.rcParams['figure.figsize'] = (12.0, 8.0)
plt.rcParams['font.family'] = 'Times New Roman'
plt.rcParams['font.size'] = 16

x_cor = range(1, len(Global_LB_change) + 1)
plt.plot(x_cor, Global_LB_change, 'b', label='LB')
plt.plot(x_cor, Global_UB_change, 'r', label='UB')
plt.legend()
plt.xlabel('Iteration', fontdict=font_dict)
plt.ylabel('Bounds update', fontdict=font_dict)
plt.title('Bounds update during BnB Optimization', fontsize=23)
plt.savefig('BnB_update.eps')
plt.show()