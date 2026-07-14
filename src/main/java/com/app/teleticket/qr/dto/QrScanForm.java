package com.app.teleticket.qr.dto;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public class QrScanForm {

    @RestForm("qr")
    private FileUpload qr;

    public FileUpload getQr() {
        return qr;
    }

    public void setQr(FileUpload qr) {
        this.qr = qr;
    }
}