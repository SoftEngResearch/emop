package mop;
import java.net.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect URLDecoder_DecodeUTF8MonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public URLDecoder_DecodeUTF8MonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock URLDecoder_DecodeUTF8_MOPLock = new ReentrantLock();
	static Condition URLDecoder_DecodeUTF8_MOPLock_cond = URLDecoder_DecodeUTF8_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut URLDecoder_DecodeUTF8_decode(String enc) : (call(* URLDecoder.decode(String, String)) && args(*, enc)) && MOP_CommonPointCut();
	before (String enc) : URLDecoder_DecodeUTF8_decode(enc) {
	}

}
