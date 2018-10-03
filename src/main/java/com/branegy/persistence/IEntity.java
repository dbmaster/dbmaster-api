package com.branegy.persistence;

import java.util.Date;

public interface IEntity {

    long getId();

    Date getUpdated();
    Date getCreated();
    String getCreateAuthor();
    String getUpdateAuthor();

}
