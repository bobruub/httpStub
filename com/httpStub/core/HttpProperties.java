package com.httpStub.core;

/**
class: HttpServerProperties
Purpose: holds server properties for HTTP requests.
Notes:
Author: Tim Lane
Date: 24/03/2014

**/

import java.io.*;
import org.w3c.dom.Element;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class HttpProperties {
  
  private String serverIP;
  private int ServerPort;
  private int ServerBacklog;
  private String configFileName;
  private int threadCount;
  
  public static final String HOST_TAG = "Host";
  public static final String PORT_TAG = "Port";
  public static final String THREAD_COUNT_TAG = "ThreadCount";
  
  
  boolean testFlag = false;
  
  public HttpProperties(Element httpReceiverElement) {
    
    setServerIP(httpReceiverElement.getAttribute(HOST_TAG));
    setServerPort(httpReceiverElement.getAttribute(PORT_TAG));
    setThreadCount(httpReceiverElement.getAttribute(THREAD_COUNT_TAG));
    
  }
  
  public void setThreadCount(String threadCount){
    this.threadCount = Integer.parseInt(threadCount);
  }
  
  public int getThreadCount(){
    return threadCount;
  }
  
  public void setServerIP(String serverIP){
    this.serverIP = serverIP;
  }
  
  public String getServerIP(){
    return serverIP;
  }
  
   public void setConfigFileName(String configFileName){
    this.configFileName = configFileName;
  }
  
  public String getConfigFileName(){
    return configFileName;
  }
  
  public void setServerPort(String ServerPort){
    this.ServerPort = Integer.parseInt(ServerPort);
  }

  public void setServerBacklog(String ServerBacklog){
    this.ServerBacklog = Integer.parseInt(ServerBacklog);
  }


  
  public int getServerPort(){
    return ServerPort;
    
  }
  
  public int getServerBacklog(){
    return ServerBacklog;
  }

  public boolean setTestFlag(String testFlagStr){
    if (testFlagStr.toUpperCase().matches("TRUE")){
      return this.testFlag = true;
    } else {
      return this.testFlag = false;
    }
  
  }

  public boolean getTestFlag(){
    return testFlag;
  }
 


}
