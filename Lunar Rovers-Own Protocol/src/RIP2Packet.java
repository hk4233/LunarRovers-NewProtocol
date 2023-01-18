import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

class RIP2Packet {

    private String destIP;   
    private String srcIP;        
    private FlagsDifferent pcktType;       
    private int num;            
    private int len;           
    private byte[] conts;   

    RIP2Packet(String destIP, String srcIP, FlagsDifferent pcktType, int num,
                 int len, byte[] conts) {
                     
        this.pcktType = pcktType;
        this.num = num;
        this.len = len;            
        this.destIP = destIP;
        this.conts = conts;
        this.srcIP = srcIP;
        
    }


    FlagsDifferent getPacketType() {
        return pcktType;
    }

    int getNumber() {
        return num;
    }

    String getSourceIP() {
        return srcIP;
    }

    byte[] getContents() {
        return conts;
    }

    String getDestinationIP() {
        return destIP;
    }


    byte[] getBytes() throws UnknownHostException {
        ArrayList<Byte> arrayList = new ArrayList<>();
        byte[] IPAddr = InetAddress.getByName(destIP).getAddress();
        for (byte b : IPAddr) {
            arrayList.add(b);                           
        }

        IPAddr = InetAddress.getByName(srcIP).getAddress();
        for (byte b : IPAddr) {
            arrayList.add(b);                           
        }

        if (pcktType == FlagsDifferent.SEQ) {
            arrayList.add((byte) 1);
        } else if (pcktType == FlagsDifferent.FIN_ACK) {
            arrayList.add((byte) 3);
        } else if (pcktType == FlagsDifferent.ACK) {            
            arrayList.add((byte) 2);
        } else {
            arrayList.add((byte) 0);
        }

        byte[] nums = ByteBuffer.allocate(4).putInt(num).array();
        for (byte n : nums) {                           
            arrayList.add(n);
        }

        byte[] lengthBytes = ByteBuffer.allocate(4).putInt(len).array();
        for (byte l : lengthBytes) {
            arrayList.add(l);                           
        }

        byte[] contentsBytes = conts;
        for (byte b : contentsBytes) {
            arrayList.add(b);                          
        }

        byte[] buffer = new byte[arrayList.size()];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = arrayList.get(i);
        }
        return buffer;
    }

    @Override
    public String toString() {
        return "========== Ripcom Packet==========" + "\n" +
                "Destination IP: " + destIP + "\n" +
                "Source IP: " + srcIP + "\n" +
                "Type: " + pcktType + "\n" +
                "Number: " + num + "\n" +
                "Length: " + len + "\n" +
                "Contents: <NOT DISPLAYED>\n";
    }
}
