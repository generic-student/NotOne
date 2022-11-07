package app.notone.io;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Converts Serializable Objects into bytes and vice versa
 */
public class ObjectSerializer {
    private static final String LOG_TAG = ObjectSerializer.class.getSimpleName();

    /**
     * Converts an Object implementing the Serializable interface to an array of bytes
     * @param object The object to be serialized
     * @param <T> The type of the object
     * @return byte representation of the Object or null if it could not be serialized
     */
    public static <T> byte[] serialize(@NonNull T object) {
        //check if the object is even serializable
        if(!(object instanceof java.io.Serializable)) {
            String message = String.format("Object of type '%s' does not implement the Serializable interface.", object.getClass().getSimpleName());
            Log.e(LOG_TAG, message);
            return null;
        }

        //try to convert the object to a byte array
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            //return the data
            return bos.toByteArray();

        } catch(IOException e) {
            Log.e(LOG_TAG, "Serialization error: " + e.getMessage());
        }

        return null;
    }

    /**
     * Converts a byte array to an Object of a given type
     * @param data The byte array
     * @param <T> The type of the Object
     * @return The Object if it could be deserialized or null if not
     */
    public static <T> T deserialize(@NonNull byte[] data) {
        //check if the array contains any elements
        if(data.length == 0) {
            return null;
        }

        //try to convert the bytes to an Object of type T
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bis);
            T object = (T) ois.readObject();
            return object;

        } catch(IOException e) {
            Log.e(LOG_TAG, "Deserialization error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e(LOG_TAG, "Could not convert the byte-stream to the expected type: " + e.getMessage());
        }
        return null;
    }

}
