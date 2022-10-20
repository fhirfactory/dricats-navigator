package net.fhirfactory.dricats.navigator.im.workshops.issi.handlers;

import net.fhirfactory.dricats.navigator.im.workshops.issi.handlers.base.NavigatorHandlerBase;
import net.fhirfactory.pegacorn.core.interfaces.ui.resources.ParticipantUIServicesAPI;
import net.fhirfactory.pegacorn.core.model.ui.resources.simple.PetasosParticipantESR;
import net.fhirfactory.pegacorn.core.model.ui.resources.simple.PractitionerRoleESR;
import net.fhirfactory.pegacorn.core.model.ui.resources.simple.search.exceptions.ESRPaginationException;
import net.fhirfactory.pegacorn.core.model.ui.resources.simple.search.exceptions.ESRSortingException;
import net.fhirfactory.pegacorn.core.model.ui.resources.summaries.PetasosParticipantSummary;
import net.fhirfactory.pegacorn.core.model.ui.transactions.ESRMethodOutcome;
import net.fhirfactory.pegacorn.core.model.ui.transactions.exceptions.ResourceInvalidSearchException;
import net.fhirfactory.pegacorn.core.model.ui.transactions.exceptions.ResourceInvalidSortException;
import net.fhirfactory.pegacorn.core.model.ui.transactions.exceptions.ResourceNotFoundException;
import net.fhirfactory.pegacorn.core.model.ui.transactions.exceptions.ResourceUpdateException;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Dependent
public class NavigatorParticipantServiceHandler extends NavigatorHandlerBase {
    private static final Logger LOG = LoggerFactory.getLogger(NavigatorParticipantServiceHandler.class);

    @Inject
    private ParticipantUIServicesAPI participantServicesAPI;

    @Override
    protected Logger getLogger() {
        return (LOG);
    }


    //
    // Create
    //

    public void createPractitionerRole(PractitionerRoleESR entryToUpdate, Exchange camelExchange){
        getLogger().info(".update(): Entry, inputBody --> {}", entryToUpdate);
    }

    //
    // Review
    //

    public PetasosParticipantSummary getParticipantSummary(@Header("shortName") String shortName) throws ResourceInvalidSearchException {
        getLogger().debug(".getResource(): Entry, shortName->{}", shortName);
        PetasosParticipantSummary participantSummary = participantServicesAPI.getParticipantSummary(shortName);
        getLogger().debug(".getEntry(): Exit, participantSummary->{}",participantSummary);
        return(participantSummary);
    }

    public PetasosParticipantESR getParticipantESR(@Header("shortName") String shortName) throws ResourceInvalidSearchException {
        getLogger().debug(".getResource(): Entry, shortName->{}", shortName);
        PetasosParticipantESR participantESR = participantServicesAPI.getParticipantESR(shortName);
        getLogger().debug(".getEntry(): Exit, participantSummary->{}",participantESR);
        return(participantESR);
    }


    //
    // Review (Search)
    //

    public List<PetasosParticipantSummary> participantSearch(@Header("shortName") String shortName,
                                                                    @Header("longName") String longName,
                                                                    @Header("displayName") String displayName,
                                                                    @Header("subsystemName") String subsystemName,
                                                                    @Header("sortBy") String sortBy,
                                                                    @Header("sortOrder") String sortOrder,
                                                                    @Header("pageSize") String pageSize,
                                                                    @Header("page") String page)
            throws ResourceNotFoundException, ResourceInvalidSortException, ESRPaginationException, ResourceInvalidSearchException, ESRSortingException {
        getLogger().debug(".defaultSearch(): Entry, shortName->{}, longName->{}, displayName->{}"
                        + "sortBy->{}, sortOrder->{}, pageSize->{},page->{},primaryRoleCategoryID->{}"
                        + "primaryRoleID->{}, primaryOrganizationID->{}, primaryLocationID->{}",
                shortName, longName, displayName, sortBy, sortOrder, pageSize, page, subsystemName);
        String searchAttributeName = null;
        String searchAttributeValue = null;
        if(shortName != null) {
            searchAttributeValue = shortName;
            searchAttributeName = "shortName";
        } else if(longName != null){
            searchAttributeValue = longName;
            searchAttributeName = "longName";
        } else if(displayName != null){
            searchAttributeValue = displayName;
            searchAttributeName = "displayName";
        } else if(subsystemName != null){
            searchAttributeValue = subsystemName;
            searchAttributeName = "subsystemName";
        }
        else {
            throw( new ResourceInvalidSearchException("Search parameter not specified"));
        }
        Integer pageSizeValue = null;
        Integer pageValue = null;
        Boolean sortOrderValue = true;
        if(pageSize != null) {
            pageSizeValue = Integer.valueOf(pageSize);
        }
        if(page != null) {
            pageValue = Integer.valueOf(page);
        }
        if(sortOrder != null) {
            sortOrderValue = Boolean.valueOf(sortOrder);
        }
        String searchAttributeValueURLDecoded = URLDecoder.decode(searchAttributeValue, StandardCharsets.UTF_8);
        List<PetasosParticipantSummary> participantList = participantServicesAPI.listParticipants(shortName);
        getLogger().debug(".defaultSearch(): Exit");
        return(participantList);
    }


    //
    // Update
    //

    public PetasosParticipantSummary update(PetasosParticipantSummary participantToUpdate, Exchange camelExchange)
            throws ResourceUpdateException, ResourceInvalidSearchException {
        getLogger().info(".update(): Entry, participantToUpdate->{}", participantToUpdate);

        if(participantToUpdate == null){
            return(null);
        }

        PetasosParticipantSummary centralParticipantSummary = participantServicesAPI.getParticipantSummary(participantToUpdate.getParticipantId().getName());
        if(centralParticipantSummary.getControlStatus().equals(participantToUpdate.getControlStatus())){
            return(centralParticipantSummary);
        }

        centralParticipantSummary = participantServicesAPI.setControlStatus(participantToUpdate.getParticipantId().getName(), participantToUpdate.getControlStatus());

        if(centralParticipantSummary == null){
            getLogger().info(".update(): Exit, could not update status");
            return(null);
        }
        getLogger().info(".update(): Exit, centralParticipantSummary->{}", centralParticipantSummary);
        return(centralParticipantSummary);
    }

    //
    // Delete
    //

    public void deletePractitionerRole(String id){
        getLogger().debug(".deletePractitionerRole(): Entry, id --> {}", id);
    }

    @Override
    protected void printOutcome(ESRMethodOutcome outcome) {
        if(outcome.isSearchSuccessful()){
            for(Integer counter = 0; counter < outcome.getSearchResult().size(); counter += 1){
                PractitionerRoleESR currentEntry = (PractitionerRoleESR)outcome.getSearchResult().get(counter);
                getLogger().debug("Info: Entry --> {} :: {}", currentEntry.getPrimaryRoleCategoryID(), currentEntry.getDisplayName() );
            }
        }
    }
}
