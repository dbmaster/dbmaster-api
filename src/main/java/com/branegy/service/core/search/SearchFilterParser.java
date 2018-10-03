package com.branegy.service.core.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.branegy.service.core.search.CustomCriterion.Operator;

public final class SearchFilterParser {


    public static List<CustomCriterion> parseFilter(String filter) {
        String key = null;
        Operator operator = null;
        List<CustomCriterion> result = new ArrayList<CustomCriterion>();
        if (filter!=null && filter.trim().length()>0) {
            for (String singleFilter : filter.split("&&")) {
                boolean foundOperator = false;
                singleFilter = singleFilter.trim();
                for (Operator m: Operator.values()) {
                    int i = singleFilter.indexOf(m.toString());
                    if (i!=-1) {
                        key = singleFilter.substring(0,i);
                        operator = m;
                        String rawValue = singleFilter.substring(i+m.toString().length());
                        if (rawValue.length()==0) {
                            rawValue = null;
                        }
                        result.add(new CustomCriterion(key, operator, rawValue));
                        foundOperator = true;
                        break;
                    }
                }
                if (!foundOperator) { // Free Text Search
                    result.add(new CustomCriterion(singleFilter));
                }
            }
        }
        return Collections.unmodifiableList(result);
    }
}
