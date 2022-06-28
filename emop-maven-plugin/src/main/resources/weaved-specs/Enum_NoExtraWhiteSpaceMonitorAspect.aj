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

public aspect Enum_NoExtraWhiteSpaceMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Enum_NoExtraWhiteSpaceMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Enum_NoExtraWhiteSpace_MOPLock = new ReentrantLock();
	static Condition Enum_NoExtraWhiteSpace_MOPLock_cond = Enum_NoExtraWhiteSpace_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Enum_NoExtraWhiteSpace_valueOf(Class c, String name) : (call(* Enum+.valueOf(Class, String)) && args(c, name)) && MOP_CommonPointCut();
	before (Class c, String name) : Enum_NoExtraWhiteSpace_valueOf(c, name) {
	}

}
