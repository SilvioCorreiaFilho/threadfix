package com.denimgroup.threadfix.webapp.controller.rest;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.denimgroup.threadfix.data.entities.Application;
import com.denimgroup.threadfix.data.entities.ApplicationCriticality;
import com.denimgroup.threadfix.data.entities.Organization;
import com.denimgroup.threadfix.service.APIKeyService;
import com.denimgroup.threadfix.service.ApplicationCriticalityService;
import com.denimgroup.threadfix.service.ApplicationService;
import com.denimgroup.threadfix.service.OrganizationService;

@Controller
@RequestMapping("/rest/teams")
public class TeamRestController extends RestController {

	private OrganizationService organizationService;
	private ApplicationService applicationService;
	private ApplicationCriticalityService applicationCriticalityService;
	
	public static final String CREATION_FAILED = "New Team creation failed.";
	public static final String LOOKUP_FAILED = "Team Lookup failed.";
	
	private final static String DETAIL = "teamIDLookup", 
		LOOKUP = "teamNameLookup",
		NEW = "newTeam",
		INDEX = "teamList";
	
	// TODO finalize which methods need to be restricted
	static {
		restrictedMethods.add(NEW);
	}
	
	@Autowired
	public TeamRestController(OrganizationService organizationService,
                              APIKeyService apiKeyService,
                              ApplicationCriticalityService applicationCriticalityService,
                              ApplicationService applicationService) {
		super(apiKeyService);
		this.organizationService = organizationService;
		this.applicationService = applicationService;
		this.applicationCriticalityService = applicationCriticalityService;
	}

	@RequestMapping(headers = "Accept=application/json", value="/{teamID}", method = RequestMethod.GET)
	public @ResponseBody RestResponse teamIDLookup(@PathVariable("teamID") int teamId,
			HttpServletRequest request) {
		log.info("Received REST request for Team with ID " + teamId + ".");

		String result = checkKey(request, DETAIL);
		if (!result.equals(API_KEY_SUCCESS)) {
			return RestResponse.failure(result);
		}

		Organization org = organizationService.loadOrganization(teamId);

		if (org == null) {
			log.warn("Team lookup failed for ID " + teamId + ".");
			return RestResponse.failure(LOOKUP_FAILED);
		} else {
			log.info("REST request for Team with ID " + teamId
					+ " completed successfully.");
			return RestResponse.success(org);
		}
	}
	
	/**
	 * Create a new application with the supplied name and URL. 
	 * The rest of the configuration is done through other methods.
	 */
	@RequestMapping(headers="Accept=application/json", value="/{teamId}/applications/new", method=RequestMethod.POST)
	public @ResponseBody RestResponse newApplication(HttpServletRequest request,
			@PathVariable("teamId") int teamId) {
		log.info("Received REST request for a new Application.");

		String result = checkKey(request, NEW);
		if (!result.equals(API_KEY_SUCCESS)) {
			return RestResponse.failure(result);
		}
		
		// By not using @RequestParam notations, we can catch the error in the code
		// and provide better error messages.
		String name = request.getParameter("name");
		String url = request.getParameter("url");
		
		if (name == null) {
			log.warn("Call to New Application was missing the name parameter.");
			return RestResponse.failure(CREATION_FAILED);
		}
		
		if (url != null) {
			// test URL format
			try {
				new URL(url);
			} catch (MalformedURLException e) {
				log.warn("The supplied URL was not formatted correctly.");
				return RestResponse.failure(CREATION_FAILED);
			}
		}
		
		Organization organization = organizationService.loadOrganization(teamId);
		
		if (organization == null) {
			log.warn("Invalid Team ID.");
			return RestResponse.failure(CREATION_FAILED);
		}
		
		Application application = new Application();
        application.setOrganization(organization);
        application.setName(name.trim());
        if (url != null) {
            application.setUrl(url.trim());
        }
        // TODO include this as a parameter
        application.setApplicationCriticality(
                applicationCriticalityService.loadApplicationCriticality(
                        ApplicationCriticality.LOW));

		if (applicationService.checkApplication(application)) {
			applicationService.storeApplication(application);
			log.info("Application creation was successful. Returning application.");
			return RestResponse.success(application);
		} else {
			//	TODO - We could really use some better debug here
			log.warn("Something was invalid.");
			return RestResponse.failure("Problems creating application.");
		}
	}
	
	@RequestMapping(headers = "Accept=application/json", value="/lookup", method = RequestMethod.GET)
	public @ResponseBody RestResponse teamNameLookup(HttpServletRequest request) {
		
		String teamName = request.getParameter("name");
		
		log.info("Received REST request for Team with ID " + teamName + ".");

		String result = checkKey(request, LOOKUP);
		if (!result.equals(API_KEY_SUCCESS)) {
			return RestResponse.failure(result);
		}

		Organization org = organizationService.loadOrganization(teamName);

		if (org == null) {
			log.warn("Team lookup failed for ID " + teamName + ".");
			return RestResponse.failure("No team found with name '" + teamName + "'");
		} else {
			log.info("REST request for Team with ID " + teamName
					+ " completed successfully.");
			return RestResponse.success(org);
		}
	}

	@RequestMapping(headers = "Accept=application/json", value = "/new", method = RequestMethod.POST,
            produces = "application/json; charset=utf-8")
	public @ResponseBody RestResponse newTeam(HttpServletRequest request) {
		log.info("Received REST request for new Team.");

		String result = checkKey(request, NEW);
		if (!result.equals(API_KEY_SUCCESS)) {
			return RestResponse.failure(result);
		}

		if (request.getParameter("name") != null) {
			
			Organization organization = new Organization();
			organization.setName(request.getParameter("name"));
			
			if (organizationService.isValidOrganization(organization)) {
				organizationService.storeOrganization(organization);
				log.info("Successfully created new Team.");
				return RestResponse.success(organization);
			} else {
				log.info(CREATION_FAILED);
				return RestResponse.failure(CREATION_FAILED);
			}
			
		} else {
			log.warn("\"name\" parameter was not present, new Team creation failed.");
			return RestResponse.failure("\"name\" parameter was not present, new Team creation failed.");
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/")
	public @ResponseBody RestResponse teamList(HttpServletRequest request) {
		log.info("Received REST request for Team list.");
		
		String result = checkKey(request, INDEX);
		if (!result.equals(API_KEY_SUCCESS)) {
			return RestResponse.failure(result);
		}

		return RestResponse.success(organizationService.loadAllActive());
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "")
	public @ResponseBody RestResponse alsoTeamList(HttpServletRequest request) {
		return teamList(request);
	}

}
