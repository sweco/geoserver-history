package org.geoserver.ows.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.geoserver.ows.OWS;
import org.geoserver.ows.Service;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Initializes an OWS service on an incoming request.
 * <p>
 * 
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class OWSInitializer implements HandlerInterceptor, ApplicationContextAware {

	ApplicationContext applicationContext;
	
	public void setApplicationContext(ApplicationContext applicationContext)
		throws BeansException {
		
		this.applicationContext = applicationContext;
	}
	
	public boolean preHandle(
		HttpServletRequest request, HttpServletResponse response, Object handler
	) throws Exception {
		
		//only for the OWS dispatched
		if ( handler instanceof Dispatcher ) {
			Collection descriptors = 
				applicationContext.getBeansOfType( Service.class ).values();
			for ( Iterator d = descriptors.iterator(); d.hasNext(); ) {
				Service descriptor = (Service) d.next();
				Object service = descriptor.getService();
				
				if ( service instanceof OWS ) {
					OWS ows = (OWS) service;
					
					//set the online resource
					if ( ows.getOnlineResource() == null ) {
						try {
							URL url = new URL(
								baseURL( request ) + descriptor.getId()
							);
							ows.setOnlineResource( url );
						} 
						catch (MalformedURLException e) {
							throw new RuntimeException( e );
						}
							
					}
					
					//set the schema base
					if ( ows.getSchemaBaseURL() == null ) {
						ows.setSchemaBaseURL( baseURL( request ) + "schemas" );
					}
					
					//set the charset
					if ( ows.getCharSet() == null ) {
						Charset charSet = null;
						try {
							charSet = Charset.forName( request.getCharacterEncoding() );
						}
						catch( UnsupportedCharsetException e ) {
							charSet = Charset.forName( "UTF-8" );
						}
						
						ows.setCharSet( charSet );
					}
					
					//set the schema base
					
					
					
				}
			}
		}
		
		return true;
	}

	public void postHandle(
		HttpServletRequest request, HttpServletResponse response, Object handler,
		ModelAndView modelAndView
	) throws Exception {
		
		// do nothing

	}

	public void afterCompletion(
		HttpServletRequest request, HttpServletResponse response, Object handler, 
		Exception ex
	) throws Exception {
		
		//do nothing

	}
	

	//JD: move this method into utility class, and have old "Requests" class
	// delegate to it
	String baseURL( HttpServletRequest req ) {
		String url = req.getScheme() + "://" + req.getServerName() + ":"
        + req.getServerPort() + req.getContextPath() +"/";
		
		return url;
	}
}
