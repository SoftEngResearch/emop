package mop;
import java.util.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect Dictionary_NullKeyOrValueMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Dictionary_NullKeyOrValueMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Dictionary_NullKeyOrValue_MOPLock = new ReentrantLock();
	static Condition Dictionary_NullKeyOrValue_MOPLock_cond = Dictionary_NullKeyOrValue_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Dictionary_NullKeyOrValue_putnull(Dictionary d, Object key, Object value) : (call(* Dictionary+.put(..)) && args(key, value) && target(d)) && MOP_CommonPointCut();
	before (Dictionary d, Object key, Object value) : Dictionary_NullKeyOrValue_putnull(d, key, value) {
	}

}
