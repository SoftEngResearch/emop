package mop;
import java.net.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect URL_SetURLStreamHandlerFactoryMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public URL_SetURLStreamHandlerFactoryMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock URL_SetURLStreamHandlerFactory_MOPLock = new ReentrantLock();
	static Condition URL_SetURLStreamHandlerFactory_MOPLock_cond = URL_SetURLStreamHandlerFactory_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut URL_SetURLStreamHandlerFactory_set() : (call(* URL.setURLStreamHandlerFactory(..))) && MOP_CommonPointCut();
	before () : URL_SetURLStreamHandlerFactory_set() {
	}

}
