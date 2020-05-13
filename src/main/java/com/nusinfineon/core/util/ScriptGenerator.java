package com.nusinfineon.core.util;

import static com.nusinfineon.util.Directories.SCRIPT_FILE_NAME;
import static com.nusinfineon.core.util.FlexScriptDefaultCodes.GET_PROCESS_TIME_CODE;
import static com.nusinfineon.core.util.FlexScriptDefaultCodes.MAIN_15_CODE;
import static com.nusinfineon.core.util.FlexScriptDefaultCodes.ON_RUN_STOP_CODE;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ScriptGenerator {

    private String scriptFilepath = "./" + SCRIPT_FILE_NAME;
    private File scriptFile;
    private String stopTime;
    private String runSpeed;

    public ScriptGenerator (String runSpeed, String stopTime){
        this.runSpeed = "runspeed(" + runSpeed + ");";
        this.stopTime = "stoptime(" + stopTime + ");";
    }

    /**
     * Creates the Flexscript for the model
     * @throws IOException
     */
    public File generateScript(String inputLocation, String inputFile, String outputLocation,
                               String outputFile, String excelOutputFileName) throws IOException {
        scriptFile = new File(scriptFilepath);
        scriptFile.createNewFile();
        FileWriter fileWriter = new FileWriter(scriptFilepath);
        fileWriter.write(runSpeed + "\n"
                + stopTime + "\n"
                +"\nshowprogressbar(\"\");"
                + "MAIN2LoadData (\"" + inputLocation + "\",\"" + inputFile + "\");\n"
                + "excellaunch();\n"
                + editNodeCode("RunStop", "MODEL://Tools//OnRunStop", "concat(" + ON_RUN_STOP_CODE
                + ",\"MAIN15WriteReports(true, \\\""
                + outputLocation + "\", " + "\\\"" + outputFile
                + "\\\" , \\\"" + excelOutputFileName + "\\\");"
                + "\\nhideprogressbar();"
                + "\\nsocketinit();"
                + "\\nint socknum = clientcreate();"
                + "\\nclientconnect(socknum,\\\"127.0.0.1\\\",1880);"
                + "\\nclientsend(socknum,\\\"REQ:service\\\")\\n;"
                + "\\nclientclose (socknum);"
                + "\\nsocketend();"
                + "\\ncmdexit ();\\n}\")")
                + editNodeCode("ProcessTime", "MODEL:/Tools/UserCommands/ProcessTimeGetTotal/code", GET_PROCESS_TIME_CODE)
                + editNodeCode("MAIN15", "MODEL://Tools/UserCommands//MAIN15WriteReports//code", MAIN_15_CODE)
                + "MAINBuldAndRun ();\nresetmodel();\ngo();");
        fileWriter.close();

        return scriptFile;
    }

    /**
     * Function to generate a default template for replace code in a Flexsim node
     * @param name
     * @param nodePath
     * @param code
     * @return script
     */
    public String editNodeCode(String name, String nodePath, String code) {
        String nodeName = name + "Node";
        String codeName = name + "Code";
        String script = "treenode " + nodeName + " = node(\"" + nodePath + "\");\n"
                + "string " + codeName + " = " + code + ";\n"
                + "setnodestr(" + nodeName + "," + codeName + ");\n"
                + "enablecode(" + nodeName + ");\n"
                + "buildnodeflexscript(" + nodeName + ");\n";
        return script;
    }
}
