
QUESTION:
Now that we have a router for our rovers, we need a reliable
transfer protocol to ensure our packets arrive back on Earth
safely. Your goal is to produce and demonstrate a protocol that
can guarantee the delivery of the packets. The packets produced
must also move easily through the regular Internet. The resulting
protocol must, in theory, perform better than TCP with smaller
packets and hopefully fewer packets sent.

SOLUTION:

The UDP2.0 has been implemented to send data packets efficiently, calclulating the distance between each of the routers. Also, this protocol makes use of 2D parity checking to verify whether an error has taken place or not, and where exactly the error has taken place


 A UDP2.0 Packet has the following structure -


    |-----------|---------------|-------------|---------------|------------|----------------------|-------------|
    | Dest IP   |   Source IP   |     Type    |    SEQ No.    |    Length  |  Contents of Length  | Parity Check|
    |-----------|---------------|-------------|---------------|------------|----------------------|-------------|



 UDP2.0 has been derived from the base idea of the Routing Information Protocol 

This implements the situation where there exist 3 rovers, A, B and C and while C is still trying to communicate with A, it may go out of reach. To handle this situation, C communicates with A through B and B will send the packets to A.

Also, this Protocol finds the shortest and most efficient distance to send packets from one node to another. In this manner, when C goes out of bounds, it would be able to send the packet through B to A.

Dest IP and Source IP:
The source port serves analogues to the destination port, but is used by the sending host to help keep track of new incoming connections and existing data streams.

As most of you are well aware, in TCP/UDP data communications, a host will always provide a destination and source port number. We have already analysed the destination port, and how it allows the host to select the service it requires. 

PACKET TYPE:
The packet type defines the state of the router whether it is sending or receiving. Based on its
state, the packet type will be defined by the following.

FIN. this tells that this is the last packet to be sent, and that it is closing the connection
FIN_ACK. this tells that the final packet has been received and the connection is going to close
SEQ. this tells what packet the sender is sending
ACK: this tells what the packet is expecting to receive next
 
 
Number:
This tells the acknowledgement that the rover must send back

CONTENTS:
This consists of the text, or files that are being sent in bytes

PARITY CHECK:
The parity check utilises 2D parity check in order to verify whether there has been an error in transmission or not. When the additional parity bit line has been sent, the receiver can verify whether the message received is correct or not, and in case there is any error, it would mention where the error exists as the parity check is 2 dimensional.