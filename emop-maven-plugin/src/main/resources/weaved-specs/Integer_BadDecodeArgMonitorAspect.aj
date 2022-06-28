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

public aspect Integer_BadDecodeArgMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Integer_BadDecodeArgMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Integer_BadDecodeArg_MOPLock = new ReentrantLock();
	static Condition Integer_BadDecodeArg_MOPLock_cond = Integer_BadDecodeArg_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Integer_BadDecodeArg_decode(Integer i, String nm) : (call(* Integer.decode(String)) && args(nm) && target(i)) && MOP_CommonPointCut();
	before (Integer i, String nm) : Integer_BadDecodeArg_decode(i, nm) {
	}

}
