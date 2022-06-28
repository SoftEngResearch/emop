package mop;
import java.io.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect ByteArrayOutputStream_FlushBeforeRetrieveMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public ByteArrayOutputStream_FlushBeforeRetrieveMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock ByteArrayOutputStream_FlushBeforeRetrieve_MOPLock = new ReentrantLock();
	static Condition ByteArrayOutputStream_FlushBeforeRetrieve_MOPLock_cond = ByteArrayOutputStream_FlushBeforeRetrieve_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut ByteArrayOutputStream_FlushBeforeRetrieve_tostring(ByteArrayOutputStream b) : (call(* ByteArrayOutputStream+.toString(..)) && target(b)) && MOP_CommonPointCut();
	before (ByteArrayOutputStream b) : ByteArrayOutputStream_FlushBeforeRetrieve_tostring(b) {
	}

	pointcut ByteArrayOutputStream_FlushBeforeRetrieve_tobytearray(ByteArrayOutputStream b) : (call(* ByteArrayOutputStream+.toByteArray(..)) && target(b)) && MOP_CommonPointCut();
	before (ByteArrayOutputStream b) : ByteArrayOutputStream_FlushBeforeRetrieve_tobytearray(b) {
	}

	pointcut ByteArrayOutputStream_FlushBeforeRetrieve_close(OutputStream o) : (call(* OutputStream+.close(..)) && target(o)) && MOP_CommonPointCut();
	before (OutputStream o) : ByteArrayOutputStream_FlushBeforeRetrieve_close(o) {
	}

	pointcut ByteArrayOutputStream_FlushBeforeRetrieve_flush(OutputStream o) : (call(* OutputStream+.flush(..)) && target(o)) && MOP_CommonPointCut();
	before (OutputStream o) : ByteArrayOutputStream_FlushBeforeRetrieve_flush(o) {
	}

	pointcut ByteArrayOutputStream_FlushBeforeRetrieve_write(OutputStream o) : (call(* OutputStream+.write*(..)) && target(o)) && MOP_CommonPointCut();
	before (OutputStream o) : ByteArrayOutputStream_FlushBeforeRetrieve_write(o) {
	}

	pointcut ByteArrayOutputStream_FlushBeforeRetrieve_outputstreaminit(ByteArrayOutputStream b) : (call(OutputStream+.new(..)) && args(b, ..)) && MOP_CommonPointCut();
	after (ByteArrayOutputStream b) returning (OutputStream o) : ByteArrayOutputStream_FlushBeforeRetrieve_outputstreaminit(b) {
	}

}
