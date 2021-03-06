/*
  Copyright (c) 2017 Zacheusz Siedlecki.

  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
 */
package eu.zacheusz.alexa;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.zacheusz.alexa.handler.IntentHandler;

import eu.zacheusz.alexa.handler.LaunchHandler;
import eu.zacheusz.alexa.handler.SessionEndedHandler;
import eu.zacheusz.alexa.handler.SessionStartedHandler;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zacheusz
 */
@Component(
        label = "Alexa Speechlet OSGi Service",
        description = "Alexa Speechlet OSGi Service",
        metatype = true,
        configurationFactory = true)
@Service(AlexaSlingSpeechlet.class)
public class AlexaSlingSpeechlet implements SpeechletV2 {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Property(label = "Supported skill name.") //TODO
    private static final String SKILL_PROPERTY = "skill";

    @Property(label = "Default onLaunch message.", value = "")
    private static final String ON_LAUNCH_MESSAGE_PROPERTY = "onLaunchMessage";

    private String onLaunchMessage = ""; //TODO documentation

    @Property(label = "Speech response when there is no handler.",
            value = "I'm sorry - there is no implementation for this request.")
    private static final String NO_HANDLER_MESSAGE_PROPERTY = "noHandlerMessage";

    private String noHandlerMessage = ""; //TODO documentation

    @Reference(cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
            bind = "bindHanlder", unbind = "unbindHanlder",
            referenceInterface = IntentHandler.class,
            policy = ReferencePolicy.DYNAMIC)
    protected final List<IntentHandler> handlers = new ArrayList<>();

    @Reference(cardinality = ReferenceCardinality.OPTIONAL_UNARY,
            referenceInterface = SessionStartedHandler.class,
            policy = ReferencePolicy.DYNAMIC)
    protected volatile SessionStartedHandler sessionStartedHandler;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL_UNARY,
            referenceInterface = SessionEndedHandler.class,
            policy = ReferencePolicy.DYNAMIC)
    protected volatile SessionEndedHandler sessionEndedHandler;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL_UNARY,
            referenceInterface = LaunchHandler.class,
            policy = ReferencePolicy.DYNAMIC)
    protected volatile LaunchHandler launchHandler;

    @Activate
    protected final void activate(final Map<String, Object> properties) throws Exception {
        this.onLaunchMessage = PropertiesUtil.toString(properties.get(ON_LAUNCH_MESSAGE_PROPERTY), "");
        this.noHandlerMessage = PropertiesUtil.toString(properties.get(NO_HANDLER_MESSAGE_PROPERTY), "");
    }

    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
        log.info("onSessionStarted"); //TODO improve log message and level
        if (this.sessionStartedHandler != null) {
            this.sessionStartedHandler.handleSessionStarted(requestEnvelope);
        } else {
            log.info("no sessionStartedHandler");
        }
    }

    //TODO documentation
    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
        log.info("onLaunch"); //TODO improve log message and level
        final SpeechletResponse response;
        if (this.launchHandler != null) {
            response = this.launchHandler.handleLaunch(requestEnvelope);
        } else {
            response = newDefaultOnLaunchMessage();
        }
        return response;
    }

    protected SpeechletResponse newDefaultOnLaunchMessage() {
        return newTellResponse(this.onLaunchMessage);
    }

    protected SpeechletResponse newTellResponse(final String text) {
        final PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(text);
        return SpeechletResponse.newTellResponse(speech);
    }

    @Override
    public SpeechletResponse onIntent(final SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        final IntentRequest request = requestEnvelope.getRequest();
        final String intentName = request.getIntent().getName();
        log.info("processing intent request {}", intentName);
        //filter handlers
        final Supplier<Stream<IntentHandler>> filtered =
                () -> this.handlers.stream().filter(h -> h.supportsIntent(intentName));
        if (filtered.get().count() > 1) {
            log.warn("Multiple handlers supports {} intent.", intentName);
        }
        return filtered.get().findFirst().orElse(this.defaultIntentHandler).handleIntent(requestEnvelope);
    }

    protected final IntentHandler defaultIntentHandler = new IntentHandler() {
        @Override
        public boolean supportsIntent(String intentName) {
            return true;
        }

        @Override
        public SpeechletResponse handleIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
            return newTellResponse(AlexaSlingSpeechlet.this.noHandlerMessage);
        }
    };

    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
        log.info("onSessionEnded"); //TODO improve log message and level
        if (this.sessionEndedHandler != null) {
            this.sessionEndedHandler.handleSessionEnded(requestEnvelope);
        } else {
            log.info("no sessionEndedHandler");
        }
    }

    protected void bindHanlder(final IntentHandler handler) {
        this.handlers.add(handler);
    }

    protected void unbindHanlder(final IntentHandler handler) {
        this.handlers.remove(handler);
    }

}
