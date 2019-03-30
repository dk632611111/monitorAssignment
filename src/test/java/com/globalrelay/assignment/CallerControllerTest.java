package com.globalrelay.assignment;

import com.globalrelay.assignment.common.MessageI;
import com.globalrelay.assignment.common.SuccessMessageEnum;
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

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CallerControllerTest {

    private static final MediaType JSON_MEDIA_TYPE = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    private static final String BASE_URL = "/v1/caller";
    private static final String CREATE_URL = BASE_URL+"/create";
    private static final String DELETE_URL = BASE_URL+"/delete";

    private static final String BASE_TCP_SERVICE_URL = "/v1/tcpservice";
    private static final String START_TCP_SERVICE_URL = BASE_TCP_SERVICE_URL+"/start";
    private static final String DELETE_TCP_SERVICE_URL = BASE_TCP_SERVICE_URL+"/delete";

    private static final String CALLER_HOST = "localhost";
    private static final int CALLER_PORT = 39001;

    private static final String HOST = "localhost";
    private static final int HOST_PORT = 29001;

    private static final int POLLING_RATE = 5;
    private static boolean threadRunning;

    private static final String SUCCESS_RUNNING = "For host "+HOST+" port "+HOST_PORT+" the status is Success TCP service is running";
    private static final String FAILURE_RUNNING = "For host "+HOST+" port "+HOST_PORT+" the status is Error: TCP service is NOT running";

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setup() {

        // Clean out out data
        try {
            String url = DELETE_URL;
            MessageI expectedResult = SuccessMessageEnum.DELETE_CALLER;
            performHttpCaller(url, CALLER_HOST, CALLER_PORT, HOST, HOST_PORT, POLLING_RATE,
                    0, 0, 0, expectedResult);
        } catch(Throwable e) {
        }

        try {
            String url = DELETE_TCP_SERVICE_URL;
            MessageI expectedResult = SuccessMessageEnum.DELETE_TCP_SERVICE;
            performTcpService(url, HOST, HOST_PORT, expectedResult);
        } catch(Throwable e) {
        }
    }

    @Test
    public void testCreateCaller() throws Exception {

        String url = CREATE_URL;

        // Assert can create caller
        MessageI expectedResult = SuccessMessageEnum.CREATE_CALLER;
        performHttpCaller(url, CALLER_HOST, CALLER_PORT, HOST, HOST_PORT, POLLING_RATE,
                0, 0, 0, expectedResult);

        String expectedMessage = SUCCESS_RUNNING;

        int timeout = POLLING_RATE*5000;
        assertCallerNotified(CALLER_PORT,expectedMessage,timeout);
    }

    @Test
    public void testDeleteCaller() throws Exception {

        // Assert can create caller
        String url = CREATE_URL;
        MessageI expectedResult = SuccessMessageEnum.CREATE_CALLER;
        performHttpCaller(url, CALLER_HOST, CALLER_PORT, HOST, HOST_PORT, POLLING_RATE,
                0, 0, 0, expectedResult);

        // Assert can delete caller
        url = DELETE_URL;
        expectedResult = SuccessMessageEnum.DELETE_CALLER;
        performHttpCaller(url, CALLER_HOST, CALLER_PORT, HOST, HOST_PORT, POLLING_RATE,
                0, 0, 0, expectedResult);
    }

    @Test
    public void testCreateCallerWithOutage() throws Exception  {

        try {
            String url = CREATE_URL;

            // Assert can create caller with outage
            long currentTimeMilli = System.currentTimeMillis();
            long startOutage = currentTimeMilli;
            long endOutage = currentTimeMilli+(POLLING_RATE*4000);

            MessageI expectedResult = SuccessMessageEnum.CREATE_CALLER;
            performHttpCaller(url, CALLER_HOST, CALLER_PORT, HOST, HOST_PORT, POLLING_RATE,
                    startOutage, endOutage, 0, expectedResult);

            int timeout = POLLING_RATE*2000;
            assertCallerNotified(CALLER_PORT, "",timeout);

            // If no SocketTimeoutException pitched, then fail
            fail();

        } catch (Exception e) {
            assertTrue(e instanceof SocketTimeoutException);
        }
    }

    @Test
    public void testCreateCallerWithNonRunningTcpService() throws Exception {

        String url = CREATE_URL;

        // Assert can create caller
        MessageI expectedResult = SuccessMessageEnum.CREATE_CALLER;
        performHttpCaller(url, CALLER_HOST, CALLER_PORT, HOST, HOST_PORT, POLLING_RATE,
                0, 0, 0, expectedResult);

        String expectedMessage = SUCCESS_RUNNING;

        int timeout = POLLING_RATE*5000;
        assertCallerNotified(CALLER_PORT,expectedMessage,timeout);

        // Stop tcpService
        url = DELETE_TCP_SERVICE_URL;
        expectedResult = SuccessMessageEnum.DELETE_TCP_SERVICE;
        performTcpService(url, HOST, HOST_PORT, expectedResult);

        // Assert will get non-running error
        expectedMessage = FAILURE_RUNNING;
        assertCallerNotified(CALLER_PORT,expectedMessage,timeout);
    }

    @Test
    public void testCreateCallerWithNonRunningTcpServiceButwithGracePeriod() throws Exception {

        String url = CREATE_URL;

        // Assert can create caller
        MessageI expectedResult = SuccessMessageEnum.CREATE_CALLER;
        int gracePeriodSec = POLLING_RATE*2;
        performHttpCaller(url, CALLER_HOST, CALLER_PORT, HOST, HOST_PORT, POLLING_RATE,
                0, 0, gracePeriodSec, expectedResult);

        String expectedMessage = SUCCESS_RUNNING;

        int timeout = POLLING_RATE*5000;
        assertCallerNotified(CALLER_PORT,expectedMessage,timeout);

        // Stop tcpService
        url = DELETE_TCP_SERVICE_URL;
        expectedResult = SuccessMessageEnum.DELETE_TCP_SERVICE;
        performTcpService(url, HOST, HOST_PORT, expectedResult);

        // Assert will eventually get running message
        threadRunning = true;
        new Thread()
        {
            public void run() {
                try {
                    String expectedMessage = SUCCESS_RUNNING;
                    int timeout = POLLING_RATE*5000;
                    assertCallerNotified(CALLER_PORT, expectedMessage, timeout);
                    threadRunning = false;
                } catch (Exception e) {
                    fail();
                }
            }
        }.start();

        // Recreate tcpService
        url = START_TCP_SERVICE_URL;
        expectedResult = SuccessMessageEnum.START_TCP_SERVICE;
        performTcpService(url, HOST, HOST_PORT, expectedResult);

        // Wait to see if success ever found in running thread
        int numChecks = 0;
        while(threadRunning) {
            Thread.sleep(1000);

            if (numChecks++ > 25) {
                fail();
            }
        }
    }

    private void performHttpCaller(String url,
                                   String callerHost,
                                   int callerPort,
                                   String host,
                                   int hostPort,
                                   int pollingRateSec,
                                   long startOutage,
                                   long endOutage,
                                   int gracePeriodSec,
                                   MessageI expectedResult) throws Exception {

        MvcResult result = this.mockMvc.perform(get(url)
                .param("callerHost", callerHost)
                .param("callerPort", Integer.toString(callerPort))
                .param("host", host)
                .param("hostPort", Integer.toString(hostPort))
                .param("pollingRateSec", Integer.toString(pollingRateSec))
                .param("startOutage", Long.toString(startOutage))
                .param("endOutage", Long.toString(endOutage))
                .param("gracePeriodSec", Integer.toString(gracePeriodSec)))
                .andReturn();

        assertEquals(expectedResult.getHttpStatus().value(),result.getResponse().getStatus());
        assertEquals(expectedResult.getMessage(),result.getResponse().getContentAsString());
    }

    private void performTcpService(String url, String host, int port, MessageI expectedResult) throws Exception {
        MvcResult result = this.mockMvc.perform(get(url)
                .param("host", host)
                .param("port", Integer.toString(port)))
                .andReturn();

        assertEquals(expectedResult.getHttpStatus().value(),result.getResponse().getStatus());
        assertEquals(expectedResult.getMessage(),result.getResponse().getContentAsString());
    }

    private void assertCallerNotified(int callerPort, String expectedMessage, int timeout) throws Exception {

        ServerSocket serverSocket = null;
        Socket socket = null;
        ObjectInputStream objectInputStream = null;


        try {
            serverSocket = new ServerSocket(callerPort);
            serverSocket.setSoTimeout(timeout);

            socket = serverSocket.accept();
            objectInputStream = new ObjectInputStream(socket.getInputStream());

            String message = (String) objectInputStream.readObject();
            assertEquals(expectedMessage,message);

        } finally {
            try {
                serverSocket.close();
                socket.close();
                objectInputStream.close();
            } catch (Exception ioe) {
            }
        }
    }

}
