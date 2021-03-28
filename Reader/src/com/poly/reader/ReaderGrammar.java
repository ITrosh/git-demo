package com.poly.reader;

import ru.spbstu.pipeline.BaseGrammar;

public class ReaderGrammar extends BaseGrammar {

    public ReaderGrammar() {
        super(new String[]{";", "bufSize"});
    }
}
