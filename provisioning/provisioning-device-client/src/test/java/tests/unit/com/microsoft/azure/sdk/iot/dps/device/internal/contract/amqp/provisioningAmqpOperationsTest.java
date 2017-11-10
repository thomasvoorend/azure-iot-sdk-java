/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.dps.device.internal.contract.amqp;

import com.microsoft.azure.sdk.iot.deps.transport.amqp.AmqpListener;
import com.microsoft.azure.sdk.iot.deps.transport.amqp.AmqpMessage;
import com.microsoft.azure.sdk.iot.deps.transport.amqp.AmqpsConnection;
import com.microsoft.azure.sdk.iot.deps.util.ObjectLock;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ResponseCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.amqp.provisioningAmqpOperations;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceConnectionException;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.qpid.proton.amqp.Binary;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.qpid.proton.amqp.messaging.Data;

import javax.net.ssl.SSLContext;

import static org.junit.Assert.assertEquals;

/*
 * Unit tests for ContractAPIHttp
 * Code coverage : 100% methods, 100% lines
 */
@RunWith(JMockit.class)
public class provisioningAmqpOperationsTest
{
    private static final String TEST_SCOPE_ID = "testScopeID";
    private static final String TEST_HOST_NAME = "testHostName";
    private static final String TEST_REGISTRATION_ID = "testRegistrationId";
    private static final String TEST_OPERATION_ID = "testOperationId";

    @Mocked
    private AmqpsConnection mockedAmqpConnection;

    @Mocked
    private SSLContext mockedSSLContext;

    @Mocked
    private ResponseCallback mockedResponseCallback;

    @Mocked
    private AmqpMessage mockedAmqpMessage;

    @Mocked
    private LinkedBlockingQueue<AmqpMessage> mockedQueueMessage = new LinkedBlockingQueue<>();

    @Mocked
    private byte[] mockedData;

    @Mocked
    private Binary mockedBinaryData;

    @Mocked
    private ObjectLock mockedObjectLock = new ObjectLock();


    private void setupSendReceiveMocks() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        new NonStrictExpectations()
        {
            {
                new AmqpMessage();
                result = mockedAmqpMessage;

                mockedAmqpConnection.sendAmqpMessage(mockedAmqpMessage);

                mockedObjectLock.waitLock(anyLong);

                mockedQueueMessage.size();
                result = 1;

                mockedQueueMessage.remove();
                result = mockedAmqpMessage;

                mockedAmqpMessage.getAmqpBody();
                result = mockedData;
            }
        };
    }

    @Test
    public void constructorSucceeds() throws ProvisioningDeviceClientException
    {
        //arrange

        //act
        provisioningAmqpOperations provisioningAmqpOperations = new provisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        //assert
        assertEquals(TEST_SCOPE_ID, Deencapsulation.getField(provisioningAmqpOperations, "scopeId"));
        assertEquals(TEST_HOST_NAME, Deencapsulation.getField(provisioningAmqpOperations, "hostName"));
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnNullScopeId() throws ProvisioningDeviceClientException
    {
        //arrange

        //act
        provisioningAmqpOperations contractAPIAmqp = new provisioningAmqpOperations(null, TEST_HOST_NAME);

        //assert
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnNullHostName() throws ProvisioningDeviceClientException
    {
        //arrange

        //act
        provisioningAmqpOperations contractAPIAmqp = new provisioningAmqpOperations(TEST_SCOPE_ID, null);

        //assert
    }

    @Test
    public void isAmqpConnectedNotConnectedSucceeds() throws ProvisioningDeviceClientException
    {
        //arrange
        provisioningAmqpOperations provisioningAmqpOperations = new provisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        boolean isConnected = provisioningAmqpOperations.isAmqpConnected();

        //assert
        assertEquals(false, isConnected);
    }

    @Test
    public void isAmqpConnectedSucceeds() throws ProvisioningDeviceClientException, IOException
    {
        //arrange
        provisioningAmqpOperations provisioningAmqpOperations = new provisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);
        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.setListener((AmqpListener)any);
                mockedAmqpConnection.open();
            }
        };

        provisioningAmqpOperations.open(TEST_REGISTRATION_ID, mockedSSLContext, true);
        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.isConnected();
                result = true;
            }
        };

        //act
        boolean isConnected = provisioningAmqpOperations.isAmqpConnected();

        //assert
        assertEquals(true, isConnected);
    }

    @Test (expected = ProvisioningDeviceConnectionException.class)
    public void openThrowsOnNullRegistrationId() throws ProvisioningDeviceClientException
    {
        //arrange
        provisioningAmqpOperations provisioningAmqpOperations = new provisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        provisioningAmqpOperations.open(null, mockedSSLContext, true);

        //assert
    }

    @Test (expected = ProvisioningDeviceConnectionException.class)
    public void openThrowsOnEmptyRegistrationId() throws ProvisioningDeviceClientException
    {
        //arrange
        provisioningAmqpOperations provisioningAmqpOperations = new provisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        provisioningAmqpOperations.open("", mockedSSLContext, true);

        //assert
    }

    @Test (expected = ProvisioningDeviceConnectionException.class)
    public void openThrowsOnNullSSLContext() throws ProvisioningDeviceClientException
    {
        //arrange
        provisioningAmqpOperations provisioningAmqpOperations = new provisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        provisioningAmqpOperations.open(TEST_REGISTRATION_ID, null, true);

        //assert
    }

    @Test (expected = ProvisioningDeviceConnectionException.class)
    public void openThrowsOnOpenFailure() throws ProvisioningDeviceClientException, IOException
    {
        //arrange
        provisioningAmqpOperations provisioningAmqpOperations = new provisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.setListener((AmqpListener)any);
                mockedAmqpConnection.open();
                result = new Exception();
            }
        };

        //act
        provisioningAmqpOperations.open(TEST_REGISTRATION_ID, mockedSSLContext, true);
    }

    @Test
    public void openSucceeds() throws ProvisioningDeviceClientException, IOException
    {
        //arrange
        provisioningAmqpOperations provisioningAmqpOperations = new provisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.setListener((AmqpListener)any);
                mockedAmqpConnection.open();
                //result = new Exception();
            }
        };

        //act
        provisioningAmqpOperations.open(TEST_REGISTRATION_ID, mockedSSLContext, true);

        //assert
        new Verifications()
        {
            {
                mockedAmqpConnection.open();
                times = 1;
            }
        };
    }

    @Test (expected = IOException.class)
    public void closeThrowsIoException() throws ProvisioningDeviceClientException, IOException
    {
        //arrange
        provisioningAmqpOperations provisioningAmqpOperations = new provisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);
        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.setListener((AmqpListener)any);
                mockedAmqpConnection.open();
            }
        };
        provisioningAmqpOperations.open(TEST_REGISTRATION_ID, mockedSSLContext, true);

        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.setListener((AmqpListener)any);
                mockedAmqpConnection.close();
                result = new IOException();
            }
        };

        //act
        provisioningAmqpOperations.close();

        //assert
    }

    @Test
    public void closeSucceeds() throws ProvisioningDeviceClientException, IOException
    {
        //arrange
        provisioningAmqpOperations provisioningAmqpOperations = new provisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);
        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.setListener((AmqpListener)any);
                mockedAmqpConnection.open();
            }
        };
        provisioningAmqpOperations.open(TEST_REGISTRATION_ID, mockedSSLContext, true);

        //act
        provisioningAmqpOperations.close();

        //assert
        new Verifications()
        {
            {
                mockedAmqpConnection.close();
                times = 1;
            }
        };
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void sendStatusThrowsOnOperationIdNull() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        provisioningAmqpOperations provisioningAmqpOperations = new provisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        provisioningAmqpOperations.sendStatusMessage(null, mockedResponseCallback, null);

        //assert
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void sendStatusThrowsOnOperationIdEmpty() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        provisioningAmqpOperations provisioningAmqpOperations = new provisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        provisioningAmqpOperations.sendStatusMessage("", mockedResponseCallback, null);

        //assert
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void sendStatusThrowsOnResponseCallbackNull() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        provisioningAmqpOperations provisioningAmqpOperations = new provisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        provisioningAmqpOperations.sendStatusMessage(TEST_OPERATION_ID, null, null);

        //assert
    }

    @Test
    public void sendStatusMessageSucceeds() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        provisioningAmqpOperations provisioningAmqpOperations = new provisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);
        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.setListener((AmqpListener)any);
                mockedAmqpConnection.open();
            }
        };
        provisioningAmqpOperations.open(TEST_REGISTRATION_ID, mockedSSLContext, true);

        setupSendReceiveMocks();

        //act
        provisioningAmqpOperations.sendStatusMessage(TEST_OPERATION_ID, mockedResponseCallback, null);

        //assert
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void sendStatusMessageThrowsOnWaitLock() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        provisioningAmqpOperations provisioningAmqpOperations = new provisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);
        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.setListener((AmqpListener)any);
                mockedAmqpConnection.open();
            }
        };
        provisioningAmqpOperations.open(TEST_REGISTRATION_ID, mockedSSLContext, true);

        new NonStrictExpectations()
        {
            {
                new AmqpMessage();
                result = mockedAmqpMessage;

                mockedAmqpConnection.sendAmqpMessage(mockedAmqpMessage);

                mockedObjectLock.waitLock(anyLong);
                result = new InterruptedException();
            }
        };

        //act
        provisioningAmqpOperations.sendStatusMessage(TEST_OPERATION_ID, mockedResponseCallback, null);

        //assert
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void sendStatusMessageThrowsOnSendAmqpMessage() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        provisioningAmqpOperations provisioningAmqpOperations = new provisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);
        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.setListener((AmqpListener) any);
                mockedAmqpConnection.open();
            }
        };
        provisioningAmqpOperations.open(TEST_REGISTRATION_ID, mockedSSLContext, true);

        new NonStrictExpectations()
        {
            {
                new AmqpMessage();
                result = new Exception();
            }
        };

        //act
        provisioningAmqpOperations.sendStatusMessage(TEST_OPERATION_ID, mockedResponseCallback, null);

        //assert
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void sendRegisterMessageThrowsOnResponseCallbackNull() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        provisioningAmqpOperations provisioningAmqpOperations = new provisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        provisioningAmqpOperations.sendRegisterMessage(null, null);

        //assert
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void sendRegisterMessageThrowsInterrruptedException() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        provisioningAmqpOperations provisioningAmqpOperations = new provisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);
        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.setListener((AmqpListener)any);
                mockedAmqpConnection.open();
            }
        };
        provisioningAmqpOperations.open(TEST_REGISTRATION_ID, mockedSSLContext, true);

        new NonStrictExpectations()
        {
            {
                new AmqpMessage();
                result = mockedAmqpMessage;

                mockedAmqpConnection.sendAmqpMessage(mockedAmqpMessage);

                mockedObjectLock.waitLock(anyLong);
                result = new InterruptedException();
            }
        };

        //act
        provisioningAmqpOperations.sendRegisterMessage(mockedResponseCallback, null);

        //assert
    }

    @Test
    public void sendRegisterMessageSucceeds() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        provisioningAmqpOperations provisioningAmqpOperations = new provisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);
        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.setListener((AmqpListener)any);
                mockedAmqpConnection.open();
            }
        };
        provisioningAmqpOperations.open(TEST_REGISTRATION_ID, mockedSSLContext, true);

        setupSendReceiveMocks();

        //act
        provisioningAmqpOperations.sendRegisterMessage(mockedResponseCallback, null);

        //assert
    }

    @Test
    public void MessageReceivedSucceeds() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        provisioningAmqpOperations provisioningAmqpOperations = new provisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        provisioningAmqpOperations.MessageReceived(mockedAmqpMessage);

        //assert
        new Verifications()
        {
            {
                mockedObjectLock.notifyLock();
                times = 1;
            }
        };
    }

    @Test
    public void UnusedFunctionsSucceeds() throws ProvisioningDeviceClientException
    {
        //arrange
        provisioningAmqpOperations provisioningAmqpOperations = new provisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        provisioningAmqpOperations.ConnectionEstablished();
        provisioningAmqpOperations.ConnectionLost();
        provisioningAmqpOperations.MessageSent();

        //assert
    }
}