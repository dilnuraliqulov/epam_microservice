package com.example.workload.component.h2;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;


@Suite
@IncludeEngines("cucumber")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.example.workload.component.h2")
@ConfigurationParameter(key = FEATURES_PROPERTY_NAME, value = "classpath:features/component")
public class H2ComponentSuiteTest  {}