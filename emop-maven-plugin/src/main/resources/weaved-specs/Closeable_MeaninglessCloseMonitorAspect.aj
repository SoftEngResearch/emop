package mop;
import java.io.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect Closeable_MeaninglessCloseMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Closeable_MeaninglessCloseMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Closeable_MeaninglessClose_MOPLock = new ReentrantLock();
	static Condition Closeable_MeaninglessClose_MOPLock_cond = Closeable_MeaninglessClose_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Closeable_MeaninglessClose_close() : (call(* Closeable+.close()) && (target(ByteArrayInputStream) || target(ByteArrayOutputStream) || target(CharArrayWriter) || target(StringWriter))) && MOP_CommonPointCut();
	before () : Closeable_MeaninglessClose_close() {
	}

}
