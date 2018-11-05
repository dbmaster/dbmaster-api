package com.branegy.tools.model;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleWiring;

import com.branegy.dbmaster.custom.CustomFieldConfig.Type;
import com.branegy.tools.api.ExportType;
import com.branegy.tools.api.ToolExecutionStatusException;
import com.branegy.tools.model.ToolHistory.Status;

public class ToolConfig {

    /**
     * Tool id - shouldn't be visible to users.
     * Used for reference.
     */
    String id;
    
    /**
     * parent id for permission
     */
    String parentId;

    /**
     * Title displayed in the list of tools.
     * i18n
     */
    String title;

    /**
     * Brief description of the tool, its purpose.
     * i18n
     */
    String description;
    
    /**
     * Extensive text to describe parameters and how the tool works in details.
     */
    String help;
    
    /*INVENTORY,MODELING*/
    String projectTypes;
    
    /**
     * When false the tool shoudn't be displayed in global list of tools
     */
    boolean hidden;

    /**
     * Parameters to be rendered on ui and passed to script
     */
    List<Parameter> parameters = new ArrayList<Parameter>(10);

    List<OutputEngine> outputEngines;

    private transient Bundle bundle;
    
    public ToolConfig(String id, String parentId, String title) {
        this.id = id;
        this.parentId = parentId;
        this.title = title;
    }

    /**
     * @return a classloader used to run tools
     */
    public ClassLoader getToolClassLoader() {
        BundleWiring wiring = bundle.adapt(BundleWiring.class);
        if (wiring != null) {
            ClassLoader cl = wiring.getClassLoader();
            if (cl != null) {
                return cl;
            }
        }
        throw new ToolExecutionStatusException("Plugin [" + bundle.getSymbolicName() + "/"
                + bundle.getVersion() + "] is during installation or uninstallation", Status.FAILED);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isVisible() {
        return !hidden;
    }

    public void setVisible(boolean visible) {
        this.hidden = !visible;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public Parameter addParameter(String name, Type type, String title) {
        Parameter parameter = new Parameter(name,type,title);
        parameters.add(parameter);
        return parameter;
    }

    public Parameter addParameter(String name, Type type, String title, String defaultValue) {
        Parameter parameter = new Parameter(name, type, title, defaultValue);
        parameters.add(parameter);
        return parameter;
    }

    public String getHelpDescription() {
        return help;
    }

    public void setHelpDescription(String help) {
        this.help = help;
    }

    public List<OutputEngine> getOutput() {
        return outputEngines;
    }

    public void setOutput(List<OutputEngine> output) {
        this.outputEngines = output;
    }

    public OutputEngine getEngine(ExportType type){
        for (OutputEngine e:outputEngines){
            if (e.getExportType().contains(type)){
                return e;
            }
        }
        throw new IllegalArgumentException();
    }
    
    public boolean hasProjectType(String name) {
        return projectTypes == null
               || (","+projectTypes.toUpperCase()+",").contains(","+name.toUpperCase()+",")
               || (" "+projectTypes.toUpperCase()+" ").contains(" "+name.toUpperCase()+" ");
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof ToolConfig))
            return false;
        ToolConfig other = (ToolConfig) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public Version getVersion(){
        return bundle.getVersion();
    }

    public String getProjectTypes() {
        return projectTypes;
    }

    public void setProjectTypes(String projectTypes) {
        this.projectTypes = projectTypes;
    }

    public String getParentId() {
        return parentId;
    }
    
    public String getParentOrToolId(){
        return parentId!=null?parentId:id;
    }
}
