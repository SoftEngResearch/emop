package mop;
import java.util.*;
import java.util.concurrent.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.locks.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect Deque_OfferRatherThanAddMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Deque_OfferRatherThanAddMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Deque_OfferRatherThanAdd_MOPLock = new ReentrantLock();
	static Condition Deque_OfferRatherThanAdd_MOPLock_cond = Deque_OfferRatherThanAdd_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Deque_OfferRatherThanAdd_add(Deque q) : ((call(* Deque+.addFirst(..)) || call(* Deque+.addLast(..)) || call(* Deque+.add(..)) || call(* Deque+.push(..))) && target(q)) && MOP_CommonPointCut();
	before (Deque q) : Deque_OfferRatherThanAdd_add(q) {
	}

	pointcut Deque_OfferRatherThanAdd_create() : (call(LinkedBlockingDeque+.new(int))) && MOP_CommonPointCut();
	after () returning (Deque q) : Deque_OfferRatherThanAdd_create() {
	}

}
