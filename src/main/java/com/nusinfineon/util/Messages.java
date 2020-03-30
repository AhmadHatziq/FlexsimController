package com.nusinfineon.util;

import static com.nusinfineon.util.Directories.APP_EXE_FILE_NAME;
import static com.nusinfineon.util.Directories.SAVE_FILE_NAME;

public class Messages {
    public static final String ABOUT_MESSAGE = "This application is an optimised interface for the IBIS simulation model on FlexSim.\n"
            + "\nYou can try a range of minimum batch sizes to simulate based on the supplied input information.\n"
            + "\nYou can also choose a lot sequencing rule which sorts the supplied lots in the desired sequence, and define a few other settings, for the simulation.\n"
            + "\nEnsure all fields are filled in correctly before running the simulation!\n"
            + "\nAll input fields are automatically saved upon running and exiting, and automatically loaded the next time the program is opened.\n";

    public static final String CONFIRM_SAVE_MESSAGE = "The input fields will be saved in \"" + SAVE_FILE_NAME + "\" in the folder of \"" + APP_EXE_FILE_NAME + "\"."
            + "\nAny save files in the folder will be overwritten!";

    public static final String CONFIRM_LOAD_MESSAGE = "The input fields will be loaded from \"" + SAVE_FILE_NAME + "\" in the folder of \"" + APP_EXE_FILE_NAME + "\"."
            + "\nEnsure that the save file is previously saved from the program.";

    public static final String CONFIRM_RUN_MESSAGE = "Warning: The more runs there are, the longer it will take until completion."
            + "\nPreviously generated files in the Input and Output folders will be overwritten."
            + "\nPlease save and close all opened files on Excel before you click OK.";

    public static final String COMPLETED_MESSAGE = "You may run another simulation or close the program.\n"
            + "\nYou can find the Output folder in the directory which you have defined for Output Excel.";

    public static final String SAVE_FILE_WRONG_FORMAT_MESSAGE = SAVE_FILE_NAME
            + " is of the wrong format!\nPlease place a previously saved file into the folder of \"" + APP_EXE_FILE_NAME + "\".";

    public static final String SAVE_FILE_NOT_FOUND_MESSAGE = SAVE_FILE_NAME
            + " cannot be found!\nPlease place a previously saved file into the folder of \"" + APP_EXE_FILE_NAME + "\".";
}
