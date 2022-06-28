package mop;
import java.net.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect ServerSocket_PortMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public ServerSocket_PortMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock ServerSocket_Port_MOPLock = new ReentrantLock();
	static Condition ServerSocket_Port_MOPLock_cond = ServerSocket_Port_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut ServerSocket_Port_construct_port(int port) : ((call(ServerSocket.new(int)) || call(ServerSocket.new(int, int)) || call(ServerSocket.new(int, int, InetAddress))) && args(port, ..)) && MOP_CommonPointCut();
	before (int port) : ServerSocket_Port_construct_port(port) {
	}

}
