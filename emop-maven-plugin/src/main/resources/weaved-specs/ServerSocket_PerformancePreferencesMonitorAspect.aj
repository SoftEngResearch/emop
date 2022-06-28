package mop;
import java.net.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect ServerSocket_PerformancePreferencesMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public ServerSocket_PerformancePreferencesMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock ServerSocket_PerformancePreferences_MOPLock = new ReentrantLock();
	static Condition ServerSocket_PerformancePreferences_MOPLock_cond = ServerSocket_PerformancePreferences_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut ServerSocket_PerformancePreferences_set(ServerSocket sock) : (call(* ServerSocket+.setPerformancePreferences(..)) && target(sock)) && MOP_CommonPointCut();
	before (ServerSocket sock) : ServerSocket_PerformancePreferences_set(sock) {
	}

	pointcut ServerSocket_PerformancePreferences_bind(ServerSocket sock) : (call(* ServerSocket+.bind(..)) && target(sock)) && MOP_CommonPointCut();
	before (ServerSocket sock) : ServerSocket_PerformancePreferences_bind(sock) {
	}

	pointcut ServerSocket_PerformancePreferences_create_bound() : (call(ServerSocket.new(int, ..))) && MOP_CommonPointCut();
	after () returning (ServerSocket sock) : ServerSocket_PerformancePreferences_create_bound() {
	}

	pointcut ServerSocket_PerformancePreferences_create_unbound() : (call(ServerSocket.new())) && MOP_CommonPointCut();
	after () returning (ServerSocket sock) : ServerSocket_PerformancePreferences_create_unbound() {
	}

}
