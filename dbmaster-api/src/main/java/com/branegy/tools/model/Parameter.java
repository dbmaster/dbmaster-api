package com.branegy.tools.model;

import static org.apache.commons.lang.ArrayUtils.EMPTY_STRING_ARRAY;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.branegy.dbmaster.custom.CustomFieldConfig.Type;
import com.branegy.service.core.exception.ApiException;

@SuppressWarnings("serial")
public class Parameter implements Serializable{

    /**
     * Parameter name. Value is passed to execution script using <code>name</code>.
     */
    String name;

    /**
     * Title of the parameter. Should be displayed for user.
     * I18N
     */
    String title;

    /**
     * When false parameter shoudn't be displayed for user.
     * Invisible parameters can be used to pass values to execution script.
     */
    boolean hidden;

    boolean required;

    boolean multiple;

    /**
     * Default values for the parameter
     *
     */
    String  defaultValue;

    /**
     * Value of this parameter should is limited by this list of values
     */
    String[] values;

    /**
     * Parameter type for correct parsing.
     */
    Type type;

    /**
     * Used for parameters of type "TEXT". Initial width of textarea in pixels.
     */
    int width;

    /**
     * Used for parameters of type "TEXT". Initial height of textarea in pixels.
     */
    int height;

    /**
     * Dynamic suggestion
     * When defined - a combobox should be displayed and data loaded from server.
     * Only <code>suggestion</code> or <code>values</code> can be defined for parameter.
     * When both - static values take precedence.
     */
    String suggestion;

    boolean disableSuggestionCache;
    
    @Deprecated
    boolean suggestionEditable = true;

    @Deprecated
    boolean suggestionForce = false;

    public Parameter(String name, Type type, String title) {
        this.name = name;
        this.type = type;
        this.title = title;
    }

    public Parameter(String name, Type type, String title,String defaultValue) {
        this.name = name;
        this.type = type;
        this.title = title;
        this.defaultValue  = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public boolean isVisible() {
        return !hidden;
    }

    /*
    public <T> T getValue() {
        return convertValue(type, defaultValue);
    }
*/

    @SuppressWarnings("unchecked")
    private static <T> T convertValue(Type type, String value) {
        T result = null;
        if (value!=null) {
            switch (type) {
            case HTML:
            case STRING:
            case TEXT:
            case FILE_REF:
                result =  (T) value;
                break;
            case BOOLEAN:
                result = (T) Boolean.valueOf(value);
                break;
            case DATE:
                try {
                    result = (T) new SimpleDateFormat("MM/dd/yy").parse(value); // TODO
                } catch (ParseException e) {
                    throw new ApiException(e);
                }
                break;
            case FLOAT:
                result = (T) Double.valueOf(value);
                break;
            case INTEGER:
                result = (T) Long.valueOf(value);
                break;
            }
        }
        return result;
    }

    public Type getType() {
        return type;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public <T> T getDefaultValue() {
        if (multiple) {
            throw new ApiException("Multiple values field, call getDefaultValueList() instead");
        }
        return convertValue(type, defaultValue);
    }
    
    public Object[] getDefaultValues(){
       if (multiple) {
           String[] rawDefaultValues = defaultValue!=null?defaultValue.split(","):EMPTY_STRING_ARRAY;
           Object[] result = null;
           switch (type) {
           case FILE_REF:
           case HTML:
           case STRING:
           case TEXT:
               result = new String[rawDefaultValues.length];
               break;
           case DATE:
               result = new java.util.Date[rawDefaultValues.length];
               break;
           case FLOAT:
               result = new Double[rawDefaultValues.length];
               break;
           case INTEGER:
               result = new Long[rawDefaultValues.length];
               break;
           default:
               return null;
           }
           for (int i=0; i<rawDefaultValues.length; ++i) {
               result[i] = convertValue(type, rawDefaultValues[i]);
           }
           return result;
       } else {
           throw new ApiException("Single values field, call getDefaultValue() instead");
       }
    }
    
    public Object[] getListValues() {
        if (values == null) {
            return null;
        } else {
            Object[] newValues = null;
            switch (type) {
            case FILE_REF:
            case HTML:
            case STRING:
            case TEXT:
                newValues = new String[values.length];
                break;
            case DATE:
                newValues = new java.util.Date[values.length];
                break;
            case FLOAT:
                newValues = new Double[values.length];
                break;
            case INTEGER:
                newValues = new Long[values.length];
                break;
            default:
                return null;
            }
            for (int i=0;i<values.length;i++) {
                newValues[i] = convertValue(type, values[i]);
            }
            return newValues;
        }
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public boolean isDisableSuggestionCache() {
        return disableSuggestionCache;
    }

    public void setDisableSuggestionCache(boolean disableSuggestionCache) {
        this.disableSuggestionCache = disableSuggestionCache;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isSuggestionEditable() {
        return suggestionEditable;
    }

    public void setSuggestionEditable(boolean suggestionEditable) {
        this.suggestionEditable = suggestionEditable;
    }

    public boolean isSuggestionForce() {
        return suggestionForce;
    }

    public void setSuggestionForce(boolean suggestionForce) {
        this.suggestionForce = suggestionForce;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setVisible(boolean visible) {
        this.hidden = !visible;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    
}