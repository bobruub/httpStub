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

public class DatabaseEventDetail {
  
  private String databaseEventDetailname;
  private String databaseEventName;
  private String dbField;
  
  public void setName(String databaseEventDetailname){
    this.databaseEventDetailname = databaseEventDetailname;
    
  }
  
  public String getName(){
    return this.databaseEventDetailname;
  }
  
  public void setDBEventName(String databaseEventName){
    this.databaseEventName = databaseEventName;
    
  }
  
  public String getDBEventName(){
    return this.databaseEventName;
  }
  
  public void setDBField(String dbField){
    this.dbField = dbField;
    
  }
  
  public String getDBField(){
    return this.dbField;
  }
 
  
}
