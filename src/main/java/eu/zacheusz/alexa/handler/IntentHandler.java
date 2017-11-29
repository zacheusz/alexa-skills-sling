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
package eu.zacheusz.alexa.handler;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.SpeechletResponse;

/**
 * Handling speech initiated requests.<br>
 *
 * This is where the logic lives. Intent requests are handled by this class.
 *
 *
 * @author zacheusz
 */
public interface IntentHandler {

    /**
     * Check if the handler has logic implementation for the intent with the given name.
     *
     * @param intentName the name of the intent
     * @return <code>true</code> if the handler supports the intent
     * with a given name and <code>false</code> if it doesn't support it
     */
    boolean supportsIntent(String intentName);

    /**
     * Handling speech initiated requests.<br>
     *
     * This is where the logic lives. Intent requests are handled by this method
     * and return responses to render to the user.<br>
     *
     * If this is the initial request of a new session, {@link Session#isNew()}
     * returns {@code true}. Otherwise, this is a subsequent request within an existing session.
     *
     * @param requestEnvelope
     *            the intent request envelope to handle
     * @return the response, spoken and visual, to the request
     */
    SpeechletResponse handleIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope);
}

