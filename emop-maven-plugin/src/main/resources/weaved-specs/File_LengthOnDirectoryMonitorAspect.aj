package mop;
import java.io.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect File_LengthOnDirectoryMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public File_LengthOnDirectoryMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock File_LengthOnDirectory_MOPLock = new ReentrantLock();
	static Condition File_LengthOnDirectory_MOPLock_cond = File_LengthOnDirectory_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut File_LengthOnDirectory_bad_length(File f) : (call(* File+.length()) && target(f)) && MOP_CommonPointCut();
	before (File f) : File_LengthOnDirectory_bad_length(f) {
	}

}
