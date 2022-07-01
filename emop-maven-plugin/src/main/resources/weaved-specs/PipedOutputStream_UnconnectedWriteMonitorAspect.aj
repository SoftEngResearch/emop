package mop;
import java.io.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect PipedOutputStream_UnconnectedWriteMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public PipedOutputStream_UnconnectedWriteMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock PipedOutputStream_UnconnectedWrite_MOPLock = new ReentrantLock();
	static Condition PipedOutputStream_UnconnectedWrite_MOPLock_cond = PipedOutputStream_UnconnectedWrite_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut PipedOutputStream_UnconnectedWrite_write(PipedOutputStream o) : (call(* PipedOutputStream+.write(..)) && target(o)) && MOP_CommonPointCut();
	before (PipedOutputStream o) : PipedOutputStream_UnconnectedWrite_write(o) {
	}

	pointcut PipedOutputStream_UnconnectedWrite_connect2(PipedOutputStream o) : (call(* PipedOutputStream+.connect(PipedInputStream+)) && target(o)) && MOP_CommonPointCut();
	before (PipedOutputStream o) : PipedOutputStream_UnconnectedWrite_connect2(o) {
	}

	pointcut PipedOutputStream_UnconnectedWrite_connect1(PipedOutputStream o) : (call(* PipedInputStream+.connect(PipedOutputStream+)) && args(o)) && MOP_CommonPointCut();
	before (PipedOutputStream o) : PipedOutputStream_UnconnectedWrite_connect1(o) {
	}

	pointcut PipedOutputStream_UnconnectedWrite_create_oi(PipedOutputStream o) : (call(PipedInputStream+.new(PipedOutputStream+)) && args(o)) && MOP_CommonPointCut();
	before (PipedOutputStream o) : PipedOutputStream_UnconnectedWrite_create_oi(o) {
	}

	pointcut PipedOutputStream_UnconnectedWrite_create() : (call(PipedOutputStream+.new())) && MOP_CommonPointCut();
	after () returning (PipedOutputStream o) : PipedOutputStream_UnconnectedWrite_create() {
	}

	pointcut PipedOutputStream_UnconnectedWrite_create_io() : (call(PipedOutputStream+.new(PipedInputStream+))) && MOP_CommonPointCut();
	after () returning (PipedOutputStream o) : PipedOutputStream_UnconnectedWrite_create_io() {
	}

}
