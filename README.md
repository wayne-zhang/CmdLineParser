# CmdLineParser - Java command line argument parser that supports Posix format

This project has no other dependencies even test case

Command line argument definination

Command line argument, which composed of
    short name, e.g -a
    long name, e.g --action
    a flag that indicates if the argument has or has not value,
    a list of enumeration values, e.g [create|update|delete]
    value, e.g the value of argument
    a flag that indicates if the argument is mandatory

Initialization pattern as the following:

   short name,long name,has value[,value enumeration][,mandatory]

for example

   -a,--action,true,create|update|delete,true
   -t,--type,true,CLOB|BLOB
   -v,--verbose,false
   -i,--input,true,,true


An example of how to use CmdLineArgumentParser:
	
	pulbic static void main(String[] args){
        CmdLineArgumentParser parser = new CmdLineArgumentParser();
        parser.defineArguments(
            "-f,--file,true,,true",
            "-d,--scanDir,true,,true",
            "-b,--backupDir,true",
            "-a,--action,true,ADD|REMOVE,true",
            "-l,--line,true,,true",
            "-x,--excludePath,true",
            "-w,--where,true,BEGIN|END,false",
            "-c,--criteria,true",
            "-v,--verbose,false"
        );
        
        // --where and --criteria valid only when --action is ADD
        parser.addArgumentRules(
            "-c dependsOn -a=ADD",
            "-w dependsOn -a=ADD"
        );
        
        try{
            parser.parse(args);
        }catch(Exception e){
            e.printStackTrace();            
            parser.help();
            
            System.exit(1);
        }
        
        File scanDir = new File(parser.getArgumentValue("-d"));
        Action action = Action.valueOf(parser.getArgumentValue("-a"));
       
		String criteria = null;
		if(parser.isArgumentSupplied("-c")){
			// long name can be used too
			criteria = parser.getArgumentValue("--criteira");
		}
	
	}
