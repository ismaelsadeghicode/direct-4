package com.dml.topup.service;

import com.dml.topup.config.Constants;
import com.dml.topup.domain.Charge;
import com.dml.topup.dto.request.InquiryRequest;
import com.dml.topup.dto.request.topup.InquiryRequestTopup;
import com.dml.topup.dto.response.InquiryChargeResponse;
import com.dml.topup.dto.response.topup.InquiryChargeResponseTopup;
import com.dml.topup.dto.response.topup.Response;
import com.dml.topup.exception.ErrorCode;
import com.dml.topup.repository.LoggingRepository;
import com.dml.topup.util.MessageConstants;
import com.dml.topup.util.ObjectUtils;
import com.dml.topup.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@Component
public class InquiryService extends Service<InquiryChargeResponse> {

    private final LoggingRepository loggingRepository;

    @Autowired
    private LoggingRepository getLoggingRepository;

    @Autowired
    public InquiryService(LoggingRepository loggingRepository) {
        super(InquiryChargeResponse.class);
        this.loggingRepository = loggingRepository;
    }

    public Response<InquiryChargeResponseTopup> inquiryChargeRequest(InquiryRequestTopup request, String authorization) {
        Assert.notNull(request);
        Assert.notNull(authorization);
        Response<InquiryChargeResponseTopup> result = new Response<InquiryChargeResponseTopup>();
        InquiryChargeResponseTopup response = new InquiryChargeResponseTopup();
        result = cheackAuthentication(authorization);
        if (cheackAuthentication(authorization).getErrorDescription() != null) {
            return result;
        }

        InquiryRequest inquiryRequest = new InquiryRequest();
        inquiryRequest.setAuthValue(SecurityUtil.authValue);
        inquiryRequest.setUid(getEnv().getProperty(Constants.SETAREYEK_UID));

        int method = 0;
        String urlBuilder = null;

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
            Date dateRequest = dateFormat.parse(request.getTransactionDate());
            long dateRequestDate = dateRequest.getTime();

            Charge charge = getLoggingRepository.findByResNoAndRequestDateTopup(request.getTraceNo(), dateRequestDate);
            if (charge.getMessageId() != null) {
                inquiryRequest.setProviderID(charge.getMessageId());
                if (charge.getMethod() == 3) {
                    urlBuilder = createUrl(Constants.MCI_REST_GET_TRANSACTION_INQUIRY);
                } else {
                    urlBuilder = createUrl(Constants.MTN_RIGHTEL_REST_GET_TRANSACTION_INQUIRY);
                }
                inquiryRequest.setResNum(request.getTraceNo());

                Mono<InquiryChargeResponse> inquiryResponse = callWebService(HttpMethod.POST, urlBuilder, inquiryRequest);
                response.setResCode(Objects.requireNonNull(inquiryResponse.block()).getResCode());

                if (response.getResCode() != null) {
                    switch (response.getResCode()) {
                        case "0":
                            result.setSuccessful(Boolean.TRUE);
                            response.setAmount(Objects.requireNonNull(inquiryResponse.block()).getAmount());
                            response.setMethod(Objects.requireNonNull(inquiryResponse.block()).getMethod());
                            response.setRequestedDate(Objects.requireNonNull(inquiryResponse.block()).getRequestedDate());
                            response.setSubscriberNo(Objects.requireNonNull(inquiryResponse.block()).getPhoneNumber());
                            response.setResCode(null);
                            break;
                        case "-1":
                            response.setAmount("0");
                            response.setMethod(Objects.requireNonNull(inquiryResponse.block()).getMethod());
                            response.setRequestedDate(Objects.requireNonNull(inquiryResponse.block()).getRequestedDate());
                            response.setSubscriberNo(Objects.requireNonNull(inquiryResponse.block()).getPhoneNumber());
                            break;
                        case "-3":
                            result.setErrorCode(ErrorCode.UNKNOWN_EXCEPTION.getCode());
                            result.setErrorDescription(MessageConstants.THERE_IS_NO_SUCH_TRANSACTION);
                            break;
                    }
                } else {
                    response = getInquiryfromdb(inquiryRequest);
                }

            } else if (charge.getMessageId() == null && charge.getRequestDateTopup() != null) {
                Response responseError = new Response();
                responseError.setErrorCode(ErrorCode.TRANSACTION_FAILED.getCode());
                responseError.setErrorDescription(ErrorCode.TRANSACTION_FAILED.getDescription());
                return responseError;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Response responseError = new Response();
            responseError.setErrorCode(ErrorCode.DATA_NOT_FOUND_EXCEPTION.getCode());
            responseError.setErrorDescription(ErrorCode.DATA_NOT_FOUND_EXCEPTION.getDescription());
            return responseError;
        }

        result.setResponse(response);
        if (!result.isSuccessful()) {
            result.setResponse(null);
        }
        return result;
    }

    private InquiryChargeResponseTopup getInquiryfromdb(InquiryRequest request) {
        InquiryChargeResponseTopup responce = new InquiryChargeResponseTopup();
        Charge charge = loggingRepository.findByMessageId(request.getProviderID());
        if (ObjectUtils.isNotNull(charge)) {
            if (charge.getMessageId() != null && charge.getErrorCode() == null) {
                responce.setResCode(String.valueOf(charge.getResCodeOrder()));
                responce.setAmount(String.valueOf(charge.getAmount()));
                responce.setMethod(String.valueOf(charge.getMethod()));
                responce.setRequestedDate(String.valueOf(charge.getCreatedDate()));
                responce.setSubscriberNo(charge.getSubscriberNo());
//                if (charge.getErrorCode() != null) {
//                    responce.setErrorCode(charge.getErrorCode());
//                }
            }
        }
        return responce;
    }
}