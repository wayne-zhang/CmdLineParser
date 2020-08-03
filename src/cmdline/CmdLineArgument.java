package cmdline;
        
import java.util.HashSet;
import java.util.Set;

/**
 * Command line argument, which composed of
 *     short name, e.g -a
 *     long name, e.g --action
 *     a flag that indicates if the argument has or has not value,
 *     a list of enumeration values, e.g [create|update|delete]
 *     value, e.g the value of argument
 *     a flag that indicates if the argument is mandatory
 * 
 * Initialization pattern as the following:
 * 
 *    short name,long name,has value[,value enumeration][,mandatory]
 * 
 * for example
 * 
 *    -a,--action,true,create|update|delete,true
 *    -t,--type,true,CLOB|BLOB
 *    -v,--verbose,false
 *    -i,--input,true,,true
 * 
 * 
 * @author Wayne Zhang
 */

public class CmdLineArgument implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String shortName;
    private final String longName;
    private final boolean hasValue;  // if the argument has value?
    private final Set<String> enumValues;
    private String value;    
    private boolean isMandatory;     // Ehancmement - mandatory argument
    
    public CmdLineArgument(String shortName, String longName, boolean hasValue,
            Set<String> enumValues, boolean isMandatory){
        // check short/long name format
        if(!shortName.startsWith("-") || shortName.startsWith("--")){
            throw new IllegalArgumentException("Argument short name format error: " + shortName);
        }
        
        if(!longName.startsWith("--")){
            throw new IllegalArgumentException("Argument long name format error: " + longName);
        }        
        
        this.shortName  = shortName;
        this.longName   = longName;
        this.hasValue   = hasValue;
        this.enumValues = enumValues;
        this.isMandatory= isMandatory;
    }
    
    public static CmdLineArgumentBuilder builder(){
        return new CmdLineArgumentBuilder();
    }
    
    public static CmdLineArgument valueOf(String define){
        String[] params = define.split(",", -1);
        
        if(params.length > 5 || params.length < 3){
            throw new IllegalArgumentException(define);
        }
        
        Set<String> enumValues = null;
        if(params.length > 3 && !params[3].isEmpty()){
            enumValues = new HashSet<>();
            
            String[] values = params[3].split("\\|");
            for(String v : values){
                enumValues.add(v.trim());
            }
        }
        
        // mandatory flag supplied
        try{
            boolean isMandatory = false;
            if(params.length == 5){
                isMandatory = toBoolean(params[4]);
            }

            return new CmdLineArgument(params[0].trim(), params[1].trim(), 
                    toBoolean(params[2]), enumValues, isMandatory
            );
        }catch(RuntimeException e){
            throw new IllegalArgumentException(define);
        }
    }
    
    public String getShortName(){
        return shortName;        
    }
    
    public String getLongName(){
        return longName;
    }
    
    public String getName(){
        return shortName + "|" + longName;
    }
    
    public boolean hasValue(){
        return hasValue;
    }
    
    public Set<String> getEnumValues(){
        return enumValues;
    }
    
    public String getEnumValuesAsString(){
        if(!isEnumValue()){
            return "";
        }
        
        StringBuilder buf = new StringBuilder();
        
        for(String v : enumValues){
            if(buf.length() > 0){
                buf.append('|');
            }
            buf.append(v);
        }
        
        return buf.toString();
    }
    
    public boolean isEnumValue(){
        return enumValues != null && !enumValues.isEmpty();
    }
    
    public String getValue(){
        return value;
    }
    
    public void setValue(String value){
        this.value = value;
    }
    
    public boolean isSupplied(){
        return value != null;
    }
    
    public boolean isMandatory(){
        return isMandatory;
    }
    
    public void setMandatory(boolean isMandatory){
        this.isMandatory = isMandatory;
    }
    
    public void validate(){
        if(!hasValue && value != null && !value.isEmpty()){
            throw new IllegalArgumentException(getName() 
                    + " is a no value argument but set a value: " + value);
        }
        
        if(isMandatory && !isSupplied()){
            throw new IllegalArgumentException(getName() 
                    + " is a manditory argument but has not supplied");            
        }
        
        if(isEnumValue() && !enumValues.contains(value)){
            throw new IllegalArgumentException(getName() + " value (" + value + ") "
                    + "is not permit, it can be: " + getEnumValuesAsString());
        }
    }
    
    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder();
        buf.append(shortName)
           .append(',').append(longName)
           .append(',').append(hasValue)
           .append(',').append(getEnumValuesAsString());
        
        return buf.toString();
    }
    
    public String getHelpInfo(){
        StringBuilder buf = new StringBuilder();
        buf.append(shortName)
           .append('|')
           .append(longName)
           .append(" ");
        
        if(hasValue()){
            if(isEnumValue()){
                buf.append('[')
                   .append(getEnumValuesAsString())
                   .append(']');
            }else{
                buf.append(userFriendlyHelpValuePlaceHolder());
            }
        }
        
        if(isMandatory()){
            buf.append('*');
        }
        
        return buf.toString();
    }
    
    /**
     * Build a user friendly help value place holder, based on long name.
     * If it ends with File, then add a name suffix.
     * 
     * @return 
     */
    private String userFriendlyHelpValuePlaceHolder(){
        StringBuilder buf = new StringBuilder();
        
        buf.append("{");
        if(longName.startsWith("--")){
            buf.append(toWords(longName.substring(2)));
        }
        
        if(longName.endsWith("File")){
            buf.append(" name");
        }
        buf.append("}");
        
        
        return buf.toString();
    }
    
    /**
     * Break a camel case string to words, for example,
     *   'fileName' to 'file name'
     * 
     * @param s
     * @return 
     */
    private String toWords(String s){
        StringBuilder buf = new StringBuilder();
        
        for(char c : s.toCharArray()){
            if(Character.isUpperCase(c)){
                buf.append(' ').append(Character.toLowerCase(c));
            }else{
                buf.append(c);
            }
        }
        
        return buf.toString();
    }
    
    /**
     * Recognize 'boolean' values Y/N, YES/NO, TRUE/FALSE, 1/0
     * 
     * @param value
     * @return 
     */
    private static String[] BOOL_YES = {"Y", "YES", "TRUE", "1", "T"};
    private static String[] BOOL_NO  = {"N", "NO", "FALSE", "0", "F"};
    private static Boolean toBoolean(String value){
        String sv = value.trim().toUpperCase();
        
        for(String yes : BOOL_YES){
            if(yes.equals(sv)){
                return true;
            }
        }
        
        for(String no : BOOL_NO){
            if(no.equals(sv)){
                return false;
            }
        }
        
        throw new RuntimeException("Illegal boolean value: " + value);
    }
    
    public static class CmdLineArgumentBuilder {
        private String shortName;
        private String longName;
        private boolean hasValue;  // if the argument has value?
        private Set<String> enumValues;  
        private boolean isMandatory;     // Ehancmement - mandatory argument    
        
        private boolean built;
        
        public CmdLineArgumentBuilder shortName(String shortName){
            checkBuilt();
            
            if(!shortName.startsWith("-") || shortName.startsWith("--")){
                throw new IllegalArgumentException("Argument short name format error: " + shortName);
            }   
            
            this.shortName = shortName;
            
            return this;
        }
        
        public CmdLineArgumentBuilder longName(String longName){
            checkBuilt();
            
            if(!longName.startsWith("--")){
                throw new IllegalArgumentException("Argument long name format error: " + longName);
            } 

            this.longName = longName;
            return this;
        }
        
        public CmdLineArgumentBuilder hasValue(boolean hasValue){
            checkBuilt();
            
            this.hasValue = hasValue;
            
            return this;
        }
        
        public CmdLineArgumentBuilder isMandatory(boolean isMandatory){
            checkBuilt();
            
            this.isMandatory = isMandatory;
            
            return this;
        }
        
        public CmdLineArgumentBuilder enumValues(Set<String> enumValues){
            checkBuilt();
            
            this.enumValues = enumValues;
            
            return this;
        }
        
        public CmdLineArgumentBuilder enumValues(String... values){
            checkBuilt();
            
            if(enumValues == null){
                enumValues = new HashSet<>();
            }
            
            for(String enumValue : values){
                if(!enumValues.contains(enumValue)){                         
                    enumValues.add(enumValue);
                }
            }
            
            return this;
        }        
        
        public CmdLineArgumentBuilder enumValue(String enumValue){
            checkBuilt();
            
            if(enumValues == null){
                enumValues = new HashSet<>();
            }
            
            if(!enumValues.contains(enumValue)){                         
                enumValues.add(enumValue);
            }
            
            return this;
        }  
        
        private void checkBuilt(){
            if(built) throw new RuntimeException("Can't supply new info after build() called");
        }
        
        public CmdLineArgument build(){
            built = true;
            
            return new CmdLineArgument(
                    shortName,
                    longName,
                    hasValue,
                    enumValues,
                    isMandatory
            );
        }
    }    
}