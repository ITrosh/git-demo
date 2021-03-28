import java.util.HashMap;
import java.util.logging.Logger;

public class SemanticParser {
    private static final Logger logger = Logger.getLogger(SemanticParser.class.getName());

    private SemanticParser() {}

    public static Integer getInteger(HashMap<String, String> syntaxMap, String token) {
        if (syntaxMap == null || token == null) {
            logger.severe(MsgInLog.INVALID_ARGUMENTS.msg);
            return null;
        }
        try {
            Integer number = Integer.parseInt(syntaxMap.get(token));
            return number;
        }
        catch(NumberFormatException | NullPointerException ex) {
            logger.severe(MsgInLog.NOT_FOUND_TOKEN.msg + token);
        }
        return null;
    }

    public static Double getDouble(HashMap<String, String> syntaxMap, String token) {
        if (syntaxMap == null || token == null) {
            logger.severe(MsgInLog.INVALID_ARGUMENTS.msg);
            return null;
        }
        try {
            Double number = Double.parseDouble(syntaxMap.get(token));
            return number;
        }
        catch(NumberFormatException | NullPointerException ex) {
            logger.severe(MsgInLog.NOT_FOUND_TOKEN.msg + token);
        }
        return null;
    }

    public static String getString(HashMap<String, String> syntaxMap, String token) {
        if (syntaxMap == null || token == null) {
            logger.severe(MsgInLog.INVALID_ARGUMENTS.msg);
            return null;
        }
        String str = syntaxMap.get(token);
        if (str == null) {
            logger.severe(MsgInLog.CONFIG_SEMANTIC_ERROR.msg + ":\n" + token);
        }
        return str;
    }
}

