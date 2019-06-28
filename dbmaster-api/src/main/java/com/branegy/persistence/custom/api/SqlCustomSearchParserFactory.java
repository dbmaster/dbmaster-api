package com.branegy.persistence.custom.api;

import com.google.inject.Inject;

public final class SqlCustomSearchParserFactory {
    @Inject
    volatile static SqlCustomSearchParser INSTANCE;
    
    private SqlCustomSearchParserFactory() {
    }
    
    public static SqlCustomSearchParser get() {
        return INSTANCE;
    }
}
