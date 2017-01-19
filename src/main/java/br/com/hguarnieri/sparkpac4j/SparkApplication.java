package br.com.hguarnieri.sparkpac4j;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.mustache.MustacheTemplateEngine;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pac4j.core.config.Config;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.sparkjava.ApplicationLogoutRoute;
import org.pac4j.sparkjava.CallbackRoute;
import org.pac4j.sparkjava.SecurityFilter;
import org.pac4j.sparkjava.SparkWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.get;
import static spark.Spark.staticFileLocation;

import static spark.Spark.*;

/**
 * Created by Henrique on 18/01/17.
 * This is the main class for the Spark project. Here is where the magic happens.
 * All procedures are managed on the main() method, and it's divided in 5 parts:
 * 1 - Static configurations: server port, folder location and etc.
 * 2 - Callback URLs: login authentication
 * 3 - Security filters: you can set which users can access each method
 * 4 - Methods: methods used to expose the user APIs
 * 5 - Exceptions: logger
 */
public class SparkApplication {
	
	private final static MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();
	
	private final static Logger logger = LoggerFactory.getLogger(SparkApplication.class);

	public static void main(String[] args) {
		// TODO: Server port
		port(8080);
		// TODO: Static file folder
		staticFileLocation("/public");
		
		final Config config = new ProjectConfigFactory(templateEngine).build();
	
		final CallbackRoute callback = new CallbackRoute(config, null, true);
		get("/callback", callback);
		post("/callback", callback);
		
		// TODO: Add here security filters
		before("/", new SecurityFilter(config, "FormClient", "admin"));
		
		// TODO: Add here get/post methods
		get("/", SparkApplication::index, new MustacheTemplateEngine());
		get("/loginForm", (rq, rs) -> form(config), templateEngine);
		get("/logout", new ApplicationLogoutRoute(config, "/?defaulturlafterlogout"));
		
		exception(Exception.class, (e, request, response) -> {
			logger.error("Unexpected exception", e);
			response.body(templateEngine.render(new ModelAndView(new HashMap<>(), "error500.mustache")));
		});
	}

	public static ModelAndView index(Request req, Response res) {
		Map<String, Object> params = new HashMap<>();
		params.put("name", req.queryParams("name"));
		params.put("profiles", getProfiles(req, res));
		return new ModelAndView(params, "index.mustache");
	}
	
	private static ModelAndView form(final Config config) {
		final Map<String, String> map = new HashMap<String, String>();
		final FormClient formClient = config.getClients().findClient(FormClient.class);
		map.put("callbackUrl", formClient.getCallbackUrl());
		return new ModelAndView(map, "loginForm.mustache");
	}
	
	// Return logged profile
	private static List<CommonProfile> getProfiles(final Request request, final Response response) {
		final SparkWebContext context = new SparkWebContext(request, response);
		final ProfileManager<CommonProfile> manager = new ProfileManager<CommonProfile>(context);
		return manager.getAll(true);
	}
	
}
