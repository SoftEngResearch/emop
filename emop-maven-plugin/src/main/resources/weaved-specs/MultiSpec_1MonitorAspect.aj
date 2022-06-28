package mop;
import java.util.*;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging;
import com.runtimeverification.rvmonitor.java.rt.RVMLogging.Level;
import java.net.*;
import java.io.*;
import java.lang.*;
import java.nio.*;
import java.io.OutputStream;
import javax.swing.*;
import java.lang.reflect.*;
import org.aspectj.lang.Signature;
import java.nio.channels.*;
import java.io.InputStream;
import java.awt.EventQueue;
import java.security.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import java.lang.ref.*;
import org.aspectj.lang.*;

aspect BaseAspect {
	pointcut notwithin() :
	!within(sun..*) &&
	!within(java..*) &&
	!within(javax..*) &&
	!within(javafx..*) &&
	!within(com.sun..*) &&
	!within(org.dacapo.harness..*) &&
	!within(net.sf.cglib..*) &&
	!within(mop..*) &&
	!within(javamoprt..*) &&
	!within(rvmonitorrt..*) &&
	!within(org.junit..*) &&
	!within(junit..*) &&
	!within(java.lang.Object) &&
	!within(com.runtimeverification..*) &&
	!within(org.apache.maven.surefire..*) &&
	!within(org.mockito..*) &&
	!within(org.powermock..*) &&
	!within(org.easymock..*) &&
	!within(com.mockrunner..*) &&
	!within(org.jmock..*);
}

public aspect MultiSpec_1MonitorAspect implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	public MultiSpec_1MonitorAspect(){
		Runtime.getRuntime().addShutdownHook(new MultiSpec_1_DummyHookThread());
	}

	// Declarations for the Lock
	static ReentrantLock MultiSpec_1_MOPLock = new ReentrantLock();
	static Condition MultiSpec_1_MOPLock_cond = MultiSpec_1_MOPLock.newCondition();

	pointcut MOP_CommonPointCut() : !within(com.runtimeverification.rvmonitor.java.rt.RVMObject+) && !adviceexecution() && BaseAspect.notwithin();
	pointcut InputStream_UnmarkedReset_mark(InputStream i) : (call(* InputStream+.mark(..)) && target(i) && if(i instanceof BufferedInputStream || i instanceof DataInputStream || i instanceof LineNumberInputStream)) && MOP_CommonPointCut();
	before (InputStream i) : InputStream_UnmarkedReset_mark(i) {
	}

	pointcut Reader_MarkReset_reset(Reader r) : (call(* Reader+.reset(..)) && target(r) && (target(PushbackReader) || target(InputStreamReader) || target(FileReader) || target(PipedReader))) && MOP_CommonPointCut();
	before (Reader r) : Reader_MarkReset_reset(r) {
	}

	pointcut Reader_MarkReset_mark(Reader r) : (call(* Reader+.mark(..)) && target(r) && (target(PushbackReader) || target(InputStreamReader) || target(FileReader) || target(PipedReader))) && MOP_CommonPointCut();
	before (Reader r) : Reader_MarkReset_mark(r) {
	}

	pointcut DatagramSocket_TrafficClass_settc(DatagramSocket socket, int tc) : (call(void DatagramSocket.setTrafficClass(int)) && target(socket) && args(tc)) && MOP_CommonPointCut();
	before (DatagramSocket socket, int tc) : DatagramSocket_TrafficClass_settc(socket, tc) {
	}

	pointcut PipedStream_SingleThread_read(PipedInputStream i) : (call(* InputStream+.read(..)) && target(i)) && MOP_CommonPointCut();
	before (PipedInputStream i) : PipedStream_SingleThread_read(i) {
		Thread t = Thread.currentThread();
	}

	pointcut PipedStream_SingleThread_write(PipedOutputStream o) : (call(* OutputStream+.write(..)) && target(o)) && MOP_CommonPointCut();
	before (PipedOutputStream o) : PipedStream_SingleThread_write(o) {
		Thread t = Thread.currentThread();
	}

	pointcut PipedStream_SingleThread_create4(PipedOutputStream o, PipedInputStream i) : (call(* PipedOutputStream+.connect(PipedInputStream+)) && target(o) && args(i)) && MOP_CommonPointCut();
	before (PipedOutputStream o, PipedInputStream i) : PipedStream_SingleThread_create4(o, i) {
	}

	pointcut PipedStream_SingleThread_create2(PipedInputStream i, PipedOutputStream o) : (call(* PipedInputStream+.connect(PipedOutputStream+)) && target(i) && args(o)) && MOP_CommonPointCut();
	before (PipedInputStream i, PipedOutputStream o) : PipedStream_SingleThread_create2(i, o) {
	}

	pointcut HttpCookie_Domain_setdomain(String domain) : (call(void HttpCookie.setDomain(String)) && args(domain)) && MOP_CommonPointCut();
	before (String domain) : HttpCookie_Domain_setdomain(domain) {
	}

	pointcut Closeable_MultipleClose_close(Closeable c) : (call(* Closeable+.close(..)) && target(c)) && MOP_CommonPointCut();
	before (Closeable c) : Closeable_MultipleClose_close(c) {
	}

	pointcut RandomAccessFile_ManipulateAfterClose_close(RandomAccessFile f) : (call(* RandomAccessFile+.close(..)) && target(f)) && MOP_CommonPointCut();
	before (RandomAccessFile f) : RandomAccessFile_ManipulateAfterClose_close(f) {
	}

	pointcut RandomAccessFile_ManipulateAfterClose_manipulate(RandomAccessFile f) : ((call(* RandomAccessFile+.read*(..)) || call(* RandomAccessFile+.write*(..))) && target(f)) && MOP_CommonPointCut();
	before (RandomAccessFile f) : RandomAccessFile_ManipulateAfterClose_manipulate(f) {
	}

	pointcut IDN_ToAscii_toascii(String input) : ((call(* IDN.toASCII(String)) || call(* IDN.toASCII(String, int))) && args(input, ..)) && MOP_CommonPointCut();
	before (String input) : IDN_ToAscii_toascii(input) {
	}

	pointcut Reader_ReadAheadLimit_mark(Reader r, int l) : (call(* Reader+.mark(int)) && target(r) && args(l) && if(r instanceof BufferedReader || r instanceof LineNumberReader)) && MOP_CommonPointCut();
	before (Reader r, int l) : Reader_ReadAheadLimit_mark(r, l) {
	}

	pointcut Socket_LargeReceiveBuffer_set(Socket sock, int size) : (call(* Socket+.setReceiveBufferSize(int)) && target(sock) && args(size)) && MOP_CommonPointCut();
	before (Socket sock, int size) : Socket_LargeReceiveBuffer_set(sock, size) {
	}

	pointcut StringBuffer_SingleThreadUsage_use(StringBuffer s) : (call(* StringBuffer.*(..)) && target(s)) && MOP_CommonPointCut();
	before (StringBuffer s) : StringBuffer_SingleThreadUsage_use(s) {
		Thread t = Thread.currentThread();
	}

	pointcut Console_CloseWriter_close(Writer w) : (call(* Writer+.close(..)) && target(w)) && MOP_CommonPointCut();
	before (Writer w) : Console_CloseWriter_close(w) {
	}

	pointcut Socket_Timeout_set(int timeout) : (call(* Socket+.setSoTimeout(int)) && args(timeout)) && MOP_CommonPointCut();
	before (int timeout) : Socket_Timeout_set(timeout) {
	}

	pointcut StrictMath_ContendedRandom_onethread_use() : (call(* StrictMath.random(..))) && MOP_CommonPointCut();
	before () : StrictMath_ContendedRandom_onethread_use() {
		Thread t = Thread.currentThread();
		//StrictMath_ContendedRandom_otherthread_use
		//StrictMath_ContendedRandom_onethread_use
	}

	pointcut ProcessBuilder_ThreadSafe_safe_oper(ProcessBuilder p) : (call(* ProcessBuilder.*(..)) && target(p)) && MOP_CommonPointCut();
	before (ProcessBuilder p) : ProcessBuilder_ThreadSafe_safe_oper(p) {
		Thread t = Thread.currentThread();
		//ProcessBuilder_ThreadSafe_unsafe_oper
		//ProcessBuilder_ThreadSafe_safe_oper
	}

	pointcut ClassLoader_UnsafeClassDefinition_defineClass(String name) : (call(* ClassLoader+.defineClass(String, ..)) && args(name, ..)) && MOP_CommonPointCut();
	before (String name) : ClassLoader_UnsafeClassDefinition_defineClass(name) {
	}

	pointcut Collections_CopySize_bad_copy(List dest, List src) : (call(void Collections.copy(List, List)) && args(dest, src)) && MOP_CommonPointCut();
	before (List dest, List src) : Collections_CopySize_bad_copy(dest, src) {
	}

	pointcut SocketImpl_CloseOutput_close(SocketImpl sock) : ((call(* SocketImpl+.close(..)) || call(* SocketImpl+.shutdownOutput(..))) && target(sock)) && MOP_CommonPointCut();
	before (SocketImpl sock) : SocketImpl_CloseOutput_close(sock) {
	}

	pointcut InetSocketAddress_Port_construct(int port) : ((call(InetSocketAddress.new(int)) || call(InetSocketAddress.new(InetAddress, int)) || call(InetSocketAddress.new(String, int)) || call(* InetSocketAddress.createUnresolved(String, int))) && args(.., port)) && MOP_CommonPointCut();
	before (int port) : InetSocketAddress_Port_construct(port) {
	}

	pointcut Deque_OfferRatherThanAdd_add(Deque q) : ((call(* Deque+.addFirst(..)) || call(* Deque+.addLast(..)) || call(* Deque+.add(..)) || call(* Deque+.push(..))) && target(q)) && MOP_CommonPointCut();
	before (Deque q) : Deque_OfferRatherThanAdd_add(q) {
	}

	pointcut Map_UnsafeIterator_modifyCol(Collection c) : ((call(* Collection+.clear(..)) || call(* Collection+.offer*(..)) || call(* Collection+.pop(..)) || call(* Collection+.push(..)) || call(* Collection+.remove*(..)) || call(* Collection+.retain*(..))) && target(c)) && MOP_CommonPointCut();
	before (Collection c) : Map_UnsafeIterator_modifyCol(c) {
	}

	pointcut Map_UnsafeIterator_modifyMap(Map m) : ((call(* Map+.clear*(..)) || call(* Map+.put*(..)) || call(* Map+.remove(..))) && target(m)) && MOP_CommonPointCut();
	before (Map m) : Map_UnsafeIterator_modifyMap(m) {
	}

	pointcut Socket_ReuseAddress_set(Socket sock) : (call(* Socket+.setReuseAddress(..)) && target(sock)) && MOP_CommonPointCut();
	before (Socket sock) : Socket_ReuseAddress_set(sock) {
	}

	pointcut Enum_NoOrdinal_ordinal() : (call(* Enum+.ordinal())) && MOP_CommonPointCut();
	before () : Enum_NoOrdinal_ordinal() {
	}

	pointcut InputStream_ReadAheadLimit_badreset(InputStream i) : (call(* InputStream+.reset(..)) && target(i) && if(i instanceof BufferedInputStream || i instanceof DataInputStream || i instanceof LineNumberInputStream)) && MOP_CommonPointCut();
	before (InputStream i) : InputStream_ReadAheadLimit_badreset(i) {
		//InputStream_UnmarkedReset_reset
		//InputStream_ReadAheadLimit_goodreset
		//InputStream_ReadAheadLimit_badreset
	}

	pointcut InputStream_ReadAheadLimit_mark(InputStream i, int l) : (call(* InputStream+.mark(int)) && target(i) && args(l) && if(i instanceof BufferedInputStream || i instanceof DataInputStream || i instanceof LineNumberInputStream)) && MOP_CommonPointCut();
	before (InputStream i, int l) : InputStream_ReadAheadLimit_mark(i, l) {
	}

	pointcut StringTokenizer_HasMoreElements_next(StringTokenizer i) : ((call(* StringTokenizer.nextToken()) || call(* StringTokenizer.nextElement())) && target(i)) && MOP_CommonPointCut();
	before (StringTokenizer i) : StringTokenizer_HasMoreElements_next(i) {
	}

	pointcut Appendable_ThreadSafe_safe_append(Appendable a) : (call(* Appendable+.append(..)) && target(a) && !target(StringBuffer)) && MOP_CommonPointCut();
	before (Appendable a) : Appendable_ThreadSafe_safe_append(a) {
		Thread t = Thread.currentThread();
		//Appendable_ThreadSafe_unsafe_append
		//Appendable_ThreadSafe_safe_append
	}

	pointcut System_NullArrayCopy_null_arraycopy(Object src, int srcPos, Object dest, int destPos, int length) : (call(* System.arraycopy(Object, int, Object, int, int)) && args(src, srcPos, dest, destPos, length)) && MOP_CommonPointCut();
	before (Object src, int srcPos, Object dest, int destPos, int length) : System_NullArrayCopy_null_arraycopy(src, srcPos, dest, destPos, length) {
	}

	pointcut DatagramPacket_SetLength_setlength(DatagramPacket packet, int length) : (call(void DatagramPacket.setLength(int)) && target(packet) && args(length)) && MOP_CommonPointCut();
	before (DatagramPacket packet, int length) : DatagramPacket_SetLength_setlength(packet, length) {
	}

	pointcut Reader_UnmarkedReset_reset(Reader r) : (call(* Reader+.reset(..)) && target(r) && if(r instanceof BufferedReader || r instanceof LineNumberReader)) && MOP_CommonPointCut();
	before (Reader r) : Reader_UnmarkedReset_reset(r) {
		//Reader_ReadAheadLimit_goodreset
		//Reader_ReadAheadLimit_badreset
		//Reader_UnmarkedReset_reset
	}

	pointcut Reader_UnmarkedReset_mark(Reader r) : (call(* Reader+.mark(..)) && target(r) && if(r instanceof BufferedReader || r instanceof LineNumberReader)) && MOP_CommonPointCut();
	before (Reader r) : Reader_UnmarkedReset_mark(r) {
	}

	pointcut NavigableMap_Modification_modify2(NavigableMap m2) : ((call(* Map+.clear*(..)) || call(* Map+.put*(..)) || call(* Map+.remove(..))) && target(m2)) && MOP_CommonPointCut();
	before (NavigableMap m2) : NavigableMap_Modification_modify2(m2) {
	}

	pointcut NavigableMap_Modification_modify1(NavigableMap m1) : ((call(* Map+.clear*(..)) || call(* Map+.put*(..)) || call(* Map+.remove(..))) && target(m1)) && MOP_CommonPointCut();
	before (NavigableMap m1) : NavigableMap_Modification_modify1(m1) {
	}

	pointcut InputStream_MarkAfterClose_close(InputStream i) : (call(* InputStream+.close(..)) && target(i)) && MOP_CommonPointCut();
	before (InputStream i) : InputStream_MarkAfterClose_close(i) {
	}

	pointcut InputStream_MarkAfterClose_mark(InputStream i) : (call(* InputStream+.mark(..)) && target(i)) && MOP_CommonPointCut();
	before (InputStream i) : InputStream_MarkAfterClose_mark(i) {
	}

	pointcut Thread_StartOnce_start(Thread t) : (call(* Thread+.start()) && target(t)) && MOP_CommonPointCut();
	before (Thread t) : Thread_StartOnce_start(t) {
	}

	pointcut ArrayDeque_UnsafeIterator_useiter(Iterator i) : (call(* Iterator.*(..)) && target(i)) && MOP_CommonPointCut();
	before (Iterator i) : ArrayDeque_UnsafeIterator_useiter(i) {
	}

	pointcut ArrayDeque_UnsafeIterator_modify(ArrayDeque q) : (target(ArrayDeque) && (call(* Collection+.add*(..)) || call(* Collection+.clear(..)) || call(* Collection+.offer*(..)) || call(* Collection+.pop(..)) || call(* Collection+.push(..)) || call(* Collection+.remove*(..)) || call(* Collection+.retain*(..))) && target(q)) && MOP_CommonPointCut();
	before (ArrayDeque q) : ArrayDeque_UnsafeIterator_modify(q) {
	}

	pointcut Vector_InsertIndex_insert(Vector v, int index) : (call(* Vector+.insertElementAt(Object, int)) && target(v) && args(.., index)) && MOP_CommonPointCut();
	before (Vector v, int index) : Vector_InsertIndex_insert(v, index) {
	}

	pointcut HttpCookie_Name_construct(String name) : (call(HttpCookie.new(String, String)) && args(name, ..)) && MOP_CommonPointCut();
	before (String name) : HttpCookie_Name_construct(name) {
	}

	pointcut Socket_PerformancePreferences_set(Socket sock) : (call(* Socket+.setPerformancePreferences(..)) && target(sock)) && MOP_CommonPointCut();
	before (Socket sock) : Socket_PerformancePreferences_set(sock) {
	}

	pointcut URLEncoder_EncodeUTF8_encode(String enc) : (call(* URLEncoder.encode(String, String)) && args(*, enc)) && MOP_CommonPointCut();
	before (String enc) : URLEncoder_EncodeUTF8_encode(enc) {
	}

	pointcut DatagramPacket_Length_construct_offlen(byte[] buffer, int offset, int length) : ((call(DatagramPacket.new(byte[], int, int)) || call(DatagramPacket.new(byte[], int, int, InetAddress, int)) || call(DatagramPacket.new(byte[], int, int, SocketAddress))) && args(buffer, offset, length, ..)) && MOP_CommonPointCut();
	before (byte[] buffer, int offset, int length) : DatagramPacket_Length_construct_offlen(buffer, offset, length) {
	}

	pointcut DatagramPacket_Length_construct_len(byte[] buffer, int length) : ((call(DatagramPacket.new(byte[], int)) || call(DatagramPacket.new(byte[], int, InetAddress, int)) || call(DatagramPacket.new(byte[], int, SocketAddress))) && args(buffer, length, ..)) && MOP_CommonPointCut();
	before (byte[] buffer, int length) : DatagramPacket_Length_construct_len(buffer, length) {
	}

	pointcut Short_BadDecodeArg_decode(Short s, String nm) : (call(* Short.decode(String)) && args(nm) && target(s)) && MOP_CommonPointCut();
	before (Short s, String nm) : Short_BadDecodeArg_decode(s, nm) {
	}

	pointcut ObjectStreamClass_Initialize_init(ObjectStreamClass c) : ((call(* ObjectStreamClass+.initProxy(..)) || call(* ObjectStreamClass+.initNonProxy(..)) || call(* ObjectStreamClass+.readNonProxy(..))) && target(c)) && MOP_CommonPointCut();
	before (ObjectStreamClass c) : ObjectStreamClass_Initialize_init(c) {
	}

	pointcut SecurityManager_Permission_check(SecurityManager manager, Object context) : (call(* SecurityManager.checkPermission(Permission, Object)) && target(manager) && args(.., context)) && MOP_CommonPointCut();
	before (SecurityManager manager, Object context) : SecurityManager_Permission_check(manager, context) {
	}

	pointcut PipedInputStream_UnconnectedRead_read(PipedInputStream i) : ((call(* PipedInputStream+.read(..)) || call(* PipedInputStream+.receive(..)) || call(* PipedInputStream+.available(..))) && target(i)) && MOP_CommonPointCut();
	before (PipedInputStream i) : PipedInputStream_UnconnectedRead_read(i) {
	}

	pointcut PipedInputStream_UnconnectedRead_connect2(PipedInputStream i) : (call(* PipedInputStream+.connect(PipedOutputStream+)) && target(i)) && MOP_CommonPointCut();
	before (PipedInputStream i) : PipedInputStream_UnconnectedRead_connect2(i) {
	}

	pointcut PipedInputStream_UnconnectedRead_connect1(PipedInputStream i) : (call(* PipedOutputStream+.connect(PipedInputStream+)) && args(i)) && MOP_CommonPointCut();
	before (PipedInputStream i) : PipedInputStream_UnconnectedRead_connect1(i) {
	}

	pointcut PipedInputStream_UnconnectedRead_create_io(PipedInputStream i) : (call(PipedOutputStream+.new(PipedInputStream+)) && args(i)) && MOP_CommonPointCut();
	before (PipedInputStream i) : PipedInputStream_UnconnectedRead_create_io(i) {
	}

	pointcut Collections_SortBeforeBinarySearch_bsearch2(List list, Comparator comp2) : (call(int Collections.binarySearch(List, Object, Comparator)) && args(list, .., comp2)) && MOP_CommonPointCut();
	before (List list, Comparator comp2) : Collections_SortBeforeBinarySearch_bsearch2(list, comp2) {
		//Collections_SortBeforeBinarySearch_bad_bsearch2
		//Collections_SortBeforeBinarySearch_bsearch2
	}

	pointcut Collections_SortBeforeBinarySearch_bsearch1(List list) : (call(int Collections.binarySearch(List, Object)) && args(list, ..)) && MOP_CommonPointCut();
	before (List list) : Collections_SortBeforeBinarySearch_bsearch1(list) {
	}

	pointcut Collections_SortBeforeBinarySearch_modify(List list) : ((call(* Collection+.add*(..)) || call(* Collection+.remove*(..)) || call(* Collection+.clear(..)) || call(* Collection+.retain*(..)) || call(* List+.set(..))) && target(list)) && MOP_CommonPointCut();
	before (List list) : Collections_SortBeforeBinarySearch_modify(list) {
	}

	pointcut Collections_SortBeforeBinarySearch_sort2(List list, Comparator comp2) : (call(void Collections.sort(List, Comparator)) && args(list, comp2)) && MOP_CommonPointCut();
	before (List list, Comparator comp2) : Collections_SortBeforeBinarySearch_sort2(list, comp2) {
	}

	pointcut EnumMap_NonNull_insertnull_12(Map m) : (call(* EnumMap.putAll(Map)) && args(m)) && MOP_CommonPointCut();
	before (Map m) : EnumMap_NonNull_insertnull_12(m) {
	}

	pointcut EnumMap_NonNull_insertnull_11(Object e) : (call(* EnumMap.put(Object, Object)) && args(e, ..)) && MOP_CommonPointCut();
	before (Object e) : EnumMap_NonNull_insertnull_11(e) {
	}

	pointcut Byte_BadParsingArgs_bad_arg2(String s) : (call(* Byte.parseByte(String)) && args(s)) && MOP_CommonPointCut();
	before (String s) : Byte_BadParsingArgs_bad_arg2(s) {
	}

	pointcut Byte_BadParsingArgs_bad_arg(String s, int radix) : (call(* Byte.parseByte(String, int)) && args(s, radix)) && MOP_CommonPointCut();
	before (String s, int radix) : Byte_BadParsingArgs_bad_arg(s, radix) {
	}

	pointcut ObjectInput_Close_close(ObjectInput i) : (call(* ObjectInput+.close(..)) && target(i)) && MOP_CommonPointCut();
	before (ObjectInput i) : ObjectInput_Close_close(i) {
	}

	pointcut URLConnection_SetBeforeConnect_connect(URLConnection c) : ((call(* URLConnection+.connect(..)) || call(* URLConnection+.getContent(..)) || call(* URLConnection+.getContentEncoding(..)) || call(* URLConnection+.getContentLength(..)) || call(* URLConnection+.getContentType(..)) || call(* URLConnection+.getDate(..)) || call(* URLConnection+.getExpiration(..)) || call(* URLConnection+.getHeaderField(..)) || call(* URLConnection+.getHeaderFieldInt(..)) || call(* URLConnection+.getHeaderFields(..)) || call(* URLConnection+.getInputStream(..)) || call(* URLConnection+.getLastModified(..)) || call(* URLConnection+.getOutputStream(..))) && target(c)) && MOP_CommonPointCut();
	before (URLConnection c) : URLConnection_SetBeforeConnect_connect(c) {
	}

	pointcut URLConnection_SetBeforeConnect_set(URLConnection c) : (call(* URLConnection+.set*(..)) && target(c)) && MOP_CommonPointCut();
	before (URLConnection c) : URLConnection_SetBeforeConnect_set(c) {
	}

	pointcut StringBuilder_ThreadSafe_safe_oper(StringBuilder b) : (call(* StringBuilder.*(..)) && target(b)) && MOP_CommonPointCut();
	before (StringBuilder b) : StringBuilder_ThreadSafe_safe_oper(b) {
		Thread t = Thread.currentThread();
		//StringBuilder_ThreadSafe_unsafe_oper
		//StringBuilder_ThreadSafe_safe_oper
	}

	pointcut Socket_InputStreamUnavailable_shutdown(Socket sock) : (call(* Socket+.shutdownInput()) && target(sock)) && MOP_CommonPointCut();
	before (Socket sock) : Socket_InputStreamUnavailable_shutdown(sock) {
	}

	pointcut Socket_InputStreamUnavailable_get(Socket sock) : (call(* Socket+.getInputStream(..)) && target(sock)) && MOP_CommonPointCut();
	before (Socket sock) : Socket_InputStreamUnavailable_get(sock) {
	}

	pointcut Socket_ReuseSocket_bind(Socket sock) : (call(* Socket+.bind(..)) && target(sock)) && MOP_CommonPointCut();
	before (Socket sock) : Socket_ReuseSocket_bind(sock) {
		//Socket_ReuseAddress_bind
		//Socket_ReuseSocket_bind
	}

	pointcut InputStream_ManipulateAfterClose_close(InputStream i) : (call(* InputStream+.close(..)) && target(i) && !target(ByteArrayInputStream) && !target(StringBufferInputStream)) && MOP_CommonPointCut();
	before (InputStream i) : InputStream_ManipulateAfterClose_close(i) {
	}

	pointcut File_DeleteTempFile_implicit(File f) : (call(* File+.deleteOnExit(..)) && target(f)) && MOP_CommonPointCut();
	before (File f) : File_DeleteTempFile_implicit(f) {
	}

	pointcut File_DeleteTempFile_explicit(File f) : (call(* File+.delete(..)) && target(f)) && MOP_CommonPointCut();
	before (File f) : File_DeleteTempFile_explicit(f) {
	}

	pointcut Collection_UnsafeIterator_modify(Collection c) : ((call(* Collection+.add*(..)) || call(* Collection+.clear(..)) || call(* Collection+.offer*(..)) || call(* Collection+.pop(..)) || call(* Collection+.push(..)) || call(* Collection+.remove*(..)) || call(* Collection+.retain*(..))) && target(c)) && MOP_CommonPointCut();
	before (Collection c) : Collection_UnsafeIterator_modify(c) {
	}

	pointcut List_UnsynchronizedSubList_usesublist(List s) : (call(* List.*(..)) && target(s)) && MOP_CommonPointCut();
	before (List s) : List_UnsynchronizedSubList_usesublist(s) {
	}

	pointcut List_UnsynchronizedSubList_modifybackinglist(List b) : ((call(* Collection+.add*(..)) || call(* Collection+.remove*(..)) || call(* Collection+.clear(..)) || call(* Collection+.retain*(..))) && target(b)) && MOP_CommonPointCut();
	before (List b) : List_UnsynchronizedSubList_modifybackinglist(b) {
	}

	pointcut ObjectOutput_Close_close(ObjectOutput o) : (call(* ObjectOutput+.close(..)) && target(o)) && MOP_CommonPointCut();
	before (ObjectOutput o) : ObjectOutput_Close_close(o) {
	}

	pointcut ContentHandler_GetContent_get_content() : (call(* ContentHandler+.getContent(..))) && MOP_CommonPointCut();
	before () : ContentHandler_GetContent_get_content() {
	}

	pointcut TreeSet_Comparable_addall(Collection c) : (call(* Collection+.addAll(Collection)) && target(TreeSet) && args(c)) && MOP_CommonPointCut();
	before (Collection c) : TreeSet_Comparable_addall(c) {
	}

	pointcut TreeSet_Comparable_add(Object e) : (call(* Collection+.add*(..)) && target(TreeSet) && args(e)) && MOP_CommonPointCut();
	before (Object e) : TreeSet_Comparable_add(e) {
	}

	pointcut ServerSocket_Timeout_set(int timeout) : (call(* ServerSocket+.setSoTimeout(int)) && args(timeout)) && MOP_CommonPointCut();
	before (int timeout) : ServerSocket_Timeout_set(timeout) {
	}

	pointcut Socket_CloseInput_use(InputStream input) : (call(* InputStream+.*(..)) && target(input)) && MOP_CommonPointCut();
	before (InputStream input) : Socket_CloseInput_use(input) {
	}

	pointcut Socket_CloseInput_close(Socket sock) : (call(* Socket+.close(..)) && target(sock)) && MOP_CommonPointCut();
	before (Socket sock) : Socket_CloseInput_close(sock) {
		//Socket_ReuseSocket_close
		//Socket_CloseInput_close
	}

	pointcut Dictionary_NullKeyOrValue_putnull(Dictionary d, Object key, Object value) : (call(* Dictionary+.put(..)) && args(key, value) && target(d)) && MOP_CommonPointCut();
	before (Dictionary d, Object key, Object value) : Dictionary_NullKeyOrValue_putnull(d, key, value) {
	}

	pointcut Properties_ManipulateAfterLoad_manipulate(InputStream i) : ((call(* InputStream+.read(..)) || call(* InputStream+.available(..)) || call(* InputStream+.reset(..)) || call(* InputStream+.skip(..))) && target(i) && !target(ByteArrayInputStream) && !target(StringBufferInputStream)) && MOP_CommonPointCut();
	before (InputStream i) : Properties_ManipulateAfterLoad_manipulate(i) {
		//InputStream_ManipulateAfterClose_manipulate
		//Properties_ManipulateAfterLoad_manipulate
	}

	pointcut PipedOutputStream_UnconnectedWrite_write(PipedOutputStream o) : (call(* PipedOutputStream+.write(..)) && target(o)) && MOP_CommonPointCut();
	before (PipedOutputStream o) : PipedOutputStream_UnconnectedWrite_write(o) {
	}

	pointcut PipedOutputStream_UnconnectedWrite_connect2(PipedOutputStream o) : (call(* PipedOutputStream+.connect(PipedInputStream+)) && target(o)) && MOP_CommonPointCut();
	before (PipedOutputStream o) : PipedOutputStream_UnconnectedWrite_connect2(o) {
	}

	pointcut PipedOutputStream_UnconnectedWrite_connect1(PipedOutputStream o) : (call(* PipedInputStream+.connect(PipedOutputStream+)) && args(o)) && MOP_CommonPointCut();
	before (PipedOutputStream o) : PipedOutputStream_UnconnectedWrite_connect1(o) {
	}

	pointcut PipedOutputStream_UnconnectedWrite_create_oi(PipedOutputStream o) : (call(PipedInputStream+.new(PipedOutputStream+)) && args(o)) && MOP_CommonPointCut();
	before (PipedOutputStream o) : PipedOutputStream_UnconnectedWrite_create_oi(o) {
	}

	pointcut StreamTokenizer_AccessInvalidField_nval(StreamTokenizer s) : (get(* StreamTokenizer.nval) && target(s)) && MOP_CommonPointCut();
	before (StreamTokenizer s) : StreamTokenizer_AccessInvalidField_nval(s) {
	}

	pointcut StreamTokenizer_AccessInvalidField_sval(StreamTokenizer s) : (get(* StreamTokenizer.sval) && target(s)) && MOP_CommonPointCut();
	before (StreamTokenizer s) : StreamTokenizer_AccessInvalidField_sval(s) {
	}

	pointcut Collections_ImplementComparable_invalid_minmax(Collection col) : ((call(* Collections.min(Collection)) || call(* Collections.max(Collection))) && args(col)) && MOP_CommonPointCut();
	before (Collection col) : Collections_ImplementComparable_invalid_minmax(col) {
	}

	pointcut Collections_ImplementComparable_invalid_sort(List list) : (call(void Collections.sort(List)) && args(list)) && MOP_CommonPointCut();
	before (List list) : Collections_ImplementComparable_invalid_sort(list) {
		//Collections_SortBeforeBinarySearch_sort1
		//Collections_ImplementComparable_invalid_sort
	}

	pointcut Character_ValidateChar_toCodePoint(char high, char low) : (call(* Character.toCodePoint(char, char)) && args(high, low)) && MOP_CommonPointCut();
	before (char high, char low) : Character_ValidateChar_toCodePoint(high, low) {
	}

	pointcut Character_ValidateChar_charCount(int codePoint) : (call(* Character.charCount(int)) && args(codePoint)) && MOP_CommonPointCut();
	before (int codePoint) : Character_ValidateChar_charCount(codePoint) {
	}

	pointcut Socket_OutputStreamUnavailable_shutdown(Socket sock) : (call(* Socket+.shutdownOutput()) && target(sock)) && MOP_CommonPointCut();
	before (Socket sock) : Socket_OutputStreamUnavailable_shutdown(sock) {
	}

	pointcut Socket_OutputStreamUnavailable_close(Socket sock) : (call(* Socket+.close()) && target(sock)) && MOP_CommonPointCut();
	before (Socket sock) : Socket_OutputStreamUnavailable_close(sock) {
		//Socket_InputStreamUnavailable_close
		//Socket_OutputStreamUnavailable_close
	}

	pointcut Socket_OutputStreamUnavailable_get(Socket sock) : (call(* Socket+.getOutputStream(..)) && target(sock)) && MOP_CommonPointCut();
	before (Socket sock) : Socket_OutputStreamUnavailable_get(sock) {
	}

	pointcut Socket_OutputStreamUnavailable_connect(Socket sock) : (call(* Socket+.connect(..)) && target(sock)) && MOP_CommonPointCut();
	before (Socket sock) : Socket_OutputStreamUnavailable_connect(sock) {
		//Socket_LargeReceiveBuffer_connect
		//Socket_PerformancePreferences_connect
		//Socket_InputStreamUnavailable_connect
		//Socket_ReuseSocket_connect
		//Socket_OutputStreamUnavailable_connect
	}

	pointcut Collections_SynchronizedCollection_accessIter(Iterator iter) : (call(* Iterator.*(..)) && target(iter)) && MOP_CommonPointCut();
	before (Iterator iter) : Collections_SynchronizedCollection_accessIter(iter) {
		//Collections_SynchronizedMap_accessIter
		//Collections_SynchronizedCollection_accessIter
	}

	pointcut NetPermission_Actions_construct(String actions) : (call(NetPermission.new(String, String)) && args(.., actions)) && MOP_CommonPointCut();
	before (String actions) : NetPermission_Actions_construct(actions) {
	}

	pointcut Long_BadDecodeArg_decode(Long l, String nm) : (call(* Long.decode(String)) && args(nm) && target(l)) && MOP_CommonPointCut();
	before (Long l, String nm) : Long_BadDecodeArg_decode(l, nm) {
	}

	pointcut Socket_CloseOutput_use(OutputStream output) : (call(* OutputStream+.*(..)) && target(output)) && MOP_CommonPointCut();
	before (OutputStream output) : Socket_CloseOutput_use(output) {
		//SocketImpl_CloseOutput_use
		//Socket_CloseOutput_use
	}

	pointcut Socket_CloseOutput_close(Socket sock) : ((call(* Socket+.close(..)) || call(* Socket+.shutdownOutput(..))) && target(sock)) && MOP_CommonPointCut();
	before (Socket sock) : Socket_CloseOutput_close(sock) {
	}

	pointcut Long_BadParsingArgs_bad_arg2(String s) : (call(* Long.parseLong(String)) && args(s)) && MOP_CommonPointCut();
	before (String s) : Long_BadParsingArgs_bad_arg2(s) {
	}

	pointcut Long_BadParsingArgs_bad_arg(String s, int radix) : (call(* Long.parseLong(String, int)) && args(s, radix)) && MOP_CommonPointCut();
	before (String s, int radix) : Long_BadParsingArgs_bad_arg(s, radix) {
	}

	pointcut Socket_TrafficClass_settc(Socket socket, int tc) : (call(void Socket.setTrafficClass(int)) && target(socket) && args(tc)) && MOP_CommonPointCut();
	before (Socket socket, int tc) : Socket_TrafficClass_settc(socket, tc) {
	}

	pointcut Enum_UserFriendlyName_name() : (call(* Enum+.name())) && MOP_CommonPointCut();
	before () : Enum_UserFriendlyName_name() {
	}

	pointcut CharSequence_NotInSet_set_addall(Collection c) : (call(* Set+.addAll(Collection)) && args(c)) && MOP_CommonPointCut();
	before (Collection c) : CharSequence_NotInSet_set_addall(c) {
	}

	pointcut CharSequence_NotInSet_set_add() : (call(* Set+.add(..)) && args(CharSequence) && !args(String) && !args(CharBuffer)) && MOP_CommonPointCut();
	before () : CharSequence_NotInSet_set_add() {
	}

	pointcut Short_BadParsingArgs_bad_arg2(String s) : (call(* Short.parseShort(String)) && args(s)) && MOP_CommonPointCut();
	before (String s) : Short_BadParsingArgs_bad_arg2(s) {
	}

	pointcut Short_BadParsingArgs_bad_arg(String s, int radix) : (call(* Short.parseShort(String, int)) && args(s, radix)) && MOP_CommonPointCut();
	before (String s, int radix) : Short_BadParsingArgs_bad_arg(s, radix) {
	}

	pointcut Socket_SetTimeoutBeforeBlockingInput_enter(InputStream input) : (call(* InputStream+.read(..)) && target(input)) && MOP_CommonPointCut();
	before (InputStream input) : Socket_SetTimeoutBeforeBlockingInput_enter(input) {
	}

	pointcut SortedSet_Comparable_addall(Collection c) : (call(* Collection+.addAll(Collection)) && target(SortedSet) && args(c)) && MOP_CommonPointCut();
	before (Collection c) : SortedSet_Comparable_addall(c) {
	}

	pointcut SortedSet_Comparable_add(Object e) : ((call(* Collection+.add*(..)) || call(* Queue+.offer*(..))) && target(SortedSet) && args(e)) && MOP_CommonPointCut();
	before (Object e) : SortedSet_Comparable_add(e) {
	}

	pointcut CharSequence_UndefinedHashCode_hashCode() : (call(* CharSequence+.hashCode(..)) && !target(String) && !target(CharBuffer)) && MOP_CommonPointCut();
	before () : CharSequence_UndefinedHashCode_hashCode() {
	}

	pointcut CharSequence_UndefinedHashCode_equals() : (call(* CharSequence+.equals(..)) && !target(String) && !target(CharBuffer)) && MOP_CommonPointCut();
	before () : CharSequence_UndefinedHashCode_equals() {
	}

	pointcut Enumeration_Obsolete_use(Enumeration e) : (call(* Enumeration+.*(..)) && target(e)) && MOP_CommonPointCut();
	before (Enumeration e) : Enumeration_Obsolete_use(e) {
	}

	pointcut Set_ItselfAsElement_addall(Set s, Collection src) : (call(* Set+.addAll(Collection)) && target(s) && args(src)) && MOP_CommonPointCut();
	before (Set s, Collection src) : Set_ItselfAsElement_addall(s, src) {
	}

	pointcut Set_ItselfAsElement_add(Set s, Object elem) : (call(* Set+.add(Object)) && target(s) && args(elem)) && MOP_CommonPointCut();
	before (Set s, Object elem) : Set_ItselfAsElement_add(s, elem) {
	}

	pointcut ByteArrayOutputStream_FlushBeforeRetrieve_tostring(ByteArrayOutputStream b) : (call(* ByteArrayOutputStream+.toString(..)) && target(b)) && MOP_CommonPointCut();
	before (ByteArrayOutputStream b) : ByteArrayOutputStream_FlushBeforeRetrieve_tostring(b) {
	}

	pointcut ByteArrayOutputStream_FlushBeforeRetrieve_tobytearray(ByteArrayOutputStream b) : (call(* ByteArrayOutputStream+.toByteArray(..)) && target(b)) && MOP_CommonPointCut();
	before (ByteArrayOutputStream b) : ByteArrayOutputStream_FlushBeforeRetrieve_tobytearray(b) {
	}

	pointcut ByteArrayOutputStream_FlushBeforeRetrieve_close(OutputStream o) : (call(* OutputStream+.close(..)) && target(o)) && MOP_CommonPointCut();
	before (OutputStream o) : ByteArrayOutputStream_FlushBeforeRetrieve_close(o) {
	}

	pointcut ByteArrayOutputStream_FlushBeforeRetrieve_flush(OutputStream o) : (call(* OutputStream+.flush(..)) && target(o)) && MOP_CommonPointCut();
	before (OutputStream o) : ByteArrayOutputStream_FlushBeforeRetrieve_flush(o) {
	}

	pointcut ByteArrayOutputStream_FlushBeforeRetrieve_write(OutputStream o) : (call(* OutputStream+.write*(..)) && target(o)) && MOP_CommonPointCut();
	before (OutputStream o) : ByteArrayOutputStream_FlushBeforeRetrieve_write(o) {
	}

	pointcut Collection_UnsynchronizedAddAll_modify(Collection s) : ((call(* Collection+.add*(..)) || call(* Collection+.remove*(..)) || call(* Collection+.clear(..)) || call(* Collection+.retain*(..))) && target(s)) && MOP_CommonPointCut();
	before (Collection s) : Collection_UnsynchronizedAddAll_modify(s) {
	}

	pointcut Collection_UnsynchronizedAddAll_enter(Collection t, Collection s) : (call(boolean Collection+.addAll(..)) && target(t) && args(s)) && MOP_CommonPointCut();
	before (Collection t, Collection s) : Collection_UnsynchronizedAddAll_enter(t, s) {
	}

	pointcut Throwable_InitCauseOnce_initCause(Throwable t) : (call(* Throwable+.initCause(..)) && target(t)) && MOP_CommonPointCut();
	before (Throwable t) : Throwable_InitCauseOnce_initCause(t) {
	}

	pointcut Math_ContendedRandom_onethread_use() : (call(* Math.random(..))) && MOP_CommonPointCut();
	before () : Math_ContendedRandom_onethread_use() {
		Thread t = Thread.currentThread();
		//Math_ContendedRandom_otherthread_use
		//Math_ContendedRandom_onethread_use
	}

	pointcut System_WrongKeyOrValue_nullQuery(Map map, Object o) : ((call(* Map.containsKey(..)) || call(* Map.containsValue(..)) || call(* Map.get(..)) || call(* Map.remove(..))) && target(map) && args(o)) && MOP_CommonPointCut();
	before (Map map, Object o) : System_WrongKeyOrValue_nullQuery(map, o) {
		//ProcessBuilder_NullKeyOrValue_nullQuery
		//System_WrongKeyOrValue_notStringQuery
		//System_WrongKeyOrValue_nullQuery
	}

	pointcut System_WrongKeyOrValue_nullPut_4(Map map, Map map2) : (call(* Map.putAll(Map)) && args(map2) && target(map)) && MOP_CommonPointCut();
	before (Map map, Map map2) : System_WrongKeyOrValue_nullPut_4(map, map2) {
		//ProcessBuilder_NullKeyOrValue_nullPut_8
		//System_WrongKeyOrValue_nullPut_4
	}

	pointcut System_WrongKeyOrValue_nullPut_3(Map map, Object key, Object value) : (call(* Map.put(..)) && args(key, value) && target(map)) && MOP_CommonPointCut();
	before (Map map, Object key, Object value) : System_WrongKeyOrValue_nullPut_3(map, key, value) {
		//ProcessBuilder_NullKeyOrValue_nullPut_7
		//System_WrongKeyOrValue_nullPut_3
	}

	pointcut TreeMap_Comparable_putall(Map src) : (call(* Map+.putAll(Map)) && args(src) && target(TreeMap)) && MOP_CommonPointCut();
	before (Map src) : TreeMap_Comparable_putall(src) {
	}

	pointcut TreeMap_Comparable_put(Object key) : (call(* Map+.put(Object, Object)) && args(key, ..) && target(TreeMap)) && MOP_CommonPointCut();
	before (Object key) : TreeMap_Comparable_put(key) {
	}

	pointcut TreeMap_Comparable_create(Map src) : (call(TreeMap.new(Map)) && args(src)) && MOP_CommonPointCut();
	before (Map src) : TreeMap_Comparable_create(src) {
	}

	pointcut Collections_Comparable_invalid_minmax(Collection col, Comparator comp) : ((call(* Collections.min(Collection, Comparator)) || call(* Collections.max(Collection, Comparator))) && args(col, comp)) && MOP_CommonPointCut();
	before (Collection col, Comparator comp) : Collections_Comparable_invalid_minmax(col, comp) {
	}

	pointcut Collections_Comparable_invalid_sort(List list, Comparator comp) : (call(void Collections.sort(List, Comparator)) && args(list, comp)) && MOP_CommonPointCut();
	before (List list, Comparator comp) : Collections_Comparable_invalid_sort(list, comp) {
	}

	pointcut Object_NoClone_clone(Object o) : (call(* Object.clone()) && !within(org.apache.xerces.dom.NodeImpl) && !within(org.apache.batik.bridge.UpdateManager) && !within(org.codehaus.janino.ScriptEvaluator) && !within(org.apache.batik.ext.awt.image.codec.png.PNGEncodeParam) && !within(org.apache.batik.ext.awt.image.spi.MagicNumberRegistryEntry) && !within(org.apache.xerces.util.XMLCatalogResolver) && target(o)) && MOP_CommonPointCut();
	before (Object o) : Object_NoClone_clone(o) {
	}

	pointcut ServiceLoaderIterator_Remove_remove(Iterator i) : (call(* Iterator+.remove(..)) && target(i)) && MOP_CommonPointCut();
	before (Iterator i) : ServiceLoaderIterator_Remove_remove(i) {
	}

	pointcut OutputStream_ManipulateAfterClose_close(OutputStream o) : (call(* OutputStream+.close(..)) && target(o) && !target(ByteArrayOutputStream)) && MOP_CommonPointCut();
	before (OutputStream o) : OutputStream_ManipulateAfterClose_close(o) {
	}

	pointcut OutputStream_ManipulateAfterClose_manipulate(OutputStream o) : ((call(* OutputStream+.write*(..)) || call(* OutputStream+.flush(..))) && target(o) && !target(ByteArrayOutputStream)) && MOP_CommonPointCut();
	before (OutputStream o) : OutputStream_ManipulateAfterClose_manipulate(o) {
	}

	pointcut InputStream_MarkReset_mark_or_reset() : ((call(* InputStream+.mark(..)) || call(* InputStream+.reset(..))) && (target(FileInputStream) || target(PushbackInputStream) || target(ObjectInputStream) || target(PipedInputStream) || target(SequenceInputStream))) && MOP_CommonPointCut();
	before () : InputStream_MarkReset_mark_or_reset() {
	}

	pointcut List_UnsafeListIterator_useiter(ListIterator i) : ((call(* Iterator+.hasNext(..)) || call(* ListIterator+.hasPrevious(..)) || call(* Iterator+.next(..)) || call(* ListIterator+.previous(..)) || call(* ListIterator+.nextIndex(..)) || call(* ListIterator+.previousIndex(..))) && target(i)) && MOP_CommonPointCut();
	before (ListIterator i) : List_UnsafeListIterator_useiter(i) {
	}

	pointcut List_UnsafeListIterator_modify(List l) : ((call(* Collection+.add*(..)) || call(* Collection+.clear(..)) || call(* Collection+.remove*(..)) || call(* Collection+.retain*(..))) && target(l)) && MOP_CommonPointCut();
	before (List l) : List_UnsafeListIterator_modify(l) {
		//ResourceBundleControl_MutateFormatList_mutate
		//List_UnsafeListIterator_modify
	}

	pointcut Scanner_ManipulateAfterClose_manipulate(Closeable c) : ((call(* InputStream+.read(..)) || call(* InputStream+.available(..)) || call(* InputStream+.reset(..)) || call(* InputStream+.skip(..)) || call(* Readable+.read(..)) || call(* ReadableByteChannel+.read(..))) && target(c) && !target(ByteArrayInputStream) && !target(StringBufferInputStream)) && MOP_CommonPointCut();
	before (Closeable c) : Scanner_ManipulateAfterClose_manipulate(c) {
	}

	pointcut Map_ItselfAsValue_putall(Map map, Map src) : (call(* Map+.putAll(Map)) && target(map) && args(src)) && MOP_CommonPointCut();
	before (Map map, Map src) : Map_ItselfAsValue_putall(map, src) {
		//Map_ItselfAsKey_putall
		//Map_ItselfAsValue_putall
	}

	pointcut Map_ItselfAsValue_put(Map map, Object key, Object value) : (call(* Map+.put(Object, Object)) && target(map) && args(key, value)) && MOP_CommonPointCut();
	before (Map map, Object key, Object value) : Map_ItselfAsValue_put(map, key, value) {
		//Map_ItselfAsKey_put
		//Map_ItselfAsValue_put
	}

	pointcut ServiceLoader_MultipleConcurrentThreads_gooduse(ServiceLoader s) : ((call(* ServiceLoader+.iterator()) || call(* ServiceLoader+.reload())) && target(s)) && MOP_CommonPointCut();
	before (ServiceLoader s) : ServiceLoader_MultipleConcurrentThreads_gooduse(s) {
		Thread t2 = Thread.currentThread();
		//ServiceLoader_MultipleConcurrentThreads_baduse
		//ServiceLoader_MultipleConcurrentThreads_gooduse
	}

	pointcut Arrays_Comparable_invalid_sort(Object[] arr) : (target(Arrays) && (call(void Arrays.sort(Object[])) || call(void Arrays.sort(Object[], ..))) && args(arr, ..)) && MOP_CommonPointCut();
	before (Object[] arr) : Arrays_Comparable_invalid_sort(arr) {
	}

	pointcut File_LengthOnDirectory_bad_length(File f) : (call(* File+.length()) && target(f)) && MOP_CommonPointCut();
	before (File f) : File_LengthOnDirectory_bad_length(f) {
	}

	pointcut ShutdownHook_PrematureStart_userstart(Thread t) : (call(* Thread+.start(..)) && target(t)) && MOP_CommonPointCut();
	before (Thread t) : ShutdownHook_PrematureStart_userstart(t) {
	}

	pointcut PushbackInputStream_UnreadAheadLimit_safeunread_6(PushbackInputStream p, int len) : (call(* PushbackInputStream+.unread(byte[], int, int)) && target(p) && args(.., len)) && MOP_CommonPointCut();
	before (PushbackInputStream p, int len) : PushbackInputStream_UnreadAheadLimit_safeunread_6(p, len) {
		//PushbackInputStream_UnreadAheadLimit_unsafeunread_6
		//PushbackInputStream_UnreadAheadLimit_safeunread_6
	}

	pointcut PushbackInputStream_UnreadAheadLimit_safeunread_5(PushbackInputStream p, Object b) : (call(* PushbackInputStream+.unread(byte[])) && target(p) && args(b)) && MOP_CommonPointCut();
	before (PushbackInputStream p, Object b) : PushbackInputStream_UnreadAheadLimit_safeunread_5(p, b) {
		//PushbackInputStream_UnreadAheadLimit_unsafeunread_5
		//PushbackInputStream_UnreadAheadLimit_safeunread_5
	}

	pointcut PushbackInputStream_UnreadAheadLimit_safeunread_4(PushbackInputStream p) : (call(* PushbackInputStream+.unread(int)) && target(p)) && MOP_CommonPointCut();
	before (PushbackInputStream p) : PushbackInputStream_UnreadAheadLimit_safeunread_4(p) {
		//PushbackInputStream_UnreadAheadLimit_unsafeunread_4
		//PushbackInputStream_UnreadAheadLimit_safeunread_4
	}

	pointcut Map_UnsynchronizedAddAll_modify(Map s) : ((call(* Map+.clear(..)) || call(* Map+.put*(..)) || call(* Map+.remove*(..))) && target(s)) && MOP_CommonPointCut();
	before (Map s) : Map_UnsynchronizedAddAll_modify(s) {
	}

	pointcut Map_UnsynchronizedAddAll_enter(Map t, Map s) : (call(boolean Map+.putAll(..)) && target(t) && args(s)) && MOP_CommonPointCut();
	before (Map t, Map s) : Map_UnsynchronizedAddAll_enter(t, s) {
	}

	pointcut BufferedInputStream_SynchronizedFill_fill(BufferedInputStream i) : (call(* BufferedInputStream.fill(..)) && target(i) && !cflow(call(synchronized * *.*(..)))) && MOP_CommonPointCut();
	before (BufferedInputStream i) : BufferedInputStream_SynchronizedFill_fill(i) {
	}

	pointcut Writer_ManipulateAfterClose_close(Writer w) : (call(* Writer+.close(..)) && target(w) && !target(CharArrayWriter) && !target(StringWriter)) && MOP_CommonPointCut();
	before (Writer w) : Writer_ManipulateAfterClose_close(w) {
	}

	pointcut Writer_ManipulateAfterClose_manipulate(Writer w) : ((call(* Writer+.write*(..)) || call(* Writer+.flush(..))) && target(w) && !target(CharArrayWriter) && !target(StringWriter)) && MOP_CommonPointCut();
	before (Writer w) : Writer_ManipulateAfterClose_manipulate(w) {
	}

	pointcut Arrays_MutuallyComparable_invalid_sort(Object[] arr, Comparator comp) : ((call(void Arrays.sort(Object[], Comparator)) || call(void Arrays.sort(Object[], int, int, Comparator))) && args(arr, .., comp)) && MOP_CommonPointCut();
	before (Object[] arr, Comparator comp) : Arrays_MutuallyComparable_invalid_sort(arr, comp) {
	}

	pointcut PriorityQueue_NonNull_insertnull_8(Collection c) : (call(* Collection+.addAll(Collection)) && target(PriorityQueue) && args(c)) && MOP_CommonPointCut();
	before (Collection c) : PriorityQueue_NonNull_insertnull_8(c) {
		//PriorityQueue_NonComparable_insertnull_16
		//PriorityQueue_NonNull_insertnull_8
	}

	pointcut PriorityQueue_NonNull_insertnull_7(Object e) : ((call(* Collection+.add*(..)) || call(* Queue+.offer*(..))) && target(PriorityQueue) && args(e)) && MOP_CommonPointCut();
	before (Object e) : PriorityQueue_NonNull_insertnull_7(e) {
		//PriorityQueue_NonComparable_insertnull_15
		//PriorityQueue_NonNull_insertnull_7
	}

	pointcut Comparable_CompareToNullException_badexception(Object o) : (call(* Comparable+.compareTo(..)) && args(o) && if(o == null)) && MOP_CommonPointCut();
	before (Object o) : Comparable_CompareToNullException_badexception(o) {
	}

	pointcut Closeable_MeaninglessClose_close() : (call(* Closeable+.close()) && (target(ByteArrayInputStream) || target(ByteArrayOutputStream) || target(CharArrayWriter) || target(StringWriter))) && MOP_CommonPointCut();
	before () : Closeable_MeaninglessClose_close() {
	}

	pointcut Integer_BadDecodeArg_decode(Integer i, String nm) : (call(* Integer.decode(String)) && args(nm) && target(i)) && MOP_CommonPointCut();
	before (Integer i, String nm) : Integer_BadDecodeArg_decode(i, nm) {
	}

	pointcut Scanner_SearchAfterClose_search(Scanner s) : ((call(* Scanner+.find*(..)) || call(* Scanner+.has*(..)) || call(* Scanner+.match(..)) || call(* Scanner+.next*(..)) || call(* Scanner+.skip(..))) && target(s)) && MOP_CommonPointCut();
	before (Scanner s) : Scanner_SearchAfterClose_search(s) {
	}

	pointcut Scanner_SearchAfterClose_close(Scanner s) : (call(* Scanner+.close(..)) && target(s)) && MOP_CommonPointCut();
	before (Scanner s) : Scanner_SearchAfterClose_close(s) {
	}

	pointcut InvalidPropertiesFormatException_NonSerializable_serialize(ObjectOutputStream out, InvalidPropertiesFormatException obj) : (call(* ObjectOutputStream+.writeObject(..)) && target(out) && args(obj)) && MOP_CommonPointCut();
	before (ObjectOutputStream out, InvalidPropertiesFormatException obj) : InvalidPropertiesFormatException_NonSerializable_serialize(out, obj) {
	}

	pointcut MulticastSocket_TTL_set2(int ttl) : (call(* MulticastSocket+.setTimeToLive(int)) && args(ttl)) && MOP_CommonPointCut();
	before (int ttl) : MulticastSocket_TTL_set2(ttl) {
	}

	pointcut MulticastSocket_TTL_set1(byte ttl) : (call(* MulticastSocket+.setTTL(byte)) && args(ttl)) && MOP_CommonPointCut();
	before (byte ttl) : MulticastSocket_TTL_set1(ttl) {
	}

	pointcut Iterator_RemoveOnce_next(Iterator i) : (call(* Iterator+.next()) && target(i)) && MOP_CommonPointCut();
	before (Iterator i) : Iterator_RemoveOnce_next(i) {
		//Iterator_HasNext_next
		//Iterator_RemoveOnce_next
	}

	pointcut Iterator_RemoveOnce_remove(Iterator i) : (call(void Iterator+.remove()) && target(i)) && MOP_CommonPointCut();
	before (Iterator i) : Iterator_RemoveOnce_remove(i) {
	}

	pointcut ListIterator_Set_set(ListIterator i) : (call(* ListIterator+.set(..)) && target(i)) && MOP_CommonPointCut();
	before (ListIterator i) : ListIterator_Set_set(i) {
	}

	pointcut ListIterator_Set_add(ListIterator i) : (call(void ListIterator+.add(..)) && target(i)) && MOP_CommonPointCut();
	before (ListIterator i) : ListIterator_Set_add(i) {
	}

	pointcut NavigableMap_UnsafeIterator_modifySet(Set s) : ((call(* Collection+.add(..)) || call(* Collection+.addAll(..))) && target(s)) && MOP_CommonPointCut();
	before (Set s) : NavigableMap_UnsafeIterator_modifySet(s) {
	}

	pointcut NavigableMap_UnsafeIterator_modifyMap(NavigableMap m) : ((call(* Map+.clear*(..)) || call(* Map+.put*(..)) || call(* Map+.remove*(..))) && target(m)) && MOP_CommonPointCut();
	before (NavigableMap m) : NavigableMap_UnsafeIterator_modifyMap(m) {
	}

	pointcut ShutdownHook_SystemExit_unregister(Thread t) : (call(* Runtime+.removeShutdownHook(..)) && args(t)) && MOP_CommonPointCut();
	before (Thread t) : ShutdownHook_SystemExit_unregister(t) {
		boolean MOP_skipAroundAdvice = false;
		//ShutdownHook_UnsafeAWTCall_unregister
		//ShutdownHook_PrematureStart_unregister
		//ShutdownHook_UnsafeSwingCall_unregister
		//ShutdownHook_SystemExit_unregister
	}

	pointcut ShutdownHook_SystemExit_register(Thread t) : (call(* Runtime+.addShutdownHook(..)) && args(t)) && MOP_CommonPointCut();
	before (Thread t) : ShutdownHook_SystemExit_register(t) {
		boolean MOP_skipAroundAdvice = false;
		//ShutdownHook_UnsafeAWTCall_register
		//ShutdownHook_PrematureStart_bad_register
		//ShutdownHook_PrematureStart_good_register
		//ShutdownHook_UnsafeSwingCall_register
		//ShutdownHook_SystemExit_register
	}

	pointcut EnumSet_NonNull_insertnull_4(Collection c) : (call(* EnumSet+.addAll(Collection)) && args(c)) && MOP_CommonPointCut();
	before (Collection c) : EnumSet_NonNull_insertnull_4(c) {
	}

	pointcut EnumSet_NonNull_insertnull_3(Object e) : (call(* EnumSet+.add(Object)) && args(e)) && MOP_CommonPointCut();
	before (Object e) : EnumSet_NonNull_insertnull_3(e) {
	}

	pointcut Dictionary_Obsolete_use(Dictionary d) : (call(* Dictionary+.*(..)) && target(d)) && MOP_CommonPointCut();
	before (Dictionary d) : Dictionary_Obsolete_use(d) {
	}

	pointcut URLDecoder_DecodeUTF8_decode(String enc) : (call(* URLDecoder.decode(String, String)) && args(*, enc)) && MOP_CommonPointCut();
	before (String enc) : URLDecoder_DecodeUTF8_decode(enc) {
	}

	pointcut ServerSocket_Port_construct_port(int port) : ((call(ServerSocket.new(int)) || call(ServerSocket.new(int, int)) || call(ServerSocket.new(int, int, InetAddress))) && args(port, ..)) && MOP_CommonPointCut();
	before (int port) : ServerSocket_Port_construct_port(port) {
	}

	pointcut Collections_NewSetFromMap_access(Map map) : (call(* Map+.*(..)) && target(map)) && MOP_CommonPointCut();
	before (Map map) : Collections_NewSetFromMap_access(map) {
	}

	pointcut Collections_NewSetFromMap_create(Map map) : (call(* Collections.newSetFromMap(Map)) && args(map)) && MOP_CommonPointCut();
	before (Map map) : Collections_NewSetFromMap_create(map) {
		//Collections_NewSetFromMap_bad_create
		//Collections_NewSetFromMap_create
	}

	pointcut Map_CollectionViewAdd_add(Collection c) : ((call(* Collection+.add(..)) || call(* Collection+.addAll(..))) && target(c)) && MOP_CommonPointCut();
	before (Collection c) : Map_CollectionViewAdd_add(c) {
		//NavigableMap_Modification_modify3
		//Map_CollectionViewAdd_add
	}

	pointcut Socket_SetTimeoutBeforeBlockingOutput_set(Socket sock, int timeout) : (call(* Socket+.setSoTimeout(int)) && target(sock) && args(timeout)) && MOP_CommonPointCut();
	before (Socket sock, int timeout) : Socket_SetTimeoutBeforeBlockingOutput_set(sock, timeout) {
		//Socket_SetTimeoutBeforeBlockingInput_set
		//Socket_SetTimeoutBeforeBlockingOutput_set
	}

	pointcut Socket_SetTimeoutBeforeBlockingOutput_enter(OutputStream output) : (call(* OutputStream+.write(..)) && target(output)) && MOP_CommonPointCut();
	before (OutputStream output) : Socket_SetTimeoutBeforeBlockingOutput_enter(output) {
	}

	pointcut Arrays_DeepHashCode_invalid_deephashcode(Object[] arr) : (call(int Arrays.deepHashCode(Object[])) && args(arr)) && MOP_CommonPointCut();
	before (Object[] arr) : Arrays_DeepHashCode_invalid_deephashcode(arr) {
	}

	pointcut URLConnection_Connect_implicit(URLConnection c) : ((call(* URLConnection+.getContent(..)) || call(* URLConnection+.getContentEncoding(..)) || call(* URLConnection+.getContentLength(..)) || call(* URLConnection+.getContentType(..)) || call(* URLConnection+.getDate(..)) || call(* URLConnection+.getExpiration(..)) || call(* URLConnection+.getHeaderField(..)) || call(* URLConnection+.getHeaderFieldInt(..)) || call(* URLConnection+.getHeaderFields(..)) || call(* URLConnection+.getInputStream(..)) || call(* URLConnection+.getLastModified(..)) || call(* URLConnection+.getOutputStream(..))) && target(c)) && MOP_CommonPointCut();
	before (URLConnection c) : URLConnection_Connect_implicit(c) {
	}

	pointcut URLConnection_Connect_explicit(URLConnection c) : (call(* URLConnection+.connect(..)) && target(c)) && MOP_CommonPointCut();
	before (URLConnection c) : URLConnection_Connect_explicit(c) {
	}

	pointcut Thread_SetDaemonBeforeStart_setDaemon(Thread t) : (call(* Thread+.setDaemon(..)) && target(t)) && MOP_CommonPointCut();
	before (Thread t) : Thread_SetDaemonBeforeStart_setDaemon(t) {
	}

	pointcut NavigableSet_Modification_useiter(Iterator i) : ((call(* Iterator.hasNext(..)) || call(* Iterator.next(..))) && target(i)) && MOP_CommonPointCut();
	before (Iterator i) : NavigableSet_Modification_useiter(i) {
		//Map_UnsafeIterator_useiter
		//NavigableMap_Modification_useiter
		//Collection_UnsafeIterator_useiter
		//NavigableMap_UnsafeIterator_useiter
		//NavigableSet_Modification_useiter
	}

	pointcut NavigableSet_Modification_modify2(NavigableSet s2) : ((call(* Collection+.add*(..)) || call(* Collection+.clear(..)) || call(* Collection+.remove*(..)) || call(* Collection+.retain*(..))) && target(s2)) && MOP_CommonPointCut();
	before (NavigableSet s2) : NavigableSet_Modification_modify2(s2) {
	}

	pointcut NavigableSet_Modification_modify1(NavigableSet s1) : ((call(* Collection+.add*(..)) || call(* Collection+.clear(..)) || call(* Collection+.remove*(..)) || call(* Collection+.retain*(..))) && target(s1)) && MOP_CommonPointCut();
	before (NavigableSet s1) : NavigableSet_Modification_modify1(s1) {
	}

	pointcut ServerSocket_Backlog_set(int backlog) : (call(* ServerSocket+.bind(SocketAddress, int)) && args(*, backlog)) && MOP_CommonPointCut();
	before (int backlog) : ServerSocket_Backlog_set(backlog) {
	}

	pointcut ServerSocket_Backlog_construct(int backlog) : ((call(ServerSocket.new(int, int)) || call(ServerSocket.new(int, int, InetAddress))) && args(*, backlog, ..)) && MOP_CommonPointCut();
	before (int backlog) : ServerSocket_Backlog_construct(backlog) {
	}

	pointcut Byte_BadDecodeArg_decode(Byte b, String nm) : (call(* Byte.decode(String)) && target(b) && args(nm)) && MOP_CommonPointCut();
	before (Byte b, String nm) : Byte_BadDecodeArg_decode(b, nm) {
	}

	pointcut DatagramSocket_Port_construct_port(int port) : ((call(DatagramSocket.new(int)) || call(DatagramSocket.new(int, InetAddress))) && args(port, ..)) && MOP_CommonPointCut();
	before (int port) : DatagramSocket_Port_construct_port(port) {
	}

	pointcut DatagramSocket_SoTimeout_settimeout(int timeout) : (call(void DatagramSocket.setSoTimeout(int)) && args(timeout)) && MOP_CommonPointCut();
	before (int timeout) : DatagramSocket_SoTimeout_settimeout(timeout) {
	}

	pointcut ServerSocket_ReuseAddress_set(ServerSocket sock) : (call(* ServerSocket+.setReuseAddress(..)) && target(sock)) && MOP_CommonPointCut();
	before (ServerSocket sock) : ServerSocket_ReuseAddress_set(sock) {
	}

	pointcut ServerSocket_LargeReceiveBuffer_set(ServerSocket sock, int size) : (call(* ServerSocket+.setReceiveBufferSize(int)) && target(sock) && args(size)) && MOP_CommonPointCut();
	before (ServerSocket sock, int size) : ServerSocket_LargeReceiveBuffer_set(sock, size) {
	}

	pointcut Object_MonitorOwner_bad_wait(Object o) : (call(* Object+.wait(..)) && target(o) && if(!Thread.holdsLock(o))) && MOP_CommonPointCut();
	before (Object o) : Object_MonitorOwner_bad_wait(o) {
	}

	pointcut Object_MonitorOwner_bad_notify(Object o) : ((call(* Object+.notify(..)) || call(* Object+.notifyAll(..))) && target(o) && if(!Thread.holdsLock(o))) && MOP_CommonPointCut();
	before (Object o) : Object_MonitorOwner_bad_notify(o) {
	}

	pointcut CharSequence_NotInMap_map_putall(Map map, Map m) : (call(* Map+.putAll(Map)) && args(m) && target(map)) && MOP_CommonPointCut();
	before (Map map, Map m) : CharSequence_NotInMap_map_putall(map, m) {
	}

	pointcut CharSequence_NotInMap_map_put(Map map) : (call(* Map+.put(..)) && target(map) && args(CharSequence, ..) && !args(String, ..) && !args(CharBuffer, ..)) && MOP_CommonPointCut();
	before (Map map) : CharSequence_NotInMap_map_put(map) {
	}

	pointcut Collections_UnnecessaryNewSetFromMap_unnecessary() : (call(* Collections.newSetFromMap(Map)) && (args(HashMap) || args(TreeMap))) && MOP_CommonPointCut();
	before () : Collections_UnnecessaryNewSetFromMap_unnecessary() {
	}

	pointcut InetAddress_IsReachable_isreachable_4(int ttl, int timeout) : (call(* InetAddress+.isReachable(NetworkInterface, int, int)) && args(*, ttl, timeout)) && MOP_CommonPointCut();
	before (int ttl, int timeout) : InetAddress_IsReachable_isreachable_4(ttl, timeout) {
	}

	pointcut InetAddress_IsReachable_isreachable_3(int timeout) : (call(* InetAddress+.isReachable(int)) && args(timeout)) && MOP_CommonPointCut();
	before (int timeout) : InetAddress_IsReachable_isreachable_3(timeout) {
	}

	pointcut ListIterator_hasNextPrevious_previous(ListIterator i) : (call(* ListIterator.previous()) && target(i)) && MOP_CommonPointCut();
	before (ListIterator i) : ListIterator_hasNextPrevious_previous(i) {
	}

	pointcut ListIterator_hasNextPrevious_next(ListIterator i) : (call(* ListIterator.next()) && target(i)) && MOP_CommonPointCut();
	before (ListIterator i) : ListIterator_hasNextPrevious_next(i) {
	}

	pointcut HttpURLConnection_SetBeforeConnect_connect(HttpURLConnection c) : ((call(* URLConnection+.connect(..)) || call(* URLConnection+.getContent(..)) || call(* URLConnection+.getContentEncoding(..)) || call(* URLConnection+.getContentLength(..)) || call(* URLConnection+.getContentType(..)) || call(* URLConnection+.getDate(..)) || call(* URLConnection+.getExpiration(..)) || call(* URLConnection+.getHeaderField(..)) || call(* URLConnection+.getHeaderFieldInt(..)) || call(* URLConnection+.getHeaderFields(..)) || call(* URLConnection+.getInputStream(..)) || call(* URLConnection+.getLastModified(..)) || call(* URLConnection+.getOutputStream(..)) || call(* HttpURLConnection+.getErrorStream(..)) || call(* HttpURLConnection+.getHeaderFieldDate(..)) || call(* HttpURLConnection+.getHeaderFieldKey(..)) || call(* HttpURLConnection+.getResponseCode(..)) || call(* HttpURLConnection+.getResponseMessage(..))) && target(c)) && MOP_CommonPointCut();
	before (HttpURLConnection c) : HttpURLConnection_SetBeforeConnect_connect(c) {
	}

	pointcut HttpURLConnection_SetBeforeConnect_set(HttpURLConnection c) : ((call(* HttpURLConnection+.setFixedLengthStreamingMode(..)) || call(* HttpURLConnection+.setChunkedStreamingMode(..)) || call(* HttpURLConnection+.setRequestMethod(..))) && target(c)) && MOP_CommonPointCut();
	before (HttpURLConnection c) : HttpURLConnection_SetBeforeConnect_set(c) {
	}

	pointcut NetPermission_Name_construct(String name) : ((call(NetPermission.new(String)) || call(NetPermission.new(String, String))) && args(name, ..)) && MOP_CommonPointCut();
	before (String name) : NetPermission_Name_construct(name) {
	}

	pointcut Reader_ManipulateAfterClose_close(Reader r) : (call(* Reader+.close(..)) && target(r)) && MOP_CommonPointCut();
	before (Reader r) : Reader_ManipulateAfterClose_close(r) {
		//Console_CloseReader_close
		//Reader_ManipulateAfterClose_close
	}

	pointcut Reader_ManipulateAfterClose_manipulate(Reader r) : ((call(* Reader+.read(..)) || call(* Reader+.ready(..)) || call(* Reader+.mark(..)) || call(* Reader+.reset(..)) || call(* Reader+.skip(..))) && target(r)) && MOP_CommonPointCut();
	before (Reader r) : Reader_ManipulateAfterClose_manipulate(r) {
	}

	pointcut ArrayDeque_NonNull_insertnull(Object e) : ((call(* ArrayDeque.add*(..)) || call(* ArrayDeque.offer*(..)) || call(* ArrayDeque.push(..))) && args(Object+) && args(e)) && MOP_CommonPointCut();
	before (Object e) : ArrayDeque_NonNull_insertnull(e) {
	}

	pointcut Console_FillZeroPassword_obliterate(Object pwd) : (call(* Arrays.fill(char[], char)) && args(pwd, ..)) && MOP_CommonPointCut();
	before (Object pwd) : Console_FillZeroPassword_obliterate(pwd) {
		//PasswordAuthentication_FillZeroPassword_obliterate
		//Console_FillZeroPassword_obliterate
	}

	pointcut ServerSocket_PerformancePreferences_set(ServerSocket sock) : (call(* ServerSocket+.setPerformancePreferences(..)) && target(sock)) && MOP_CommonPointCut();
	before (ServerSocket sock) : ServerSocket_PerformancePreferences_set(sock) {
	}

	pointcut ServerSocket_PerformancePreferences_bind(ServerSocket sock) : (call(* ServerSocket+.bind(..)) && target(sock)) && MOP_CommonPointCut();
	before (ServerSocket sock) : ServerSocket_PerformancePreferences_bind(sock) {
		//ServerSocket_ReuseAddress_bind
		//ServerSocket_LargeReceiveBuffer_bind
		//ServerSocket_PerformancePreferences_bind
	}

	pointcut ListIterator_RemoveOnce_previous(ListIterator i) : (call(* ListIterator+.previous()) && target(i)) && MOP_CommonPointCut();
	before (ListIterator i) : ListIterator_RemoveOnce_previous(i) {
		//ListIterator_Set_previous
		//ListIterator_RemoveOnce_previous
	}

	pointcut ListIterator_RemoveOnce_next(ListIterator i) : (call(* Iterator+.next()) && target(i)) && MOP_CommonPointCut();
	before (ListIterator i) : ListIterator_RemoveOnce_next(i) {
		//ListIterator_Set_next
		//ListIterator_RemoveOnce_next
	}

	pointcut ListIterator_RemoveOnce_remove(ListIterator i) : (call(void Iterator+.remove()) && target(i)) && MOP_CommonPointCut();
	before (ListIterator i) : ListIterator_RemoveOnce_remove(i) {
		//ListIterator_Set_remove
		//ListIterator_RemoveOnce_remove
	}

	pointcut Enum_NoExtraWhiteSpace_valueOf(Class c, String name) : (call(* Enum+.valueOf(Class, String)) && args(c, name)) && MOP_CommonPointCut();
	before (Class c, String name) : Enum_NoExtraWhiteSpace_valueOf(c, name) {
	}

	pointcut SocketPermission_Actions_construct(String actions) : (call(SocketPermission.new(String, String)) && args(*, actions)) && MOP_CommonPointCut();
	before (String actions) : SocketPermission_Actions_construct(actions) {
	}

	pointcut ServerSocket_SetTimeoutBeforeBlocking_set(ServerSocket sock, int timeout) : (call(* ServerSocket+.setSoTimeout(int)) && target(sock) && args(timeout)) && MOP_CommonPointCut();
	before (ServerSocket sock, int timeout) : ServerSocket_SetTimeoutBeforeBlocking_set(sock, timeout) {
	}

	pointcut ServerSocket_SetTimeoutBeforeBlocking_enter(ServerSocket sock) : (call(* ServerSocket+.accept(..)) && target(sock)) && MOP_CommonPointCut();
	before (ServerSocket sock) : ServerSocket_SetTimeoutBeforeBlocking_enter(sock) {
	}

	pointcut URL_SetURLStreamHandlerFactory_set() : (call(* URL.setURLStreamHandlerFactory(..))) && MOP_CommonPointCut();
	before () : URL_SetURLStreamHandlerFactory_set() {
	}

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

	after (ServerSocket sock) : ServerSocket_SetTimeoutBeforeBlocking_enter(sock) {
	}

	pointcut Long_StaticFactory_constructor_create() : (call(Long+.new(long))) && MOP_CommonPointCut();
	after () returning (Long l) : Long_StaticFactory_constructor_create() {
	}

	pointcut ServerSocket_PerformancePreferences_create_bound() : (call(ServerSocket.new(int, ..))) && MOP_CommonPointCut();
	after () returning (ServerSocket sock) : ServerSocket_PerformancePreferences_create_bound() {
		//ServerSocket_PerformancePreferences_create_bound
		//ServerSocket_LargeReceiveBuffer_create_bound
		//ServerSocket_ReuseAddress_create_bound
	}

	pointcut ServerSocket_PerformancePreferences_create_unbound() : (call(ServerSocket.new())) && MOP_CommonPointCut();
	after () returning (ServerSocket sock) : ServerSocket_PerformancePreferences_create_unbound() {
		//ServerSocket_PerformancePreferences_create_unbound
		//ServerSocket_LargeReceiveBuffer_create_unbound
		//ServerSocket_ReuseAddress_create_unbound
	}

	pointcut Short_StaticFactory_constructor_create() : (call(Short+.new(short))) && MOP_CommonPointCut();
	after () returning (Short l) : Short_StaticFactory_constructor_create() {
	}

	pointcut Console_FillZeroPassword_read() : (call(char[] Console+.readPassword(..))) && MOP_CommonPointCut();
	after () returning (Object pwd) : Console_FillZeroPassword_read() {
	}

	pointcut RuntimePermission_PermName_constructor_runtimeperm(String name) : (call(RuntimePermission.new(String)) && args(name)) && MOP_CommonPointCut();
	after (String name) returning (RuntimePermission r) : RuntimePermission_PermName_constructor_runtimeperm(name) {
	}

	after (Object o) throwing (Exception e) : Comparable_CompareToNullException_badexception(o) {
	}

	after (Object o) returning (int i) : Comparable_CompareToNullException_badexception(o) {
	}

	pointcut ListIterator_hasNextPrevious_hasnexttrue(ListIterator i) : (call(* ListIterator.hasNext()) && target(i)) && MOP_CommonPointCut();
	after (ListIterator i) returning (boolean b) : ListIterator_hasNextPrevious_hasnexttrue(i) {
		//ListIterator_hasNextPrevious_hasnexttrue
		//ListIterator_hasNextPrevious_hasnextfalse
	}

	pointcut ListIterator_hasNextPrevious_hasprevioustrue(ListIterator i) : (call(* ListIterator.hasPrevious()) && target(i)) && MOP_CommonPointCut();
	after (ListIterator i) returning (boolean b) : ListIterator_hasNextPrevious_hasprevioustrue(i) {
		//ListIterator_hasNextPrevious_hasprevioustrue
		//ListIterator_hasNextPrevious_haspreviousfalse
	}

	pointcut Byte_StaticFactory_constructor_create() : (call(Byte+.new(byte))) && MOP_CommonPointCut();
	after () returning (Byte b) : Byte_StaticFactory_constructor_create() {
	}

	pointcut RuntimePermission_NullAction_constructor_runtimeperm(String name, String actions) : (call(RuntimePermission.new(String, String)) && args(name, actions)) && MOP_CommonPointCut();
	after (String name, String actions) returning (RuntimePermission r) : RuntimePermission_NullAction_constructor_runtimeperm(name, actions) {
	}

	pointcut NavigableSet_Modification_create(NavigableSet s1) : (call(NavigableSet NavigableSet+.descendingSet()) && target(s1)) && MOP_CommonPointCut();
	after (NavigableSet s1) returning (NavigableSet s2) : NavigableSet_Modification_create(s1) {
	}

	pointcut NavigableSet_Modification_getiter1(NavigableSet s1) : (call(Iterator Iterable+.iterator()) && target(s1)) && MOP_CommonPointCut();
	after (NavigableSet s1) returning (Iterator i) : NavigableSet_Modification_getiter1(s1) {
	}

	pointcut NavigableSet_Modification_getiter2(NavigableSet s2) : (call(Iterator Iterable+.iterator()) && target(s2)) && MOP_CommonPointCut();
	after (NavigableSet s2) returning (Iterator i) : NavigableSet_Modification_getiter2(s2) {
	}

	pointcut Socket_SetTimeoutBeforeBlockingOutput_getoutput(Socket sock) : (call(OutputStream Socket+.getOutputStream()) && target(sock)) && MOP_CommonPointCut();
	after (Socket sock) returning (OutputStream output) : Socket_SetTimeoutBeforeBlockingOutput_getoutput(sock) {
		//Socket_SetTimeoutBeforeBlockingOutput_getoutput
		//Socket_CloseOutput_getoutput
	}

	after (OutputStream output) : Socket_SetTimeoutBeforeBlockingOutput_enter(output) {
	}

	pointcut Map_CollectionViewAdd_getset(Map m) : ((call(Set Map+.keySet()) || call(Set Map+.entrySet()) || call(Collection Map+.values())) && target(m)) && MOP_CommonPointCut();
	after (Map m) returning (Collection c) : Map_CollectionViewAdd_getset(m) {
		//Map_CollectionViewAdd_getset
		//Map_UnsafeIterator_getset
	}

	pointcut ShutdownHook_SystemExit_exit() : (call(* System.exit(..))) && MOP_CommonPointCut();
	void around () : ShutdownHook_SystemExit_exit() {
		boolean MOP_skipAroundAdvice = false;
		Thread t = Thread.currentThread();
		if(MOP_skipAroundAdvice){
			return;
		} else {
			proceed();
		}
	}

	pointcut ShutdownHook_UnsafeSwingCall_swingcall1() : (call(* SwingUtilities+.invokeAndWait(..)) || call(* SwingUtilities+.invokeLater(..)) || call(* SwingWorker+.execute(..))) && MOP_CommonPointCut();
	void around () : ShutdownHook_UnsafeSwingCall_swingcall1() {
		boolean MOP_skipAroundAdvice = false;
		Thread t = Thread.currentThread();
		if(MOP_skipAroundAdvice){
			return;
		} else {
			proceed();
		}
	}

	pointcut ShutdownHook_UnsafeSwingCall_swingcall2() : (call(* SwingWorker+.get(..))) && MOP_CommonPointCut();
	Object around () : ShutdownHook_UnsafeSwingCall_swingcall2() {
		boolean MOP_skipAroundAdvice = false;
		Thread t = Thread.currentThread();
		if(MOP_skipAroundAdvice){
			return null;
		} else {
			return proceed();
		}
	}

	pointcut NavigableMap_UnsafeIterator_getset(NavigableMap m) : ((call(Set NavigableMap+.navigableKeySet()) || call(Set NavigableMap+.descendingKeySet())) && target(m)) && MOP_CommonPointCut();
	after (NavigableMap m) returning (Set s) : NavigableMap_UnsafeIterator_getset(m) {
	}

	pointcut NavigableMap_UnsafeIterator_getiter(Set s) : (call(Iterator Iterable+.iterator()) && target(s)) && MOP_CommonPointCut();
	after (Set s) returning (Iterator i) : NavigableMap_UnsafeIterator_getiter(s) {
	}

	pointcut ListIterator_Set_create() : (call(ListIterator Iterable+.listIterator())) && MOP_CommonPointCut();
	after () returning (ListIterator i) : ListIterator_Set_create() {
	}

	pointcut InvalidPropertiesFormatException_NonSerializable_deserialize(ObjectInputStream in) : (call(* ObjectInputStream+.readObject(..)) && target(in)) && MOP_CommonPointCut();
	after (ObjectInputStream in) returning (InvalidPropertiesFormatException obj) : InvalidPropertiesFormatException_NonSerializable_deserialize(in) {
	}

	pointcut PasswordAuthentication_FillZeroPassword_read() : (call(char[] PasswordAuthentication+.getPassword(..))) && MOP_CommonPointCut();
	after () returning (Object pwd) : PasswordAuthentication_FillZeroPassword_read() {
	}

	pointcut Boolean_StaticFactory_constructor_create() : (call(Boolean+.new(boolean))) && MOP_CommonPointCut();
	after () returning (Boolean b) : Boolean_StaticFactory_constructor_create() {
	}

	pointcut SortedMap_StandardConstructors_staticinit() : (staticinitialization(SortedMap+)) && MOP_CommonPointCut();
	after () : SortedMap_StandardConstructors_staticinit() {
	}

	pointcut Map_UnsynchronizedAddAll_leave(Map t, Map s) : (call(void Map+.putAll(..)) && target(t) && args(s)) && MOP_CommonPointCut();
	after (Map t, Map s) : Map_UnsynchronizedAddAll_leave(t, s) {
	}

	pointcut PushbackInputStream_UnreadAheadLimit_create_3() : (call(PushbackInputStream+.new(InputStream))) && MOP_CommonPointCut();
	after () returning (PushbackInputStream p) : PushbackInputStream_UnreadAheadLimit_create_3() {
	}

	pointcut PushbackInputStream_UnreadAheadLimit_create_4(int size) : (call(PushbackInputStream+.new(InputStream, int)) && args(.., size)) && MOP_CommonPointCut();
	after (int size) returning (PushbackInputStream p) : PushbackInputStream_UnreadAheadLimit_create_4(size) {
	}

	pointcut PushbackInputStream_UnreadAheadLimit_read1(PushbackInputStream p) : (call(* PushbackInputStream+.read()) && target(p)) && MOP_CommonPointCut();
	after (PushbackInputStream p) returning (int r) : PushbackInputStream_UnreadAheadLimit_read1(p) {
	}

	pointcut PushbackInputStream_UnreadAheadLimit_read2(PushbackInputStream p) : (call(* PushbackInputStream+.read(byte[], int, int)) && target(p)) && MOP_CommonPointCut();
	after (PushbackInputStream p) returning (int n) : PushbackInputStream_UnreadAheadLimit_read2(p) {
	}

	pointcut ServiceLoader_MultipleConcurrentThreads_create() : (call(ServiceLoader ServiceLoader+.load*(..))) && MOP_CommonPointCut();
	after () returning (ServiceLoader s) : ServiceLoader_MultipleConcurrentThreads_create() {
		Thread t2 = Thread.currentThread();
	}

	pointcut Scanner_ManipulateAfterClose_create(Closeable c) : ((call(Scanner+.new(InputStream, ..)) || call(Scanner+.new(Readable, ..)) || call(Scanner+.new(ReadableByteChannel, ..))) && args(c, ..)) && MOP_CommonPointCut();
	after (Closeable c) returning (Scanner s) : Scanner_ManipulateAfterClose_create(c) {
	}

	pointcut Scanner_ManipulateAfterClose_close(Scanner s) : (call(* Scanner+.close()) && target(s) && !args(ByteArrayInputStream) && !args(StringBufferInputStream)) && MOP_CommonPointCut();
	after (Scanner s) : Scanner_ManipulateAfterClose_close(s) {
	}

	pointcut List_UnsafeListIterator_create(List l) : (call(ListIterator List+.listIterator()) && target(l)) && MOP_CommonPointCut();
	after (List l) returning (ListIterator i) : List_UnsafeListIterator_create(l) {
	}

	pointcut ServiceLoaderIterator_Remove_create(ServiceLoader s) : (call(Iterator ServiceLoader.iterator()) && target(s)) && MOP_CommonPointCut();
	after (ServiceLoader s) returning (Iterator i) : ServiceLoaderIterator_Remove_create(s) {
	}

	pointcut Runnable_OverrideRun_staticinit() : (staticinitialization(Runnable+)) && MOP_CommonPointCut();
	after () : Runnable_OverrideRun_staticinit() {
	}

	pointcut Serializable_UID_staticinit() : (staticinitialization(Serializable+)) && MOP_CommonPointCut();
	after () : Serializable_UID_staticinit() {
		//Serializable_UID_staticinit
		//Serializable_NoArgConstructor_staticinit
	}

	pointcut System_WrongKeyOrValue_createMap() : (call(Map System.getenv())) && MOP_CommonPointCut();
	after () returning (Map map) : System_WrongKeyOrValue_createMap() {
	}

	pointcut Throwable_InitCauseOnce_createWithoutThrowable() : (call(Throwable+.new()) || call(Throwable+.new(String))) && MOP_CommonPointCut();
	after () returning (Throwable t) : Throwable_InitCauseOnce_createWithoutThrowable() {
	}

	pointcut Throwable_InitCauseOnce_createWithThrowable() : (call(Throwable+.new(String, Throwable)) || call(Throwable+.new(Throwable))) && MOP_CommonPointCut();
	after () returning (Throwable t) : Throwable_InitCauseOnce_createWithThrowable() {
	}

	after (Collection t, Collection s) : Collection_UnsynchronizedAddAll_enter(t, s) {
	}

	pointcut ByteArrayOutputStream_FlushBeforeRetrieve_outputstreaminit(ByteArrayOutputStream b) : (call(OutputStream+.new(..)) && args(b, ..)) && MOP_CommonPointCut();
	after (ByteArrayOutputStream b) returning (OutputStream o) : ByteArrayOutputStream_FlushBeforeRetrieve_outputstreaminit(b) {
	}

	pointcut Character_StaticFactory_constructor_create() : (call(Character+.new(char))) && MOP_CommonPointCut();
	after () returning (Character b) : Character_StaticFactory_constructor_create() {
	}

	pointcut Socket_SetTimeoutBeforeBlockingInput_getinput(Socket sock) : (call(InputStream Socket+.getInputStream()) && target(sock)) && MOP_CommonPointCut();
	after (Socket sock) returning (InputStream input) : Socket_SetTimeoutBeforeBlockingInput_getinput(sock) {
		//Socket_SetTimeoutBeforeBlockingInput_getinput
		//Socket_CloseInput_getinput
	}

	after (InputStream input) : Socket_SetTimeoutBeforeBlockingInput_enter(input) {
	}

	pointcut SortedSet_StandardConstructors_staticinit() : (staticinitialization(SortedSet+)) && MOP_CommonPointCut();
	after () : SortedSet_StandardConstructors_staticinit() {
	}

	pointcut ShutdownHook_UnsafeAWTCall_awtcall() : (call(* EventQueue.invokeAndWait(..)) || call(* EventQueue.invokeLater(..))) && MOP_CommonPointCut();
	void around () : ShutdownHook_UnsafeAWTCall_awtcall() {
		boolean MOP_skipAroundAdvice = false;
		Thread t = Thread.currentThread();
		if(MOP_skipAroundAdvice){
			return;
		} else {
			proceed();
		}
	}

	pointcut Collections_SynchronizedCollection_sync() : (call(* Collections.synchronizedCollection(Collection)) || call(* Collections.synchronizedSet(Set)) || call(* Collections.synchronizedSortedSet(SortedSet)) || call(* Collections.synchronizedList(List))) && MOP_CommonPointCut();
	after () returning (Collection col) : Collections_SynchronizedCollection_sync() {
	}

	pointcut Collections_SynchronizedCollection_syncCreateIter(Collection col) : (call(* Collection+.iterator()) && target(col)) && MOP_CommonPointCut();
	after (Collection col) returning (Iterator iter) : Collections_SynchronizedCollection_syncCreateIter(col) {
		//Collections_SynchronizedCollection_syncCreateIter
		//Collections_SynchronizedCollection_asyncCreateIter
		//Collections_SynchronizedMap_syncCreateIter
		//Collections_SynchronizedMap_asyncCreateIter
	}

	pointcut Socket_OutputStreamUnavailable_create_connected() : (call(Socket.new(InetAddress, int)) || call(Socket.new(InetAddress, int, boolean)) || call(Socket.new(InetAddress, int, InetAddress, int)) || call(Socket.new(String, int)) || call(Socket.new(String, int, boolean)) || call(Socket.new(String, int, InetAddress, int))) && MOP_CommonPointCut();
	after () returning (Socket sock) : Socket_OutputStreamUnavailable_create_connected() {
		//Socket_OutputStreamUnavailable_create_connected
		//Socket_InputStreamUnavailable_create_connected
		//Socket_PerformancePreferences_create_connected
		//Socket_ReuseAddress_create_connected
		//Socket_LargeReceiveBuffer_create_connected
	}

	pointcut Socket_OutputStreamUnavailable_create_unconnected() : (call(Socket.new()) || call(Socket.new(Proxy)) || call(Socket.new(SocketImpl))) && MOP_CommonPointCut();
	after () returning (Socket sock) : Socket_OutputStreamUnavailable_create_unconnected() {
		//Socket_OutputStreamUnavailable_create_unconnected
		//Socket_InputStreamUnavailable_create_unconnected
		//Socket_PerformancePreferences_create_unconnected
		//Socket_ReuseAddress_create_unconnected
		//Socket_LargeReceiveBuffer_create_unconnected
	}

	pointcut StreamTokenizer_AccessInvalidField_nexttoken_word(StreamTokenizer s) : (call(* StreamTokenizer+.nextToken(..)) && target(s)) && MOP_CommonPointCut();
	after (StreamTokenizer s) returning (int t) : StreamTokenizer_AccessInvalidField_nexttoken_word(s) {
		//StreamTokenizer_AccessInvalidField_nexttoken_word
		//StreamTokenizer_AccessInvalidField_nexttoken_num
		//StreamTokenizer_AccessInvalidField_nexttoken_eol
		//StreamTokenizer_AccessInvalidField_nexttoken_eof
	}

	pointcut Random_OverrideNext_staticinit() : (staticinitialization(Random+)) && MOP_CommonPointCut();
	after () : Random_OverrideNext_staticinit() {
	}

	pointcut PipedOutputStream_UnconnectedWrite_create() : (call(PipedOutputStream+.new())) && MOP_CommonPointCut();
	after () returning (PipedOutputStream o) : PipedOutputStream_UnconnectedWrite_create() {
	}

	pointcut PipedOutputStream_UnconnectedWrite_create_io() : (call(PipedOutputStream+.new(PipedInputStream+))) && MOP_CommonPointCut();
	after () returning (PipedOutputStream o) : PipedOutputStream_UnconnectedWrite_create_io() {
	}

	pointcut Properties_ManipulateAfterLoad_close(InputStream i) : (call(* Properties+.loadFromXML(InputStream)) && args(i) && !args(ByteArrayInputStream) && !args(StringBufferInputStream)) && MOP_CommonPointCut();
	after (InputStream i) : Properties_ManipulateAfterLoad_close(i) {
	}

	pointcut Iterator_HasNext_hasnexttrue(Iterator i) : (call(* Iterator+.hasNext()) && target(i)) && MOP_CommonPointCut();
	after (Iterator i) returning (boolean b) : Iterator_HasNext_hasnexttrue(i) {
		//Iterator_HasNext_hasnexttrue
		//Iterator_HasNext_hasnextfalse
	}

	pointcut ObjectOutput_Close_create() : (call(ObjectOutput+.new(..))) && MOP_CommonPointCut();
	after () returning (ObjectOutput o) : ObjectOutput_Close_create() {
	}

	pointcut List_UnsynchronizedSubList_createsublist(List b) : (call(* List.subList(..)) && target(b)) && MOP_CommonPointCut();
	after (List b) returning (List s) : List_UnsynchronizedSubList_createsublist(b) {
	}

	pointcut Collection_UnsafeIterator_create(Collection c) : (call(Iterator Iterable+.iterator()) && target(c)) && MOP_CommonPointCut();
	after (Collection c) returning (Iterator i) : Collection_UnsafeIterator_create(c) {
		//Collection_UnsafeIterator_create
		//NavigableMap_Modification_getiter
		//Map_UnsafeIterator_getiter
	}

	pointcut File_DeleteTempFile_create() : (call(File+ File.createTempFile(..))) && MOP_CommonPointCut();
	after () returning (File f) : File_DeleteTempFile_create() {
	}

	pointcut String_UseStringBuilder_constructor_create() : (call(String.new(StringBuilder))) && MOP_CommonPointCut();
	after () returning (String b) : String_UseStringBuilder_constructor_create() {
	}

	pointcut ObjectInput_Close_create() : (call(ObjectInput+.new(..))) && MOP_CommonPointCut();
	after () returning (ObjectInput i) : ObjectInput_Close_create() {
	}

	pointcut PipedInputStream_UnconnectedRead_create() : (call(PipedInputStream+.new())) && MOP_CommonPointCut();
	after () returning (PipedInputStream i) : PipedInputStream_UnconnectedRead_create() {
	}

	pointcut PipedInputStream_UnconnectedRead_create_oi() : (call(PipedInputStream+.new(PipedOutputStream+))) && MOP_CommonPointCut();
	after () returning (PipedInputStream i) : PipedInputStream_UnconnectedRead_create_oi() {
	}

	pointcut SecurityManager_Permission_get(SecurityManager manager) : (call(* SecurityManager.getSecurityContext(..)) && target(manager)) && MOP_CommonPointCut();
	after (SecurityManager manager) returning (Object context) : SecurityManager_Permission_get(manager) {
	}

	pointcut ObjectStreamClass_Initialize_create() : (call(ObjectStreamClass+.new())) && MOP_CommonPointCut();
	after () returning (ObjectStreamClass c) : ObjectStreamClass_Initialize_create() {
	}

	pointcut Console_CloseReader_getreader() : (call(Reader+ Console+.reader())) && MOP_CommonPointCut();
	after () returning (Reader r) : Console_CloseReader_getreader() {
	}

	pointcut ArrayDeque_UnsafeIterator_create(ArrayDeque q) : (target(ArrayDeque) && (call(Iterator Iterable+.iterator()) || call(Iterator Deque+.descendingIterator())) && target(q)) && MOP_CommonPointCut();
	after (ArrayDeque q) returning (Iterator i) : ArrayDeque_UnsafeIterator_create(q) {
	}

	pointcut Collections_SynchronizedMap_sync() : (call(* Collections.synchronizedMap(Map)) || call(* Collections.synchronizedSortedMap(SortedMap))) && MOP_CommonPointCut();
	after () returning (Map syncMap) : Collections_SynchronizedMap_sync() {
	}

	pointcut Collections_SynchronizedMap_createSet(Map syncMap) : ((call(Set Map+.keySet()) || call(Set Map+.entrySet()) || call(Collection Map+.values())) && target(syncMap)) && MOP_CommonPointCut();
	after (Map syncMap) returning (Collection col) : Collections_SynchronizedMap_createSet(syncMap) {
	}

	pointcut NavigableMap_Modification_create(NavigableMap m1) : (call(NavigableMap NavigableMap+.descendingMap()) && target(m1)) && MOP_CommonPointCut();
	after (NavigableMap m1) returning (NavigableMap m2) : NavigableMap_Modification_create(m1) {
	}

	pointcut NavigableMap_Modification_getset1(NavigableMap m1) : ((call(Set Map+.keySet()) || call(Set Map+.entrySet()) || call(Collection Map+.values())) && target(m1)) && MOP_CommonPointCut();
	after (NavigableMap m1) returning (Collection c) : NavigableMap_Modification_getset1(m1) {
	}

	pointcut NavigableMap_Modification_getset2(NavigableMap m2) : ((call(Set Map+.keySet()) || call(Set Map+.entrySet()) || call(Collection Map+.values())) && target(m2)) && MOP_CommonPointCut();
	after (NavigableMap m2) returning (Collection c) : NavigableMap_Modification_getset2(m2) {
	}

	pointcut Map_StandardConstructors_staticinit() : (staticinitialization(Map+)) && MOP_CommonPointCut();
	after () : Map_StandardConstructors_staticinit() {
	}

	pointcut StringTokenizer_HasMoreElements_hasnexttrue(StringTokenizer i) : ((call(boolean StringTokenizer.hasMoreTokens()) || call(boolean StringTokenizer.hasMoreElements())) && target(i)) && MOP_CommonPointCut();
	after (StringTokenizer i) returning (boolean b) : StringTokenizer_HasMoreElements_hasnexttrue(i) {
		//StringTokenizer_HasMoreElements_hasnexttrue
		//StringTokenizer_HasMoreElements_hasnextfalse
	}

	pointcut Float_StaticFactory_constructor_create() : (call(Float+.new(float))) && MOP_CommonPointCut();
	after () returning (Float f) : Float_StaticFactory_constructor_create() {
	}

	pointcut ProcessBuilder_NullKeyOrValue_createMap() : (call(* ProcessBuilder.environment())) && MOP_CommonPointCut();
	after () returning (Map map) : ProcessBuilder_NullKeyOrValue_createMap() {
	}

	after (Thread t) : ShutdownHook_SystemExit_register(t) {
	}

	after (Thread t) : ShutdownHook_SystemExit_unregister(t) {
	}

	pointcut InputStream_ReadAheadLimit_read1(InputStream i) : (call(* InputStream+.read()) && target(i) && if(i instanceof BufferedInputStream || i instanceof DataInputStream || i instanceof LineNumberInputStream)) && MOP_CommonPointCut();
	after (InputStream i) returning (int n) : InputStream_ReadAheadLimit_read1(i) {
	}

	pointcut InputStream_ReadAheadLimit_readn(InputStream i) : (call(* InputStream+.read(char[], ..)) && target(i) && if(i instanceof BufferedInputStream || i instanceof DataInputStream || i instanceof LineNumberInputStream)) && MOP_CommonPointCut();
	after (InputStream i) returning (int n) : InputStream_ReadAheadLimit_readn(i) {
	}

	pointcut Collection_StandardConstructors_staticinit() : (staticinitialization(Collection+)) && MOP_CommonPointCut();
	after () : Collection_StandardConstructors_staticinit() {
		//Collection_StandardConstructors_staticinit
		//Collection_HashCode_staticinit
	}

	pointcut Deque_OfferRatherThanAdd_create() : (call(LinkedBlockingDeque+.new(int))) && MOP_CommonPointCut();
	after () returning (Deque q) : Deque_OfferRatherThanAdd_create() {
	}

	pointcut SocketImpl_CloseOutput_getoutput(SocketImpl sock) : (call(OutputStream SocketImpl+.getOutputStream()) && target(sock)) && MOP_CommonPointCut();
	after (SocketImpl sock) returning (OutputStream output) : SocketImpl_CloseOutput_getoutput(sock) {
	}

	pointcut ResourceBundleControl_MutateFormatList_create() : (call(List ResourceBundle.Control.getFormats(..)) || call(List ResourceBundle.Control.getCandidateLocales(..))) && MOP_CommonPointCut();
	after () returning (List l) : ResourceBundleControl_MutateFormatList_create() {
	}

	pointcut URLConnection_OverrideGetPermission_staticinit() : (staticinitialization(URLConnection+)) && MOP_CommonPointCut();
	after () : URLConnection_OverrideGetPermission_staticinit() {
	}

	pointcut Authenticator_OverrideGetPasswordAuthentication_staticinit() : (staticinitialization(Authenticator+)) && MOP_CommonPointCut();
	after () : Authenticator_OverrideGetPasswordAuthentication_staticinit() {
	}

	pointcut Console_CloseWriter_getwriter() : (call(Writer+ Console+.writer())) && MOP_CommonPointCut();
	after () returning (Writer w) : Console_CloseWriter_getwriter() {
	}

	pointcut StringBuffer_SingleThreadUsage_init() : (call(StringBuffer.new(..))) && MOP_CommonPointCut();
	after () returning (StringBuffer s) : StringBuffer_SingleThreadUsage_init() {
		Thread t = Thread.currentThread();
	}

	pointcut Reader_ReadAheadLimit_read1(Reader r) : (call(* Reader+.read()) && target(r) && if(r instanceof BufferedReader || r instanceof LineNumberReader)) && MOP_CommonPointCut();
	after (Reader r) returning (int n) : Reader_ReadAheadLimit_read1(r) {
	}

	pointcut Reader_ReadAheadLimit_readn(Reader r) : (call(* Reader+.read(char[], ..)) && target(r) && if(r instanceof BufferedReader || r instanceof LineNumberReader)) && MOP_CommonPointCut();
	after (Reader r) returning (int n) : Reader_ReadAheadLimit_readn(r) {
	}

	pointcut Double_StaticFactory_constructor_create() : (call(Double+.new(double))) && MOP_CommonPointCut();
	after () returning (Double d) : Double_StaticFactory_constructor_create() {
	}

	after (PipedOutputStream o) returning (PipedInputStream i) : PipedOutputStream_UnconnectedWrite_create_oi(o) {
	}

	after (PipedInputStream i) returning (PipedOutputStream o) : PipedInputStream_UnconnectedRead_create_io(i) {
	}

	pointcut Integer_StaticFactory_constructor_create() : (call(Integer+.new(int))) && MOP_CommonPointCut();
	after () returning (Integer i) : Integer_StaticFactory_constructor_create() {
	}

	static HashMap<Thread, Runnable> Thread_SetDaemonBeforeStart_start_ThreadToRunnable = new HashMap<Thread, Runnable>();
	static Thread Thread_SetDaemonBeforeStart_start_MainThread = null;

	after (Runnable r) returning (Thread t): ((call(Thread+.new(Runnable+,..)) && args(r,..))|| (initialization(Thread+.new(ThreadGroup+, Runnable+,..)) && args(ThreadGroup, r,..))) && MOP_CommonPointCut() {
		while (!MultiSpec_1_MOPLock.tryLock()) {
			Thread.yield();
		}
		Thread_SetDaemonBeforeStart_start_ThreadToRunnable.put(t, r);
		MultiSpec_1_MOPLock.unlock();
	}

	before (Thread t_1): ( execution(void Thread+.run()) && target(t_1) ) && MOP_CommonPointCut() {
		if(Thread.currentThread() == t_1) {
			Thread t = Thread.currentThread();
		}
	}

	before (Runnable r_1): ( execution(void Runnable+.run()) && !execution(void Thread+.run()) && target(r_1) ) && MOP_CommonPointCut() {
		while (!MultiSpec_1_MOPLock.tryLock()) {
			Thread.yield();
		}
		if(Thread_SetDaemonBeforeStart_start_ThreadToRunnable.get(Thread.currentThread()) == r_1) {
			Thread t = Thread.currentThread();
		}
		MultiSpec_1_MOPLock.unlock();
	}

	before (): (execution(void *.main(..)) ) && MOP_CommonPointCut() {
		if(Thread_SetDaemonBeforeStart_start_MainThread == null){
			Thread_SetDaemonBeforeStart_start_MainThread = Thread.currentThread();
			Thread t = Thread.currentThread();
		}
	}

	static HashMap<Thread, Runnable> ShutdownHook_SystemExit_start_ThreadToRunnable = new HashMap<Thread, Runnable>();
	static Thread ShutdownHook_SystemExit_start_MainThread = null;

	after (Runnable r) returning (Thread t): ((call(Thread+.new(Runnable+,..)) && args(r,..))|| (initialization(Thread+.new(ThreadGroup+, Runnable+,..)) && args(ThreadGroup, r,..))) && MOP_CommonPointCut() {
		while (!MultiSpec_1_MOPLock.tryLock()) {
			Thread.yield();
		}
		ShutdownHook_SystemExit_start_ThreadToRunnable.put(t, r);
		MultiSpec_1_MOPLock.unlock();
	}

	before (Thread t_1): ( execution(void Thread+.run()) && target(t_1) ) && MOP_CommonPointCut() {
		if(Thread.currentThread() == t_1) {
			Thread t = Thread.currentThread();
		}
	}

	before (Runnable r_1): ( execution(void Runnable+.run()) && !execution(void Thread+.run()) && target(r_1) ) && MOP_CommonPointCut() {
		while (!MultiSpec_1_MOPLock.tryLock()) {
			Thread.yield();
		}
		if(ShutdownHook_SystemExit_start_ThreadToRunnable.get(Thread.currentThread()) == r_1) {
			Thread t = Thread.currentThread();
		}
		MultiSpec_1_MOPLock.unlock();
	}

	before (): (execution(void *.main(..)) ) && MOP_CommonPointCut() {
		if(ShutdownHook_SystemExit_start_MainThread == null){
			ShutdownHook_SystemExit_start_MainThread = Thread.currentThread();
			Thread t = Thread.currentThread();
		}
	}

	static HashMap<Thread, Runnable> ShutdownHook_UnsafeSwingCall_start_ThreadToRunnable = new HashMap<Thread, Runnable>();
	static Thread ShutdownHook_UnsafeSwingCall_start_MainThread = null;

	after (Runnable r) returning (Thread t): ((call(Thread+.new(Runnable+,..)) && args(r,..))|| (initialization(Thread+.new(ThreadGroup+, Runnable+,..)) && args(ThreadGroup, r,..))) && MOP_CommonPointCut() {
		while (!MultiSpec_1_MOPLock.tryLock()) {
			Thread.yield();
		}
		ShutdownHook_UnsafeSwingCall_start_ThreadToRunnable.put(t, r);
		MultiSpec_1_MOPLock.unlock();
	}

	before (Thread t_1): ( execution(void Thread+.run()) && target(t_1) ) && MOP_CommonPointCut() {
		if(Thread.currentThread() == t_1) {
			Thread t = Thread.currentThread();
		}
	}

	before (Runnable r_1): ( execution(void Runnable+.run()) && !execution(void Thread+.run()) && target(r_1) ) && MOP_CommonPointCut() {
		while (!MultiSpec_1_MOPLock.tryLock()) {
			Thread.yield();
		}
		if(ShutdownHook_UnsafeSwingCall_start_ThreadToRunnable.get(Thread.currentThread()) == r_1) {
			Thread t = Thread.currentThread();
		}
		MultiSpec_1_MOPLock.unlock();
	}

	before (): (execution(void *.main(..)) ) && MOP_CommonPointCut() {
		if(ShutdownHook_UnsafeSwingCall_start_MainThread == null){
			ShutdownHook_UnsafeSwingCall_start_MainThread = Thread.currentThread();
			Thread t = Thread.currentThread();
		}
	}

	static HashMap<Thread, Runnable> ShutdownHook_UnsafeAWTCall_start_ThreadToRunnable = new HashMap<Thread, Runnable>();
	static Thread ShutdownHook_UnsafeAWTCall_start_MainThread = null;

	after (Runnable r) returning (Thread t): ((call(Thread+.new(Runnable+,..)) && args(r,..))|| (initialization(Thread+.new(ThreadGroup+, Runnable+,..)) && args(ThreadGroup, r,..))) && MOP_CommonPointCut() {
		while (!MultiSpec_1_MOPLock.tryLock()) {
			Thread.yield();
		}
		ShutdownHook_UnsafeAWTCall_start_ThreadToRunnable.put(t, r);
		MultiSpec_1_MOPLock.unlock();
	}

	before (Thread t_1): ( execution(void Thread+.run()) && target(t_1) ) && MOP_CommonPointCut() {
		if(Thread.currentThread() == t_1) {
			Thread t = Thread.currentThread();
		}
	}

	before (Runnable r_1): ( execution(void Runnable+.run()) && !execution(void Thread+.run()) && target(r_1) ) && MOP_CommonPointCut() {
		while (!MultiSpec_1_MOPLock.tryLock()) {
			Thread.yield();
		}
		if(ShutdownHook_UnsafeAWTCall_start_ThreadToRunnable.get(Thread.currentThread()) == r_1) {
			Thread t = Thread.currentThread();
		}
		MultiSpec_1_MOPLock.unlock();
	}

	before (): (execution(void *.main(..)) ) && MOP_CommonPointCut() {
		if(ShutdownHook_UnsafeAWTCall_start_MainThread == null){
			ShutdownHook_UnsafeAWTCall_start_MainThread = Thread.currentThread();
			Thread t = Thread.currentThread();
		}
	}

	static HashMap<Thread, Runnable> ShutdownHook_LateRegister_start_ThreadToRunnable = new HashMap<Thread, Runnable>();
	static Thread ShutdownHook_LateRegister_start_MainThread = null;

	after (Runnable r) returning (Thread t): ((call(Thread+.new(Runnable+,..)) && args(r,..))|| (initialization(Thread+.new(ThreadGroup+, Runnable+,..)) && args(ThreadGroup, r,..))) && MOP_CommonPointCut() {
		while (!MultiSpec_1_MOPLock.tryLock()) {
			Thread.yield();
		}
		ShutdownHook_LateRegister_start_ThreadToRunnable.put(t, r);
		MultiSpec_1_MOPLock.unlock();
	}

	before (Thread t_1): ( execution(void Thread+.run()) && target(t_1) ) && MOP_CommonPointCut() {
		if(Thread.currentThread() == t_1) {
			Thread t = Thread.currentThread();
		}
	}

	before (Runnable r_1): ( execution(void Runnable+.run()) && !execution(void Thread+.run()) && target(r_1) ) && MOP_CommonPointCut() {
		while (!MultiSpec_1_MOPLock.tryLock()) {
			Thread.yield();
		}
		if(ShutdownHook_LateRegister_start_ThreadToRunnable.get(Thread.currentThread()) == r_1) {
			Thread t = Thread.currentThread();
		}
		MultiSpec_1_MOPLock.unlock();
	}

	before (): (execution(void *.main(..)) ) && MOP_CommonPointCut() {
		if(ShutdownHook_LateRegister_start_MainThread == null){
			ShutdownHook_LateRegister_start_MainThread = Thread.currentThread();
			Thread t = Thread.currentThread();
		}
	}

	class MultiSpec_1_DummyHookThread extends Thread {
		public void run(){
			{
			}
			{
			}
			{
			}
			{
			}
			{
			}
			{
			}
			{
			}
		}
	}
}
