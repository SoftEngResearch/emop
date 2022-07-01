package mop;
import java.io.*;
import java.lang.*;
import java.util.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect System_WrongKeyOrValueMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public System_WrongKeyOrValueMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock System_WrongKeyOrValue_MOPLock = new ReentrantLock();
	static Condition System_WrongKeyOrValue_MOPLock_cond = System_WrongKeyOrValue_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut System_WrongKeyOrValue_nullQuery(Map map, Object o) : ((call(* Map.containsKey(..)) || call(* Map.containsValue(..)) || call(* Map.get(..)) || call(* Map.remove(..))) && target(map) && args(o)) && MOP_CommonPointCut();
	before (Map map, Object o) : System_WrongKeyOrValue_nullQuery(map, o) {
		//System_WrongKeyOrValue_notStringQuery
		//System_WrongKeyOrValue_nullQuery
	}

	pointcut System_WrongKeyOrValue_nullPut_4(Map map, Map map2) : (call(* Map.putAll(Map)) && args(map2) && target(map)) && MOP_CommonPointCut();
	before (Map map, Map map2) : System_WrongKeyOrValue_nullPut_4(map, map2) {
	}

	pointcut System_WrongKeyOrValue_nullPut_3(Map map, Object key, Object value) : (call(* Map.put(..)) && args(key, value) && target(map)) && MOP_CommonPointCut();
	before (Map map, Object key, Object value) : System_WrongKeyOrValue_nullPut_3(map, key, value) {
	}

	pointcut System_WrongKeyOrValue_createMap() : (call(Map System.getenv())) && MOP_CommonPointCut();
	after () returning (Map map) : System_WrongKeyOrValue_createMap() {
	}

}
