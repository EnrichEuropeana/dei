package pl.psnc.dei.queue.context;

import pl.psnc.dei.model.Record;

public class RecordContext extends Context{
    private Record record;

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }
}
