package org.pentaho.di.sdk.samples.steps.ruby;

import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;

public class RubyStepFactory {

	synchronized public static ScriptingContainer createScriptingContainer(boolean withPersistentLocalVars){
		
		ScriptingContainer c = new ScriptingContainer(LocalContextScope.SINGLETHREAD, (withPersistentLocalVars)?LocalVariableBehavior.PERSISTENT:LocalVariableBehavior.TRANSIENT);
		
		//c.setCompileMode(CompileMode.JIT);

		c.setRunRubyInProcess(false);
		ClassLoader loader = ScriptingContainer.class.getClassLoader();
		c.setClassLoader(loader);
		
		// does it make sense to include more in the class path? 
		
//		List<String> paths = new ArrayList<String>();
//		paths.add(c.getHomeDirectory());
//		paths.add(ScriptingContainer.class.getProtectionDomain().getCodeSource().getLocation().toString());
//		c.setLoadPaths(paths); 
				
		return c;
		
	}
	
}
