package mop;
import java.io.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect StreamTokenizer_AccessInvalidFieldMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public StreamTokenizer_AccessInvalidFieldMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock StreamTokenizer_AccessInvalidField_MOPLock = new ReentrantLock();
	static Condition StreamTokenizer_AccessInvalidField_MOPLock_cond = StreamTokenizer_AccessInvalidField_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut StreamTokenizer_AccessInvalidField_nval(StreamTokenizer s) : (get(* StreamTokenizer.nval) && target(s)) && MOP_CommonPointCut();
	before (StreamTokenizer s) : StreamTokenizer_AccessInvalidField_nval(s) {
	}

	pointcut StreamTokenizer_AccessInvalidField_sval(StreamTokenizer s) : (get(* StreamTokenizer.sval) && target(s)) && MOP_CommonPointCut();
	before (StreamTokenizer s) : StreamTokenizer_AccessInvalidField_sval(s) {
	}

	pointcut StreamTokenizer_AccessInvalidField_nexttoken_word(StreamTokenizer s) : (call(* StreamTokenizer+.nextToken(..)) && target(s)) && MOP_CommonPointCut();
	after (StreamTokenizer s) returning (int t) : StreamTokenizer_AccessInvalidField_nexttoken_word(s) {
		//StreamTokenizer_AccessInvalidField_nexttoken_word
		//StreamTokenizer_AccessInvalidField_nexttoken_num
		//StreamTokenizer_AccessInvalidField_nexttoken_eol
		//StreamTokenizer_AccessInvalidField_nexttoken_eof
	}

}
