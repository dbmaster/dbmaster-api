package io.dbmaster.event;

public interface Event {
    String getType();
    <T> T getData();
}
