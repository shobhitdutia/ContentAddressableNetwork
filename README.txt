Distributed Systems - Project 1, Shobhit N. Dutia
The project is developed using RMI and sockets.
The implementation covers node join, insert, search and display. Leave is NOT attempted and the leave option simply  exits out of the code.

Identtification of zone:
In the display method, 
lx refers to lower x coordinate of the zone
hx refers to higher x coordinate of the zone
ly refers to lower y coordinate of the zone
hy refers to higher y coordinate of the zone
Server
The bootstrap server should contain the interface as well as the implementation of the interface
The bootstrap server is named Bootstrap.java and the interface and implementation files are named JoinInterface.java  and JoinImpl.java respectively.
Kindly put these three files in the server directory

Client:
The client should contain the Peer.java, JoinInterface.java and the Coordinate.java files

To start the project, first run BootStrap.java file on the server machine and then run Peer.java on the client machines