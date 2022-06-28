package mop;
import java.util.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect Arrays_DeepHashCodeMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Arrays_DeepHashCodeMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Arrays_DeepHashCode_MOPLock = new ReentrantLock();
	static Condition Arrays_DeepHashCode_MOPLock_cond = Arrays_DeepHashCode_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Arrays_DeepHashCode_invalid_deephashcode(Object[] arr) : (call(int Arrays.deepHashCode(Object[])) && args(arr)) && MOP_CommonPointCut();
	before (Object[] arr) : Arrays_DeepHashCode_invalid_deephashcode(arr) {
	}

}
