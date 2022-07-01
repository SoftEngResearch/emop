package mop;
import java.lang.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect StrictMath_ContendedRandomMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public StrictMath_ContendedRandomMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock StrictMath_ContendedRandom_MOPLock = new ReentrantLock();
	static Condition StrictMath_ContendedRandom_MOPLock_cond = StrictMath_ContendedRandom_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut StrictMath_ContendedRandom_onethread_use() : (call(* StrictMath.random(..))) && MOP_CommonPointCut();
	before () : StrictMath_ContendedRandom_onethread_use() {
		Thread t = Thread.currentThread();
		//StrictMath_ContendedRandom_otherthread_use
		//StrictMath_ContendedRandom_onethread_use
	}

}
