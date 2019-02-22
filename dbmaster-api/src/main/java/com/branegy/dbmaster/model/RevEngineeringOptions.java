package com.branegy.dbmaster.model;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang.StringUtils;

import com.branegy.service.core.exception.IllegalArgumentApiException;

@Embeddable
public class RevEngineeringOptions {
    private static final Pattern FILTERS_PATTERN = Pattern.compile("\\s*(?<op>[+-]?)\\s*(?<type>[^:]+)\\s*:\\s*(?<name>.+)\\s*");

    @Size(min=1,max=255)
    String database;
    
    @Access(AccessType.PROPERTY)
    @javax.persistence.Column(name="config",length=8*1024,nullable=false)
    @NotNull
    String rawConfig;
    
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }
    
    public interface Filter {
        String getName();
        boolean isPattern();
        boolean isInclude();
        boolean accept(String name);
    }
    
    private static final class FilterItemImpl implements Filter{
        private final String name;
        private final boolean pattern;
        private final boolean include;
        private Pattern compiledPattern;
       
        public FilterItemImpl(String name, boolean pattern, boolean include) {
            this.name = name;
            this.pattern = pattern;
            this.include = include;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isPattern() {
            return pattern;
        }

        @Override
        public boolean isInclude() {
            return include;
        }

        @Override
        public boolean accept(String name) {
            if (pattern) {
                if (StringUtils.isEmpty(name)) {
                    return false;
                }
                if (compiledPattern == null) {
                    compiledPattern = Pattern.compile(
                        ("\\Q" + this.name + "\\E")
                            .replace("*", "\\E.*\\Q")
                            .replace("?", "\\E.\\Q")
                    );
                }
                return compiledPattern.matcher(name).matches();
            } else {
                return this.name.equals(name);
            }
        }
        
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (include ? 1231 : 1237);
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + (pattern ? 1231 : 1237);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            FilterItemImpl other = (FilterItemImpl) obj;
            if (include != other.include) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            if (pattern != other.pattern) {
                return false;
            }
            return true;
        }
    }
    
    private static class FilterList{
        private final boolean includeByDefault; // = no explicit include filters
        private final List<Filter> filters;
        
        public FilterList(boolean includeByDefault, List<Filter> filters) {
            this.includeByDefault = includeByDefault;
            this.filters = filters;
        }
        public boolean isIncludeByDefault() {
            return includeByDefault;
        }
        public List<Filter> getFilters() {
            return filters;
        }
    }
    
    @Transient
    volatile Map<String,FilterList> filtersByType = new HashMap<>(); 
    
    // if empty             -> includeByDefault = false       | not import
    // if exclude only      -> includeByDefault = true        | include all, except excluded
    // if include only      -> includeByDefault = false       | 
    // if include + exclude -> includeByDefault = false
    
    public String getRawConfig() {
        return rawConfig;
    }
    
    public synchronized void setRawConfig(String config) {
        if (config == null) {
            this.rawConfig = "";
            this.filtersByType = Collections.emptyMap();
            return;
        }
        if (config.equals(this.rawConfig)) {
            return;
        }
        Map<String, Set<FilterItemImpl>> filters = Pattern
        .compile("(\\s*[\\r\\n]\\s*)+")
        .splitAsStream(config)
        .map(line->{
            Matcher matcher = FILTERS_PATTERN.matcher(line);
            if (!matcher.matches()) {
                throw new IllegalArgumentApiException(line+" has an unexpected format");
            }
            
            boolean include = !"-".equals(matcher.group("op"));
            String type = matcher.group("type");
            String name = matcher.group("name");
            boolean pattern = StringUtils.containsAny(name, "?*");
            return new SimpleEntry<>(type,new FilterItemImpl(name, pattern, include));
        }).collect(Collectors.groupingBy(SimpleEntry::getKey, 
                   Collectors.mapping(SimpleEntry::getValue, 
                               Collectors.toCollection(LinkedHashSet::new))));
        
        Map<String, FilterList> filtersByType = new HashMap<>();
        for (Entry<String, Set<FilterItemImpl>> e:filters.entrySet()) {
            Set<FilterItemImpl> set = e.getValue();
            boolean includeAll = !set.stream().anyMatch(FilterItemImpl::isInclude);
            filtersByType.put(e.getKey(), new FilterList(includeAll, new ArrayList<>(set)));
            assert !set.isEmpty();
        }
        this.filtersByType = filtersByType;
        this.rawConfig = config;
    }
    
    public boolean isExcludedObjectType(String objectType) {
        FilterList filterList = filtersByType.get(objectType);
        if (filterList == null) {
            return true;
        }
        List<Filter> filters = filterList.getFilters();
        return filters.size()==1 && "*".equals(filters.get(0).getName()) && !filters.get(0).isInclude();
    }
    
    public boolean isIncludeByDefault(String objectType) {
        FilterList filterList = filtersByType.get(objectType);
        if (filterList == null) {
            return false;
        }
        return filterList.isIncludeByDefault();
    }
    
    public List<Filter> getIncludedObjects(String objectType){
        FilterList filterList = filtersByType.get(objectType);
        if (filterList == null) {
            return Collections.emptyList();
        }
        if (filterList.isIncludeByDefault()) {
            return Collections.singletonList(new FilterItemImpl("*", true, true));
        }
        return filterList.getFilters().stream()
                                      .filter(Filter::isInclude)
                                      .collect(Collectors.toList());
    }
    
    public List<Filter> getExcludedObjects(String objectType){
        FilterList filterList = filtersByType.get(objectType);
        if (filterList == null) {
            return Collections.emptyList();
        }
        return filterList.getFilters().stream()
                                      .filter(fi->!fi.isInclude())
                                      .collect(Collectors.toList());
    }
    
    public boolean accept(String objectType, String name) {
        FilterList filterList = filtersByType.get(objectType);
        if (filterList == null) {
            return false;
        }

        ListIterator<Filter> it = filterList.getFilters().listIterator();
        while (it.hasPrevious()) {
            Filter item = it.previous();
            if (item.accept(name)) {
                return item.isInclude(); 
            }
        }
        return false;
    }

   
}
