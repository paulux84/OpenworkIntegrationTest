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
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;


public class AppTest
{

    private static final String USERNAME = "administrator";
    private static final String PASSWORD = "SupTest!";

    ISessionManagement sessionManagement = new ISessionManagementbasicHttpBindingGateway().getBasicHttpBindingGatewayISessionManagement();

    IIdentityManagement identityManagement =  new IIdentityManagementbasicHttpBindingGateway().getBasicHttpBindingGatewayIIdentityManagement();

    IDataManagement dataManagement = new IDataManagementbasicHttpBindingGateway().getBasicHttpBindingGatewayIDataManagement();

    IEventManagement eventManagement = new IEventManagementbasicHttpBindingGateway().getBasicHttpBindingGatewayIEventManagement();

    String repositoryId = "0782621F10E744FE9E2DA2B200EFE71D" ;
    String modelUrlUtenti ="/"+repositoryId+"/Model/8BF7283E735943D780C3A2B3010CDA2A";


    @org.junit.Test
    public void testCreateUser() throws UnknownHostException {
       //effettua il login
        RequestInfo requestInfo = login(USERNAME,PASSWORD);

        //inizializza una nuova identità UtenteSuPlus da utilizzare per la creazione (equivalente della parola chiave "new"  in remoto)
        ServiceResultOfIdentity identityResult = identityManagement.getNewIdentity(repositoryId, modelUrlUtenti, requestInfo);
        assertFalse(identityResult.isError());
        Identity identity = identityResult.getValue();
        identity.setName("Mario Rossi"); //TODO: serve?

        //mappa di fields da aggiornare e relativi valori
        Map<String, String> updatedFields = new HashMap<String, String>();
        updatedFields.put("Nome", "Mario");
        updatedFields.put( "Nome", "Mario");
        updatedFields.put( "Cognome", "Marii");
//         fieldsToUpdate.put("Data di nascita",new Date()));
        updatedFields.put("Username", "nomeOrg_Able2_" + new Date().getTime());
        updatedFields.put("Sesso", "M");
        updatedFields.put("Email", "test@mail.com");

        //aggiorna l'entità con i fields sopra
        updateFields(identity.getFields(), updatedFields);

        //creazione identità in SuPlus
        ServiceResultOfstring createResult = identityManagement.createIdentity(repositoryId, identity, requestInfo);
        assertFalse(createResult.isError());

        //Creazione Account
        Account userAccount = new Account();

        userAccount.setName("user_" + new Date().getTime());
        userAccount.setAllowPasswordChange(true);
        //associa l'account ad un'identità in base al suo id (in questo caso è quello ritornato in fase di creazione in remoto)
        userAccount.setId(createResult.getValue());
        userAccount.setPasswordRequired(true);

        //Crea l'account in remoto
        ServiceResult accountResult = identityManagement.createAccount(repositoryId,userAccount,"password",requestInfo);
        assertFalse(accountResult.isError());
    }

    private RequestInfo login(String username, String password) throws UnknownHostException {
        //effettua il login
        ServiceResultOfLoginInfo result = sessionManagement.login("spesautileplustesT", username, password, false, InetAddress.getLocalHost().getHostName());

        assertFalse(result.isError());
        //crea una nuova request con all'interno il token del login
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setToken(result.getValue().getToken());
        return requestInfo;
    }

    private void updateFields(Fields fields , Map<String, String> updatedFields ){
        for(Fields.Item item : fields.getItem()){
            //controlla se è presente un aggiornamento per la chiave puntata
            String potentialValue=updatedFields.get(item.getKey());
            //se presente effettua l'update
            if(potentialValue!=null)
                item.setVal(potentialValue);
        }
    }

    @Test
    public void searchParameters() throws UnknownHostException {
        SearchParameters parameters = new SearchParameters();

        Ordering ordering = new Ordering();
        ordering.setAscending(true);
        ordering.setColumnName("Nome");

        ArrayOfOrdering ao = new ArrayOfOrdering();
        ao.getOrdering().add(ordering);

        parameters.setOrderingCriteria(ao);

        parameters.setFilter(getStringFilter("Nome", PredicateTypes.EQUALS_TO, "nomeOrg_Able2_1433520789264"));

        //Lancia No Permission Error
        ServiceResultOfCatalog result = identityManagement.getCatalog(repositoryId, modelUrlUtenti, parameters, login(USERNAME, PASSWORD));
        assertFalse(result.isError());

    }

    public static Filter getStringFilter (final String subject,PredicateTypes predicate, String complement)
    {
        App.main(new String[]{});

        Filter filter = new Filter();

        Subject subjectClass = new Subject();
        subjectClass.setCode(subject);

        filter.setSubject(subjectClass);

        Predicate predicateClass = new Predicate();

        predicateClass.setType(predicate);
        predicateClass.setValue(PredicateValueTypes.TRUE);
            //C# code
//            Type=predicate,
//            TypeSpecified=true,
//            Value=PredicateValueTypes.True,
//            ValueSpecified=true,

        EvaluationExpression evaluationExpression = new EvaluationExpression();
        evaluationExpression.setCode(complement);
        filter.setComplement(evaluationExpression);
        return filter;
    }

}

