package mop;
import java.io.*;
import java.lang.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect Appendable_ThreadSafeMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Appendable_ThreadSafeMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Appendable_ThreadSafe_MOPLock = new ReentrantLock();
	static Condition Appendable_ThreadSafe_MOPLock_cond = Appendable_ThreadSafe_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Appendable_ThreadSafe_safe_append(Appendable a) : (call(* Appendable+.append(..)) && target(a) && !target(StringBuffer)) && MOP_CommonPointCut();
	before (Appendable a) : Appendable_ThreadSafe_safe_append(a) {
		Thread t = Thread.currentThread();
		//Appendable_ThreadSafe_unsafe_append
		//Appendable_ThreadSafe_safe_append
	}

}
