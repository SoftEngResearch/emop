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

public aspect Long_BadParsingArgsMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Long_BadParsingArgsMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Long_BadParsingArgs_MOPLock = new ReentrantLock();
	static Condition Long_BadParsingArgs_MOPLock_cond = Long_BadParsingArgs_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Long_BadParsingArgs_bad_arg2(String s) : (call(* Long.parseLong(String)) && args(s)) && MOP_CommonPointCut();
	before (String s) : Long_BadParsingArgs_bad_arg2(s) {
	}

	pointcut Long_BadParsingArgs_bad_arg(String s, int radix) : (call(* Long.parseLong(String, int)) && args(s, radix)) && MOP_CommonPointCut();
	before (String s, int radix) : Long_BadParsingArgs_bad_arg(s, radix) {
	}

}
