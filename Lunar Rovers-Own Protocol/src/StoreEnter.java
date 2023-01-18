import java.util.ArrayList;

class StoreEnter {
    private int rovID;
    private ArrayList<RoutingTableEntry> arrlist;
    

    StoreEnter(ArrayList<RoutingTableEntry> arrlist, int rovID) {
        this.arrlist = arrlist;
        this.rovID = rovID;
    }

    ArrayList<RoutingTableEntry> getArrayList() {
        return arrlist;
    }

    int getRoverID() {
        return rovID;
    }
    
}
