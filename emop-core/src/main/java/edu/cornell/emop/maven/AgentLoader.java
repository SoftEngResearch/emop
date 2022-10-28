package edu.cornell.emop.maven;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * This class is duplicated from STARTS, with minor changes.
 */
public final class AgentLoader {
    private static final String TOOLS_JAR_NAME = "tools.jar";
    private static final String CLASSES_JAR_NAME = "classes.jar";
    private static final String LIB = "lib";
    private static final String AGENT_INIT = AgentLoader.class.getName() + " Initialized";

    /**
     * Method loading an agent specified in argument.
     */
    @SuppressWarnings("checkstyle:Regexp")
    public static boolean loadDynamicAgent(String classname) {
        try {
            if (System.getProperty(AGENT_INIT) == null) {
                URL agentJarURL = AgentLoader.class.getResource(classname);
                URL agentJarURLConnection = extractJarURL(agentJarURL);
                System.out.println("================================");
                System.out.println(agentJarURLConnection.getPath());
                System.out.println("================================");
                boolean isLoadAgentSuccessful = loadAgent(agentJarURLConnection);
                if (isLoadAgentSuccessful) {
                    System.setProperty(AGENT_INIT, "");
                }
                return isLoadAgentSuccessful;
            } else {
                System.out.println("An agent has already been loaded.");
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Loads and attaches the agent.
     */
    public static boolean loadAgent(URL aju) throws Exception {
        URL toolsJarFile = findToolsJar();
        if (toolsJarFile == null) {
            return false;
        }

        Class<?> vc = loadVirtualMachine(new URL[]{toolsJarFile});
        if (vc == null) {
            return false;
        }

        attachAgent(vc, aju);
        return true;
    }

    private static void attachAgent(Class<?> vc, URL aju) throws Exception {
        String pid = getPID();
        String agentAbsolutePath = new File(aju.toURI().getSchemeSpecificPart()).getAbsolutePath();

//        TODO: probably need to provide the other args too?
//        String excludes = "=trace.exclude=org.dacapo.*;instrument.exclude=org.dacapo.*";
        Object vm = getAttachMethod(vc).invoke(null, new Object[]{pid});
//        Object vm = getAttachMethod(vc).invoke(null, new Object[]{pid});
        getLoadAgentMethod(vc).invoke(vm, new Object[]{agentAbsolutePath});
        getDetachMethod(vc).invoke(vm);
    }

    private static Method getAttachMethod(Class<?> vc) throws SecurityException, NoSuchMethodException {
        return vc.getMethod("attach", new Class<?>[]{String.class});
    }

    private static Method getLoadAgentMethod(Class<?> vc) throws SecurityException, NoSuchMethodException {
        return vc.getMethod("loadAgent", new Class[]{String.class});
    }

    private static Method getDetachMethod(Class<?> vc) throws SecurityException, NoSuchMethodException {
        return vc.getMethod("detach");
    }

    private static Class<?> loadVirtualMachine(URL[] urls) throws Exception {
        URLClassLoader loader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
        return loader.loadClass("com.sun.tools.attach.VirtualMachine");
    }

    private static String getPID() {
        String vmName = ManagementFactory.getRuntimeMXBean().getName();
        return vmName.substring(0, vmName.indexOf("@"));
    }

    private static URL findToolsJar() throws MalformedURLException {
        String javaHome = System.getProperty("java.home");
        File javaHomeFile = new File(javaHome);
        File tjf = new File(javaHomeFile, LIB + File.separator + TOOLS_JAR_NAME);

        if (!tjf.exists()) {
            tjf = new File(System.getenv("java_home"), LIB + File.separator + TOOLS_JAR_NAME);
        }

        if (!tjf.exists() && javaHomeFile.getAbsolutePath().endsWith(File.separator + "jre")) {
            javaHomeFile = javaHomeFile.getParentFile();
            tjf = new File(javaHomeFile, LIB + File.separator + TOOLS_JAR_NAME);
        }

        if (!tjf.exists() && isMac() && javaHomeFile.getAbsolutePath().endsWith(File.separator + "Home")) {
            javaHomeFile = javaHomeFile.getParentFile();
            tjf = new File(javaHomeFile, "Classes" + File.separator + CLASSES_JAR_NAME);
        }

        return tjf.toURI().toURL();
    }

    private static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0;
    }

    public static URL extractJarURL(URL url) throws IOException {
        JarURLConnection connection = (JarURLConnection) url.openConnection();
        return connection.getJarFileURL();
    }

    // added so we can quickly test this class on the command line
    public static void main(String[] args) {
        loadDynamicAgent("JavaAgent.class");
    }
}
