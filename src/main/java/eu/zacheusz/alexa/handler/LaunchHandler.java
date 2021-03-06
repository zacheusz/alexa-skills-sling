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
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SpeechletResponse;

/**
 * Sling Service which expose this interface will handle a speech initiated request
 * to start the skill without providing an {@code Intent}.
 *
 * @author zacheusz
 */
public interface LaunchHandler {


    /**
     * Entry point for handling a speech initiated request to start the skill
     * without providing an {@code Intent}.<br>
     *
     *
     * @param requestEnvelope
     *            the launch request envelope
     * @return the response, spoken and visual, to the request
     */
    SpeechletResponse handleLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope);
}

