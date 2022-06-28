package mop;
import java.io.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect Reader_UnmarkedResetMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Reader_UnmarkedResetMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Reader_UnmarkedReset_MOPLock = new ReentrantLock();
	static Condition Reader_UnmarkedReset_MOPLock_cond = Reader_UnmarkedReset_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Reader_UnmarkedReset_reset(Reader r) : (call(* Reader+.reset(..)) && target(r) && if(r instanceof BufferedReader || r instanceof LineNumberReader)) && MOP_CommonPointCut();
	before (Reader r) : Reader_UnmarkedReset_reset(r) {
	}

	pointcut Reader_UnmarkedReset_mark(Reader r) : (call(* Reader+.mark(..)) && target(r) && if(r instanceof BufferedReader || r instanceof LineNumberReader)) && MOP_CommonPointCut();
	before (Reader r) : Reader_UnmarkedReset_mark(r) {
	}

}
