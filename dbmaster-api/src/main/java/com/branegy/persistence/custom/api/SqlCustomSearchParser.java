package com.branegy.persistence.custom.api;

public interface SqlCustomSearchParser {
    
    QueryExpression parse(String text);
    
}
