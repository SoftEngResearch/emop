package mop;
import java.io.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect Reader_ReadAheadLimitMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Reader_ReadAheadLimitMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Reader_ReadAheadLimit_MOPLock = new ReentrantLock();
	static Condition Reader_ReadAheadLimit_MOPLock_cond = Reader_ReadAheadLimit_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Reader_ReadAheadLimit_badreset(Reader r) : (call(* Reader+.reset(..)) && target(r) && if(r instanceof BufferedReader || r instanceof LineNumberReader)) && MOP_CommonPointCut();
	before (Reader r) : Reader_ReadAheadLimit_badreset(r) {
		//Reader_ReadAheadLimit_goodreset
		//Reader_ReadAheadLimit_badreset
	}

	pointcut Reader_ReadAheadLimit_mark(Reader r, int l) : (call(* Reader+.mark(int)) && target(r) && args(l) && if(r instanceof BufferedReader || r instanceof LineNumberReader)) && MOP_CommonPointCut();
	before (Reader r, int l) : Reader_ReadAheadLimit_mark(r, l) {
	}

	pointcut Reader_ReadAheadLimit_read1(Reader r) : (call(* Reader+.read()) && target(r) && if(r instanceof BufferedReader || r instanceof LineNumberReader)) && MOP_CommonPointCut();
	after (Reader r) returning (int n) : Reader_ReadAheadLimit_read1(r) {
	}

	pointcut Reader_ReadAheadLimit_readn(Reader r) : (call(* Reader+.read(char[], ..)) && target(r) && if(r instanceof BufferedReader || r instanceof LineNumberReader)) && MOP_CommonPointCut();
	after (Reader r) returning (int n) : Reader_ReadAheadLimit_readn(r) {
	}

}
