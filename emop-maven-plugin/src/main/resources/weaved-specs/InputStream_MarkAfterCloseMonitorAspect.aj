package mop;
import java.io.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect InputStream_MarkAfterCloseMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public InputStream_MarkAfterCloseMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock InputStream_MarkAfterClose_MOPLock = new ReentrantLock();
	static Condition InputStream_MarkAfterClose_MOPLock_cond = InputStream_MarkAfterClose_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut InputStream_MarkAfterClose_close(InputStream i) : (call(* InputStream+.close(..)) && target(i)) && MOP_CommonPointCut();
	before (InputStream i) : InputStream_MarkAfterClose_close(i) {
	}

	pointcut InputStream_MarkAfterClose_mark(InputStream i) : (call(* InputStream+.mark(..)) && target(i)) && MOP_CommonPointCut();
	before (InputStream i) : InputStream_MarkAfterClose_mark(i) {
	}

}
