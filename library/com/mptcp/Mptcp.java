package com.mptcp;
import java.io.IOException;
import java.net.Socket;
import java.lang.RuntimeException;
import java.io.FileInputStream;
import sun.misc.SharedSecrets;

public class Mptcp{
    /**
        Structure that defines an host tuple (host, port)
    */
    public static class HostTuple{
        public int port;
        public String host;

        public HostTuple(String host, int port){
            this.host = host;
            this.port = port;
        }

        public String toString(){
            return this.host + ":" + this.port; 
        }
    }

    /**
        Private variables
    */
	private int fileDescriptor; 
	
	
	// -- Public interface -----------------------------------------------------
	
	/**
	    Closes one specific subflow
	    
	    @param sock : the mptcp source socket 
	    @param subId : the id of the subflow to close
	    @param how : how to close the subflow
	    
	    @return ?? todo ??
	*/
	public static int closeSubflow(Socket sock, int subId, int how){
		return _native_closeSubflow(getSocketFD(sock), subId, how); 
	}
	/**
	    Gets the list of all subflows
	    
	    @param sock : the mptcp source socket 
	    
	    @return ?? todo ??
	*/
	public static int[][] getSubflowList(Socket sock){
            	return _native_getSubflowList(getSocketFD(sock));
	}
	/**
	    Gets the source and destination host tuple of a given subflow
	    
	    @param sock : the mptcp source socket 
	    @param subid : the id of the subflow to inspect
	    
	    @return ?? todo ??
	*/
	public static HostTuple[] getSubflowTuple(Socket sock, int subid){

	    String get=_native_getSubflowTuple(getSocketFD(sock), subid);
	    String[] part=get.split(" ");
	    String[] h=part[0].split(":");
	    String[] d=part[1].split(":");
 
	    return new HostTuple[]{new HostTuple(h[0],Integer.parseInt(h[1])), new HostTuple(d[0], Integer.parseInt(d[1]))}; 
	}
	
	/**
	    Opens a new subflow
	    
	    @param sock : the mptcp source socket 
	    @param source : the host tuple of the source
	    @param dest : the host tuple of the destination 
	    @param priority : the priority of the subflow
	    
	    @return ?? todo ??
	*/
	public static int openSubflow(Socket sock, HostTuple source, HostTuple dest, int priority){
	    return _native_openSubflow(getSocketFD(sock), source.host, source.port, dest.host, dest.port, priority); 
	}
	
	// -- private interface ----------------------------------------------------
	
	// TODO !!! MAKE REAL EXCEPTION SYSTEM (for GETFD) and for other methods
	/**
	    Helper methods
	*/
	private static int getSocketFD(Socket s){
	    try{
	        int fd = SharedSecrets.getJavaIOFileDescriptorAccess().get(
                    ((FileInputStream)s.getInputStream()).getFD()
                );  
            if(fd <= 0){
                throw new RuntimeException("Native error"); 
            }  
            return fd; 
           }
        catch(IOException e){
            throw new RuntimeException("Native error"); 
        }
	}
	// -- native interface -----------------------------------------------------
	private native static int[][] _native_getSubflowList(int fd); 
	private native static String _native_getSubflowTuple(int fd, int subid); 
	private native static int _native_closeSubflow(int fd, int subid, int how); 
	private native static int _native_openSubflow(int fd, String source_host, int source_port, String dest_host, int dest_port, int priority); 

	// -- linking interface ----------------------------------------------------
	
    /**
        Linking library 
    */
	static {
		// we need to load the native library here
		try{
			System.load("libmptcp-api.so"); 
		}
		catch(UnsatisfiedLinkError e){
			// when a .so is packed inside a jar file, we need to locate and import
			//  the file
			try {
				NativeUtils.loadLibraryFromJar("/native/libmptcp-api.so"); 
			} 
			catch (IOException e1) {
			throw new RuntimeException(e1);
			}
		}
	}
}
