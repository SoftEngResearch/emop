package mop;
import java.io.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect ObjectStreamClass_InitializeMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public ObjectStreamClass_InitializeMonitorAspect(){
		Runtime.getRuntime().addShutdownHook(new ObjectStreamClass_Initialize_DummyHookThread());
	}

	// Declarations for the Lock
	static ReentrantLock ObjectStreamClass_Initialize_MOPLock = new ReentrantLock();
	static Condition ObjectStreamClass_Initialize_MOPLock_cond = ObjectStreamClass_Initialize_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut ObjectStreamClass_Initialize_init(ObjectStreamClass c) : ((call(* ObjectStreamClass+.initProxy(..)) || call(* ObjectStreamClass+.initNonProxy(..)) || call(* ObjectStreamClass+.readNonProxy(..))) && target(c)) && MOP_CommonPointCut();
	before (ObjectStreamClass c) : ObjectStreamClass_Initialize_init(c) {
	}

	pointcut ObjectStreamClass_Initialize_create() : (call(ObjectStreamClass+.new())) && MOP_CommonPointCut();
	after () returning (ObjectStreamClass c) : ObjectStreamClass_Initialize_create() {
	}

	class ObjectStreamClass_Initialize_DummyHookThread extends Thread {
		public void run(){
		}
	}
}
