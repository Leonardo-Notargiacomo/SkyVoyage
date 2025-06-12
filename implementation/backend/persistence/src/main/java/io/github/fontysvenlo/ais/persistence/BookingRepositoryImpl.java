package io.github.fontysvenlo.ais.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import io.github.fontysvenlo.ais.datarecords.BookingData;
import io.github.fontysvenlo.ais.datarecords.CustomerData;
import io.github.fontysvenlo.ais.persistence.api.BookingRepository;

class BookingRepositoryImpl implements BookingRepository {
    private final DataSource db;
    private static final int DEFAULT_EMPLOYEE_ID = 1;

    public BookingRepositoryImpl(DBConfig config) {
        this.db = DBProvider.getDataSource(config);
    }
    
    private List<String> extractAllFlightIds(BookingData bookingData) {
        List<String> flightIds = new ArrayList<>();
        flightIds.add(bookingData.flightId());
        return flightIds;
    }
    
    private void saveFlightIfNeeded(Connection connection, BookingData bookingData, String flightId) throws SQLException {
        if (flightExists(connection, flightId)) {
            return;
        }
        
        if (flightId.equals(bookingData.flightId())) {
            saveMainFlight(connection, bookingData);
        } else {
            String[] parts = flightId.split("-");
            if (parts.length >= 2) {
                insertFlightRecord(connection, flightId, 
                        "Airport " + parts[0], parts[0], null, null, null,
                        "Airport " + parts[1], parts[1], null, null, null, 180);
            } else {
                insertFlightRecord(connection, flightId, 
                        "Unknown Airport", "UNK", null, null, null,
                        "Unknown Airport", "UNK", null, null, null, 180);
            }
        }
    }
    
    private void saveMainFlight(Connection connection, BookingData bookingData) throws SQLException {
        String departureAirport = null;
        String departureAirportShort = null;
        String departureTerminal = null;
        String departureGate = null;
        java.sql.Timestamp departureTime = null;
        
        String arrivalAirport = null;
        String arrivalAirportShort = null;
        String arrivalTerminal = null;
        String arrivalGate = null;
        java.sql.Timestamp arrivalTime = null;
        
        Integer duration = null;
        
        try {
            if (bookingData.flight() != null) {
                departureAirport = bookingData.flight().departureAirport();
                departureAirportShort = bookingData.flight().departureAirportShort();
                departureTerminal = bookingData.flight().departureTerminal();
                departureGate = bookingData.flight().departureGate();
                duration = bookingData.flight().duration();
                
                if (bookingData.flight().departureScheduledTime() != null) {
                    departureTime = java.sql.Timestamp.valueOf(bookingData.flight().departureScheduledTime());
                }
                
                arrivalAirport = bookingData.flight().arrivalAirport();
                arrivalAirportShort = bookingData.flight().arrivalAirportShort();
                arrivalTerminal = bookingData.flight().arrivalTerminal();
                arrivalGate = bookingData.flight().arrivalGate();
                
                if (bookingData.flight().arrivalScheduledTime() != null) {
                    arrivalTime = java.sql.Timestamp.valueOf(bookingData.flight().arrivalScheduledTime());
                }
            }
        } catch (Exception e) {
            // Silent failure, continue with defaults
        }
        
        if (departureAirportShort == null || arrivalAirportShort == null) {
            String[] parts = bookingData.flightId().split("-");
            if (parts.length >= 2) {
                departureAirportShort = parts[0];
                departureAirport = departureAirport != null ? departureAirport : "Airport " + departureAirportShort;
                arrivalAirportShort = parts[1];
                arrivalAirport = arrivalAirport != null ? arrivalAirport : "Airport " + arrivalAirportShort;
            } else {
                departureAirport = "Unknown Departure Airport";
                departureAirportShort = "UNK";
                arrivalAirport = "Unknown Arrival Airport";
                arrivalAirportShort = "UNK";
            }
        }
        
        if (duration == null) {
            duration = 180;
        }
        
        insertFlightRecord(connection, bookingData.flightId(), 
                departureAirport, departureAirportShort, departureTerminal, departureGate, departureTime,
                arrivalAirport, arrivalAirportShort, arrivalTerminal, arrivalGate, arrivalTime, duration);
    }
    
    private void insertFlightRecord(Connection connection, String flightId,
            String departureAirport, String departureAirportShort, String departureTerminal,
            String departureGate, java.sql.Timestamp departureTime,
            String arrivalAirport, String arrivalAirportShort, String arrivalTerminal,
            String arrivalGate, java.sql.Timestamp arrivalTime, Integer duration) throws SQLException {
        
        String sql = "INSERT INTO public.flight (id, departure_airport, departure_airport_short, " +
                "departure_terminal, departure_gate, departure_scheduled_time, arrival_airport, " +
                "arrival_airport_short, arrival_terminal, arrival_gate, arrival_scheduled_time, " +
                "duration) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, flightId);
            stmt.setString(2, departureAirport);
            stmt.setString(3, departureAirportShort);
            
            if (departureTerminal != null) stmt.setString(4, departureTerminal);
            else stmt.setNull(4, java.sql.Types.VARCHAR);
            
            if (departureGate != null) stmt.setString(5, departureGate);
            else stmt.setNull(5, java.sql.Types.VARCHAR);
            
            if (departureTime != null) stmt.setTimestamp(6, departureTime);
            else stmt.setNull(6, java.sql.Types.TIMESTAMP);
            
            stmt.setString(7, arrivalAirport);
            stmt.setString(8, arrivalAirportShort);
            
            if (arrivalTerminal != null) stmt.setString(9, arrivalTerminal);
            else stmt.setNull(9, java.sql.Types.VARCHAR);
            
            if (arrivalGate != null) stmt.setString(10, arrivalGate);
            else stmt.setNull(10, java.sql.Types.VARCHAR);
            
            if (arrivalTime != null) stmt.setTimestamp(11, arrivalTime);
            else stmt.setNull(11, java.sql.Types.TIMESTAMP);
            
            stmt.setInt(12, duration);
            
            stmt.executeUpdate();
        }
    }

    private int parseDurationToMinutes(String duration) {
        int minutes = 0;
        
        try {
            String time = duration.substring(2);
            
            int hIndex = time.indexOf('H');
            if (hIndex > 0) {
                minutes += Integer.parseInt(time.substring(0, hIndex)) * 60;
                time = time.substring(hIndex + 1);
            }
            
            int mIndex = time.indexOf('M');
            if (mIndex > 0) {
                minutes += Integer.parseInt(time.substring(0, mIndex));
            }
        } catch (Exception e) {
            return 60;
        }
        
        return minutes > 0 ? minutes : 60;
    }
    
    private int createBooking(Connection connection) throws SQLException {
        String sql = "INSERT INTO public.booking (employeeid, isactive) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, DEFAULT_EMPLOYEE_ID);
            stmt.setBoolean(2, true);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating booking failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating booking failed, no ID obtained.");
                }
            }
        }
    }
    
    private boolean flightExists(Connection connection, String flightId) throws SQLException {
        String sql = "SELECT 1 FROM public.flight WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, flightId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    private void linkBookingToFlight(Connection connection, int bookingId, String flightId) throws SQLException {
        String sql = "INSERT INTO public.booking_flight (bookingid, flightid) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            stmt.setString(2, flightId);
            stmt.executeUpdate();
        }
    }
    
    private int createTicket(Connection connection, int customerId, String flightId) throws SQLException {
        String sql = "INSERT INTO public.ticket (flightid, customerid, tariff) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, flightId);
            stmt.setInt(2, customerId);
            stmt.setInt(3, 0);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating ticket failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating ticket failed, no ID obtained.");
                }
            }
        }
    }
    
    private int createAddress(Connection connection, CustomerData customer) throws SQLException {
        if (customer.street() == null || customer.street().isEmpty()) {
            return 0;
        }
        
        String sql = "INSERT INTO public.address (street, housenumber, city, country) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, customer.street());
            stmt.setString(2, customer.houseNumber());
            stmt.setString(3, customer.city());
            stmt.setString(4, customer.country());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating address failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating address failed, no ID obtained.");
                }
            }
        }
    }

    private Integer saveOrUpdateCustomer(Connection connection, CustomerData customer) throws SQLException {
        if (customer.id() != null && customer.id() > 0) {
            String checkSql = "SELECT id FROM public.customer WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(checkSql)) {
                stmt.setInt(1, customer.id());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        updateCustomer(connection, customer);
                        return customer.id();
                    }
                }
            }
        }

        int addressId = createAddress(connection, customer);
        
        String insertSql = "INSERT INTO public.customer (firstname, lastname, email, phonenumber, addressid, isinfant) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, customer.firstName());
            stmt.setString(2, customer.lastName());
            stmt.setString(3, customer.email());
            stmt.setString(4, customer.phone());
            
            if (addressId > 0) {
                stmt.setInt(5, addressId);
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            
            stmt.setBoolean(6, customer.isInfant());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating customer failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating customer failed, no ID obtained.");
                }
            }
        }
    }

    private void updateCustomer(Connection connection, CustomerData customer) throws SQLException {
        int addressId = 0;
        String checkAddressSql = "SELECT addressid FROM public.customer WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(checkAddressSql)) {
            stmt.setInt(1, customer.id());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Integer existingAddressId = rs.getInt("addressid");
                    if (existingAddressId != null && existingAddressId > 0) {
                        updateAddress(connection, existingAddressId, customer);
                        addressId = existingAddressId;
                    } else {
                        addressId = createAddress(connection, customer);
                    }
                }
            }
        }
        
        String sql = "UPDATE public.customer SET firstname = ?, lastname = ?, email = ?, phonenumber = ?, "
                + "addressid = ?, isinfant = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, customer.firstName());
            stmt.setString(2, customer.lastName());
            stmt.setString(3, customer.email());
            stmt.setString(4, customer.phone());
            
            if (addressId > 0) {
                stmt.setInt(5, addressId);
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            
            stmt.setBoolean(6, customer.isInfant());
            stmt.setInt(7, customer.id());
            stmt.executeUpdate();
        }
    }
    
    private void updateAddress(Connection connection, int addressId, CustomerData customer) throws SQLException {
        if (customer.street() == null || customer.street().isEmpty()) {
            return;
        }
        
        String sql = "UPDATE public.address SET street = ?, housenumber = ?, city = ?, country = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, customer.street());
            stmt.setString(2, customer.houseNumber());
            stmt.setString(3, customer.city());
            stmt.setString(4, customer.country());
            stmt.setInt(5, addressId);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<BookingData> list() {
        String sql = "SELECT b.id, b.isactive, bf.flightid, f.departure_airport, f.arrival_airport " +
                "FROM public.booking b " +
                "JOIN public.booking_flight bf ON b.id = bf.bookingid " +
                "JOIN public.flight f ON bf.flightid = f.id";

        try (Connection connection = db.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<BookingData> bookings = new ArrayList<>();

            while (rs.next()) {
                int bookingId = rs.getInt("id");
                bookings.add(buildBookingData(connection, bookingId, rs));
            }

            return bookings;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list bookings", e);
        }
    }

    @Override
    public BookingData getOne(Integer id) {
        String sql = "SELECT b.id, b.isactive, bf.flightid, f.departure_airport, f.arrival_airport " +
                "FROM public.booking b " +
                "JOIN public.booking_flight bf ON b.id = bf.bookingid " +
                "JOIN public.flight f ON bf.flightid = f.id " +
                "WHERE b.id = ?";

        try (Connection connection = db.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return buildBookingData(connection, id, rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get booking with ID " + id, e);
        }

        return null;
    }
    
    private BookingData buildBookingData(Connection connection, int bookingId, ResultSet rs) throws SQLException {
        String flightId = rs.getString("flightid");
        boolean isActive = rs.getBoolean("isactive");
        String departureAirport = rs.getString("departure_airport");
        String arrivalAirport = rs.getString("arrival_airport");
        
        List<CustomerData> customers = getCustomersForBooking(connection, bookingId);
        
        int adultCount = 0;
        int infantCount = 0;
        for (CustomerData customer : customers) {
            if (customer.isInfant()) {
                infantCount++;
            } else {
                adultCount++;
            }
        }
        
        return new BookingData(
                bookingId,
                flightId,
                "Unknown Airline",
                0.0,
                adultCount,
                infantCount,
                "ECONOMY",
                0,
                "",
                LocalDateTime.now(),
                isActive ? "ACTIVE" : "CANCELLED",
                customers
        );
    }

    @Override
    public List<BookingData> getByCustomerId(Integer customerId) {
        String sql = "SELECT DISTINCT b.id, b.isactive, bf.flightid, f.departure_airport, f.arrival_airport " +
                "FROM public.booking b " +
                "JOIN public.booking_flight bf ON b.id = bf.bookingid " +
                "JOIN public.flight f ON bf.flightid = f.id " +
                "JOIN public.ticket t ON f.id = t.flightid " +
                "WHERE t.customerid = ?";

        try (Connection connection = db.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<BookingData> bookings = new ArrayList<>();

                while (rs.next()) {
                    int bookingId = rs.getInt("id");
                    bookings.add(buildBookingData(connection, bookingId, rs));
                }

                return bookings;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get bookings for customer ID " + customerId, e);
        }
    }

    private List<CustomerData> getCustomersForBooking(Connection connection, int bookingId) throws SQLException {
        String sql = "SELECT c.*, a.street, a.housenumber, a.city, a.country " +
                "FROM public.customer c " +
                "LEFT JOIN public.address a ON c.addressid = a.id " +
                "JOIN public.ticket t ON c.id = t.customerid " +
                "JOIN public.booking_flight bf ON t.flightid = bf.flightid " +
                "WHERE bf.bookingid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<CustomerData> customers = new ArrayList<>();

                while (rs.next()) {
                    customers.add(new CustomerData(
                            rs.getInt("id"),
                            rs.getString("firstname") != null ? rs.getString("firstname") : "",
                            rs.getString("lastname") != null ? rs.getString("lastname") : "",
                            rs.getString("email") != null ? rs.getString("email") : "",
                            rs.getString("phonenumber") != null ? rs.getString("phonenumber") : "",
                            rs.getString("street") != null ? rs.getString("street") : "",
                            rs.getString("housenumber") != null ? rs.getString("housenumber") : "",
                            rs.getString("city") != null ? rs.getString("city") : "",
                            rs.getString("country") != null ? rs.getString("country") : "",
                            rs.getBoolean("isinfant")
                    ));
                }

                return customers;
            }
        }
    }

    @Override
    public Map<String, Object> add(Map<String, Object> bookingMap) {
        Connection connection = null;
        try {
            connection = db.getConnection();
            connection.setAutoCommit(false);

            String flightId = getSafeString(bookingMap, "flightId");
            String airline = getSafeString(bookingMap, "airline");
            int adultPassengers = getSafeInt(bookingMap, "adultPassengers", 1);
            int infantPassengers = getSafeInt(bookingMap, "infantPassengers", 0);
            
            List<String> flightIdsToLink = new ArrayList<>();
            
            List<Map<String, Object>> mainFlights = getSafeList(bookingMap, "mainFlights");
            if (mainFlights != null && !mainFlights.isEmpty()) {
                for (Map<String, Object> mainFlight : mainFlights) {
                    String mainFlightId = getSafeString(mainFlight, "id");
                    if (mainFlightId == null) {
                        String depCode = getSafeString(mainFlight, "departureAirportShort");
                        String arrCode = getSafeString(mainFlight, "arrivalAirportShort");
                        String flightNumber = getSafeString(mainFlight, "number", "0");
                        
                        if (depCode != null && arrCode != null) {
                            mainFlightId = flightNumber + "-" + depCode + "-" + arrCode;
                            mainFlight.put("id", mainFlightId);
                        }
                    }

                    if (mainFlightId != null) {
                        saveFlightMapIfNeeded(connection, mainFlight);
                        if (!flightIdsToLink.contains(mainFlightId)) {
                            flightIdsToLink.add(mainFlightId);
                        }
                    }
                }
            }
            
            Map<String, Object> outboundFlight = getSafeMap(bookingMap, "outboundFlight");
            Map<String, Object> returnFlight = getSafeMap(bookingMap, "returnFlight");
            
            if (outboundFlight != null) {
                saveFlightMapIfNeeded(connection, outboundFlight);
                String outboundId = getSafeString(outboundFlight, "id");
                if (outboundId != null && !flightIdsToLink.contains(outboundId)) {
                    flightIdsToLink.add(outboundId);
                }
            }
            
            if (returnFlight != null) {
                saveFlightMapIfNeeded(connection, returnFlight);
                String returnId = getSafeString(returnFlight, "id");
                if (returnId != null && !flightIdsToLink.contains(returnId)) {
                    flightIdsToLink.add(returnId);
                }
            }
            
            if (flightIdsToLink.isEmpty()) {
                Map<String, Object> singleFlight = getSafeMap(bookingMap, "flight");
                if (singleFlight != null) {
                    saveFlightMapIfNeeded(connection, singleFlight);
                    String singleFlightId = getSafeString(singleFlight, "id");
                    if (singleFlightId != null) {
                        flightIdsToLink.add(singleFlightId);
                    }
                } else if (flightId != null) {
                    saveBasicFlightIfNeeded(connection, flightId);
                    flightIdsToLink.add(flightId);
                }
            }
            
            List<Map<String, Object>> connectionFlights = getSafeList(bookingMap, "connectionFlights");
            if (connectionFlights != null) {
                for (Map<String, Object> connectionFlight : connectionFlights) {
                    saveFlightMapIfNeeded(connection, connectionFlight);
                    String connectionId = getSafeString(connectionFlight, "id");
                    if (connectionId != null && !flightIdsToLink.contains(connectionId)) {
                        flightIdsToLink.add(connectionId);
                    }
                }
            }
            
            if (flightIdsToLink.isEmpty()) {
                throw new SQLException("No valid flights found in booking data");
            }
            
            int bookingId = createBooking(connection);
            
            for (String id : flightIdsToLink) {
                linkBookingToFlight(connection, bookingId, id);
            }
            
            List<Map<String, Object>> customers = getSafeList(bookingMap, "customers");
            if (customers != null) {
                for (Map<String, Object> customer : customers) {
                    int customerId = saveCustomerMap(connection, customer);
                    
                    for (String id : flightIdsToLink) {
                        createTicket(connection, customerId, id);
                    }
                }
            }

            connection.commit();
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", bookingId);
            result.put("allFlightIds", flightIdsToLink);
            result.put("airline", airline);
            result.put("status", getSafeString(bookingMap, "status", "CONFIRMED"));
            result.put("adultPassengers", adultPassengers);
            result.put("infantPassengers", infantPassengers);
            
            return result;
            
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    // Silent rollback failure
                }
            }
            throw new RuntimeException("Failed to add booking", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    // Silent close failure
                }
            }
        }
    }

    private void saveFlightMapIfNeeded(Connection connection, Map<String, Object> flightMap) throws SQLException {
        String flightId = getSafeString(flightMap, "id");
        if (flightId == null) {
            String depCode = getSafeString(flightMap, "departureAirportShort");
            String arrCode = getSafeString(flightMap, "arrivalAirportShort");
            String flightNumber = getSafeString(flightMap, "number", "0");
            
            if (depCode != null && arrCode != null) {
                flightId = flightNumber + "-" + depCode + "-" + arrCode;
                flightMap.put("id", flightId);
            } else {
                return;
            }
        }
        
        if (flightExists(connection, flightId)) {
            return;
        }
        
        String departureAirport = getSafeString(flightMap, "departureAirport");
        String departureAirportShort = getSafeString(flightMap, "departureAirportShort");
        if (departureAirport == null && departureAirportShort != null) {
            departureAirport = "Airport " + departureAirportShort;
        }
        
        String departureTerminal = getSafeString(flightMap, "departureTerminal");
        String departureGate = getSafeString(flightMap, "departureGate");
        String departureScheduledTimeStr = getSafeString(flightMap, "departureScheduledTime");
        java.sql.Timestamp departureTime = parseTimestamp(departureScheduledTimeStr);
        
        String arrivalAirport = getSafeString(flightMap, "arrivalAirport");
        String arrivalAirportShort = getSafeString(flightMap, "arrivalAirportShort");
        if (arrivalAirport == null && arrivalAirportShort != null) {
            arrivalAirport = "Airport " + arrivalAirportShort;
        }
        
        String arrivalTerminal = getSafeString(flightMap, "arrivalTerminal");
        String arrivalGate = getSafeString(flightMap, "arrivalGate");
        String arrivalScheduledTimeStr = getSafeString(flightMap, "arrivalScheduledTime");
        java.sql.Timestamp arrivalTime = parseTimestamp(arrivalScheduledTimeStr);
        
        Integer duration = getSafeInt(flightMap, "duration", 180);
        
        insertFlightRecord(connection, flightId, 
                departureAirport, departureAirportShort, departureTerminal, departureGate, departureTime,
                arrivalAirport, arrivalAirportShort, arrivalTerminal, arrivalGate, arrivalTime, duration);
    }
    
    private void saveBasicFlightIfNeeded(Connection connection, String flightId) throws SQLException {
        if (flightId == null || flightId.isEmpty() || flightExists(connection, flightId)) {
            return;
        }
        
        String departureAirportShort = "UNK";
        String arrivalAirportShort = "UNK";
        
        String[] parts = flightId.split("-");
        if (parts.length >= 3) {
            departureAirportShort = parts[1];
            arrivalAirportShort = parts[2];
        } else if (parts.length >= 2) {
            departureAirportShort = parts[0];
            arrivalAirportShort = parts[1];
        }
        
        String departureAirport = "Airport " + departureAirportShort; 
        String arrivalAirport = "Airport " + arrivalAirportShort;
        
        insertFlightRecord(connection, flightId, 
                departureAirport, departureAirportShort, null, null, null,
                arrivalAirport, arrivalAirportShort, null, null, null, 180);
    }

    private boolean customerExists(Connection connection, int customerId) throws SQLException {
        String sql = "SELECT 1 FROM public.customer WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet results = stmt.executeQuery()) {
                return results.next();
            }
        }
    }

    private Integer getCustomerAddressId(Connection connection, int customerId) throws SQLException {
        String sql = "SELECT addressid FROM public.customer WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet results = stmt.executeQuery()) {
                if (results.next()) {
                    int addressId = results.getInt("addressid");
                    return results.wasNull() ? null : addressId;
                }
                return null;
            }
        }
    }

    private void updateCustomerMap(Connection connection, Map<String, Object> customerMap) throws SQLException {
        Integer customerId = getSafeInt(customerMap, "id", null);
        if (customerId == null) {
            return;
        }

        Integer existingAddressId = getCustomerAddressId(connection, customerId);
        int addressId = 0;

        String street = getSafeString(customerMap, "street");
        String houseNumber = getSafeString(customerMap, "houseNumber");
        String city = getSafeString(customerMap, "city");
        String country = getSafeString(customerMap, "country");

        if (street != null && !street.isEmpty()) {
            if (existingAddressId != null && existingAddressId > 0) {
                updateAddressSimple(connection, existingAddressId, street, houseNumber, city, country);
                addressId = existingAddressId;
            } else {
                addressId = createAddressSimple(connection, street, houseNumber, city, country);
            }
        }

        String firstName = getSafeString(customerMap, "firstName");
        String lastName = getSafeString(customerMap, "lastName");
        String email = getSafeString(customerMap, "email");
        String phone = getSafeString(customerMap, "phone");
        boolean isInfant = getSafeBoolean(customerMap, "isInfant", false);

        String sql = "UPDATE public.customer SET firstname = ?, lastname = ?, email = ?, phonenumber = ?, " +
                "addressid = ?, isinfant = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, phone);

            if (addressId > 0) {
                stmt.setInt(5, addressId);
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }

            stmt.setBoolean(6, isInfant);
            stmt.setInt(7, customerId);
            stmt.executeUpdate();
        }
    }
    
    private int saveCustomerMap(Connection connection, Map<String, Object> customerMap) throws SQLException {
        Integer customerId = getSafeInt(customerMap, "id", null);
        if (customerId != null && customerExists(connection, customerId)) {
            updateCustomerMap(connection, customerMap);
            return customerId;
        }
        
        String email = getSafeString(customerMap, "email");
        if (email != null && !email.isEmpty()) {
            Map<String, Object> existingCustomer = findCustomerByEmail(email);
            if (existingCustomer != null) {
                customerId = (Integer) existingCustomer.get("id");
                customerMap.put("id", customerId);
                updateCustomerMap(connection, customerMap);
                return customerId;
            }
        }
        
        int addressId = 0;
        String street = getSafeString(customerMap, "street");
        String houseNumber = getSafeString(customerMap, "houseNumber");
        String city = getSafeString(customerMap, "city");
        String country = getSafeString(customerMap, "country");
        
        if (street != null && !street.isEmpty()) {
            Map<String, Object> existingAddress = findAddressByComponents(street, houseNumber, city, country);
            
            if (existingAddress != null) {
                addressId = (Integer) existingAddress.get("id");
            } else {
                addressId = createAddressSimple(connection, street, houseNumber, city, country);
            }
        }
        
        String firstName = getSafeString(customerMap, "firstName");
        String lastName = getSafeString(customerMap, "lastName");
        boolean isInfant = getSafeBoolean(customerMap, "isInfant", false);
        
        return createCustomerSimple(connection, firstName, lastName, email, getSafeString(customerMap, "phone"), addressId, isInfant);
    }

    private String getSafeString(Map<String, Object> map, String key) {
        return getSafeString(map, key, null);
    }
    
    private String getSafeString(Map<String, Object> map, String key, String defaultValue) {
        if (map == null || !map.containsKey(key)) return defaultValue;
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    private Integer getSafeInt(Map<String, Object> map, String key, Integer defaultValue) {
        if (map == null || !map.containsKey(key)) return defaultValue;
        Object value = map.get(key);
        if (value == null) return defaultValue;
        
        try {
            if (value instanceof Number) {
                return ((Number)value).intValue();
            } else {
                return Integer.parseInt(value.toString());
            }
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    private boolean getSafeBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        if (map == null || !map.containsKey(key)) return defaultValue;
        Object value = map.get(key);
        if (value == null) return defaultValue;
        
        if (value instanceof Boolean) {
            return (Boolean)value;
        } else {
            String strValue = value.toString().toLowerCase();
            return "true".equals(strValue) || "yes".equals(strValue) || "1".equals(strValue);
        }
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> getSafeMap(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) return null;
        Object value = map.get(key);
        return value instanceof Map ? (Map<String, Object>)value : null;
    }
    
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getSafeList(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) return null;
        Object value = map.get(key);
        if (!(value instanceof List)) return null;
        
        List<Object> list = (List<Object>)value;
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Object item : list) {
            if (item instanceof Map) {
                result.add((Map<String, Object>)item);
            }
        }
        
        return result;
    }

    private java.sql.Timestamp parseTimestamp(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return null;
        
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr);
            return java.sql.Timestamp.valueOf(dateTime);
        } catch (Exception e) {
            return null;
        }
    }
    
    private int createAddressSimple(Connection connection, String street, String houseNumber, 
                                   String city, String country) throws SQLException {
        Map<String, Object> existingAddress = findAddressByComponents(street, houseNumber, city, country);
        if (existingAddress != null) {
            return (Integer) existingAddress.get("id");
        }
        
        String sql = "INSERT INTO public.address (street, housenumber, city, country) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, street);
            stmt.setString(2, houseNumber);
            stmt.setString(3, city);
            stmt.setString(4, country);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating address failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating address failed, no ID obtained.");
                }
            }
        }
    }
    
    private void updateAddressSimple(Connection connection, int addressId, String street, String houseNumber, 
                                    String city, String country) throws SQLException {
        String sql = "UPDATE public.address SET street = ?, housenumber = ?, city = ?, country = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, street);
            stmt.setString(2, houseNumber);
            stmt.setString(3, city);
            stmt.setString(4, country);
            stmt.setInt(5, addressId);
            stmt.executeUpdate();
        }
    }
    
    private int createCustomerSimple(Connection connection, String firstName, String lastName,
                                    String email, String phone, int addressId, boolean isInfant) throws SQLException {
        String sql = "INSERT INTO public.customer (firstname, lastname, email, phonenumber, addressid, isinfant) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, phone);
            
            if (addressId > 0) {
                stmt.setInt(5, addressId);
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            
            stmt.setBoolean(6, isInfant);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating customer failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating customer failed, no ID obtained.");
                }
            }
        }
    }

    @Override
    public Map<String, Object> findCustomerByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        
        String sql = "SELECT c.*, a.street, a.housenumber, a.city, a.country " +
                     "FROM public.customer c " +
                     "LEFT JOIN public.address a ON c.addressid = a.id " +
                     "WHERE LOWER(c.email) = LOWER(?)";
        
        try (Connection connection = db.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, email.trim().toLowerCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> customerData = new HashMap<>();
                    customerData.put("id", rs.getInt("id"));
                    customerData.put("firstName", rs.getString("firstname"));
                    customerData.put("lastName", rs.getString("lastname"));
                    customerData.put("email", rs.getString("email"));
                    customerData.put("phone", rs.getString("phonenumber"));
                    customerData.put("isInfant", rs.getBoolean("isinfant"));
                    
                    if (rs.getObject("street") != null) {
                        customerData.put("street", rs.getString("street"));
                        customerData.put("houseNumber", rs.getString("housenumber"));
                        customerData.put("city", rs.getString("city"));
                        customerData.put("country", rs.getString("country"));
                    }
                    
                    return customerData;
                }
            }
            
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find customer by email", e);
        }
    }

    private Map<String, Object> findAddressByComponents(String street, String houseNumber, String city, String country) {
        if (street == null || houseNumber == null || city == null || country == null) {
            return null;
        }
        
        String sql = "SELECT * FROM public.address " +
                     "WHERE LOWER(street) = LOWER(?) " +
                     "AND LOWER(housenumber) = LOWER(?) " +
                     "AND LOWER(city) = LOWER(?) " +
                     "AND LOWER(country) = LOWER(?)";
        
        try (Connection connection = db.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, street.trim().toLowerCase());
            stmt.setString(2, houseNumber.trim().toLowerCase());
            stmt.setString(3, city.trim().toLowerCase());
            stmt.setString(4, country.trim().toLowerCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> addressData = new HashMap<>();
                    addressData.put("id", rs.getInt("id"));
                    addressData.put("street", rs.getString("street"));
                    addressData.put("houseNumber", rs.getString("housenumber"));
                    addressData.put("city", rs.getString("city"));
                    addressData.put("country", rs.getString("country"));
                    
                    return addressData;
                }
            }
            
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find address", e);
        }
    }
}
