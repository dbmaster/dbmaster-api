package com.branegy.tools.model;

public class ToolWarning {
    
    public enum Type{
        WARN, ERROR
    }
    
    private final Type type;
    private final String tool;
    private final String message;
    private final boolean runtime;

    public ToolWarning(Type type,String tool, String message, boolean runtime) {
        this.type = type;
        this.tool = tool;
        this.message = message;
        this.runtime = runtime;
    }
    
    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
    
    public String getTool() {
        return tool;
    }

    @Override
    public String toString() {
        return "[" + type +"/" + tool +"] " + message;
    }

    public boolean isRuntime() {
        return runtime;
    }

}
