package mop;
import java.net.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.io.InputStream;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect Socket_CloseInputMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Socket_CloseInputMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Socket_CloseInput_MOPLock = new ReentrantLock();
	static Condition Socket_CloseInput_MOPLock_cond = Socket_CloseInput_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Socket_CloseInput_use(InputStream input) : (call(* InputStream+.*(..)) && target(input)) && MOP_CommonPointCut();
	before (InputStream input) : Socket_CloseInput_use(input) {
	}

	pointcut Socket_CloseInput_close(Socket sock) : (call(* Socket+.close(..)) && target(sock)) && MOP_CommonPointCut();
	before (Socket sock) : Socket_CloseInput_close(sock) {
	}

	pointcut Socket_CloseInput_getinput(Socket sock) : (call(InputStream Socket+.getInputStream()) && target(sock)) && MOP_CommonPointCut();
	after (Socket sock) returning (InputStream input) : Socket_CloseInput_getinput(sock) {
	}

}
