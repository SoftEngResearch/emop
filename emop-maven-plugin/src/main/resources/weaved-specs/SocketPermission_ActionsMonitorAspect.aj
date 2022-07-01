package mop;
import java.net.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect SocketPermission_ActionsMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public SocketPermission_ActionsMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock SocketPermission_Actions_MOPLock = new ReentrantLock();
	static Condition SocketPermission_Actions_MOPLock_cond = SocketPermission_Actions_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut SocketPermission_Actions_construct(String actions) : (call(SocketPermission.new(String, String)) && args(*, actions)) && MOP_CommonPointCut();
	before (String actions) : SocketPermission_Actions_construct(actions) {
	}

}
