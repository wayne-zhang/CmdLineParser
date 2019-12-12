package cmdline;

import cmdline.CmdLineArgument.CmdLineArgumentBuilder;

/**
 * Self implemented (unit/integration) test without dependency of ANY framework.
 * 
 * @author Wayne Zhang
 */
public class CmdLineArgumentParserTest{
    private CmdLineArgumentParser parser = new CmdLineArgumentParser(
            "-a, --action, true, create|update|delete", 
            "-v,--verbose, false",
            "-i,--inputFile,true"
    );
    
    public static void main(String[] args)  {
        CmdLineArgumentParserTest test = new CmdLineArgumentParserTest();
        
        test.testValid();
        test.testWrongArg();
        test.testWrongArgValue();
        test.testWrongArgValue4NoValueArg();
        test.testGetUndefinedArgumentValue();
        test.testNoParseCalled();
        test.testNoArgumentValueSupplied();
        test.testInvalidArgumentDefinationWrongValue();
        test.testInvalidArgumentDefinationWrongFormat();
        test.testInvalidArgumentDefinationWrongBoolean();
        test.testManditoryArgument();
        test.testManditoryArgDefine();
        test.testManditoryArgumentMissing();
        test.testInvalidArgumentNames();
        test.testCmdLineArgumentBuilder();
        test.testCmdLineArgumentBuilderWrongUsage();
        test.testDefineArgsDuplicated();
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
    
    private void testValid(){
        parser.parse(new String[]{"-v","-a","create"});
        
        System.out.println("-a: " + parser.getArgumentValue("-a"));
        System.out.println("--action: " + parser.getArgumentValue("--action"));
        System.out.println("-v: " + parser.isArgumentSupplied("-v"));
        
        //System.out.println(parser.getHelpInfo());
        parser.help(CmdLineArgumentParserTest.class);
    }
    
    private void testNoArgumentValueSupplied(){
        try{
            parser.parse(new String[] {"-a"});
            
            fail("test fail");
        }catch(RuntimeException e){
            // exception expected
        }
    }    
    
    private void testWrongArg(){
        try{
            parser.parse("--foo", "bar", "-v");
            
            fail("test fail");
        }catch(RuntimeException e){
            // exception expected
        }
    }
    
    private void testWrongArgValue(){
        try{
            parser.parse("-v", "--action", "drop");
            
            fail("test fail");
        }catch(RuntimeException e){
            // exception expected
        }
    }    
    
    private void testWrongArgValue4NoValueArg(){
        try{
            parser.parse("-v", "bad", "--action", "drop");
            
            fail("test fail");
        }catch(IllegalArgumentException e){
            // exception expected
        }
    }   
    
    private void testGetUndefinedArgumentValue(){
        try{
            parser.parse(new String[]{"-v","-a","create"});
            parser.getArgumentValue("-b");
            
            fail("test fail");
        }catch(RuntimeException e){
            // exception expected
        }
    }
    
    private void testNoParseCalled(){
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
    
    private void testInvalidArgumentDefinationWrongValue(){
        try{
            CmdLineArgumentParser parser = new CmdLineArgumentParser();

            parser.defineArgument("-a--action,true,create|update|delete");
            parser.defineArgument("-v,--verbose,false");              
            
            fail("test fail");
        }catch(RuntimeException e){
            // exception expected
        }
    }
    
    private void testInvalidArgumentDefinationWrongFormat(){
        try{
            CmdLineArgumentParser parser = new CmdLineArgumentParser();

            parser.defineArgument("-v,--verbose");              
            
            fail("test fail");
        }catch(IllegalArgumentException e){
            // exception expected
        }
    }
    
    private void testInvalidArgumentDefinationWrongBoolean(){
        try{
            CmdLineArgumentParser parser = new CmdLineArgumentParser();

            parser.defineArgument("-v,--verbose,bool?");              
            
            fail("test fail");
        }catch(IllegalArgumentException e){
            // exception expected
        }
    }    
    
    private void testManditoryArgument(){
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        CmdLineArgument input = parser.defineArgument("-i,--input,true");
        input.setMandatory(true);
        parser.defineArgument("-o, --output, true");
        
        parser.parse(new String[]{"-i", "/tmp/a.txt"});
    }
    
    private void testManditoryArgDefine(){
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        parser.defineArgument("-i,--input,true,,true");
        parser.defineArgument("-o, --output, true");
        
        parser.parse(new String[]{"-i", "/tmp/a.txt"});
    }    
    
    private void testManditoryArgumentMissing(){
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
    
    private void testInvalidArgumentNames(){
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        
        try{
            parser.defineArgument("--s,-short,false");
            
            fail("Illegal argument exception expected");
        }catch(IllegalArgumentException e){
            // expected 
        }
    }
    
    private void testCmdLineArgumentBuilder() {
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
    
    private void testCmdLineArgumentBuilderWrongUsage() {
        CmdLineArgumentBuilder builder = CmdLineArgument.builder();
        
        builder.shortName("-v").longName("--verbose")
                .hasValue(false).build();
        
        try{
            builder.isMandatory(false);
            
            fail("Can't set information after build() called");
        }catch(Exception e) {
            // exception expected
        }
    }     

    private void testDefineArgsDuplicated() {
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        String argumentDefination = "-v,--verbose,false,";
        parser.defineArgument(argumentDefination);
        
        try{
            // other attributes are not significant, only names matters
            parser.defineArgument(argumentDefination + ",true");
            
            fail("Duplicate argment defination exception expected");
        }catch(RuntimeException e) {
            // exception expected
        }        
    }
}