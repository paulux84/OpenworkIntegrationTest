package it.trecube.openwork.test;

import com.openworkbpm.schema.ServiceResultOfLoginInfo;
import com.openworkbpm.schema.session_application.ISessionManagement;
import com.openworkbpm.schema.session_application.ISessionManagementbasicHttpBindingGateway;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.net.InetAddress;
import java.net.UnknownHostException;



/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */

    public void testApp() throws UnknownHostException {
        ISessionManagement sessionManagement = new ISessionManagementbasicHttpBindingGateway().getBasicHttpBindingGatewayISessionManagement();
        ServiceResultOfLoginInfo result = sessionManagement.login("spesautileplustesT", "administrator", "SupTest!", false, InetAddress.getLocalHost().getHostName());
        assertFalse(result.isError());
    }
}
