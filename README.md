# camp-app-backend

Backend REST API for a summer camp management application built with Java and Spring Boot

![Status](https://img.shields.io/badge/status-in%20progress-yellow)

## Planned Business Logic

> This project is currently under development. The following section describes the planned business workflow and rules.

The Camp App will manage camper applications, approvals, payments, documents, and confirmed participation in specific
camp periods.

### User Roles

The application will support three user roles:

* **Guardian:** Registers in the system, manages campers, submits applications, uploads documents, completes the medical
  questionnaire, makes payments, and downloads the final camper declaration.
* **Admin:** Reviews applications, corrects submitted information when necessary, approves or rejects applications,
  manages camp periods, assigns leaders, and cancels participation.
* **Leader:** Has read-only access to confirmed campers belonging to the camp periods assigned to them.

A secondary guardian may be included in an application but does not need to register as a system user.

### Application Workflow

1. A guardian creates an application for one of their campers and selects a camp period.
2. While the application is in `DRAFT` status, the guardian can update its information.
3. The guardian completes the medical questionnaire, uploads the required documents, and submits the application.
4. After submission, the application is locked for the guardian. Only an admin can make corrections.
5. The admin reviews the application and either approves or rejects it.
6. A rejected application includes a mandatory rejection reason.
7. An approved application temporarily reserves a place for seven days and becomes eligible for payment.
8. The guardian completes the payment through Stripe.
9. The place is confirmed only after the backend receives a successful Stripe webhook.
10. The guardian can then download the final camper declaration, and the leaders assigned to the selected camp period
    are notified.

### Payment Rules

* An unsuccessful payment attempt does not cancel the approval or release the temporarily reserved place.
* The guardian may retry the payment until the seven-day deadline expires.
* If payment is not completed before the deadline, the approval expires automatically and the place becomes available
  again.
* An admin may approve the application again if places are still available.
* Payment and refund results are confirmed only through Stripe webhooks.
* Card details are handled exclusively by Stripe and are never stored by the Camp App.

### Camp Period Rules

Each camp period will define:

* start and end dates,
* eligible gender,
* minimum and maximum completed school grade,
* participation cost,
* maximum capacity,
* application deadline,
* open or closed status.

A camper may participate in different camp periods and submit new applications in future years. However:

* a camper cannot have two active applications for the same period,
* a camper cannot participate in camp periods with overlapping dates,
* eligibility is validated using gender and completed school grade,
* a place is permanently confirmed only after successful payment.

### Cancellation and Refunds

Only an admin can cancel an application or confirmed participation.

When confirmed participation is cancelled:

* the place becomes available again,
* the guardian and the assigned leaders are notified,
* any refund is processed through Stripe,
* the Camp App is updated through the corresponding Stripe webhook.

### Documents and Medical Information

Applications may contain personal documents and medical information relating to minors.

Access will therefore be restricted:

* guardians can access only their own applications,
* admins can access all applications,
* leaders can access only confirmed campers from their assigned periods.

Sensitive information and attached documents will not be included in emails. Email notifications will direct users back
to the authenticated application.

### Notifications

Brevo will be used for transactional emails related to:

* application submission,
* approval or rejection,
* payment reminders,
* payment deadline expiration,
* successful payment,
* confirmed participation,
* cancellation and refund updates.

### Planned Reports

The administration area will provide reports such as:

* complete camper and guardian application details,
* approved applications,
* rejected applications,
* approved applications awaiting payment,
* confirmed campers by camp period,
* available and occupied places.

The system will preserve status history and important administrative changes to provide a clear audit trail.