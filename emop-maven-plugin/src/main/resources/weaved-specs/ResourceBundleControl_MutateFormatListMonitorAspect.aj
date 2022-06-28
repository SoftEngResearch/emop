package mop;
import java.util.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect ResourceBundleControl_MutateFormatListMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public ResourceBundleControl_MutateFormatListMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock ResourceBundleControl_MutateFormatList_MOPLock = new ReentrantLock();
	static Condition ResourceBundleControl_MutateFormatList_MOPLock_cond = ResourceBundleControl_MutateFormatList_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut ResourceBundleControl_MutateFormatList_mutate(List l) : ((call(* Collection+.add*(..)) || call(* Collection+.clear(..)) || call(* Collection+.remove*(..)) || call(* Collection+.retain*(..))) && target(l)) && MOP_CommonPointCut();
	before (List l) : ResourceBundleControl_MutateFormatList_mutate(l) {
	}

	pointcut ResourceBundleControl_MutateFormatList_create() : (call(List ResourceBundle.Control.getFormats(..)) || call(List ResourceBundle.Control.getCandidateLocales(..))) && MOP_CommonPointCut();
	after () returning (List l) : ResourceBundleControl_MutateFormatList_create() {
	}

}
