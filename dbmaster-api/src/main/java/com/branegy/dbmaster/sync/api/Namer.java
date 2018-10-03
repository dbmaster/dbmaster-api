package com.branegy.dbmaster.sync.api;

/**
 * Helps to name existing objects without changing any interfaces
 */
public interface Namer {

    String getName(Object o);
    String getType(Object o);

}
