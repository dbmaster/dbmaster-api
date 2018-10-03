package com.branegy.tools.api;

import java.util.Date;

public interface ToolLogEntry {
    
    public enum Severity{
        ERROR, WARN, INFO, DEBUG;
        
        private transient final int bit = 1 << ordinal();
        
        public int asBit(){
            return bit;
        }
        
        public static int setBit(boolean set, Severity severity,int bits){
            if (set){
                return bits | severity.asBit();
            } else {
                return bits & (~severity.asBit());
            }
        }
        
        public static boolean checkBit(Severity severity, int bits){
            return (bits & severity.asBit())!=0;
        }
    }
    
    Severity getSeverity();
    Date getDate();
    String getMessage();
    int getIndex();
}
