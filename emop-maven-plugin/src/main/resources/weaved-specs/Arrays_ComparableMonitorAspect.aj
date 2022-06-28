package mop;
import java.util.*;
import java.lang.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect Arrays_ComparableMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Arrays_ComparableMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Arrays_Comparable_MOPLock = new ReentrantLock();
	static Condition Arrays_Comparable_MOPLock_cond = Arrays_Comparable_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Arrays_Comparable_invalid_sort(Object[] arr) : (target(Arrays) && (call(void Arrays.sort(Object[])) || call(void Arrays.sort(Object[], ..))) && args(arr, ..)) && MOP_CommonPointCut();
	before (Object[] arr) : Arrays_Comparable_invalid_sort(arr) {
	}

}
