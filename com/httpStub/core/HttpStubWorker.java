package com.httpStub.core;

/**
class: HttpStubWorker
Purpose: new thread for HTTP stub.
Notes:
Author: Tim Lane
Date: 24/03/2014

**/

import com.sharkysoft.printf.Printf;
import com.sharkysoft.printf.PrintfTemplate;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.io.BufferedOutputStream;
import java.io.*;
import java.io.IOException;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.util.Random;
import java.util.Formatter;
import java.util.Date;
import java.util.Locale;
import java.net.URL; 
import java.sql.Timestamp;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringEscapeUtils;

public class HttpStubWorker implements Runnable {

    public static final String DELIMITED_TYPE = "Delimited";
    public static final String MULTIDELIMITED_TYPE = "MultiDelimited";
    public static final String EXTRACTVALUE_TYPE = "ExtractValue";
    public static final String FILE_TYPE = "File";
    public static final String NUMBER_TYPE = "Number";
    public static final String POSITIONAL_TYPE = "Positional";
    public static final String RANDOM_DOUBLE_TYPE = "RandomDouble";
    public static final String RANDOM_LONG_TYPE = "RandomLong";
    public static final String STRING_TYPE = "String";
    public static final String THREAD_TYPE = "Thread";
    public static final String TIMESTAMP_TYPE = "Timestamp";
    public static final String LOOKUP_TYPE = "Lookup";
    public static final String HEX_TYPE = "HEX";
    public static final String GUID_TYPE = "Guid";
    public static final String THREAD_COUNT_TYPE = "ThreadCount";
    public static final String CONTENT_LENGTH_TYPE = "ContentLength";
    
    private RandomNumberGenerator randomGenerator;
    
    private Socket clientSocket;
    private HttpProperties httpProperties;
    private CoreProperties coreProperties;
    private Logger logger;
          
    public HttpStubWorker(Socket clientSocket, 
                          HttpProperties httpProperties,
                          CoreProperties coreProperties,
                          Logger logger){
        this.clientSocket = clientSocket;
        this.httpProperties = httpProperties;
        this.coreProperties = coreProperties;
        this.logger = logger;
    }

    @Override
    public void run() {
      
      HttpInputStream inStream = null;
      String httpLine = null;
      String postHttpLine = null;
      String searchType = null;
      String searchValue = null;
      String receiverName = null;
      String baselineNameMatch = null;
      String responseMsg = null;
      boolean msgFound = false;
      float  waitFrom;
      float  waitTo;
      String  waitDistribution;
      String messageName; 
      String variableValue = null;
      String searchLine = null;
      double waitTime;
      SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");
      String formattedDate;
      String formattedStartDate;
      Date date;
      Date startDate;
      Date endDate;
      int postLength = 0;
      String firstLine = null;
      String threadName = null;
      
      ReceiverEvent receiverEvent = new ReceiverEvent() ;
      DatabaseEvent databaseEvent = new DatabaseEvent();
      List<String> databaseEvents = new ArrayList<String>();
      boolean dbEventFound=false;
      
      // read the input
      try
        {
        httpProperties.setActiveThreadCount();
        threadName = Printf.format("%.8d", new Object[] {Double.valueOf(Thread.currentThread().getId())});
                     
        /*
         * get header details 
         */
        Vector<String> inputMsgLines = new Vector<String>();
        try {
          inStream = new HttpInputStream(clientSocket.getInputStream());
          while ((httpLine = inStream.readLine()).length() > 0){
            inputMsgLines.addElement(httpLine);
            /*
             * get the content length for put post messages
             */
            if (httpLine.contains("Content-Length:")) {
              postLength = Integer.parseInt(httpLine.substring(16,(httpLine.length())));
            }
            
          }
        } catch (Exception e) {
          date = new Date();
          formattedDate = sdf.format(date);
          logger.error("httpStubWorker: " + formattedDate + " error in getting header : " + e);
          e.printStackTrace();
        }
        
        //logger.debug("httpStubWorker: inputMessage : " + inputMsgLines.toString());
        /*
         * if a POST or PUT message you'll need to read the remainder of the message
         */
        try {
          firstLine = (String)inputMsgLines.elementAt(0);
        } catch (Exception e) {
          date = new Date();
          formattedDate = sdf.format(date);
          logger.error("httpStubWorker: " + formattedDate + " error in getting first line : " + e);
          e.printStackTrace();
        }  
                
        String postLine = null;
        if (firstLine.substring(0,4).equalsIgnoreCase("POST") || 
            firstLine.substring(0,3).equalsIgnoreCase("PUT")) {
          try {
            postLine = inStream.readLine(postLength);
          } catch (Exception e) {
            date = new Date();
            formattedDate = sdf.format(date);
            logger.error("httpStubWorker: " + formattedDate + " error in getting body : " + e);
            e.printStackTrace();
          }
          inputMsgLines.addElement(postLine);
        }

        /* 
          * have got the INPUT message now find the matching Response
            <ReceiverEvent Name="TestPageRECEIVEREVENT" KeyType="STRING" KeyValue="test_page">
               <EventMessage BaselineMessage="TestPageEVENTMESSAGE" WaitDistribution="UNIFORM" MinWait="0.2" MaxWait="1.2"/>
            </ReceiverEvent>
            <BaselineMessage Name="TestPageEVENTMESSAGE"><![CDATA[<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"><soapenv:Body>You have sucesfully hit the GoMoney server at TIMESTAMP%TIMESTAMP%TIMESTAMP</soapenv:Body></soapenv:Envelope>]]></BaselineMessage> 
          *  
          * Loop through all RECEIVEREVENT and extract matching by KEYTYPE/STRING the EVENTMESSAGE 
          * to get the corresponding BASELINEMESSAGE
          * 
          */
        searchLine = inputMsgLines.toString();
        for (int i = 0; i < coreProperties.getReceiverEvents().size(); i++) {
           receiverEvent = (ReceiverEvent) coreProperties.getReceiverEvents().get(i);
           searchValue =  receiverEvent.getKeyValue();
           searchType = receiverEvent.getKeyType();
           if (searchType.toUpperCase().equals("STRING")) {
             if (searchLine.contains(searchValue)){
               receiverName = receiverEvent.getName();
               break; // found a message so stop looking
             }  
           } else {  // must be REGEX
               Pattern myPattern = Pattern.compile(searchValue);
               Matcher matcher = myPattern.matcher(searchLine);
               if (matcher.find()) {
                 receiverName = receiverEvent.getName();
                 break; // found a message so stop looking
               }
           }
        }
        if (logger.isInfoEnabled()) {
          logger.info(sdf.format(new Date()) 
                        + " RECV : T" + threadName + " " 
                        + receiverName + " " 
                        + inputMsgLines.toString().replaceAll("[\r\n]", ""));
        }
        /*
         * Once we have the receiver name then get the correspnding message config
         * to be returned. HTTP only allows one EVENT message per receiver so no need to loop through array.
         * get the wait distribution (type, min and max) from message config
         */
         EventMessage message = (EventMessage) receiverEvent.getMessages().get(0);
         messageName = message.getName();
         waitDistribution = message.getWaitDistribution();
         waitFrom = message.getWaitFrom();
         waitTo = message.getWaitTo(); 
         /*
          * once we have set the message config find the corresponding BASELINE message (response data)
          * in the baseline message array
          */
         for (int i = 0; i < coreProperties.getBaselineMessages().size(); i++) {
            BaseLineMessage baseLineMessage =  (BaseLineMessage) coreProperties.getBaselineMessages().get(i);
            if (baseLineMessage.getName().equals(messageName)){
              responseMsg = baseLineMessage.getCdata();
              break; // found a match so stop looking
            }
          }

        /*
         * now we have the response message need to replace all Variables, tagged with %varName%
         * in the response message.
         * So loop through all variables and see if they exist in the response message
         */
         boolean dbEventRequired=true;
         String httpVar;
         String dbEventStr;
         for (int i = 0; i < coreProperties.getVariables().size(); i++) {
           Variable variable =  (Variable) coreProperties.getVariables().get(i);
           CharSequence seq = "%" + variable.getName() + "%";
           /*
            * if they exist in the response message then process them
            */
           if (responseMsg.contains(seq)) { 
             variableValue = processMessage(searchLine,variable,responseMsg);
             responseMsg = responseMsg.replaceAll(seq.toString(), variableValue);
             /*
              * if the variable is tagged as a database write 
              * <Variable Name="PHONENUMBER" Type="String" Value="041122222" 
              *  DatabaseEvent="WRITECODE" />
              * then add it to arry for processing AFTER message has been sent
              */
             String variableDBEventCode = variable.getDatabaseEvent();
             dbEventRequired=true;
             for (i = 0; i < databaseEvents.size(); i++) {
               dbEventStr = databaseEvents.get(i);
               if (dbEventStr.contains(variable.getDatabaseEvent())){
                 if (variable.getDatabaseEvent() != null && !variable.getDatabaseEvent().equals("")) {
                     httpVar=variable.getName() + "=" + variableValue + ""; 
                     httpVar=dbEventStr + "&" + httpVar;
                     databaseEvents.set(i,httpVar);
                     dbEventRequired=false;
                     break;
                   }
               } else {
                 dbEventRequired=true;
               }
             }
             /*
              * if array is empty or write flag (dbEventRequired) is true then add a new entry
              */
             if (dbEventRequired){ // new entry
               if (variable.getDatabaseEvent() != null && !variable.getDatabaseEvent().equals("")) {
                 httpVar=variable.getDatabaseEvent() + ":" + variable.getName() + "=" + variableValue + ""; 
                 databaseEvents.add(httpVar) ;
               }
             }
           
        }
         }
         
        /*
         * we have all the required details to write a response, so it is here that we 
         * insert the delay in responding as per EVENT message config.
         */
        RandomNumberGenerator generator = new RandomNumberGenerator(waitDistribution, waitFrom, waitTo);
        waitTime = generator.randomDouble();
        long longWaitTime;
        try {
          longWaitTime = Double.valueOf(waitTime * 1000).longValue();
          Thread.sleep(longWaitTime);
        } catch (InterruptedException e) {
          date = new Date();
          formattedDate = sdf.format(date);
          logger.error("httpStubWorker: " + formattedDate + " error in thread sleep : " + e);
          e.printStackTrace();
        }

         /*
         * write the header and body
         */
        try {
          BufferedOutputStream outStream = new BufferedOutputStream(clientSocket.getOutputStream());
          /*
           * if its a close connection then dont write any data
           */
          if (!inputMsgLines.toString().contains("Connection: close")) {
            /*
             * if the mesage holds any escape codes for storage in XML , e.g. &93;
             * that require conversion at calling system then convert them to test ]
             */
            if (message.getDecodeEscape()){
              responseMsg = StringEscapeUtils.unescapeHtml4(responseMsg);
            }
            outStream.write(responseMsg.getBytes()); // write the response message.
          }
          /*
           * close all open file handles
           */ 
          outStream.flush(); 
          outStream.close();
          inStream.close();
          clientSocket.close();          
        } catch (java.io.IOException e) {
          date = new Date();
          formattedDate = sdf.format(date);
          logger.error("httpStubWorker: " + formattedDate + " error in writing message : " + e);
          e.printStackTrace();
        }
 
        /*
         * write the message to a log file, dependant on levle set
         */
        if (logger.isInfoEnabled()) {
          logger.info(sdf.format(new Date()) 
                        + " SEND : T" + threadName + " " 
                        + receiverName + " " 
                        + "Wait : " + String.format("%.3f", waitTime) + " "  
                        + responseMsg.replaceAll("[\r\n]", ""));
        }
                    
        } catch (Exception e) { 
          date = new Date();
          formattedDate = sdf.format(date);
          logger.error("httpStubWorker: " + formattedDate + " error writing to output stream. : " + e.toString());
          e.printStackTrace();  
        } finally {
        /*
         * write any save to data database here
         */
         String databaseString;
         /*
          * loop through all database events
          */
         for (int p = 0; p < databaseEvents.size(); p++) {
           /*
            * build database call
            */
           String[] parts = databaseEvents.get(p).split(":");
           for (int i = 0; i < coreProperties.getDatabaseEvents().size(); i++) {
             databaseEvent = (DatabaseEvent) coreProperties.getDatabaseEvents().get(i);
             if (databaseEvent.getName().equals(parts[0])){
               databaseString = "http://" + coreProperties.getDBServerIp() 
                                 + ":" + coreProperties.getDBServerPort()
                                 + "/" + databaseEvent.getPHPFile() 
                                 + "?" + parts[1];
               if (logger.isInfoEnabled()) {
                   date = new Date();
                   formattedDate = sdf.format(date);
                   logger.info("httpstubworker: " + formattedDate + " calling:  "  + databaseString);
               }
              /*
               * call xammp database via url
               */
               BufferedReader in = null;
               try
               {
                 URL xampp = new URL(databaseString);
                 URLConnection xamppUC = xampp.openConnection();
                 in = new BufferedReader(new InputStreamReader(xamppUC.getInputStream()));
                 String inputLine;
                 String httpResponse = "";
                 while ((inputLine = in.readLine()) != null) {
                   httpResponse = httpResponse + inputLine;                   
                 }
                 if (logger.isInfoEnabled()) {
                   date = new Date();
                   formattedDate = sdf.format(date);
                   logger.info("httpStubWorker: " + formattedDate + " HTTP response: " + httpResponse) ;
                 }
                   
               } catch (java.io.IOException e) {
                 date = new Date();
                 formattedDate = sdf.format(date);
                 logger.error("httpStubWorker: " + formattedDate + " error writing to database . : " + e.toString());
                 e.printStackTrace();           
               } finally {
                 try {
                   if (in != null) in.close();
                 } catch (java.io.IOException e) {
                   date = new Date();
                   formattedDate = sdf.format(date);
                   logger.error("httpStubWorker: " + formattedDate + " error writing to database . : " + e.toString());
                   e.printStackTrace();           
                 }
               }
             }
           }
         }
         
        }
        
    }

    private String processMessage(String httpLine, Variable variable, String responseMsg) {
      
      String variableValue = null; 
      if (variable.getType().equals(DELIMITED_TYPE)){
        variableValue = processDelimitedType(httpLine, variable);
      } else if (variable.getType().equals(CONTENT_LENGTH_TYPE)){
        variableValue = processContentLengthType(responseMsg);
      } else if (variable.getType().equals(TIMESTAMP_TYPE)){
        variableValue = processTimestampType(variable);
      } else if (variable.getType().equals(RANDOM_LONG_TYPE)){
        variableValue = processRandomLongType(variable);
      } else if (variable.getType().equals(POSITIONAL_TYPE)){
        variableValue = processPositionalType(httpLine, variable);
      } else if (variable.getType().equals(GUID_TYPE)){
        variableValue = processGuidType();
      } else if (variable.getType().equals(THREAD_COUNT_TYPE)){
        variableValue = processThreadCountType();
      } else if (variable.getType().equals(NUMBER_TYPE)){
        variableValue = processNumberType(variable);
      } else if (variable.getType().equals(STRING_TYPE)){
        variableValue = processStringType(variable);
      }
      
      return variableValue;
    }
           
    public String processDelimitedType(String httpLine, Variable variable){
      
      String variableValue = null;
      
      try {
        String[] rightOf = httpLine.split(variable.getRightOf());
        String[] leftOf = rightOf[1].split(variable.getLeftOf());
        variableValue = leftOf[0];
      } catch(Exception e){
        if (variable.getDefaultValue().length() > 0){
          logger.info("httpStubWorker : delimited variable : " + variable.getName() + " not found with: " 
                               + variable.getRightOf() + " : " + variable.getLeftOf() 
                               + " default value : " + variable.getDefaultValue() + " used.");
          variableValue = variable.getDefaultValue();
        } else {
          logger.error("httpStubWorker : delimited variable : " + variable.getName() + " not found with: " 
                               + variable.getRightOf() + " : " + variable.getLeftOf() + ". " + e); 
          variableValue = variable.getName() + " not found"; // avoid null pointer errors
        }
      }
      return variableValue;
    }
    
    public String processTimestampType(Variable variable){
      
      String variableValue = null;
      Calendar cal = Calendar.getInstance();
      /*
       * * apply a time offset if required
       */
      if (variable.getOffset() > 0){
        cal.add(Calendar.SECOND, variable.getOffset());
      } 
      SimpleDateFormat sdf = new SimpleDateFormat(variable.getFormat());
      variableValue = sdf.format(cal.getTime());
      return variableValue;
    } 

    public String processRandomLongType(Variable variable){
      
      String variableValue = null;
      Random random = new Random();
      long randMin = (long) variable.getRandMin(); 
      long randMax = (long) variable.getRandMax();
      long range = (long)randMax - (long)randMin + 1;
      long fraction = (long)(range * random.nextDouble());
      int randomNumber =  (int)(fraction + randMin);
      /*
       * apply the printf format if any
       */
      if (variable.getFormat().length() == 0) {
        variableValue = Double.valueOf(randomNumber).toString();
      } else {
        variableValue = Printf.format(variable.getFormat(), new Object[] {Double.valueOf(randomNumber)});
      }
      return variableValue;
    } 
    
    public String processNumberType(Variable variable)
   {
      
      String variableValue = variable.getNumberValue();
      double numberValue = Double.parseDouble(variableValue);
      
      if (variable.getFormat().length() == 0) {
        variableValue = Double.valueOf(numberValue).toString();
      } else {
        variableValue = Printf.format(variable.getFormat(), new Object[] {Double.valueOf(numberValue)});
      }
      /*
       * update the variable counter file
       */
      variable.updateNumberValue(variable.getFileName(),variableValue, variable.getIncrement());
            
      return variableValue;
    } 
    
    public String processPositionalType(String httpLine, Variable variable){
      
      String variableValue = null;

      try {
        int startPos = variable.getStartPosition();
        int strLength = startPos + variable.getLength();
        variableValue = httpLine.substring(startPos, strLength);
                                               
      } catch( Exception e ) {
        if (variable.getDefaultValue().length() > 0){
          variableValue = variable.getDefaultValue();
          logger.info("httpStubWorker: positional variable " + variable.getName() 
                        + " not found at: " + variable.getStartPosition() + " default value used.");
        } else {
          logger.error("httpStubWorker: positional variable " + variable.getName() 
                         + " not found at: " + variable.getStartPosition() + ". " + e);
          variableValue = variable.getName() + " not found"; // avoid null pointer errors
        }
      } 
      return variableValue;
    }
    
    public String processGuidType(){
      String variableValue = null;
      UUID idOne = UUID.randomUUID();
      variableValue = String.valueOf(idOne);
      return variableValue;
    }
    
    public String processStringType(Variable variable){
      String variableValue = variable.getValue();
      return variableValue;
    }
    
    public String processThreadCountType(){
      String variableValue = String.valueOf(httpProperties.getActiveThreadCount());
      return variableValue;
    }
    
    public String processContentLengthType(String responseMsg){
      String variableValue = null;
      // splitting on empty line, header in part 0, body is remainder 
      String[] parts = responseMsg.split("(?:\r\n|[\r\n])[ \t]*(?:\r\n|[\r\n])");
      // if response message has multiple blank line breaks
      // then loop through all adding the length
      if (parts.length > 2) { 
        int bodyLen = 0;
        // start on second part of string, avoids header
        for (int x=1; x<parts.length; x++){
          bodyLen += parts[x].length();  
        }
        bodyLen += parts.length; // add blank lines counter
        variableValue = Integer.toString(bodyLen);
      } else {
        // just determine length of second part.
        String stringBody = parts[1];
        variableValue = Integer.toString(stringBody.length());
      }
      
      return variableValue;
    }
        
}
