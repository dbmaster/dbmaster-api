package com.branegy.cfg;

import java.util.Set;

public interface IReloadPropertySupplier extends IPropertySupplier {

    void addListener(PropertiesChangeCallback callback);
    void removeListener(PropertiesChangeCallback callback);

    interface PropertiesChangeCallback {
        void onPropertiesChanged(Set<String> changesKeys,
                IPropertySupplier values, IPropertySupplier oldValues);
    }

}
