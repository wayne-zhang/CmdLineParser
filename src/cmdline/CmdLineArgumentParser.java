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
    
    public static CmdLineArgumentParser parse(Class caller, String[] argDef, String[] args){
        CmdLineArgumentParser instance = null;
        try{
            instance = new CmdLineArgumentParser(argDef);
            instance.parse(args);
        }catch(IllegalArgumentException e){
            instance.help(caller);
            
            System.exit(-1);
        }
        
        return instance;
    }
    
    public CmdLineArgumentParser(){
        
    }
    
    public CmdLineArgumentParser(String... argumentDefination){
        for(String define: argumentDefination){
            defineArgument(define);
        }
    }
    
    public CmdLineArgumentParser(String[] argumentDefination, String[] args){
        this(argumentDefination);
        
        parse(args);
    }
    
    // define an argument 
    public CmdLineArgument defineArgument(String argumentDefination){
        CmdLineArgument arg = CmdLineArgument.valueOf(argumentDefination);
        
        defineArgument(arg);
        
        return arg;
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
            }else{
                throw new IllegalArgumentException("Argument " + arg + " can't be recognised");
            }
        }
        
        for(CmdLineArgument arg : shortNameMap.values()){
            if(arg.isSupplied()){
                arg.validate();
            } else if(arg.isMandatory()) { // always validate mandatory arguments
                arg.validate();
            }
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
        
        CmdLineArgument arg = null;
        if(name.startsWith("--")){                
            arg = longNameMap.get(name);
        }else{
            arg = shortNameMap.get(name);
        }
        
        if(arg == null){
            throw new RuntimeException("Argument '" + name + "' not defined");
        }
        
        return arg.getValue();
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
    
    public void help(){
        String caller = Thread.currentThread().getStackTrace()[2].getClassName();
        
        help(caller, System.out);
    }
    public void help(Class runner){
        help(runner.getName(), System.out);
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