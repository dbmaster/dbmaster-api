package com.branegy.tools.api;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * subject to change.
 */
@Deprecated
public final class SubreportHelper {
    private Map<String, Object> subreport = new HashMap<String, Object>();
    private Map<String, Object> context;

    public SubreportHelper(Map<String, Object> context) {
        this.context = context;
    }

    public SubreportHelper all() {
        subreport.putAll(context);
        return this;
    }

    public SubreportHelper set(String key, Object value) {
        subreport.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        return JsonConverter.encode(subreport).toString(); // TODO (Vitali) is encode -- ?
    }

    public String render(String content) {
       return "<span>" +
                 "<span style=\"cursor:pointer;color:blue;\">"+
                     "<!--"+toString()+"-->"+
                     "<span style=\"font-family: 'DejaVu Sans', Arial, Helvetica, sans-serif; " +
                             "font-size: 15px\">"+
                         content+
                     "</span>"+
                 "</span>" +
              "</span>";
    }
    /**
     * Pure java encoder for com.extjs.gxt.ui.client.js.JsonConverter
     */
    private static class JsonConverter {

        /**
         * Encodes a map into a JSONObject.
         *
         * @param map
         *            the map
         * @return the JSONObject
         */
        public static JSONObject encode(Map<String, Object> map) {
            try {
                return encodeMap(map);
            } catch (JSONException e) {
                return null;
            }
        }

        protected static String encodeValue(Object value) {
            if (value instanceof Date) {
                return "d:" + ((Date) value).getTime();
            } else if (value instanceof Byte || value instanceof Integer
                    || value instanceof Short || value instanceof Long) {
                return "i:" + ((Number)value).longValue();
            } else if (value instanceof Number) {
                return "f:" + ((Number)value).doubleValue();
            }
            return "s:" + value.toString();
        }

        @SuppressWarnings("unchecked")
        protected static JSONObject encodeMap(Map<String, Object> data) throws JSONException {
            JSONObject jsobj = new JSONObject();
            for (String key : data.keySet()) {
                Object val = data.get(key);
                if (val instanceof Boolean) {
                    jsobj.put(key, (Boolean) val);
                } else if (val == null) {
                    jsobj.put(key, JSONObject.NULL);
                } else if (val instanceof Map) {
                    jsobj.put(key, encodeMap((Map<String, Object>) val));
                } else if (val instanceof List) {
                    jsobj.put(key, encodeList((List<Object>) val));
                } else {
                    jsobj.put(key, encodeValue(val));
                }
            }
            return jsobj;
        }

        @SuppressWarnings("unchecked")
        protected static JSONArray encodeList(List<Object> data) throws JSONException {
            JSONArray jsona = new JSONArray();
            for (int i = 0; i < data.size(); i++) {
                Object val = data.get(i);
                if (val instanceof Map) {
                    jsona.put(i, encodeMap((Map<String, Object>) val));
                } else if (val instanceof List) {
                    jsona.put(i, encodeList((List<Object>) val));
                } else if (val instanceof Boolean) {
                    jsona.put(i, (Boolean) val);
                } else if (val == null) {
                    jsona.put(i, JSONObject.NULL);
                } else {
                    jsona.put(i, encodeValue(val));
                }
            }
            return jsona;
        }
    }


}
