package mop;
import java.util.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

public aspect Arrays_SortBeforeBinarySearchMonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public Arrays_SortBeforeBinarySearchMonitorAspect(){
	}

	// Declarations for the Lock
	static ReentrantLock Arrays_SortBeforeBinarySearch_MOPLock = new ReentrantLock();
	static Condition Arrays_SortBeforeBinarySearch_MOPLock_cond = Arrays_SortBeforeBinarySearch_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() ;
	pointcut Arrays_SortBeforeBinarySearch_bsearch2_4(Object[] arr, int from, int to, Comparator comp2) : (call(int Arrays.binarySearch(Object[], int, int, Object, Comparator)) && args(arr, from, to, .., comp2)) && MOP_CommonPointCut();
	before (Object[] arr, int from, int to, Comparator comp2) : Arrays_SortBeforeBinarySearch_bsearch2_4(arr, from, to, comp2) {
	}

	pointcut Arrays_SortBeforeBinarySearch_bsearch2_3(Object[] arr, Comparator comp2) : (call(int Arrays.binarySearch(Object[], Object, Comparator)) && args(arr, .., comp2)) && MOP_CommonPointCut();
	before (Object[] arr, Comparator comp2) : Arrays_SortBeforeBinarySearch_bsearch2_3(arr, comp2) {
	}

	pointcut Arrays_SortBeforeBinarySearch_bsearch1_4(Object[] arr, int from, int to) : (call(int Arrays.binarySearch(Object[], int, int, Object)) && args(arr, from, to, ..)) && MOP_CommonPointCut();
	before (Object[] arr, int from, int to) : Arrays_SortBeforeBinarySearch_bsearch1_4(arr, from, to) {
	}

	pointcut Arrays_SortBeforeBinarySearch_bsearch1_3(Object[] arr) : (call(int Arrays.binarySearch(Object[], Object)) && args(arr, ..)) && MOP_CommonPointCut();
	before (Object[] arr) : Arrays_SortBeforeBinarySearch_bsearch1_3(arr) {
	}

	pointcut Arrays_SortBeforeBinarySearch_modify(Object[] arr) : (set(Object[] *) && args(arr)) && MOP_CommonPointCut();
	before (Object[] arr) : Arrays_SortBeforeBinarySearch_modify(arr) {
	}

	pointcut Arrays_SortBeforeBinarySearch_sort2_4(Object[] arr, int from, int to, Comparator comp2) : (call(void Arrays.sort(Object[], int, int, Comparator)) && args(arr, from, to, comp2)) && MOP_CommonPointCut();
	before (Object[] arr, int from, int to, Comparator comp2) : Arrays_SortBeforeBinarySearch_sort2_4(arr, from, to, comp2) {
	}

	pointcut Arrays_SortBeforeBinarySearch_sort2_3(Object[] arr, Comparator comp2) : (call(void Arrays.sort(Object[], Comparator)) && args(arr, comp2)) && MOP_CommonPointCut();
	before (Object[] arr, Comparator comp2) : Arrays_SortBeforeBinarySearch_sort2_3(arr, comp2) {
	}

	pointcut Arrays_SortBeforeBinarySearch_sort1_4(Object[] arr, int from, int to) : (call(void Arrays.sort(Object[], int, int)) && args(arr, from, to)) && MOP_CommonPointCut();
	before (Object[] arr, int from, int to) : Arrays_SortBeforeBinarySearch_sort1_4(arr, from, to) {
	}

	pointcut Arrays_SortBeforeBinarySearch_sort1_3(Object[] arr) : (call(void Arrays.sort(Object[])) && args(arr)) && MOP_CommonPointCut();
	before (Object[] arr) : Arrays_SortBeforeBinarySearch_sort1_3(arr) {
	}

}
