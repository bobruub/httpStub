package com.httpStub.core;

/**
class: BaseLineMessage
Purpose: setup the baseline respopnse message.
Notes:
Author: Tim Lane
Date: 25/03/2014

**/

import java.util.List;

public class BaseLineMessage {
  
    private String  Name;
    private String  Cdata;

    public static BaseLineMessage findInList(List templateList, String templateName)
    {
        for (Object aTemplateList : templateList)
        {
            BaseLineMessage tmp = (BaseLineMessage) aTemplateList;
            if (tmp.getName().equals(templateName))
            {
                return tmp;
            }
        }
        return null;
    }
    
    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getCdata() {
        return Cdata;
    }

    public void setCdata(String cdata) {
        Cdata = cdata;
    }

}
