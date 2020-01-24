package ir.sbu.ac;

import java.util.Arrays;

public class RouterNode {
    private int myID;
    private GuiTextArea myGUI;
    private RouterSimulator sim;
    private int[] costs = new int[RouterSimulator.NUM_NODES];
    private int[] minCosts = new int[RouterSimulator.NUM_NODES];
    private int[] path = new int[RouterSimulator.NUM_NODES];
    private int protocolMetric = 1000;

    //--------------------------------------------------
    public RouterNode(int ID, RouterSimulator sim, int[] costs) {
        myID = ID;
        this.sim = sim;
        myGUI = new GuiTextArea("  Output window for Router #" + ID + "  ");

        System.arraycopy(costs, 0, this.costs, 0, RouterSimulator.NUM_NODES);
        minCosts = costs.clone();
        for (int i = 0; i < path.length; i++) {
            path[i] = myID;
        }
        sendTable(false);
    }

    private void sendTable(boolean sendOriginal) {
        int[] toSend = minCosts;
        if (sendOriginal) {
            toSend = costs;
        }
        for (int i = 0; i < RouterSimulator.NUM_NODES; i++) {
            if (toSend[i] != RouterSimulator.INFINITY) {
                sendUpdate(new RouterPacket(myID, i, toSend));
            }
        }
    }

    private boolean hasMetric(int[] costs) {
        for (int cost : costs) {
            if (cost == protocolMetric) {
                return true;
            }
        }
        return false;
    }

    //--------------------------------------------------
    public void recvUpdate(RouterPacket pkt) {
        boolean changed = false;
        myGUI.println(pkt.sourceid + " " + pkt.destid + " " + Arrays.toString(pkt.mincost));
        for (int i = 0; i < pkt.mincost.length; i++) {
            if (minCosts[i] > minCosts[pkt.sourceid] + pkt.mincost[i]) {
                minCosts[i] = minCosts[pkt.sourceid] + pkt.mincost[i];
                path[i] = pkt.sourceid;
                changed = true;
            }
        }
        if (changed) {
            sendTable(false);
        }
    }


    //--------------------------------------------------
    private void sendUpdate(RouterPacket pkt) {
        sim.toLayer2(pkt);
    }


    //--------------------------------------------------
    public void printDistanceTable() {
        myGUI.println("Current table for " + myID +
                "  at time " + sim.getClocktime());
        myGUI.println(Arrays.toString(minCosts));
    }

    //--------------------------------------------------
    public void updateLinkCost(int dest, int newcost) {
        costs[dest] = protocolMetric;
        sendTable(true);
        costs[dest] = newcost;
    }
}
