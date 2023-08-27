package VRPTW;

import ilog.concert.IloColumn;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class VrpTw {

    static final int customerNum = 25;
    static final int nodeNum = customerNum + 2;
    static final int vehicleNum = 25;
    static double gap = 1e-8;

    public static void main(String[] args) throws IOException, IloException {
        String path = "D:\\BaiduNetdiskDownload\\Note\\OR\\ORinstance\\solomon-100\\In\\c101.txt";
        Instance instance = readData(path);
        printInstance(instance);

        IloCplex Cplex = new IloCplex();

        IloNumVar[][][] X = new IloNumVar[instance.nodeNum][instance.nodeNum][instance.vehicleNum];
        IloNumVar[][] S = new IloNumVar[instance.nodeNum][instance.vehicleNum];
        for (int i = 0; i < instance.nodeNum; i++) {
            for (int j = 0; j < instance.nodeNum; j++) {
                for (int k = 0; k < instance.vehicleNum; k++) {
                    if (i == j) {
                        X[i][j][k] = null;
                    } else {
                        X[i][j][k] = Cplex.boolVar("X" + i + j + k);
                        S[i][k] = Cplex.intVar(0, Integer.MAX_VALUE, "S" + i + k);
                    }
                }
            }
        }

        IloNumExpr obj = Cplex.numExpr();
        for (int i = 0; i < instance.nodeNum; i++) {
            for (int j = 0; j < instance.nodeNum; j++) {
                if (i != j) {
                    System.out.println("i = " + i + ", j = " + j);
                    int x1 = instance.Nodes.get(i).Xcoor;
                    int y1 = instance.Nodes.get(i).Ycoor;
                    int x2 = instance.Nodes.get(j).Xcoor;
                    int y2 = instance.Nodes.get(j).Ycoor;
                    double arcDistance = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
                    arcDistance = double_truncate(arcDistance);
                    System.out.println(i + "\t" + j + "\t" + arcDistance);

                    for (int k = 0; k < instance.vehicleNum; k++) {
                        if (X[i][j][k] != null) {
                            // obj.addTerm(arcDistance, X[i][j][k]);
                            obj = Cplex.sum(obj, Cplex.prod(arcDistance, X[i][j][k]));
                        }
                    }
                }
            }
        }
        Cplex.addMinimize(obj);

        IloNumExpr expr = Cplex.numExpr();
        IloNumExpr expr1 = Cplex.numExpr();
        IloNumExpr expr2 = Cplex.numExpr();

        for (int i = 1; i < instance.nodeNum - 1; i++) {
            expr = Cplex.numExpr();
            for (int j = 0; j < instance.nodeNum; j++) {
                for (int k = 0; k < instance.vehicleNum; k++) {
                    if (i != j) {
                        expr = Cplex.sum(expr, X[i][j][k]);
                    }
                }
            }
            Cplex.addEq(expr, 1.0);
        }

        for (int k = 0; k < instance.vehicleNum; k++) {
            expr = Cplex.numExpr();
            for (int j = 1; j < instance.nodeNum; j++) {
                expr = Cplex.sum(expr, X[0][j][k]);
            }
            Cplex.addEq(expr, 1);
        }


        for (int k = 0; k < instance.vehicleNum; k++) {
            expr = Cplex.numExpr();
            for (int i = 1; i < instance.customerNum; i++) {
                for (int j = 0; j < instance.nodeNum; j++) {
                    expr = Cplex.sum(expr, Cplex.prod(instance.Nodes.get(i).demand, X[i][j][k]));
                }
            }
            Cplex.addLe(expr, instance.vehicleCapacity);
        }

        for (int k = 0; k < instance.vehicleNum; k++) {
            for (int h = 1; h < instance.nodeNum - 1; h++) {
                expr1 = Cplex.numExpr();
                expr2 = Cplex.numExpr();
                for (int i = 0; i < instance.nodeNum; i++) {
                    if (i != h) {
                        expr1 = Cplex.sum(expr1, X[i][h][k]);
                    }
                }
                for (int j = 0; j < instance.nodeNum; j++) {
                    if (j != h) {
                        expr2 = Cplex.sum(expr2, X[h][j][k]);
                    }
                }
                Cplex.addEq(expr1, expr2);
            }
        }

        for (int k = 0; k < instance.vehicleNum; k++) {
            expr = Cplex.numExpr();
            for (int i = 0; i < instance.nodeNum; i++) {
                expr = Cplex.sum(expr, X[i][instance.nodeNum - 1][k]);
            }
            Cplex.addEq(expr, 1.0);
        }

        int M = Integer.MAX_VALUE;
        for (int i = 0; i < instance.nodeNum; i++) {
            for (int j = 0; j < instance.nodeNum; j++) {
                for (int k = 0; k < instance.vehicleNum; k++) {
                    if (i != j) {
                        int x1 = instance.Nodes.get(i).Xcoor;
                        int y1 = instance.Nodes.get(i).Ycoor;
                        int x2 = instance.Nodes.get(j).Xcoor;
                        int y2 = instance.Nodes.get(j).Ycoor;
                        double arcDistance = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
                        arcDistance = double_truncate(arcDistance);

                        expr = Cplex.numExpr();
                        expr = Cplex.sum(expr, S[i][k]);
                        expr = Cplex.diff(expr, S[j][k]);
                        expr = Cplex.sum(expr, arcDistance);
                        expr = Cplex.diff(expr, M);
                        expr = Cplex.sum(expr, Cplex.prod(M, X[i][j][k]));
                        Cplex.addLe(expr, 0);
                    }
                }
            }
        }
        for (int i = 0; i < instance.nodeNum; i++) {
            for (int k = 0; k < instance.vehicleNum; k++) {
                expr = Cplex.numExpr();
                expr = Cplex.sum(expr, S[i][k]);
                Cplex.addGe(expr, instance.Nodes.get(i).readyTime);
                Cplex.addLe(expr, instance.Nodes.get(i).dueTime);
            }
        }

        Cplex.solve();

        System.out.println("\n\n----------以下是求解结果----------\n");
        System.out.println("Objective\t\t:" + Cplex.getObjValue());
        int vehicleNum = 0;
        for (int k = 0; k < instance.vehicleNum; k++) {
            wc:
            for (int i = 0; i < instance.nodeNum; i++) {
                nc:
                for (int j = 0; j < instance.nodeNum; j++) {
                    if (i != j) {
                        if (Cplex.getValue(X[i][j][k]) != 0 && i != 0 && j != instance.nodeNum - 1) {
                            vehicleNum += 1;
                            break wc;
                        }
                    }
                }
            }
        }
        System.out.println("VehicleNum\t\t:" + vehicleNum);

        int count = 1;
        int nextNode = 0;
        for (int k = 0; k < instance.vehicleNum; k++) {
            double load = 0;
            double distance = 0;
            w:
            while (nextNode != instance.nodeNum - 1) {
                int i = nextNode;
                wc:
                for (; i < instance.nodeNum; i++) {
                    nc:
                    for (int j = 0; j < instance.nodeNum; j++) {
                        if (i != j && Cplex.getValue(X[i][j][k]) != 0) {
                            if (i == 0 && j == instance.nodeNum - 1) {
                                break w;
                            } else if (i != 0 || j != instance.nodeNum - 1) {
                                System.out.println(nextNode + "-");
                                load += instance.Nodes.get(i).demand;

                                int x1 = instance.Nodes.get(i).Xcoor;
                                int y1 = instance.Nodes.get(i).Ycoor;
                                int x2 = instance.Nodes.get(j).Xcoor;
                                int y2 = instance.Nodes.get(j).Ycoor;
                                double arcDistance = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
                                arcDistance = double_truncate(arcDistance);

                                distance += arcDistance;

                                nextNode = j;
                                i = nextNode - 1;
                                if (nextNode == instance.nodeNum - 1) {
                                    System.out.println(0 + "\t\tCap: " + load + "\t " + "distance: " + distance + "\n");
                                    nextNode = 0;
                                    break w;
                                }
                                break nc;
                            }
                        }
                    }
                }
            }
            // System.out.println();
        }
    }

    public static  Instance readData(String path) throws IOException {
        Instance instance = new Instance();
        List<Node> Nodes = new ArrayList<Node>();

        BufferedReader br = new BufferedReader(new FileReader(path));

        String line = null;
        int count = 0;
        while ((line = br.readLine()) != null) {
            count += 1;
            System.out.println(line + count);
            if (count == 5) {
                String[] str = line.split("\\s+");
                instance.customerNum = customerNum;
                instance.nodeNum = nodeNum;
                instance.vehicleNum = Integer.parseInt(str[1]);
                instance.vehicleCapacity = Integer.parseInt(str[2]);
            } else if (count >= 10 && count <= 10 + customerNum) {
                String[] str = line.split("\\s+");
                Node node = new Node();
                node.ID = Integer.parseInt(str[1]);
                node.Xcoor = Integer.parseInt(str[2]);
                node.Ycoor = Integer.parseInt(str[3]);
                node.demand = Integer.parseInt(str[4]);
                node.readyTime = Integer.parseInt(str[5]);
                node.dueTime = Integer.parseInt(str[6]);
                node.serviceTime = Integer.parseInt(str[7]);
                Nodes.add(node);
            }
        }
        Node node1 = new Node();
        node1.ID = instance.customerNum + 1;
        node1.Xcoor = Nodes.get(0).Xcoor;
        node1.Ycoor = Nodes.get(0).Ycoor;
        node1.demand = Nodes.get(0).demand;
        node1.readyTime = Nodes.get(0).readyTime;
        node1.dueTime = Nodes.get(0).dueTime;
        node1.serviceTime = Nodes.get(0).serviceTime;
        Nodes.add(node1);

        instance.Nodes = Nodes;
        br.close();

        return instance;
    }

    public  static void printInstance(Instance instance) {
        System.out.println("vehicleNum" + "\t\t: " + instance.vehicleNum);
        System.out.println("vehicleCapacity" + "\t\t: " + instance.vehicleCapacity);
        for (Node node : instance.Nodes) {
            System.out.println(node.ID + "\t\t: " +
                               node.Xcoor + "\t" +
                               node.Ycoor + "\t" +
                               node.demand + "\t" +
                               node.readyTime + "\t" +
                               node.dueTime + "\t" +
                               node.serviceTime);
        }
    }

    public static double double_truncate(double v) {
        int iv = (int) v;
        if (iv + 1 - v <= gap)
            return  iv + 1;
        double dv = (v - iv) * 10;
        int idv = (int) dv;
        double rv = iv + idv / 10.0;
        return rv;
    }
}