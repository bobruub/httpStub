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
    private double  InitialValue;
    private double CurrentValue;
    private double  Increment;
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
    // private VariableFile variableFile;
    private RandomNumberGenerator randomGenerator;
    private int  Column;
    private String Delimiter;
    
    // Standard Getter and Setter Functions:
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
    
    public void setValue(String value)
    {
        Value = value;
    }
    
    public String getValue() {
        return Value;
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
    
    public double getInitialValue() {
        return InitialValue;
    }

    public void setInitialValue(double initialValue) {
        InitialValue = initialValue;
    }
    
    public void setInitialValue(String initialValue) {
        if (initialValue.length() == 0)
            setInitialValue(0);
        else
            setInitialValue(Double.valueOf(initialValue));
    }
    
        public double getIncrement() {
        return Increment;
    }

    public void setIncrement(double increment) {
        Increment = increment;
    }

    public void setIncrement(String increment) {
        if (increment.length() == 0)
            setIncrement(0);
        else
            setIncrement(Double.valueOf(increment));
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
        return defaultValue;
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
   // 1.7.5. option to trim cr/lf
    public boolean isTrim() {
        return isTrim;
    }
    
    public void setTrim(String trim) {
        if (trim.equals("TRUE"))
            this.isTrim = true;
        else
            this.isTrim = false;
    }

  
}
