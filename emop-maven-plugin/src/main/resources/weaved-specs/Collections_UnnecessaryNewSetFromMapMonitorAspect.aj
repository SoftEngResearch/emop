package mop;
import java.util.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect Collections_UnnecessaryNewSetFromMapMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Collections_UnnecessaryNewSetFromMapMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Collections_UnnecessaryNewSetFromMap_MOPLock = new ReentrantLock();
	static Condition Collections_UnnecessaryNewSetFromMap_MOPLock_cond = Collections_UnnecessaryNewSetFromMap_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Collections_UnnecessaryNewSetFromMap_unnecessary() : (call(* Collections.newSetFromMap(Map)) && (args(HashMap) || args(TreeMap))) && MOP_CommonPointCut();
	before () : Collections_UnnecessaryNewSetFromMap_unnecessary() {
	}

}
