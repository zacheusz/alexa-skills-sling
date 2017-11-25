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

import com.amazon.speech.Sdk;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletRequestHandler;
import com.amazon.speech.speechlet.SpeechletRequestHandlerException;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.speechlet.authentication.SpeechletRequestSignatureVerifier;
import com.amazon.speech.speechlet.servlet.ServletSpeechletRequestHandler;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Dictionary;

/**
 *
 * @author zacheusz
 */
@Component(name = "Alexa Speechlet Servlet",
        label = "Alexa Speechlet Servlet",
        description = "Alexa Speechlet Servlet",
        metatype = true, immediate = true)
@Service(Servlet.class)
@Properties({
        @Property(name = "sling.servlet.methods", value = {"POST", "GET"}, propertyPrivate = true),
        @Property(name = "sling.servlet.paths", value = {"/bin/services/alexa"}, propertyPrivate = true),
        @Property(label = "Disable request signature checking.",
                name = Sdk.DISABLE_REQUEST_SIGNATURE_CHECK_SYSTEM_PROPERTY, boolValue = false),
        @Property(label = "Supported skill name.", name = "skill")})
public class AlexaSlingSpeechletServlet extends SlingAllMethodsServlet {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.OPTIONAL_UNARY,
            referenceInterface = SpeechletV2.class,
            policy = ReferencePolicy.DYNAMIC)
    protected volatile SpeechletV2 customSpeechlet;

    @Reference
    protected AlexaSlingSpeechlet slingSpeechlet;

    protected final SpeechletRequestHandler requestHandler = new ServletSpeechletRequestHandler();

    protected boolean disableRequestSignatureCheck;

    @Activate
    public void activate(final ComponentContext componentContext) {
        log.debug("Entering activate method.");
        final Dictionary properties = componentContext.getProperties();
        if (null != properties) {
            this.disableRequestSignatureCheck = PropertiesUtil.toBoolean(
                    properties.get(Sdk.DISABLE_REQUEST_SIGNATURE_CHECK_SYSTEM_PROPERTY), true);
        } else {
            this.disableRequestSignatureCheck = Boolean.parseBoolean(
                    System.getProperty(Sdk.DISABLE_REQUEST_SIGNATURE_CHECK_SYSTEM_PROPERTY));
        }

        log.debug("Exiting activate method.");
    }

    @Override
    protected void doPost(final SlingHttpServletRequest servletRequest, final SlingHttpServletResponse servletResponse)
            throws IOException {

        final byte[] output;
        try {

            final byte[] speechletRequest = IOUtils.toByteArray(servletRequest.getInputStream());
            if (this.disableRequestSignatureCheck) {
                log.warn("Speechlet request signature verification is disabled.");
            } else {
                checkRequestSignature(speechletRequest, servletRequest);
            }
            output = handleSpeechletCall(speechletRequest);

        } catch (SpeechletRequestHandlerException | SecurityException ex) {
            final int status = HttpServletResponse.SC_BAD_REQUEST;
            log.error("Exception occurred during POST request processing. " +
                    "Returning status code {}", status, ex);
            servletResponse.sendError(status, ex.getMessage());
            return;
        } catch (Exception ex) {
            final int status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            log.error("Exception occured during POST request processing. " +
                    "Returning status code {}", status, ex);
            servletResponse.sendError(status, ex.getMessage());
            return;
        }

        servletResponse.setContentType("application/json");
        try (final OutputStream out = servletResponse.getOutputStream()) {
            servletResponse.setContentLength(output.length);
            out.write(output);
        }
    }

    /**
     * Check the signature and certificate provided in the request.
     * @param speechletRequest serialized speechlet request
     * @param httpRequest HTTP Servlet request
     */
    protected final void checkRequestSignature(final byte[] speechletRequest,
                                               final SlingHttpServletRequest httpRequest) {
        SpeechletRequestSignatureVerifier.checkRequestSignature(speechletRequest,
                httpRequest.getHeader(Sdk.SIGNATURE_REQUEST_HEADER),
                httpRequest.getHeader(Sdk.SIGNATURE_CERTIFICATE_CHAIN_URL_REQUEST_HEADER));
    }

    protected byte[] handleSpeechletCall(final byte[] speechletRequest)
            throws SpeechletRequestHandlerException, SpeechletException, IOException {
        return this.requestHandler.handleSpeechletCall(getSpeechlet(), speechletRequest);
    }

    protected final SpeechletV2 getSpeechlet() {
        return this.customSpeechlet == null ? this.slingSpeechlet : this.customSpeechlet;
    }

    @Override
    protected void doGet(final SlingHttpServletRequest servletRequest, final SlingHttpServletResponse servletResponse)
            throws IOException {
        log.info("GET diagnostic mehtod");
        try ( final PrintWriter writer = servletResponse.getWriter()) {
            writer.write(getClass() + " is running\n");
            writer.write("customSpeechlet: " + customSpeechlet + "\n");
            writer.write("slingSpeechlet: " + slingSpeechlet + "\n");
        }
    }
}
