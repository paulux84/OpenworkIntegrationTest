package it.trecube.openwork.test;

import com.openworkbpm.schema.*;
import com.openworkbpm.schema.data_application.IDataManagement;
import com.openworkbpm.schema.data_application.IDataManagementbasicHttpBindingGateway;
import com.openworkbpm.schema.event_application.IEventManagement;
import com.openworkbpm.schema.event_application.IEventManagementbasicHttpBindingGateway;
import com.openworkbpm.schema.identity_application.IIdentityManagement;
import com.openworkbpm.schema.identity_application.IIdentityManagementbasicHttpBindingGateway;
import com.openworkbpm.schema.session_application.ISessionManagement;
import com.openworkbpm.schema.session_application.ISessionManagementbasicHttpBindingGateway;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;


/**
 * Unit test for simple App.
 */
public class AppTest
        extends TestCase
{

    ISessionManagement sessionManagement = new ISessionManagementbasicHttpBindingGateway().getBasicHttpBindingGatewayISessionManagement();

    IIdentityManagement identityManagement =  new IIdentityManagementbasicHttpBindingGateway().getBasicHttpBindingGatewayIIdentityManagement();

    IDataManagement dataManagement = new IDataManagementbasicHttpBindingGateway().getBasicHttpBindingGatewayIDataManagement();

    IEventManagement eventManagement = new IEventManagementbasicHttpBindingGateway().getBasicHttpBindingGatewayIEventManagement();

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


    String repositoryId = "0782621F10E744FE9E2DA2B200EFE71D" ;
    String modelUrlUtenti ="/"+repositoryId+"/Model/8BF7283E735943D780C3A2B3010CDA2A";


    public void testCreateUser() throws UnknownHostException {
        ServiceResultOfLoginInfo result = sessionManagement.login("spesautileplustesT", "administrator", "SupTest!", false, InetAddress.getLocalHost().getHostName());

        assertFalse(result.isError());
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setToken(result.getValue().getToken());

        ServiceResultOfIdentity identityResult = identityManagement.getNewIdentity(repositoryId, modelUrlUtenti, requestInfo);
        assertFalse(identityResult.isError());
        Identity identity = identityResult.getValue();
        identity.setName("Mario Rossi"); //TODO: serve?

        updateFieldItem(identity.getFields(), "Nome", "Mario");

    
                
        updateFieldItem(identity.getFields(), "Nome", "Mario");
        updateFieldItem(identity.getFields(), "Cognome", "Marii");
//        updateFieldItem(identity.getFields(),"Data di nascita",new Date()));
        updateFieldItem(identity.getFields(), "Username", "nomeOrg_Able2_"+new Date().getTime());
        updateFieldItem(identity.getFields(), "Sesso", "M");

        updateFieldItem(identity.getFields(), "Email", "test@mail.com");


        //identity.setFields(fields);

        ServiceResultOfstring createResult = identityManagement.createIdentity(repositoryId, identity, requestInfo);
        assertFalse(createResult.isError());


        //Creazione Account
        Account userAccount = new Account();

        userAccount.setName("user_"+new Date().getTime());
        userAccount.setAllowPasswordChange(true);
        userAccount.setId(createResult.getValue());
        userAccount.setPasswordRequired(true);

        ServiceResult accountResult = identityManagement.createAccount(repositoryId,userAccount,"password",requestInfo);
        assertFalse(accountResult.isError());
    }

    private void updateFieldItem(Fields fields , String key, Object value){
        for(Fields.Item item : fields.getItem()){
            if(item.getKey().equals(key))
                item.setVal(value);
        }
    }
}
