package mop;
import java.util.*;
import java.lang.reflect.*;
import org.aspectj.lang.Signature;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect Random_OverrideNextMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Random_OverrideNextMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Random_OverrideNext_MOPLock = new ReentrantLock();
	static Condition Random_OverrideNext_MOPLock_cond = Random_OverrideNext_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Random_OverrideNext_staticinit() : (staticinitialization(Random+)) && MOP_CommonPointCut();
	after () : Random_OverrideNext_staticinit() {
	}

}
