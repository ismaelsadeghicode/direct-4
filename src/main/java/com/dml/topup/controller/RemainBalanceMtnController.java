package com.dml.topup.controller;

import com.dml.topup.config.Constants;
import com.dml.topup.dto.response.RemainedBalanceResponse;
import com.dml.topup.dto.response.topup.Response;
import com.dml.topup.service.RemainBalanceMtnRightelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * Controller for call Remain Balance Mtn servic.
 *
 * @author Ismael Sadeghi
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Api(value = "Balance", description = "REST API for Remain Balance Mtn", tags = {"Remain Balance Mtn"})
public class RemainBalanceMtnController {

    private RemainBalanceMtnRightelService service;

    @Autowired
    public RemainBalanceMtnController(RemainBalanceMtnRightelService service) {
        this.service = service;
    }

    @Async
    @ApiOperation(response = RemainedBalanceResponse.class, value = "Remain Balance Mtn")
    @GetMapping(value = "v1/remain-balance-mtn", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public CompletableFuture<Response<RemainedBalanceResponse>> remainedBalance(@RequestHeader(value = Constants.AUTHORIZATION) String authorization) {
        Response<RemainedBalanceResponse> result = service.remainedBalanceMtn(authorization);
        return CompletableFuture.completedFuture(result);
    }
}
