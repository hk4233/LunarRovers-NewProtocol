import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


public class Rover extends Thread {
    private final static int Update_Freq = 5000; 
    private final static byte Def_mask = 24;
    private final static int Inf = 16;     
    private final static int TimeOut = 10000;  
    private final static int Max_entries_sent = 20;
    private final static int Buff_Cap = 5000;
    private final static int WinSize = 1;
    private final static int PcktTimeout = 500; 
    private final static int RecSize = 5056;
    private DataInputStream inputStream;
    private FileOutputStream outputStream;
    private long lenCounter;
    private DatagramSocket dataSocket;
    private int sequenceNum = 0;
    private int ackNum = 0;
    private HashMap<String, Timer> tmrs = new HashMap<>();
    private HashMap<Integer, RIP2Packet> win = new HashMap<>();
    private ArrayList<RoutingTableEntry> routTable;
    private HashMap<Integer, Timer> pckttmr = new HashMap<>();

    int RipPort = 520;
    String MCIP = "233.33.33.33";
    boolean verbOutputs;
    int verbLevel = 200;
    int MCPort = 20001;     
    int udpPort = 6767;
    String filename;
    int rovID;
    String destIP;

    private final String selfIP;

    private void allocateSocket() {
        try {
            dataSocket = new DatagramSocket(RipPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    
    private void setIP(int[] addrIP, byte[] pckt, int i) {
        for (int j = 0; j < 4; j++) {
            addrIP[j] = Byte.toUnsignedInt(pckt[i++]);
        }
    }
    
    private String getSelfIP() throws SocketException, UnknownHostException {
        DatagramSocket dataSoc = new DatagramSocket();
        dataSoc.connect(InetAddress.getByName("8.8.8.8"), 25252);
        return dataSoc.getLocalAddress().getHostAddress();
    }

    
    private Rover() throws SocketException, UnknownHostException {
        routTable = new ArrayList<>();
        selfIP = getSelfIP();
    }

    private void startTimer(String AddrIP, int RovID) {
        Timer timer;
        if (tmrs.containsKey(AddrIP)) {
            timer = tmrs.get(AddrIP);
            timer.cancel();
        }
        timer = new Timer();
        tmrs.put(AddrIP, timer);
        String localIP = getIP(RovID);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println(localIP + " timed out!");
                RoutingTableEntry r = findTableEntry(localIP);
                r.count = Inf;
                ArrayList<RoutingTableEntry> arrayList =
                        getEntries(AddrIP);
                for (RoutingTableEntry routingTableEntry : arrayList) {
                    routingTableEntry.count = Inf;
                }
                displayRouteTable();
                try {
                    sendPacketMessage();      
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }, TimeOut);
    }

    private void startListening() {
        try {
            MulticastSocket soc = new MulticastSocket(MCPort);
            byte[] buff = new byte[256];
            InetAddress iGroup = InetAddress.getByName(MCIP);
            while (true) {
                DatagramPacket dataPckt = new DatagramPacket(buff,
                        buff.length);
                soc.receive(dataPckt);
                StoreEnter entryHolder = extractEntries(dataPckt);
                int recRoverID = entryHolder.getRoverID();
                if (recRoverID == rovID) {   //Ignore self packets
                    continue;
                }
                ArrayList<RoutingTableEntry> recEntries =
                        entryHolder.getArrayList();
                addSingleRoutingEntry(recRoverID, dataPckt.getAddress());
                startTimer(dataPckt.getAddress().getHostAddress(),
                        recRoverID);
                updateRoutingTable(recEntries, dataPckt.getAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   
    private void startThreads() {
        Thread listner =
                new Thread(this::startListening); 
        listner.start();

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    sendPacketMessage();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }, 0, Update_Freq);


        Thread udpthread = new Thread(() -> {
            try {
                udpServer();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        udpthread.start();
    }

   
    private void sendPacketMessage() throws UnknownHostException {
        byte[] buff = getDataPacket();
        InetAddress iGroup = null;

        try {
            iGroup = InetAddress.getByName(MCIP);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        DatagramPacket dataPckt = new DatagramPacket(buff, buff.length,
                iGroup, MCPort);

        try {
            dataSocket.send(dataPckt);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private StoreEnter extractEntries(DatagramPacket dataPckt) {
        byte[] recPckt = new byte[dataPckt.getLength()];
        System.arraycopy(dataPckt.getData(), 0, recPckt, 0,
                recPckt.length);
        StoreEnter entryHolder = decodeRIPPacket(recPckt);
        ArrayList<RoutingTableEntry> entries = entryHolder.getArrayList();

        if (verbOutputs) {
            System.out.println("Received the following Table Entries from " +
                    dataPckt.getAddress().getHostAddress());
            System.out.println("Address\t\tNextHop\t\tCost");
            for (RoutingTableEntry r : entries) {
                System.out.println(r.AddrIP + "\t" + r.nextRov + "\t" + r.count);
            }
        }

        return entryHolder;
    }
    
    private void addSingleRoutingEntry(int recRovID,
                                       InetAddress inetAddr) {
        if (recRovID == rovID) {
            return;
        }

        String ipToAdd = getIP(recRovID);
        String nextRov = inetAddr.getHostAddress();
        boolean isInTable = false;
        boolean isChanged = false;
        for (RoutingTableEntry routTabularEntry : routTable) {
            if (routTabularEntry.AddrIP.equals(ipToAdd)) {
                isInTable = true;
                if (routTabularEntry.count != 1) {
                    routTabularEntry.nextRov = nextRov;
                    routTabularEntry.count = 1;
                    isChanged = true;
                    break;
                }
            }
        }

        if (!isInTable) {
            RoutingTableEntry r = new RoutingTableEntry(ipToAdd, Def_mask
                    , nextRov, (byte) 1);
            routTable.add(r);
            isChanged = true;
        }

        if (isChanged) {
            displayRouteTable();
        }
    }

    private byte[] getDataPacket() throws UnknownHostException {
        ArrayList<Byte> list = new ArrayList<>();

        byte command = (byte) (1);
        byte zero = 0;

        list.add(command);     

        byte version = 2;
        list.add(version);  

        list.add(zero);
        list.add((byte) rovID);

        for (RoutingTableEntry r : routTable) {
            list.add(zero);
            list.add((byte) 2);     

            byte routeTag = 1;
            list.add(routeTag);
            list.add(routeTag);    

            String ip = r.AddrIP;
            byte[] ipBytes = InetAddress.getByName(ip).getAddress();

            for (byte b : ipBytes) {
                list.add(b);       
            }

            byte subMask = r.crrntMask;
            list.add(zero);
            list.add(zero);
            list.add(zero);
            list.add(subMask);  

            String nextRov = r.nextRov;
            byte[] nextRovBytes = InetAddress.getByName(nextRov).getAddress();

            for (byte b : nextRovBytes) {
                list.add(b);      
            }

            byte count = r.count;
            list.add(zero);
            list.add(zero);
            list.add(zero);
            list.add(count);       
        }

        int size = list.size();
        Byte[] bytes = list.toArray(new Byte[size]);
        byte[] ripPckt = new byte[size];
        int i = 0;
        for (byte b : bytes) {
            ripPckt[i++] = b;
        }
        return ripPckt;
    }

    private String getIPAddressInStringForm(int[] addrIP) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < addrIP.length; i++) {
            sb.append(addrIP[i]);
            if (i < addrIP.length - 1) sb.append(".");
        }

        return sb.toString();
    }

    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    private StoreEnter decodeRIPPacket(byte[] pckt) {
        ArrayList<RoutingTableEntry> list = new ArrayList<>();
        int i = 3;
        int RovID = pckt[i++];
        while (i < pckt.length) {
            byte[] AFI = new byte[2];
            AFI[0] = pckt[i++];
            AFI[1] = pckt[i++];

            byte[] route = new byte[2];
            route[0] = pckt[i++];
            route[1] = pckt[i++];

            int[] addrIP = new int[4];
            setIP(addrIP, pckt, i);
            i += 4;
            String addrIPString = getIPAddressInStringForm(addrIP);

            i += 3;
            byte subnetMask = pckt[i++];

            int[] nextRov = new int[4];
            setIP(nextRov, pckt, i);
            i += 7;
            String nextRovStringForm = getIPAddressInStringForm(nextRov);

            byte count = pckt[i];

            RoutingTableEntry r = new RoutingTableEntry(addrIPString,
                    subnetMask, nextRovStringForm, count);
            list.add(r);
            i++;
        }
        return new StoreEnter(list, RovID);
    }

    private void displayRouteTable() {
        System.out.println();
        System.out.println("============================");
        System.out.println("Routing Table Entries");
        System.out.println("Address\t\tNextHop\t\tCost");
        for (RoutingTableEntry r : routTable) {
            System.out.println(r.AddrIP + "/" + Def_mask + "\t" + r.nextRov + "\t" + r.count);
        }
        System.out.println();
    }

    private String getIP(int rovID) {
        return "10.0." + rovID + ".0";
    }

    private void updateRoutingTable(ArrayList<RoutingTableEntry> recTable,
                                    InetAddress inetAddr) throws UnknownHostException {
        String senderIP = inetAddr.getHostAddress();
        boolean isUpdated = false;

        for (RoutingTableEntry r : recTable) {
            String addrIP = r.AddrIP;
            int rovID = Integer.parseInt(addrIP.split("\\.")[2]);
            RoutingTableEntry routTableEntry =
                    findTableEntry(addrIP);
            if (rovID != this.rovID) {
                byte count = (byte) (r.count + 1);
                if (count > Inf) {
                    count = Inf;
                }

                if (routTableEntry == null) {
                    String localIP = getIP(rovID);
                    routTableEntry = new RoutingTableEntry(localIP,
                            Def_mask, senderIP, count);
                    routTable.add(routTableEntry);
                    isUpdated = true;
                    continue;
                }
                if (r.nextRov.equals(selfIP)) {
                    
                    continue;
                }
                if (count < getCost(routTableEntry)) {
                    routTableEntry.nextRov = senderIP;
                    routTableEntry.count = count;
                    isUpdated = true;
                } else {
                    
                    if (senderIP.equals(routTableEntry.nextRov)) {
                        if (routTableEntry.count != count) {
                            routTableEntry.count = count;
                            isUpdated = true;
                        }
                    }
                }
            }
        }

        if (verbOutputs || isUpdated) {
            displayRouteTable();
        }
        if (isUpdated)
            sendPacketMessage();    
    }


    private int getCost(RoutingTableEntry routTableEntry) {
        return routTableEntry != null ? routTableEntry.count : Inf;
    }
   
    private RoutingTableEntry findTableEntry(String ip) {
        for (RoutingTableEntry r : routTable) {
            if (r.AddrIP.equals(ip)) {
                return r;
            }
        }
        return null;
    }

    private ArrayList<RoutingTableEntry> getEntries(String ip) {
        ArrayList<RoutingTableEntry> list = new ArrayList<>();
        for (RoutingTableEntry routTableEntry : routTable) {
            if (routTableEntry.nextRov.equals(ip)) {
                list.add(routTableEntry);
            }
        }
        return list;
    }

    private RoutingTableEntry getEntryForDestination(String destIP) throws InterruptedException {
        if (verbLevel <= 1) {
            System.out.println("Finding next hop for " + destIP);
        }
        RoutingTableEntry routTableEntry = findTableEntry(destIP);
        int retryCounter = 0;
        while (routTableEntry == null || routTableEntry.count == Inf) {
            if (routTableEntry == null) {
                System.out.println("Could not find an entry for " + destIP + ". " +
                        "Retrying in " + Update_Freq + "ms ...");
            } else {
                System.out.println("Cannot send packet to " + destIP + " as the " +
                        "cost is " + Inf + ". Will retry again in " + Update_Freq + " ms ...");
            }
            Thread.sleep(Update_Freq);
            routTableEntry = findTableEntry(destIP);
            retryCounter++;
            if (retryCounter >= Max_entries_sent) {
                System.out.println("Max retry limit reached, giving up on sending to " + destIP);
                return null;
            }
        }
        return routTableEntry;
    }

    private void udpServer() throws IOException, InterruptedException {
        DatagramSocket serv = new DatagramSocket(udpPort);
        byte[] buff = new byte[RecSize];
        while (true) {
            DatagramPacket pckt = new DatagramPacket(buff, buff.length);
            serv.receive(pckt);
            RIP2head ripcomPcktManager =
                    new RIP2head();
            RIP2Packet ripcomPckt =
                    ripcomPcktManager.getRipcomPacket(pckt.getData());

            if (verbLevel <= 1) {
                System.out.println("Received a Ripcom packet.");
                System.out.println("Unpacking...");
                System.out.println(ripcomPckt);
            }
            String destIP = ripcomPckt.getDestinationIP();
            if (destIP.equals(getIP(rovID))) {
                acceptPacket(ripcomPckt);
            } else {
                if (verbLevel <= 1) {
                    System.out.println("Forwarding packet");
                }
                sendPacket(ripcomPckt);
            }
        }
    }

    private void cancelTimer(int num) {
        win.remove(num);
        if (verbLevel <= 1) {
            System.out.println("Number of elements in window: " + win.size());
            System.out.println("Removing timer for this packet: " + num);
        }
        Timer timer = pckttmr.get(num);
        timer.cancel();
        pckttmr.remove(num);
    }

    
    private void acceptPacket(RIP2Packet pckt) throws IOException, InterruptedException {
        // ClientParityCheck cp = new ClientParityCheck();
        // String packet = cp.makeString(ripcomPacket.getBytes().toString());

        FlagsDifferent packetType = pckt.getPacketType();
        switch (packetType) {
            case SEQ:
                if (verbLevel <= 1) {
                    System.out.println("Received SEQ " + pckt.getNumber());
                }
                boolean expPckt = true;
                if (pckt.getNumber() != ackNum) {
                    expPckt = false;
                    if (verbLevel <= 1) {
                        System.out.println("Received a wrong packet: " + pckt.getNumber());
                        System.out.println("Sending ACK again for packet: " + ackNum);
                    }
                }
                if (expPckt) {
                    ackNum++;
                    byte[] message = pckt.getContents();
                    if (outputStream == null) {
                        outputStream =
                                new FileOutputStream("output");
                    }
                    outputStream.write(message);
                }
                String destIP = pckt.getSourceIP();
                RIP2Packet ackPckt = new RIP2Packet(
                        destIP,
                        getIP(rovID),
                        FlagsDifferent.ACK,
                        ackNum,
                        0,
                        new byte[0]);
                win.remove(ackNum - 1);
                win.put(ackNum, ackPckt);
                sendPacket(ackPckt);
                break;
            case ACK:
                int num = pckt.getNumber();
                if (verbLevel <= 1) {
                    System.out.println("Received ACK " + num);
                }
                cancelTimer(num - 1);
                add(pckt.getSourceIP());
                RIP2Packet nxtPckt = win.get(num);
                sendPacket(nxtPckt);
                startTimer(nxtPckt.getNumber());
                break;
            case FIN:
                if (verbLevel <= 1) {
                    System.out.println("Received FIN " + pckt.getNumber());
                }
                byte[] msg = pckt.getContents();
                outputStream.write(msg);
                outputStream.close();
                System.out.println("Received message successfully. See file output " +
                        "for the final output.");
                ackNum++;
                destIP = pckt.getSourceIP();
                RIP2Packet finAckPckt = new RIP2Packet(
                        destIP,
                        getIP(rovID),
                        FlagsDifferent.FIN_ACK,
                        ackNum,
                        0,
                        new byte[0]);
                win.remove(ackNum - 1);
                if (verbLevel <= 1) {
                    System.out.println("Sending FIN_ACK packet, ackNumber is " + ackNum);
                }
                win.put(ackNum, finAckPckt);
                sendPacket(finAckPckt);
                break;
            case FIN_ACK:
                if (verbLevel <= 1) {
                    System.out.println("Received FIN_ACK " + pckt.getNumber());
                }
                cancelTimer(sequenceNum - 1);
                System.out.println("Finished sending all data");
                if (verbLevel <= 1) {
                    System.out.println("Completed sending. Window size: " + win.size());
                    System.out.println("Packet timer size: " + pckttmr.size());
                }
                break;
        }
    }


    private void sendPacket(RIP2Packet Pckt) throws IOException, InterruptedException {
        // ClientParityCheck cp = new ClientParityCheck();
        // String packet = cp.makeString(ripcomPacket.getBytes().toString());
        String destIP = Pckt.getDestinationIP();
        RoutingTableEntry routTableEntry = getEntryForDestination(destIP);
        if (routTableEntry != null) {
            if (verbLevel <= 1) {
                System.out.println("Sending to: " + routTableEntry.nextRov);
            }
            byte[] buff = Pckt.getBytes();
            DatagramSocket dataSoc = new DatagramSocket();
            InetAddress inetAddress = InetAddress.getByName(routTableEntry.nextRov);
            DatagramPacket dataPckt = new DatagramPacket(buff, buff.length,
                    inetAddress, udpPort);
            dataSoc.send(dataPckt);
            if (verbLevel <= 1) {
                System.out.println("Sent successfully.");
            }
        }
    }

   
    private byte[] removeContents(byte[] conts) {
        int size = 0;
        for (int i = 0; i < conts.length; i++) {
            if (conts[i] == 0) {
                size = i;
                break;
            }
        }

        byte[] buff = new byte[size];
        System.arraycopy(conts, 0, buff, 0, size);
        return buff;
    }

   
    private void add(String destIP) throws IOException {
        FlagsDifferent type = FlagsDifferent.SEQ;
        byte[] conts = new byte[Buff_Cap];
        if (inputStream.read(conts) == -1) {
            type = FlagsDifferent.FIN;
        }
        if (Buff_Cap > lenCounter) {
            conts = removeContents(conts);
        } else {
            lenCounter -= Buff_Cap;
        }

        RIP2Packet pckt = new RIP2Packet(destIP,
                getIP(rovID), type, sequenceNum, conts.length, conts);
        win.put(sequenceNum, pckt);
        sequenceNum++;
    }

    
    private void startTimer(int num) {
        Timer timer = new Timer();
        pckttmr.put(num, timer);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (verbLevel <= 1) {
                    System.out.println("Packet number " + num + " timed out!");
                }
                RIP2Packet pckt = win.get(num);
                try {
                    if (pckt != null) {
                        sendPacket(pckt);
                        startTimer(pckt.getNumber());
                    }
                } catch (IOException | InterruptedException e) {
                    System.err.println();
                }

            }
        }, PcktTimeout);
    }

   
    private void getFlags() throws IOException, InterruptedException {
        if (destIP != null) {
            File file = new File(filename);
            lenCounter = file.length();
            System.out.println();
            inputStream = new DataInputStream(new FileInputStream(file));
            for (int i = 0; i < WinSize; i++) {
                add(destIP);
            }
            for (int i = 0; i < WinSize; i++) {
                RIP2Packet pckt = win.get(i);
                sendPacket(pckt);
                startTimer(pckt.getNumber());
            }
        }
    }


    public static void main(String[] args) throws ExceptArgs, IOException, InterruptedException {
        Rover rover = new Rover();
        new ArgumentParse().parseArguments(args, rover);
        rover.allocateSocket();
        rover.startThreads();
        rover.getFlags();
    }
}