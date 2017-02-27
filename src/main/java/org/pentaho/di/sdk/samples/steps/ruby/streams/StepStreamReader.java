package org.pentaho.di.sdk.samples.steps.ruby.streams;

import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.sdk.samples.steps.ruby.RubyStepData;
import org.pentaho.di.sdk.samples.steps.ruby.execmodels.SimpleExecutionModel;
import org.pentaho.di.trans.step.BaseStep;

/**
 * Instances of StepStreamReader are designed to be used from within ruby scripts. 
 * They are used to read from info streams.
 */

public class StepStreamReader {
	
	private RowSet rs;
	private BaseStep step;
	private SimpleExecutionModel model;
	private RubyStepData data;
	
	public StepStreamReader(SimpleExecutionModel model, String srcStepName) throws KettleStepException{
		
		this.model = model;
		this.step = model.getStep();
		this.data = model.getData();

		rs = step.findInputRowSet(srcStepName);
		 
	}
	 
	public IRubyObject read() throws KettleException{
		
		Object r[] = rs.getRow();

		// signal that there's no more rows coming
		if (r == null){
			return data.runtime.getNil();
		}
		
		step.incrementLinesRead();
		IRubyObject rubyRow = model.createRubyInputRow(rs.getRowMeta(), r);
		return rubyRow;
	}

	public IRubyObject read(long upTo) throws KettleException{
		
		// request to read <0 rows
		if (upTo < 0) return data.runtime.getNil();
		
		RubyArray arr = data.runtime.newArray();
		int read = 0;
		while(read < upTo){
			IRubyObject o = read();
			if (o.isNil()) break;
			arr.append(o);
			read++;
		}
		
		// request to read from empty stream
		if (arr.size() == 0 && upTo > 0) return data.runtime.getNil();
		
		return arr;
	
	}
	
	public RubyArray readAll() throws KettleException{
		
		RubyArray arr = data.runtime.newArray();
		
		while(true){
			IRubyObject o = read();
			if (o.isNil()) break;
			arr.append(o);
		}
		
		return arr;
		
	}
	
}
