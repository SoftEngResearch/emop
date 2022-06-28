package mop;
import java.io.*;
import java.lang.*;
import java.security.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect SecurityManager_PermissionMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public SecurityManager_PermissionMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock SecurityManager_Permission_MOPLock = new ReentrantLock();
	static Condition SecurityManager_Permission_MOPLock_cond = SecurityManager_Permission_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut SecurityManager_Permission_check(SecurityManager manager, Object context) : (call(* SecurityManager.checkPermission(Permission, Object)) && target(manager) && args(.., context)) && MOP_CommonPointCut();
	before (SecurityManager manager, Object context) : SecurityManager_Permission_check(manager, context) {
	}

	pointcut SecurityManager_Permission_get(SecurityManager manager) : (call(* SecurityManager.getSecurityContext(..)) && target(manager)) && MOP_CommonPointCut();
	after (SecurityManager manager) returning (Object context) : SecurityManager_Permission_get(manager) {
	}

}
