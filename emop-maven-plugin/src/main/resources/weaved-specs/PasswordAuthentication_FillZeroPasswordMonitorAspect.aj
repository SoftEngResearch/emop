package mop;
import java.net.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect PasswordAuthentication_FillZeroPasswordMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public PasswordAuthentication_FillZeroPasswordMonitorAspect(){
		Runtime.getRuntime().addShutdownHook(new PasswordAuthentication_FillZeroPassword_DummyHookThread());
	}

	// Declarations for the Lock
	static ReentrantLock PasswordAuthentication_FillZeroPassword_MOPLock = new ReentrantLock();
	static Condition PasswordAuthentication_FillZeroPassword_MOPLock_cond = PasswordAuthentication_FillZeroPassword_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut PasswordAuthentication_FillZeroPassword_obliterate(Object pwd) : (call(* Arrays.fill(char[], char)) && args(pwd, ..)) && MOP_CommonPointCut();
	before (Object pwd) : PasswordAuthentication_FillZeroPassword_obliterate(pwd) {
	}

	pointcut PasswordAuthentication_FillZeroPassword_read() : (call(char[] PasswordAuthentication+.getPassword(..))) && MOP_CommonPointCut();
	after () returning (Object pwd) : PasswordAuthentication_FillZeroPassword_read() {
	}

	class PasswordAuthentication_FillZeroPassword_DummyHookThread extends Thread {
		public void run(){
		}
	}
}
