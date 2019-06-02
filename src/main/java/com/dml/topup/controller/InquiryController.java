package com.dml.topup.controller;

import com.dml.topup.config.Constants;
import com.dml.topup.dto.request.topup.InquiryRequestTopup;
import com.dml.topup.dto.response.topup.InquiryChargeResponseTopup;
import com.dml.topup.dto.response.topup.Response;
import com.dml.topup.service.InquiryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for call Transaction Inquiry servic.
 *
 * @author Ismael Sadeghi
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Api(value = "Inquiry", description = "REST API for Transaction Inquiry", tags = {"Transaction Inquiry"})
public class InquiryController {

    private InquiryService service;

    @Autowired
    public InquiryController(InquiryService service) {
        this.service = service;
    }

    @Async
    @ApiOperation(response = InquiryChargeResponseTopup.class, value = "inquiry charge")
    @PostMapping(value = "v1/inquiry", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public CompletableFuture<Response<InquiryChargeResponseTopup>> inquiryChargeRequest(@RequestHeader(value = Constants.AUTHORIZATION) String authorization,
                                                                                        @RequestBody @Valid InquiryRequestTopup request) {
        return CompletableFuture.completedFuture(service.inquiryChargeRequest(request, authorization));
    }

}
