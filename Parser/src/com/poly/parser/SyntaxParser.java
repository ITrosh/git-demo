package com.poly.parser;

import java.io.*;
import java.util.HashMap;
import java.util.logging.Logger;

import com.poly.logger.MsgInLog;

public class SyntaxParser {

    private static final Logger logger = Logger.getLogger(SyntaxParser.class.getName());

    private SyntaxParser() { }

    static public HashMap<String, String> isValidCfg(String[] arguments) {
        if (arguments.length != 3) {
            logger.severe(MsgInLog.INCORRECT_NUM_OF_ARGUMENTS.getMsg());
            return null;
        }
        if (arguments[0] == null || arguments[1] == null || arguments[2] == null) {
            logger.severe(MsgInLog.INVALID_ARGUMENTS.getMsg());
            return null;
        }
        logger.info(MsgInLog.START_PARS.getMsg() + "\n" + arguments[0]);
        HashMap<String, String> validMap = new HashMap<>();
        try {
            File file = new File(arguments[0]);
            FileReader reader = new FileReader(file);
            BufferedReader bufReader = new BufferedReader(reader);
            String line = bufReader.readLine();
            while (line != null) {
                if (line.matches("[ ]*[a-zA-z_][a-zA-Z_0-9]*[ ]*" + arguments[1]
                        + "[ ]*[\\\\a-zA-Z_0-9'.-]+[ ]*" + arguments[2])) {
                    String newLine = line.replace(" ", "").replace(arguments[2], "");
                    int equalsSymbol = newLine.indexOf(arguments[1]);
                    validMap.put(newLine.substring(0, equalsSymbol),
                            newLine.substring(equalsSymbol + 1, newLine.length()));
                } else {
                    logger.info(MsgInLog.INVALID_EXPRESSION.getMsg() + ": " + line);
                }
                line = bufReader.readLine();
            }
            bufReader.close();
            reader.close();
        } catch (FileNotFoundException ex) {
            logger.severe(MsgInLog.NO_CONFIG_FILE.getMsg());
        } catch (IOException ex) {
            logger.severe(MsgInLog.CONFIG_GRAMMAR_ERROR.getMsg());
        }
        return validMap;

    }
}
