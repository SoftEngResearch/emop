package mop;
import java.io.*;
import java.lang.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect Object_NoCloneMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Object_NoCloneMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Object_NoClone_MOPLock = new ReentrantLock();
	static Condition Object_NoClone_MOPLock_cond = Object_NoClone_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Object_NoClone_clone(Object o) : (call(* Object.clone()) && !within(org.apache.xerces.dom.NodeImpl) && !within(org.apache.batik.bridge.UpdateManager) && !within(org.codehaus.janino.ScriptEvaluator) && !within(org.apache.batik.ext.awt.image.codec.png.PNGEncodeParam) && !within(org.apache.batik.ext.awt.image.spi.MagicNumberRegistryEntry) && !within(org.apache.xerces.util.XMLCatalogResolver) && target(o)) && MOP_CommonPointCut();
	before (Object o) : Object_NoClone_clone(o) {
	}

}
