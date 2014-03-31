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
      
      
      ReceiverEvent receiverEvent = new ReceiverEvent() ;
      
      
      // read the input
      try
        {
        
        Vector<String> inputMsgLines = new Vector<String>();
        /*
         * get header details 
         */
        int postLength = 0;
        inStream = new HttpInputStream(clientSocket.getInputStream());
        while ((httpLine = inStream.readLine()).length() > 0){
           inputMsgLines.addElement(httpLine);
        }
        /*
         * if a POST or PUT message you'll need to read the remainder of the message
         */
        String firstLine = (String)inputMsgLines.elementAt(0); 
        String thisLine = null;
        String postLine = null;
        if (firstLine.substring(0,4).equalsIgnoreCase("POST") || 
            firstLine.substring(0,3).equalsIgnoreCase("PUT")) {
            for (Iterator<String>  iterator = inputMsgLines.iterator(); iterator.hasNext();){
              thisLine = (String)iterator.next();
              if ((thisLine.length() > 0) && (thisLine.length() > 16)) {
                if (thisLine.substring(0,15).equalsIgnoreCase("Content-Length:")) {
                  postLength = Integer.parseInt(thisLine.substring(16,(thisLine.length())));
                  break; // found the content length so stop looking
                }
              }
            }
          postLine = inStream.readLine(postLength);
          inputMsgLines.addElement(postLine);
        }
         
        // also need to add a close option and keep alive but only rarely needed.
   
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
           logger.trace("httpStubWorker: search line : " + searchLine
                          + " searchValue : " + searchValue
                          + " searchType : " + searchType);
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
         logger.trace("httpStubWorker: waitfrom " + waitFrom + " waitTo " + waitTo);
         
         /*
          * once we have set the message config find the corresponding baseline message (response data)
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
         String threadName = Printf.format("%.4d", new Object[] {Double.valueOf(Thread.currentThread().getId())});
         Date date = new Date();
         SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");
         String formattedDate = sdf.format(date);
         logger.info(formattedDate + " RECV : " + threadName + " " + receiverName + " " + searchLine.replaceAll("[\r\n]", ""));

         for (int i = 0; i < coreProperties.getVariables().size(); i++) {
           Variable variable =  (Variable) coreProperties.getVariables().get(i);
           CharSequence seq = "%" + variable.getName() + "%";
           if (responseMsg.contains(seq)) { 
             variableValue = processMessage(searchLine,variable);
             responseMsg = responseMsg.replaceAll(seq.toString(), variableValue);
           }
         }
        
        /*
         * we have all the required details to write a response, so it is here that we 
         * insert the delay in responding as per EVENT message config.
         */
        RandomNumberGenerator generator = new RandomNumberGenerator(waitDistribution, waitFrom, waitTo);
        long timeToSleep = Double.valueOf(generator.uniformRandom(waitFrom, waitTo)).longValue();
        String sleepTime = String.format("%.2f", generator.uniformRandom(waitFrom, waitTo));
        Thread.currentThread().sleep(timeToSleep * 1000);
 
        /*
         * setup HTTP header
        */
        String httpOutStream = "HTTP/1.1 200 OK\r\n";
        String contentLength = "Content-Length: " + responseMsg.length() + "\r\n";
        String keepAlive = "Connection: close\r\n";
        String contentType = "Content-Type: text/html; charset=utf-8\r\n";
        String contentLanguage = "Content-Language: en\r\n";
        /*
         * write the header and body
         */
        BufferedOutputStream outStream = new BufferedOutputStream(clientSocket.getOutputStream());
        outStream.write(httpOutStream.getBytes());
        outStream.write(contentLength.getBytes());
        outStream.write(keepAlive.getBytes());
        outStream.write(contentType.getBytes());
        outStream.write(contentLanguage.getBytes());
        outStream.write("\r\n".getBytes()); // blank line before data
        outStream.write(responseMsg.getBytes()); // write the response message.
        outStream.flush(); 
        outStream.close();
        clientSocket.close();
         
        date = new Date();
        formattedDate = sdf.format(date);
        logger.info(formattedDate + " SEND : " + threadName + " " 
                      + "Wait : " + sleepTime + " "  
                      + receiverName + " " 
                      + responseMsg.replaceAll("[\r\n]", ""));
                     
      } catch (Exception e) {
          logger.error("httpStubWorker: error writing to output stream.");
          e.printStackTrace();  
      }
    }

    private String getReceiverName(String httpLine, CoreProperties coreProperties){
      String receiverName = null;
      
      return receiverName;
      
      
    }

    private String getResponseMsg(String httpLine, CoreProperties coreProperties, String messageName){
      String responseMsg = null;
      
      return responseMsg;
      
      
    }

    private String processMessage(String httpLine, Variable variable) {
      
      String variableValue = null; 
      if (variable.getType().equals(DELIMITED_TYPE)){
        variableValue = processDelimitedType(httpLine, variable);
      } else if (variable.getType().equals(TIMESTAMP_TYPE)){
        variableValue = processTimestampType(variable);
      } else if (variable.getType().equals(RANDOM_LONG_TYPE)){
        variableValue = processRandomLongType(variable);
      } else if (variable.getType().equals(POSITIONAL_TYPE)){
        variableValue = processPositionalType(httpLine, variable);
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
          logger.info("httpStubWorker : delimited variable not found with: " 
                               + variable.getRightOf() + " : " + variable.getLeftOf() 
                               + " default value : " + variable.getDefaultValue() + " used.");
          variableValue = variable.getDefaultValue();
        } else {
          logger.error("httpStubWorker : delimited variable not found with: " 
                               + variable.getRightOf() + " : " + variable.getLeftOf() + "."); 
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
    
    public String processPositionalType(String httpLine, Variable variable){
      
      String variableValue = null;

      try {
        int startPos = variable.getStartPosition();
        int strLength = startPos + variable.getLength();
        variableValue = httpLine.substring(startPos, strLength);
                                               
      } catch( Exception e ) {
        if (variable.getDefaultValue().length() > 0){
          variableValue = variable.getDefaultValue();
          logger.info("Variable: positional variable not found at: " + variable.getStartPosition() + " default value used.");
        } else {
          logger.error("Variable: positional variable not found at: " + variable.getStartPosition() +".");
        }
      } 
      return variableValue;
}
}
