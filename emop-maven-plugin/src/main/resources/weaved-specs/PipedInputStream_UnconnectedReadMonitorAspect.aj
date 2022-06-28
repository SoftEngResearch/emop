package mop;
import java.io.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect PipedInputStream_UnconnectedReadMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public PipedInputStream_UnconnectedReadMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock PipedInputStream_UnconnectedRead_MOPLock = new ReentrantLock();
	static Condition PipedInputStream_UnconnectedRead_MOPLock_cond = PipedInputStream_UnconnectedRead_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut PipedInputStream_UnconnectedRead_read(PipedInputStream i) : ((call(* PipedInputStream+.read(..)) || call(* PipedInputStream+.receive(..)) || call(* PipedInputStream+.available(..))) && target(i)) && MOP_CommonPointCut();
	before (PipedInputStream i) : PipedInputStream_UnconnectedRead_read(i) {
	}

	pointcut PipedInputStream_UnconnectedRead_connect2(PipedInputStream i) : (call(* PipedInputStream+.connect(PipedOutputStream+)) && target(i)) && MOP_CommonPointCut();
	before (PipedInputStream i) : PipedInputStream_UnconnectedRead_connect2(i) {
	}

	pointcut PipedInputStream_UnconnectedRead_connect1(PipedInputStream i) : (call(* PipedOutputStream+.connect(PipedInputStream+)) && args(i)) && MOP_CommonPointCut();
	before (PipedInputStream i) : PipedInputStream_UnconnectedRead_connect1(i) {
	}

	pointcut PipedInputStream_UnconnectedRead_create_io(PipedInputStream i) : (call(PipedOutputStream+.new(PipedInputStream+)) && args(i)) && MOP_CommonPointCut();
	before (PipedInputStream i) : PipedInputStream_UnconnectedRead_create_io(i) {
	}

	pointcut PipedInputStream_UnconnectedRead_create() : (call(PipedInputStream+.new())) && MOP_CommonPointCut();
	after () returning (PipedInputStream i) : PipedInputStream_UnconnectedRead_create() {
	}

	pointcut PipedInputStream_UnconnectedRead_create_oi() : (call(PipedInputStream+.new(PipedOutputStream+))) && MOP_CommonPointCut();
	after () returning (PipedInputStream i) : PipedInputStream_UnconnectedRead_create_oi() {
	}

}
