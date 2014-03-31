package com.httpStub.core;

/**
class: RandomNumberGenerator
Purpose: returns a random number for sleep time
Notes: only support UNIFORM distribution
Author: Tim Lane
Date: 24/03/2014
**/

import java.util.Random;
import java.util.Date;

public class RandomNumberGenerator {
  
    public static final String UNIFORM_RANDOM = "UNIFORM";
    public static final double waitMultplier = 1000.0;
  
    private Random _generator;
    private String _distribution;
    private double _randMin;
    private double _randMax; 
  
    public RandomNumberGenerator(String distribution, long randMin, long randMax){
        _generator = new Random();
        _distribution = distribution;
        _randMin = randMin;
        _randMax = randMax;
    }
    
    public RandomNumberGenerator(String distribution, double randMin, double randMax){
        _generator = new Random();
        _distribution = distribution;
        _randMin = randMin;
        _randMax = randMax;
    }
    
    public double uniformRandom(double randMin, double randMax){
     return _generator.nextDouble() * (randMax - randMin) + randMin;
    }
    
    public double uniformLong(double randMin, double randMax){
     return _generator.nextDouble() * (randMax - randMin) + randMin;
    }
    
        
}
