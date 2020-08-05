package com.shawn.demo.controller;

import com.shawn.mvcFramework.annotations.Autowired;
import com.shawn.mvcFramework.annotations.Controller;
import com.shawn.mvcFramework.annotations.RequestMapping;
import com.shawn.demo.service.IDemoService;
import com.shawn.mvcFramework.annotations.Security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/demo")
@Security(httpMethodConstraints = {"zhangsan", "lisi"})
public class DemoController {

    @Autowired
    private IDemoService demoService;

    /**
     * url: /demo/query?name=lisi
     * @param request
     * @param response
     * @param name
     * @return
     */
    @RequestMapping("/query")
    @Security(httpMethodConstraints = {"zhaoliu", "wangwu"})
    public String query(HttpServletRequest request, HttpServletResponse response, String name) throws IOException {
        response.getWriter().write("hello, " + demoService.get(name) + "! Welcome Home!");
        return demoService.get(name);
    }

    /**
     * url: /demo/query1?name=adam
     * @param request
     * @param response
     * @param name
     * @return
     */
    @RequestMapping("/query1")
    @Security(httpMethodConstraints = {"adam", "bob"})
    public String query1(HttpServletRequest request, HttpServletResponse response, String name) throws IOException {
        response.getWriter().write("hello, " + demoService.get(name) + "! Welcome Home!");
        return demoService.get(name);
    }
}
