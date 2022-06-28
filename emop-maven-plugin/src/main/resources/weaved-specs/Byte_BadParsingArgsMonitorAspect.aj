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

public aspect Byte_BadParsingArgsMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Byte_BadParsingArgsMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Byte_BadParsingArgs_MOPLock = new ReentrantLock();
	static Condition Byte_BadParsingArgs_MOPLock_cond = Byte_BadParsingArgs_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Byte_BadParsingArgs_bad_arg2(String s) : (call(* Byte.parseByte(String)) && args(s)) && MOP_CommonPointCut();
	before (String s) : Byte_BadParsingArgs_bad_arg2(s) {
	}

	pointcut Byte_BadParsingArgs_bad_arg(String s, int radix) : (call(* Byte.parseByte(String, int)) && args(s, radix)) && MOP_CommonPointCut();
	before (String s, int radix) : Byte_BadParsingArgs_bad_arg(s, radix) {
	}

}
