package com.httpStub.core;

/**
class: HttpStub
Purpose: main method for HTTP stubbing
Notes: http only
Author: Tim Lane
Date: 24/03/2014
Version: 
0.1 24/03/2014 lanet - initial write
**/

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class httpStub {
  
  private HttpProperties httpProperties;
  private ServerSocket serverSocket;
  private HttpBaseLineMessage httpBaseLineMessage;
  private LogFileProperties logFileProperties;
  private static String httpVersion = "1.0a";
  
  // Create an HTTPStub for a particular TCP port
  public httpStub(HttpProperties httpProperties, LogFileProperties logFileProperties)
  {
    this.httpProperties = httpProperties;
    this.logFileProperties = logFileProperties;
  }
    
  static Logger logger = Logger.getLogger(httpStub.class);
  public static void main(String[] args) {
    
    /*
     * get config file, need command line option
     */
    String configFileName = null;
    
    System.out.println("version " + httpVersion);
    
    if (args.length > 0) {
      configFileName = args[0];
      System.out.println("using config file: " + configFileName);
    } else {
      configFileName = "d:\\dropbox\\java\\httpstub\\vie_test.xml";
      System.out.println("using default config file: " + configFileName);
    } 
    
    try {
      /*
       * open XML config file and from xml config file read HTTP properties
       */
      XMLExtractor extractor = new XMLExtractor(new FileInputStream(new File(configFileName)));
      HttpProperties httpProperties = new HttpProperties(extractor.getElement("HTTPServer"));
      httpProperties.setConfigFileName(configFileName);
     
     /*
     * setup logging
     * TRACE < DEBUG < INFO < WARN < ERROR < FATAL
     */
      LogFileProperties logFileProperties = new LogFileProperties(extractor.getElement("Header")) ;
      System.out.println("log config file : " + logFileProperties.getLogFileName()); 
      PropertyConfigurator.configure(logFileProperties.getLogFileName());
      System.out.println("Log4j appender configuration is successful");
      logger.info("Info version " + httpVersion);
      
      httpStub httpStub = new httpStub(httpProperties, logFileProperties);
      httpStub.RunIsolator();
    } catch (Exception e) {
      logger.error("error extracting XML file " + configFileName);
      //e.printStackTrace();
       System.exit(1);
    }
   
  }
    
    ServerSocket getServerSocket() throws Exception {
        logger.info("Preparing a regular HTTP Server Socket on server:port " + httpProperties.getServerIP() + ":" + httpProperties.getServerPort());
        return new ServerSocket (httpProperties.getServerPort(),
                                 httpProperties.getServerBacklog(), 
                                 InetAddress.getByName(httpProperties.getServerIP()));

    }
    
  public void RunIsolator() {
    
    CoreProperties coreProperties = new CoreProperties(httpProperties.getConfigFileName());
    /*
     * display stub information to log file
     */
    logger.info("Author : " + coreProperties.getAuthor()
                  + " Name : " + coreProperties.getName()
                  + " Description : " + coreProperties.getDescription()
                  + " Date : " + coreProperties.getDate());
    /*
     * load the variable configurations
     */
    for (int i = 0; i < coreProperties.getVariables().size(); i++) {
      Variable variable =  (Variable) coreProperties.getVariables().get(i);
    }
    /*
     * load the baseline response message templates
     */
    for (int i = 0; i < coreProperties.getBaselineMessages().size(); i++) {
      BaseLineMessage baseLineMessage =  (BaseLineMessage) coreProperties.getBaselineMessages().get(i);
    }
    /*
     * load the receiver events and the associated messages
     */
    for (int i = 0; i < coreProperties.getReceiverEvents().size(); i++) {
      ReceiverEvent receiverEvent =  (ReceiverEvent) coreProperties.getReceiverEvents().get(i);
      int numberOfMessages = receiverEvent.getMessages().size();
      for (int c = 0; c < numberOfMessages;  c++ ) {
        EventMessage message = (EventMessage) receiverEvent.getMessages().get(c);
      }
    }
    /*
     * setup thread pool
     */
    logger.info("setting up threadpool of size : " + httpProperties.getThreadCount()); 
    ExecutorService executor = Executors.newFixedThreadPool(httpProperties.getThreadCount());

    boolean socketLoop = true;
    boolean connectionLoop = true;
    int connectionLoopCntr = 0;
    while (socketLoop) {
    
      serverSocket = null;
      try {
        /*
         * open the socket
         */
        serverSocket = getServerSocket();
        serverSocket.setSoTimeout(5 * 1000);
        
      } catch (Exception e) {
        logger.error("Unable to listen on " + httpProperties.getServerIP() + ":" + httpProperties.getServerPort());
        //e.printStackTrace();
        // exit on fail to bind port id.
        System.exit(1);
      }
      /*
       * listen for connections...
       */
      while (connectionLoop){
        try {
          /*
           * accept connections a connection on a new socket
           */
          Socket clientConnection = null;
          clientConnection = serverSocket.accept();
          clientConnection.setSoTimeout(5 * 1000);
          /*
           * Handle the connection with a separate thread                     
           */
          if (clientConnection != null) {
            Runnable httpStubWorker = new HttpStubWorker(clientConnection, 
                                                         httpProperties,
                                                         coreProperties,
                                                         logger);
            executor.execute(httpStubWorker);
            
          }
                    
        } catch (SocketTimeoutException e) {
          // System.out.println("socket timeout " + connectionLoopCntr + ".");
          // DO NOTHING - The timeout just allows the checking of the restart
          // request and will only close the socket server if a restart request
          // has been issued
        } catch (Exception e) {
          logger.error("socket exception.");
          //e.printStackTrace();
        } finally {
          
        }
            
      }
      /* 
       * shutdown threads
       */
      executor.shutdown();
      while (!executor.isTerminated()) {
      }
                  
    }
    
    
  }
  
  

  
}

