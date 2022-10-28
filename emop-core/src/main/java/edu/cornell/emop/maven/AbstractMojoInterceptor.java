package edu.cornell.emop.maven;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractMojoInterceptor {

    protected static final Logger LOGGER = Logger.getGlobal();

    public static URL extractJarURL(URL url) throws IOException {
        JarURLConnection connection = (JarURLConnection) url.openConnection();
        return connection.getJarFileURL();
    }

    public static URL extractJarURLFromClass(Class<?> clz) throws IOException {
        return extractJarURL(getResource(clz));
    }

    public static URL getResource(Class<?> clz) {
        URL resource = clz.getResource(File.separator + clz.getName().replace('.', File.separatorChar) + ".class");
        return resource;
    }

    protected static void throwMojoExecutionException(Object mojo, String message, Exception cause) throws Exception {
        Class<?> clz = mojo.getClass().getClassLoader().loadClass("org.apache.maven.plugin.MojoExecutionException");
        Constructor<?> con = clz.getConstructor(String.class, Exception.class);
        Exception ex = (Exception) con.newInstance(message, cause);
        throw ex;
    }

    /**
     * Sets a specified field in the mojo.
     */
    public static void setField(String fieldName, Object mojo, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field;
        try {
            field = mojo.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            field = mojo.getClass().getSuperclass().getDeclaredField(fieldName);
        }
        field.setAccessible(true);
        field.set(mojo, value);
    }

    /**
     * Prints out the fields that the mojo contains.
     */
    @SuppressWarnings("checkstyle:Regexp")
    public static void printAllFields(Object mojo) throws Exception {
        Field [] fields = mojo.getClass().getDeclaredFields();
        System.out.println("Number of fields: " + fields.length);
        for (Field f : fields) {
            System.out.println(f.toString());
        }

    }

    /**
     * Returns the field. returns null if there is no such field.
     */
    public static Object getField(String fieldName, Object mojo) throws NoSuchFieldException, IllegalAccessException {
        Field field;
        try {
            field = mojo.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            field = mojo.getClass().getSuperclass().getDeclaredField(fieldName);
        }
        field.setAccessible(true);
        return field.get(mojo);
    }

    /**
     * Returns the field in the case that it is a list of strings.
     */
    protected static List<String> getListField(String fieldName, Object mojo) throws Exception {
        return (List<String>) getField(fieldName, mojo);
    }
}
