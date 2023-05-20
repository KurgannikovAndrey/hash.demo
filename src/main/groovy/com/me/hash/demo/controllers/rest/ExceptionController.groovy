package com.me.hash.demo.controllers.rest

import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest

@Controller
class ExceptionController extends AbstractErrorController{
    ExceptionController(ErrorAttributes errorAttributes) {
        super(errorAttributes)
    }

    @RequestMapping('${server.error.path:${error.path:/error}}')
    @GetMapping
    public ModelAndView exceptionHandler(HttpServletRequest httpRequest){
        new ModelAndView("error").with {
            it.addObject(
                    'error',
                    getErrorAttributes(httpRequest, ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE)).get('message')
            )
        }
    }


}
