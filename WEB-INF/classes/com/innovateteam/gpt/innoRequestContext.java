/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.innovateteam.gpt; 
 
import com.esri.gpt.framework.context.RequestContext;      
import com.esri.gpt.framework.security.principal.User;  
import javax.servlet.ServletRequest;         
 
/**
 *
 * @author Deewen
 */
public class innoRequestContext extends RequestContext{
    
    public innoRequestContext(ServletRequest request){
        super(request);
    }
    
   
}
