package mop;
import java.util.*;
import java.lang.reflect.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect Set_ItselfAsElementMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Set_ItselfAsElementMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Set_ItselfAsElement_MOPLock = new ReentrantLock();
	static Condition Set_ItselfAsElement_MOPLock_cond = Set_ItselfAsElement_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Set_ItselfAsElement_addall(Set s, Collection src) : (call(* Set+.addAll(Collection)) && target(s) && args(src)) && MOP_CommonPointCut();
	before (Set s, Collection src) : Set_ItselfAsElement_addall(s, src) {
	}

	pointcut Set_ItselfAsElement_add(Set s, Object elem) : (call(* Set+.add(Object)) && target(s) && args(elem)) && MOP_CommonPointCut();
	before (Set s, Object elem) : Set_ItselfAsElement_add(s, elem) {
	}

}
