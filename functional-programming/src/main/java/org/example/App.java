package org.example;

import java.util.Comparator;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        // Lambdas and method references:
        // Lambda
        Comparator<String> byLength = (a, b) -> Integer.compare(a.length(), b.length());

// Method reference
        List<String> names = List.of("Ana","Luis","Sofía","Pablo");
        names.sort(Comparator.comparingInt(String::length));
        System.out.println( "Hello World!" );
    }
}
