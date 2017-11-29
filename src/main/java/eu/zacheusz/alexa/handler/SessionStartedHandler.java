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
import com.amazon.speech.speechlet.SessionStartedRequest;

/**
 * Sling service which expose this interface will be notified that a new session
 * started as a result of a user interacting with the device.
 *
 * @author zacheusz
 */
public interface SessionStartedHandler  {

    /**
     * Used to notify that a new session started as a result of a user interacting with the device.
     * This method enables services to perform initialization logic and allows for session
     * attributes to be stored for subsequent requests.
     *
     * @param requestEnvelope
     *            the session started request envelope
     */
    void handleSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope);
}
