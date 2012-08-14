import java.net.*;
import java.io.*;

/*
  Class: UrftServer
  Author: Jay Elrod
  Description: This class acts as a server process for a file upload. Its file output
      is relative to the directory under which it is run. The server
      occupies a port, listens for an incoming packet, and receives
      a file upload from another host.
 */

public class UrftServer {    
    public static void main (String[] args) {
	DatagramSocket DGSock = null;
	OutputStream dataOut = null;
	ProtogramPacket lastReceived = new ProtogramPacket();
	ProtogramPacket lastSent = new ProtogramPacket();;
	DatagramPacket packetDock = null;
	byte[] bbuff = null;
	String filename = "";
	long filesize = 0;
	int port = 0;

	//usage checking
	try { port = Integer.parseInt(args[0]); } catch ( Exception e ) {
	    System.out.println("Usage: java UrftServer [port]");
	    System.exit(-1);
	}

	//setup
	try {
	    bbuff = new byte[512];
	    DGSock = new DatagramSocket(port);
	    packetDock = new DatagramPacket(bbuff, bbuff.length);
	    try {
		DGSock.receive(packetDock);
		DGSock.setSoTimeout(1000);
		lastReceived = ProtogramPacket.fromDatagram(packetDock);	    
		filename = ((new String(lastReceived.payload)).split(" ")[0]);
		filesize = Long.parseLong((new String(lastReceived.payload)).split(" ")[1]);
		dataOut = (OutputStream)(new FileOutputStream(new File(filename)));
	    } catch ( Exception e ) {
		System.out.println("Received bad file descriptor!\nTerminating program...");
		System.exit(-1);
	    }
		
	    //traffic loop
	    while (lastReceived.end != -1) {
		if ( (lastReceived.end > 0) && ((long)lastReceived.end > ((FileOutputStream)(dataOut)).getChannel().size())
		     && (lastReceived.payload.length > 0) ) {
		    lastReceived.writePayload(dataOut);
		    System.out.println("Upload " + ((lastReceived.end*100)/filesize*100)/100 + " percent complete.");
		}
		
		//create ACK packet
		lastSent = new ProtogramPacket(lastReceived.start,
					       lastReceived.end,
					       lastReceived.asDatagram().getAddress(),
					       lastReceived.asDatagram().getPort());
		
		//guarantee delivery
		while (true) {
		    try {
			DGSock.send(lastSent.asDatagram());
			DGSock.receive(packetDock);
			lastReceived = ProtogramPacket.fromDatagram(packetDock);
			break;
		    } catch ( SocketTimeoutException e ) {
		    } catch ( Exception e ) {
			e.printStackTrace();
		    }
		}
	    }
	} catch ( Exception e ) {
	    e.printStackTrace();
	}

	//FIN cycle
	lastSent = new ProtogramPacket(-1, -1, lastReceived.asDatagram().getAddress(), lastReceived.asDatagram().getPort());
	//try { DGSock.setSoTimeout(5000); } catch ( Exception e ) {}

	while (true) {
	    try {
		DGSock.send(lastSent.asDatagram());
		DGSock.receive(packetDock);
		DGSock.send(lastSent.asDatagram());
		lastReceived = ProtogramPacket.fromDatagram(packetDock);
	    } catch ( SocketTimeoutException e ) {
		break;
	    } catch ( Exception e ) {
		e.printStackTrace();
	    }
	}

	System.out.println("Server process terminated.");
	
    }
}