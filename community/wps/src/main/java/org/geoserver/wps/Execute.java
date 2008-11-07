/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.wps;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Locale;
import java.util.TimeZone;
import java.io.IOException;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.servlet.http.HttpServletResponse;
import javax.xml.datatype.XMLGregorianCalendar;

import net.opengis.wps.WpsFactory;
import net.opengis.ows11.CodeType;
import net.opengis.ows11.Ows11Factory;
import net.opengis.ows11.LanguageStringType;
import net.opengis.ows11.ReferenceType;

import org.geotools.xml.EMFUtils;
import org.geotools.xml.Encoder;
import org.geotools.xml.Configuration;
import org.geotools.xml.EncoderDelegate;
import org.geotools.util.Converters;
import org.geotools.wps.WPSConfiguration;

import org.geotools.data.Parameter;
import org.geotools.process.Process;
import org.geotools.process.ProcessFactory;
import org.geoserver.ows.Ows11Util;
import org.geoserver.platform.ServiceException;
import org.geoserver.wps.ppio.ComplexPPIO;
import org.geoserver.wps.ppio.LiteralPPIO;
import org.geoserver.wps.ppio.ProcessParameterIO;
import org.geoserver.wps.ppio.ReferencePPIO;
import org.geoserver.wps.ppio.XMLPPIO;
import org.geoserver.wps.transmute.Transmuter;
import org.geoserver.wps.transmute.ComplexTransmuter;
import org.geoserver.wps.transmute.LiteralTransmuter;
import org.springframework.context.ApplicationContext;
import org.xml.sax.ContentHandler;

import net.opengis.wps.DataType;
import net.opengis.wps.DocumentOutputDefinitionType;
import net.opengis.wps.InputReferenceType;
import net.opengis.wps.InputType;
import net.opengis.wps.MethodType;
import net.opengis.wps.OutputDefinitionType;
import net.opengis.wps.OutputDefinitionsType;
import net.opengis.wps.OutputReferenceType;
import net.opengis.wps.ProcessFailedType;
import net.opengis.wps.StatusType;
import net.opengis.wps.ExecuteType;
import net.opengis.wps.OutputDataType;
import net.opengis.wps.LiteralDataType;
import net.opengis.wps.ComplexDataType;
import net.opengis.wps.ProcessBriefType;
import net.opengis.wps.ProcessOutputsType1;
import net.opengis.wps.ExecuteResponseType;

/**
 * Main class used to handle Execute requests
 *
 * @author Lucas Reed, Refractions Research Inc
 */
public class Execute {
    WPSInfo             wps;
    ApplicationContext  context;

    public Execute(WPSInfo wps, ApplicationContext context) {
        this.wps      = wps;
        this.context = context;
    }

    /**
     * Main method for performing decoding, execution, and response
     *
     * @param object
     * @param output
     * @throws IllegalArgumentException
     */
    public ExecuteResponseType run(ExecuteType request) {
        //note the current time
        Date started = Calendar.getInstance().getTime();
        
        //load the process factory
        ProcessFactory pf = WPSUtils.findProcessFactory(request.getIdentifier());
        if ( pf == null ) {
            throw new WPSException( "No such process: " + request.getIdentifier().getValue() );
        }
        
        //parse the inputs for the request
        Map<String, Object> inputs = new HashMap();
        
        for ( Iterator i = request.getDataInputs().getInput().iterator(); i.hasNext(); ) {
            InputType input = (InputType) i.next();
            
            //locate the parameter for this request
            Parameter p = pf.getParameterInfo().get( input.getIdentifier().getValue() );
            if ( p == null ) {
                throw new WPSException( "No such parameter: " + input.getIdentifier().getValue() );
            }
            
            //find the ppio
            ProcessParameterIO ppio = ProcessParameterIO.find( p, context );
            if ( ppio == null ) {
                throw new WPSException( "Unable to decode input: " + input.getIdentifier().getValue() );
            }
            
            //read the data
            Object decoded = null;
            if ( input.getReference() != null ) {
                //this is a reference
                InputReferenceType ref = input.getReference();
                
                //grab the location and method
                String href = ref.getHref();
                MethodType meth = ref.getMethod() != null ? ref.getMethod() : MethodType.GET_LITERAL; 
                
                //handle get vs post
                if ( meth == MethodType.POST_LITERAL ) {
                    //post, handle the body
                }
                else {
                    //get, parse kvp
                }
            }
            else {
                //actual data, figure out which type 
                DataType data = input.getData();
               
                if ( data.getLiteralData() != null ) {
                    LiteralDataType literal = data.getLiteralData();
                    decoded = ((LiteralPPIO)ppio).decode( literal.getValue() );
                }
                else if ( data.getComplexData() != null ) {
                    ComplexDataType complex = data.getComplexData();
                    decoded = complex.getData().get( 0 );
                }
                
            }
            
            //decode the input
            inputs.put( p.key, decoded );
        }
        
        //execute the process
        Map<String,Object> result = null;
        Throwable error = null;
        try {
            Process p = pf.create();
            result = p.execute( inputs, null );    
        }
        catch( Throwable t ) {
            //save the error to report back
            error = t;
        }
        
        //build the response
        WpsFactory f = WpsFactory.eINSTANCE;
        ExecuteResponseType response = f.createExecuteResponseType();
       
        //process 
        response.setProcess( f.createProcessBriefType() );
        response.getProcess().setIdentifier( Ows11Util.code( request.getIdentifier() ) );
       
        //status
        response.setStatus( f.createStatusType() );
        response.getStatus().setCreationTime( Converters.convert( started, XMLGregorianCalendar.class ));
        
        if ( error != null ) {
            ProcessFailedType failure = f.createProcessFailedType();
            response.getStatus().setProcessFailed( failure );
            
            failure.setExceptionReport( Ows11Util.exceptionReport( new ServiceException( error ), wps.getGeoServer().isVerboseExceptions() ) );
        }
        else {
            response.getStatus().setProcessSucceeded( "Process succeeded.");
        }
      
        //inputs
        response.setDataInputs( f.createDataInputsType1() );
        for ( Iterator i = request.getDataInputs().getInput().iterator(); i.hasNext(); ) {
            InputType input = (InputType) i.next();
            response.getDataInputs().getInput().add( EMFUtils.clone( input, f, true ) );
        }
        
        //output definitions
        OutputDefinitionsType outputs = f.createOutputDefinitionsType();
        response.setOutputDefinitions( outputs );
        
        Map<String,Parameter<?>> outs = pf.getResultInfo(null);
        Map<String,ProcessParameterIO> ppios = new HashMap();
        
        for ( String key : result.keySet() ) {
            Parameter p = pf.getResultInfo(null).get( key );
            if ( p == null ) {
                throw new WPSException( "No such output: " + key );
            }
            
            //find the ppio
            ProcessParameterIO ppio = ProcessParameterIO.find( p, context );
            if ( ppio == null ) {
                throw new WPSException( "Unable to encode output: " + p.key );
            }
            ppios.put( p.key, ppio );
            
            DocumentOutputDefinitionType output = f.createDocumentOutputDefinitionType();
            outputs.getOutput().add( output );
            
            output.setIdentifier( Ows11Util.code( p.key ) );
            if ( ppio instanceof ComplexPPIO ) {
                output.setMimeType( ((ComplexPPIO) ppio).getMimeType() );
            }
            
            //TODO: encoding + schema
        }
        
        //process outputs
        ProcessOutputsType1 processOutputs = f.createProcessOutputsType1();
        response.setProcessOutputs( processOutputs );
        
        for ( String key : result.keySet() ) {
            OutputDataType output = f.createOutputDataType();
            processOutputs.getOutput().add( output );
            
            final Object o = result.get( key );
            ProcessParameterIO ppio = ppios.get( key );
            
            if ( ppio instanceof ReferencePPIO ) {
                //encode as a reference
                OutputReferenceType ref = f.createOutputReferenceType();
                output.setReference( ref );
                
                //TODO: mime type
                ref.setHref( ((ReferencePPIO) ppio).encode(o).toString() );
            }
            else {
                //encode as data
                DataType data = f.createDataType();
                output.setData( data );
           
                if ( ppio instanceof LiteralPPIO ) {
                    LiteralDataType literal = f.createLiteralDataType();
                    data.setLiteralData( literal );
                    
                    literal.setValue( ((LiteralPPIO) ppio).encode( o ) );
                }
                else if ( ppio instanceof ComplexPPIO ) {
                    ComplexDataType complex = f.createComplexDataType();
                    data.setComplexData( complex );
                    
                    ComplexPPIO cppio = (ComplexPPIO) ppio;
                    complex.setMimeType( cppio.getMimeType() );
                    
                    if ( cppio instanceof XMLPPIO ) {
                        //encode directly
                        complex.getData().add( 
                            new ComplexDataEncoderDelegate( (XMLPPIO) cppio, o )
                        );
                    }
                    else {
                       //TODO: handle other content types, perhaps as CDATA
                    }
                }
            }
        }
        
        return response;
    }

//    @SuppressWarnings("unchecked")
//    private void outputs(Map<String, Object> outputs) {
//        ProcessFactory      pf             = this.executor.getProcessFactory();
//        ProcessOutputsType1 processOutputs = WpsFactory.eINSTANCE.createProcessOutputsType1();
//
//        for(String outputName : outputs.keySet()) {
//            Parameter<?> param = (pf.getResultInfo(null)).get(outputName);
//
//            OutputDataType output = WpsFactory.eINSTANCE.createOutputDataType();
//
//            CodeType identifier = Ows11Factory.eINSTANCE.createCodeType();
//            identifier.setValue(param.key);
//            output.setIdentifier(identifier);
//
//            LanguageStringType title = Ows11Factory.eINSTANCE.createLanguageStringType();
//            title.setValue(param.title.toString(this.locale));
//            output.setTitle(title);
//
//            DataType data = WpsFactory.eINSTANCE.createDataType();
//
//            // Determine the output type, Complex or Literal
//            Object outputParam = outputs.get(outputName);
//
//            final Transmuter transmuter = this.dataTransformer.getDefaultTransmuter(outputParam.getClass());
//
//            // Create appropriate response document node for given type
//            if (transmuter instanceof ComplexTransmuter) {
//                data.setComplexData(this.complexData((ComplexTransmuter)transmuter, outputParam));
//            } else {
//                if (transmuter instanceof LiteralTransmuter) {
//                    data.setLiteralData(this.literalData((LiteralTransmuter)transmuter, outputParam));
//                } else {
//                    throw new WPSException("NoApplicableCode", "Could not find transmuter for output " + outputName);
//                }
//            }
//
//            output.setData(data);
//
//            processOutputs.getOutput().add(output);
//        }
//
//        this.response.setProcessOutputs(processOutputs);
//    }
//
//    private LiteralDataType literalData(LiteralTransmuter transmuter, Object value) {
//        LiteralDataType data = WpsFactory.eINSTANCE.createLiteralDataType();
//
//        data.setValue(   transmuter.encode(value));
//        data.setDataType(transmuter.getEncodedType());
//
//        return data;
//    }
//
//    @SuppressWarnings("unchecked")
//    private ComplexDataType complexData(ComplexTransmuter transmuter, Object value) {
//        ComplexDataType data = WpsFactory.eINSTANCE.createComplexDataType();
//
//        data.setSchema(  transmuter.getSchema(this.request.getBaseUrl()));
//        data.setMimeType(transmuter.getMimeType());
//        data.getData().add(value);
//
//        return data;
//    }
//
//    private void processBrief() {
//        ProcessFactory     pf    = this.executor.getProcessFactory();
//        ProcessBriefType   brief = WpsFactory.eINSTANCE.createProcessBriefType();
//        LanguageStringType title = Ows11Factory.eINSTANCE.createLanguageStringType();
//
//        brief.setProcessVersion(pf.getVersion());
//        brief.setIdentifier(this.request.getIdentifier());
//        title.setValue(pf.getTitle().toString(this.locale));
//        brief.setTitle(title);
//
//        this.response.setProcess(brief);
//    }
//
//    private void status() {
//        StatusType status = WpsFactory.eINSTANCE.createStatusType();
//
//        status.setProcessSucceeded("Process completed successfully.");
//
//        XMLGregorianCalendar calendar;
//
//        try {
//            calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(
//                new GregorianCalendar(TimeZone.getTimeZone("UTC")));
//        } catch(Exception e) {
//            throw new WPSException("NoApplicableCode", e.getMessage());
//        }
//
//        status.setCreationTime(calendar);
//
//        this.response.setStatus(status);
//    }
}