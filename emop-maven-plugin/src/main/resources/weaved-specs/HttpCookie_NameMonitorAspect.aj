package mop;
import java.net.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect HttpCookie_NameMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public HttpCookie_NameMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock HttpCookie_Name_MOPLock = new ReentrantLock();
	static Condition HttpCookie_Name_MOPLock_cond = HttpCookie_Name_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut HttpCookie_Name_construct(String name) : (call(HttpCookie.new(String, String)) && args(name, ..)) && MOP_CommonPointCut();
	before (String name) : HttpCookie_Name_construct(name) {
	}

}
