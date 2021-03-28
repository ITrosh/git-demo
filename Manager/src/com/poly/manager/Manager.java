package com.poly.manager;

import ru.spbstu.pipeline.RC;
import ru.spbstu.pipeline.IPipelineStep;
import ru.spbstu.pipeline.IReader;
import ru.spbstu.pipeline.IWriter;
import ru.spbstu.pipeline.IConfigurable;

import java.io.*;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.ArrayDeque;
import java.lang.reflect.InvocationTargetException;

import com.poly.logger.MsgInLog;

import com.poly.parser.SyntaxParser;

public class Manager {

    private IPipelineStep pipelineStart;
    private static final ManagerGrammar grammar = new ManagerGrammar();
    private static final Logger logger = Logger.getLogger(Manager.class.getName());
    private FileInputStream inputStream;
    private FileOutputStream outputStream;
    private RC returningCode;

    public Manager(String cfgPath) {
        String[] args = new String[3];
        args[0] = cfgPath;
        args[1] = grammar.delimiter();
        args[2] = grammar.token(0);

        this.returningCode = null;

        HashMap<String, String> validSyntaxMap = SyntaxParser.isValidCfg(args);

        RC returningCode = createConveyor(validSyntaxMap);

        if (returningCode != RC.CODE_SUCCESS) {
            pipelineStart = null;
            logger.severe(MsgInLog.FAILED_PIPELINE_CONSTRUCTION.getMsg());
        } else {
            logger.info(MsgInLog.SUCCESS.getMsg());
        }
    }

    private RC createConveyor(HashMap<String, String> validSyntaxMap) {

        ArrayDeque<String> workersNames = new ArrayDeque<>();
        ArrayDeque<String> workersCfg = new ArrayDeque<>();
        RC returningCode = fillArrayDequeues(validSyntaxMap, workersNames, workersCfg);

        if (returningCode != RC.CODE_SUCCESS)
            return returningCode;

        String inputPath = validSyntaxMap.get(grammar.token(5));
        String outputPath = validSyntaxMap.get(grammar.token(6));

        try {
            IReader reader = setWorker(workersNames.pollFirst(), workersCfg.pollFirst());
            if (reader == null) {
                return this.returningCode;
            }

            reader.setProducer(null);
            returningCode = setInputStream(reader, inputPath);

            if (returningCode != RC.CODE_SUCCESS)
                return returningCode;

            pipelineStart = reader;
            IPipelineStep prevWorker = pipelineStart;

            IWriter writer = setWorker(workersNames.pollFirst(), workersCfg.pollFirst());
            if (writer == null) {
                return this.returningCode;
            }
            returningCode = setOutputStream(writer, outputPath);

            if (returningCode != RC.CODE_SUCCESS)
                return returningCode;

            writer.setProducer(prevWorker);
            prevWorker.setConsumer(writer);

            return returningCode;
        } catch (IllegalAccessException |
                ClassNotFoundException |
                InstantiationException |
                NullPointerException |
                NoSuchMethodException |
                InvocationTargetException e) {
            return RC.CODE_CONFIG_SEMANTIC_ERROR;
        }
    }

    // method  fills ArrayDequeues with workers and their configs from manager config
    private RC fillArrayDequeues(HashMap<String, String> syntaxValidMap, ArrayDeque<String> workersNames, ArrayDeque<String> workersCfg) {
        String readerName = grammar.token(1);
        String readerCfg = grammar.token(4) + grammar.token(1);
        RC workerRC = isVarInDeque(syntaxValidMap, workersNames, readerName);
        RC worker_configRC = isVarInDeque(syntaxValidMap, workersCfg, readerCfg);

        if (workerRC != RC.CODE_SUCCESS)
            return workerRC;

        if (worker_configRC != RC.CODE_SUCCESS)
            return worker_configRC;

        String writerName = grammar.token(3);
        String writerCfg = grammar.token(4) + grammar.token(3);
        workerRC = isVarInDeque(syntaxValidMap, workersNames, writerName);
        worker_configRC = isVarInDeque(syntaxValidMap, workersCfg, writerCfg);

        if (workerRC != RC.CODE_SUCCESS)
            return workerRC;

        return worker_configRC;
    }

    private RC isVarInDeque(HashMap<String, String> syntaxValidMap, ArrayDeque<String> workersNames, String token) {
        String nextWorker = syntaxValidMap.get(token);
        if (nextWorker == null)
            return RC.CODE_FAILED_PIPELINE_CONSTRUCTION;
        workersNames.add(nextWorker);
        return RC.CODE_SUCCESS;
    }

    private RC setInputStream(IReader reader, String inputPath) {
        try {
            inputStream = new FileInputStream(inputPath);
            return reader.setInputStream(inputStream);
        } catch (FileNotFoundException e) {
            inputStream = null;
            logger.severe(MsgInLog.INVALID_INPUT_STREAM.getMsg());
            return RC.CODE_INVALID_INPUT_STREAM;
        }
    }

    private RC setOutputStream(IWriter writer, String outputPath) {
        try {
            outputStream = new FileOutputStream(outputPath);
            return writer.setOutputStream(outputStream);
        } catch (FileNotFoundException ex) {
            outputStream = null;
            logger.severe(MsgInLog.INVALID_OUTPUT_STREAM.getMsg());
            return RC.CODE_INVALID_OUTPUT_STREAM;
        }
    }

    // the mechanism of introspection
    private <T extends IConfigurable> T setWorker(String workerName, String workerCfg)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        logger.info(MsgInLog.TRY_CREATE_WORKER.getMsg() + ": " + workerName);
        Class<?> workerClass = Class.forName(workerName);
        T worker = (T) workerClass.getConstructor(Logger.class).newInstance(logger);
        RC rc = worker.setConfig(workerCfg);

        if (rc != RC.CODE_SUCCESS) {
            returningCode = rc;
            return null;
        }

        logger.info(MsgInLog.SUCCESS.getMsg());
        return worker;
    }

    public RC execute() {
        if (pipelineStart == null) {
            return RC.CODE_FAILED_PIPELINE_CONSTRUCTION;
        }
        RC rc = pipelineStart.execute(null);
        logger.info(rc.toString());
        pipelineStart = null;
        try {
            inputStream.close();
        } catch (IOException e) {
            logger.severe(MsgInLog.INVALID_INPUT_STREAM.getMsg());
            rc = RC.CODE_INVALID_INPUT_STREAM;
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            logger.severe(MsgInLog.INVALID_OUTPUT_STREAM.getMsg());
            rc = RC.CODE_INVALID_OUTPUT_STREAM;
        }
        return rc;
    }
}
