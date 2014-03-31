package com.httpStub.core;

/**
class: HttpBaseLineMessage
Purpose: the baseline message setup
Notes:
Author: Tim Lane
Date: 24/03/2014

**/
public class HttpBaseLineMessage {
  
  private String baseLineMessage;
  private String baseLineName;
  
   public void setName(String baseLineName){
    this.baseLineName = baseLineName;
  }
  
  public String getName(){
    return baseLineName;
  }
      
  public void setMessage(String baseLineMessage){
    this.baseLineMessage = baseLineMessage;
  }
  
  public String getMessage(){
    return baseLineMessage;
  }
  
}
