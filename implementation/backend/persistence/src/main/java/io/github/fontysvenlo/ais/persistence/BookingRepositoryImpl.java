package io.github.fontysvenlo.ais.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import io.github.fontysvenlo.ais.datarecords.BookingData;
import io.github.fontysvenlo.ais.datarecords.CustomerData;
import io.github.fontysvenlo.ais.datarecords.FlightData;
import io.github.fontysvenlo.ais.persistence.api.BookingRepository;

class BookingRepositoryImpl implements BookingRepository {
    private static final Logger LOGGER = Logger.getLogger(BookingRepositoryImpl.class.getName());
    private final DataSource db;
    // Default employee ID to use if none provided - in a real app, this would come from auth context
    private static final int DEFAULT_EMPLOYEE_ID = 1;

    public BookingRepositoryImpl(DBConfig config) {
        this.db = DBProvider.getDataSource(config);
    }

    @Override
    public BookingData add(BookingData bookingData) {
        Connection connection = null;
        try {
            connection = db.getConnection();
            connection.setAutoCommit(false);  // Start transaction

            // 1. Save the main flight if provided
            if (bookingData.flight() != null) {
                saveFlightIfNeeded(connection, bookingData.flight());
            }
            
            // 2. Save any connection flights if provided
            if (bookingData.connectionFlights() != null && !bookingData.connectionFlights().isEmpty()) {
                for (FlightData connectionFlight : bookingData.connectionFlights()) {
                    saveFlightIfNeeded(connection, connectionFlight);
                }
            }
            
            // 3. Create booking record
            int bookingId = createBooking(connection);
            
            // 4. Link the main flight to the booking
            linkBookingToFlight(connection, bookingId, bookingData.flightId());
            
            // 5. Link any connection flights to the booking
            if (bookingData.connectionFlights() != null) {
                for (FlightData connectionFlight : bookingData.connectionFlights()) {
                    linkBookingToFlight(connection, bookingId, connectionFlight.id());
                }
            }
            
            // 6. Create tickets for each passenger for each flight
            if (bookingData.customers() != null) {
                for (CustomerData customer : bookingData.customers()) {
                    Integer customerId = saveOrUpdateCustomer(connection, customer);
                    
                    // Main flight ticket
                    createTicket(connection, customerId, bookingData.flightId());
                    
                    // Connection flight tickets
                    if (bookingData.connectionFlights() != null) {
                        for (FlightData connectionFlight : bookingData.connectionFlights()) {
                            createTicket(connection, customerId, connectionFlight.id());
                        }
                    }
                }
            }
            
            connection.commit();  // Commit transaction
            
            // Return a rebuilt BookingData object
            return new BookingData(
                    bookingId,
                    bookingData.flightId(),
                    bookingData.airline(),
                    bookingData.price(),
                    bookingData.adultPassengers(),
                    bookingData.infantPassengers(),
                    bookingData.travelClass(),
                    bookingData.discount(),
                    bookingData.discountReason(),
                    LocalDateTime.now(),
                    bookingData.status(),
                    bookingData.customers(),
                    bookingData.flight(),
                    bookingData.connectionFlights()
            );
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding booking to the database", e);
            if (connection != null) {
                try {
                    connection.rollback();  // Rollback transaction on error
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
                }
            }
            throw new RuntimeException("Failed to add booking", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error closing connection", e);
                }
            }
        }
    }
    
    /**
     * Save flight data to the database if it doesn't already exist.
     */
    private void saveFlightIfNeeded(Connection connection, FlightData flightData) throws SQLException {
        if (flightExists(connection, flightData.id())) {
            LOGGER.info("Flight " + flightData.id() + " already exists in database.");
            return;
        }
        
        LOGGER.info("Creating new flight record for ID: " + flightData.id());
        
        // Parse flight data and insert to database
        java.sql.Timestamp departureTime = null;
        if (flightData.departureScheduledTime() != null) {
            departureTime = java.sql.Timestamp.valueOf(flightData.departureScheduledTime());
        }
        
        java.sql.Timestamp arrivalTime = null;
        if (flightData.arrivalScheduledTime() != null) {
            arrivalTime = java.sql.Timestamp.valueOf(flightData.arrivalScheduledTime());
        }
        
        insertFlightRecord(connection, flightData.id(),
                flightData.departureAirport(), flightData.departureAirportShort(),
                flightData.departureTerminal(), flightData.departureGate(), departureTime,
                flightData.arrivalAirport(), flightData.arrivalAirportShort(),
                flightData.arrivalTerminal(), flightData.arrivalGate(), arrivalTime,
                flightData.duration());
    }
    
    /**
     * Save the main flight using booking data and the FlightData structure
     */
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
                // Extract complete flight details from the flight object sent from frontend
                departureAirport = bookingData.flight().departureAirport();
                departureAirportShort = bookingData.flight().departureAirportShort();
                departureTerminal = bookingData.flight().departureTerminal();
                departureGate = bookingData.flight().departureGate();
                duration = bookingData.flight().duration();
                
                // Handle departure scheduled time
                if (bookingData.flight().departureScheduledTime() != null) {
                    // Convert to SQL timestamp regardless of source type
                    departureTime = java.sql.Timestamp.valueOf(bookingData.flight().departureScheduledTime());
                }
                
                arrivalAirport = bookingData.flight().arrivalAirport();
                arrivalAirportShort = bookingData.flight().arrivalAirportShort();
                arrivalTerminal = bookingData.flight().arrivalTerminal();
                arrivalGate = bookingData.flight().arrivalGate();
                
                // Handle arrival scheduled time
                if (bookingData.flight().arrivalScheduledTime() != null) {
                    // Convert to SQL timestamp regardless of source type
                    arrivalTime = java.sql.Timestamp.valueOf(bookingData.flight().arrivalScheduledTime());
                }
                
                // Check for connection flights and save them
                try {
                    Object connectionFlights = getValue(bookingData.flight(), "connectionFlights");
                    if (connectionFlights instanceof List) {
                        for (Object connectionFlight : (List<?>)connectionFlights) {
                            saveConnectionFlight(connection, connectionFlight);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warning("Error processing connection flights: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Error extracting flight details: " + e.getMessage() + ". Using basic information.");
        }
        
        // If we couldn't extract the data, try to parse from flightId (e.g., "JFK-LAX")
        if (departureAirportShort == null || arrivalAirportShort == null) {
            String[] parts = bookingData.flightId().split("-");
            if (parts.length >= 2) {
                departureAirportShort = parts[0];
                departureAirport = departureAirport != null ? departureAirport : "Airport " + departureAirportShort;
                arrivalAirportShort = parts[1];
                arrivalAirport = arrivalAirport != null ? arrivalAirport : "Airport " + arrivalAirportShort;
            } else {
                // Default values if we can't extract
                departureAirport = "Unknown Departure Airport";
                departureAirportShort = "UNK";
                arrivalAirport = "Unknown Arrival Airport";
                arrivalAirportShort = "UNK";
            }
        }
        
        // Use a default duration if none provided
        if (duration == null) {
            duration = 180; // 3 hours default
        }
        
        LOGGER.info("Saving flight with ID: " + bookingData.flightId() + 
                    ", from: " + departureAirportShort + 
                    ", to: " + arrivalAirportShort +
                    ", duration: " + duration + " minutes");
        
        // Insert flight record with all available information
        insertFlightRecord(connection, bookingData.flightId(), 
                departureAirport, departureAirportShort, departureTerminal, departureGate, departureTime,
                arrivalAirport, arrivalAirportShort, arrivalTerminal, arrivalGate, arrivalTime, duration);
    }

    /**
     * Extract all flight IDs from booking data, including the main flight and any connections
     */
    private List<String> extractAllFlightIds(BookingData bookingData) {
        List<String> flightIds = new ArrayList<>();
        
        // Add the main flight ID
        flightIds.add(bookingData.flightId());
        
        // Look for connection flights in the flight object
        try {
            if (bookingData.flight() != null) {
                Object connectionFlights = getValue(bookingData.flight(), "connectionFlights");
                if (connectionFlights instanceof List) {
                    for (Object connectionFlight : (List<?>)connectionFlights) {
                        String connectionId = (String)getValue(connectionFlight, "id");
                        if (connectionId != null && !connectionId.isEmpty()) {
                            flightIds.add(connectionId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Error extracting connection flight IDs: " + e.getMessage());
        }
        
        LOGGER.info("Extracted flight IDs: " + flightIds);
        return flightIds;
    }
    
    /**
     * Save flight data to the database if it doesn't already exist.
     */
    private void saveFlightIfNeeded(Connection connection, BookingData bookingData, String flightId) throws SQLException {
        if (flightExists(connection, flightId)) {
            LOGGER.info("Flight " + flightId + " already exists in database.");
            return;
        }
        
        LOGGER.info("Creating new flight record for ID: " + flightId);
        
        // For the main flight ID
        if (flightId.equals(bookingData.flightId())) {
            // Extract main flight details and save
            saveMainFlight(connection, bookingData);
        } else {
            // This is a connection flight - create a basic record from the ID
            // since we don't have direct access to connection flight details
            String[] parts = flightId.split("-");
            if (parts.length >= 2) {
                insertFlightRecord(connection, flightId, 
                        "Airport " + parts[0], parts[0], null, null, null,
                        "Airport " + parts[1], parts[1], null, null, null, 180);
            } else {
                // Create a very basic record if we can't extract anything
                insertFlightRecord(connection, flightId, 
                        "Unknown Airport", "UNK", null, null, null,
                        "Unknown Airport", "UNK", null, null, null, 180);
            }
        }
    }
    
    /**
     * Insert a flight record with the given details
     */
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
            
            // Set nullable fields properly
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
            LOGGER.info("Created flight record for ID: " + flightId);
        }
    }

    // Helper to safely extract values from dynamic objects
    private Object getValue(Object obj, String fieldName) {
        try {
            // Simplistic approach - would need to be adapted for real implementation
            java.lang.reflect.Method method = obj.getClass().getMethod(fieldName);
            return method.invoke(obj);
        } catch (Exception e) {
            return null;
        }
    }
    
    // Save a connection flight from the fullOffer data
    private void saveConnectionFlight(Connection connection, Object flightData) {
        try {
            String flightId = (String) getValue(flightData, "id");
            String duration = (String) getValue(flightData, "duration");
            String number = (String) getValue(flightData, "number");
            String carrierCode = (String) getValue(flightData, "carrierCode");
            
            // Extract departure info
            Object departure = getValue(flightData, "departure");
            String depIata = departure != null ? (String) getValue(departure, "iata") : null;
            String depTerminal = departure != null ? (String) getValue(departure, "terminal") : null;
            String depScheduled = departure != null ? (String) getValue(departure, "scheduled") : null;
            
            // Extract arrival info
            Object arrival = getValue(flightData, "arrival");
            String arrIata = arrival != null ? (String) getValue(arrival, "iata") : null;
            String arrTerminal = arrival != null ? (String) getValue(arrival, "terminal") : null;
            String arrScheduled = arrival != null ? (String) getValue(arrival, "scheduled") : null;
            
            // Calculate duration in minutes from PT format
            Integer durationMinutes = null;
            if (duration != null && duration.startsWith("PT")) {
                durationMinutes = parseDurationToMinutes(duration);
            } else {
                durationMinutes = 60; // Default 1 hour
            }
            
            // Insert this flight if it doesn't exist
            if (!flightExists(connection, flightId)) {
                String sql = "INSERT INTO public.flight (id, departure_airport_short, " +
                        "departure_terminal, departure_scheduled_time, arrival_airport_short, " +
                        "arrival_terminal, arrival_scheduled_time, duration) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, flightId);
                    stmt.setString(2, depIata);
                    
                    if (depTerminal != null) stmt.setString(3, depTerminal);
                    else stmt.setNull(3, java.sql.Types.VARCHAR);
                    
                    if (depScheduled != null) {
                        try {
                            java.time.LocalDateTime depDateTime = java.time.LocalDateTime.parse(
                                    depScheduled,
                                    java.time.format.DateTimeFormatter.ISO_DATE_TIME);
                            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(depDateTime));
                        } catch (Exception e) {
                            stmt.setNull(4, java.sql.Types.TIMESTAMP);
                        }
                    } else {
                        stmt.setNull(4, java.sql.Types.TIMESTAMP);
                    }
                    
                    stmt.setString(5, arrIata);
                    
                    if (arrTerminal != null) stmt.setString(6, arrTerminal);
                    else stmt.setNull(6, java.sql.Types.VARCHAR);
                    
                    if (arrScheduled != null) {
                        try {
                            java.time.LocalDateTime arrDateTime = java.time.LocalDateTime.parse(
                                    arrScheduled,
                                    java.time.format.DateTimeFormatter.ISO_DATE_TIME);
                            stmt.setTimestamp(7, java.sql.Timestamp.valueOf(arrDateTime));
                        } catch (Exception e) {
                            stmt.setNull(7, java.sql.Types.TIMESTAMP);
                        }
                    } else {
                        stmt.setNull(7, java.sql.Types.TIMESTAMP);
                    }
                    
                    stmt.setInt(8, durationMinutes);
                    
                    stmt.executeUpdate();
                    LOGGER.info("Created connection flight record for ID: " + flightId);
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Failed to save connection flight: " + e.getMessage());
        }
    }
    
    // Parse duration like PT3H10M to minutes
    private int parseDurationToMinutes(String duration) {
        int minutes = 0;
        
        try {
            // Remove PT prefix
            String time = duration.substring(2);
            
            // Find hours
            int hIndex = time.indexOf('H');
            if (hIndex > 0) {
                minutes += Integer.parseInt(time.substring(0, hIndex)) * 60;
                time = time.substring(hIndex + 1);
            }
            
            // Find minutes
            int mIndex = time.indexOf('M');
            if (mIndex > 0) {
                minutes += Integer.parseInt(time.substring(0, mIndex));
            }
        } catch (Exception e) {
            return 60; // Default 1 hour
        }
        
        return minutes > 0 ? minutes : 60; // Default 1 hour if parsing failed
    }
    
    private int createBooking(Connection connection) throws SQLException {
        String sql = "INSERT INTO public.booking (employeeid, isactive) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, DEFAULT_EMPLOYEE_ID); // In a real app, would use current user ID
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
    
    // New helper method to check if a flight exists
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
        // Log the actual flightId being used for debugging purposes
        LOGGER.info("Linking booking " + bookingId + " to flight with ID: " + flightId);
        
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
            stmt.setInt(3, 0); // Default tariff, would be calculated based on class and discounts
            
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
            return 0; // No address to create
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
        // Check if customer already exists
        if (customer.id() != null && customer.id() > 0) {
            String checkSql = "SELECT id FROM public.customer WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(checkSql)) {
                stmt.setInt(1, customer.id());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // Customer exists, update it
                        updateCustomer(connection, customer);
                        return customer.id();
                    }
                }
            }
        }

        // Create an address first if needed
        int addressId = createAddress(connection, customer);
        
        // Create a new customer
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
        // First check if address needs to be created or updated
        int addressId = 0;
        String checkAddressSql = "SELECT addressid FROM public.customer WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(checkAddressSql)) {
            stmt.setInt(1, customer.id());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Integer existingAddressId = rs.getInt("addressid");
                    if (existingAddressId != null && existingAddressId > 0) {
                        // Update existing address
                        updateAddress(connection, existingAddressId, customer);
                        addressId = existingAddressId;
                    } else {
                        // Create new address
                        addressId = createAddress(connection, customer);
                    }
                }
            }
        }
        
        // Update customer
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
            return; // No address data to update
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
            LOGGER.log(Level.SEVERE, "Error retrieving bookings", e);
            return Collections.emptyList();
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
            LOGGER.log(Level.SEVERE, "Error retrieving booking with ID " + id, e);
        }

        return null;
    }
    
    private BookingData buildBookingData(Connection connection, int bookingId, ResultSet rs) throws SQLException {
        String flightId = rs.getString("flightid");
        boolean isActive = rs.getBoolean("isactive");
        String departureAirport = rs.getString("departure_airport");
        String arrivalAirport = rs.getString("arrival_airport");
        
        // Get customers/tickets for this booking
        List<CustomerData> customers = getCustomersForBooking(connection, bookingId);
        
        // Calculate counts
        int adultCount = 0;
        int infantCount = 0;
        for (CustomerData customer : customers) {
            if (customer.isInfant()) {
                infantCount++;
            } else {
                adultCount++;
            }
        }
        
        // In a real app, we would fetch more details about the flight, prices, etc.
        return new BookingData(
                bookingId,
                flightId,
                "Unknown Airline", // Would be fetched from flight data
                0.0, // Would be calculated based on tickets
                adultCount,
                infantCount,
                "ECONOMY", // Would be fetched from ticket data
                0, // Would be calculated from discounts
                "",
                LocalDateTime.now(), // Would be fetched from booking creation timestamp
                isActive ? "ACTIVE" : "CANCELLED",
                customers
        );
    }

    @Override
    public BookingData update(BookingData bookingData) {
        // In the actual schema, update would focus on changing the active status
        String sql = "UPDATE public.booking SET isactive = ? WHERE id = ?";

        try (Connection connection = db.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            boolean isActive = !"CANCELLED".equalsIgnoreCase(bookingData.status());
            stmt.setBoolean(1, isActive);
            stmt.setInt(2, bookingData.id());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Updating booking failed, no rows affected.");
            }

            return getOne(bookingData.id());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating booking with ID " + bookingData.id(), e);
            throw new RuntimeException("Failed to update booking", e);
        }
    }

    public boolean delete(Integer id) {
        // In practice, we might just set isactive to false instead of deleting
        String sql = "UPDATE public.booking SET isactive = false WHERE id = ?";
        
        try (Connection connection = db.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deactivating booking with ID " + id, e);
            return false;
        }
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
            LOGGER.log(Level.SEVERE, "Error retrieving bookings for customer ID " + customerId, e);
            return Collections.emptyList();
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
}
