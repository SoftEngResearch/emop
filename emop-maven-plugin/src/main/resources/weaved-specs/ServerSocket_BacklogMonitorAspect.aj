package mop;
import java.net.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect ServerSocket_BacklogMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public ServerSocket_BacklogMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock ServerSocket_Backlog_MOPLock = new ReentrantLock();
	static Condition ServerSocket_Backlog_MOPLock_cond = ServerSocket_Backlog_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut ServerSocket_Backlog_set(int backlog) : (call(* ServerSocket+.bind(SocketAddress, int)) && args(*, backlog)) && MOP_CommonPointCut();
	before (int backlog) : ServerSocket_Backlog_set(backlog) {
	}

	pointcut ServerSocket_Backlog_construct(int backlog) : ((call(ServerSocket.new(int, int)) || call(ServerSocket.new(int, int, InetAddress))) && args(*, backlog, ..)) && MOP_CommonPointCut();
	before (int backlog) : ServerSocket_Backlog_construct(backlog) {
	}

}
