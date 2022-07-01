package mop;
import java.io.*;
import java.util.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect Console_FillZeroPasswordMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Console_FillZeroPasswordMonitorAspect(){
		Runtime.getRuntime().addShutdownHook(new Console_FillZeroPassword_DummyHookThread());
	}

	// Declarations for the Lock
	static ReentrantLock Console_FillZeroPassword_MOPLock = new ReentrantLock();
	static Condition Console_FillZeroPassword_MOPLock_cond = Console_FillZeroPassword_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Console_FillZeroPassword_obliterate(Object pwd) : (call(* Arrays.fill(char[], char)) && args(pwd, ..)) && MOP_CommonPointCut();
	before (Object pwd) : Console_FillZeroPassword_obliterate(pwd) {
	}

	pointcut Console_FillZeroPassword_read() : (call(char[] Console+.readPassword(..))) && MOP_CommonPointCut();
	after () returning (Object pwd) : Console_FillZeroPassword_read() {
	}

	class Console_FillZeroPassword_DummyHookThread extends Thread {
		public void run(){
		}
	}
}
