<div align="center">

# ⚙️ RevWorkForce
### Human Resource Management System

*A full-stack Spring Boot workforce management platform*

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Oracle](https://img.shields.io/badge/Oracle%20DB-F80000?style=for-the-badge&logo=oracle&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Tech Stack](#-tech-stack)
- [Features](#-features)
- [User Roles](#-user-roles--permissions)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [URL Routes](#-key-url-routes)
- [Database Entities](#-database-entities)
- [Security](#-security-architecture)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🌟 Overview

RevWorkForce is a full-stack **Human Resource Management System** built with Spring Boot. It provides three dedicated portals — **Admin**, **Manager**, and **Employee** — each with a tailored dashboard and feature set designed around real-world HR workflows.

| Stat | Count |
|------|-------|
| Java Source Files | 77 |
| HTML Templates | 50 |
| Database Entities | 13 |
| User Roles | 3 |
| Core Modules | 10+ |

- 🔐 **Role-Based Access** — Spring Security enforces strict portal isolation
- 🔔 **Real-Time Notifications** — every action triggers instant in-app alerts
- 🌐 **Public Landing Page** — marketing page before login
- 📊 **Live Dashboards** — role-specific stats and analytics

---

## 🛠 Tech Stack

| Layer | Technology | Details |
|-------|-----------|---------|
| Backend | Spring Boot 4.0.3 | MVC architecture, REST + MVC controllers |
| Language | Java 21 | LTS release |
| Security | Spring Security + JWT | BCrypt, role-based access, session management |
| ORM | Hibernate / JPA | Auto schema update, JPQL queries |
| Database | Oracle DB (XEPDB1) | ojdbc11, port 1521 |
| Templates | Thymeleaf 3 | Server-side rendering, layout dialect |
| Frontend | HTML5 / CSS3 / JS | Vanilla JS, Font Awesome 6, Google Fonts (Syne + DM Sans) |
| Build | Maven | spring-boot-maven-plugin, Lombok annotation processor |
| Auth Tokens | jjwt 0.11.5 | jjwt-api, jjwt-impl, jjwt-jackson |

---

## ✨ Features

| Module | Description | Roles |
|--------|-------------|-------|
| 🎯 Goal Management | Employees create goals with priority (High/Medium/Low). Managers view, change priority, update status. Deadline alerts included. | All |
| 📅 Leave Management | Apply for leave with type, dates and reason. Manager approval/rejection with comments. Full history and balance tracking. | All |
| ⭐ Performance Reviews | Structured review cycles between managers and employees. Ratings, comments and historical tracking. | Mgr / Emp |
| 🔔 Notifications | Real-time in-app notifications for goal updates, leave decisions, review assignments and announcements. | All |
| 📣 Announcements | Org-wide and team-level announcements. Admins broadcast company news; managers post team updates. | Admin / Mgr |
| 🗓️ Team Calendar | Visual leave calendar showing team availability. Holiday management included. | Mgr / Admin |
| 📊 Analytics Dashboard | Role-specific dashboards with live stats — active goals, leave counts, pending reviews, team size. | Admin / Mgr |
| 👤 Employee Profiles | Rich profiles with department, designation, profile picture, and personal details. | All |
| 🔐 Secure Auth | BCrypt password hashing, JWT token auth, Spring Security role-based access control. | System |
| 📝 Activity Logs | Admin audit trail of all key actions — who did what and when. | Admin |

---

## 👥 User Roles & Permissions

| Feature | Admin | Manager | Employee |
|---------|:-----:|:-------:|:--------:|
| View own dashboard | ✅ | ✅ | ✅ |
| Manage employees & roles | ✅ | ❌ | ❌ |
| Configure departments | ✅ | ❌ | ❌ |
| Set leave policies | ✅ | ❌ | ❌ |
| View org-wide analytics | ✅ | ❌ | ❌ |
| View audit / activity logs | ✅ | ❌ | ❌ |
| Post announcements | ✅ | ✅ | ❌ |
| Approve / reject leaves | ❌ | ✅ | ❌ |
| View team leave calendar | ❌ | ✅ | ❌ |
| Conduct performance reviews | ❌ | ✅ | ❌ |
| Update team goal priority | ❌ | ✅ | ❌ |
| Set personal goals | ❌ | ✅ | ✅ |
| Apply for leave | ✅ | ✅ | ✅ |
| View own notifications | ✅ | ✅ | ✅ |
| Update own profile | ✅ | ✅ | ✅ |

---

## 📁 Project Structure

```
revworkforce/
├── src/main/java/com/revworkforce/revworkforce/
│   ├── admin/            ← Admin controllers, services, entities
│   ├── auth/             ← JWT, Spring Security, login handlers
│   ├── calendar/         ← Holiday management
│   ├── common/           ← Security config, exception handler, utils
│   ├── employee/         ← Employee, Goal, Leave, Review entities & services
│   ├── manager/          ← Manager controllers, DTO, review service
│   └── notification/     ← Real-time notification system
├── src/main/resources/
│   ├── templates/
│   │   ├── layout/       ← Shared base layouts (admin, manager, employee)
│   │   ├── admin/        ← 14 admin HTML pages
│   │   ├── manager/      ← 12 manager HTML pages
│   │   ├── employee/     ← 9 employee HTML pages
│   │   ├── landing.html  ← Public landing page
│   │   └── login.html    ← Login page
│   ├── static/css/       ← Global stylesheets
│   └── application.properties
└── pom.xml
```

---

## 🚀 Getting Started

### Prerequisites

- Java 21 JDK
- Apache Maven 3.8+
- Oracle Database XE (or any Oracle DB instance)
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/revworkforce.git
cd revworkforce
```

### 2. Configure the Database

Open `src/main/resources/application.properties` and update:

```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521/XEPDB1
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.OracleDialect

spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=20MB
```

### 3. Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

### 4. Open in Browser

| URL | Page |
|-----|------|
| `http://localhost:8080/` | Landing page |
| `http://localhost:8080/login` | Login page |
| `http://localhost:8080/admin/dashboard` | Admin dashboard |
| `http://localhost:8080/manager/dashboard` | Manager dashboard |
| `http://localhost:8080/employee/dashboard` | Employee dashboard |

---

## 🗺 Key URL Routes

### Public
```
GET  /              → Landing page
GET  /login         → Login page
POST /login         → Authenticate user
GET  /auth/forgot-password → Forgot password
```

### Admin  (`/admin/**`)
```
GET  /admin/dashboard      → Admin dashboard
GET  /admin/employees      → Manage all employees
GET  /admin/analytics      → Org-wide analytics
GET  /admin/leaves         → All leave requests
GET  /admin/activity-logs  → Audit logs
```

### Manager  (`/manager/**`)
```
GET  /manager/dashboard                    → Manager dashboard
GET  /manager/goals                        → View team goals
POST /manager/goals/update/:id             → Update team goal status
POST /manager/goals/update-priority/:id    → Update goal priority
GET  /manager/my-goals                     → Manager personal goals
POST /manager/my-goals/create              → Add personal goal
GET  /manager/leaves                       → Pending leave requests
GET  /manager/reviews                      → Performance reviews
```

### Employee  (`/employee/**`)
```
GET  /employee/dashboard       → Employee dashboard
GET  /employee/goals           → View & create goals
POST /employee/goals/create    → Create new goal
GET  /employee/leaves          → View leave history
POST /employee/leaves/apply    → Apply for leave
GET  /employee/reviews         → View performance reviews
```

---

## 🗄 Database Entities

13 JPA entities managed by Hibernate with `ddl-auto=update`.

| Entity | Key Fields |
|--------|-----------|
| `Employee` | id, employeeId, firstName, lastName, email, role, department, designation, manager, status |
| `Goal` | id, employee, description, targetDate, status (GoalStatus), priority (GoalPriority) |
| `Leave` | id, employee, leaveType, startDate, endDate, status, reason, managerComments |
| `LeaveBalance` | id, employee, paidLeave, sickLeave, casualLeave |
| `LeavePolicy` | id, leaveType, maxDays, carryForward |
| `PerformanceReview` | id, employee, manager, rating, comments, reviewDate, status |
| `Notification` | id, employee, message, isRead, createdAt |
| `Announcement` | id, title, content, createdBy, createdAt |
| `Department` | id, name, description |
| `Designation` | id, name, department |
| `ActivityLog` | id, userEmail, role, action, description, timestamp |
| `Holiday` | id, name, date, type |
| `LeaveRequest` | id, employee, type, dates, reason, status |

---

## 🔐 Security Architecture

### Authentication
- Form-based login via Spring Security at `POST /login`
- Passwords hashed with **BCryptPasswordEncoder** (strength 10)

### Route Guards
```
/admin/**     → ROLE_ADMIN only
/manager/**   → ROLE_MANAGER only
/employee/**  → ROLE_EMPLOYEE only
/             → Public (no auth required)
/login        → Public (no auth required)
/auth/**      → Public (no auth required)
```

- CSRF protection disabled (stateless JWT model)
- Custom `LoginSuccessHandler` redirects to role-specific dashboard on login

---

## ⚠️ Known Issues & Notes

- **SLF4J warning** — resolved by removing `spring-boot-starter-log4j2` from `pom.xml` (Logback is included by default via Spring Boot starters)
- **Schema mode** — `ddl-auto=update` is for development only; change to `validate` for production
- **Credentials** — externalise `application.properties` secrets via environment variables in production
- **File uploads** — limited to 20MB by default; configurable in `application.properties`

---

## 🤝 Contributing

Contributions are welcome!

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Commit your changes: `git commit -m "feat: add your feature"`
4. Push to the branch: `git push origin feature/your-feature-name`
5. Open a Pull Request against the `main` branch


---

<div align="center">

Built with ❤️ using **Spring Boot · Oracle DB · Thymeleaf · Java 17**

</div>
