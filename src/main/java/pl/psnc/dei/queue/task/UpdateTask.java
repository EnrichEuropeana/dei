package pl.psnc.dei.queue.task;

import pl.psnc.dei.model.Record;

public class UpdateTask extends Task {

//	TODO add annotation id as param? or JSON with data?
	public UpdateTask(Record record) {
		super(record);
	}

	@Override
	public void process() throws Exception {
//		TODO implementation
		throw new UnsupportedOperationException("Not implemented yet!");
	}
}
