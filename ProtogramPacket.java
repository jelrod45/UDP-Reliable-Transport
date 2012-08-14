import java.lang.Math.*;
import java.net.*;
import java.io.*;

/*
  Class: ProtogramPacket
  Author: Jay Elrod
  Description: This class is used to represent a packet of
      custom type. It's fields will typically represent parsed
      datagram information. ProtogramPackets can be transformed
      into DatagramPackets and vice versa via ProtogramPacket
      methods. This is necessary because a DatagramSocket only
      accepts DatagramPackets and the class cannot be extended.
 */

public class ProtogramPacket {
    long start;
    long end;
    byte[] payload;
    InetAddress address;
    int port = 1111;

    //default constructor
    public ProtogramPacket() {
	this.start = -1;
	this.end = 0;
	this.port = 0;
    }	

    //data packet
    public ProtogramPacket (long start, long end, byte[] payload, InetAddress address, int port) {
	this.start = start;
	this.end = end;
	this.payload = payload;
	this.address = address;
	this.port = port;
    }

    //ACK packet
    public ProtogramPacket (long start, long end, InetAddress address, int port) {
	this.start = start;
	this.end = end;
	this.payload = new byte[0];
	this.address = address;
	this.port = port;
    }

    //returns Protogram in datagram format
    public DatagramPacket asDatagram() {
	byte[] bytes = new byte[payload.length+32];
	int i;
	for ( i=0; i<Long.toString(this.start).length(); i++ )
	    bytes[i] = (byte)(Long.toString(this.start).charAt(i));
	while ( i<16 ) { bytes[i] = (byte)(' '); i++; }
	for ( i=i; i<(Long.toString(this.end).length())+16; i++ )
	    bytes[i] = (byte)(Long.toString(this.end).charAt(i-16));
	while ( i<32 ) { bytes[i] = (byte)(' '); i++; }
	for ( int j=0; j<payload.length; j++) {
	    bytes[i] = payload[j];
	    i++;
	}
	return new DatagramPacket(bytes, payload.length+32, address, port);
    }

    //writes the payload of the protogram to the specified output stream
    public void writePayload(OutputStream destination) {
	try {
	    destination.write(payload);
	} catch ( Exception e ) {
	    e.printStackTrace();
	}
    }

    //comparison to tell if two protograms are identical
    public boolean equals(ProtogramPacket anotherProtogram) {
	if ( this.start != anotherProtogram.start ||
	     this.end != anotherProtogram.end ) return false;
	for ( int i=0; i<Math.min(this.payload.length, anotherProtogram.payload.length); i++ )
	    if ( this.payload[i] != anotherProtogram.payload[i] ) return false;
	return true;
    }

    //returns datagram in protogram format
    public static ProtogramPacket fromDatagram(DatagramPacket pkt) {
	byte[] bytes;
	long newStart = Long.parseLong((new String(pkt.getData())).substring(0,15).trim());
	long newEnd = Long.parseLong((new String(pkt.getData())).substring(16,31).trim());
	try {
	    bytes = new byte[(int)(newEnd - newStart)];
	} catch ( Exception e ) {
	    bytes = new byte[0];
	}
	for (int i=0; i<bytes.length; i++)
	    bytes[i] = pkt.getData()[i+32];
	
	return new ProtogramPacket (newStart, newEnd, bytes, pkt.getAddress(), pkt.getPort());				    
    }

}