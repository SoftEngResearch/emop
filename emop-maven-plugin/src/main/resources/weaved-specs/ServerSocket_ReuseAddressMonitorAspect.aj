package mop;
import java.net.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect ServerSocket_ReuseAddressMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public ServerSocket_ReuseAddressMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock ServerSocket_ReuseAddress_MOPLock = new ReentrantLock();
	static Condition ServerSocket_ReuseAddress_MOPLock_cond = ServerSocket_ReuseAddress_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut ServerSocket_ReuseAddress_set(ServerSocket sock) : (call(* ServerSocket+.setReuseAddress(..)) && target(sock)) && MOP_CommonPointCut();
	before (ServerSocket sock) : ServerSocket_ReuseAddress_set(sock) {
	}

	pointcut ServerSocket_ReuseAddress_bind(ServerSocket sock) : (call(* ServerSocket+.bind(..)) && target(sock)) && MOP_CommonPointCut();
	before (ServerSocket sock) : ServerSocket_ReuseAddress_bind(sock) {
	}

	pointcut ServerSocket_ReuseAddress_create_bound() : (call(ServerSocket.new(int, ..))) && MOP_CommonPointCut();
	after () returning (ServerSocket sock) : ServerSocket_ReuseAddress_create_bound() {
	}

	pointcut ServerSocket_ReuseAddress_create_unbound() : (call(ServerSocket.new())) && MOP_CommonPointCut();
	after () returning (ServerSocket sock) : ServerSocket_ReuseAddress_create_unbound() {
	}

}
