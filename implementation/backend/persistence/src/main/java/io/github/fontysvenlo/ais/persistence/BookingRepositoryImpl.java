package io.github.fontysvenlo.ais.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Override
    public List<BookingData> list() {
        String sql = "SELECT b.id, b.isactive, bf.flightid, f.departure_airport, f.arrival_airport " +
                "FROM public.booking b " +
                "JOIN public.booking_flight bf ON b.id = bf.bookingid " +
                "JOIN public.flight f ON bf.flightid = f.id";

        try (Connection connection = db.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet results = stmt.executeQuery()) {

            List<BookingData> bookings = new ArrayList<>();
            while (results.next()) {
                int bookingId = results.getInt("id");
                bookings.add(buildBookingData(connection, bookingId, results));
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
            try (ResultSet results = stmt.executeQuery()) {
                if (results.next()) {
                    return buildBookingData(connection, id, results);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get booking with ID " + id, e);
        }
        return null;
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
            try (ResultSet results = stmt.executeQuery()) {
                List<BookingData> bookings = new ArrayList<>();
                while (results.next()) {
                    int bookingId = results.getInt("id");
                    bookings.add(buildBookingData(connection, bookingId, results));
                }
                return bookings;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get bookings for customer ID " + customerId, e);
        }
    }

    private BookingData buildBookingData(Connection connection, int bookingId, ResultSet results) throws SQLException {
        String flightId = results.getString("flightid");
        boolean isActive = results.getBoolean("isactive");
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
                bookingId, flightId, "Unknown Airline", 0.0,
                adultCount, infantCount, "ECONOMY", 0, "",
                LocalDateTime.now(), isActive ? "ACTIVE" : "CANCELLED", customers
        );
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
            List<CustomerData> customers = new ArrayList<>();
            try (ResultSet results = stmt.executeQuery()) {
                while (results.next()) {
                    customers.add(new CustomerData(
                            results.getInt("id"),
                            getString(results, "firstname"),
                            getString(results, "lastname"),
                            getString(results, "email"),
                            getString(results, "phonenumber"),
                            getString(results, "street"),
                            getString(results, "housenumber"),
                            getString(results, "city"),
                            getString(results, "country"),
                            results.getBoolean("isinfant")
                    ));
                }
            }
            return customers;
        }
    }
    
    private String getString(ResultSet results, String columnName) throws SQLException {
        String value = results.getString(columnName);
        return value != null ? value : "";
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
            try (ResultSet results = stmt.executeQuery()) {
                if (results.next()) {
                    Map<String, Object> customerData = new HashMap<>();
                    customerData.put("id", results.getInt("id"));
                    customerData.put("firstName", results.getString("firstname"));
                    customerData.put("lastName", results.getString("lastname"));
                    customerData.put("email", results.getString("email"));
                    customerData.put("phone", results.getString("phonenumber"));
                    customerData.put("isInfant", results.getBoolean("isinfant"));
                    
                    if (results.getObject("street") != null) {
                        customerData.put("street", results.getString("street"));
                        customerData.put("houseNumber", results.getString("housenumber"));
                        customerData.put("city", results.getString("city"));
                        customerData.put("country", results.getString("country"));
                    }
                    return customerData;
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find customer by email", e);
        }
    }

    @Override
    public Map<String, Object> add(Map<String, Object> bookingMap) {
        Connection connection = null;
        try {
            connection = db.getConnection();
            connection.setAutoCommit(false);

            // Extract flight IDs and pricing information
            List<String> flightIds = extractFlightIds(bookingMap);
            Map<String, Integer> flightPrices = extractFlightPrices(bookingMap);
            
            // Save all flight data
            for (String id : flightIds) {
                saveFlightIfNotExists(connection, bookingMap, id);
            }
            
            // Create booking record
            int bookingId = createBooking(connection);
            
            // Link booking to flights
            for (String id : flightIds) {
                linkBookingToFlight(connection, bookingId, id);
            }
            
            // Create tickets for customers
            createTicketsForCustomers(connection, bookingMap, flightIds, flightPrices);

            connection.commit();
            
            // Prepare result
            Map<String, Object> result = new HashMap<>();
            result.put("id", bookingId);
            result.put("allFlightIds", flightIds);
            result.put("airline", MapUtils.getString(bookingMap, "airline"));
            result.put("status", MapUtils.getString(bookingMap, "status", "CONFIRMED"));
            result.put("adultPassengers", MapUtils.getInt(bookingMap, "adultPassengers", 1));
            result.put("infantPassengers", MapUtils.getInt(bookingMap, "infantPassengers", 0));
            
            return result;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    e.addSuppressed(ex);
                }
            }
            throw new RuntimeException("Failed to add booking: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    // Ignore
                }
            }
        }
    }

    private List<String> extractFlightIds(Map<String, Object> bookingMap) {
        List<String> flightIds = new ArrayList<>();
        
        // Get flights from mainFlights array (primary source)
        List<Map<String, Object>> mainFlights = MapUtils.getMapList(bookingMap, "mainFlights");
        if (mainFlights != null) {
            for (Map<String, Object> flight : mainFlights) {
                String id = MapUtils.getString(flight, "id");
                if (id != null && !flightIds.contains(id)) {
                    flightIds.add(id);
                }
            }
        }
        
        // Alternative sources if mainFlights is empty
        if (flightIds.isEmpty()) {
            // Try outbound and return flight
            Map<String, Object> outboundFlight = MapUtils.getMap(bookingMap, "outboundFlight");
            if (outboundFlight != null) {
                String id = MapUtils.getString(outboundFlight, "id");
                if (id != null) flightIds.add(id);
            }
            
            Map<String, Object> returnFlight = MapUtils.getMap(bookingMap, "returnFlight");
            if (returnFlight != null) {
                String id = MapUtils.getString(returnFlight, "id");
                if (id != null) flightIds.add(id);
            }
            
            // Try single flight object
            if (flightIds.isEmpty()) {
                Map<String, Object> flight = MapUtils.getMap(bookingMap, "flight");
                if (flight != null) {
                    String id = MapUtils.getString(flight, "id");
                    if (id != null) flightIds.add(id);
                } else {
                    // Try direct flightId
                    String flightId = MapUtils.getString(bookingMap, "flightId");
                    if (flightId != null) flightIds.add(flightId);
                }
            }
        }
        
        return flightIds;
    }
    
    private Map<String, Integer> extractFlightPrices(Map<String, Object> bookingMap) {
        Map<String, Integer> prices = new HashMap<>();
        double basePrice = MapUtils.getDouble(bookingMap, "price", 0);

        // Try to get prices from Amadeus trip data
        Map<String, Object> flight = MapUtils.getMap(bookingMap, "flight");
        if (flight != null) {
            Map<String, Object> fullOffer = MapUtils.getMap(flight, "fullOffer");
            if (fullOffer != null) {
                List<Map<String, Object>> trips = MapUtils.getMapList(fullOffer, "trips");
                if (trips != null) {
                    for (Map<String, Object> trip : trips) {
                        double tripPrice = MapUtils.getDouble(trip, "price", 0);
                        List<Map<String, Object>> tripFlights = MapUtils.getMapList(trip, "flights");
                        
                        if (tripFlights != null && !tripFlights.isEmpty()) {
                            // Calculate per-flight price
                            int pricePerFlight = (int)((tripPrice * 100) / tripFlights.size());
                            
                            for (Map<String, Object> f : tripFlights) {
                                String flightId = createFlightIdFromObject(f);
                                if (flightId != null) {
                                    prices.put(flightId, pricePerFlight);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Fallback to prices in mainFlights
        List<Map<String, Object>> mainFlights = MapUtils.getMapList(bookingMap, "mainFlights");
        if (mainFlights != null) {
            for (Map<String, Object> f : mainFlights) {
                String id = MapUtils.getString(f, "id");
                if (id != null && !prices.containsKey(id)) {
                    double flightPrice = MapUtils.getDouble(f, "price", basePrice);
                    prices.put(id, (int)(flightPrice * 100));
                }
            }
        }
        
        return prices;
    }
    
    private String createFlightIdFromObject(Map<String, Object> flight) {
        String id = MapUtils.getString(flight, "id");
        if (id != null) return id;
        
        String number = MapUtils.getString(flight, "number", "0");
        
        // Try to get from departure/arrival
        Map<String, Object> departure = MapUtils.getMap(flight, "departure");
        Map<String, Object> arrival = MapUtils.getMap(flight, "arrival");
        if (departure != null && arrival != null) {
            String depCode = MapUtils.getString(departure, "iata");
            String arrCode = MapUtils.getString(arrival, "iata");
            if (depCode != null && arrCode != null) {
                return number + "-" + depCode + "-" + arrCode;
            }
        }
        
        return null;
    }
    
    private void saveFlightIfNotExists(Connection connection, Map<String, Object> bookingMap, String flightId) throws SQLException {
        if (flightExists(connection, flightId)) {
            return;
        }
        
        // Find flight data from various sources
        Map<String, Object> flightData = findFlightDataById(bookingMap, flightId);
        if (flightData == null) {
            // Create minimal flight record with defaults
            createMinimalFlight(connection, flightId);
            return;
        }
        
        // Extract flight details
        String depAirport = MapUtils.getString(flightData, "departureAirport");
        String depAirportShort = MapUtils.getString(flightData, "departureAirportShort");
        String depTerminal = MapUtils.getString(flightData, "departureTerminal");
        String depGate = MapUtils.getString(flightData, "departureGate");
        String arrAirport = MapUtils.getString(flightData, "arrivalAirport");
        String arrAirportShort = MapUtils.getString(flightData, "arrivalAirportShort");
        String arrTerminal = MapUtils.getString(flightData, "arrivalTerminal");
        String arrGate = MapUtils.getString(flightData, "arrivalGate");
        
        // Parse times if available
        String depTimeStr = MapUtils.getString(flightData, "departureScheduledTime");
        String arrTimeStr = MapUtils.getString(flightData, "arrivalScheduledTime");
        java.sql.Timestamp depTime = parseTime(depTimeStr);
        java.sql.Timestamp arrTime = parseTime(arrTimeStr);
        
        // Get duration
        Integer duration = MapUtils.getInt(flightData, "duration", 180);
        
        // Set defaults for missing fields
        if (depAirportShort == null || arrAirportShort == null) {
            String[] parts = flightId.split("-");
            if (parts.length >= 3) {
                depAirportShort = parts[1];
                arrAirportShort = parts[2];
            }
        }
        
        if (depAirport == null && depAirportShort != null) {
            depAirport = "Airport " + depAirportShort;
        }
        
        if (arrAirport == null && arrAirportShort != null) {
            arrAirport = "Airport " + arrAirportShort;
        }
        
        // Insert flight record
        insertFlight(connection, flightId, depAirport, depAirportShort, depTerminal, depGate, depTime,
                arrAirport, arrAirportShort, arrTerminal, arrGate, arrTime, duration);
    }
    
    private Map<String, Object> findFlightDataById(Map<String, Object> bookingMap, String flightId) {
        // Check mainFlights
        List<Map<String, Object>> mainFlights = MapUtils.getMapList(bookingMap, "mainFlights");
        if (mainFlights != null) {
            for (Map<String, Object> flight : mainFlights) {
                if (flightId.equals(MapUtils.getString(flight, "id"))) {
                    return flight;
                }
            }
        }
        
        // Check other sources
        Map<String, Object>[] flightSources = new Map[] {
            MapUtils.getMap(bookingMap, "outboundFlight"),
            MapUtils.getMap(bookingMap, "returnFlight"),
            MapUtils.getMap(bookingMap, "flight")
        };
        
        for (Map<String, Object> source : flightSources) {
            if (source != null && flightId.equals(MapUtils.getString(source, "id"))) {
                return source;
            }
        }
        
        // Check full offer
        Map<String, Object> flight = MapUtils.getMap(bookingMap, "flight");
        if (flight != null) {
            Map<String, Object> fullOffer = MapUtils.getMap(flight, "fullOffer");
            if (fullOffer != null) {
                List<Map<String, Object>> trips = MapUtils.getMapList(fullOffer, "trips");
                if (trips != null) {
                    for (Map<String, Object> trip : trips) {
                        List<Map<String, Object>> tripFlights = MapUtils.getMapList(trip, "flights");
                        if (tripFlights != null) {
                            for (Map<String, Object> f : tripFlights) {
                                String id = MapUtils.getString(f, "id");
                                String constructedId = createFlightIdFromObject(f);
                                if (flightId.equals(id) || flightId.equals(constructedId)) {
                                    return convertAmadeusFlightToFlightMap(f);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    private Map<String, Object> convertAmadeusFlightToFlightMap(Map<String, Object> amadeusFlightData) {
        Map<String, Object> result = new HashMap<>();
        
        // Copy direct fields
        result.put("id", MapUtils.getString(amadeusFlightData, "id"));
        result.put("number", MapUtils.getString(amadeusFlightData, "number"));
        result.put("duration", parseDuration(MapUtils.getString(amadeusFlightData, "duration")));
        
        // Process departure info
        Map<String, Object> departure = MapUtils.getMap(amadeusFlightData, "departure");
        if (departure != null) {
            result.put("departureAirportShort", MapUtils.getString(departure, "iata"));
            result.put("departureAirport", "Airport " + MapUtils.getString(departure, "iata"));
            result.put("departureScheduledTime", MapUtils.getString(departure, "scheduled"));
            result.put("departureTerminal", MapUtils.getString(departure, "terminal"));
        }
        
        // Process arrival info
        Map<String, Object> arrival = MapUtils.getMap(amadeusFlightData, "arrival");
        if (arrival != null) {
            result.put("arrivalAirportShort", MapUtils.getString(arrival, "iata"));
            result.put("arrivalAirport", "Airport " + MapUtils.getString(arrival, "iata"));
            result.put("arrivalScheduledTime", MapUtils.getString(arrival, "scheduled"));
            result.put("arrivalTerminal", MapUtils.getString(arrival, "terminal"));
        }
        
        return result;
    }
    
    private Integer parseDuration(String isoDuration) {
        if (isoDuration == null || !isoDuration.startsWith("PT")) {
            return 180; // Default 3 hours
        }
        
        try {
            String time = isoDuration.substring(2);
            int minutes = 0;
            
            int hIndex = time.indexOf('H');
            if (hIndex > 0) {
                minutes += Integer.parseInt(time.substring(0, hIndex)) * 60;
                time = time.substring(hIndex + 1);
            }
            
            int mIndex = time.indexOf('M');
            if (mIndex > 0) {
                minutes += Integer.parseInt(time.substring(0, mIndex));
            }
            
            return Math.max(minutes, 60); // Minimum 1 hour
        } catch (Exception e) {
            return 180; // Default on error
        }
    }
    
    private void createMinimalFlight(Connection connection, String flightId) throws SQLException {
        String depAirportShort = "UNK";
        String arrAirportShort = "UNK";
        
        String[] parts = flightId.split("-");
        if (parts.length >= 3) {
            depAirportShort = parts[1];
            arrAirportShort = parts[2];
        }
        
        insertFlight(connection, flightId, 
                "Airport " + depAirportShort, depAirportShort, null, null, null,
                "Airport " + arrAirportShort, arrAirportShort, null, null, null, 180);
    }
    
    private java.sql.Timestamp parseTime(String timeStr) {
        if (timeStr == null) return null;
        try {
            LocalDateTime dateTime = LocalDateTime.parse(timeStr);
            return java.sql.Timestamp.valueOf(dateTime);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean flightExists(Connection connection, String flightId) throws SQLException {
        String sql = "SELECT 1 FROM public.flight WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, flightId);
            try (ResultSet results = stmt.executeQuery()) {
                return results.next();
            }
        }
    }

    private void insertFlight(Connection connection, String flightId,
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
            setNullableString(stmt, 4, departureTerminal);
            setNullableString(stmt, 5, departureGate);
            setNullableTimestamp(stmt, 6, departureTime);
            stmt.setString(7, arrivalAirport);
            stmt.setString(8, arrivalAirportShort);
            setNullableString(stmt, 9, arrivalTerminal);
            setNullableString(stmt, 10, arrivalGate);
            setNullableTimestamp(stmt, 11, arrivalTime);
            stmt.setInt(12, duration);
            stmt.executeUpdate();
        }
    }

    private void setNullableString(PreparedStatement stmt, int index, String value) throws SQLException {
        if (value != null) stmt.setString(index, value);
        else stmt.setNull(index, java.sql.Types.VARCHAR);
    }
    
    private void setNullableTimestamp(PreparedStatement stmt, int index, java.sql.Timestamp value) throws SQLException {
        if (value != null) stmt.setTimestamp(index, value);
        else stmt.setNull(index, java.sql.Types.TIMESTAMP);
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

    private void linkBookingToFlight(Connection connection, int bookingId, String flightId) throws SQLException {
        String sql = "INSERT INTO public.booking_flight (bookingid, flightid) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            stmt.setString(2, flightId);
            stmt.executeUpdate();
        }
    }

    private void createTicketsForCustomers(Connection connection, Map<String, Object> bookingMap, 
            List<String> flightIds, Map<String, Integer> flightPrices) throws SQLException {
        
        List<Map<String, Object>> customers = MapUtils.getMapList(bookingMap, "customers");
        if (customers == null || customers.isEmpty()) return;
        
        double discountPercent = MapUtils.getDouble(bookingMap, "discount", 0);
        double basePrice = MapUtils.getDouble(bookingMap, "price", 0);
        
        for (Map<String, Object> customer : customers) {
            int customerId = saveOrGetCustomerId(connection, customer);
            boolean isInfant = MapUtils.getBoolean(customer, "isInfant", false);
            
            for (String flightId : flightIds) {
                // Get price (default to base price if not found)
                int priceCents = flightPrices.getOrDefault(flightId, (int)(basePrice * 100));
                
                // Apply discount
                if (discountPercent > 0) {
                    priceCents = (int)(priceCents * (1 - (discountPercent / 100)));
                }
                
                // Infants fly free
                if (isInfant) {
                    priceCents = 0;
                }
                
                createTicket(connection, customerId, flightId, priceCents);
            }
        }
    }

    private int saveOrGetCustomerId(Connection connection, Map<String, Object> customer) throws SQLException {
        // Check if customer has ID
        Integer customerId = MapUtils.getInt(customer, "id", null);
        if (customerId != null && customerExists(connection, customerId)) {
            updateCustomer(connection, customer);
            return customerId;
        }
        
        // Check by email
        String email = MapUtils.getString(customer, "email");
        if (email != null && !email.isEmpty()) {
            Map<String, Object> existingCustomer = findCustomerByEmail(email);
            if (existingCustomer != null) {
                customerId = (Integer) existingCustomer.get("id");
                customer.put("id", customerId);
                updateCustomer(connection, customer);
                return customerId;
            }
        }
        
        // Create new customer
        return createCustomer(connection, customer);
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

    private void updateCustomer(Connection connection, Map<String, Object> customer) throws SQLException {
        Integer customerId = MapUtils.getInt(customer, "id", null);
        if (customerId == null) return;
        
        // Get address ID
        Integer addressId = getCustomerAddressId(connection, customerId);
        
        // Create/update address if we have address data
        String street = MapUtils.getString(customer, "street");
        if (street != null && !street.isEmpty()) {
            String houseNumber = MapUtils.getString(customer, "houseNumber");
            String city = MapUtils.getString(customer, "city");
            String country = MapUtils.getString(customer, "country");
            
            if (addressId != null && addressId > 0) {
                updateAddress(connection, addressId, street, houseNumber, city, country);
            } else {
                addressId = createAddress(connection, street, houseNumber, city, country);
            }
        }
        
        // Update customer
        String sql = "UPDATE public.customer SET firstname = ?, lastname = ?, email = ?, " + 
                     "phonenumber = ?, addressid = ?, isinfant = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, MapUtils.getString(customer, "firstName"));
            stmt.setString(2, MapUtils.getString(customer, "lastName"));
            stmt.setString(3, MapUtils.getString(customer, "email"));
            stmt.setString(4, MapUtils.getString(customer, "phone"));
            
            if (addressId != null && addressId > 0) {
                stmt.setInt(5, addressId);
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            
            stmt.setBoolean(6, MapUtils.getBoolean(customer, "isInfant", false));
            stmt.setInt(7, customerId);
            stmt.executeUpdate();
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
    
    private int createCustomer(Connection connection, Map<String, Object> customer) throws SQLException {
        // Create address if provided
        Integer addressId = null;
        String street = MapUtils.getString(customer, "street");
        if (street != null && !street.isEmpty()) {
            String houseNumber = MapUtils.getString(customer, "houseNumber");
            String city = MapUtils.getString(customer, "city");
            String country = MapUtils.getString(customer, "country");
            addressId = createAddress(connection, street, houseNumber, city, country);
        }
        
        // Create customer
        String sql = "INSERT INTO public.customer (firstname, lastname, email, phonenumber, addressid, isinfant) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, MapUtils.getString(customer, "firstName"));
            stmt.setString(2, MapUtils.getString(customer, "lastName"));
            stmt.setString(3, MapUtils.getString(customer, "email"));
            stmt.setString(4, MapUtils.getString(customer, "phone"));
            
            if (addressId != null && addressId > 0) {
                stmt.setInt(5, addressId);
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            
            stmt.setBoolean(6, MapUtils.getBoolean(customer, "isInfant", false));
            
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
    
    private Integer createAddress(Connection connection, String street, String houseNumber, 
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
    
    private void updateAddress(Connection connection, int addressId, String street, String houseNumber, 
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
    
    private Map<String, Object> findAddressByComponents(String street, String houseNumber, String city, String country) {
        if (street == null || houseNumber == null || city == null || country == null) {
            return null;
        }
        
        String sql = "SELECT * FROM public.address WHERE LOWER(street) = LOWER(?) " +
                     "AND LOWER(housenumber) = LOWER(?) AND LOWER(city) = LOWER(?) " +
                     "AND LOWER(country) = LOWER(?)";
        
        try (Connection connection = db.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, street.trim().toLowerCase());
            stmt.setString(2, houseNumber.trim().toLowerCase());
            stmt.setString(3, city.trim().toLowerCase());
            stmt.setString(4, country.trim().toLowerCase());
            
            try (ResultSet results = stmt.executeQuery()) {
                if (results.next()) {
                    Map<String, Object> addressData = new HashMap<>();
                    addressData.put("id", results.getInt("id"));
                    addressData.put("street", results.getString("street"));
                    addressData.put("houseNumber", results.getString("housenumber"));
                    addressData.put("city", results.getString("city"));
                    addressData.put("country", results.getString("country"));
                    return addressData;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find address", e);
        }
    }
    
    private int createTicket(Connection connection, int customerId, String flightId, int priceCents) throws SQLException {
        String sql = "INSERT INTO public.ticket (flightid, customerid, tariff) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, flightId);
            stmt.setInt(2, customerId);
            stmt.setInt(3, priceCents);
            
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
    
    // Utility class for map operations
    private static class MapUtils {
        public static String getString(Map<String, Object> map, String key) {
            return getString(map, key, null);
        }
        
        public static String getString(Map<String, Object> map, String key, String defaultValue) {
            if (map == null || !map.containsKey(key)) return defaultValue;
            Object value = map.get(key);
            return value != null ? value.toString() : defaultValue;
        }
        
        public static Integer getInt(Map<String, Object> map, String key, Integer defaultValue) {
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
        
        public static Double getDouble(Map<String, Object> map, String key, double defaultValue) {
            if (map == null || !map.containsKey(key)) return defaultValue;
            Object value = map.get(key);
            if (value == null) return defaultValue;
            
            try {
                if (value instanceof Number) {
                    return ((Number)value).doubleValue();
                } else {
                    return Double.parseDouble(value.toString());
                }
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        
        public static Boolean getBoolean(Map<String, Object> map, String key, Boolean defaultValue) {
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
        public static Map<String, Object> getMap(Map<String, Object> map, String key) {
            if (map == null || !map.containsKey(key)) return null;
            Object value = map.get(key);
            return value instanceof Map ? (Map<String, Object>)value : null;
        }
        
        @SuppressWarnings("unchecked")
        public static List<Map<String, Object>> getMapList(Map<String, Object> map, String key) {
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
    }
}
