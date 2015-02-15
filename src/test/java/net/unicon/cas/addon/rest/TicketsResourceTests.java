/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.unicon.cas.addon.rest;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.TicketException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link net.unicon.cas.addon.rest.TicketsResource}.
 *
 * @author Dmitriy Kopylenko
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TicketsResourceTests {

    @Mock
    private CentralAuthenticationService casMock;

    @InjectMocks
    private TicketsResource ticketsResourceUnderTest;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(this.ticketsResourceUnderTest)
                .defaultRequest(get("/")
                        .contextPath("/cas")
                        .servletPath("/v1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .build();
    }

    @Test
    public void normalCreationOfTGT() throws Throwable {
        final String expectedReturnEntityBody = "<!DOCTYPE HTML PUBLIC \\\"-//IETF//DTD HTML 2.0//EN\\\">"
                + "<html><head><title>201 Created</title></head><body><h1>TGT Created</h1>"
                + "<form action=\"http://localhost/cas/v1/tickets/TGT-1\" "
                + "method=\"POST\">Service:<input type=\"text\" name=\"service\" value=\"\">"
                + "<br><input type=\"submit\" value=\"Submit\"></form></body></html>";

        configureCasMockToCreateValidTGT();

        this.mockMvc.perform(post("/cas/v1/tickets")
                .param("username", "test")
                .param("password", "test"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/cas/v1/tickets/TGT-1"))
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andExpect(content().string(expectedReturnEntityBody));
    }

    @Test
    public void creationOfTGTWithAuthenticationException() throws Throwable {
        configureCasMockTGTCreationToThrowAuthenticationException();

        this.mockMvc.perform(post("/cas/v1/tickets")
                .param("username", "test")
                .param("password", "test"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void normalCreationOfST() throws Throwable {
        configureCasMockToCreateValidST();

        this.mockMvc.perform(post("/cas/v1/tickets/TGT-1")
                .param("service", "https://www.google.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=ISO-8859-1"))
                .andExpect(content().string("ST-1"));
    }

    @Test
    public void creationOfSTWithInvalidTicketException() throws Throwable {
        configureCasMockSTCreationToThrow(new InvalidTicketException());

        this.mockMvc.perform(post("/cas/v1/tickets/TGT-1")
                .param("service", "https://www.google.com"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("TicketGrantingTicket could not be found"));
    }

    @Test
    public void creationOfSTWithGeneralException() throws Throwable {
        configureCasMockSTCreationToThrow(new RuntimeException("Other exception"));

        this.mockMvc.perform(post("/cas/v1/tickets/TGT-1")
                .param("service", "https://www.google.com"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Other exception"));
    }

    @Test
    public void deletionOfTGT() throws Throwable {
        this.mockMvc.perform(delete("/cas/v1/tickets/TGT-1"))
                .andExpect(status().isOk());
    }

    private void configureCasMockToCreateValidTGT() throws Throwable {
        when(this.casMock.createTicketGrantingTicket(any(Credentials.class))).thenReturn("TGT-1");
    }

    private void configureCasMockTGTCreationToThrowAuthenticationException() throws Throwable {
        when(this.casMock.createTicketGrantingTicket(any(Credentials.class))).thenThrow(new TicketException("err") {
        });
    }

    private void configureCasMockSTCreationToThrow(final Throwable e) throws Throwable {
        when(this.casMock.grantServiceTicket(anyString(), any(Service.class))).thenThrow(e);
    }

    private void configureCasMockToCreateValidST() throws Throwable {
        when(this.casMock.grantServiceTicket(anyString(), any(Service.class))).thenReturn("ST-1");
    }
}
