package uk.co.terminological.rjava;

import java.io.File;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

public class LogController {

	static void changeLogLevel(String logLevel) {
		Configurator
			.setAllLevels(LogManager.getRootLogger().getName(), 
				Level.toLevel(logLevel, Level.INFO));
	}
	
	static void reconfigureLog(String filename) {
		LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
		File file = new File(filename);
		// this will force a reconfiguration
		context.setConfigLocation(file.toURI());
	}
		
	static void configureLog(String logLevel) {
		ConfigurationBuilder<BuiltConfiguration> builder = 
				ConfigurationBuilderFactory.newConfigurationBuilder();

		builder
			.setStatusLevel(
				Level.toLevel(logLevel, Level.INFO));
		// naming the logger configuration
		builder
			.setConfigurationName("DefaultLogger");

		// create a console appender
		AppenderComponentBuilder appenderBuilder = builder
			.newAppender("Console", "CONSOLE")
		    .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
		// add a layout like pattern, json etc
		appenderBuilder
			.add(builder.newLayout("PatternLayout")
					.addAttribute("pattern", "%d %p %c [%t] %m%n"));
		RootLoggerComponentBuilder rootLogger = 
				builder.newRootLogger(Level.DEBUG);
		rootLogger.add(builder.newAppenderRef("Console"));

		builder.add(appenderBuilder);
		builder.add(rootLogger);
		Configurator.initialize(builder.build());
	}
}
