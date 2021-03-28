import java.io.*;
import java.util.logging.Logger;

import ru.spbstu.pipeline.IWriter;
import ru.spbstu.pipeline.IExecutable;
import ru.spbstu.pipeline.RC;

public class Writer implements IWriter {

    private final Logger logger;

    FileOutputStream fos;
    IExecutable consumer;
    IExecutable producer;

    WriterGrammar grammar = new WriterGrammar();

    public Writer(Logger logger) {
        this.logger = logger;
    }

    @Override
    public RC setOutputStream(FileOutputStream file) {
        if (file == null) {
            return RC.CODE_INVALID_OUTPUT_STREAM;
        }
        fos = file;
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setConfig(String cfgPath) {
        logger.info(MsgInLog.SUCCESS.msg);
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setConsumer(IExecutable consumer) {
        if (consumer == null)
            return RC.CODE_INVALID_ARGUMENT;
        this.consumer = consumer;
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setProducer(IExecutable producer) {
        if (producer == null) {
            return RC.CODE_INVALID_ARGUMENT;
        }
        this.producer = producer;
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC execute(byte[] inputData){
        if (inputData == null)
            return RC.CODE_SUCCESS;

        try{
            fos.write(inputData);
            return RC.CODE_SUCCESS;
        } catch (IOException ex) {
            logger.severe(MsgInLog.FAILED_TO_WRITE.msg);
        }
        return RC.CODE_FAILED_TO_WRITE;
    }
}
