package com.dml.topup.service;

import com.dml.topup.config.Constants;
import com.dml.topup.domain.Charge;
import com.dml.topup.dto.request.ChargeRequest;
import com.dml.topup.dto.request.SubmitRequest;
import com.dml.topup.dto.request.topup.ChargeRequestTopup;
import com.dml.topup.dto.response.ChargeResponse;
import com.dml.topup.dto.response.topup.ChargeResponseTopup;
import com.dml.topup.dto.response.topup.Response;
import com.dml.topup.exception.ErrorCode;
import com.dml.topup.repository.LoggingRepository;
import com.dml.topup.util.MessageConstants;
import com.dml.topup.util.ObjectUtils;
import com.dml.topup.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

/**
 * Servic for direct com.dml.topup.
 *
 * @author Ismael Sadeghi
 */
@Component
public class DirectTopupService extends Service<ChargeResponse> {

    private static final Logger log = LoggerFactory.getLogger(DirectTopupService.class);
    private final LoggingRepository loggingRepository;
    private int responseCodeOrder;
    private int responseCodeSubmit;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");


    @Autowired
    public DirectTopupService(LoggingRepository loggingRepository) {
        super(ChargeResponse.class);
        this.loggingRepository = loggingRepository;
    }

    public Response<ChargeResponseTopup> charge(ChargeRequestTopup request, String authorization) {
        Assert.notNull(authorization);
        Assert.notNull(request);
        Response<ChargeResponseTopup> result;

        result = cheackAuthentication(authorization);
        if (cheackAuthentication(authorization).getErrorDescription() != null) {
            return result;
        }

        try {
            if (dateFormat.parse(request.getCurrentDateTime()).getTime() < (new Date()).getTime()) {
                Response responseError = new Response();
                responseError.setErrorCode(ErrorCode.INVALID_DATE.getCode());
                responseError.setErrorDescription(ErrorCode.INVALID_DATE.getDescription());
                return responseError;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Response responseError = new Response();
            responseError.setErrorCode(ErrorCode.DATA_NOT_FOUND_EXCEPTION.getCode());
            responseError.setErrorDescription(ErrorCode.DATA_NOT_FOUND_EXCEPTION.getDescription());
            return responseError;
        }
        ChargeResponseTopup response = new ChargeResponseTopup();
        ChargeRequest chargeRequest = new ChargeRequest();
        chargeRequest.setUid(getEnv().getProperty(Constants.SETAREYEK_UID));
        chargeRequest.setAuthValue(SecurityUtil.authValue);
        chargeRequest.setOrderType(Constants.ORDER_TYPE_VALUE);
        chargeRequest.setPhoneDialer(request.getSubscriberNo());
        chargeRequest.setPhoneToCharge(request.getSubscriberNo());
        chargeRequest.setResNum(request.getTraceNo());
        chargeRequest.setAmount(request.getAmount());
        chargeRequest.setMethod(request.getMethod());
        String urlBuilder;
        String urlMciSubmit;
        if (request.getMethod() == 3) {
            urlBuilder = createUrl(Constants.MCI_REST_ORDER_URL);
            urlMciSubmit = createUrl(Constants.MCI_REST_SUBMIT_URL);
        } else if (request.getMethod() == 21 || request.getMethod() == 22 ||
                request.getMethod() == 41 || request.getMethod() == 42) {
            urlBuilder = createUrl(Constants.MTN_RIGHTEL_REST_ORDER_URL);
            urlMciSubmit = createUrl(Constants.MTN_RIGHTEL_REST_SUBMIT_URL);
        } else {
            result.setErrorCode(ErrorCode.BAD_REQUEST_EXCEPTION.getCode());
            result.setErrorDescription(ErrorCode.BAD_REQUEST_EXCEPTION.getDescription());
            result.setResponse(null);
            return result;
        }
        Mono<ChargeResponse> orderResponse = callWebService(HttpMethod.POST, urlBuilder, chargeRequest);
        if (ObjectUtils.isNotNull(orderResponse)) {
            responseCodeOrder = Objects.requireNonNull(orderResponse.block()).getResCode();
            response.setTraceNo(chargeRequest.getResNum());
            Mono<ChargeResponse> resultRestSubmit;

            if (responseCodeOrder == 0) {
                SubmitRequest submitRequest = new SubmitRequest();
                submitRequest.setAuthValue(SecurityUtil.authValue);
                submitRequest.setUid(getEnv().getProperty(Constants.SETAREYEK_UID));
                submitRequest.setProviderID(Objects.requireNonNull(Objects.requireNonNull(orderResponse.block()).getProviderId()));

                resultRestSubmit = callWebService(HttpMethod.POST, urlMciSubmit, submitRequest);
                responseCodeSubmit = Objects.requireNonNull(resultRestSubmit.block()).getResCode();
                if (responseCodeSubmit == 0) {
                    result.setSuccessful(Boolean.TRUE);
                    response.setResDescription(Objects.requireNonNull(resultRestSubmit.block()).getResDescription());
                    response.setTransactionId(submitRequest.getProviderID());

                } else if (responseCodeSubmit == -27 ||
                        responseCodeSubmit == -7) {
                    result.setSuccessful(Boolean.TRUE);
                    response.setTransactionId(submitRequest.getProviderID());
                    response.setResDescription(MessageConstants.SUCCESSFULLY_CHARGED + request.getAmount());
                } else {
                    result.setErrorCode(ErrorCode.BAD_REQUEST_EXCEPTION.getCode());
                    result.setErrorDescription(ErrorCode.BAD_REQUEST_EXCEPTION.getDescription());
                }
            } else {
                result.setErrorCode(ErrorCode.BAD_REQUEST_EXCEPTION.getCode());
                result.setErrorDescription(ErrorCode.BAD_REQUEST_EXCEPTION.getDescription());
            }
        } else {
            result.setErrorCode(ErrorCode.UNKNOWN_EXCEPTION.getCode());
            result.setErrorDescription(ErrorCode.UNKNOWN_EXCEPTION.getDescription());
        }

        mapToDB(chargeRequest, request, response);

        result.setResponse(response);
        if (!result.isSuccessful()) {
            result.setResponse(null);
        }
        return result;
    }

    private void mapToDB(ChargeRequest chargeRequest, ChargeRequestTopup request, ChargeResponseTopup response) {
        Charge charge = new Charge();
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            LocalDateTime dateTime = LocalDateTime.parse(request.getCurrentDateTime(), DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss"));
            LocalDate localDate = dateTime.toLocalDate();
            charge.setAmount(request.getAmount());
            charge.setOrderType(chargeRequest.getOrderType());
            charge.setMethod(request.getMethod());
            charge.setSubscriberNo(chargeRequest.getPhoneDialer());
            charge.setResNo(request.getTraceNo());
            charge.setResCodeOrder(responseCodeOrder);
            charge.setResCodeSubmit(responseCodeSubmit);
            charge.setPostageDate(request.getPostageDate());
            charge.setRequestDateTimeTopup(dateFormat.parse(request.getCurrentDateTime()).getTime());
            charge.setRequestDateTopup(format.parse(localDate.toString()).getTime());
            if (responseCodeSubmit == 0) {
                charge.setMessageId(response.getTransactionId());
            }
            save(charge);
        } catch (ParseException e) {
            log.info(String.format("the entered data format is not valid : %s", request.getCurrentDateTime()));
            save(charge);
            e.printStackTrace();
        }
    }

    @Transactional
    @Async
    public void save(Charge request) {
        loggingRepository.save(request);
    }

}