/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmdline;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Tool to run a unit test based on Java reflection.
 * 
 * Create it to avoid unit test library dependency.
 * 
 * @author Wayne Zhang
 */
public class TestRunner {
    public static boolean isVerbose = true;
    
    private static final class TestResult {
        private boolean isPassed;
        private String exceptionStack;
        
        TestResult(boolean isPassed, String exceptionStack){
            this.isPassed = isPassed;
            this.exceptionStack = exceptionStack;
        }
    }
    
    public static void fire(Class<?> testClass){
        final Map<String, TestResult> testResults = new HashMap<>();
        
        // run test cases
        try{
            Method[] methods = testClass.getDeclaredMethods();
            Object test = testClass.newInstance();
            for(Method m : methods){
                String method = m.getName();
                
                // ignoe non test methods
                if(!method.startsWith("test")){
                    continue;
                }
                
                boolean result = true;
                String exceptionStack = null;
                try{
                    // make non-public method callable
                    m.setAccessible(true);
                    
                    m.invoke(test);
                }catch(Exception e){
                    if(isVerbose){
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        try(PrintStream ps = new PrintStream(bos)){
                            e.printStackTrace(ps);
                        }
                        exceptionStack = bos.toString();
                    }
                    
                    result = false;
                }

                testResults.put(method, new TestResult(result, exceptionStack));
            }
        }catch(Exception e){
            e.printStackTrace();
            System.exit(0);
        }
        
        // Print test result
        boolean testPassed = true;
        for(TestResult result : testResults.values()){
            if(result == null || !result.isPassed){
                testPassed = false;
                break;
            }
        }
        
        StringBuilder buf = new StringBuilder();
        buf.append("\n================= ")
           .append(testClass.getName())
           .append(testPassed ? " Passed" : " Failed")
           .append(" =================\n");               
        
        for(String method : testResults.keySet()){
            TestResult result = testResults.get(method);
            boolean isPassed = result != null && result.isPassed;
            buf.append(method)
               .append(": ")
               .append(isPassed ? "V" : "X")
               .append("\n");
            
            if(isVerbose && !isPassed){
                buf.append(result.exceptionStack);
            }
        }
        
        System.out.flush();
        System.out.println(buf.toString());
    }    
}
