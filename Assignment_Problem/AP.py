from gurobipy import *
import numpy as np
import random

employee_num = job_num = 5
cost_matrix = np.zeros((employee_num, job_num))
for i in range(employee_num):
    for j in range(job_num):
        random.seed(i * employee_num + j)
        cost_matrix[i][j] = round(10 * random.random() + 5, 0)

model = Model('Assignment_Problem')

x = [[[]for i in range(employee_num)] for j in range(job_num)]
for i in range(employee_num):
    for j in range(job_num):
        x[i][j] = model.addVar(vtype=GRB.BINARY, name="x_"+str(i)+"_"+str(j))

obj = LinExpr(0)

for i in range(employee_num):
    for j in range(job_num):
        obj.addTerms(cost_matrix[i][j], x[i][j])

model.setObjective(obj, GRB.MINIMIZE)

for j in range(employee_num):
    expr = LinExpr(0)
    for i in range(job_num):
        expr.addTerms(1, x[i][j])
    model.addConstr(expr == 1, name="D_"+str(j))

for i in range(employee_num):
    expr = LinExpr(0)
    for j in range(job_num):
        expr.addTerms(1, x[i][j])
    model.addConstr(expr == 1, name="R_"+str(i))

model.write('model_ap.lp')
model.optimize()

for var in model.getVars():
    if var.x > 0:
        print(var.varName, '\t', var.x)
print('Objective: ', model.objVal)

