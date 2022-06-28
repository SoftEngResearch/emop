package mop;
import java.net.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect NetPermission_ActionsMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public NetPermission_ActionsMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock NetPermission_Actions_MOPLock = new ReentrantLock();
	static Condition NetPermission_Actions_MOPLock_cond = NetPermission_Actions_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut NetPermission_Actions_construct(String actions) : (call(NetPermission.new(String, String)) && args(.., actions)) && MOP_CommonPointCut();
	before (String actions) : NetPermission_Actions_construct(actions) {
	}

}
