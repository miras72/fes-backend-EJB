package pl.tycm.fes.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.jboss.resteasy.plugins.interceptors.CorsFilter;
import org.jboss.resteasy.plugins.interceptors.RoleBasedSecurityFeature;

@ApplicationPath("/api")
public class JaxRsActivator extends Application {

	private Set<Object> singletons = new HashSet<Object>();
	private HashSet<Class<?>> classes = new HashSet<Class<?>>();

	public JaxRsActivator() {
		CorsFilter corsFilter = new CorsFilter();
		corsFilter.getAllowedOrigins().add("*");
		corsFilter.setAllowedMethods("OPTIONS, GET, POST, DELETE, PUT, PATCH");
		singletons.add(corsFilter);
		singletons.add(new AuthenticationFilter());
		//singletons.add(new AuthorizationFilter());

		classes.add(TaskStatusRestService.class);
		classes.add(TaskConfigRestService.class);
		classes.add(EncryptionKeyRestService.class);
		classes.add(DecryptionKeyRestService.class);
		classes.add(FileExchangeStatusRestService.class);
		classes.add(ManualFileExchangeRestService.class);
		classes.add(ServerRestService.class);
		classes.add(AuthenticationEndpointRestService.class);
		classes.add(RoleBasedSecurityFeature.class);

	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}

	@Override
	public HashSet<Class<?>> getClasses() {
		return classes;
	}
}