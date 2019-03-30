package com.monitor.assignment;

import com.monitor.assignment.common.ErrorMessageEnum;
import com.monitor.assignment.common.MessageI;
import com.monitor.assignment.common.SuccessMessageEnum;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TcpServiceControllerTest {

    private static final MediaType JSON_MEDIA_TYPE = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    private static final String BASE_URL = "/v1/tcpservice";
    private static final String START_URL = BASE_URL+"/start";

    private static final String STOP_URL = BASE_URL+"/stop";
    private static final String RUNNING_URL = BASE_URL+"/running";
    private static final String DELETE_URL = BASE_URL+"/delete";

    private static final String HOST = "localhost";
    private static final int PORT = 29001;

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setup() {

        // Clean out out data
        try {
            String url = DELETE_URL;
            MessageI expectedResult = SuccessMessageEnum.DELETE_TCP_SERVICE;
            performTcpService(url, HOST, PORT, expectedResult);
        } catch(Throwable e) {
        }
    }

    @Test
    public void testStartService() throws Exception {

        String url = START_URL;

        // Assert can create service
        MessageI expectedResult = SuccessMessageEnum.START_TCP_SERVICE;
        performTcpService(url, HOST, PORT, expectedResult);

        // Assert can NOT create duplicate service
        expectedResult = ErrorMessageEnum.UNABLE_TO_START_TCP_SERVICE;
        performTcpService(url, HOST, PORT, expectedResult);
    }

    @Test
    public void testStopService() throws Exception {

        // Assert can't stop service because never started
        String url = STOP_URL;
        MessageI expectedResult = ErrorMessageEnum.UNABLE_TO_STOP_TCP_SERVICE;
        performTcpService(url, HOST, PORT, expectedResult);

        // Assert can create service
        url = START_URL;
        expectedResult = SuccessMessageEnum.START_TCP_SERVICE;
        performTcpService(url, HOST, PORT, expectedResult);

        // Assert can stop service
        url = STOP_URL;
        expectedResult = SuccessMessageEnum.STOP_TCP_SERVICE;
        performTcpService(url, HOST, PORT, expectedResult);
    }

    @Test
    public void testRunningService() throws Exception {

        // Assert service not running because never started
        String url = RUNNING_URL;
        MessageI expectedResult = ErrorMessageEnum.NOT_RUNNING_TCP_SERVICE;
        performTcpService(url, HOST, PORT, expectedResult);

        // Assert can create service
        url = START_URL;
        expectedResult = SuccessMessageEnum.START_TCP_SERVICE;
        performTcpService(url, HOST, PORT, expectedResult);

        // Assert service now running
        url = RUNNING_URL;
        expectedResult = SuccessMessageEnum.RUNNING_TCP_SERVICE;
        performTcpService(url, HOST, PORT, expectedResult);
    }


    @Test
    public void testDeletingService() throws Exception {

        // Assert not able to delete service because never started
        String url = DELETE_URL;
        MessageI expectedResult = ErrorMessageEnum.UNABLE_TO_DELETE_TCP_SERVICE;
        performTcpService(url, HOST, PORT, expectedResult);

        // Assert can create service
        url = START_URL;
        expectedResult = SuccessMessageEnum.START_TCP_SERVICE;
        performTcpService(url, HOST, PORT, expectedResult);

        // Assert service now can delete
        url = DELETE_URL;
        expectedResult = SuccessMessageEnum.DELETE_TCP_SERVICE;
        performTcpService(url, HOST, PORT, expectedResult);
    }

    private void performTcpService(String url, String host, int port, MessageI expectedResult) throws Exception {
        MvcResult result = this.mockMvc.perform(get(url)
                .param("host", host)
                .param("port", Integer.toString(port)))
                .andReturn();

        assertEquals(expectedResult.getHttpStatus().value(),result.getResponse().getStatus());
        assertEquals(expectedResult.getMessage(),result.getResponse().getContentAsString());
    }
}
