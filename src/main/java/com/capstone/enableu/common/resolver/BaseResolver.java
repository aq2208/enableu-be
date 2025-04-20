package com.capstone.enableu.common.resolver;


import com.capstone.enableu.common.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseResolver<S extends BaseService> {

    @Autowired
    protected S service;


}
