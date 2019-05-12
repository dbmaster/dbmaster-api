package com.branegy.service.core.search;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;


// relation:criterion
// key = value
// key =
// free
@SuppressWarnings("serial")
public class CustomCriterion implements Serializable {
    // TODO #1874 get rid of this format
    // Single date format should be specified in configuration
    public final static String DATE_FORMAT = "MM/dd/yy";
    //private final static SearchFilterParser INSTANCE = new SearchFilterParser();
    
    private String key;
    private Operator operator;
    private ParsedValue value;
    private String relation;

    private boolean strictKey;
    private boolean strictText;
    
    public static final class ParsedValue implements Serializable{
        String text;
        Boolean bool;
        Double fraction;
        Long longValue;
        Date date;
        
        public final String getText() {
            return text;
        }
        public final Boolean getBool() {
            return bool;
        }
        public final Double getFraction() {
            return fraction;
        }
        public final Long getLongValue() {
            return longValue;
        }
        public final Date getDate() {
            return date;
        }
    }

    /**
     * don't change enum order!
     */
    public enum Operator {
        NEQ("<>"), GTE(">="), LTE("<="), GT(">"), LT("<"), EQ("=");

        final String value;

        private Operator(String value){
            this.value = value;
        }
        @Override
        public String toString() {
            return value;
        }

        public static Operator parse(String operatorStr){
            for (Operator operator:values()){
                if (operator.value.equals(operatorStr)) {
                    return operator;
                }
            }
            return null;
        }
    }

    private static boolean isEmptyString(String str){
        return str==null || str.length()==0;
    }

    // TODO Free text search
    public CustomCriterion(String value) {
        this(null, Operator.EQ, value);
    }

    public CustomCriterion(String key, String value) {
        this(key, Operator.EQ, value);
    }

    public CustomCriterion(String key, Operator operator, String value) {
        this.operator = operator;
        setKey(key);
        setValue(value);
    }
    
    
    void setValue(String newValue) {
        ParsedValue result = new ParsedValue();
        
        if (newValue==null || newValue.length()==0) {
            this.value = result;
            return;
        } else if (newValue.startsWith("contact:")) {
            relation = "contact";
            newValue = newValue.substring("contact:".length());
        }
        
        newValue = newValue.trim();
        result.text = newValue;
        if (newValue.startsWith("\"") && newValue.endsWith("\"")){
            newValue = newValue.substring(1, newValue.length()-1);
        }
        if (operator==Operator.EQ || operator==Operator.NEQ){
            if ("yes".equalsIgnoreCase(newValue) || "true".equalsIgnoreCase(newValue)) {
                result.bool = Boolean.TRUE;
            } else if ("no".equalsIgnoreCase(newValue) || "false".equalsIgnoreCase(newValue)) {
                result.bool = Boolean.FALSE;
            }
        }
        try {
            result.fraction = Double.parseDouble(newValue);
            result.longValue = Long.parseLong(newValue); // if not double then not a integer
            // if number then not a date
        } catch (Exception e) {
            if (result.fraction==null){
                try{
                    result.date = (Date)new SimpleDateFormat(DATE_FORMAT).parse(newValue);
                    //CHECKSTYLE:OFF
                } catch (Exception e2) {
                    // if date cannot be parsed then filter is a simple text string
                }
                //CHECKSTYLE:ON
            }
        }
        
        if (!isEmptyString(result.text)) {
            result.text = result.text.trim();
            strictText = result.text.contains("?") || result.text.contains("*");
            result.text = result.text.replaceAll("_", "!_") .replaceAll("%", "!%")
                                     .replaceAll("\\?", "_").replaceAll("\\*", "%");
            if (result.text.startsWith("\"") && result.text.endsWith("\"")) {
                strictText = true;
                result.text = result.text.substring(1, result.text.length()-1);
            }
        }
        this.value = result;
    }

    private void setKey(final String aKey) {
        if (!isEmptyString(aKey)){
            this.key = aKey.trim();
            strictKey = key.contains("?") || key.contains("*");
            key = key.
                replaceAll("_", "!_").
                replaceAll("%", "!%").
                replaceAll("\\?", "_").
                replaceAll("\\*", "%");
            if (key.startsWith("\"") && key.endsWith("\"")){
                strictKey = true;
                key = key.substring(1,key.length()-1);
            }
            if (key.startsWith("contact:")) {
                relation = "contact";
                key = key.substring("contact:".length());
            }
        } else {
            key = null;
        }
    }

    

    public boolean isStrictKey() {
        return strictKey;
    }

    public boolean isStrictText() {
        return strictText;
    }

    public boolean isPair() {
        return !isEmptyString(key) && !isEmptyString(value.text);
    }

    public boolean isEmptyValue() {
        return !isEmptyString(key) && isEmptyString(value.text);
    }

    public boolean isFreeTextSearch(){
        return isEmptyString(key);
    }

    public ParsedValue getValue() {
        return value;
    }

    public Operator getOperator() {
        return operator;
    }

    public String getKey() {
        return key;
    }
    
    public String getRelation() {
        return relation;
    }
}