package com.nusinfineon.util;

import java.util.ArrayList;
import java.util.Arrays;

public class Directories {
    public static final String ICON_APPLICATION = "/images/icon_large.png";

    public static final String SAVE_FILE_NAME = "saveFile.txt";
    public static final String SCRIPT_FILE_NAME = "script.txt";
    public static final String APP_EXE_FILE_NAME = "IBIS_Simulation.exe";

    public static final String INPUT_FOLDER_NAME = "Input";
    public static final String OUTPUT_FOLDER_NAME = "Output";
    public static final String RAW_OUTPUT_FOLDER_NAME = "Raw Output Excel Files";

    public static final String TABLEAU_EXCEL_FILE_NAME = "tableau-excel-file.xlsx";
    public static final String TABLEAU_WORKBOOK_SOURCE_DIR = "/output";
    public static final String TABLEAU_WORKBOOK_NAME = "IBIS_Simulation_Output_Visualisation.twb";
    public static final String PRODUCT_KEY_COST_FILE_DIR = "/output/product_key_cost.xlsx";

    public static final ArrayList<String> INPUT_EXCEL_SHEETS = new ArrayList<>(Arrays.asList(
            "Product Info and Eqpt Matrix", "Product Yield", "Process Time", "Changeover Time", "Eqpt Info",
            "Oven Info", "BIB ASRS Info", "Stocker Info", "BIB Info", "Location Info", "Elevator Info", "Trolley Info",
            "BIB Initial Allocation", "BIB ASRS Time", "Stocker Time", "Activity Time", "Manpower Requirement",
            "Movement", "Resource Group", "Resource Skill and Availability", "Time Table", "Time Table Property",
            "Actual Lot Info", "Demand Info", "MTBE-MED", "Node Network", "Node Link", "Resource Visual", "Settings"));
}
