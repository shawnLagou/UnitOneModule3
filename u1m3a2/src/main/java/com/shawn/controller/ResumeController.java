package com.shawn.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.jdbc.StringUtils;
import com.shawn.dao.ResumeDao;
import com.shawn.pojo.Resume;
import com.shawn.pojo.User;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("/jpa")
public class ResumeController {

    @Autowired
    private ResumeDao resumeDao;

    @RequestMapping("/login")
    @ResponseBody
    public ModelAndView login(User user, HttpSession session) {
        if (user != null && "admin".equalsIgnoreCase(user.getUsername()) && "admin".equalsIgnoreCase(user.getPassword())) {
            session.setAttribute("USER_SESSION",user);
            return new ModelAndView("redirect:/jpa/query");
        } else {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("index");
            return modelAndView;
        }
    }

    @RequestMapping(value = "/query")
    @ResponseBody
    public ModelAndView query(HttpServletRequest request) throws Exception {
        List<Resume> all = resumeDao.findAll();
        ModelAndView modelAndView = new ModelAndView();
        request.setAttribute("datasList", all);
        modelAndView.setViewName("operation");
        return modelAndView;
    }

    @RequestMapping(value = "/save" , method = RequestMethod.POST)
    @ResponseBody
    public Resume save(@RequestBody Resume resume) {
        return resumeDao.save(resume);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @ResponseBody
    public void delete(@RequestBody Resume resume) {
        resumeDao.delete(resume);
    }

}
