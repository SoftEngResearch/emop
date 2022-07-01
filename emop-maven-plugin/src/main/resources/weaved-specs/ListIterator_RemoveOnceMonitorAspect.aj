package mop;
import java.util.*;
import java.lang.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect ListIterator_RemoveOnceMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public ListIterator_RemoveOnceMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock ListIterator_RemoveOnce_MOPLock = new ReentrantLock();
	static Condition ListIterator_RemoveOnce_MOPLock_cond = ListIterator_RemoveOnce_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut ListIterator_RemoveOnce_previous(ListIterator i) : (call(* ListIterator+.previous()) && target(i)) && MOP_CommonPointCut();
	before (ListIterator i) : ListIterator_RemoveOnce_previous(i) {
	}

	pointcut ListIterator_RemoveOnce_next(ListIterator i) : (call(* Iterator+.next()) && target(i)) && MOP_CommonPointCut();
	before (ListIterator i) : ListIterator_RemoveOnce_next(i) {
	}

	pointcut ListIterator_RemoveOnce_remove(ListIterator i) : (call(void Iterator+.remove()) && target(i)) && MOP_CommonPointCut();
	before (ListIterator i) : ListIterator_RemoveOnce_remove(i) {
	}

}
