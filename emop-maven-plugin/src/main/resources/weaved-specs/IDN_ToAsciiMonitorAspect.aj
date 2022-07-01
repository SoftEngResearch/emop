package mop;
import java.net.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect IDN_ToAsciiMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public IDN_ToAsciiMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock IDN_ToAscii_MOPLock = new ReentrantLock();
	static Condition IDN_ToAscii_MOPLock_cond = IDN_ToAscii_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut IDN_ToAscii_toascii(String input) : ((call(* IDN.toASCII(String)) || call(* IDN.toASCII(String, int))) && args(input, ..)) && MOP_CommonPointCut();
	before (String input) : IDN_ToAscii_toascii(input) {
	}

}
