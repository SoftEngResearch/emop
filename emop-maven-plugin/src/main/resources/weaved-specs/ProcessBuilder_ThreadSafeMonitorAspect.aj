package mop;
import java.io.*;
import java.lang.*;
import java.util.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect ProcessBuilder_ThreadSafeMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public ProcessBuilder_ThreadSafeMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock ProcessBuilder_ThreadSafe_MOPLock = new ReentrantLock();
	static Condition ProcessBuilder_ThreadSafe_MOPLock_cond = ProcessBuilder_ThreadSafe_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut ProcessBuilder_ThreadSafe_safe_oper(ProcessBuilder p) : (call(* ProcessBuilder.*(..)) && target(p)) && MOP_CommonPointCut();
	before (ProcessBuilder p) : ProcessBuilder_ThreadSafe_safe_oper(p) {
		Thread t = Thread.currentThread();
		//ProcessBuilder_ThreadSafe_unsafe_oper
		//ProcessBuilder_ThreadSafe_safe_oper
	}

}
