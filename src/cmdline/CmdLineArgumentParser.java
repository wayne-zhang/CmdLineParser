package cmdline;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encapsulate command line paring &amp; validation logic
 * 
 * @author Wayne Zhang
 */

public final class CmdLineArgumentParser {
    // Map indexed by argument short name
    // Note: use linked map to keep argument define order
    private final Map<String, CmdLineArgument> shortNameMap = new LinkedHashMap<>();
    // Map indexed by argument long name
    private final Map<String, CmdLineArgument> longNameMap = new LinkedHashMap<>();
    
    // Has argument parsed? that is parse(_) method called?
    private boolean hasParsed = false;
    
    /**
     * Define &amp; parse arguments at one call.
     * 
     * Deprecated as it doesn't support command line argument rules.
     * 
     * @param argsDef argument definitions
     * @param args command line arguments
     * @return command line argument parser built
     * @deprecated
     */
    @Deprecated
    public static CmdLineArgumentParser parse(String[] argsDef, String[] args){
        return new CmdLineArgumentParser(argsDef, args);
    }
    
    public CmdLineArgumentParser(){
        // support -h/--help?
        defineArgument("-h,--help,false");
    }
    
    public CmdLineArgumentParser(String... argumentDefinitions){
        this();
        
        defineArguments(argumentDefinitions);
    }
    
    /**
     * Deprecated as it doesn't support command line argument rules.
     * 
     * There is no chance to define argument rules.
     * 
     * @param argumentDefinition command line argument definitions
     * @param args command line arguments
     * @deprecated
     */
    @Deprecated
    public CmdLineArgumentParser(String[] argumentDefinition, String[] args){
        this(argumentDefinition);
        
        parse(args);
    }
    
    // define an argument 
    public CmdLineArgument defineArgument(String argumentDefination){
        CmdLineArgument arg = CmdLineArgument.valueOf(argumentDefination);
        
        defineArgument(arg);
        
        return arg;
    }
    
    public void defineArguments(String... argumentDefination){
        for(String define: argumentDefination){
            defineArgument(define);
        }        
    }
    
    // define an argument, using a builder for example
    public void defineArgument(CmdLineArgument arg){
        // check if argument has been defined already!
        if(shortNameMap.get(arg.getShortName()) != null ||
           longNameMap.get(arg.getLongName()) != null )
        {
            throw new RuntimeException("Argument '" + 
                    arg.getShortName() + "," +
                    arg.getLongName() + "' has been defined already" 
            );
        }
        
        shortNameMap.put(arg.getShortName(), arg);
        longNameMap.put(arg.getLongName(), arg);
    }
    
    public void addArgumentRules(String... rules){
        for(String rule : rules){
            CmdLineArgumentRule argRule = new CmdLineArgumentRule(rule);
            CmdLineArgument arg = getArgument(argRule.getArg1Name());
            if(arg == null){
                throw new RuntimeException(
                        "CmdLineArgument rule definatin error, argument not found: " +
                         rule
                );
            }
            arg.addRule(argRule);
        }
    }
    
    public void parse(String... args) {    
        hasParsed = true;
        
        for(int i = 0; i < args.length; i ++){
            String arg = args[i];
            if(arg.startsWith("-")){                
                CmdLineArgument argDef = null;
                if(arg.startsWith("--")){
                    argDef = longNameMap.get(arg);
                } else {
                    argDef = shortNameMap.get(arg);
                }
                
                if(argDef == null){
                    throw new IllegalArgumentException("Argument " + arg + " can't be recognised");
                }
                
                if(argDef.hasValue()){                   
                    // has value supplied?
                    if(i + 1 >= args.length){
                        throw new IllegalArgumentException("Argument value not supplied for: " + arg);
                    }
                    
                    String argVal = args[i + 1];                    
                    if(argVal.startsWith("-")){
                        throw new IllegalArgumentException("Wrong argument value '" + argVal + "' for: " + arg);
                    }
                    
                    argDef.setValue(argVal);
                    i++;
                }else{
                    argDef.setValue(""); // set empty value to indicate argument exist!
                }
            }else if(!arg.isEmpty()){
                throw new IllegalArgumentException("Argument " + arg + " can't be recognised");
            }
        }
        
        // is help? check help before validation
        if(isArgumentSupplied("-h")){
            help();
            
            System.exit(0);
        }
        
        for(CmdLineArgument arg : shortNameMap.values()){
            arg.validate(this);
        }
    }
    
    /**
     * Set cmd line argument values to the app by reflection.
     * 
     * @param app application which arguments applied to
     */
    public void setArgumentsTo(Object app){
        for(CmdLineArgument arg : shortNameMap.values()){
            arg.applyTo(app);
        }
    }
    
    /**
     * Clear the values parsed last time and ready next parse.
     * It is not useful in real world but unit tests.
     */
    public void reset(){
        for(CmdLineArgument arg : shortNameMap.values()){
            arg.setValue(null);
        }
    }
    
    /**
     * Get argument value with defaults if not defined
     * 
     * @param name argument name
     * @param defaultValue default value if not supplied
     * @return argument value if supplied or default value
     */
    public String getArgumentValue(String name, String defaultValue){
        String value = getArgumentValue(name);
        
        return value == null ? defaultValue : value;
    }	
    
    /**
     * Get the value of argument supplied in the command line
     * 
     * @param name argument short name or long name, e.g -a  or --argument
     * @return argument value. An empty string returned if it is a no value argument
     */
    public String getArgumentValue(String name){
        if(!hasParsed){
            throw new RuntimeException("Command line arguments hasn't been parsed.");
        }
        
        CmdLineArgument arg = getArgument(name);
        
        if(arg == null){
            throw new RuntimeException("Argument '" + name + "' not defined");
        }
        
        return arg.getValue();
    }
    
    /**
     * Get argument by name
     * @param argumentName argument name, short or long
     * @return CmdLineArgument define or null if not defined
     */
    CmdLineArgument getArgument(String argumentName){
        if(argumentName.startsWith("--")){                
            return longNameMap.get(argumentName);
        }else{
            return shortNameMap.get(argumentName);
        }        
    }
	
    public boolean isArgumentSupplied(String name){
        return getArgumentValue(name) != null;
    }
	
    /**
     * Get the argument format to build help message when argument is wrong
     * 
     * @return help info line built
     */
    public String getHelpInfo(){
        StringBuilder buf = new StringBuilder();
        
        for(CmdLineArgument arg : shortNameMap.values()){
            buf.append(arg.getHelpInfo()).append(" ");
        }
        
        return buf.toString();
    }
    
    /**
     * Get the class name that owns main() method
     * 
     * @return class name that owns main method or "unknown"
     *         if main method is not on call stack
     */
    private static String getMainClass(){
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for(StackTraceElement se : stack){
            if(se.getMethodName().equals("main")){
                return se.getClassName();
            }
        }
        
        return "Unknown";
    }
    
    public void help(){
        help(getMainClass());
    }
    
    public void help(String runner){
        help(runner, System.out);
    }
    
    public void help(String runner, java.io.PrintStream target){
        StringBuilder help = new StringBuilder();
        help.append("Usage: java ")
            .append(runner)
            .append(" ")
            .append(getHelpInfo());
      
        target.println(help.toString());
    }
}