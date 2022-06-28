package mop;
import java.util.*;
import java.io.*;
import java.nio.channels.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect Scanner_ManipulateAfterCloseMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Scanner_ManipulateAfterCloseMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Scanner_ManipulateAfterClose_MOPLock = new ReentrantLock();
	static Condition Scanner_ManipulateAfterClose_MOPLock_cond = Scanner_ManipulateAfterClose_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Scanner_ManipulateAfterClose_manipulate(Closeable c) : ((call(* InputStream+.read(..)) || call(* InputStream+.available(..)) || call(* InputStream+.reset(..)) || call(* InputStream+.skip(..)) || call(* Readable+.read(..)) || call(* ReadableByteChannel+.read(..))) && target(c) && !target(ByteArrayInputStream) && !target(StringBufferInputStream)) && MOP_CommonPointCut();
	before (Closeable c) : Scanner_ManipulateAfterClose_manipulate(c) {
	}

	pointcut Scanner_ManipulateAfterClose_create(Closeable c) : ((call(Scanner+.new(InputStream, ..)) || call(Scanner+.new(Readable, ..)) || call(Scanner+.new(ReadableByteChannel, ..))) && args(c, ..)) && MOP_CommonPointCut();
	after (Closeable c) returning (Scanner s) : Scanner_ManipulateAfterClose_create(c) {
	}

	pointcut Scanner_ManipulateAfterClose_close(Scanner s) : (call(* Scanner+.close()) && target(s) && !args(ByteArrayInputStream) && !args(StringBufferInputStream)) && MOP_CommonPointCut();
	after (Scanner s) : Scanner_ManipulateAfterClose_close(s) {
	}

}
