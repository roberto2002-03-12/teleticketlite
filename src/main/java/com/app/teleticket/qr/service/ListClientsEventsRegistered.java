package com.app.teleticket.qr.service;

import com.app.teleticket.qr.dto.MyInscriptionsDTO;

import java.util.List;

public interface ListClientsEventsRegistered {
    List<MyInscriptionsDTO> getMyInscriptions(String email);
}
