package com.regtech.regcore.sierradevops.prometheus.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.regtech.regcore.sierradevops.prometheus.services.MetricsAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;


@Slf4j
@RestController
//@RequestMapping("/device-expiry")
public class App
{
    @Autowired
    MetricsAggregator aggregator;

    @GetMapping("/metrics")
    public ResponseEntity<String> deleteDeviceInfo()
    {
        return ResponseEntity.ok(aggregator.getBufferedOutput());
    }

}
