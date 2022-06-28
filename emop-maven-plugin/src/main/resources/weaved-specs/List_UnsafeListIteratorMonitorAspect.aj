package mop;
import java.util.*;
import java.lang.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect List_UnsafeListIteratorMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public List_UnsafeListIteratorMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock List_UnsafeListIterator_MOPLock = new ReentrantLock();
	static Condition List_UnsafeListIterator_MOPLock_cond = List_UnsafeListIterator_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut List_UnsafeListIterator_useiter(ListIterator i) : ((call(* Iterator+.hasNext(..)) || call(* ListIterator+.hasPrevious(..)) || call(* Iterator+.next(..)) || call(* ListIterator+.previous(..)) || call(* ListIterator+.nextIndex(..)) || call(* ListIterator+.previousIndex(..))) && target(i)) && MOP_CommonPointCut();
	before (ListIterator i) : List_UnsafeListIterator_useiter(i) {
	}

	pointcut List_UnsafeListIterator_modify(List l) : ((call(* Collection+.add*(..)) || call(* Collection+.clear(..)) || call(* Collection+.remove*(..)) || call(* Collection+.retain*(..))) && target(l)) && MOP_CommonPointCut();
	before (List l) : List_UnsafeListIterator_modify(l) {
	}

	pointcut List_UnsafeListIterator_create(List l) : (call(ListIterator List+.listIterator()) && target(l)) && MOP_CommonPointCut();
	after (List l) returning (ListIterator i) : List_UnsafeListIterator_create(l) {
	}

}
