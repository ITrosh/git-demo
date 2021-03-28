package com.poly.manager;

import java.util.logging.Logger;
import com.poly.logger.MsgInLog;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    public static void main(String[] args) {
        if (args.length != 1) {
            logger.severe(MsgInLog.INVALID_ARGUMENTS.getMsg());
        } else {
            Manager manager = new Manager(args[0]);
            manager.execute();
        }
    }
}
