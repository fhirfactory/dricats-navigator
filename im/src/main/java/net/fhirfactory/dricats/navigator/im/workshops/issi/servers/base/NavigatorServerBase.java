/*
 * Copyright (c) 2021 Mark A. Hunter (ACT Health)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.fhirfactory.dricats.navigator.im.workshops.issi.servers.base;

import net.fhirfactory.pegacorn.core.constants.systemwide.DRICaTSReferenceProperties;
import net.fhirfactory.pegacorn.core.model.ui.transactions.exceptions.ResourceNotFoundException;
import net.fhirfactory.pegacorn.core.model.ui.transactions.exceptions.ResourceUpdateException;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.OnExceptionDefinition;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestConfigurationDefinition;
import org.apache.camel.model.rest.RestDefinition;
import org.apache.camel.model.rest.RestParamType;
import org.slf4j.Logger;

import javax.inject.Inject;

public abstract class NavigatorServerBase extends RouteBuilder {

    @Inject
    private DRICaTSReferenceProperties pegacornReferenceProperties;


    private static String SERVER_PORT = "12121";
    private static String SERVER_HOST = "0.0.0.0";

    @Override
    public void configure() throws Exception {
    }

    protected DRICaTSReferenceProperties getPegacornReferenceProperties() {
        return (pegacornReferenceProperties);
    }

    protected String getPathSuffix() {
        String suffix = "?matchOnUriPrefix=true&option.enableCORS=true&option.corsAllowedCredentials=true";
        return (suffix);
    }

    abstract protected Logger getLogger();
    abstract protected String specifyESRName();
    abstract protected Class specifyESRClass();

    public String getESRName(){
        return(specifyESRName());
    }

    public Class getESRClass(){
        return(specifyESRClass());
    }

    public static String getServerPort() {
        return (SERVER_PORT);
    }

    public static String getServerHost() {
        return SERVER_HOST;
    }

    protected RestConfigurationDefinition getRestConfigurationDefinition() {
        RestConfigurationDefinition restConf = restConfiguration()
                .component("netty-http")
                .scheme("http")
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true")
                .contextPath(getPegacornReferenceProperties().getDricatsResourceDirectoryR1Path()).host(getServerHost()).port(getServerPort())
                .enableCORS(true)
                .corsAllowCredentials(true)
                .corsHeaderProperty("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, login");
        return (restConf);
    }

    protected RestDefinition getRestGetDefinition() {
        RestDefinition restDef = rest("/" + getESRName())
            .get("/{simplifiedID}").outType(getESRClass())
                .to("direct:" + getESRName() + "GET")
            .get("?pageSize={pageSize}&page={page}&sortBy={sortBy}&sortOrder={sortOrder}")
                .param().name("pageSize").type(RestParamType.query).required(false).endParam()
                .param().name("page").type(RestParamType.query).required(false).endParam()
                .param().name("sortBy").type(RestParamType.query).required(false).endParam()
                .param().name("sortOrder").type(RestParamType.query).required(false).endParam()
                .to("direct:" + getESRName() + "ListGET");
        return (restDef);
    }

    protected OnExceptionDefinition getResourceNotFoundException() {
        OnExceptionDefinition exceptionDef = onException(ResourceNotFoundException.class)
                .handled(true)
                .log(LoggingLevel.INFO, "ResourceNotFoundException...")
                // use HTTP status 404 when data was not found
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                .setBody(simple("${exception.message}\n"));

        return(exceptionDef);
    }

    protected OnExceptionDefinition getResourceUpdateException() {
        OnExceptionDefinition exceptionDef = onException(ResourceUpdateException.class)
                .handled(true)
                .log(LoggingLevel.INFO, "ResourceUpdateException...")
                // use HTTP status 404 when data was not found
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setBody(simple("${exception.message}\n"));

        return(exceptionDef);
    }

    protected OnExceptionDefinition getGeneralException() {
        OnExceptionDefinition exceptionDef = onException(Exception.class)
                .handled(true)
                // use HTTP status 500 when we had a server side error
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setBody(simple("${exception.message}\n"));
        return (exceptionDef);
    }
}
