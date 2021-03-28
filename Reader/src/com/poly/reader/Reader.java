package com.poly.reader;

import com.poly.logger.MsgInLog;
import com.poly.parser.SemanticParser;
import com.poly.parser.SyntaxParser;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;
import ru.spbstu.pipeline.IExecutable;
import ru.spbstu.pipeline.IReader;
import ru.spbstu.pipeline.RC;

public class Reader implements IReader {

    private final Logger logger;

    private Integer nProcBytes;
    private int fileSize;
    private FileInputStream fis;
    private IExecutable consumer;
    private ReaderGrammar grammar = new ReaderGrammar();
    private int nReadData = 0;

    public Reader(Logger logger) {
        this.logger = logger;
    }

    public static void main(String[] args) {
        try (FileInputStream file = new FileInputStream(
                "file.txt")) {
            Logger logger = Logger.getLogger("reader");
            IReader reader = new Reader(logger);
            reader.setConfig("config.txt");
            reader.setInputStream(file);
            reader.execute(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public RC setInputStream(FileInputStream file) {
        if (file == null) {
            return RC.CODE_INVALID_INPUT_STREAM;
        }
        fis = file;
        try {
            fileSize = (int) file.getChannel().size();
        } catch (IOException ex) {
            logger.severe(MsgInLog.INVALID_INPUT_STREAM.getMsg());
            return RC.CODE_INVALID_INPUT_STREAM;
        }
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setConfig(String cfgPath) {
        String[] args = new String[3];
        args[0] = cfgPath;
        args[1] = grammar.delimiter();
        args[2] = grammar.token(0);

        HashMap<String, String> syntaxValidMap = SyntaxParser.isValidCfg(args);

        nProcBytes = SemanticParser.getInteger(syntaxValidMap, grammar.token(1));

        if (nProcBytes == null) {
            logger.severe(MsgInLog.INVALID_CONFIG_DATA.getMsg());
            return RC.CODE_CONFIG_SEMANTIC_ERROR;
        }
        logger.info(MsgInLog.SUCCESS.name());
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setConsumer(IExecutable consumer) {
        if (consumer == null) {
            return RC.CODE_INVALID_ARGUMENT;
        }
        this.consumer = consumer;
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setProducer(IExecutable producer) {
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC execute(byte[] inputData) {
        RC rc = RC.CODE_SUCCESS;
        byte[] data = readFile();
        while (data != null) {
            rc = consumer.execute(data);

            if (rc != RC.CODE_SUCCESS) {
                break;
            }

            data = readFile();
        }
        return rc;
    }

    private byte[] readFile() {

        if (fis == null) {
            return null;
        }

        try {
            byte[] buffer = null;

            if (nProcBytes < fileSize - nReadData) {
                buffer = new byte[nProcBytes];
                fis.read(buffer);
                nReadData += nProcBytes;
            } else if (fileSize - nReadData > 0) {
                buffer = new byte[fileSize - nReadData];
                fis.read(buffer);
                nReadData = fileSize;
            }

            return buffer;
        } catch (IOException ex) {
            logger.severe(MsgInLog.FAILED_TO_READ.getMsg());
            return null;
        }
    }
}
