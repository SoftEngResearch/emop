package mop;
import java.util.*;
import java.lang.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect ArrayDeque_UnsafeIteratorMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public ArrayDeque_UnsafeIteratorMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock ArrayDeque_UnsafeIterator_MOPLock = new ReentrantLock();
	static Condition ArrayDeque_UnsafeIterator_MOPLock_cond = ArrayDeque_UnsafeIterator_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut ArrayDeque_UnsafeIterator_useiter(Iterator i) : (call(* Iterator.*(..)) && target(i)) && MOP_CommonPointCut();
	before (Iterator i) : ArrayDeque_UnsafeIterator_useiter(i) {
	}

	pointcut ArrayDeque_UnsafeIterator_modify(ArrayDeque q) : (target(ArrayDeque) && (call(* Collection+.add*(..)) || call(* Collection+.clear(..)) || call(* Collection+.offer*(..)) || call(* Collection+.pop(..)) || call(* Collection+.push(..)) || call(* Collection+.remove*(..)) || call(* Collection+.retain*(..))) && target(q)) && MOP_CommonPointCut();
	before (ArrayDeque q) : ArrayDeque_UnsafeIterator_modify(q) {
	}

	pointcut ArrayDeque_UnsafeIterator_create(ArrayDeque q) : (target(ArrayDeque) && (call(Iterator Iterable+.iterator()) || call(Iterator Deque+.descendingIterator())) && target(q)) && MOP_CommonPointCut();
	after (ArrayDeque q) returning (Iterator i) : ArrayDeque_UnsafeIterator_create(q) {
	}

}
