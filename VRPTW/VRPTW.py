from __future__ import print_function
from gurobipy import *
import re
import math
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd


class Data:
    customerNum = 0
    nodeNum = 0
    vehicleNum = 0
    capacity = 0
    cor_X = []
    cor_Y = []
    demand = []
    readyTime = []
    dueTime = []
    serviceTime = []
    disMatrix = [[]]


class Solution:
    ObjVal = 0
    X = [[[]]]
    S = [[]]
    routes = [[]]
    routeNum = 0

    def __init__(self):
        self.ObjVal = model.ObjVal
        self.X = [[([0] * data.vehicleNum) for i in range(data.nodeNum)] for j in range(data.nodeNum)]
        self.S = [([0] * data.nodeNum) for j in range(data.vehicleNum)]
        self.route = [[]]

    def getSolution(self, data, model):
        solution = Solution()
        solution.ObjVal = model.ObjVal
        Solution.X = [[([0] * data.vehicleNum) for i in range(data.nodeNum)] for j in range(data.nodeNum)]
        solution.S = [([0] * data.nodeNum) for j in range(data.vehicleNum)]
        solution.route = [[]]

        print("\n\n----------这个是最优解----------")
        for m in model.getVars():
            str = re.split((r"_", m.Varname))
            if str[0] == "X" and m.x == 1:
                print("str[1] = %d" % int(str[1]))
                print("str[2] = %d" % int(str[2]))
                print("str[3] = %d" % int(str[3]))
                print("m.x = ", end="")
                print(m.x)
                solution.X[int(str[1])][int(str[2])][int(str[3])] = m.x
                print(str, end="")
            elif str[0] == "S" and m.x == 1:
                solution.S[int(str[1])][int(str[2])] = m.x
        print("----------solution----------")
        for k in range(data.vehicleNum):
            for i in range(data.nodeNum):
                for j in range(data.nodeNum):
                    if solution.X[i][j][k] > 0 and not (i == 0 and j == data.nodeNum - 1):
                        print("x{0}, {1}, {2}] = {3}".format(i, j, k, solution.X[i][j][k]))

        print(solution.X)
        for k in range(data.vehicleNum):
            i = 0
            subRoute = []
            subRoute.append(i)
            finish = False
            while not finish:
                for j in range(data.nodeNum):
                    if solution.X[i][j][k] > 0:
                        subRoute.append(i)
                        i = j
                        if j == data.nodeNum - 1:
                            finish = True

            if len(subRoute) >= 3:
                subRoute[len(subRoute) - 1] = 0
                solution.routes.append(subRoute)
                solution.routeNum += 1

        print("\n\n----------Routes of Vehicles----------")
        print(solution.routes)

        print("\n\n----------Drawing the graph----------")
        # data1 = Data()
        # readData(data1, path, 100)
        fig = plt.figure(0)
        plt.xlabel("X")
        plt.ylabel("Y")
        plt.title("All customers")
        '''
        marker='o'
        '''
        plt.scatter(data.cor_X[0], data.cor_Y[0], c='blue', alpha=1, marker=',', linewidths=3, label='depot')
        plt.scatter(data.cor_X[1:-1], data.cor_Y[1:-1], c='black', alpha=1, marker='o', linewidths=3, label='customer')

        for k in range(solution.routeNum + 1):
            for i in range(len(solution.routes[k]) - 1):
                a = solution.routes[k][i]
                b = solution.routes[k][i + 1]
                x = [data.cor_X[a], data.cor_X[b]]
                y = [data.cor_Y[a], data.cor_Y[b]]
                plt.plot(x, y, 'k', linewidth=1)

        plt.grid(False)
        plt.legend(loc='best')
        plt.show()

        return solution


def readData(data, path, customerNum):
    data.customerNum = customerNum
    data.nodeNum = customerNum + 2
    f = open(path, 'r')
    count = 0
    for line in f.readlines():
        count += 1
        if count == 3:
            line = line[:-1]
            str = re.split(r' +', line)
            data.vehicleNum = int(str[0])
            data.capacity = float(str[1])
        elif 7 <= count <= customerNum + 6:
            line = line[:-1]
            str = re.split(r' +', line)
            data.cor_X.append(float(str[2]))
            data.cor_Y.append(float(str[3]))
            data.demand.append(float(str[4]))
            data.readyTime.append(float(str[5]))
            data.dueTime.append(float(str[6]))
            data.serviceTime.append(float(str[7]))

    data.cor_X.append(data.cor_X[0])
    data.cor_Y.append(data.cor_Y[0])
    data.demand.append(data.demand[0])
    data.readyTime.append(data.readyTime[0])
    data.dueTime.append(data.dueTime[0])
    data.serviceTime.append(data.serviceTime[0])

    data.disMatrix = [([0] * data.nodeNum) for p in range(data.nodeNum)]
    for i in range(data.nodeNum):
        for j in range(data.nodeNum):
            data.disMatrix[i][j] = math.sqrt(
                (data.cor_X[i] - data.cor_X[j]) ** 2 + (data.cor_Y[i] - data.cor_Y[j]) ** 2)

    return data


def printData(data, customerNum):
    print("vehicleNum = ", data.vehicleNum)
    print("capacity = ", data.capacity)
    for i in range(len(data.demand)):
        print('{0}\t{1}\t{2}\t{3}'.format(data.demand[i], data.readyTime[i], data.dueTime[i], data.serviceTim[i]))

    for i in range(data.nodeNum):
        for j in range(data.nodeNum):
            print("%6.2f" % data.disMatrix[i][j], end="")
        print()


data = Data()

path = "D:\\BaiduNetdiskDownload\\Note\\OR\\ORinstance\\solomon-100\\In\\R101.txt"
customerNum = 100
readData(data, path, customerNum)
printData(data, customerNum)

# ==========build model===========
big_M = 10000
model = Model("VRPTW")
X = [[[[] for k in range(data.vehicleNum)] for j in range(data.nodeNum)] for i in range(data.nodeNum)]
S = [[[] for k in range(data.vehicleNum)] for i in range(data.nodeNum)]
for i in range(data.nodeNum):
    for k in range(data.vehicleNum):
        name1 = "S_" + str(i) + "_" + str(k)
        S[i][k] = model.addVar(data.readyTime[i], data.dueTime[i], vtype=GRB.CONTINUOUS, name=name1)
        for j in range(data.nodeNum):
            name2 = "X_" + str(i) + "_" + str(j) + "_" + str(k)
            X[i][j][k] = model.addVar(0, 1, vtype=GRB.BINARY, name=name2)

obj = LinExpr(0)
for i in range(data.nodeNum):
    for k in range(data.vehicleNum):
        if i != j:
            for k in range(data.vehicleNum):
                obj.addTerms(data.disMatrix, X[i][j][k])

model.setObjective(obj, GRB.MINIMIZE)

for i in range(1, data.nodeNum - 1):
    expr = LinExpr(0)
    for j in range(data.nodeNum):
        if i != j:
            for k in range(data.vehicleNum):
                if i != 0 and i != data.nodeNum - 1:
                    expr.addTerms(1, X[i][j][k])

    model.addConstr(expr == 1, "c1")
    expr.clear()

for k in range(data.vehicleNum):
    expr = LinExpr(0)
    for i in range(1, data.nodeNum - 1):
        for j in range(data.nodeNum):
            if i != 0 and i != data.nodeNum - 1 and i != j:
                expr.addTerms(data.demand[i], X[i][j][k])
    model.addConstr(expr <= data.capacity, "c2")
    expr.clear()

for k in range(data.vehicleNum):
    for h in range(1, data.nodeNum - 1):
        expr1 = LinExpr(0)
        expr2 = LinExpr(0)
        for i in range(data.nodeNum):
            if h != i:
                expr1.addTerms(1, X[i][h][k])

        for j in range(data.nodeNum):
            if h != j:
                expr2.addTerms(1, X[h][j][k])

        model.addConstr(expr1 == expr2, "c4")
        expr1.clear()
        expr2.clear()

for k in range(data.vehicleNum):
    expr = LinExpr(0)
    for i in range(1, data.nodeNum - 1):
        expr.addTerms(1, X[i][data.nodeNum - 1][k])
    model.addConstr(expr == 1, "c5")
    expr.clear()

for k in range(data.vehicleNum):
    for i in range(data.nodeNum):
        for j in range(data.nodeNum):
            if i != j:
                model.addConstr(S[i][k] + data.disMatrix[i][j] -S[j][k] <= big_M - big_M * X[i][j][k], "c6")


model.write("VRPTW.lp")
model.optimize()
print("\n\n-----optimal value-----")
print(model.objVal)

for m in model.getVars():
    if m.x == 1:
        print(m.varName, m.x)


# solution = Solution()
# solution = solution.getSolution(data, model)