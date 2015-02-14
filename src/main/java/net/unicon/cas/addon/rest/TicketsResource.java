package net.unicon.cas.addon.rest;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.ticket.InvalidTicketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Formatter;

/**
 * {@link org.springframework.stereotype.Controller} implementation of CAS' REST API.
 *
 * This class implements main CAS RESTful resource for vending/deleting TGTs and vending STs:
 *
 * <ul>
 * <li>{@code POST /v1/tickets}</li>
 * <li>{@code POST /v1/tickets/{TGT-id}}</li>
 * <li>{@code DELETE /v1/tickets/{TGT-id}}</li>
 * </ul>
 *
 * @author Dmitriy Kopylenko
 * @since 1.0.0
 */
@Controller("/v1")
public class TicketsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketsResource.class);

    @Autowired
    private CentralAuthenticationService cas;

    /**
     * Create new ticket granting ticket.
     *
     * @param requestBody username and password application/x-www-form-urlencoded values
     * @param request raw HttpServletRequest used to call this method
     *
     * @return ResponseEntity representing RESTful response
     */
    @RequestMapping(value = "/tickets", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public final ResponseEntity<String> createTicketGrantingTicket(@RequestBody final MultiValueMap<String, String> requestBody,
                                                                   final HttpServletRequest request) {
        try (Formatter fmt = new Formatter()) {
            final String tgtId = this.cas.createTicketGrantingTicket(obtainCredentials(requestBody));
            final URI ticketReference = new URI(request.getRequestURL().toString() + "/" + tgtId);
            final HttpHeaders headers = new HttpHeaders();
            headers.setLocation(ticketReference);
            headers.setContentType(MediaType.TEXT_HTML);
            fmt.format("<!DOCTYPE HTML PUBLIC \\\"-//IETF//DTD HTML 2.0//EN\\\"><html><head><title>");
            //IETF//DTD HTML 2.0//EN\\\"><html><head><title>");
            fmt.format("%s %s", HttpStatus.CREATED, HttpStatus.CREATED.getReasonPhrase())
                    .format("</title></head><body><h1>TGT Created</h1><form action=\"%s", ticketReference.toString())
                    .format("\" method=\"POST\">Service:<input type=\"text\" name=\"service\" value=\"\">")
                    .format("<br><input type=\"submit\" value=\"Submit\"></form></body></html>");
            return new ResponseEntity<String>(fmt.toString(), headers, HttpStatus.CREATED);
        }
        catch (final Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Create new service ticket.
     *
     * @param requestBody service application/x-www-form-urlencoded value
     * @param tgtId ticket granting ticket id URI path param
     *
     * @return {@link org.springframework.http.ResponseEntity} representing RESTful response
     */
    @RequestMapping(value = "/tickets/{tgtId:.+}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public final ResponseEntity<String> createServiceTicket(@RequestBody final MultiValueMap<String, String> requestBody,
                                                            @PathVariable("tgtId") final String tgtId) {
        try {
            final String serviceTicketId = this.cas.grantServiceTicket(tgtId,
                    new SimpleWebApplicationServiceImpl(requestBody.getFirst("service")));
            return new ResponseEntity<String>(serviceTicketId, HttpStatus.OK);
        }
        catch (final InvalidTicketException e) {
            return new ResponseEntity<String>("TicketGrantingTicket could not be found", HttpStatus.NOT_FOUND);
        }
        catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Destroy ticket granting ticket.
     *
     * @param tgtId ticket granting ticket id URI path param
     *
     * @return {@link org.springframework.http.ResponseEntity} representing RESTful response. Signals
     * {@link org.springframework.http.HttpStatus#OK} when successful.
     */
    @RequestMapping(value = "/tickets/{tgtId:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public final ResponseEntity<String> deleteTicketGrantingTicket(@PathVariable("tgtId") final String tgtId) {
        this.cas.destroyTicketGrantingTicket(tgtId);
        return new ResponseEntity<String>(tgtId, HttpStatus.OK);
    }

    /**
     * Obtain credentials from the request. Could be overridden by subclasses.
     *
     * @param requestBody raw entity request body
     *
     * @return the credentials instance
     */
    protected Credentials obtainCredentials(final MultiValueMap<String, String> requestBody) {
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials();
        credentials.setUsername(requestBody.getFirst("username"));
        credentials.setPassword(requestBody.getFirst("password"));
        return credentials;
    }
}
