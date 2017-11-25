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
import java.util.List;

import eu.zacheusz.alexa.handler.IntentHandler;

import eu.zacheusz.alexa.handler.LaunchHandler;
import eu.zacheusz.alexa.handler.SessionEndedHandler;
import eu.zacheusz.alexa.handler.SessionStartedHandler;
import org.apache.felix.scr.annotations.*;
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
@Properties({@Property(label = "Supported skill name.", name = "skill")}) //TODO
public class AlexaSlingSpeechlet implements SpeechletV2 {

    protected final Logger log = LoggerFactory.getLogger(getClass());

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

    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
        log.info("onSessionStarted"); //TODO improve log message and level
        if (this.sessionStartedHandler != null) {
            this.sessionStartedHandler.handleSessionStarted(requestEnvelope);
        } else {
            log.info("no sessionStartedHandler");
        }
    }

    //TODO implement handler
    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
        log.info("onLaunch"); //TODO improve log message and level
        if (this.launchHandler != null) {
            return this.launchHandler.handleLaunch(requestEnvelope);
        }
        final PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(""); //TODO?
        SpeechletResponse response = SpeechletResponse.newTellResponse(speech);
        return response;
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        final IntentRequest request = requestEnvelope.getRequest();
        final Session session = requestEnvelope.getSession();
        log.info("processing intent request " + request.getIntent().getName());
        final String intentName = request.getIntent().getName();
        for (final IntentHandler handler : this.handlers) {
            if (handler.supportsIntent(intentName)) {
                return handler.handleIntent(requestEnvelope);//TODO warn if there are more handlers than one
            }
        }
        final PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("I'm sorry - I can't handle this request."); //TODO exception?
        final SpeechletResponse response = SpeechletResponse.newTellResponse(speech);
        return response;
    }

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
