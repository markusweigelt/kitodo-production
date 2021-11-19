package org.kitodo.production.helper;

import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import java.util.HashMap;
import java.util.Map;

public class SortHelper {

    public static Map<String, SortMeta> getSingleSortMeta(String fieldName, SortOrder order) {
        return (Map<String, SortMeta>) new HashMap<String, SortMeta>().put(fieldName,
            SortMeta.builder().field(fieldName).order(order).build());
    }

}
