package mop;
import java.util.*;
import java.lang.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect NavigableMap_UnsafeIteratorMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public NavigableMap_UnsafeIteratorMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock NavigableMap_UnsafeIterator_MOPLock = new ReentrantLock();
	static Condition NavigableMap_UnsafeIterator_MOPLock_cond = NavigableMap_UnsafeIterator_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut NavigableMap_UnsafeIterator_useiter(Iterator i) : ((call(* Iterator.hasNext(..)) || call(* Iterator.next(..))) && target(i)) && MOP_CommonPointCut();
	before (Iterator i) : NavigableMap_UnsafeIterator_useiter(i) {
	}

	pointcut NavigableMap_UnsafeIterator_modifySet(Set s) : ((call(* Collection+.add(..)) || call(* Collection+.addAll(..))) && target(s)) && MOP_CommonPointCut();
	before (Set s) : NavigableMap_UnsafeIterator_modifySet(s) {
	}

	pointcut NavigableMap_UnsafeIterator_modifyMap(NavigableMap m) : ((call(* Map+.clear*(..)) || call(* Map+.put*(..)) || call(* Map+.remove*(..))) && target(m)) && MOP_CommonPointCut();
	before (NavigableMap m) : NavigableMap_UnsafeIterator_modifyMap(m) {
	}

	pointcut NavigableMap_UnsafeIterator_getset(NavigableMap m) : ((call(Set NavigableMap+.navigableKeySet()) || call(Set NavigableMap+.descendingKeySet())) && target(m)) && MOP_CommonPointCut();
	after (NavigableMap m) returning (Set s) : NavigableMap_UnsafeIterator_getset(m) {
	}

	pointcut NavigableMap_UnsafeIterator_getiter(Set s) : (call(Iterator Iterable+.iterator()) && target(s)) && MOP_CommonPointCut();
	after (Set s) returning (Iterator i) : NavigableMap_UnsafeIterator_getiter(s) {
	}

}
