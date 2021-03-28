package com.poly.parser;

import java.util.HashMap;
import java.util.logging.Logger;

import com.poly.logger.MsgInLog;

public class SemanticParser {

    private static final Logger logger = Logger.getLogger(SemanticParser.class.getName());

    private SemanticParser() {}

    public static Integer getInteger(HashMap<String, String> syntaxMap, String token) {
        if (syntaxMap == null || token == null) {
            logger.severe(MsgInLog.INVALID_ARGUMENTS.getMsg());
            return null;
        }
        try {
            Integer number = Integer.parseInt(syntaxMap.get(token));
            return number;
        } catch (NumberFormatException | NullPointerException ex) {
            logger.severe(MsgInLog.NOT_FOUND_TOKEN.getMsg() + token);
        }
        return null;
    }

    public static Double getDouble(HashMap<String, String> syntaxMap, String token) {
        if (syntaxMap == null || token == null) {
            logger.severe(MsgInLog.INVALID_ARGUMENTS.getMsg());
            return null;
        }
        try {
            Double number = Double.parseDouble(syntaxMap.get(token));
            return number;
        } catch (NumberFormatException | NullPointerException ex) {
            logger.severe(MsgInLog.NOT_FOUND_TOKEN.getMsg() + token);
        }
        return null;
    }

    public static String getString(HashMap<String, String> syntaxMap, String token) {
        if (syntaxMap == null || token == null) {
            logger.severe(MsgInLog.INVALID_ARGUMENTS.getMsg());
            return null;
        }
        String str = syntaxMap.get(token);
        if (str == null) {
            logger.severe(MsgInLog.CONFIG_SEMANTIC_ERROR.getMsg() + ":\n" + token);
        }
        return str;
    }
}
