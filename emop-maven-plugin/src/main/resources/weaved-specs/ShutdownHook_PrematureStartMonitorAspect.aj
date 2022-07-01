package mop;
import java.lang.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect ShutdownHook_PrematureStartMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public ShutdownHook_PrematureStartMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock ShutdownHook_PrematureStart_MOPLock = new ReentrantLock();
	static Condition ShutdownHook_PrematureStart_MOPLock_cond = ShutdownHook_PrematureStart_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut ShutdownHook_PrematureStart_userstart(Thread t) : (call(* Thread+.start(..)) && target(t)) && MOP_CommonPointCut();
	before (Thread t) : ShutdownHook_PrematureStart_userstart(t) {
	}

	pointcut ShutdownHook_PrematureStart_unregister(Thread t) : (call(* Runtime+.removeShutdownHook(..)) && args(t)) && MOP_CommonPointCut();
	before (Thread t) : ShutdownHook_PrematureStart_unregister(t) {
	}

	pointcut ShutdownHook_PrematureStart_good_register(Thread t) : (call(* Runtime+.addShutdownHook(..)) && args(t)) && MOP_CommonPointCut();
	before (Thread t) : ShutdownHook_PrematureStart_good_register(t) {
		//ShutdownHook_PrematureStart_bad_register
		//ShutdownHook_PrematureStart_good_register
	}

}
