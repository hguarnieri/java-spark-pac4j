package br.com.hguarnieri.sparkpac4j;

import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.pac4j.http.client.indirect.FormClient;

import spark.TemplateEngine;

public class ProjectConfigFactory implements ConfigFactory {

	private final TemplateEngine templateEngine;

    public ProjectConfigFactory(final TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }
	
	@SuppressWarnings("rawtypes")
	public Config build() {

		// TODO: Change project host URL
		FormClient formClient = new FormClient("http://localhost:8080/loginForm", new ProjectPasswordAuthenticator());
		Clients clients       = new Clients("http://localhost:8080/callback", formClient);
		Config config         = new Config(clients);
		
		// TODO: If the project has more than one role
		config.addAuthorizer("admin", new RequireAnyRoleAuthorizer("ROLE_ADMIN"));
		config.setHttpActionAdapter(new ProjectHttpActionAdapter(templateEngine));
		
		return config;
	}
}