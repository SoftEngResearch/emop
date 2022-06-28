package mop;
import java.io.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect Console_CloseWriterMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Console_CloseWriterMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Console_CloseWriter_MOPLock = new ReentrantLock();
	static Condition Console_CloseWriter_MOPLock_cond = Console_CloseWriter_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Console_CloseWriter_close(Writer w) : (call(* Writer+.close(..)) && target(w)) && MOP_CommonPointCut();
	before (Writer w) : Console_CloseWriter_close(w) {
	}

	pointcut Console_CloseWriter_getwriter() : (call(Writer+ Console+.writer())) && MOP_CommonPointCut();
	after () returning (Writer w) : Console_CloseWriter_getwriter() {
	}

}
