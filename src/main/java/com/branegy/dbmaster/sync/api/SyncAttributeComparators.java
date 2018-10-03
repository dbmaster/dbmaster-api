package com.branegy.dbmaster.sync.api;

import static com.branegy.dbmaster.sync.api.SyncAttributePair.AttributeChangeType.CHANGED;
import static com.branegy.dbmaster.sync.api.SyncAttributePair.AttributeChangeType.DELETED;
import static com.branegy.dbmaster.sync.api.SyncAttributePair.AttributeChangeType.EQUALS;
import static com.branegy.dbmaster.sync.api.SyncAttributePair.AttributeChangeType.NEW;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isWhitespace;

import com.google.diff.match.patch.DiffMatchPatch;
import com.google.diff.match.patch.DiffMatchPatch.Diff;
import com.google.diff.match.patch.DiffMatchPatch.Operation;

import com.branegy.dbmaster.sync.api.SyncAttributePair.AttributeChangeType;
import com.branegy.dbmaster.sync.api.SyncAttributePair.SyncAttributeComparator;

public interface SyncAttributeComparators {
    SyncAttributeComparator<Object> DEFAULT = SyncAttributePair.DEFAULT_COMPARATOR;
    
    SyncAttributeComparator<String> IGNORE_WHITESPACES = new SyncAttributeComparator<String>() {
        @Override
        public AttributeChangeType compare(String sourceValue, String targetValue) {
            if (isEmpty(sourceValue)) {
                if (isEmpty(targetValue)) {
                    return EQUALS;
                } else {
                    return NEW;
                }
            } else if (isEmpty(targetValue)) {
                return DELETED;
            } else {
                for (Diff diff: new DiffMatchPatch().diff_main(sourceValue, targetValue)) {
                    if (diff.operation != Operation.EQUAL && !isWhitespace(diff.text)) {
                        return CHANGED;
                    }
                }
                return EQUALS;
            }
        }
    };
   
}
