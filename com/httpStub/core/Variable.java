package com.httpStub.core;

/**
 class: variable
 Purpose: holds the details of the variables
 Notes:
 Author: Tim Lane
 Date: 24/03/2014
 
 **/

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import java.text.SimpleDateFormat;


public class Variable {
  
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
  public static final String REGEX_TYPE = "Regex";
  
  private String regexString;
  
  private String  Name;
  private String  Type;
  private String  Convert;
  private boolean isTrim;
  private boolean isXAMPP;
  private boolean isSQLKeyValue;
  private boolean isSQLKeyData;
  private boolean SQLKeyData;
  private String MysqlKey;
  private String MysqlData;
  private String MysqlTable;
  private String  Format;
  private int  Offset;
  private String  initialValue;
  private double CurrentValue;
  private double  Increment;
  private String stringIncrement;
  private String  Value;
  private String  RightOf;
  private String  LeftOf;
  private String  Occurrence;
  public int delimOccurence;
  private int  StartPosition;
  private int  Length;
  private double RandMin;
  private double RandMax;
  private String FileName;
  private String  defaultValue;
  private RandomNumberGenerator randomGenerator;
  private int  Column;
  private String Delimiter;
  private String numberValue;
  private String databaseEvent;

  SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");
  String formattedDate;
  Date date;
      
  private Logger logger;
  
  public Variable(Logger logger){
    this.logger = logger;
  }
  
  public String getName() {
    return Name;
  }
  
  public void setName(String name) {
    Name = name;
  }
  
  public String getType() {
    return Type;
  }
  
  public void setType(String type) {
    Type = type;
  }
  
  public void setNumberValue(String fileName, String initialValue)
    throws IOException
  {
    String numberValue = null;
    if (fileName.length() == 0) {
      //System.out.println("variable: no filename set. Number variables require a FileName");
    } else {
      /*
       * if the initial value is already set then update the file with this value
       */
      if (initialValue.length() > 0 ) {
        this.numberValue = initialValue;
        try {
          BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
          out.write(initialValue);
          out.close();
        } catch (FileNotFoundException e) { 
          date = new Date();
          formattedDate = sdf.format(date);
          logger.error("variable: " + formattedDate + " data error setting number value: " + e);
        }
      } else {
        /*
         * else read the value from the file
         */
        try {
          BufferedReader in = new BufferedReader(new FileReader(fileName));
          this.numberValue = in.readLine();
          in.close();
        } catch (FileNotFoundException e) { 
          date = new Date();
          formattedDate = sdf.format(date);
          logger.error("variable: " + formattedDate + " data error getting number value: " + e);
        }
        
      }
    }
  }
  
  public String getNumberValue() {
    return this.numberValue;
  }
  
  public void updateNumberValue(String fileName,String numberValue, String increment)
  {
    double tmpIncrement = Integer.parseInt(increment);
    double tmpNumberValue = Double.parseDouble(numberValue);
    tmpNumberValue = tmpNumberValue + tmpIncrement ;
    this.numberValue = Double.toString(tmpNumberValue);
    
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
      out.write(this.numberValue);
      out.close();
    } catch (Exception e) { 
      date = new Date();
      formattedDate = sdf.format(date);
      logger.error("variable: " + formattedDate + " data error updating number variable: " + fileName);
    }
    
  }
  
  public String getValue() {
    return Value;
  }
  
  public void setValue(String value) {
    Value = value;
  }
  
  
  public void setStartPosition(String startPosition) {
    if (startPosition.length() == 0)
      setStartPosition(0);
    else
      setStartPosition(Integer.parseInt(startPosition));
  }
  
  public void setStartPosition(int startPosition) {
    StartPosition = startPosition;
  }
  
  public int getStartPosition() {
    return StartPosition;
  }
  
  public int getLength() {
    return Length;
  }
  
  public void setLength(int length) {
    Length = length;
  }
  
  public void setLength(String length) {
    if (length.length() == 0)
      setLength(0);
    else
      setLength(Integer.parseInt(length));
  }
  
  public int getColumn() {
    return Column;
  }
  
  public void setColumn(int column) {
    Column = column;
  }
  
  public void setColumn(String column) {
    if (column.length() == 0)
      setColumn(0);
    else
      setColumn(Integer.parseInt(column));
  }
  
  public String getDelimiter() {
    return Delimiter;
  }
  
  public void setDelimiter(String delimiter) {
    Delimiter = delimiter;
  }
  
  public String getFormat() {
    return Format;
  }
  
  public void setFormat(String format) {
    Format = format;
  }
  
  public String getInitialValue() {
    return this.initialValue;
  }
  
  public void setInitialValue(String initialValue) {
    this.initialValue = initialValue;
  }
  
  public String getIncrement() {
    return this.stringIncrement;
  }
  
  public void setIncrement(String stringIncrement) {
    if (stringIncrement.length() == 0)
      this.stringIncrement = "1";
    else
      this.stringIncrement = stringIncrement;
  }
  
  public String getFileName() {
    return this.FileName;
  }
  
  public void setFileName(String fileName) 
    throws IOException
  {
    this.FileName = fileName;
    
    if (fileName.length() != 0) {
      try {
        BufferedReader in = new BufferedReader(new FileReader(this.FileName));
      } catch (FileNotFoundException e) { 
        /*
         * file doesnt exist so create it.
         */
        BufferedWriter out = new BufferedWriter(new FileWriter(this.FileName));
        out.write(getInitialValue());
        out.close();
      }
    }
  }
  
  public String getRightOf() {
    return RightOf;
  }
  
  public void setRightOf(String rightOf) {
    RightOf = rightOf;
  }
  
  public String getLeftOf() {
    return LeftOf;
  }
  
  public void setLeftOf(String leftOf) {
    LeftOf = leftOf;
  }
  
  public String getOccurrence() {
    return Occurrence;
  }
  
  public void setOccurrence(String occurrence) {
    Occurrence = occurrence;
  }
  
  public int getOffset() {
    return Offset;
  }
  
  public void setOffSet(int offset) {
    this.Offset = offset;
  }
  
  public void setOffset(String offset) {
    if (offset.length() == 0)
      setOffSet(0);
    else
      setOffSet(Integer.parseInt(offset));
  }
  
  public double getRandMin() {
    return RandMin;
  }
  
  public void setRandMin(double randMin) {
    RandMin = randMin;
  }
  
  public void setRandMin(String randMin) {
    if (randMin.length() == 0)
      setRandMin(0);
    else
      setRandMin(Double.parseDouble(randMin));
  }
  
  public double getRandMax() {
    return RandMax;
  }
  
  public void setRandMax(double randMax) {
    RandMax = randMax;
  }
  
  public void setRandMax(String randMax) {
    if (randMax.length() == 0)
      setRandMax(0);
    else
      setRandMax(Double.parseDouble(randMax));
  }
  
  public String getDefaultValue() {
    return this.defaultValue;
  }
  
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }
  
  public String getConvert() {
    return Convert;
  }
  
  public void setConvert(String convert) {
    Convert = convert;
  }
  
  public boolean isTrim() {
    return isTrim;
  }
  
  public void setTrim(String trim) {
    if (trim.equals("TRUE"))
      this.isTrim = true;
    else
      this.isTrim = false;
  }
  
  public void setDatabaseEvent(String databaseEvent) {
    this.databaseEvent = databaseEvent;
  }

  public String getDatabaseEvent() {
    return this.databaseEvent;
  }
}