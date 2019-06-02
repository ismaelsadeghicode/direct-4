package com.dml.topup.dto.request;

import lombok.Data;

import java.util.Date;

@Data
public abstract class Request {

    Date postageDate = new Date();

}
