package com.ozge.service.integration;


import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.ozge.configuration.properties.UserServiceProperties;
import com.ozge.model.request.RefundsReportRequest;
import com.ozge.model.response.RefundReportResponse;
import com.ozge.service.ReportService;
import com.ozge.util.TestUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ReportServiceIntegrationTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    private ReportService reportService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserServiceProperties properties;

    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    private final String email = "demo@bumin.com.tr";
    private final String password = "cjaiU8CV";


    @Test
    public void getRefundsReportWithValidRefundsReportRequestAndAuthorizationTokenShouldReturnRefundsReportResponse() throws Exception {
        final String authToken = TestUtils.generateAuthorizationTokenWithValidCredentials(restTemplate, properties.getLogin().getUrl(), email, password);
        final String fromDate = "2014-05-05";
        final String toDate = "2017-12-01";

        RefundsReportRequest refundsReportRequest = new RefundsReportRequest(); 
        
        refundsReportRequest.setFromDate(formatter.parse(fromDate));
        refundsReportRequest.setToDate(formatter.parse(toDate));
        
        Optional<RefundReportResponse> optional = reportService.getRefundsReport(refundsReportRequest, authToken);

        assertTrue("Fault [expected true]", optional.isPresent());
        assertEquals("Fault [expected 'Status' equals]",
                "APPROVED",
                optional.get().getStatus());
    }

    @Test
    public void getRefundsReportWithInvalidAuthorizationTokenShouldThrowUnauthorizedException() throws Exception {
        // GIVEN
        final String authToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJtZXJjaGFudFVzZXJJZCI6NTMsInJvbGUiOiJhZG1pbiIsIm1lcmNoYW50SWQiOjMsInN1Yk1lcmNoYW50SWRzIjpbMyw3NCw5MywxMTkxLDExMSwxMzcsMTM4LDE0MiwxNDUsMTQ2LDE1MywzMzQsMTc1LDE4NCwyMjAsMjIxLDIyMiwyMjMsMjk0LDMyMiwzMjMsMzI3LDMyOSwzMzAsMzQ5LDM5MCwzOTEsNDU1LDQ1Niw0NzksNDg4LDU2MywxMTQ5LDU3MCwxMTM4LDExNTYsMTE1NywxMTU4LDExNzldLCJ0aW1lc3RhbXAiOjE1MDQxMDg3NzN9.Jt5JVXoEEkck4M9fbmDOaykhMpoq-x-D40rY-7Hv_fQ";
        final String expectedExceptionMessage = "401 Unauthorized";

        RefundsReportRequest refundsReportRequest = new RefundsReportRequest(); 
        
        refundsReportRequest.setFromDate(Calendar.getInstance().getTime());
        refundsReportRequest.setToDate(Calendar.getInstance().getTime());
        refundsReportRequest.setMerchant(2);
        refundsReportRequest.setAcquirer(5);

        expectedException.expect(HttpClientErrorException.class);
        expectedException.expectMessage(expectedExceptionMessage);

        try {
            reportService.getRefundsReport(refundsReportRequest, authToken);
            fail("HttpClientErrorException must be thrown");
        } catch (Exception exp) {
            assertThat("Fault [expected 'Exception Message' asserts]",
                    exp.getMessage(),
                    is(expectedExceptionMessage));
            throw exp;
        }
    }

    @Test
    public void getRefundsReportWithInvalidRefundsReportRequestAndValidAuthorizationTokenShouldThrowInternalServerErrorException() throws Exception {
        final String authToken = TestUtils.generateAuthorizationTokenWithValidCredentials(restTemplate, properties.getLogin().getUrl(), email, password);
        final String expectedExceptionMessage = "500 Internal Server Error";
        final String fromDate = "2014123-05-05";
        final String toDate = "2017-12-01";

        RefundsReportRequest refundsReportRequest = new RefundsReportRequest(); 
        
        refundsReportRequest.setFromDate(formatter.parse(fromDate));
        refundsReportRequest.setToDate(formatter.parse(toDate));
        
        expectedException.expect(HttpServerErrorException.class);
        expectedException.expectMessage(expectedExceptionMessage);

        try {
            reportService.getRefundsReport(refundsReportRequest, authToken);
            fail("HttpServerErrorException must be thrown");
        } catch (Exception exp) {
            assertThat("Fault [expected 'Exception Message' asserts]",
                    exp.getMessage(),
                    is(expectedExceptionMessage));
            throw exp;
        }
    }

}
