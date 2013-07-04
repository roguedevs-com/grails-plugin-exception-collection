package com.roguedevs.grails.plugins.exceptionCollection

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.codehaus.groovy.grails.web.errors.GrailsExceptionResolver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.servlet.ModelAndView


class ExceptionCollectionHandler extends GrailsExceptionResolver {


    private static final Logger log = LoggerFactory.getLogger(ExceptionCollectionHandler)

    def grailsApplication
    def exceptionCollectionService

    ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {

        if (log.debugEnabled) {
            log.debug("resolveException(request=${request}, response=${response}, handler=${handler}, exception=${exception})")
        }

        def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.RenderTagLib')
        g.metaClass.prettyPrintStatus = { return '' }

        try {

            mailService.sendMail {
        					multipart true
        					to adminEmail
        					from fromEmail
        					subject "Unhandled exception in the ${GrailsUtil.environment} environment"
        					html g.renderException(exception: exception)
        				}


            //record in DB, email or log the error
            exceptionCollectionService.capture()


        } catch (Exception e) {
            if (log.errorEnabled) {
                log.error("could not send email after exception", e)
            }
        }

        return super.resolveException(request, response, handler, exception)
    }
}
