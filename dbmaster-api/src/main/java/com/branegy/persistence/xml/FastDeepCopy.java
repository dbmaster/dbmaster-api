package com.branegy.persistence.xml;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.io.input.ClassLoaderObjectInputStream;

/**
 * Utility for making deep copies (vs. clone()'s shallow copies) of objects. Objects are first serialized and
 * then deserialized. Error checking is fairly minimal in this implementation. If an object is encountered
 * that cannot be serialized (or that references an object that cannot be serialized) an error is printed to
 * System.err and null is returned. Depending on your specific application, it might make more sense to have
 * copy(...) re-throw the exception.
 *
 * URL: http://javatechniques.com/blog/faster-deep-copies-of-java-objects/
 *      http://javatechniques.com/blog/low-memory-deep-copy-technique-for-java-objects/
 */
public class FastDeepCopy {

    /**
     * Returns a copy of the object, or null if the object cannot be serialized.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T orig) throws IOException, ClassNotFoundException {
        if (orig == null){
            return null;
        }
        Object obj = null;

        // Write the object out to a byte array
        FastByteArrayOutputStream fbos = new FastByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(fbos);
        out.writeObject(orig);
        out.flush();
        out.close();

        // Retrieve an input stream from the byte array and read
        // a copy of the object back in.
        ObjectInputStream in = new ObjectInputStream(fbos.getInputStream());
        obj = in.readObject();
        return (T) obj;
    }
    
    /**
     * Returns a copy of the object, or null if the object cannot be serialized.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T orig, ClassLoader cl) throws IOException, ClassNotFoundException {
        if (orig == null){
            return null;
        }
        Object obj = null;

        // Write the object out to a byte array
        FastByteArrayOutputStream fbos = new FastByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(fbos);
        out.writeObject(orig);
        out.flush();
        out.close();

        // Retrieve an input stream from the byte array and read
        // a copy of the object back in.
        ObjectInputStream in = new ClassLoaderObjectInputStream(cl, fbos.getInputStream());
        obj = in.readObject();
        return (T) obj;
    }

}