package cmdline;

import cmdline.CmdLineArgument.CmdLineArgumentBuilder;

/**
 * Self implemented (unit/integration) test without dependency of ANY framework.
 * 
 * @author Wayne Zhang
 */
public class CmdLineArgumentParserTest{
    private final CmdLineArgumentParser parser = new CmdLineArgumentParser(
            "-a, --action, true, create|update|delete", 
            "-v,--verbose, false",
            "-i,--inputFile,true"
    );
    
    public static void main(String[] args)  {
        TestRunner.fire(CmdLineArgumentParserTest.class);
    }
    
    /**
     * Method to mock JUnit fail
     * 
     * @param msg 
     */
    private static void fail(String msg){       
        new RuntimeException(msg).printStackTrace();
        System.exit(0);
    }    
    
    public void testValid(){
        parser.parse(new String[]{"-v","-a","create"});
        
        System.out.println("-a: " + parser.getArgumentValue("-a"));
        System.out.println("--action: " + parser.getArgumentValue("--action"));
        System.out.println("-v: " + parser.isArgumentSupplied("-v"));
        
        //System.out.println(parser.getHelpInfo());
        parser.help();
    }
    
    public void testNoArgumentValueSupplied(){
        try{
            parser.parse(new String[] {"-a"});
            
            fail("test fail");
        }catch(RuntimeException e){
            // exception expected
        }
    }    
    
    public void testWrongArg(){
        try{
            parser.parse("--foo", "bar", "-v");
            
            fail("test fail");
        }catch(RuntimeException e){
            // exception expected
        }
    }
    
    public void testWrongArgValue(){
        try{
            parser.parse("-v", "--action", "drop");
            
            fail("test fail");
        }catch(RuntimeException e){
            // exception expected
        }
    }    
    
    public void testWrongArgValue4NoValueArg(){
        try{
            parser.parse("-v", "bad", "--action", "drop");
            
            fail("test fail");
        }catch(IllegalArgumentException e){
            // exception expected
        }
    }   
    
    public void testGetUndefinedArgumentValue(){
        try{
            parser.parse(new String[]{"-v","-a","create"});
            parser.getArgumentValue("-b");
            
            fail("test fail");
        }catch(RuntimeException e){
            // exception expected
        }
    }
    
    public void testNoParseCalled(){
        try{
            CmdLineArgumentParser parser = new CmdLineArgumentParser();

            parser.defineArgument("-a--action,true,create|update|delete");
            parser.defineArgument("-v,--verbose,false,");

            parser.getArgumentValue("-a");
            
            fail("test fail");
        }catch(RuntimeException e){
            // exception expected
        }
    }
    
    public void testInvalidArgumentDefinationWrongValue(){
        try{
            CmdLineArgumentParser parser = new CmdLineArgumentParser();

            parser.defineArgument("-a--action,true,create|update|delete");
            parser.defineArgument("-v,--verbose,false");              
            
            fail("test fail");
        }catch(RuntimeException e){
            // exception expected
        }
    }
    
    public void testInvalidArgumentDefinationWrongFormat(){
        try{
            CmdLineArgumentParser parser = new CmdLineArgumentParser();

            parser.defineArgument("-v,--verbose");              
            
            fail("test fail");
        }catch(IllegalArgumentException e){
            // exception expected
        }
    }
    
    public void testInvalidArgumentDefinationWrongBoolean(){
        try{
            CmdLineArgumentParser parser = new CmdLineArgumentParser();

            parser.defineArgument("-v,--verbose,bool?");              
            
            fail("test fail");
        }catch(IllegalArgumentException e){
            // exception expected
        }
    }    
    
    public void testManditoryArgument(){
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        CmdLineArgument input = parser.defineArgument("-i,--input,true");
        input.setMandatory(true);
        parser.defineArgument("-o, --output, true");
        
        parser.parse(new String[]{"-i", "/tmp/a.txt"});
    }
    
    public void testManditoryArgDefine(){
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        parser.defineArgument("-i,--input,true,,true");
        parser.defineArgument("-o, --output, true");
        
        parser.parse(new String[]{"-i", "/tmp/a.txt"});
    }    
    
    public void testManditoryArgumentMissing(){
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        CmdLineArgument input = parser.defineArgument("-i,--input,true");
        input.setMandatory(true);
        parser.defineArgument("-o, --output, true,,no");
        
        try{
            parser.parse("-o", "/tmp/a.txt");
            
            fail("Validate should fail");
        }catch(RuntimeException e){
            // expected
        }
    }    
    
    public void testInvalidArgumentNames(){
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        
        try{
            parser.defineArgument("--s,-short,false");
            
            fail("Illegal argument exception expected");
        }catch(IllegalArgumentException e){
            // expected 
        }
    }
    
    public void testCmdLineArgumentBuilder() {
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        parser.defineArgument(CmdLineArgument.builder()
                    .isMandatory(true)
                    .shortName("-a")
                    .longName("--action")
                    .enumValue("create")
                    .enumValue("udpate")
                    .enumValue("delete")
                    .hasValue(true)
                    .build()
        );
        parser.defineArgument(CmdLineArgument.builder()
                .shortName("-v").longName("--verbose")
                .hasValue(false).build()
        );
        
        parser.parse(new String[]{"-v","-a","create"});
        
        System.out.println("-a: " + parser.getArgumentValue("-a"));
        System.out.println("--action: " + parser.getArgumentValue("--action"));
        System.out.println("-v: " + parser.isArgumentSupplied("-v"));
    }    
    
    public void testCmdLineArgumentBuilderWrongUsage() {
        CmdLineArgumentBuilder builder = CmdLineArgument.builder();
        
        builder.shortName("-v").longName("--verbose")
                .hasValue(false).build();
        
        try{
            builder.isMandatory(false);
            
            fail("Can't set information after build() called");
        }catch(Exception e) {
            // exception expected
            asExpected(e);
        }
    }     

    public void testDefineArgsDuplicated() {
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        String argumentDefination = "-v,--verbose,false,";
        parser.defineArgument(argumentDefination);
        
        try{
            // other attributes are not significant, only names matters
            parser.defineArgument(argumentDefination + ",true");
            
            fail("Duplicate argment defination exception expected");
        }catch(RuntimeException e) {
            // exception expected
            
            asExpected(e);
        }        
    }
    
    public void testArgumentRulesMandatory(){
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        String argumentDefination = "-v,--verbose,false,";
        parser.defineArgument(argumentDefination);
        parser.addArgumentRules("-v isMandatory");
        
        try{
            parser.parse("");
            
            fail("-v is mandatory");
        }catch(RuntimeException e) {
            // exception expected
            asExpected(e);
        }
        
        // OK
        parser.parse("-v");
    }
    
    private static void asExpected(Exception e){
        System.out.println("Exception " + e.getClass().getName() + " is as expected: ");
        e.printStackTrace(System.out);
    }
    
    public void testArgumentRulesIsInteger(){
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        String argumentDefination = "-y,--year,true,";
        parser.defineArgument(argumentDefination);
        parser.addArgumentRules("-y isInteger");
        
        try{
            parser.parse("-y",  "nextYear");
            
            fail("-y is integer");
        }catch(RuntimeException e) {
            // exception expected
            asExpected(e);
        }
        
        parser.parse("--year", "2000");
    }    
    
    public void testArgumentRulesIsNumber(){
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        String argumentDefination = "-q,--qantitiy,true,";
        parser.defineArgument(argumentDefination);
        parser.addArgumentRules("-q isNumber");
        
        try{
            parser.parse("-q", "not-a-number");
            
            fail("-q is number");
        }catch(RuntimeException e) {
            // exception expected
            asExpected(e);
        }
        
        parser.parse("-q", "120.78");
    }  
    
    public void testArgumentRulesLessThanConstant(){
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        parser.defineArgument("-q,--qantitiy,true,");
        parser.addArgumentRules("-q lessThan 100.05");
        
        try{
            parser.parse("-q", "100.06");
            
            fail("-q less than 100.05");
        }catch(RuntimeException e) {
            // exception expected
            asExpected(e);
        }
        
        parser.parse("-q", "99.78");
        
        // no argument should be OK
        parser.parse("");
    }  
    
    public void testArgumentRulesLessThanArgument(){
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        parser.defineArguments("-q,--qantitiy,true,", "-m,--maxQuantity,true,");
        parser.addArgumentRules("-q lessThan -m");
        
        try{
            parser.parse("-q", "100.06", "-m", "90");
            
            fail("-q less than -m");
        }catch(RuntimeException e) {
            // exception expected
            asExpected(e);
        }
        
        parser.reset();
        parser.parse("-q", "99.78", "-m", "100");
        
        // no max argument, validation skipped
        parser.reset();;        
        parser.parse("-q", "1000");
        
        // no quantity, validate skipped!
        parser.reset();
        parser.parse("-m", "0.1");
    }     
    
    public void testArgumentRulesDependsOn(){
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        parser.defineArguments("-s,--startTag,true,", "-S,--keepStartTag,false,");
        final String rule = "-S dependsOn -s";
        parser.addArgumentRules(rule);
        
        try{
            parser.parse("-S");
            
            fail(rule);
        }catch(RuntimeException e) {
            // exception expected
            asExpected(e);
        }
        
        parser.parse("-s", "INFO", "-S");        
    }
    
    public void testArgumentRulesDependsOnWithCriteriaEquals(){
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        parser.defineArguments("-s,--startTag,true,", "-S,--keepStartTag,false,");
        final String rule = "-S dependsOn -s=<TIME>";
        parser.addArgumentRules(rule);
        
        try{
            parser.parse("-S");
            
            fail(rule);
        }catch(RuntimeException e) {
            // exception expected
            asExpected(e);
        }
        
        try{
            parser.reset();
            parser.parse("-s", "INFO", "-S");
            
            fail(rule);
        }catch(RuntimeException e){
            // expected
        }
        
        try{
            parser.reset();
            parser.parse("-s", "INFO");
        }catch(RuntimeException e){
            fail(rule);
        }
        
        parser.reset();
        parser.parse("-s", "<TIME>", "-S");
    }    
    
    public void testArgumentRulesDependsOnWithCriteriaGreateThan(){
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        parser.defineArguments("-s,--startTime,true,", "-S,--keepStartTime,false,");
        final String rule = "-S dependsOn -s>12";
        parser.addArgumentRules(rule);
        
        try{
            parser.parse("-s", "6", "-S");
            
            fail(rule);
        }catch(Exception e){
            // expected
        }
        
        parser.reset();
        parser.parse("-s", "13", "-S");
    }    
    
    public void testArgumentRulesDependsOnWithCriteriaLessThan(){
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        parser.defineArguments("-s,--startTime,true,", "-S,--keepStartTime,false,");
        final String rule = "-S dependsOn -s<12";
        parser.addArgumentRules(rule);
        
        parser.parse("-s", "6", "-S");

        
        try{
            parser.reset();
            parser.parse("-s", "13", "-S");
            
            fail(rule);
        }catch(Exception e){
            // expected
        }
    }      
    
    public void testArgumentRulesConflictsWith(){
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        parser.defineArguments("-u,--unique,false,", "-t,--keepTimestamp,false,");
        final String rule = "-u conflictsWith -t";
        parser.addArgumentRules(rule);
        
        try{
            parser.parse("-t", "-u");
            
            fail(rule);
        }catch(RuntimeException e) {
            // exception expected
            asExpected(e);
        }
        
        parser.reset();
        parser.parse("-u");
        
        parser.reset();
        parser.parse("-t");
    }
    
    public void testArgumentRulesIsIn(){
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        parser.defineArguments("-a,--action,true,");
        final String rule = "-a isIn [insert,update,delete]";
        parser.addArgumentRules(rule);
        
        try{
            parser.parse("-a", "read");
            
            fail(rule);
        }catch(RuntimeException e) {
            // exception expected
            asExpected(e);
        }
        
        parser.reset();
        parser.parse("-a", "insert");
        
        parser.reset();
        parser.parse("-a", "delete");
    }
    
    public void testArgumentRulesIsInDelimiterOr(){
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        parser.defineArguments("-l,--lob,true");
        final String rule2= "-l isIn 'CLOB|BLOB'";
        parser.addArgumentRules(rule2);
        
        // test delimiter |
        try{
            parser.parse("-l", "VARCHAR");
            
            fail(rule2);
        }catch(RuntimeException e) {
            // exception expected
            asExpected(e);
        }
        
        parser.reset();
        parser.parse("-l", "BLOB");
    }    
}