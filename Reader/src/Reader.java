import java.io.*;
import java.util.HashMap;
import java.util.logging.Logger;

import ru.spbstu.pipeline.IExecutable;
import ru.spbstu.pipeline.IReader;
import ru.spbstu.pipeline.RC;

public class Reader implements IReader {

    final Logger logger;

    private Integer nProcBytes;
    int fileSize;
    int nReadData;
    FileInputStream fis;
    IExecutable consumer;
    IExecutable producer;
    ReaderGrammar grammar = new ReaderGrammar();

    public Reader(Logger logger) {
        this.logger = logger;
    }

    private byte[] readFile() {
        if (fis == null)
            return null;
        try {
            byte[] data = null;
            if (nProcBytes < fileSize - nReadData){
                data = new byte[nProcBytes];
                fis.read(data);
                nReadData += nProcBytes;
            }
            else if (fileSize - nReadData > 0){
                data = new byte[fileSize - nReadData];
                nReadData = fileSize;
                fis.read(data);
            }
            return data;
        } catch (IOException ex) {
            logger.severe(MsgInLog.FAILED_TO_READ.msg);
        }
        return null;
    }

    @Override
    public RC setInputStream(FileInputStream file) {
        if (file == null) {
            return RC.CODE_INVALID_INPUT_STREAM;
        }
        fis = file;
        try {
            fileSize = (int)file.getChannel().size();
        }
        catch(IOException ex) {
            logger.severe(MsgInLog.INVALID_INPUT_STREAM.msg);
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

        HashMap<String,String> syntaxValidMap = SyntaxParser.isValidCfg(args);

        nProcBytes = SemanticParser.getInteger(syntaxValidMap, grammar.token(1));

        if (nProcBytes == null) {
            logger.severe(MsgInLog.INVALID_CONFIG_DATA.msg);
            return RC.CODE_CONFIG_SEMANTIC_ERROR;
        }
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
        this.producer = null;
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC execute(byte[] inputData){
        byte[] data;
        RC rc = RC.CODE_SUCCESS;
        do {
            data = readFile();
            if (data == null)
                break;
            rc = consumer.execute(data);
            if (rc != RC.CODE_SUCCESS)
                break;
            //logger...
        } while (data != null);

        return rc;
    }
}

