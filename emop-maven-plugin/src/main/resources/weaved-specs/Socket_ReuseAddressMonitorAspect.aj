package mop;
import java.net.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect Socket_ReuseAddressMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Socket_ReuseAddressMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Socket_ReuseAddress_MOPLock = new ReentrantLock();
	static Condition Socket_ReuseAddress_MOPLock_cond = Socket_ReuseAddress_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Socket_ReuseAddress_set(Socket sock) : (call(* Socket+.setReuseAddress(..)) && target(sock)) && MOP_CommonPointCut();
	before (Socket sock) : Socket_ReuseAddress_set(sock) {
	}

	pointcut Socket_ReuseAddress_bind(Socket sock) : (call(* Socket+.bind(..)) && target(sock)) && MOP_CommonPointCut();
	before (Socket sock) : Socket_ReuseAddress_bind(sock) {
	}

	pointcut Socket_ReuseAddress_create_connected() : (call(Socket.new(InetAddress, int)) || call(Socket.new(InetAddress, int, boolean)) || call(Socket.new(InetAddress, int, InetAddress, int)) || call(Socket.new(String, int)) || call(Socket.new(String, int, boolean)) || call(Socket.new(String, int, InetAddress, int))) && MOP_CommonPointCut();
	after () returning (Socket sock) : Socket_ReuseAddress_create_connected() {
	}

	pointcut Socket_ReuseAddress_create_unconnected() : (call(Socket.new()) || call(Socket.new(Proxy)) || call(Socket.new(SocketImpl))) && MOP_CommonPointCut();
	after () returning (Socket sock) : Socket_ReuseAddress_create_unconnected() {
	}

}
