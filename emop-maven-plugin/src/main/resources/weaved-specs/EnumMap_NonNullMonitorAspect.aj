package mop;
import java.util.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect EnumMap_NonNullMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public EnumMap_NonNullMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock EnumMap_NonNull_MOPLock = new ReentrantLock();
	static Condition EnumMap_NonNull_MOPLock_cond = EnumMap_NonNull_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut EnumMap_NonNull_insertnull_4(Map m) : (call(* EnumMap.putAll(Map)) && args(m)) && MOP_CommonPointCut();
	before (Map m) : EnumMap_NonNull_insertnull_4(m) {
	}

	pointcut EnumMap_NonNull_insertnull_3(Object e) : (call(* EnumMap.put(Object, Object)) && args(e, ..)) && MOP_CommonPointCut();
	before (Object e) : EnumMap_NonNull_insertnull_3(e) {
	}

}
