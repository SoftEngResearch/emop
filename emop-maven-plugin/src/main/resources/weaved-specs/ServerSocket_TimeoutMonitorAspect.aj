package mop;
import java.net.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect ServerSocket_TimeoutMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public ServerSocket_TimeoutMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock ServerSocket_Timeout_MOPLock = new ReentrantLock();
	static Condition ServerSocket_Timeout_MOPLock_cond = ServerSocket_Timeout_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut ServerSocket_Timeout_set(int timeout) : (call(* ServerSocket+.setSoTimeout(int)) && args(timeout)) && MOP_CommonPointCut();
	before (int timeout) : ServerSocket_Timeout_set(timeout) {
	}

}
