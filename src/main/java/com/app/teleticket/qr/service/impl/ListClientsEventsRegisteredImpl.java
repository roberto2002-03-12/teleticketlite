package com.app.teleticket.qr.service.impl;

import com.app.teleticket.events.dto.EventObjForQrList;
import com.app.teleticket.events.repository.EventRepository;
import com.app.teleticket.qr.dto.MyInscriptionsDTO;
import com.app.teleticket.qr.entity.QrTicketEntity;
import com.app.teleticket.qr.repository.QrTicketRepository;
import com.app.teleticket.users.exception.UserException;
import com.app.teleticket.users.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class ListClientsEventsRegisteredImpl implements com.app.teleticket.qr.service.ListClientsEventsRegistered {

    @Inject
    EventRepository eventRepository;

    @Inject
    QrTicketRepository qrTicketRepository;

    @Inject
    UserRepository userRepository;

    @Override
    public List<MyInscriptionsDTO> getMyInscriptions(String email) {
        Integer userId = this.userRepository.findByEmailAndReturnOnlyUsersId(email).orElse(null);

        if (Objects.isNull(userId))
            throw new UserException(404, "User not found");

        List<QrTicketEntity> tickets = this.qrTicketRepository.listTicketsByUserId(userId);

        if (tickets.isEmpty()) return new ArrayList<>();

        List<Integer> eventsIds = tickets.stream().map(QrTicketEntity::getEventId).toList();

        List<EventObjForQrList> eventObjForQrLists = this.eventRepository.listEventsNameAndAddress(eventsIds);

        Map<Integer, EventObjForQrList> eventIdEventObjMap = eventObjForQrLists
                .stream()
                .collect(
                        Collectors.toMap(EventObjForQrList::getId, e -> e)
                );

        List<MyInscriptionsDTO> myInscriptionsDTOs = new ArrayList<>();

        tickets.forEach(ticket -> {
            EventObjForQrList eventObj = eventIdEventObjMap.get(ticket.getEventId());
            MyInscriptionsDTO myInscriptionsDTO = new MyInscriptionsDTO(
                    ticket.getQrUrl(),
                    ticket.isAlreadyApplied(),
                    eventObj.getEventName(),
                    eventObj.getEventAddress(),
                    eventObj.getStartDate(),
                    eventObj.getFinishDate()
            );
            myInscriptionsDTOs.add(myInscriptionsDTO);
        });

        return myInscriptionsDTOs;
    }
}
