package com.internal.exceptions.error.custom;

public class MasterDataServiceException extends RuntimeException {

    public MasterDataServiceException() {
        super();
    }

    public MasterDataServiceException(String message) {
        super(message);
    }

    public MasterDataServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public MasterDataServiceException(Throwable cause) {
        super(cause);
    }
}
