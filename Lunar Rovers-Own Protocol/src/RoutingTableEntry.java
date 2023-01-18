
class RoutingTableEntry {
    byte crrntMask;
    String AddrIP;
    byte count;
    String nextRov;

    RoutingTableEntry(String AddrIP, byte crrntMask, String nextRov, byte count) {
        this.crrntMask = crrntMask;
        this.nextRov = nextRov;
        this.AddrIP = AddrIP;
        this.count = count;
    }
}
