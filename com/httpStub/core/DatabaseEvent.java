package com.httpStub.core;

/**
class: ReceiverEvent
Purpose: setup the receiver events.
Notes:
Author: Tim Lane
Date: 25/03/2014

**/

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseEvent {
  
  private String dbTable;
  private String dbEventName;
  private String phpFile;
  private String dbServerIP;
  
  
  private List<DatabaseEventDetail> databaseEventDetails = new ArrayList<DatabaseEventDetail>();
  
    public List getMessages() {
        return databaseEventDetails;
    }

    public void addMessage(DatabaseEventDetail databaseEventDetail)
    {
        databaseEventDetails.add(databaseEventDetail);
    }
    
  public void setDBTable(String dbTable){
    this.dbTable = dbTable;
  }  
  
   public String getDBTable(){
    return this.dbTable;
  }
   
   public void setName(String dbEventName){
    this.dbEventName = dbEventName;
  }  
  
   public String getName(){
    return this.dbEventName;
  }
   
    public void setPHPFile(String phpFile){
    this.phpFile = phpFile;
  }  
  
   public String getPHPFile(){
    return this.phpFile;
  }
   
   public void setDBServerIP(String dbServerIP){
    this.dbServerIP = dbServerIP;
  }  
  
   public String getDBServerIP(){
    return this.dbServerIP;
  }
   
  /* ADD YOUR CODE HERE */
  
}
