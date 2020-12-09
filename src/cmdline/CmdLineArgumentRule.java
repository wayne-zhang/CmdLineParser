/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmdline;

/**
 * Define relationship between 2 command line arguments
 * 
 * @author Wayne Zhang
 */
class CmdLineArgumentRule {
    private static enum Rule {
        dependsOn{
            boolean validate(Object... args){
                if(args.length < 2){
                    return false;
                }
                
                if(args[0] != null){
                    if(args.length > 2){    // with critiea
                        CmdLineArgument arg2 = (CmdLineArgument)args[1];
                        String criteria = (String)args[2];
                        if(arg2 == null || 
                           criteria == null || 
                           arg2.getValue() == null)
                        {
                            return false;
                        }

                        // assert criteria equals
                        if(criteria.startsWith("=")){
                            return criteria.substring(1).equals(arg2.getValue());
                        } else if(criteria.startsWith(">")){
                            try{
                                Double arg2Value = Double.valueOf(arg2.getValue());
                                Double criteriaValue = Double.valueOf(criteria.substring(1));

                                return arg2Value > criteriaValue;
                            }catch(NumberFormatException e){
                                return false;
                            }
                        } else if(criteria.startsWith("<")){
                            try{
                                Double arg2Value = Double.valueOf(arg2.getValue());
                                Double criteriaValue = Double.valueOf(criteria.substring(1));

                                return arg2Value < criteriaValue;
                            }catch(NumberFormatException e){
                                return false;
                            }                        
                        }
                    } else {    // without criteria
                        return args[1] != null;
                    }
                }
                
                return true;
            }
        },
        conflictsWith{
            boolean validate(Object... args){
                if(args.length != 2){
                    return false;
                }
                
                // online one can be NOT null
                return args[0] == null || args[1] == null;
            }
        },
        isInteger{
            boolean isValidateOnValue(){
                return true;
            }
            
            boolean validate(Object... args){
                if(args.length == 0){
                    return false;
                }
                
                try{
                    Integer.valueOf(args[0].toString());
                    
                    return true;
                }catch(NumberFormatException e){
                    return false;
                }               
            }
        },
        isNumber{
            boolean isValidateOnValue(){
                return true;
            }
            
            boolean validate(Object... args){
                if(args.length == 0){
                    return false;
                }
                
                try{
                    Double.valueOf(args[0].toString());
                    
                    return true;
                }catch(NumberFormatException e){
                    return false;
                }               
            }
        },
        lessThan{
            boolean isValidateOnValue(){
                return true;
            }
            
            boolean validate(Object... args){
                if(args.length < 2){
                    return false;
                }
                
                try{
                    Double v1 = Double.valueOf(args[0].toString());
                    Double v2 = args[1] == null ? null : Double.valueOf(args[1].toString());
                                        
                    return v2 == null || v1 < v2;
                }catch(NumberFormatException e){
                    return false;
                }               
            }
        },
        greatThan{
            boolean isValidateOnValue(){
                return true;
            }
            
            boolean validate(Object... args){
                if(args.length < 2){
                    return false;
                }
                
                try{
                    Double v1 = Double.valueOf(args[0].toString());
                    Double v2 = args[1] == null ? null : Double.valueOf(args[1].toString());
                                        
                    return v2 == null || v1 > v2;
                }catch(NumberFormatException e){
                    return false;
                }               
            }
        },
        isMandatory{
            boolean validate(Object... args){
                return args[0] != null;
            }
        },
        isIn{
            boolean isValidateOnValue(){
                return true;
            }
            
            boolean validate(Object... args){
                if(args.length < 2){
                    return false;
                }
                
                String v1 = (String)args[0];
                
                // don't validate if argument is not supplied
                if(v1 == null){
                    return true;
                }
                
                String[] values = toArray((String)args[1]);
                for(String value : values){
                    if(v1.equals(value)){
                        return true;
                    }
                }
                
                return false;
            }
            
            /**
             * Convert a enumeration to a string array. Supported format:
             *      (v1,v2, ...)
             *      [v1,v2,...]
             *      {v1,v2,...}
             *      'v1,v2, ...'
             *      "v1,v2,..."
             *      v1,v2,v3
             * NOTE: NO spaces supported as space is delimiter of rule elements
             * 
             * @param value value enumeration line
             * @return value array list parsed
             */
            private String[] toArray(String value){
                if(value == null || value.isEmpty()){
                    return new String[0];
                }

                value = value.trim();

                value = deQuote(value, '(', ')');
                value = deQuote(value, '[', ']');
                value = deQuote(value, '{', '}');
                value = deQuote(value, '\'', '\'');
                value = deQuote(value, '\"', '\"');

                return value.split("[,\\|]");
            }

            /**
             * Check and de-quote
             */
            private String deQuote(String value, char quoteStartChar, char quoteEndChar)
            {
                if(value.charAt(0) == quoteStartChar){
                    if(value.charAt(value.length() - 1) != quoteEndChar){
                        throw new RuntimeException("isIn format error: " + value);
                    }

                    return value.substring(1, value.length() - 1);
                }

                return value;
            }            
        };
        
        // is validation on argument value ?
        boolean isValidateOnValue(){
            return false;
        }
        
        abstract boolean validate(Object... args);
        
        public String toString(){
            // append leading and trailing space to make build 
            // error message easy
            return " " + CmdLineArgument.toWords(super.toString()) + " ";
        }
    };
    
    // only one argument rule?
    private boolean uniOp = false;
    // argument 1 name, short or long
    private String arg1Name;
    // argument 1 object, it always exist!
    private CmdLineArgument arg1;
    // arugment 2 name, short or long; it can be constant value too
    private String arg2;
    // is argument 2 an argument (nor constant value)?
    private boolean isArg2CmdLineArgument = false;
    // Rule
    private Rule rule;
    
    public CmdLineArgumentRule(String ruleDef){
        String[] ruleElements = ruleDef.split(" ");
        // it can be 2 or 3 elements, depends on if it is uniOp
        if(ruleElements.length != 2 && ruleElements.length != 3){
            throw new IllegalArgumentException("Rule definination error: " + ruleDef);
        }
        uniOp = ruleElements.length == 2;
        
        // arg 1
        arg1Name = ruleElements[0].trim();
        
        // ruleDef type
        this.rule = Rule.valueOf(ruleElements[1].trim());
        
        // arg 2 (optional)
        if(!uniOp){
            arg2 = ruleElements[2].trim();
            
            // --xxx or
            // -[a-zA-Z]
            // negtive value such as -1 and -3.14 is not argument
            isArg2CmdLineArgument = arg2.startsWith("-") && 
                   arg2.length() > 1 && 
                   !Character.isDigit(arg2.charAt(1));          
        }
        
        if(this.rule == null){
            throw new IllegalArgumentException("Rule definination error: " + ruleDef);
        }
    }
    
    public String getArg1Name(){
        return arg1Name;
    }
    
    public void setArg1(CmdLineArgument arg1){
        this.arg1 = arg1;
    }
    
    public CmdLineArgument getArg1(){
        return arg1;
    }
    
    public String getArg2(){
        // Argument 2 may be include value criteria, for example
        //   -argument1 depdensOn -argument2=ACTION
        // resolve the argument name in this case
        int pos = indexOf(arg2, '=', '<', '>');
        if(pos > 0){
            return arg2.substring(0, pos);
        } else {
            return arg2;
        }
    }
    
    private static int indexOf(String s, char... dc){
        for(char c : dc){
            int pos = s.indexOf(c);
            if(pos >= 0){
                return pos;
            }
        }
        
        return -1;
    }
   
    /**
     * Argument 2 may be include value criteria, for example
     * 
     *      argument1 depdensOn -argument2=ACTION
     * 
     * resolve the argument criteria if any
     * 
     * @return argument 2 criteria or null of not exist
     */
    public String getArg2Criteria(){
        int pos = indexOf(arg2, '=', '>', '<');
        if(pos > 0){
            return arg2.substring(pos);
        }  else {
            return null;
        }        
    }
    
    public boolean isArg2WithCriteria(){
        return arg2 != null && 
               indexOf(arg2, '=', '<', '>') > 0;
    }
    
    public void validate(CmdLineArgumentParser parser){
        Object arg1 = null, arg2 = null;
        if(rule.isValidateOnValue()){
            arg1 = parser.getArgumentValue(getArg1Name());
            
            // when validate argument value, validte only when argument 1 supplied!
            if(arg1 == null){
                // don't validate when argument is not supplied!
                return;
            }
            
            if(!uniOp){
                // is the second argument constant value or command line argument?
                if(isArg2CmdLineArgument) {
                    arg2 = parser.getArgumentValue(getArg2());
                }else{
                    arg2 = getArg2();
                }
            }
        }else{
            arg1 = getArgumentIfSupplied(parser, getArg1Name());
            if(!uniOp){
                arg2 = getArgumentIfSupplied(parser, getArg2());
                
            }
        }
        
        if(isArg2WithCriteria()){
            String arg2Criteria = getArg2Criteria();
            if(!rule.validate(arg1, arg2, arg2Criteria)){
                throw new IllegalArgumentException(buildArgumentErrorMsg(arg1, arg2, arg2Criteria));
            }            
        } else {
            if(!rule.validate(arg1, arg2)){
                throw new IllegalArgumentException(buildArgumentErrorMsg(arg1, arg2, null));
            }               
        }
    }
    
    /**
     * Build argument validation error message
     * 
     * @param arg1 argument 1 or value
     * @param arg2 argument 2 or value
     * @return validation error message
     */
    private String buildArgumentErrorMsg(Object arg1, Object arg2, String criteria){
        StringBuilder buf = new StringBuilder();
        buf.append("Argument ")
           .append(getArg1Name())
           .append(rule);

        if(uniOp){
            buf.append("but ")
               .append(arg1 == null ? "not supplied" : arg1);
        }else{
            buf.append(getArg2());
            if(criteria != null){
                buf.append(criteria);
            }
            if(rule.isValidateOnValue()){
                buf.append(" but ");
                if(isArg2CmdLineArgument){
                   buf.append(arg1)
                      .append(" and ")
                      .append(arg2);
                } else {
                   buf.append(arg1);
                }
            }
            
            // process criteria
            if(isArg2WithCriteria()){
                CmdLineArgument argument2 = (CmdLineArgument)arg2;
                buf.append(" but ")
                   .append(argument2.getValue());
            }
        }  
        
        return buf.toString();
    }
    
    /**
     * Return the argument (object) if it is supplied
     * 
     * @param parser CmdLineArgumentParser instance
     * @param argName argument name, short or long
     * @return CmdLineArgument or null if not supplied
     */
    private static CmdLineArgument getArgumentIfSupplied(
            CmdLineArgumentParser parser, String argName)
    {
        if(parser.isArgumentSupplied(argName)){
            return parser.getArgument(argName);
        }  
        
        return null;
    }   
}
