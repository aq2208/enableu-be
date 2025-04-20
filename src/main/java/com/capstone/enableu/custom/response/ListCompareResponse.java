package com.capstone.enableu.custom.response;

import java.util.List;

public record ListCompareResponse(List<Long> oldItemList, List<Long> newItemList) {
}
