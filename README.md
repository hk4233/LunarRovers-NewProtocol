# Lunar Rovers New Protocol

UDP2.0 has been derived from the base idea of the Routing Information Protocol

This implements the situation where there exist 3 rovers, A, B and C and while C is still trying to communicate with A, it may go out of reach. To handle this situation, C communicates with A through B and B will send the packets to A.

Also, this Protocol finds the shortest and most efficient distance to send packets from one node to another. In this manner, when C goes out of bounds, it would be able to send the packet through B to A.

To build this project, use the following steps,

To compile the rovers javac *.java

Initialise a Rover using java Rover -r <ID>. Note that there can only be a maximum of 128 rovers, so the ID cannot exceed 128 For ex: java Rover -r 5 -i 10.178.11.1 -d 10.2.9.1 -f test -v 1 will start a new Rover with:

where test file would be the file that we would send over

After this rover is running, we can build other rovers on other machines and the rover will compute the shortest path to execute
