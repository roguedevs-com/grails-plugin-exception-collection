package com.roguedevs.grails.plugins.exceptionCollection

class ExceptionInfo {

    //User fields
    String userSession
    String ipAddress
    String appServer
    String operatingSystem
    String browserName
    String feedbackType = "EXCEPTION"          //reason we're here
    String uri              //request.'javax.servlet.error.request_uri'
    String userMessage
    String statusCode = 0
    String errorMessage          //request.'javax.servlet.error.message'

    //Exception specific fields
    String exceptionMessage //exception.message
    String causedBy         //exception.cause?.message
    String className        //exception.className
    String lineNumber       //exception.lineNumber
    String codeSnippet
    String stackTrace       //exception.codeSnippet.cs*

    Exception rootException

    static transients = ['rootException']

    static mapping = {
        columns {
            codeSnippet type: 'text'
            stackTrace type: 'text'
        }
    }
    static constraints = {
        userSession(nullable: true, maxSize: 200)
        ipAddress(nullable: true, maxSize: 20)
        appServer(nullable: true, maxSize: 200)
        operatingSystem(nullable: true, maxSize: 200)
        browserName(nullable: true, maxSize: 2000)
        uri(nullable: true, maxSize: 2000)
        feedbackType(nullable: true, maxSize: 4000)
        userMessage(nullable: true, maxSize: 4000)
        errorMessage(nullable: true, maxSize: 4000)
        exceptionMessage(nullable: true, maxSize: 4000)
        causedBy(nullable: true, maxSize: 4000)
        className(nullable: true, maxSize: 4000)
        lineNumber(nullable: true, maxSize: 4000)
        codeSnippet(nullable: true)
        stackTrace(nullable: true)

    }
}
