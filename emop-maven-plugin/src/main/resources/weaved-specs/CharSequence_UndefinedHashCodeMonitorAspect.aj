package mop;
import java.io.*;
import java.lang.*;
import java.nio.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect CharSequence_UndefinedHashCodeMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public CharSequence_UndefinedHashCodeMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock CharSequence_UndefinedHashCode_MOPLock = new ReentrantLock();
	static Condition CharSequence_UndefinedHashCode_MOPLock_cond = CharSequence_UndefinedHashCode_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut CharSequence_UndefinedHashCode_hashCode() : (call(* CharSequence+.hashCode(..)) && !target(String) && !target(CharBuffer)) && MOP_CommonPointCut();
	before () : CharSequence_UndefinedHashCode_hashCode() {
	}

	pointcut CharSequence_UndefinedHashCode_equals() : (call(* CharSequence+.equals(..)) && !target(String) && !target(CharBuffer)) && MOP_CommonPointCut();
	before () : CharSequence_UndefinedHashCode_equals() {
	}

}
