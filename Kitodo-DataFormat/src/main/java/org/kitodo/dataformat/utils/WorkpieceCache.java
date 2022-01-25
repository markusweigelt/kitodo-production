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

package org.kitodo.dataformat.utils;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.kitodo.api.dataformat.Workpiece;

public class WorkpieceCache {

    public static final int CACHING_HEAP = 100;

    private static WorkpieceCache instance;

    private static final Map<String, Workpiece> workpieceCache = new ConcurrentHashMap();

    private static final Map<String, Long> workpieceHistory = new ConcurrentHashMap();

    public static WorkpieceCache getInstance() {
        WorkpieceCache localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (WorkpieceCache.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new WorkpieceCache();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    public static Workpiece get(URI uri) {
        if (workpieceCache.containsKey(uri.getPath())) {
            workpieceHistory.put(uri.getPath(), System.currentTimeMillis());
            return workpieceCache.get(uri.getPath());
        }
        return null;
    }

    public static void put(Workpiece workpiece, URI uri) {
        if (!workpieceCache.containsKey(uri.getPath()) && workpieceCache.size() >= CACHING_HEAP) {
            removeOldestEntry();
        }
        workpieceCache.put(uri.getPath(), workpiece);
        workpieceHistory.put(uri.getPath(), System.currentTimeMillis());
    }

    private static void removeOldestEntry() {
        Stream<Map.Entry<String, Long>> sorted = workpieceHistory.entrySet().stream()
                .sorted(Map.Entry.comparingByValue());
        String key = sorted.findFirst().get().getKey();
        workpieceCache.remove(key);
        workpieceHistory.remove(key);
    }
}
