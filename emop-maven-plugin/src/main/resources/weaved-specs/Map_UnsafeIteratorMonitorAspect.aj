package mop;
import java.util.*;
import java.lang.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect Map_UnsafeIteratorMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Map_UnsafeIteratorMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Map_UnsafeIterator_MOPLock = new ReentrantLock();
	static Condition Map_UnsafeIterator_MOPLock_cond = Map_UnsafeIterator_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Map_UnsafeIterator_useiter(Iterator i) : ((call(* Iterator.hasNext(..)) || call(* Iterator.next(..))) && target(i)) && MOP_CommonPointCut();
	before (Iterator i) : Map_UnsafeIterator_useiter(i) {
	}

	pointcut Map_UnsafeIterator_modifyCol(Collection c) : ((call(* Collection+.clear(..)) || call(* Collection+.offer*(..)) || call(* Collection+.pop(..)) || call(* Collection+.push(..)) || call(* Collection+.remove*(..)) || call(* Collection+.retain*(..))) && target(c)) && MOP_CommonPointCut();
	before (Collection c) : Map_UnsafeIterator_modifyCol(c) {
	}

	pointcut Map_UnsafeIterator_modifyMap(Map m) : ((call(* Map+.clear*(..)) || call(* Map+.put*(..)) || call(* Map+.remove(..))) && target(m)) && MOP_CommonPointCut();
	before (Map m) : Map_UnsafeIterator_modifyMap(m) {
	}

	pointcut Map_UnsafeIterator_getset(Map m) : ((call(Set Map+.keySet()) || call(Set Map+.entrySet()) || call(Collection Map+.values())) && target(m)) && MOP_CommonPointCut();
	after (Map m) returning (Collection c) : Map_UnsafeIterator_getset(m) {
	}

	pointcut Map_UnsafeIterator_getiter(Collection c) : (call(Iterator Iterable+.iterator()) && target(c)) && MOP_CommonPointCut();
	after (Collection c) returning (Iterator i) : Map_UnsafeIterator_getiter(c) {
	}

}
