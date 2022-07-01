package mop;
import java.net.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect DatagramSocket_SoTimeoutMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public DatagramSocket_SoTimeoutMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock DatagramSocket_SoTimeout_MOPLock = new ReentrantLock();
	static Condition DatagramSocket_SoTimeout_MOPLock_cond = DatagramSocket_SoTimeout_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut DatagramSocket_SoTimeout_settimeout(int timeout) : (call(void DatagramSocket.setSoTimeout(int)) && args(timeout)) && MOP_CommonPointCut();
	before (int timeout) : DatagramSocket_SoTimeout_settimeout(timeout) {
	}

}
