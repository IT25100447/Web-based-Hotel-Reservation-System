-- USE SmartStayDB;
-- GO

-- ---------- Branch ----------
CREATE TABLE Branch (
    branch_id       INT IDENTITY(1,1) PRIMARY KEY,
    name            NVARCHAR(100)  NOT NULL,
    address         NVARCHAR(255)  NULL,
    contact_number  NVARCHAR(20)   NULL
);

-- ---------- Staff ----------
CREATE TABLE Staff (
    staff_id         INT IDENTITY(1,1) PRIMARY KEY,
    branch_id        INT            NOT NULL,
    supervisor_id    INT            NULL,         
    name             NVARCHAR(100)  NOT NULL,
    email            NVARCHAR(150)  NOT NULL UNIQUE,
    phone            NVARCHAR(20)   NULL,
    password         NVARCHAR(255)  NOT NULL,
    role             NVARCHAR(30)   NOT NULL
                        CHECK (role IN ('MANAGER','RESERVATIONS','FRONT_OFFICE','GUEST_RELATIONS','FINANCE','IT')),
    hire_date        DATE           NULL,
    status           NVARCHAR(20)   NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE','DEACTIVATED')),
    target_bookings  INT            NULL,   
    access_level     NVARCHAR(30)   NULL,  
    approval_level   NVARCHAR(30)   NULL,  
    response_sla_hrs INT            NULL,  
    CONSTRAINT FK_Staff_Branch     FOREIGN KEY (branch_id)     REFERENCES Branch(branch_id),
    CONSTRAINT FK_Staff_Supervisor FOREIGN KEY (supervisor_id) REFERENCES Staff(staff_id)
);

-- ---------- Room ----------
CREATE TABLE Room (
    room_id      INT IDENTITY(1,1) PRIMARY KEY,
    branch_id    INT            NOT NULL,
    room_number  NVARCHAR(20)   NOT NULL,
    type         NVARCHAR(50)   NOT NULL,
    price        DECIMAL(10,2)  NOT NULL,
    status       NVARCHAR(20)   NOT NULL DEFAULT 'AVAILABLE'
                    CHECK (status IN ('AVAILABLE','OCCUPIED','MAINTENANCE')),
    CONSTRAINT FK_Room_Branch FOREIGN KEY (branch_id) REFERENCES Branch(branch_id),
    CONSTRAINT UQ_Room_Branch_Number UNIQUE (branch_id, room_number)
);

CREATE TABLE RoomImage (
    image_id  INT IDENTITY(1,1) PRIMARY KEY,
    room_id   INT NOT NULL,
    image_url NVARCHAR(500) NOT NULL,
    CONSTRAINT FK_RoomImage_Room FOREIGN KEY (room_id) REFERENCES Room(room_id) ON DELETE CASCADE
);

CREATE TABLE RoomFacility (
    facility_id   INT IDENTITY(1,1) PRIMARY KEY,
    room_id       INT NOT NULL,
    facility_name NVARCHAR(100) NOT NULL,
    CONSTRAINT FK_RoomFacility_Room FOREIGN KEY (room_id) REFERENCES Room(room_id) ON DELETE CASCADE
);

-- ---------- Promotion ----------
CREATE TABLE Promotion (
    promo_id      INT IDENTITY(1,1) PRIMARY KEY,
    name          NVARCHAR(100)  NOT NULL,
    discount_pct  DECIMAL(5,2)   NOT NULL,
    start_date    DATE           NOT NULL,
    end_date      DATE           NOT NULL,
    CHECK (end_date > start_date)
);

CREATE TABLE Room_Promotion (
    room_id  INT NOT NULL,
    promo_id INT NOT NULL,
    PRIMARY KEY (room_id, promo_id),
    CONSTRAINT FK_RP_Room      FOREIGN KEY (room_id)  REFERENCES Room(room_id),
    CONSTRAINT FK_RP_Promotion FOREIGN KEY (promo_id) REFERENCES Promotion(promo_id)
);

-- ---------- Guest ----------
CREATE TABLE Guest (
    guest_id INT IDENTITY(1,1) PRIMARY KEY,
    name     NVARCHAR(100)  NOT NULL,
    email    NVARCHAR(150)  NOT NULL UNIQUE,
    phone    NVARCHAR(20)   NULL,
    address  NVARCHAR(255)  NULL,
    password NVARCHAR(255)  NOT NULL
);

-- ---------- Reservation ----------
CREATE TABLE Reservation (
    reservation_id       INT IDENTITY(1,1) PRIMARY KEY,
    guest_id             INT NOT NULL,
    room_id              INT NOT NULL,
    entered_by_staff_id  INT NULL,   
    booking_date         DATETIME    NOT NULL DEFAULT GETDATE(),
    check_in_date        DATE        NOT NULL,
    check_out_date       DATE        NOT NULL,
    num_guests           INT         NOT NULL DEFAULT 1,
    status               NVARCHAR(20) NOT NULL DEFAULT 'CONFIRMED'
                            CHECK (status IN ('CONFIRMED','MODIFIED','CANCELLED','CHECKED_IN','CHECKED_OUT')),
    CONSTRAINT FK_Reservation_Guest FOREIGN KEY (guest_id) REFERENCES Guest(guest_id),
    CONSTRAINT FK_Reservation_Room  FOREIGN KEY (room_id)  REFERENCES Room(room_id),
    CONSTRAINT FK_Reservation_Staff FOREIGN KEY (entered_by_staff_id) REFERENCES Staff(staff_id),
    CHECK (check_out_date > check_in_date)
);

CREATE TABLE SpecialRequest (
    request_id      INT IDENTITY(1,1) PRIMARY KEY,
    reservation_id  INT NOT NULL,
    type            NVARCHAR(50)  NOT NULL,
    description     NVARCHAR(255) NULL,
    CONSTRAINT FK_SpecialRequest_Reservation FOREIGN KEY (reservation_id)
        REFERENCES Reservation(reservation_id) ON DELETE CASCADE
);

-- ---------- Payment & Invoice ----------
CREATE TABLE Payment (
    payment_id             INT IDENTITY(1,1) PRIMARY KEY,
    reservation_id         INT NOT NULL,
    processed_by_staff_id  INT NULL,
    method                 NVARCHAR(20) NOT NULL
                              CHECK (method IN ('CASH','CARD','ONLINE','BANK_TRANSFER')),
    amount                 DECIMAL(10,2) NOT NULL,
    payment_date           DATETIME NOT NULL DEFAULT GETDATE(),
    reference_number       NVARCHAR(100) NULL,
    status                 NVARCHAR(20) NOT NULL DEFAULT 'PENDING'
                              CHECK (status IN ('PENDING','VERIFIED','FAILED')),
    CONSTRAINT FK_Payment_Reservation FOREIGN KEY (reservation_id) REFERENCES Reservation(reservation_id),
    CONSTRAINT FK_Payment_Staff       FOREIGN KEY (processed_by_staff_id) REFERENCES Staff(staff_id)
);

CREATE TABLE Invoice (
    invoice_id           INT IDENTITY(1,1) PRIMARY KEY,
    reservation_id       INT NOT NULL UNIQUE,
    total_amount         DECIMAL(10,2) NOT NULL,
    outstanding_balance  DECIMAL(10,2) NOT NULL,
    issue_date           DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_Invoice_Reservation FOREIGN KEY (reservation_id) REFERENCES Reservation(reservation_id)
);

-- ---------- Feedback & Refund ----------
CREATE TABLE Feedback (
    feedback_id          INT IDENTITY(1,1) PRIMARY KEY,
    guest_id             INT NOT NULL,
    reservation_id       INT NOT NULL,
    handled_by_staff_id  INT NULL,  
    rating               INT NULL CHECK (rating BETWEEN 1 AND 5),
    comments             NVARCHAR(1000) NULL,
    category             NVARCHAR(50)   NULL,
    status               NVARCHAR(20) NOT NULL DEFAULT 'OPEN'
                            CHECK (status IN ('OPEN','IN_PROGRESS','RESOLVED')),
    submitted_date       DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_Feedback_Guest       FOREIGN KEY (guest_id) REFERENCES Guest(guest_id),
    CONSTRAINT FK_Feedback_Reservation FOREIGN KEY (reservation_id) REFERENCES Reservation(reservation_id),
    CONSTRAINT FK_Feedback_Staff       FOREIGN KEY (handled_by_staff_id) REFERENCES Staff(staff_id)
);

CREATE TABLE Refund (
    refund_id    INT IDENTITY(1,1) PRIMARY KEY,
    feedback_id  INT NOT NULL,
    amount       DECIMAL(10,2) NOT NULL,
    status       NVARCHAR(20) NOT NULL DEFAULT 'PENDING'
                   CHECK (status IN ('PENDING','APPROVED','REJECTED','COMPLETED')),
    refund_date  DATETIME NULL,
    reason       NVARCHAR(255) NULL,
    CONSTRAINT FK_Refund_Feedback FOREIGN KEY (feedback_id) REFERENCES Feedback(feedback_id)
);

-- ---------- Activity Log ----------
CREATE TABLE ActivityLog (
    log_id     INT IDENTITY(1,1) PRIMARY KEY,
    staff_id   INT NOT NULL,
    action     NVARCHAR(255) NOT NULL,
    timestamp  DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_ActivityLog_Staff FOREIGN KEY (staff_id) REFERENCES Staff(staff_id)
);

----- Helpful for common lookup -----
CREATE INDEX IX_Room_Branch_Status   ON Room(branch_id, status);
CREATE INDEX IX_Reservation_Dates    ON Reservation(room_id, check_in_date, check_out_date);
CREATE INDEX IX_Reservation_Guest    ON Reservation(guest_id);
CREATE INDEX IX_Payment_Reservation  ON Payment(reservation_id);
CREATE INDEX IX_Feedback_Status      ON Feedback(status);
