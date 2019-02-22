package com.branegy.dbmaster.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.google.common.base.Preconditions;

import com.branegy.persistence.custom.BaseCustomEntity;

@MappedSuperclass
public abstract class DatabaseObject<P extends DatabaseObject<?>> extends BaseCustomEntity {

   @Transient
   public abstract String getName();

   /**
    * merge newList into oldList
    * @param oldList
    * @param newList
    * @return
    */
   @SuppressWarnings("unchecked")
   final <V extends DatabaseObject<?>, T extends DatabaseObject<V>> List<T> mergeList(List<T> oldList,
           List<T> newList){
       if (newList==null && oldList!=null){
           oldList.clear();
       } else if (newList!=null && oldList==null){
           oldList = new ArrayList<T>(newList);
       } else if (newList!=null && oldList!=null){
           oldList.clear();
           oldList.addAll(newList);
       }
       if (oldList != null) {
           for (T t : oldList) {
              t.setParent((V)this);
           }
       }
       return oldList;
   }
   
   @SuppressWarnings("unchecked")
   final <V extends DatabaseObject<?>,T extends DatabaseObject<V>> List<T> addChild(List<T> list, T child,
           String parameterName){
       Preconditions.checkNotNull(child, "Parameter "+parameterName+" can not be null");
       if (list == null){
           list = new ArrayList<T>();
       }
       child.setParent((V)this);
       list.add(child);
       return list;
   }
   
   final <V extends DatabaseObject<?>,T extends DatabaseObject<V>> void removeChild(List<T> list, T child,
           String parameterName){
       Preconditions.checkNotNull(child, "Parameter "+parameterName+" can not be null");
       /* use equals in base entity
        *   equals by id for different context
        *   equals by reference for new
        */
       Preconditions.checkArgument(list.contains(child), "Parameter "+parameterName+" has different parent");
       if (list!=null){
           child.setParent(null);
           list.remove(child);
       }
   }
   
   final <T extends DatabaseObject<?>> List<T> unmodifiableList(List<T> field){
       if (field==null) {
           return Collections.emptyList();
       } else {
           return Collections.unmodifiableList(field);
       }
   }
   
   final <T extends DatabaseObject<?>> T findByName(List<T> list, String name, String parameterName){
       Preconditions.checkNotNull(name, "Parameter "+parameterName+" can not be null");
       if (list == null) {
           return null;
       } else {
           for (T t : list) {
               if (name.equalsIgnoreCase(t.getName())) {
                   return t;
               }
           }
           return null;
       }
   }
   
   abstract void setParent(P parent);
   abstract P getParent();
}
