package mop;
import java.io.*;
import java.util.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect Collections_SynchronizedMapMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Collections_SynchronizedMapMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Collections_SynchronizedMap_MOPLock = new ReentrantLock();
	static Condition Collections_SynchronizedMap_MOPLock_cond = Collections_SynchronizedMap_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Collections_SynchronizedMap_accessIter(Iterator iter) : (call(* Iterator.*(..)) && target(iter)) && MOP_CommonPointCut();
	before (Iterator iter) : Collections_SynchronizedMap_accessIter(iter) {
	}

	pointcut Collections_SynchronizedMap_sync() : (call(* Collections.synchronizedMap(Map)) || call(* Collections.synchronizedSortedMap(SortedMap))) && MOP_CommonPointCut();
	after () returning (Map syncMap) : Collections_SynchronizedMap_sync() {
	}

	pointcut Collections_SynchronizedMap_createSet(Map syncMap) : ((call(Set Map+.keySet()) || call(Set Map+.entrySet()) || call(Collection Map+.values())) && target(syncMap)) && MOP_CommonPointCut();
	after (Map syncMap) returning (Collection col) : Collections_SynchronizedMap_createSet(syncMap) {
	}

	pointcut Collections_SynchronizedMap_syncCreateIter(Collection col) : (call(* Collection+.iterator()) && target(col)) && MOP_CommonPointCut();
	after (Collection col) returning (Iterator iter) : Collections_SynchronizedMap_syncCreateIter(col) {
		//Collections_SynchronizedMap_syncCreateIter
		//Collections_SynchronizedMap_asyncCreateIter
	}

}
