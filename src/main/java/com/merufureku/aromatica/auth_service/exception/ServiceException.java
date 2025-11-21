package com.merufureku.aromatica.auth_service.exception;

import com.merufureku.aromatica.auth_service.enums.CustomStatusEnums;

public class ServiceException extends RuntimeException{

    private final CustomStatusEnums customStatusEnums;

    public ServiceException(CustomStatusEnums customStatusEnums){
        super(customStatusEnums.getMessage());
        this.customStatusEnums = customStatusEnums;
    }

    public CustomStatusEnums getCustomStatusEnums(){
        return customStatusEnums;
    }

}
