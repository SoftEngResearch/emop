package mop;
import java.io.*;
import java.lang.reflect.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect Serializable_NoArgConstructorMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Serializable_NoArgConstructorMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Serializable_NoArgConstructor_MOPLock = new ReentrantLock();
	static Condition Serializable_NoArgConstructor_MOPLock_cond = Serializable_NoArgConstructor_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Serializable_NoArgConstructor_staticinit() : (staticinitialization(Serializable+)) && MOP_CommonPointCut();
	after () : Serializable_NoArgConstructor_staticinit() {
	}

}
