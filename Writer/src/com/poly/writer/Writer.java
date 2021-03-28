package com.poly.writer;

import java.io.*;
import java.util.HashMap;
import java.util.logging.Logger;

import com.poly.logger.MsgInLog;

import com.poly.parser.SemanticParser;
import com.poly.parser.SyntaxParser;

import ru.spbstu.pipeline.IWriter;
import ru.spbstu.pipeline.IExecutable;
import ru.spbstu.pipeline.RC;

public class Writer implements IWriter {

    private final Logger logger;
    private final WriterGrammar grammar = new WriterGrammar();

    private FileOutputStream outputStream;

    public Writer(Logger logger) {
        this.logger = logger;
    }

    @Override
    public RC setOutputStream(FileOutputStream file) {
        if (file == null) {
            return RC.CODE_INVALID_OUTPUT_STREAM;
        }
        outputStream = file;
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setConfig(String cfgPath) {
        String[] args = new String[3];
        args[0] = cfgPath;
        args[1] = grammar.delimiter();
        args[2] = grammar.token(0);

        HashMap<String, String> syntaxValidMap = SyntaxParser.isValidCfg(args);

        Integer bufferSize = SemanticParser.getInteger(syntaxValidMap, grammar.token(1));

        if (bufferSize == null) {
            logger.severe(MsgInLog.INVALID_CONFIG_DATA.getMsg());
            return RC.CODE_CONFIG_SEMANTIC_ERROR;
        }

        logger.info(MsgInLog.SUCCESS.getMsg());
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setConsumer(IExecutable consumer) {
        if (consumer == null) {
            return RC.CODE_INVALID_ARGUMENT;
        }
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setProducer(IExecutable producer) {
        if (producer == null) {
            return RC.CODE_INVALID_ARGUMENT;
        }
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC execute(byte[] inputData){
        if (inputData == null) {
            return RC.CODE_SUCCESS;
        }
        try{
            outputStream.write(inputData);
            return RC.CODE_SUCCESS;
        } catch (IOException ex) {
            logger.severe(MsgInLog.FAILED_TO_WRITE.getMsg());
            return RC.CODE_FAILED_TO_WRITE;
        }
    }
}
