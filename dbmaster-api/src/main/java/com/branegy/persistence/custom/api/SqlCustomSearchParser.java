package com.branegy.persistence.custom.api;

public interface SqlCustomSearchParser {
    String EXTENSION_CONTACT = "contact";

    QueryExpression parse(String text, String... extensions);
    
}
