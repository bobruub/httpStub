package com.httpStub.core;

/**
class: coreProperties
Purpose: extracts all the core details for the request/response/variables, etc from xml file.
Notes:
Author: Tim Lane
Date: 24/03/2014
**/

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class CoreProperties {

  
    public static final String CORE_TAG = "Core";
    public static final String HEADER_TAG = "Header";
    public static final String VARIABLE_TAG = "Variable";
    public static final String EVENT_RECEIVER_TAG = "ReceiverEvent";
    public static final String EVENT_SENDER_TAG = "SenderEvent";
    public static final String BASELINE_MESSAGE_TAG = "BaselineMessage";
    public static final String EVENT_MESSAGE_TAG = "EventMessage";
    public static final String DATABASE_TAG = "DataBase";
    public static final String DATABASE_EVENT_TAG = "DatabaseEvent";
    public static final String DATABASE_DETAILS_TAG = "DatabaseEventDetail";
    
        
    private String logFileName;
    private String logLevel;
    private String author;
    private String name;
    private String description;
    private String date;
    private int numberValue;
    /*
     * database element variables
     */
    private String DBServerIp;
    private String DBServerPort;
    private String DBName;
    private String DBUserName;
    private String DBUserPassword;
    
    private List<Variable>  variables = new ArrayList<Variable>();
    private List<BaseLineMessage>  baselineMessages = new ArrayList<BaseLineMessage>();
    private List<ReceiverEvent> receiverEvents = new ArrayList<ReceiverEvent>();
    private List<DatabaseEvent> databaseEvents = new ArrayList<DatabaseEvent>();
    
    
    public CoreProperties(){
    }
    
    public CoreProperties(String configFileName, Logger logger){
      
      try {
        FileInputStream inFile = new FileInputStream(new File(configFileName));
                                                     
        XMLExtractor extractor = new XMLExtractor(inFile);
        /*
         * get core details
         * <Core Author="Tim Lane" Name="ESG KISS V0.5" Description="ESG KISS Stub Port 9001" Date="07/02/2014">
         */
         Element coreElement = extractor.getElement(CORE_TAG);
         setAuthor(coreElement.getAttribute("Author"));
         setName(coreElement.getAttribute("Name"));
         setDescription(coreElement.getAttribute("Description"));
         setDate(coreElement.getAttribute("Date"));
        
        /*
         * get database details
         *  <DataBase DBServerIp="10.1.1.1" DBServerPort="80" DBName="DB_name" 
         *             DBUserName="root" DBUserPassword="password" />
        
        Note: deprecated, all database access is now via DatabaseEvents and uses
                          a xampp server.
        
        Element databaseElement = extractor.getElement(DATABASE_TAG);
        setDBServerIp(databaseElement.getAttribute("DBServerIp"));
        setDBServerPort(databaseElement.getAttribute("DBServerPort"));
        setDBName(databaseElement.getAttribute("DBName"));
        setDBUserName(databaseElement.getAttribute("DBUserName"));
        setDBUserPassword(databaseElement.getAttribute("DBUserPassword"));
         */
         
        /* 
         * get variables
         * <Variable Name="TIMESTAMP" Type="Timestamp" Format="HH:mm:ss"/>
        */
        NodeList variableNodes = extractor.getNodeList(VARIABLE_TAG);
        for (int s = 0; s < variableNodes.getLength(); s++)
        {
            Element variableElement = (Element)variableNodes.item(s);
            addVariable(getVariable(variableElement,logger));
        }
        
        /* 
         * get baseline nodes
         * <BaselineMessage Name="TestPage"><![CDATA[respopnse message]]</BaselineMessage> 
        */        
        NodeList baselineMsgNodes = extractor.getNodeList(BASELINE_MESSAGE_TAG);
        for (int s = 0; s < baselineMsgNodes.getLength(); s++) {
            Element baselineMsgElement = (Element)baselineMsgNodes.item(s);
            addBaselineMessage(getBaselineMessage(baselineMsgElement));
        }
        
        /* 
         * get Receiver events nodes
         *  <ReceiverEvent Name="3.4_GetListTokenTrans-RIB" KeyType="STRING" KeyValue="type=HISTORY">
        */
        NodeList receiverNodes = extractor.getNodeList(EVENT_RECEIVER_TAG);
        for (int s = 0; s < receiverNodes.getLength(); s++) {
            Element receiverElement = (Element)receiverNodes.item(s);
            addReceiverEvent(getReceiverEvent(receiverElement, getBaselineMessages()));
        }
        
        /*
         * get database envent nodes
         *  <DatabaseEvent Name="WRITECODE"  PHPFile="insert.php"  />
         */
        NodeList databaseNodes = extractor.getNodeList(DATABASE_EVENT_TAG);
        for (int s = 0; s < databaseNodes.getLength(); s++) {
            Element databasesElement = (Element)databaseNodes.item(s);
            addDatabaseEvent(getDatabaseEvent(databasesElement));
            
        }
        
        inFile.close();
      } catch (Exception e) {
        logger.error("CoreProperties: error processing xml config file. " + configFileName 
                    + " " + e);
        e.printStackTrace();
      } 
    }
    /*
     * core elements
     */
    
    public void setName(String name) {
      this.name = name;
    }
        
    public String getName(){
      return this.name;
    }
    
    public void setAuthor(String author) {
      this.author = author;
    }
        
    public String getAuthor(){
      return this.author;
    }
    
    public void setDescription(String description) {
      this.description = description;
    }
        
    public String getDescription(){
      return this.description;
    }
    
    public void setDate(String date) {
      this.date = date;
    }
        
    public String getDate(){
      return this.date;
    }
    
   
    public void setDBServerIp(String DBServerIp){
      this.DBServerIp = DBServerIp;
    }
    public void setDBServerPort(String DBServerPort){
      this.DBServerPort = DBServerPort;
    }
    public void setDBName(String DBName){
      this.DBName = DBName;
    }
    public void setDBUserName(String DBUserName){
      this.DBUserName = DBUserName;
    }
    public void setDBUserPassword(String DBUserPassword){
      this.DBUserPassword = DBUserPassword;
    }
    
    public String getDBServerIp(){
      return this.DBServerIp;
    }
    public String getDBServerPort(){
      return this.DBServerPort;
    }
    public String getDBName(){
      return this.DBName;
    }
    public String getDBUserName(){
      return this.DBUserName;
    }
    public String getDBUserPassword(){
      return this.DBUserPassword;
    }
        
    public void setNumberValue(String numberValue) {
      this.numberValue=Integer.valueOf(numberValue);
    }
    
    public int getNumberValue() {
      return this.numberValue;
    }
    
    public void updateNumberValue(String numberIncrement) {
      if (numberIncrement.length() == 0){
        int increment = Integer.parseInt(numberIncrement);
        this.numberValue += increment;
      } else {
          this.numberValue++;
      }
    }
    
    public void setLogFileName(String logFileName) {
      this.logFileName = logFileName;
    }
        
    public String getLogFileName(){
      return this.logFileName;
    }
    
    public void setLogLevel(String logLevel) {
      this.logLevel = "Level." + logLevel.toUpperCase();
    }
        
    public String getLogLevel(){
      return this.logLevel;
    }
    /*
     *  Populate a Variable element: 
    */
    public void addVariable(Variable variable){
        variables.add(variable);
    }
    
    public List getVariables(){
        return variables;
    }
    
    private static Variable getVariable(Element variableElement, Logger logger){
        try {
            Variable variable = new Variable(logger);
            variable.setName(variableElement.getAttribute("Name"));
            variable.setType(variableElement.getAttribute("Type"));
            variable.setValue(variableElement.getAttribute("Value"));
            variable.setStartPosition(variableElement.getAttribute("StartPosition"));
            variable.setLength(variableElement.getAttribute("Length"));
            variable.setColumn(variableElement.getAttribute("Column"));
            variable.setDelimiter(variableElement.getAttribute("Delimiter"));
            variable.setFormat(variableElement.getAttribute("Format"));
            variable.setInitialValue(variableElement.getAttribute("InitialValue"));
            variable.setIncrement(variableElement.getAttribute("Increment"));
            variable.setLeftOf(variableElement.getAttribute("LeftOf"));
            variable.setRightOf(variableElement.getAttribute("RightOf"));
            variable.setOffset(variableElement.getAttribute("Offset"));
            variable.setOccurrence(variableElement.getAttribute("Occurrence"));
            variable.setFileName(variableElement.getAttribute("Filename"));
            variable.setNumberValue(variableElement.getAttribute("Filename"), 
                                    variableElement.getAttribute("InitialValue"));
            variable.setRandMin(variableElement.getAttribute("RandMin"));
            variable.setRandMax(variableElement.getAttribute("RandMax"));
            variable.setDefaultValue(variableElement.getAttribute("DefaultValue"));
            variable.setConvert(variableElement.getAttribute("Convert"));
            variable.setTrim(variableElement.getAttribute("Trim"));
            variable.setDatabaseEvent(variableElement.getAttribute("DatabaseEvent"));  
            return variable;
        } catch (Exception e) {
            logger.error("CoreProperties: Error setting variables." + e);
            e.printStackTrace();
            return null;
        }
    }
    
    /*
     *  Populate a BaselineMessage element: 
     * 
    */
    public void addBaselineMessage(BaseLineMessage baselineMessage)
    {
        baselineMessages.add(baselineMessage);
    }

    public List getBaselineMessages()
    {
        return baselineMessages;
    }
      
    private static BaseLineMessage getBaselineMessage(Element baselineMsgElement)
    {
        BaseLineMessage baselineMsg = new BaseLineMessage();
        baselineMsg.setName(baselineMsgElement.getAttribute("Name"));
        // Now get the Cdata:
        Text baselineMsgText = (Text)baselineMsgElement.getFirstChild();
        baselineMsg.setCdata(baselineMsgText.getData());
        return baselineMsg;
    }
    
    /*
     * get the receiver event elements
    */
    public void addReceiverEvent(ReceiverEvent receiverEvent) {
        receiverEvents.add(receiverEvent);
    }
    
    public List getReceiverEvents(){
        return receiverEvents;
    }
    
    private static ReceiverEvent getReceiverEvent(Element receiverElement, List baselineMsgs){
        ReceiverEvent receiverEvent = new ReceiverEvent();
        receiverEvent.setName(receiverElement.getAttribute("Name"));
        receiverEvent.setKeyType(receiverElement.getAttribute("KeyType"));
        receiverEvent.setKeyValue(receiverElement.getAttribute("KeyValue"));

        // Get the messages appropriate for this receiverEvent:
        NodeList messageNodes = receiverElement.getElementsByTagName(EVENT_MESSAGE_TAG);
        Element messageElement = null;

        // There can be multiple messages for an receiverEvent:
        for (int s = 0; s < messageNodes.getLength(); s++)
        {
            messageElement = (Element)messageNodes.item(s);
            receiverEvent.addMessage(getMessage(messageElement, baselineMsgs));
        }
        return receiverEvent;
    }
    
    private static EventMessage getMessage(Element messageElement, List baselineMsgs){
        EventMessage message = new EventMessage();
        message.setTemplate(BaseLineMessage.findInList(baselineMsgs, messageElement.getAttribute("BaselineMessage")));
        message.setName(messageElement.getAttribute("BaselineMessage"));
        message.setWaitDistribution(messageElement.getAttribute("WaitDistribution"));
        message.setWaitFrom(messageElement.getAttribute("MinWait"));
        message.setWaitTo(messageElement.getAttribute("MaxWait"));
        message.setDecodeEscape(messageElement.getAttribute("DecodeEscape"));
        return message;
    }
        
    public void addDatabaseEvent(DatabaseEvent databaseEvent) {
        databaseEvents.add(databaseEvent);
    }
    
    public List getDatabaseEvents(){
        return databaseEvents;
    }
    
    private static DatabaseEvent getDatabaseEvent(Element databaseElement){
        DatabaseEvent databaseEvent = new DatabaseEvent();
        databaseEvent.setName(databaseElement.getAttribute("Name"));
        databaseEvent.setDBServerIP(databaseElement.getAttribute("DBServerIP"));
        databaseEvent.setDBServerPort(databaseElement.getAttribute("DBServerPort"));
        databaseEvent.setPHPFile(databaseElement.getAttribute("PHPFile"));
        return databaseEvent;
    }
    
    
    private static DatabaseEventDetail getDatabaseDetails(Element databaseDetailsElement){
        DatabaseEventDetail databaseEventDetail = new DatabaseEventDetail();
        databaseEventDetail.setName(databaseDetailsElement.getAttribute("Name"));
        databaseEventDetail.setDBEventName(databaseDetailsElement.getAttribute("DatabaseEvent"));
        databaseEventDetail.setDBField(databaseDetailsElement.getAttribute("DBField"));
        return databaseEventDetail;
    }
        
}
