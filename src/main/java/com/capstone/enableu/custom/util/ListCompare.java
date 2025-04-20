package com.capstone.enableu.custom.util;

import com.capstone.enableu.custom.response.ListCompareResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ListCompare {

    public static ListCompareResponse compareLists(List<Long> oldList, List<Long> newList) {
        Map<Long, Boolean> map = new HashMap<>();

        for (Long item : oldList) {
            map.put(item, false); // false indicates the item was in oldList
        }

        List<Long> addedItems = new ArrayList<>();
        for (Long item : newList) {
            if (!map.containsKey(item)) {
                addedItems.add(item);
            } else {
                map.put(item, true);
            }
        }

        List<Long> removedItems = new ArrayList<>();
        for (Map.Entry<Long, Boolean> entry : map.entrySet()) {
            if (!entry.getValue()) {
                removedItems.add(entry.getKey());
            }
        }

        // Return result as ListCompareResponse
        return new ListCompareResponse(removedItems, addedItems);
    }

    public static List<Long> convertIntegerToLongList(List<Integer> integerList) {
        return integerList.stream()
                .map(Integer::longValue)
                .collect(Collectors.toList());
    }

}

