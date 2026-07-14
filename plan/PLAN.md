## Description of modules

### users
#### Purpose:
Manage own user's profile, cannot modify other users profile, only it's own. Can create new users and register them in aws cognito with the role "CLIENT" as default, it also should register the user in the database with same default role "CLIENT".

When an user is created and saved in the database, it should also register in AWS cognito. If the user's registration fails when inserting into database or registering to AWS cognito, then it should be deleted to avoid future conflicts, use "transactional" so you can rollback when inserting into the database when an error occours, in the case of AWS cognito, you should delete the account manually via aws sdk that the project has.

NOTE: email cannot be the same as other's user, it's an unique value, sames as DNI and cellphone number.
#### use of cases
- edit their own personal information (needs token)
- see their own personal information (needs token)
- create an account <- should register user to database and aws cognito (no needed)
- create an account with role "STAFF" and affiliate the user with a given event_id, it uses "users" and "staff" table (needs token and only users with role "OWNER" can do it)
- desaffiliate staff user from an event by the given "event_id", this involves delete register from table "staff" where user_id matchs with user's id and event_id matchs with the given event_id
- create an account with role "OWNER", only users with role "ADMIN" can do such action. (requires token)
- delete an account, only users with role "ADMIN" can do such action. (requires token)

#### note:
users could upload or change their profile picture, or just delete it.

### auth
#### Purpose:
It is responsible for exposing middlewares or functions of type "x" to be used in endpoints to validate the received token in the request header, as well as authorization strictly based on the role defined in the token's payload. This module does not handle login or user registration. It only handles token validation and user authorization through the credentials provided by AWS Cognito. It should not make requests to the Cognito API to validate, but rather use the credentials provided by Cognito in order to validate the tokens from request header, since sending to validate with AWS Cognito API would be expensive.

This module must serve to expose its functions to the controllers or services (as deemed necessary) of other modules.

NOTE: If Quarkus with a specific AWS SDK already provides these functions, omit the creation of these validation functions and/or annotations and use them directly; however, it must comply with the requested requirements.

#### Use of cases
- Validation of requests with the token in the header.
- Validation of authorization with the assigned role via the payload of the token provided in the header.

### events
#### Purpose
This module is responsible for managing the `"event"`, `"event_category"`, and `"event_images"` tables.

The `"event"` table records events for which users with the `"CLIENT"` role can register via the `"qr_ticket"` table (which will be developed later, not in this module). The user who creates the event (roles: `"OWNER"` and `"ADMIN"`) can upload multiple images in the form, up to a maximum of 8 images. These images follow a specific sequence established during file upload, which is stored using the `"index"` column. If the form reaches the 8-image limit, the first image will have index 0, up to index 7, following the sequence in which the images were received. The user can also assign a category to the event, with a limit of exactly one category per event.

An event creation request can define the following attributes:
*   `title`
*   `description`
*   `max_people`
*   `address`
*   `available`
*   `finished`
*   `start_date`
*   `finish_date`
*   `owner_id` (retrieved from the ID of the user making the request, details below)
*   `category_id` (ID of an existing `"event_category"`)

To inspect the table attributes for this module, review `plan/ARCHITECTURE.MD`.

#### How to retrieve the "owner_id"?
Extract the email of the logged-in user from the provided token. Use this email to fetch the user ID. Once you have the user ID, you must implement a method to retrieve the `id_event_owner` from the `"event_owner"` table by querying the `"user_id"` with the ID obtained via the token email.

There is already an existing function named `"currentEmail"` in `src/main/java/com/app/teleticket/auth/service/impl/AuthServiceImpl.java` that extracts the email from the token.

#### Use Cases

role: CLIENT (token required)
*   Users with the `"CLIENT"` role can only read active events.
*   Select an event.

role: OWNER (token required)
*   Create event.
*   Edit event.
*   Cancel event.
*   View all events associated with them, regardless of their status.

role: STAFF
*   View all events associated with them, regardless of their status.
*   Edit description and category fields only.

role: ADMIN
*   All permissions within this module.
*   Create category.

#### Notes
1.  Users with the `"CLIENT"` role can search for events using the following filters:
    *   `title`
    *   `start_date`
    *   `finish_date`
    *   `category_id`
2.  The `"available"` filter must always be set to `true` when a `"CLIENT"` requests an event listing.
3.  Events can include multiple images, strictly restricted to `jpg`, `jpeg`, and `png` formats.
4.  Pagination must be strictly controlled; each page lists up to 12 events. For example, if there are 25 events, there are 3 pages available. The user cannot access page 4 since there are not enough events to populate it.

---

#### Considerations
*   Avoid empty catch blocks. For example:

    **Incorrect:**
    ```java
    try {
        // ... code logic
    } catch (Exception e) {
        // empty
    }
    ```

    **Correct:**
    ```java
    try {
        // ... code logic
    } catch (Exception e) {
        // ... error handling logic or controlled error response using the `src/main/java/com/app/teleticket/common/dto/ApiResponse.java` format
    }
    ```

*   Not all forms use Form Data; some must handle standard JSON payloads.
*   Use Form Data exclusively for forms that include image uploads.
*   All HTTP responses, whether successful or containing errors, must return the `src/main/java/com/app/teleticket/common/dto/ApiResponse.java` object.

### qr
#### purpose
To validate that a "CLIENT" user has registered for a specific event ("x" event), a QR code image is required for verification. This QR code image must store two pieces of information: the user's ID and the event's ID. 

The user sends the QR code image to an unauthenticated endpoint (no token required), and the system returns a boolean indicating whether the QR code is valid or not. Therefore, this endpoint must perform the validation of the incoming QR.

**How is validation determined?** 
Upon scanning, the system extracts the two IDs from the QR code and uses them to search for the `qr_ticket` entity. Once found, it must verify that `already_applied` is `false`. If it is `true`, or if the QR ticket is not found at all, the QR is invalid. 

When a QR code is sent for validation and successfully processed, the system must update `already_applied` to `true`. By default, when a `qr_ticket` is created, `already_applied` is always `false`.

The `qr_ticket` entity must store:
- `qr_url`: The URL of the QR code image.
- `qr_key`: The unique file name stored in AWS S3.
- the rest of columns, check `C:\Users\Roberto\Documents\projects\esan\teleticket-lite\demo\plan\ARCHITECTURE.MD`

**When is a `qr_ticket` generated?**
It is generated when a user registers for an event. Upon registration, the system must create the `qr_ticket` entity, saving the client ID (the user who registered) and the event ID. It must also generate the QR code image and upload it to AWS S3. Simultaneously, it must create an `event_assistants` entity, which records the registration date, the user ID, and the event ID.

#### use cases
You must implement the following endpoints along with their respective services:
- **Register for an Event**: Requires token authentication; restricted only to users with the "CLIENT" role.
- **Scan/Validate QR**: Public endpoint (does not require authentication).
- **List Event Attendees**: Retrieves data from the `event_assistants` table. Requires authentication; restricted only to users with the "OWNER" role.

#### Considerations
- The service generates the QR code image locally and then uploads it to S3, returning the QR object entity (with DTO) with its S3 URL in the response.
- Any new packages that you'll add must not be deprecated.