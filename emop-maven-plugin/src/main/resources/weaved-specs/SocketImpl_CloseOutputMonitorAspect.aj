package mop;
import java.net.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.io.OutputStream;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect SocketImpl_CloseOutputMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public SocketImpl_CloseOutputMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock SocketImpl_CloseOutput_MOPLock = new ReentrantLock();
	static Condition SocketImpl_CloseOutput_MOPLock_cond = SocketImpl_CloseOutput_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut SocketImpl_CloseOutput_use(OutputStream output) : (call(* OutputStream+.*(..)) && target(output)) && MOP_CommonPointCut();
	before (OutputStream output) : SocketImpl_CloseOutput_use(output) {
	}

	pointcut SocketImpl_CloseOutput_close(SocketImpl sock) : ((call(* SocketImpl+.close(..)) || call(* SocketImpl+.shutdownOutput(..))) && target(sock)) && MOP_CommonPointCut();
	before (SocketImpl sock) : SocketImpl_CloseOutput_close(sock) {
	}

	pointcut SocketImpl_CloseOutput_getoutput(SocketImpl sock) : (call(OutputStream SocketImpl+.getOutputStream()) && target(sock)) && MOP_CommonPointCut();
	after (SocketImpl sock) returning (OutputStream output) : SocketImpl_CloseOutput_getoutput(sock) {
	}

}
