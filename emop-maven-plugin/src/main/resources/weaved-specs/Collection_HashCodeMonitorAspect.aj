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

public aspect Collection_HashCodeMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Collection_HashCodeMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Collection_HashCode_MOPLock = new ReentrantLock();
	static Condition Collection_HashCode_MOPLock_cond = Collection_HashCode_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Collection_HashCode_staticinit() : (staticinitialization(Collection+)) && MOP_CommonPointCut();
	after () : Collection_HashCode_staticinit() {
	}

}
