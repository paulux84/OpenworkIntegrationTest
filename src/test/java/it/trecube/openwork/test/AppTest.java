package it.trecube.openwork.test;

import com.openworkbpm.schema.*;
import com.openworkbpm.schema.data_application.IDataManagement;
import com.openworkbpm.schema.data_application.IDataManagementbasicHttpBindingGateway;
import com.openworkbpm.schema.event_application.IEventManagement;
import com.openworkbpm.schema.event_application.IEventManagementbasicHttpBindingGateway;
import com.openworkbpm.schema.identity_application.IIdentityManagement;
import com.openworkbpm.schema.identity_application.IIdentityManagementbasicHttpBindingGateway;
import com.openworkbpm.schema.model_application.IModelManagement;
import com.openworkbpm.schema.model_application.IModelManagementbasicHttpBindingGateway;
import com.openworkbpm.schema.session_application.ISessionManagement;
import com.openworkbpm.schema.session_application.ISessionManagementbasicHttpBindingGateway;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;


public class AppTest
{

    private static final String USERNAME = "neri";
    private static final String PASSWORD = "ng";

    ISessionManagement sessionManagement = new ISessionManagementbasicHttpBindingGateway().getBasicHttpBindingGatewayISessionManagement();

    IIdentityManagement identityManagement =  new IIdentityManagementbasicHttpBindingGateway().getBasicHttpBindingGatewayIIdentityManagement();

    IDataManagement dataManagement = new IDataManagementbasicHttpBindingGateway().getBasicHttpBindingGatewayIDataManagement();

    IEventManagement eventManagement = new IEventManagementbasicHttpBindingGateway().getBasicHttpBindingGatewayIEventManagement();

    IModelManagement modelManagement = new IModelManagementbasicHttpBindingGateway().getBasicHttpBindingGatewayIModelManagement();

    String repositoryId = "0782621F10E744FE9E2DA2B200EFE71D" ;
    String modelUrlUtenti ="/"+repositoryId+"/Model/8BF7283E735943D780C3A2B3010CDA2A";

    String identityCatalogUrl = "/"+repositoryId+"/Model/68BBE39D7A474E52BDD4A30801111341";

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
    public void updateUserAccount() throws UnknownHostException {
        SearchParameters parameters = getSearchParameters("Nome",getStringFilter("Username", PredicateTypes.EQUALS_TO, "nomeOrg_Able2_1433520789264"));

        RequestInfo requestInfo = login(USERNAME, PASSWORD);
        //TODO: Reperimento modelUrl
//        ServiceResultOfCatalog resultOfCatalog = identityManagement.getMainCatalog(repositoryId, requestInfo);


        ServiceResultOfCatalog result = identityManagement.getCatalog(repositoryId, identityCatalogUrl, parameters, requestInfo);
        if(result.isError())
            assertTrue(result.getResultInfoList().getResultInfo().get(0).getDetailMessages().getString().get(0),false);

        String userId = result.getValue().getItemList().getCatalogItem().get(0).getUrl().split("/")[3];
        //update Account
        Account userAccount = new Account();

        userAccount.setName("user_" + new Date().getTime());
        userAccount.setAllowPasswordChange(true);
        //associa l'account ad un'identità in base al suo id (in questo caso è quello ritornato in fase di creazione in remoto)
        userAccount.setId(userId);
        userAccount.setPasswordRequired(true);

        identityManagement.deleteAccountById(repositoryId,userId,requestInfo);

        //Crea l'account in remoto
        ServiceResult accountResult = identityManagement.createAccount(repositoryId, userAccount, "password", requestInfo);
        assertFalse(accountResult.isError());
    }

    private SearchParameters getSearchParameters(String columnName, Filter filter) {
        SearchParameters parameters = new SearchParameters();

        Ordering ordering = new Ordering();
        ordering.setAscending(true);
        ordering.setColumnName(columnName);


        ArrayOfOrdering ao = new ArrayOfOrdering();
        ao.getOrdering().add(ordering);

        parameters.setOrderingCriteria(ao);
        parameters.setPageNumber(1);
        parameters.setPageSize(-1);

        parameters.setFilter(filter);
        return parameters;
    }

    @Test
    public void lancioEventoAzioneUtente() throws UnknownHostException {
        RequestInfo info = login("neri","ng");
        SearchParameters parameters = getSearchParameters("Name",getStringFilter("Name", PredicateTypes.EQUALS_TO, "Evento Azione Utente"));//"Azioni Promosse"

        ServiceResultOfCatalog resultOfCatalog = modelManagement.getCurrentModelsCatalog(repositoryId,Types.CUSTOM_TYPE_MODEL,parameters,info);
        if(resultOfCatalog.isError())
            assertTrue(resultOfCatalog.getResultInfoList().getResultInfo().get(0).getMessage()+ "\nDetail: "+resultOfCatalog.getResultInfoList().getResultInfo().get(0).getDetailMessages(),false);
        Catalog catalog = resultOfCatalog.getValue();
        assertEquals("non ci sono eventi con il nome Evento Azioni Utente", 1, catalog.getTotalCount().intValue());
        CatalogItem catalogItem = catalog.getItemList().getCatalogItem().get(0);
        CustomEvent eventToPublish = new CustomEvent();
        eventToPublish.setModelReference(new NamedReference());
        eventToPublish.getModelReference().setName(catalogItem.getName());
        eventToPublish.getModelReference().setUrl(catalogItem.getUrl());

        Fields fields = new Fields();
        fields.getItem().add(createFieldItem("Username","grazia"));
        fields.getItem().add(createFieldItem("Tipo Azione","soggiorno"));
        fields.getItem().add(createFieldItem("Applicazione","Cicerone"));
        fields.getItem().add(createFieldItem("Data", new Date()));

        eventToPublish.setFields(fields);

        ServiceResultOfboolean eventPublishResult = eventManagement.publishEvent(repositoryId,eventToPublish,info);
        if(eventPublishResult.isError())
            assertTrue(eventPublishResult.getResultInfoList().getResultInfo().get(0).getMessage()+ "\nDetail: "+eventPublishResult.getResultInfoList().getResultInfo().get(0).getDetailMessages(),false);

        //dataManagement

    }

    private Fields.Item createFieldItem(String key, Object value){
        Fields.Item field = new Fields.Item();
        field.setKey(key);
        field.setVal(value);
        return field;
    }

    public static Filter getStringFilter (final String subject,PredicateTypes predicate, String complement)
    {
        Filter filter = new Filter();

        Subject subjectClass = new Subject();
        subjectClass.setCode(subject);

        filter.setSubject(subjectClass);

        Predicate predicateClass = new Predicate();

        predicateClass.setType(predicate);
        predicateClass.setValue(PredicateValueTypes.TRUE);
        filter.setPredicate(predicateClass);

        EvaluationExpression evaluationExpression = new EvaluationExpression();
        evaluationExpression.setCode("'"+complement+"'");
        filter.setComplement(evaluationExpression);
        return filter;
    }

}

