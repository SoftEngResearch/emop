package mop;
import java.io.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect Writer_ManipulateAfterCloseMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Writer_ManipulateAfterCloseMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Writer_ManipulateAfterClose_MOPLock = new ReentrantLock();
	static Condition Writer_ManipulateAfterClose_MOPLock_cond = Writer_ManipulateAfterClose_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Writer_ManipulateAfterClose_close(Writer w) : (call(* Writer+.close(..)) && target(w) && !target(CharArrayWriter) && !target(StringWriter)) && MOP_CommonPointCut();
	before (Writer w) : Writer_ManipulateAfterClose_close(w) {
	}

	pointcut Writer_ManipulateAfterClose_manipulate(Writer w) : ((call(* Writer+.write*(..)) || call(* Writer+.flush(..))) && target(w) && !target(CharArrayWriter) && !target(StringWriter)) && MOP_CommonPointCut();
	before (Writer w) : Writer_ManipulateAfterClose_manipulate(w) {
	}

}
