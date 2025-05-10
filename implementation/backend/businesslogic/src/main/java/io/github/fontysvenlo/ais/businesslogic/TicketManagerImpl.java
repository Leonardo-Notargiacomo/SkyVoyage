package io.github.fontysvenlo.ais.businesslogic;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.fontysvenlo.ais.businesslogic.api.TicketManager;
import io.github.fontysvenlo.ais.persistence.api.TicketRepository;
import io.github.fontysvenlo.ais.datarecords.TicketData;

public class TicketManagerImpl implements TicketManager {

    private static final Logger logger = Logger.getLogger(EmployeeManagerImpl.class.getName());
    private final TicketRepository ticketRepository;

    /**
     * Constructor
     *
     * @param ticketRepository the ticket storage service
     */
    public TicketManagerImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public ArrayList<ArrayList<String>> getTicketData(String id) {
        ArrayList<ArrayList<String>> ticketDataList = new ArrayList<>();

        try {
            // Retrieve ticket IDs for the given booking ID
            ArrayList<String> ticketIDs = new ArrayList<>(ticketRepository.getTicketID(id));

            for (String ticketID : ticketIDs) {
                // Fetch ticket data for each ticket ID
                TicketData data = ticketRepository.getTicketData(ticketID);

                if (data != null) {
                    ArrayList<String> singleTicket = new ArrayList<>();
                    singleTicket.add(data.flightNumber());
                    singleTicket.add(data.departureAirport());
                    singleTicket.add(data.departureTerminal());
                    singleTicket.add(data.departureGate());
                    singleTicket.add(data.departureTime());
                    singleTicket.add(String.valueOf(data.departureDelay()));
                    singleTicket.add(data.arrivalAirport());
                    singleTicket.add(data.arrivalTerminal());
                    singleTicket.add(data.arrivalGate());
                    singleTicket.add(data.arrivalTime());
                    singleTicket.add(String.valueOf(data.arrivalDelay()));
                    singleTicket.add(String.valueOf(data.hasSeat()));
                    singleTicket.add(data.firstName());
                    singleTicket.add(data.lastName());
                    singleTicket.add(data.email());
                    singleTicket.add(data.phoneNumber());
                    singleTicket.add(data.country());
                    singleTicket.add(data.city());
                    singleTicket.add(data.streetName());
                    singleTicket.add(data.houseNumber());

                    ticketDataList.add(singleTicket);
                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving ticket data list for booking ID: " + id, e);
        }

        return ticketDataList;
    }
}
