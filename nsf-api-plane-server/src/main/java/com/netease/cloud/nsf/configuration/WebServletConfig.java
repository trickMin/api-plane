package com.netease.cloud.nsf.configuration;

import com.netease.cloud.nsf.web.filter.LogUUIDFilter;
import com.netease.cloud.nsf.web.filter.RequestContextHolderFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebServletConfig extends WebMvcConfigurerAdapter {

	@Bean
	public FilterRegistrationBean requestContextFilterReg() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(new RequestContextHolderFilter());
		registration.addUrlPatterns("/*");
		registration.setName(RequestContextHolderFilter.class.getSimpleName());
		registration.setOrder(0);
		return registration;
	}

	@Bean
	public FilterRegistrationBean logUuidFilterReg() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(new LogUUIDFilter());
		registration.addUrlPatterns("/*");
		registration.setName(LogUUIDFilter.class.getSimpleName());
		registration.setOrder(1);
		return registration;
	}

}
