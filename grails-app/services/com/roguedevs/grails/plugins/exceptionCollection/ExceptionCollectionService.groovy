package com.roguedevs.grails.plugins.exceptionCollection

import grails.util.GrailsUtil
import grails.util.Holders
import org.springframework.dao.DataAccessException

class ExceptionCollectionService {

    def mailService

    def capture(request, session, exception) {

        ExceptionInfo exceptionInfo = createExceptionInfo(request, session, exception)

        //sendMail
        sendMail(exceptionInfo)

        //persist
        persist(exceptionInfo)

        //debug logging
        dumpALog(exceptionInfo)

    }

    private createExceptionInfo(request, session, exception) {
        ExceptionInfo exceptionInfo = new ExceptionInfo()

        exceptionInfo.rootException = exception

        exceptionInfo.userSession = session?.id
        exceptionInfo.ipAddress = request?.getRemoteAddr()
        exceptionInfo.appServer = InetAddress?.getLocalHost()?.getHostName()
        exceptionInfo.operatingSystem = 'seeBrowserInfo'
        exceptionInfo.browserName = request.getHeader("User-Agent")

        exceptionInfo.uri = request.'javax.servlet.error.request_uri'
        exceptionInfo.statusCode = request.'javax.servlet.error.status_code' ?: 0
        exceptionInfo.errorMessage = request.'javax.servlet.error.message'

        if (exception) {
            exceptionInfo.feedbackType = 'EXCEPTION'
            exceptionInfo.exceptionMessage = exception.message
            exceptionInfo.causedBy = exception.cause?.message
            exceptionInfo.className = exception.className
            exceptionInfo.lineNumber = exception.lineNumber
            exceptionInfo.codeSnippet = getAllLines(exception.codeSnippet)
            exceptionInfo.stackTrace = getAllLines(exception.stackTraceLines)
        }


        return exceptionInfo
    }

    private String getAllLines(cs) {
        StringBuffer sb = new StringBuffer()
        cs.each {
            sb.append("\n ${it}")
        }
        sb.toString()
    }

    private sendMail(ExceptionInfo exceptionInfo) {
        if (!Holders.config.exceptionCollection.email.send) {
            return
        }

        def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.RenderTagLib')
        g.metaClass.prettyPrintStatus = { return '' }

        try {
            mailService.sendMail {
                multipart true
                to Holders.config.exceptionCollection.email.to
                from Holders.config.exceptionCollection.email.from
                subject "Unhandled exception in the ${GrailsUtil.environment} environment"
                html g.renderException(exception: exception)
            }
        } catch (Exception e) {
            log.error("Unable to send email after exception", e)
        }
    }

    private persist(ExceptionInfo exceptionInfo) {
        if (!Holders.config.exceptionCollection.info.persist) {
            return
        }

        try {
            if (!exceptionInfo.save(flush: true)) {
                exceptionInfo.discard()
            }
        } catch (DataAccessException e) {
            log.error('Unable to persist after exception', e)
        }

    }

    private dumpALog(ExceptionInfo exceptionInfo) {
        //log additional information if debug logging is enabled

        def logMsg = """Error ${exceptionCollection?.statusCode}: ${exceptionCollection?.errorMessage}
URI: ${exceptionCollection?.uri}
        """
        log.debug(logMsg)

        //we may have multiple types
        if (exceptionCollection.feedbackType == 'EXCEPTION') {
            logMsg = """Exception Message: ${error?.exceptionMessage}
Caused by: ${exceptionCollection?.causedBy}
Class: ${exceptionCollection?.className}
At Line: ${exceptionCollection?.lineNumber}
Code Snippet: ${exceptionCollection?.codeSnippet}
Stack Trace: ${exceptionCollection?.stackTrace}
"""
            log.debug(logMsg)
        }

    }

}
