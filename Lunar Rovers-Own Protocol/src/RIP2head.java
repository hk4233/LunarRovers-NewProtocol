import java.nio.ByteBuffer;


class RIP2head {
    private static final int pckt_TypeOffset = 8;
    private static final int numOffset = 9;
    private static final int dest_IPOffset = 0;
    
    private static final int lenOffset = 13;
    private static final int contsOffset = 17;
    private static final int src_IPOffset = 4;

    private StringBuilder getIP(int offset, byte[] pckt) {
        StringBuilder ip = new StringBuilder();
        for (int i = offset; i < offset + 4; i++) {
            int part = Byte.toUnsignedInt(pckt[i]);
            ip.append(part);
            if (i != offset + 3) {
                ip.append('.');
            }
        }
        return ip;
    }
    
    RIP2Packet getRipcomPacket(byte[] pckt) {
        StringBuilder srcIP = getIP(src_IPOffset, pckt);
        StringBuilder destIP = getIP(dest_IPOffset, pckt); 

        FlagsDifferent pcktType;
        int type = pckt[pckt_TypeOffset];

        byte[] numbersArray = new byte[4];
        System.arraycopy(pckt, numOffset, numbersArray, 0, 4);
        int num = ByteBuffer.wrap(numbersArray).getInt();

        byte[] lenArray = new byte[4];
        System.arraycopy(pckt, lenOffset, lenArray, 0, 4);
        int len = ByteBuffer.wrap(lenArray).getInt();

        byte[] conts = new byte[len];
        System.arraycopy(pckt, contsOffset, conts, 0, len);

        switch (type) {
            case 1:
                pcktType = FlagsDifferent.SEQ;
                break;
            case 2:
                pcktType = FlagsDifferent.ACK;
                break;
            case 3:
                pcktType = FlagsDifferent.FIN_ACK;
                break;
            default:
                pcktType = FlagsDifferent.FIN;
        }

        return new RIP2Packet(destIP.toString(), srcIP.toString(), pcktType,
                num, len, conts);
    }

}