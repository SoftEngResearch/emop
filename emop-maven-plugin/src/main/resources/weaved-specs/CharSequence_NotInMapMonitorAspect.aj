package mop;
import java.io.*;
import java.lang.*;
import java.nio.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect CharSequence_NotInMapMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public CharSequence_NotInMapMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock CharSequence_NotInMap_MOPLock = new ReentrantLock();
	static Condition CharSequence_NotInMap_MOPLock_cond = CharSequence_NotInMap_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut CharSequence_NotInMap_map_putall(Map map, Map m) : (call(* Map+.putAll(Map)) && args(m) && target(map)) && MOP_CommonPointCut();
	before (Map map, Map m) : CharSequence_NotInMap_map_putall(map, m) {
	}

	pointcut CharSequence_NotInMap_map_put(Map map) : (call(* Map+.put(..)) && target(map) && args(CharSequence, ..) && !args(String, ..) && !args(CharBuffer, ..)) && MOP_CommonPointCut();
	before (Map map) : CharSequence_NotInMap_map_put(map) {
	}

}
