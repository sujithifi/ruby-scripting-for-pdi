package org.pentaho.di.sdk.samples.steps.ruby.execmodels;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.sdk.samples.steps.ruby.RubyStep;
import org.pentaho.di.sdk.samples.steps.ruby.RubyStepData;
import org.pentaho.di.sdk.samples.steps.ruby.RubyStepMeta;

public interface ExecutionModel {
	
	public void setEnvironment(RubyStep step, RubyStepData data, RubyStepMeta meta);
	
	public boolean onInit();
	public void onDispose();
	public boolean onProcessRow() throws KettleException;
	public void onStopRunning() throws KettleException;

}
