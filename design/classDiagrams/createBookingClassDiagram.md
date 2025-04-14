```mermaid
classDiagram
  class Booking {
    +id: String
    +flightId: String
    +customers: List~CustomerData~
    +discount: Int
    +reason: String
    +finalPrice: Decimal
    +status: String
    +getTotalPrice(): Decimal
  }

  class CustomerData {
    +firstName: String
    +lastName: String
    +email: String
    +phone: String
    +passportNumber: String
  }

  class Flight {
    +id: String
    +airline: String
    +departureTime: DateTime
    +arrivalTime: DateTime
    +departureAirport: String
    +arrivalAirport: String
    +price: Decimal
    +status: String
  }

  class BookingManagerImpl {
    -bookingRepository: BookingRepository
    +create(data: BookingData): Booking
    +reserve(data: BookingData): Booking
    +cancel(id: String): void
    +getAll(): List~Booking~
  }

  class BookingRepository {
    +save(booking: Booking): Booking
    +delete(id: String): void
    +getOne(id: String): Booking
    +getAll(): List~Booking~
  }

  class BookingRepositoryImpl {
    -bookings: List~Booking~
  }

  Booking "1" --> "1..*" CustomerData
  Booking "1" --> "1" Flight
  BookingManagerImpl --> Booking
  BookingManagerImpl --> BookingRepository
  BookingRepositoryImpl ..|> BookingRepository
  BookingRepositoryImpl --> Booking
