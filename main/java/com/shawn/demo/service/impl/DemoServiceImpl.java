package com.shawn.demo.service.impl;

import com.shawn.mvcFramework.annotations.Service;
import com.shawn.demo.service.IDemoService;

@Service("demoService")
public class DemoServiceImpl implements IDemoService {

    @Override
    public String get(String name) {
        System.out.println("Service params" + name);
        return name;
    }
}
