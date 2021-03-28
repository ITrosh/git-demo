package com.poly.reader;

import com.poly.logger.MsgInLog;

import com.poly.parser.SemanticParser;
import com.poly.parser.SyntaxParser;

import java.io.*;
import java.util.HashMap;
import java.util.logging.Logger;

import ru.spbstu.pipeline.IExecutable;
import ru.spbstu.pipeline.IReader;
import ru.spbstu.pipeline.RC;

public class Reader implements IReader {

    private final Logger logger;
    private final ReaderGrammar grammar = new ReaderGrammar();

    private FileInputStream inputStream;
    private IExecutable consumer;
    private Integer numOFReadBytes;
    private Integer bufferSize;
    private Integer fileSize;

    public Reader(Logger logger) {
        this.logger = logger;
        this.numOFReadBytes = 0;
    }

    @Override
    public RC setInputStream(FileInputStream file) {
        if (file == null) {
            return RC.CODE_INVALID_INPUT_STREAM;
        }
        inputStream = file;
        try {
            fileSize = (int) file.getChannel().size();
            return RC.CODE_SUCCESS;
        } catch (IOException ex) {
            logger.severe(MsgInLog.INVALID_INPUT_STREAM.getMsg());
            return RC.CODE_INVALID_INPUT_STREAM;
        }
    }

    @Override
    public RC setConfig(String cfgPath) {
        String[] args = new String[3];
        args[0] = cfgPath;
        args[1] = grammar.delimiter();
        args[2] = grammar.token(0);

        HashMap<String, String> syntaxValidMap = SyntaxParser.isValidCfg(args);

        bufferSize = SemanticParser.getInteger(syntaxValidMap, grammar.token(1));

        if (bufferSize == null) {
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

        if (inputStream == null) {
            return null;
        }

        try {
            byte[] buffer = null;

            if (bufferSize < fileSize - numOFReadBytes) {
                buffer = new byte[bufferSize];
                inputStream.read(buffer);
                numOFReadBytes += bufferSize;
            } else if (fileSize - numOFReadBytes > 0) {
                buffer = new byte[fileSize - numOFReadBytes];
                inputStream.read(buffer);
                numOFReadBytes = fileSize;
            }

            return buffer;
        } catch (IOException ex) {
            logger.severe(MsgInLog.FAILED_TO_READ.getMsg());
            return null;
        }
    }
}
