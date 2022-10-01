package co.casterlabs.commons.ipc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

class _Util {
    private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<>();

    static {
        primitiveWrapperMap.put(boolean.class, Boolean.class);
        primitiveWrapperMap.put(byte.class, Byte.class);
        primitiveWrapperMap.put(char.class, Character.class);
        primitiveWrapperMap.put(double.class, Double.class);
        primitiveWrapperMap.put(float.class, Float.class);
        primitiveWrapperMap.put(int.class, Integer.class);
        primitiveWrapperMap.put(long.class, Long.class);
        primitiveWrapperMap.put(short.class, Short.class);
    }

    static Method deepMethodSearch(Class<?> clazz, String methodName, Class<?>[] expectedParameters)
            throws NoSuchMethodException {
        if (clazz == null) {
            String paramStr = Arrays.toString(expectedParameters);
            paramStr = paramStr.substring(1, paramStr.length() - 1);

            throw new NoSuchMethodException(String.format("Cannot find method: %s(%s)", methodName, paramStr));
        }

        for (Method method : clazz.getDeclaredMethods()) {
            // Check the name.
            if (!method.getName().equals(methodName))
                continue;

            Class<?>[] methodParameters = method.getParameterTypes();

            // Make sure they have the same amount of parameters.
            if (methodParameters.length != expectedParameters.length)
                continue;

            // Loop over each parameter and make sure they're assignable.
            boolean matches = true;

            for (int i = 0; i < expectedParameters.length; i++) {
                Class<?> methodParameter = methodParameters[i];
                Class<?> expectedParameter = expectedParameters[i];

                if (methodParameter.isPrimitive()) {
                    methodParameter = primitiveWrapperMap.get(methodParameter);
                }

                if (!methodParameter.isAssignableFrom(expectedParameter)) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                return method;
            }

            // Otherwise, continue.
        }

        // Check the inherited methods.
        return deepMethodSearch(clazz.getSuperclass(), methodName, expectedParameters);
    }

    static String serializeThrowable(Throwable t) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(t);
            out.flush();

            return Base64.getEncoder().encodeToString(bos.toByteArray());
        }
    }

    static Throwable deserializeThrowable(String ser) throws IOException, ClassNotFoundException {
        byte[] bytes = Base64.getDecoder().decode(ser);

        try (ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (Throwable) in.readObject();
        }
    }

}
