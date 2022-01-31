/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.utils;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Cache for JAXBContexts. Class contains cache map of already created
 * JAXBContext of object classes.
 */
public class JAXBContextCache {

    public static final int CACHING_HEAP = 100;

    private static JAXBContextCache instance;

    // ConcurrentHashMap takes care of synchronization in a multi-threaded
    // environment
    private static final Map<Class, JAXBContext> jaxbContextCache = new ConcurrentHashMap();

    private static final Map<ContextDescriptor, Object> contextDescriptorObjectCache = new ConcurrentHashMap();

    private static final Map<ContextDescriptor, Long> contextDescriptorHistory = new ConcurrentHashMap();

    /**
     * The synchronized function singleton() must be used to obtain singleton access
     * to the JAXBContextCache instance.
     *
     * @return the singleton JAXBContextCache instance
     */
    public static JAXBContextCache getInstance() {
        JAXBContextCache localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (JAXBContextCache.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new JAXBContextCache();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    /**
     * Get the JAXBContext by class from cache.
     *
     * @param clazz
     *            The class to be bound.
     * @return The JAXBContext object.
     * @throws JAXBException
     *             Exception when creating new instance of JAXBContext
     */
    public static JAXBContext getJAXBContext(Class<?> clazz) throws JAXBException {
        if (jaxbContextCache.containsKey(clazz)) {
            return jaxbContextCache.get(clazz);
        }
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        jaxbContextCache.put(clazz, jaxbContext);
        return jaxbContext;
    }

    /**
     * Get unmarshalled object. This function caches unmarshalled objects of class
     * and file name. If the file is changed, the object is replaced when the
     * function is called again.
     *
     * @param clazz
     *            The class of object to cache.
     * @param file
     *            The file of object to cache.
     * @param <T>
     *            The generic class type.
     * @return The unmarshalled instance of class
     * @throws JAXBException
     *             The exception while unmarshaller is created or unmarshalling is
     *             being in process
     */
    public static <T> T getUnmarshalled(final Class<T> clazz, final File file) throws JAXBException {
        final FileContextDescriptor contextDescriptor = new FileContextDescriptor(clazz, file.getName(),
                file.lastModified());
        final Object value = contextDescriptorObjectCache.get(contextDescriptor);
        if (Objects.nonNull(value)) {
            contextDescriptorHistory.put(contextDescriptor, System.currentTimeMillis());
            return (T) value;
        }

        final Unmarshaller unmarshaller = getInstance().getJAXBContext(clazz).createUnmarshaller();
        T unmarshalledFile = (T) unmarshaller.unmarshal(file);

        // update file entry if already exists but last modification date is change
        if (invalidateEntry(contextDescriptor)) {
            contextDescriptorObjectCache.put(contextDescriptor, unmarshalledFile);
            contextDescriptorHistory.put(contextDescriptor, System.currentTimeMillis());
        } else {
            addEntry(contextDescriptor, unmarshalledFile);
        }

        return unmarshalledFile;
    }

    public static <T> T getUnmarshalled(final Class<T> clazz, final InputStream inputStream, final String identifier)
            throws JAXBException {
        // the entry must be invalidated manually
        final ContextDescriptor contextDescriptor = new ContextDescriptor(clazz, identifier);
        final Object value = contextDescriptorObjectCache.get(contextDescriptor);
        if (Objects.nonNull(value)) {
            contextDescriptorHistory.put(contextDescriptor, System.currentTimeMillis());
            return (T) value;
        }
        final Unmarshaller unmarshaller = getInstance().getJAXBContext(clazz).createUnmarshaller();
        T unmarshalledFile = (T) unmarshaller.unmarshal(inputStream);
        contextDescriptorObjectCache.put(contextDescriptor, unmarshalledFile);
        contextDescriptorHistory.put(contextDescriptor, System.currentTimeMillis());
        return unmarshalledFile;
    }

    public static <T> void invalidateEntry(final Class<T> clazz, final String identifier) {
        invalidateEntry(new ContextDescriptor(clazz, identifier));
    }

    private static <T> void addEntry(FileContextDescriptor contextDescriptor, T unmarshalledFile) {
        if (!contextDescriptorObjectCache.containsKey(contextDescriptor)
                && contextDescriptorObjectCache.size() >= CACHING_HEAP) {
            removeOldestEntry();
        }
        contextDescriptorObjectCache.put(contextDescriptor, unmarshalledFile);
        contextDescriptorHistory.put(contextDescriptor, System.currentTimeMillis());
    }

    private static void removeOldestEntry() {
        Stream<Map.Entry<ContextDescriptor, Long>> sorted = contextDescriptorHistory.entrySet().stream()
                .sorted(Map.Entry.comparingByValue());
        removeEntry(sorted.findFirst().get().getKey());
    }

    private static boolean invalidateEntry(ContextDescriptor contextDescriptor) {
        if (contextDescriptorObjectCache.containsKey(contextDescriptor)) {
            removeEntry(contextDescriptor);
            return true;
        }
        return false;
    }

    private static void removeEntry(ContextDescriptor contextDescriptor) {
        contextDescriptorObjectCache.remove(contextDescriptor);
        contextDescriptorHistory.remove(contextDescriptor);
    }

    private static class ContextDescriptor {

        private final String clazz;

        private final String identifier;

        public boolean equals(Object potentialContextDescriptor) {
            if (potentialContextDescriptor instanceof ContextDescriptor) {
                final ContextDescriptor contextDescriptor = ((ContextDescriptor) potentialContextDescriptor);
                return clazz.equals(contextDescriptor.clazz) && this.identifier.equals(contextDescriptor.identifier);
            }
            return false;
        }

        public int hashCode() {
            return (clazz + identifier).hashCode();
        }

        ContextDescriptor(Class<?> clazz, String identifier) {
            this.clazz = clazz.toString();
            this.identifier = identifier;
        }
    }

    private static class FileContextDescriptor extends ContextDescriptor {

        private final long lastModified;

        FileContextDescriptor(Class<?> clazz, String identifier, long lastModified) {
            super(clazz, identifier);
            this.lastModified = lastModified;
        }

        public boolean equals(Object potentialContextDescriptor) {
            return potentialContextDescriptor instanceof FileContextDescriptor
                    && this.lastModified == ((FileContextDescriptor) potentialContextDescriptor).lastModified
                    && super.equals(potentialContextDescriptor);
        }
    }

}
