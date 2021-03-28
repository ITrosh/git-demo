package com.poly.writer;

import ru.spbstu.pipeline.BaseGrammar;

public class WriterGrammar extends BaseGrammar {

    public WriterGrammar() {
        super(new String[]{";", "bufSize"});
    }
}
