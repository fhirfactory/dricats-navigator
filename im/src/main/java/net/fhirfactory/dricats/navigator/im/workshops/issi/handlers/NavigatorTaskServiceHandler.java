package net.fhirfactory.dricats.navigator.im.workshops.issi.handlers;

import net.fhirfactory.dricats.navigator.im.workshops.issi.handlers.base.NavigatorHandlerBase;
import net.fhirfactory.pegacorn.core.interfaces.ui.TaskUIServicesAPI;
import net.fhirfactory.pegacorn.core.model.petasos.task.datatypes.status.valuesets.ActionableTaskOutcomeStatusEnum;
import net.fhirfactory.pegacorn.core.model.ui.resources.simple.PractitionerRoleESR;
import net.fhirfactory.pegacorn.core.model.ui.resources.simple.search.exceptions.ESRPaginationException;
import net.fhirfactory.pegacorn.core.model.ui.resources.simple.search.exceptions.ESRSortingException;
import net.fhirfactory.pegacorn.core.model.ui.resources.summaries.TaskSummary;
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
import java.time.Instant;
import java.util.List;

@Dependent
public class NavigatorTaskServiceHandler extends NavigatorHandlerBase {
    private static final Logger LOG = LoggerFactory.getLogger(NavigatorTaskServiceHandler.class);

    @Inject
    private TaskUIServicesAPI taskServicesAPI;

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

    //
    // Review (Search)
    //

    public List<TaskSummary> taskSummarySearch(@Header("shortName") String shortName,
                                                    @Header("longName") String longName,
                                                    @Header("displayName") String displayName,
                                                    @Header("subsystemName") String subsystemName,
                                                    @Header("componentName") String componentName,
                                                    @Header("outcomeStatus") String outcomeStatus,
                                                    @Header("sortBy") String sortBy,
                                                    @Header("sortOrder") String sortOrder,
                                                    @Header("pageSize") String pageSize,
                                                    @Header("page") String page)
            throws ResourceNotFoundException, ResourceInvalidSortException, ESRPaginationException, ResourceInvalidSearchException, ESRSortingException {
        getLogger().debug(".defaultSearch(): Entry, shortName->{}, longName->{}, displayName->{}, outcomeStatus->{}"
                        + "sortBy->{}, sortOrder->{}, pageSize->{},page->{}, subsystemName->{}",
                shortName, longName, displayName, outcomeStatus, sortBy, sortOrder, pageSize, page, subsystemName);
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
        ActionableTaskOutcomeStatusEnum outcomeStatusEnum = ActionableTaskOutcomeStatusEnum.valueOf(outcomeStatus);
        List<TaskSummary> outcome = taskServicesAPI.listTasks(shortName,outcomeStatusEnum, true, Instant.now(), Instant.EPOCH );
        getLogger().debug(".defaultSearch(): Exit");
        return(outcome);
    }


    //
    // Update
    //

    public void updateTaskSummary(PractitionerRoleESR entryToUpdate, Exchange camelExchange)
            throws ResourceUpdateException, ResourceInvalidSearchException {
        getLogger().info(".updateTaskSummary(): Entry, inputBody --> {}", entryToUpdate);

        getLogger().info(".updateTaskSummary(): Exit, something has gone wrong.....");
    }

    //
    // Delete
    //

    public void deletTask(String id){
        getLogger().debug(".updateTaskSummary(): Entry, id --> {}", id);
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
